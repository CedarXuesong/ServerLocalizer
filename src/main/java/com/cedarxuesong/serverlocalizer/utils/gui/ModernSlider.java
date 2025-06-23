package com.cedarxuesong.serverlocalizer.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ModernSlider extends GuiButton {

    // --- Animation Speeds ---
    private static final double SLIDER_ANIMATION_SPEED = 0.02;
    private static final float INNER_CIRCLE_ANIMATION_MULTIPLIER = 1.0f;

    private final double minValue;
    private final double maxValue;
    private final String valueFormat;

    private double logicalValue; // The actual, snapped value
    private double displayValue; // The animated value for rendering
    private double targetValue;  // The value the animation is moving towards

    public boolean dragging;
    private long lastFrameTime;
    private boolean handleHovered;
    private float innerCircleAnimation = 0.0f;

    public ModernSlider(int buttonId, int x, int y, int width, int height, double minValue, double maxValue, double currentValue, String valueFormat) {
        super(buttonId, x, y, width, height, "");
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.valueFormat = valueFormat;

        this.logicalValue = MathHelper.clamp_double(currentValue, minValue, maxValue);
        this.displayValue = this.logicalValue;
        this.targetValue = this.logicalValue;

        this.lastFrameTime = System.currentTimeMillis();
    }

    public double getValue() {
        return this.logicalValue;
    }

    public void setValue(double value) {
        double clampedValue = MathHelper.clamp_double(value, this.minValue, this.maxValue);
        this.logicalValue = clampedValue;
        this.targetValue = clampedValue;
        this.displayValue = clampedValue;
    }

    private double getNormalizedValue(double value) {
        return MathHelper.clamp_double((value - this.minValue) / (this.maxValue - this.minValue), 0.0, 1.0);
    }

    public void updateValueFromMouse(int mouseX) {
        int sliderWidth = this.width - 50; // 40 for value box, 10 for spacing
        if (sliderWidth <= 0) return;
        double normalizedMouse = (double)(mouseX - this.xPosition) / (double)sliderWidth;
        normalizedMouse = MathHelper.clamp_double(normalizedMouse, 0.0, 1.0);
        this.targetValue = this.minValue + (this.maxValue - this.minValue) * normalizedMouse;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            // --- Animation ---
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - this.lastFrameTime;
            this.lastFrameTime = currentTime;
            if (deltaTime > 50L) deltaTime = 50L;

            float amountToMove = (float) (1.0 - Math.exp(-SLIDER_ANIMATION_SPEED * deltaTime));

            if (Math.abs(targetValue - displayValue) > 0.001f) {
                displayValue += (targetValue - displayValue) * amountToMove;
            } else {
                displayValue = targetValue;
            }

            double currentDisplayValue = MathHelper.clamp_double(this.displayValue, this.minValue, this.maxValue);
            double normalizedDisplayValue = getNormalizedValue(currentDisplayValue);

            // --- Drawing ---
            int sliderWidth = this.width - 50; // 40 for value box, 10 for spacing
            int sliderY = this.yPosition + (this.height - 4) / 2;

            // --- Handle hover detection and animation ---
            int handleX = (int)(this.xPosition + (sliderWidth * normalizedDisplayValue) - 6);
            int handleY = this.yPosition + (this.height - 12) / 2;
            this.handleHovered = mouseX >= handleX && mouseX < handleX + 12 && mouseY >= handleY && mouseY < handleY + 12;

            float innerCircleTarget = (this.handleHovered || this.dragging) ? 1.0f : 0.0f;
            if (Math.abs(innerCircleTarget - innerCircleAnimation) > 0.01f) {
                innerCircleAnimation += (innerCircleTarget - innerCircleAnimation) * amountToMove * INNER_CIRCLE_ANIMATION_MULTIPLIER;
            } else {
                innerCircleAnimation = innerCircleTarget;
            }

            // Draw track
            GuiUtils.drawRoundedRect(this.xPosition, sliderY, sliderWidth, 4, 2, Theme.COLOR_ELEMENT_BACKGROUND);

            // Draw filled track
            if (normalizedDisplayValue > 0) {
                GuiUtils.drawRoundedRect(this.xPosition, sliderY, (float)(sliderWidth * normalizedDisplayValue), 4, 2, Theme.COLOR_ACCENT);
            }

            // Draw handle
            GuiUtils.drawRoundedRect(handleX, handleY, 12, 12, 6, Theme.COLOR_TEXT_WHITE);

            // Draw inner circle on hover/drag with scaling animation
            if (this.innerCircleAnimation > 0.01f) {
                float maxRadius = 3.0f;
                float currentRadius = maxRadius * this.innerCircleAnimation;
                float centerX = handleX + 6;
                float centerY = handleY + 6;
                GuiUtils.drawRoundedRect(centerX - currentRadius, centerY - currentRadius, currentRadius * 2, currentRadius * 2, currentRadius, Theme.COLOR_ACCENT);
            }

            // Draw value box
            int valueBoxX = this.xPosition + sliderWidth + 10;
            GuiUtils.drawRoundedRect(valueBoxX, this.yPosition, 40, this.height, 5, Theme.COLOR_ELEMENT_BACKGROUND);

            // Show target value while dragging, otherwise show the logical (snapped) value
            double valueToShow = this.dragging ? this.targetValue : this.logicalValue;
            String valueText = String.format(this.valueFormat, valueToShow);
            this.drawCenteredString(mc.fontRendererObj, valueText, valueBoxX + 20, this.yPosition + (this.height - 8) / 2, Theme.COLOR_TEXT_WHITE);
            
            if (!this.enabled) {
                GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 5, Theme.COLOR_DISABLED_OVERLAY);
            }
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.enabled && this.visible && mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height) {
            updateValueFromMouse(mouseX);
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        if (this.dragging) {
            this.dragging = false;
            // Snap to the nearest 0.1 increment
            double step = 0.1;
            double snappedValue = Math.round(this.targetValue / step) * step;
            this.logicalValue = MathHelper.clamp_double(snappedValue, this.minValue, this.maxValue);
            this.targetValue = this.logicalValue;
        }
    }
}