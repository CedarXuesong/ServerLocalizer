package com.cedarxuesong.serverlocalizer.utils.ai;

/**
 * OpenAI API 消息类
 */
public class Message {
    private String role;
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * 转换为JSON格式
     */
    public String toJson() {
        return String.format("{\"role\":\"%s\",\"content\":\"%s\"}", 
            escapeJson(role), 
            escapeJson(content)
        );
    }
    
    /**
     * 转义JSON字符串
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    // 处理其他不可打印字符
                    if (c < ' ') {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.toString();
    }
} 