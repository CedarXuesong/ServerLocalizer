package com.cedarxuesong.serverlocalizer.utils.translation;

import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * 翻译管理器，负责加载翻译文件和处理文本替换
 */
public class ItemTranslationManager {
    private static final String TAG = "ItemTranslationManager";

    // 翻译映射表 (原文 -> 译文)
    private Map<String, String> translationMap = new HashMap<>();
    
    // 是否已初始化
    private boolean initialized = false;
    
    // 是否有未保存的更改
    private boolean hasUnsavedChanges = false;
    
    // 翻译文件路径
    private String translationFilePath;

    /**
     * 初始化翻译管理器，加载翻译文件
     * @param filePath 翻译文件路径
     * @return 是否初始化成功
     */
    public boolean initialize(String filePath) {
        if (initialized) {
            return true;
        }
        
        try {
            this.translationFilePath = filePath;
            File file = new File(filePath);
            if (!file.exists()) {
                mylog.warn(TAG, "翻译文件不存在: " + filePath);
                createEmptyTranslationFile(filePath);
                initialized = true; // 即使文件不存在，也标记为已初始化
                return true;
            }
            
            loadTranslations(file);
            initialized = true;
            mylog.log(TAG, "成功加载翻译文件，共 " + translationMap.size() + " 条翻译");
            return true;
        } catch (Exception e) {
            mylog.error(TAG, "初始化翻译管理器失败", e);
            return false;
        }
    }
    
    /**
     * 重新加载翻译文件
     * @return 是否重新加载成功
     */
    public boolean reloadTranslations() {
        try {
            if (translationFilePath == null) {
                mylog.warn(TAG, "未指定翻译文件路径，无法重新加载");
                return false;
            }
            
            File file = new File(translationFilePath);
            if (!file.exists()) {
                mylog.warn(TAG, "翻译文件不存在: " + translationFilePath);
                return false;
            }
            
            // 检查是否有未保存的更改
            if (hasUnsavedChanges) {
                mylog.warn(TAG, "存在未保存的翻译更改，重新加载将丢失这些更改");
            }
            
            // 清空当前翻译
            translationMap.clear();
            
            // 加载翻译文件
            loadTranslations(file);
            initialized = true;
            hasUnsavedChanges = false;
            
            mylog.log(TAG, "成功重新加载翻译文件，共 " + translationMap.size() + " 条翻译");
            return true;
        } catch (Exception e) {
            mylog.error(TAG, "重新加载翻译文件失败", e);
            return false;
        }
    }
    
    /**
     * 获取翻译条目数量
     * @return 翻译条目数量
     */
    public int getTranslationCount() {
        return translationMap.size();
    }

    /**
     * 创建空的翻译文件
     * @param filePath 文件路径
     */
    private void createEmptyTranslationFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            Map<String, String> emptyMap = new HashMap<>();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(emptyMap);
            
            try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
                writer.write(json);
            }
            
            mylog.log(TAG, "已创建空的翻译文件: " + filePath);
        } catch (Exception e) {
            mylog.error(TAG, "创建空的翻译文件失败", e);
        }
    }
    
    /**
     * 加载翻译文件
     * @param file 翻译文件
     * @throws IOException 如果读取文件失败
     */
    private void loadTranslations(File file) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
            
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            translationMap = gson.fromJson(jsonObject, type);
            
            if (translationMap == null) {
                translationMap = new HashMap<>();
            }
        }
    }
    
    /**
     * 保存翻译到文件
     * @return 是否保存成功
     */
    public boolean saveTranslations() {
        if (translationFilePath == null) {
            mylog.log(TAG, "未指定翻译文件路径");
            return false;
        }
        
        return saveTranslations(translationFilePath);
    }
    
    /**
     * 保存翻译到文件
     * @param filePath 文件路径
     * @return 是否保存成功
     */
    public boolean saveTranslations(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(translationMap);
            
            try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
                writer.write(json);
            }
            
            hasUnsavedChanges = false;
            mylog.log(TAG, "成功保存翻译文件，共 " + translationMap.size() + " 条翻译");
            return true;
        } catch (Exception e) {
            mylog.error(TAG, "保存翻译文件失败", e);
            return false;
        }
    }
    
    /**
     * 添加或更新翻译条目
     *
     * @param original   原文模板
     * @param translated 翻译后的模板
     */
    public void addOrUpdateTranslation(String original, String translated) {
        // 如果已存在相同的原文，并且新的翻译与旧的翻译不同，则更新
        if (translationMap.containsKey(original)) {
            String existingTranslation = translationMap.get(original);
            if (translated != null && !translated.equals(existingTranslation)) {
                translationMap.put(original, translated);
                hasUnsavedChanges = true;
                return;
            }
            return; // 没有变化
        }
        
        // 如果不存在，添加新条目
        translationMap.put(original, translated);
        hasUnsavedChanges = true;
    }

    /**
     * 检查是否有未保存的更改
     * @return 是否有未保存的更改
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    /**
     * 根据原文查找翻译
     * @param original 原文
     * @return 翻译后的文本，如果不存在则返回null
     */
    public String findTranslation(String original) {
        return translationMap.get(original);
    }

    /**
     * 获取翻译映射表
     * @return 翻译映射表
     */
    public Map<String, String> getTranslationMap() {
        return translationMap;
    }
} 