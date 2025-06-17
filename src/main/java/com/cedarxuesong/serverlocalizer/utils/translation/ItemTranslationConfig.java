package com.cedarxuesong.serverlocalizer.utils.translation;

import java.io.File;
import java.util.Objects;

/**
 * 翻译配置类，管理不同模块的翻译文件路径
 */
public class ItemTranslationConfig {
    // 基础目录
    private static final String BASE_DIR = "config/serverlocalizer/language_packs/";
    
    // 翻译文件
    public static final String ITEM_TRANSLATION_FILE = BASE_DIR + "item.json";
    
    // 确保目录存在
    static {
        ensureDirectoryExists();
    }
    
    /**
     * 确保翻译文件目录存在
     */
    public static void ensureDirectoryExists() {
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * 获取配置根目录
     * @return 配置根目录路径
     */
    public static String getConfigRoot() {
        return BASE_DIR;
    }
    
    /**
     * 获取指定模块的翻译文件路径
     * @param module 模块名称
     * @return 翻译文件路径
     */
    public static String getTranslationFile(TranslationModule module) {
        if (Objects.requireNonNull(module) == TranslationModule.ITEM) {
            return ITEM_TRANSLATION_FILE;
        }
        return BASE_DIR + "default.json";
    }
    
    /**
     * 翻译模块枚举
     */
    public enum TranslationModule {
        ITEM
    }
} 