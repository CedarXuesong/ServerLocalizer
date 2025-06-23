package com.cedarxuesong.serverlocalizer.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ModernButton extends GuiButton {

    private static final double HOVER_ANIMATION_SPEED = 0.03;
    private float hoverAnimation = 0.0f;
    private long lastFrameTime = 0L;

    private int idleColor;
    private int hoverColor;

    public ModernButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        this(buttonId, x, y, widthIn, heightIn, buttonText, Theme.COLOR_BUTTON_IDLE, Theme.COLOR_BUTTON_HOVER);
    }

    public ModernButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, int idleColor, int hoverColor) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.lastFrameTime = System.currentTimeMillis();
        this.idleColor = idleColor;
        this.hoverColor = hoverColor;
    }

    public void setColors(int idleColor, int hoverColor) {
        this.idleColor = idleColor;
        this.hoverColor = hoverColor;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            long deltaTime = System.currentTimeMillis() - this.lastFrameTime;
            this.lastFrameTime = System.currentTimeMillis();
            if (deltaTime > 50L) deltaTime = 50L;

            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            float targetAnimation = (this.hovered && this.enabled) ? 1.0f : 0.0f;

            // Using an easing function for smooth animation
            float amountToMove = (float) (1.0 - Math.exp(-HOVER_ANIMATION_SPEED * deltaTime));

            if (Math.abs(targetAnimation - hoverAnimation) > 0.01f) {
                hoverAnimation += (targetAnimation - hoverAnimation) * amountToMove;
            } else {
                hoverAnimation = targetAnimation;
            }

            int backgroundColor = GuiUtils.interpolateColor(this.idleColor, this.hoverColor, this.hoverAnimation);
            GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 5, backgroundColor);

            int textColor = packedFGColour != 0 ? packedFGColour : Theme.COLOR_TEXT_WHITE;
            this.drawCenteredString(mc.fontRendererObj, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, textColor);

            if (!this.enabled) {
                GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 5, Theme.COLOR_DISABLED_OVERLAY);
            }
        }
    }
} 