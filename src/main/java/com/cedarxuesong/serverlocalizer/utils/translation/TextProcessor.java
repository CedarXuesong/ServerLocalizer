package com.cedarxuesong.serverlocalizer.utils.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本处理工具类，用于处理游戏中的动态内容和模式匹配
 */
public class TextProcessor {
    private static final String TAG = "TextProcessor";
    
    // Minecraft颜色代码正则表达式
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§[0-9a-fk-or]");
    
    // 数字匹配模式 - 更精确的格式
    private static final Pattern FRACTION_NUMBER_PATTERN = Pattern.compile("\\d+[,./|\\\\;:]\\d+"); // 例如：200/200
    private static final Pattern SEPARATOR_NUMBER_PATTERN = Pattern.compile("\\d+(?:[,./|\\\\;:]\\d+)+"); // 例如：123,456
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+"); // 例如：1000
    
    // 动态内容占位符
    private static final String DYNAMIC_CONTENT_PLACEHOLDER = "{〶}";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{〶\\}");

    // 玩家名称占位符

    /**
     * 临时存储颜色代码和占位符的映射
     */
    private static class ColorCodeMapping {
        Map<String, String> codeToPlaceholder = new HashMap<>();
        Map<String, String> placeholderToCode = new HashMap<>();
        int counter = 0;
        
        String addColorCode(String colorCode) {
            String placeholder = codeToPlaceholder.get(colorCode);
            if (placeholder == null) {
                // 使用不可见的Unicode字符作为占位符
                // 使用Unicode私有使用区域 (U+E000 - U+F8FF) 的字符
                // 每个颜色代码对应一个不同的字符
                char unicodeChar = (char) (0xE000 + counter++);
                placeholder = String.valueOf(unicodeChar);
                codeToPlaceholder.put(colorCode, placeholder);
                placeholderToCode.put(placeholder, colorCode);
            }
            return placeholder;
        }
        
        String restoreColorCodes(String text) {
            String result = text;
            for (Map.Entry<String, String> entry : placeholderToCode.entrySet()) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
            return result;
        }
    }
    
    /**
     * 将颜色代码替换为占位符
     */
    private static String replaceColorCodesWithPlaceholders(String text, ColorCodeMapping mapping) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuffer sb = new StringBuffer();
        Matcher matcher = COLOR_CODE_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String colorCode = matcher.group();
            String placeholder = mapping.addColorCode(colorCode);
            matcher.appendReplacement(sb, placeholder);
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    /**
     * 检查一个数字是否是另一个数字的一部分
     * 例如：123 不应该被认为是 123 的一部分（相等的情况）
     * 但 12 是 123 的一部分
     */
    private static boolean isPartOfOtherNumber(String number, List<String> existingNumbers) {
        for (String existingNumber : existingNumbers) {
            // 如果两个数字完全相同，不认为是其他数字的一部分
            if (existingNumber.equals(number)) {
                continue;
            }
            // 检查 number 是否是 existingNumber 的子串
            if (existingNumber.contains(number)) {
                // 进一步检查是否是独立的数字
                // 例如：对于 "123"，"12" 是其一部分
                // 但对于 "12 34"，"12" 是独立的数字
                int index = existingNumber.indexOf(number);
                boolean isIndependent = true;
                
                // 检查前一个字符（如果有）
                if (index > 0) {
                    char prevChar = existingNumber.charAt(index - 1);
                    if (Character.isDigit(prevChar)) {
                        isIndependent = false;
                    }
                }
                
                // 检查后一个字符（如果有）
                if (index + number.length() < existingNumber.length()) {
                    char nextChar = existingNumber.charAt(index + number.length());
                    if (Character.isDigit(nextChar)) {
                        isIndependent = false;
                    }
                }
                
                if (!isIndependent) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 将文本转换为模板，替换动态内容为占位符
     * @param text 原始文本
     * @return 转换后的模板和提取的动态内容
     */
    public static TemplateResult convertToTemplate(String text) {
        
        // 首先检查是否为空
        if (text == null || text.isEmpty()) {
            return new TemplateResult(text, new ArrayList<>());
        }
        
        // 创建颜色代码映射
        ColorCodeMapping colorMapping = new ColorCodeMapping();
        
        // 1. 先将原始文本中的颜色代码替换为占位符
        String processedText = replaceColorCodesWithPlaceholders(text, colorMapping);
        
        // 2. 提取动态内容（处理不带颜色代码的文本）
        List<String> dynamicContentWithPlaceholders = new ArrayList<>();
        List<String> dynamicContentOriginal = new ArrayList<>();
        
        // 首先匹配分数格式
        Matcher fractionNumberMatcher = FRACTION_NUMBER_PATTERN.matcher(processedText);
        while (fractionNumberMatcher.find()) {
            String number = fractionNumberMatcher.group();
            dynamicContentWithPlaceholders.add(number);
            String original = colorMapping.restoreColorCodes(number);
            dynamicContentOriginal.add(original);
        }
        
        // 然后匹配带分隔符的数字
        Matcher separatorNumberMatcher = SEPARATOR_NUMBER_PATTERN.matcher(processedText);
        while (separatorNumberMatcher.find()) {
            String number = separatorNumberMatcher.group();
            //检查数字是否是其他数字的一部分
            if (!dynamicContentWithPlaceholders.contains(number)) {
                dynamicContentWithPlaceholders.add(number);
                String original = colorMapping.restoreColorCodes(number);
                dynamicContentOriginal.add(original);
            }
        }
        
        // 最后匹配普通数字
        Matcher numberMatcher = NUMBER_PATTERN.matcher(processedText);
        while (numberMatcher.find()) {
            String number = numberMatcher.group();
            //检查数字是否是其他数字的一部分
            if (!isPartOfOtherNumber(number, dynamicContentWithPlaceholders)) {
                dynamicContentWithPlaceholders.add(number);
                String original = colorMapping.restoreColorCodes(number);
                dynamicContentOriginal.add(original);
            }
        }
        
        // 如果没有动态内容，直接返回原始文本作为模板
        if (dynamicContentWithPlaceholders.isEmpty()) {
            return new TemplateResult(text, dynamicContentOriginal);
        }
        
        // 按长度降序排序，避免短字符串替换导致的问题
        // 同时保持两个列表的对应关系
        List<Pair<String, String>> combinedList = new ArrayList<>();
        for (int i = 0; i < dynamicContentWithPlaceholders.size(); i++) {
            combinedList.add(new Pair<>(
                dynamicContentWithPlaceholders.get(i),
                dynamicContentOriginal.get(i)
            ));
        }
        
        combinedList.sort((a, b) -> b.first.length() - a.first.length());
        
        dynamicContentWithPlaceholders.clear();
        dynamicContentOriginal.clear();
        
        for (Pair<String, String> pair : combinedList) {
            dynamicContentWithPlaceholders.add(pair.first);
            dynamicContentOriginal.add(pair.second);
        }
        
        // 3. 创建模板 - 使用带占位符的文本和动态内容
        String template = processedText;
        for (String content : dynamicContentWithPlaceholders) {
            template = template.replace(content, DYNAMIC_CONTENT_PLACEHOLDER);
        }
        
        // 4. 最后恢复所有颜色代码
        template = colorMapping.restoreColorCodes(template);

        return new TemplateResult(template, dynamicContentOriginal);
    }

    /**
     * 将动态内容填充回模板
     * @param template 模板文本
     * @param dynamicContent 动态内容列表
     * @return 填充后的文本
     */
    public static String fillTemplate(String template, List<String> dynamicContent) {

        if (dynamicContent.isEmpty()) {
            return template;
        }
        
        // 创建颜色代码映射
        ColorCodeMapping colorMapping = new ColorCodeMapping();
        
        // 先将模板中的颜色代码替换为占位符
        String processedTemplate = replaceColorCodesWithPlaceholders(template, colorMapping);
        
        // 将动态内容也转换为带占位符的形式
        List<String> processedDynamicContent = new ArrayList<>();
        for (String content : dynamicContent) {
            String processed = replaceColorCodesWithPlaceholders(content, colorMapping);
            processedDynamicContent.add(processed);
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(processedTemplate);
        
        int index = 0;
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find() && index < processedDynamicContent.size()) {
            String replacement = processedDynamicContent.get(index);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            index++;
        }
        matcher.appendTail(sb);

        // 恢复结果中的颜色代码
        return colorMapping.restoreColorCodes(sb.toString());
    }

    /**
     * 键值对辅助类
     */
    private static class Pair<F, S> {
        public final F first;
        public final S second;
        
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
    
    /**
     * 模板结果类，存储模板和提取的动态内容
     */
    public static class TemplateResult {
        private final String template;
        private final List<String> dynamicContent;
        
        public TemplateResult(String template, List<String> dynamicContent) {
            this.template = template;
            this.dynamicContent = dynamicContent;
        }
        
        public String getTemplate() {
            return template;
        }
        
        public List<String> getDynamicContent() {
            return dynamicContent;
        }
    }
} 