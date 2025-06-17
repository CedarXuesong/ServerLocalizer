package com.cedarxuesong.serverlocalizer.utils;

import net.minecraft.client.resources.I18n;

import java.util.Arrays;
import java.util.List;

public class Lang {

    /**
     * Translates a string with the given key. It replaces "\\n" with "\n" to support multiline text from lang files.
     * @param key The key of the string to translate.
     * @return The translated string.
     */
    public static String translate(String key) {
        return I18n.format(key).replace("\\n", "\n");
    }

    /**
     * Translates a string with the given key and format arguments. It replaces "\\n" with "\n" to support multiline text from lang files.
     * @param key The key of the string to translate.
     * @param args The format arguments.
     * @return The translated and formatted string.
     */
    public static String translate(String key, Object... args) {
        return I18n.format(key, args).replace("\\n", "\n");
    }

    /**
     * Translates a string with the given key and splits it into a list of strings, ideal for tooltips.
     * Your .lang file should use "\\n" for line breaks.
     * @param key The key of the string to translate.
     * @return A list of translated strings.
     */
    public static List<String> getTranslatedLines(String key) {
        return Arrays.asList(translate(key).split("\n"));
    }

    /**
     * Translates and formats a string with the given key and arguments, then splits it into a list of strings, ideal for tooltips.
     * Your .lang file should use "\\n" for line breaks.
     * @param key The key of the string to translate.
     * @param args The format arguments.
     * @return A list of translated and formatted strings.
     */
    public static List<String> getTranslatedLines(String key, Object... args) {
        return Arrays.asList(translate(key, args).split("\n"));
    }
} 