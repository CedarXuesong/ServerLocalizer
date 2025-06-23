package com.cedarxuesong.serverlocalizer.utils.gui;

public class ScissorBox {
    public final int x, y, width, height;

    public ScissorBox(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ScissorBox intersection(ScissorBox other) {
        int x1 = Math.max(this.x, other.x);
        int y1 = Math.max(this.y, other.y);
        int x2 = Math.min(this.x + this.width, other.x + other.width);
        int y2 = Math.min(this.y + this.height, other.y + other.height);
        
        int newWidth = x2 - x1;
        int newHeight = y2 - y1;
        
        if (newWidth <= 0 || newHeight <= 0) {
            return new ScissorBox(0, 0, 0, 0); // No intersection
        }
        
        return new ScissorBox(x1, y1, newWidth, newHeight);
    }
} 