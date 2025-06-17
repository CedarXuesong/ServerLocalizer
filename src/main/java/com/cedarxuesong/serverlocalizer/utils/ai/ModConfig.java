package com.cedarxuesong.serverlocalizer.utils.ai;

import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * API配置管理类，用于从配置文件中加载和保存API配置
 */
public class ModConfig {
    private static final String TAG = "ModConfig";
    private static final String CONFIG_FILE = "config/serverlocalizer/ModConfig.json";
    // 默认配置
    private static final String DEFAULT_CONFIG = "{\n" +
            "    \"ItemTranslationApi\": {\n" +
            "        \"BaseUrl\": \"\",\n" +
            "        \"ApiKey\": \"\",\n" +
            "        \"Model\": \"\",\n" +
            "        \"Temperature\": 0.5,\n" +
            "        \"SystemPrompt\": \"你是一个专业的我的世界翻译助手.会严格遵循以下规则: 将原文翻译至中文，保留部分英文名词以保持游戏术语的准确性。保持所有格式标记(如§后的颜色代码).翻译要简洁自然，符合游戏表达习惯. 只返回翻译后的JSON数组，格式为:[{{\\\"key\\\": \\\"原key\\\", \\\"translated\\\": \\\"翻译后的文本\\\"}}}]\",\n" +
            "        \"TotalTokens\": 0\n" +
            "    },\n" +
            "    \"ChatTranslationApi\": {\n" +
            "        \"BaseUrl\": \"\",\n" +
            "        \"ApiKey\": \"\",\n" +
            "        \"Model\": \"\",\n" +
            "        \"Temperature\": 0.5,\n" +
            "        \"SystemPrompt\": \"你是一个翻译助手，请将以下文本翻译成中文，保留格式字符§*，仅输出译文。\",\n" +
            "        \"useStream\": false\n" +
            "    },\n" +
            "    \"GeneralSettings\": {\n" +
            "        \"EnableItemTranslation\": false,\n" +
            "        \"EnableItemNameTranslation\": false,\n" +
            "        \"EnableItemLoreTranslation\": false,\n" +
            "        \"EnableChatTranslation\": false,\n" +
            "        \"EnableDebugWindow\": false\n" +
            "    }\n" +
            "}";
    private static ModConfig instance;
    private final Gson gson;
    // 存储所有API配置的映射
    private JsonObject configData;
    
    private ModConfig() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    /**
     * 获取ApiConfig实例
     * @return ApiConfig实例
     */
    public static synchronized ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
            instance.loadConfig(); // Load config after instance is assigned
        }
        return instance;
    }
    
    /**
     * 初始化配置，重新加载配置文件
     * @return 是否成功初始化
     */
    public boolean initialize() {
        try {
            loadConfig();
            mylog.log(TAG, "API配置已重新初始化");
            return true;
        } catch (Exception e) {
            mylog.error(TAG, "初始化API配置时出错", e);
            return false;
        }
    }
    
    /**
     * 从配置文件加载配置
     */
    private void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        
        // 确保配置目录存在
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        
        // 如果配置文件不存在，创建默认配置
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
                    writer.write(DEFAULT_CONFIG);
                }
                configData = parseJson(DEFAULT_CONFIG);
                mylog.log(TAG, "创建了默认API配置文件");
            } catch (IOException e) {
                mylog.error(TAG, "创建默认配置文件时出错", e);
                configData = parseJson(DEFAULT_CONFIG);
            }
        } else {
            // 读取现有配置文件
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                configData = parseJson(content.toString());
                mylog.log(TAG, "成功加载API配置文件");
            } catch (Exception e) {
                mylog.error(TAG, "读取配置文件时出错，使用默认配置", e);
                configData = parseJson(DEFAULT_CONFIG);
            }
        }
    }
    
    /**
     * 将JSON字符串解析为JsonObject
     * @param json JSON字符串
     * @return 解析后的JsonObject
     */
    private JsonObject parseJson(String json) {
        try {
            // 使用旧版本Gson的方法解析JSON
            JsonParser parser = new JsonParser();
            return parser.parse(json).getAsJsonObject();
        } catch (Exception e) {
            mylog.error(TAG, "解析JSON时出错", e);
            // 创建一个空的JsonObject作为回退
            return new JsonObject();
        }
    }
    
    /**
     * 保存配置到文件
     * @return 是否保存成功
     */
    public boolean saveConfig() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(Paths.get(CONFIG_FILE)), StandardCharsets.UTF_8))) {
            writer.write(gson.toJson(configData));
            mylog.log(TAG, "成功保存API配置");
            return true;
        } catch (IOException e) {
            mylog.error(TAG, "保存配置文件时出错", e);
        }
        return false;
    }
    
    /**
     * Resets the configuration to the default state.
     */
    public void resetToDefaults() {
        this.configData = parseJson(DEFAULT_CONFIG);
        saveConfig();
        initialize();
        mylog.log(TAG, "配置已重置为默认值");
    }
    
    private JsonObject getOrCreateApiSettings(String apiName) {
        if (configData.has(apiName) && configData.get(apiName).isJsonObject()) {
            return configData.getAsJsonObject(apiName);
        }
        JsonObject newApiSettings = new JsonObject();
        configData.add(apiName, newApiSettings);
        mylog.log(TAG, "创建了缺失的配置节: " + apiName);
        return newApiSettings;
    }

    private JsonObject getOrCreateGeneralSettings() {
        if (configData.has("GeneralSettings") && configData.get("GeneralSettings").isJsonObject()) {
            return configData.getAsJsonObject("GeneralSettings");
        }
        JsonObject newGeneralSettings = new JsonObject();
        configData.add("GeneralSettings", newGeneralSettings);
        mylog.log(TAG, "创建了缺失的配置节: GeneralSettings");
        return newGeneralSettings;
    }
    
    /**
     * 获取指定API的URL
     * @param apiName API名称
     * @return API的URL
     */
    public String getBaseUrl(String apiName) {
        JsonObject apiSettings = getOrCreateApiSettings(apiName);
        if (apiSettings.has("BaseUrl") && apiSettings.get("BaseUrl").isJsonPrimitive()) {
            return apiSettings.get("BaseUrl").getAsString();
        }
        apiSettings.addProperty("BaseUrl", ""); // Default value
        return "";
    }
    
    /**
     * 获取指定API的密钥
     * @param apiName API名称
     * @return API的密钥
     */
    public String getApiKey(String apiName) {
        JsonObject apiSettings = getOrCreateApiSettings(apiName);
        if (apiSettings.has("ApiKey") && apiSettings.get("ApiKey").isJsonPrimitive()) {
            return apiSettings.get("ApiKey").getAsString();
        }
        apiSettings.addProperty("ApiKey", ""); // Default value
        return "";
    }
    
    /**
     * 获取指定API的模型
     * @param apiName API名称
     * @return API的模型
     */
    public String getModel(String apiName) {
        JsonObject apiSettings = getOrCreateApiSettings(apiName);
        if (apiSettings.has("Model") && apiSettings.get("Model").isJsonPrimitive()) {
            return apiSettings.get("Model").getAsString();
        }
        apiSettings.addProperty("Model", ""); // Default value
        return "";
    }
    
    /**
     * 获取指定API的温度
     * @param apiName API名称
     * @return API的温度
     */
    public double getTemperature(String apiName) {
        JsonObject apiSettings = getOrCreateApiSettings(apiName);
        if (apiSettings.has("Temperature") && apiSettings.get("Temperature").isJsonPrimitive()) {
            try {
                return apiSettings.get("Temperature").getAsDouble();
            } catch (NumberFormatException e) {
                // Not a valid double, fall back to default
            }
        }
        apiSettings.addProperty("Temperature", 0.5); // Default value
        return 0.5;
    }
    
    /**
     * 获取指定API的系统提示词
     * @param apiName API名称
     * @return API的系统提示词
     */
    public String getSystemPrompt(String apiName) {
        JsonObject apiSettings = getOrCreateApiSettings(apiName);
        if (apiSettings.has("SystemPrompt") && apiSettings.get("SystemPrompt").isJsonPrimitive()) {
            return apiSettings.get("SystemPrompt").getAsString();
        }
        String defaultPrompt = "You are a helpful assistant.";
        apiSettings.addProperty("SystemPrompt", defaultPrompt);
        return defaultPrompt;
    }
    
    /**
     * 设置指定API的URL
     * @param apiName API名称
     * @param baseUrl API的URL
     */
    public void setBaseUrl(String apiName, String baseUrl) {
        getOrCreateApiSettings(apiName).addProperty("BaseUrl", baseUrl);
    }
    
    /**
     * 设置指定API的密钥
     * @param apiName API名称
     * @param apiKey API的密钥
     */
    public void setApiKey(String apiName, String apiKey) {
        getOrCreateApiSettings(apiName).addProperty("ApiKey", apiKey);
    }
    
    /**
     * 设置指定API的模型
     * @param apiName API名称
     * @param model API的模型
     */
    public void setModel(String apiName, String model) {
        getOrCreateApiSettings(apiName).addProperty("Model", model);
    }
    
    /**
     * 设置指定API的温度
     * @param apiName API名称
     * @param temperature API的温度
     */
    public void setTemperature(String apiName, double temperature) {
        getOrCreateApiSettings(apiName).addProperty("Temperature", temperature);
    }
    
    /**
     * 设置指定API的系统提示词
     * @param apiName API名称
     * @param systemPrompt API的系统提示词
     */
    public void setSystemPrompt(String apiName, String systemPrompt) {
        getOrCreateApiSettings(apiName).addProperty("SystemPrompt", systemPrompt);
    }

    public long getAccumulatedTokens(String apiName) {
        JsonObject apiSettings = getOrCreateApiSettings(apiName);
        if (apiSettings.has("TotalTokens") && apiSettings.get("TotalTokens").isJsonPrimitive()) {
            try {
                return apiSettings.get("TotalTokens").getAsLong();
            } catch (NumberFormatException e) {
                // Not a valid long, fall back to default
            }
        }
        apiSettings.addProperty("TotalTokens", 0L); // Default value
        return 0L;
    }

    public void addAccumulatedTokens(String apiName, long tokensToAdd) {
        long currentTokens = getAccumulatedTokens(apiName);
        getOrCreateApiSettings(apiName).addProperty("TotalTokens", currentTokens + tokensToAdd);
    }

    private boolean getGeneralSetting(String key) {
        JsonObject generalSettings = getOrCreateGeneralSettings();
        if (generalSettings.has(key) && generalSettings.get(key).isJsonPrimitive()) {
            try {
                return generalSettings.get(key).getAsBoolean();
            } catch (Exception e) {
                // Not a boolean
            }
        }
        generalSettings.addProperty(key, false);
        return false;
    }

    private void setGeneralSetting(String key, boolean value) {
        getOrCreateGeneralSettings().addProperty(key, value);
    }
    
    // ==================== 通用设置方法 ====================
    
    /**
     * 获取是否启用物品翻译
     * @return 是否启用物品翻译
     */
    public boolean isItemTranslationEnabled() {
        return getGeneralSetting("EnableItemTranslation");
    }
    
    /**
     * 获取是否启用物品名称翻译
     * @return 是否启用物品名称翻译
     */
    public boolean isItemNameTranslationEnabled() {
        return getGeneralSetting("EnableItemNameTranslation");
    }
    
    /**
     * 设置是否启用物品名称翻译
     * @param enabled 是否启用
     */
    public void setItemNameTranslationEnabled(boolean enabled) {
        setGeneralSetting("EnableItemNameTranslation", enabled);
    }
    
    /**
     * 获取是否启用物品Lore翻译
     * @return 是否启用物品Lore翻译
     */
    public boolean isItemLoreTranslationEnabled() {
        return getGeneralSetting("EnableItemLoreTranslation");
    }
    
    /**
     * 设置是否启用物品Lore翻译
     * @param enabled 是否启用
     */
    public void setItemLoreTranslationEnabled(boolean enabled) {
        setGeneralSetting("EnableItemLoreTranslation", enabled);
    }
    
    /**
     * 获取是否启用聊天翻译
     * @return 是否启用聊天翻译
     */
    public boolean isChatTranslationEnabled() {
        return getGeneralSetting("EnableChatTranslation");
    }
    
    /**
     * 设置是否启用聊天翻译
     * @param enabled 是否启用
     */
    public void setChatTranslationEnabled(boolean enabled) {
        setGeneralSetting("EnableChatTranslation", enabled);
    }

    public void setItemTranslationEnabled(boolean itemEnabled) {
        setGeneralSetting("EnableItemTranslation", itemEnabled);
    }

    /**
     * 获取是否启用聊天流式响应
     * @return 是否启用聊天流式响应
     */
    public boolean isChatStreamEnabled() {
        JsonObject chatApiSettings = getOrCreateApiSettings("ChatTranslationApi");
        if (chatApiSettings.has("useStream") && chatApiSettings.get("useStream").isJsonPrimitive()) {
            try {
                return chatApiSettings.get("useStream").getAsBoolean();
            } catch (Exception e) {
                // Not a boolean
            }
        }
        chatApiSettings.addProperty("useStream", false); // Default
        return false;
    }

    /**
     * 设置是否启用聊天流式响应
     * @param enabled 是否启用
     */
    public void setChatStreamEnabled(boolean enabled) {
        getOrCreateApiSettings("ChatTranslationApi").addProperty("useStream", enabled);
    }

    /**
     * 获取是否启用调试窗口
     * @return 是否启用调试窗口
     */
    public boolean isDebugWindowEnabled() {
        return getGeneralSetting("EnableDebugWindow");
    }

    /**
     * 设置是否启用调试窗口
     * @param enabled 是否启用
     */
    public void setDebugWindowEnabled(boolean enabled) {
        setGeneralSetting("EnableDebugWindow", enabled);
    }
}
