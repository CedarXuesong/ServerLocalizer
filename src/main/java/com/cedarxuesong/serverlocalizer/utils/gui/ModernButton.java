package com.cedarxuesong.serverlocalizer.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ModernButton extends GuiButton {

    private float hoverAnimation = 0.0f;
    private long lastFrameTime = 0L;

    private int idleColor;
    private int hoverColor;

    public ModernButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        this(buttonId, x, y, widthIn, heightIn, buttonText, 0xFF36393F, 0xFF4A4D53);
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

    private int interpolateColor(int color1, int color2, float ratio) {
        if (ratio <= 0.01f) return color1;
        if (ratio >= 0.99f) return color2;

        float r1 = (color1 >> 16 & 255);
        float g1 = (color1 >> 8 & 255);
        float b1 = (color1 & 255);
        float a1 = (color1 >> 24 & 255);

        float r2 = (color2 >> 16 & 255);
        float g2 = (color2 >> 8 & 255);
        float b2 = (color2 & 255);
        float a2 = (color2 >> 24 & 255);

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        int a = (int) (a1 + (a2 - a1) * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - this.lastFrameTime;
            this.lastFrameTime = currentTime;
            if (deltaTime > 50L) deltaTime = 50L;

            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            float targetAnimation = (this.hovered && this.enabled) ? 1.0f : 0.0f;

            // Using an easing function for smooth animation
            double k = 0.04;
            float amountToMove = (float) (1.0 - Math.exp(-k * deltaTime));

            if (Math.abs(targetAnimation - hoverAnimation) > 0.01f) {
                hoverAnimation += (targetAnimation - hoverAnimation) * amountToMove;
            } else {
                hoverAnimation = targetAnimation;
            }

            int backgroundColor = interpolateColor(this.idleColor, this.hoverColor, this.hoverAnimation);
            if (!this.enabled) {
                backgroundColor = 0xFF2A2D31; // Disabled color
            }

            GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 5, backgroundColor);

            int textColor = 0xFFE0E0E0;
            if (packedFGColour != 0) {
                textColor = packedFGColour;
            } else if (!this.enabled) {
                textColor = 0xFFA0A0A0;
            }

            this.drawCenteredString(mc.fontRendererObj, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, textColor);
        }
    }
} 