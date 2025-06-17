package com.cedarxuesong.serverlocalizer.mixins.mixinGUI;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatLine.class)
public interface mixinChatLine {
    @Accessor("lineString")
    void setLineString(IChatComponent component);
    
    @Accessor("lineString")
    IChatComponent getLineString();
} 