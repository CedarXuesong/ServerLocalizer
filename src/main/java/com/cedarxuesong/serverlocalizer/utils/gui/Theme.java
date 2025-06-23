package com.cedarxuesong.serverlocalizer.utils.gui;

import java.awt.Color;

/**
 * A centralized theme class to hold all GUI color definitions.
 */
public class Theme {
    // Greyscale Palette
    public static final int COLOR_BACKGROUND = new Color(30, 30, 34).getRGB();
    public static final int COLOR_PANEL_BACKGROUND = new Color(43, 45, 49).getRGB();
    public static final int COLOR_ELEMENT_BACKGROUND = new Color(32, 34, 37).getRGB();

    // Text Palette
    public static final int COLOR_TEXT_WHITE = new Color(242, 243, 245).getRGB();
    public static final int COLOR_TEXT_GRAY = new Color(148, 155, 164).getRGB();
    public static final int COLOR_TEXT_DISABLED = new Color(106, 110, 115).getRGB();

    // Accent Palette (Blue)
    public static final int COLOR_ACCENT = new Color(88, 101, 242).getRGB();
    public static final int COLOR_ACCENT_HOVER = new Color(71, 82, 196).getRGB();

    // Component-specific colors
    public static final int COLOR_SCROLLBAR_TRACK = new Color(32, 34, 37, 150).getRGB();
    public static final int COLOR_SCROLLBAR_HANDLE = new Color(77, 79, 86).getRGB();
    public static final int COLOR_SCROLLBAR_HANDLE_HOVER = new Color(114, 118, 125).getRGB();

    public static final int COLOR_BUTTON_IDLE = new Color(71, 71, 71, 200).getRGB();
    public static final int COLOR_BUTTON_HOVER = new Color(92, 92, 92, 200).getRGB();
    public static final int COLOR_BUTTON_DISABLED = new Color(42, 45, 49).getRGB();
    public static final int COLOR_DISABLED_OVERLAY = new Color(43, 45, 49, 170).getRGB();

    public static final int COLOR_SEPARATOR = new Color(255, 255, 255, 50).getRGB();
    
    // Specific Highlight Colors for special buttons
    public static final int COLOR_HIGHLIGHT_GREEN = new Color(85, 142, 116).getRGB();
    public static final int COLOR_HIGHLIGHT_GREEN_HOVER = new Color(67, 110, 90).getRGB();
    public static final int COLOR_HIGHLIGHT_RED = new Color(179, 89, 89).getRGB();
    public static final int COLOR_HIGHLIGHT_RED_HOVER = new Color(140, 69, 69).getRGB();
    public static final int COLOR_HIGHLIGHT_YELLOW = new Color(176, 140, 74).getRGB();
    public static final int COLOR_HIGHLIGHT_YELLOW_HOVER = new Color(140, 109, 58).getRGB();
} 