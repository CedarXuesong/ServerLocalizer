package com.cedarxuesong.serverlocalizer.mixins.mixinGUI;

import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.translation.ChatMessageCache;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.UUID;

@Mixin(GuiNewChat.class)
public class mixinGuiChat {
    @Unique
    private static final String ServerLocalizer$TAG = "ChatMixin";

    @ModifyArg(
        method = "printChatMessageWithOptionalDeletion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiNewChat;setChatLine(Lnet/minecraft/util/IChatComponent;IIZ)V"
        ),
        index = 0
    )
    private IChatComponent onChatMessage(IChatComponent component) {
        try {
            if (!ModConfig.getInstance().isChatTranslationEnabled()) {
                return component;
            }
            // 为每条消息生成唯一ID
            String messageId = UUID.randomUUID().toString();
            
            // 缓存原始消息和ID的对应关系
            ChatMessageCache.getInstance().cacheMessage(messageId, component);
            
            // 创建一个新的聊天组件用于添加[T]标记
            IChatComponent translationMark = new ChatComponentText(" §r§7[T]§r");
            
            // 设置[T]的样式
            ChatStyle style = new ChatStyle();
            style.setColor(EnumChatFormatting.GRAY); // 设置灰色
            
            // 设置悬浮文本
            IChatComponent hoverText = new ChatComponentText("§b点击翻译此消息 §r§aby Serverlocalizer \n§r§7MessageID: "+messageId);
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            
            // 设置点击事件，使用RUN_COMMAND类型并传递消息ID
            style.setChatClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, 
                "/serverlocalizer translate " + messageId
            ));
            
            translationMark.setChatStyle(style);
            
            // 将[T]标记添加到原始消息后面
            return component.appendSibling(translationMark);
        } catch (Exception e) {
            mylog.error(ServerLocalizer$TAG, "处理聊天消息时发生错误", e);
            return component;
        }
    }
}

