package com.cedarxuesong.serverlocalizer.utils.translation;

import com.cedarxuesong.serverlocalizer.utils.ai.*;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemTranslationOnTick {
    private static final String TAG = "ItemTranslationOnTick";
    private static int tickCounter = 0;
    private static final int TRIGGER_INTERVAL = 100; // 5秒
    
    // 翻译线程池
    private final ExecutorService translationExecutor = Executors.newSingleThreadExecutor();
    
    // 正在翻译的条目缓存
    private final Set<String> translatingKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    // 是否正在翻译
    private boolean isTranslating = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            
            if (tickCounter >= TRIGGER_INTERVAL & ModConfig.getInstance().isItemTranslationEnabled()) {
                onTrigger();
                tickCounter = 0; // 重置计数器
            }
        }
    }

    /**
     * 每10秒触发一次的事件
     */
    private void onTrigger() {
        // 检查是否启用物品翻译
        ModConfig modConfig = ModConfig.getInstance();
        if (!modConfig.isItemTranslationEnabled()) {
            return;
        }
        
        if (isTranslating) {
            mylog.log(TAG, "上一次翻译任务尚未完成，跳过本次检查");
            return;
        }
        
        // 获取物品翻译管理器
        ItemTranslationManager translationManager = ModuleTranslationManager.getInstance()
                .getTranslationManager(ItemTranslationConfig.TranslationModule.ITEM);
        
        // 检查未翻译的条目
        Map<String, String> untranslatedEntries = new HashMap<>();
        for (Map.Entry<String, String> entry : translationManager.getTranslationMap().entrySet()) {
            String original = entry.getKey();
            String translated = entry.getValue();
            
            // 如果该条目正在翻译中，跳过
            if (translatingKeys.contains(original)) {
                continue;
            }
            
            // 检查是否需要翻译
            if (translated == null || translated.isEmpty()) {
                untranslatedEntries.put(original, original); // key 和 value 都是原文
            }
        }
        
        // 如果有未翻译的条目，启动翻译线程
        if (!untranslatedEntries.isEmpty()) {
            isTranslating = true;
            startTranslation(untranslatedEntries);
        }
    }
    
    /**
     * 启动翻译线程
     * @param untranslatedEntries 未翻译的条目
     */
    private void startTranslation(Map<String, String> untranslatedEntries) {
        mylog.log(TAG, "发现 " + untranslatedEntries.size() + " 条未翻译的条目，开始翻译...");
        
        CompletableFuture.runAsync(() -> {
            try {
                // 获取API配置
                ModConfig modConfig = ModConfig.getInstance();
                String apiName = "ItemTranslationApi";
                
                // 准备翻译批次
                List<Map.Entry<String, String>> entries =
                        new ArrayList<>(untranslatedEntries.entrySet());

                // 每批处理15个条目
                int batchSize = 15;
                for (int i = 0; i < entries.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, entries.size());
                    List<Map.Entry<String, String>> batch =
                            entries.subList(i, endIndex);

                    // 构建翻译请求
                    JsonObject translationRequest = new JsonObject();
                    JsonArray textsArray = new JsonArray();

                    for (Map.Entry<String, String> entry : batch) {
                        String original = entry.getKey();

                        // 添加到正在翻译的缓存
                        translatingKeys.add(original);

                        JsonObject textObj = new JsonObject();
                        textObj.addProperty("key", original); // 使用原文作为key
                        textObj.addProperty("content", original);
                        textsArray.add(textObj);
                    }
                    
                    translationRequest.add("texts", textsArray);
                    
                    // 构建消息
                    List<Message> messages = new ArrayList<>();
                    messages.add(new Message("system", modConfig.getSystemPrompt(apiName)));
                    messages.add(new Message("user", "/no_think 需要翻译的内容：\n" + translationRequest.toString()));
                    
                    // 发送翻译请求
                    OpenAIResponse response = OpenAIClient.sendRequestWithConfig(apiName, messages);
                    
                    if (response.isSuccess()) {
                        // 解析翻译结果
                        String content = response.getContent();
                        processTranslationResponse(content, batch);

                        // 累加Token使用量
                        Usage usage = response.getUsage();
                        if (usage != null) {
                            modConfig.addAccumulatedTokens(apiName, usage.getTotalTokens());
                        }
                        
                        // 每批次处理完成后保存
                        ModuleTranslationManager.getInstance().saveAllTranslations();
                        mylog.log(TAG, "已保存当前批次翻译 (" + batch.size() + " 条)");
                    } else {
                        mylog.log(TAG, "翻译请求失败: " + response.getError());
                        if (Minecraft.getMinecraft()!=null){
                            Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages("翻译请求失败: "+response.getError());
                        }
                    }
                    
                    // 从正在翻译的缓存中移除已处理的条目
                    for (Map.Entry<String, String> entry : batch) {
                        translatingKeys.remove(entry.getKey());
                    }
                    
                    // 每批次之间暂停1秒
                    Thread.sleep(500);
                }
                
                mylog.log(TAG, "翻译任务完成");
                
            } catch (Exception e) {
                mylog.error(TAG, "翻译过程中发生错误", e);
            } finally {
                isTranslating = false;
            }
        }, translationExecutor);
    }
    
    /**
     * 清理内容中的思考链
     * @param content 原始内容
     * @return 清理后的内容
     */
    private String cleanThinkChain(String content) {
        if (content == null) {
            return null;
        }
        // 移除<think>标签及其内容
        return content.replaceAll("<think>[\\s\\S]*?</think>", "").trim();
    }

    /**
     * 处理翻译响应
     * @param content 响应内容
     * @param batch 当前批次的条目
     */
    private void processTranslationResponse(String content, List<Map.Entry<String, String>> batch) {
        try {
            // 清理思考链
            content = cleanThinkChain(content);
            
            // 获取翻译管理器
            ItemTranslationManager translationManager = ModuleTranslationManager.getInstance()
                    .getTranslationManager(ItemTranslationConfig.TranslationModule.ITEM);
            
            // 解析JSON响应
            Gson gson = new Gson();
            JsonElement jsonElement = gson.fromJson(content, JsonElement.class);
            JsonArray translatedArray;
            
            // 处理不同的JSON格式
            if (jsonElement.isJsonArray()) {
                translatedArray = jsonElement.getAsJsonArray();
            } else if (jsonElement.isJsonObject()) {
                // 如果是对象，尝试获取choices数组
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("choices") && jsonObject.get("choices").isJsonArray()) {
                    JsonArray choices = jsonObject.getAsJsonArray("choices");
                    if (choices.size() > 0 && choices.get(0).isJsonObject()) {
                        JsonObject firstChoice = choices.get(0).getAsJsonObject();
                        if (firstChoice.has("message") && firstChoice.get("message").isJsonObject()) {
                            String messageContent = firstChoice.getAsJsonObject("message")
                                    .get("content").getAsString();
                            // 从消息内容中解析JSON数组
                            try {
                                translatedArray = gson.fromJson(messageContent, JsonArray.class);
                            } catch (Exception e) {
                                mylog.log(TAG, "无法从消息内容解析JSON数组，原始内容: " + messageContent);
                                return;
                            }
                        } else {
                            mylog.log(TAG, "响应格式不正确，缺少message字段");
                            return;
                        }
                    } else {
                        mylog.log(TAG, "choices数组为空或格式不正确");
                        return;
                    }
                } else {
                    mylog.log(TAG, "响应格式不正确，缺少choices数组");
                    return;
                }
            } else {
                mylog.log(TAG, "响应格式不正确，既不是数组也不是对象");
                return;
            }
            
            if (translatedArray != null) {
                for (JsonElement element : translatedArray) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    
                    JsonObject translatedObj = element.getAsJsonObject();
                    if (!translatedObj.has("key")) {
                        continue;
                    }
                    
                    String key = translatedObj.get("key").getAsString();
                    String translated = null;
                    
                    // 尝试不同的字段名
                    if (translatedObj.has("translated")) {
                        translated = translatedObj.get("translated").getAsString();
                    } else if (translatedObj.has("content")) {
                        translated = translatedObj.get("content").getAsString();
                    }
                    
                    if (translated == null) {
                        mylog.log(TAG, "翻译对象缺少translated或content字段: " + translatedObj);
                        continue;
                    }
                    
                    // 更新翻译
                    // key 在这里就是原文
                    if (key != null) {
                        translationManager.addOrUpdateTranslation(key, translated);
                    }
                }
            }
        } catch (Exception e) {
            mylog.error(TAG, "处理翻译响应时发生错误", e);
            mylog.log(TAG, "原始响应内容: " + content);
        }
    }
    
    /**
     * 关闭翻译线程池
     */
    public void shutdown() {
        translationExecutor.shutdown();
    }
}