package com.cedarxuesong.serverlocalizer.mixins.mixinGUI;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GuiNewChat.class)
public interface mixinGuiNewChatAccess {
    @Accessor("chatLines")
    List<ChatLine> getChatLines();
    
    @Accessor("drawnChatLines")
    List<ChatLine> getDrawnChatLines();

    @Accessor("scrollPos")
    int getScrollPos();

    @Accessor("scrollPos")
    void setScrollPos(int scrollPos);
} 