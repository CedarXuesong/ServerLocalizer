package com.cedarxuesong.serverlocalizer.utils;

import net.minecraft.client.resources.I18n;

public class Lang {

    /**
     * Translates a string with the given key.
     * @param key The key of the string to translate.
     * @return The translated string.
     */
    public static String translate(String key) {
        return I18n.format(key);
    }

    /**
     * Translates a string with the given key and format arguments.
     * @param key The key of the string to translate.
     * @param args The format arguments.
     * @return The translated and formatted string.
     */
    public static String translate(String key, Object... args) {
        return I18n.format(key, args);
    }
} 