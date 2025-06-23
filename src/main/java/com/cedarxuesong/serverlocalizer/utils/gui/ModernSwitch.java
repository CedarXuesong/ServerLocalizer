package com.cedarxuesong.serverlocalizer.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModernSwitch extends GuiButton {

    private static final double SWITCH_ANIMATION_SPEED = 0.04;
    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;
    private float knobAnimation = 0.0f; // 0.0 for off, 1.0 for on
    private long lastFrameTime;
    private boolean isPressed;

    private final Minecraft mc = Minecraft.getMinecraft();

    public ModernSwitch(int buttonId, int x, int y, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        super(buttonId, x, y, 36, 20, ""); // Width 36, Height 20
        this.getter = getter;
        this.setter = setter;
        this.lastFrameTime = System.currentTimeMillis();
    }

    public boolean isOn() {
        return this.getter.get();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - this.lastFrameTime;
            this.lastFrameTime = currentTime;
            if (deltaTime > 50L) deltaTime = 50L;

            float targetAnimation = this.isOn() ? 1.0f : 0.0f;
            if (Math.abs(targetAnimation - knobAnimation) > 0.01f) {
                float amountToMove = (float) (1.0 - Math.exp(-SWITCH_ANIMATION_SPEED * deltaTime));
                knobAnimation += (targetAnimation - knobAnimation) * amountToMove;
            } else {
                knobAnimation = targetAnimation;
            }

            // Draw background track
            int trackY = this.yPosition + (this.height - 14) / 2;
            int backgroundColor = GuiUtils.interpolateColor(Theme.COLOR_ELEMENT_BACKGROUND, Theme.COLOR_ACCENT, knobAnimation);
            GuiUtils.drawRoundedRect(this.xPosition, trackY, this.width, 14, 7, backgroundColor);

            // Draw handle
            float handleDiameter = 10;
            float handleX = this.xPosition + 2 + (this.width - 4 - handleDiameter) * knobAnimation;
            float handleY = this.yPosition + (this.height - handleDiameter) / 2;
            GuiUtils.drawRoundedRect(handleX, handleY, handleDiameter, handleDiameter, handleDiameter / 2f, Theme.COLOR_TEXT_WHITE);
        
            if (!this.enabled) {
                GuiUtils.drawRoundedRect(this.xPosition, trackY, this.width, 14, 7, Theme.COLOR_DISABLED_OVERLAY);
            }
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.enabled && this.visible && mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height) {
            this.isPressed = true;
            return true;
        }
        return false;
    }
    
    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        if (this.isPressed) {
            this.isPressed = false;
            // Check if the mouse is still over the button on release
            if (this.enabled && this.visible && mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height) {
                this.setter.accept(!this.isOn());
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F));
            }
        }
    }
}