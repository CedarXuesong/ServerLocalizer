package com.cedarxuesong.serverlocalizer.utils.commands;

import com.cedarxuesong.serverlocalizer.mixins.mixinGUI.mixinChatLine;
import com.cedarxuesong.serverlocalizer.mixins.mixinGUI.mixinGuiNewChatAccess;
import com.cedarxuesong.serverlocalizer.utils.ai.*;
import com.cedarxuesong.serverlocalizer.utils.translation.ChatMessageCache;
import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import com.cedarxuesong.serverlocalizer.utils.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.event.HoverEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTranslateCommand extends CommandBase {
    private static final String TAG = "ChatTranslateCommand";
    
    // API配置常量
    private static final String CHATTRANSLATION_API = "ChatTranslationApi";
    
    // 存储正在翻译的消息ID
    private static final ConcurrentHashMap<String, Boolean> translatingMessages = new ConcurrentHashMap<>();
    
    // 存储消息ID和对应的加载消息组件的映射，用于准确定位需要替换的消息
    private static final ConcurrentHashMap<String, IChatComponent> messageComponentToReplaceMap = new ConcurrentHashMap<>();
    
    // 线程池，用于管理翻译任务
    private static final ExecutorService translationExecutor = Executors.newFixedThreadPool(5);

    @Override
    public String getCommandName() {
        return "translate";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/translate <messageId>";
    }

    /**
     * 处理子命令
     * @param sender 命令发送者
     * @param args 子命令参数
     */
    public void processSubCommand(ICommandSender sender, String[] args) {
        try {
            if (args.length < 1) {
                showUsage(sender);
                return;
            }else if (!ModConfig.getInstance().isChatTranslationEnabled()) {
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(Lang.translate("command.serverlocalizer.translate.disabled")));
                return;
            }

            String messageId = args[0];
            
            // 获取原始消息
            IChatComponent originalMessage = ChatMessageCache.getInstance().getCachedMessage(messageId);

            if (originalMessage != null) {
                // 检查该消息是否已经在翻译中，使用原子操作避免竞态条件
                if (translatingMessages.putIfAbsent(messageId, true) != null) {
                    mylog.log(TAG, "该消息已在翻译队列中: " + messageId);
                    return;
                }

                if (ModConfig.getInstance().isChatStreamEnabled()) {
                    // 使用流式翻译
                    handleStreamTranslation(messageId, originalMessage);
                } else {
                    // 使用普通翻译
                    handleStandardTranslation(messageId, originalMessage);
                }
            } else {
                mylog.error(TAG, "未找到ID对应的消息: " + messageId, new Exception("消息ID无效"));
            }
        } catch (Exception e) {
            mylog.error(TAG, "处理翻译消息命令时发生错误", e);
        }
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        // 这个方法不再直接处理命令，改为由ServerLocalizerCommand调度
        showUsage(sender);
    }

    private void showUsage(ICommandSender sender) {
        IChatComponent usageMessage = new ChatComponentText(Lang.translate("command.serverlocalizer.translate.usage"));
        sender.addChatMessage(usageMessage);
    }
    
    /**
     * 调用OpenAI API进行翻译，并返回完整的响应对象
     * @param text 要翻译的文本
     * @return API响应对象
     */
    private OpenAIResponse translateTextWithUsage(String text) {
        ModConfig config = ModConfig.getInstance();
        String apiKey = config.getApiKey(CHATTRANSLATION_API);

        if (apiKey == null || apiKey.isEmpty()) {
            return new OpenAIResponse(null, Lang.translate("command.serverlocalizer.translate.api_key_empty"), 400);
        }

        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new Message("system", config.getSystemPrompt(CHATTRANSLATION_API)));
            messages.add(new Message("user", text));

            return OpenAIClient.sendRequestWithConfig(CHATTRANSLATION_API, messages);
        } catch (Exception e) {
            mylog.error(TAG, "调用翻译API时发生错误", e);
            return new OpenAIResponse(null, String.format(Lang.translate("command.serverlocalizer.translate.api_error"), e.getMessage()), 500);
        }
    }
    
    /**
     * 清理翻译输出，移除思考链和多余换行
     */
    private String cleanTranslationOutput(String content) {
        // 移除<think>...</think>思考链
        Pattern thinkPattern = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
        Matcher thinkMatcher = thinkPattern.matcher(content);
        content = thinkMatcher.replaceAll("");

        //删除残留标记
        content = content.replaceAll("\\[[TC]]","");
        
        return content.trim();
    }
    
    /**
     * 使用Mixin接口在聊天行列表中替换消息
     */
    private boolean replaceMessageInChatLines(GuiNewChat chatGUI, IChatComponent original, IChatComponent replacement) {
        try {
            mixinGuiNewChatAccess accessor = (mixinGuiNewChatAccess) chatGUI;
            
            List<ChatLine> chatLines = accessor.getChatLines();
            List<ChatLine> drawnChatLines = accessor.getDrawnChatLines();
            
            boolean replaced = false;
            
            // 替换chatLines中的消息
            replaced |= replaceInChatLineList(chatLines, original, replacement);
            
            // 替换drawnChatLines中的消息
            replaced |= replaceInChatLineList(drawnChatLines, original, replacement);
            
            return replaced;
        } catch (Exception e) {
            mylog.error(TAG, "替换消息时发生错误", e);
            return false;
        }
    }
    
    /**
     * 在ChatLine列表中替换消息
     */
    private boolean replaceInChatLineList(List<ChatLine> chatLines, IChatComponent original, IChatComponent replacement) {
        boolean replaced = false;
        
        // 使用同步块确保线程安全
        synchronized (chatLines) {
            for (ChatLine chatLine : chatLines) {
                try {
                    mixinChatLine accessor = (mixinChatLine) chatLine;
                    IChatComponent lineComponent = accessor.getLineString();
                    
                    if (lineComponent != null && lineComponent == original) {
                        // 替换消息
                        accessor.setLineString(replacement);
                        replaced = true;
                    }
                } catch (Exception e) {
                    mylog.error(TAG, "替换ChatLine中的消息时发生错误", e);
                }
            }
        }
        
        return replaced;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // 所有玩家都可以使用此命令
    }
    
    /**
     * 关闭线程池，应在模组卸载时调用
     */
    public static void shutdown() {
        translationExecutor.shutdown();
    }

    /**
     * 刷新聊天，同时保持滚动位置
     * @param chatGUI The chat GUI instance.
     */
    private void refreshChatWithScroll(GuiNewChat chatGUI) {
        mixinGuiNewChatAccess accessor = (mixinGuiNewChatAccess) chatGUI;
        int scrollPos = accessor.getScrollPos();

        chatGUI.refreshChat();

        accessor.setScrollPos(scrollPos);
    }

    /**
     * 处理标准（非流式）翻译
     */
    private void handleStandardTranslation(String messageId, IChatComponent originalMessage) {
        // 获取原始文本
        String originalText = originalMessage.getFormattedText();
        
        // 获取Minecraft实例
        Minecraft mc = Minecraft.getMinecraft();
        GuiNewChat chatGUI = mc.ingameGUI.getChatGUI();
        
        // 创建加载消息
        IChatComponent loadingMessage = new ChatComponentText(Lang.translate("command.serverlocalizer.translate.translating"));
        loadingMessage.getChatStyle().setColor(EnumChatFormatting.GRAY);
        
        // 存储消息ID和加载消息的映射关系
        messageComponentToReplaceMap.put(messageId, loadingMessage);
        
        try {
            // 替换为加载消息
            if (replaceMessageInChatLines(chatGUI, originalMessage, loadingMessage)) {
                refreshChatWithScroll(chatGUI);
                mylog.log(TAG, "显示翻译加载中: " + messageId);
                
                // 提交翻译任务
                translationExecutor.submit(() -> {
                    try {
                        OpenAIResponse response = translateTextWithUsage(originalText);
                        String translatedText = Lang.translate("command.serverlocalizer.translate.failed");
                        Usage usage = null;

                        if (response.isSuccess()) {
                            translatedText = cleanTranslationOutput(response.extractMessageContent());
                            usage = response.getUsage();
                        } else {
                            translatedText = String.format(Lang.translate("command.serverlocalizer.translate.failed_with_error"), response.getError());
                        }

                        IChatComponent translatedMessage = new ChatComponentText(translatedText);
                        translatedMessage.getChatStyle().setColor(EnumChatFormatting.GREEN);

                        if (usage != null) {
                            IChatComponent hoverText = new ChatComponentText(usage.toString());
                            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
                            translatedMessage.getChatStyle().setChatHoverEvent(hoverEvent);
                        }
                        
                        final IChatComponent finalTranslatedMessage = translatedMessage;
                        mc.addScheduledTask(() -> {
                            IChatComponent currentLoadingMessage = messageComponentToReplaceMap.get(messageId);
                            if (currentLoadingMessage != null) {
                                if (replaceMessageInChatLines(chatGUI, currentLoadingMessage, finalTranslatedMessage)) {
                                    refreshChatWithScroll(chatGUI);
                                    mylog.log(TAG, "翻译完成: " + messageId);
                                } else {
                                    mylog.error(TAG, "更新翻译消息失败: " + messageId, null);
                                }
                            }
                            translatingMessages.remove(messageId);
                            messageComponentToReplaceMap.remove(messageId);
                        });
                    } catch (Exception e) {
                        handleTranslationError(messageId, e.getMessage());
                    }
                });
            } else {
                translatingMessages.remove(messageId);
                mylog.error(TAG, "未能找到要翻译的消息: " + messageId, null);
            }
        } catch (Exception e) {
            translatingMessages.remove(messageId);
            mylog.error(TAG, "替换消息时发生错误: " + messageId, e);
        }
    }

    /**
     * 处理流式翻译
     */
    private void handleStreamTranslation(String messageId, IChatComponent originalMessage) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiNewChat chatGUI = mc.ingameGUI.getChatGUI();

        // 初始状态显示 "连接中..."
        IChatComponent connectingMessage = new ChatComponentText(Lang.translate("command.serverlocalizer.translate.connecting"));
        if (replaceMessageInChatLines(chatGUI, originalMessage, connectingMessage)) {
            refreshChatWithScroll(chatGUI);
            messageComponentToReplaceMap.put(messageId, connectingMessage);

            StringBuilder fullText = new StringBuilder();
            final boolean[] isThinking = {false};
            final boolean[] hasReceivedContent = {false};

            List<Message> messages = new ArrayList<>();
            messages.add(new Message("system", ModConfig.getInstance().getSystemPrompt(CHATTRANSLATION_API)));
            messages.add(new Message("user", originalMessage.getFormattedText()));

            OpenAIClient.sendStreamRequestWithConfig(CHATTRANSLATION_API, messages,
                // onChunk
                (chunk) -> {
                    fullText.append(chunk);
                    String currentFullText = fullText.toString();

                    IChatComponent oldComp = messageComponentToReplaceMap.get(messageId);
                    if (oldComp == null) return;

                    IChatComponent newComp;
                    // 检查是否处于思考阶段
                    if (currentFullText.contains("<think>") && !currentFullText.contains("</think>")) {
                        if (!isThinking[0]) {
                            isThinking[0] = true;
                        }
                        // 更新思考中的Token计数
                        String thinkingText = String.format(Lang.translate("command.serverlocalizer.translate.thinking"), fullText.length());
                        newComp = new ChatComponentText(thinkingText);

                        // 提取思考内容并创建悬浮事件
                        String thinkingContent = "";
                        int thinkStartIndex = currentFullText.indexOf("<think>");
                        if (thinkStartIndex != -1) {
                            thinkingContent = currentFullText.substring(thinkStartIndex + "<think>".length());
                        }
                        IChatComponent hoverTextComponent = new ChatComponentText(thinkingContent);
                        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTextComponent);
                        newComp.getChatStyle().setChatHoverEvent(hoverEvent);

                        messageComponentToReplaceMap.put(messageId, newComp);
                        mc.addScheduledTask(() -> {
                            if(replaceMessageInChatLines(chatGUI, oldComp, newComp)) {
                                refreshChatWithScroll(chatGUI);
                            }
                        });
                        return;
                    }
                    
                    isThinking[0] = false; // 离开思考阶段

                    if (!hasReceivedContent[0]) {
                        hasReceivedContent[0] = true;
                    }

                    newComp = new ChatComponentText(cleanTranslationOutput(currentFullText));
                    newComp.getChatStyle().setColor(EnumChatFormatting.GREEN);

                    messageComponentToReplaceMap.put(messageId, newComp);
                    mc.addScheduledTask(() -> {
                        if(replaceMessageInChatLines(chatGUI, oldComp, newComp)) {
                            refreshChatWithScroll(chatGUI);
                        }
                    });
                },
                // onComplete
                (usage) -> {
                    mc.addScheduledTask(() -> {
                        IChatComponent finalComponent = messageComponentToReplaceMap.get(messageId);
                        if (finalComponent != null && usage != null) {
                            IChatComponent hoverText = new ChatComponentText("§ePromptTokens: "+usage.getPromptTokens()+"\n§eTotalTokens: "+usage.getTotalTokens()+"\n§eCompletionTokens: "+usage.getCompletionTokens());
                            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
                            finalComponent.getChatStyle().setChatHoverEvent(hoverEvent);
                            refreshChatWithScroll(chatGUI);
                        }
                        translatingMessages.remove(messageId);
                        messageComponentToReplaceMap.remove(messageId);
                        mylog.log(TAG, "流式翻译完成: " + messageId);
                    });
                },
                // onError
                (error) -> {
                    // 如果在收到任何内容之前就出错，则替换"连接中..."消息
                    IChatComponent componentToReplace = hasReceivedContent[0] ? messageComponentToReplaceMap.get(messageId) : connectingMessage;
                    handleTranslationError(messageId, error, componentToReplace);
                }
            );
        } else {
            translatingMessages.remove(messageId);
            mylog.error(TAG, "未能找到要翻译的消息 (stream): " + messageId, null);
        }
    }

    /**
     * 处理翻译过程中的错误
     */
    private void handleTranslationError(String messageId, String errorMessageStr, IChatComponent componentToReplace) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (componentToReplace != null) {
                IChatComponent errorMessage = new ChatComponentText(String.format(Lang.translate("command.serverlocalizer.translate.failed_with_error"), errorMessageStr));
                
                if (replaceMessageInChatLines(Minecraft.getMinecraft().ingameGUI.getChatGUI(), componentToReplace, errorMessage)) {
                    refreshChatWithScroll(Minecraft.getMinecraft().ingameGUI.getChatGUI());
                }
            } else {
                 // 如果找不到要替换的组件，直接打印错误信息
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(String.format(Lang.translate("command.serverlocalizer.translate.failed_with_id"), messageId, errorMessageStr)));
            }
            translatingMessages.remove(messageId);
            messageComponentToReplaceMap.remove(messageId);
        });
    }

    private void handleTranslationError(String messageId, String errorMessageStr) {
        handleTranslationError(messageId, errorMessageStr, messageComponentToReplaceMap.get(messageId));
    }
} 