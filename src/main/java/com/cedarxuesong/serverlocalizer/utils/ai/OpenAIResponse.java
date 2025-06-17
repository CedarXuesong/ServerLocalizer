package com.cedarxuesong.serverlocalizer.utils.ai;

/**
 * OpenAI API 响应类
 */
public class OpenAIResponse {
    private final String content;
    private final String error;
    private final int statusCode;
    private final Usage usage;

    public OpenAIResponse(String content, String error, int statusCode) {
        this.content = content;
        this.error = error;
        this.statusCode = statusCode;
        this.usage = extractUsage();
    }

    public String getContent() {
        return content;
    }

    public String getError() {
        return error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300 && error == null;
    }

    public Usage getUsage() {
        return usage;
    }
    
    /**
     * 从JSON响应中提取消息内容
     */
    public String extractMessageContent() {
        if (!isSuccess() || content == null) {
            return null;
        }
        
        try {
            // 简单的JSON解析，查找第一个assistant消息的content
            int choicesIndex = content.indexOf("\"choices\"");
            if (choicesIndex == -1) return null;
            
            int messageIndex = content.indexOf("\"message\"", choicesIndex);
            if (messageIndex == -1) return null;
            
            int contentIndex = content.indexOf("\"content\"", messageIndex);
            if (contentIndex == -1) return null;
            
            int startQuote = content.indexOf("\"", contentIndex + 9);
            if (startQuote == -1) return null;
            
            int endQuote = findClosingQuote(content, startQuote + 1);
            if (endQuote == -1) return null;
            
            return unescapeJson(content.substring(startQuote + 1, endQuote));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从JSON响应中提取使用情况
     */
    public Usage extractUsage() {
        if (!isSuccess() || content == null) {
            return null;
        }

        try {
            int usageIndex = content.indexOf("\"usage\"");
            if (usageIndex == -1) return null;

            int promptTokensIndex = content.indexOf("\"prompt_tokens\"", usageIndex);
            if (promptTokensIndex == -1) return null;
            int promptTokens = Integer.parseInt(extractJsonValue(content, promptTokensIndex));

            int completionTokensIndex = content.indexOf("\"completion_tokens\"", usageIndex);
            if (completionTokensIndex == -1) return null;
            int completionTokens = Integer.parseInt(extractJsonValue(content, completionTokensIndex));

            int totalTokensIndex = content.indexOf("\"total_tokens\"", usageIndex);
            if (totalTokensIndex == -1) return null;
            int totalTokens = Integer.parseInt(extractJsonValue(content, totalTokensIndex));

            return new Usage(promptTokens, completionTokens, totalTokens);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractJsonValue(String json, int index) {
        int colonIndex = json.indexOf(":", index);
        int startIndex = colonIndex + 1;
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        int endIndex = startIndex;
        while (endIndex < json.length() && Character.isDigit(json.charAt(endIndex))) {
            endIndex++;
        }
        return json.substring(startIndex, endIndex);
    }
    
    /**
     * 查找JSON字符串中的闭合引号
     */
    private int findClosingQuote(String json, int startIndex) {
        boolean escaped = false;
        for (int i = startIndex; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 反转义JSON字符串
     */
    private String unescapeJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escaped) {
                switch (c) {
                    case '"':
                        result.append('"');
                        break;
                    case '\\':
                        result.append('\\');
                        break;
                    case 'b':
                        result.append('\b');
                        break;
                    case 'f':
                        result.append('\f');
                        break;
                    case 'n':
                        result.append('\n');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case 'u':
                        if (i + 4 < input.length()) {
                            String hex = input.substring(i + 1, i + 5);
                            try {
                                result.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                result.append(c);
                            }
                        } else {
                            result.append(c);
                        }
                        break;
                    default:
                        result.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}

