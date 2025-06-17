package com.cedarxuesong.serverlocalizer.utils.translation;

import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模块化翻译管理器，负责管理不同模块的翻译
 */
public class ModuleTranslationManager {
    private static final String TAG = "ModuleTranslationManager";
    
    // 单例实例
    private static ModuleTranslationManager instance;
    
    // 模块翻译管理器映射
    private final Map<ItemTranslationConfig.TranslationModule, ItemTranslationManager> moduleManagers = new ConcurrentHashMap<>();
    
    // 初始化状态
    private boolean initialized = false;
    
    // 专用翻译器实例
    private ItemTranslator itemTranslator;
    
    /**
     * 获取ModuleTranslationManager实例
     * @return ModuleTranslationManager实例
     */
    public static ModuleTranslationManager getInstance() {
        if (instance == null) {
            instance = new ModuleTranslationManager();
        }
        return instance;
    }
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private ModuleTranslationManager() {
        // 确保目录存在
        ItemTranslationConfig.ensureDirectoryExists();
    }
    
    /**
     * 初始化翻译管理器
     * @return 是否初始化成功
     */
    public boolean initialize() {
        try {
            if (initialized) {
                return true;
            }
            
            // 初始化各个模块的翻译管理器
            // 注意：getTranslationManager内部会调用initialize方法加载翻译文件
            getTranslationManager(ItemTranslationConfig.TranslationModule.ITEM);
            
            // 初始化专用翻译器
            // 注意：专用翻译器会复用已加载的TranslationManager实例
            itemTranslator = new ItemTranslator();
            
            initialized = true;
            mylog.log(TAG, "模块化翻译管理器初始化完成");
            return true;
        } catch (Exception e) {
            mylog.error(TAG, "初始化模块化翻译管理器失败", e);
            return false;
        }
    }
    
    /**
     * 获取指定模块的翻译管理器
     * @param module 模块名称
     * @return 翻译管理器
     */
    public ItemTranslationManager getTranslationManager(ItemTranslationConfig.TranslationModule module) {
        return moduleManagers.computeIfAbsent(module, k -> {
            ItemTranslationManager manager = new ItemTranslationManager();
            manager.initialize(ItemTranslationConfig.getTranslationFile(module));
            return manager;
        });
    }

    /**
     * 保存所有模块的翻译
     * @return 是否全部保存成功
     */
    public boolean saveAllTranslations() {
        boolean allSaved = true;
        for (Map.Entry<ItemTranslationConfig.TranslationModule, ItemTranslationManager> entry : moduleManagers.entrySet()) {
            ItemTranslationManager manager = entry.getValue();
            if (manager.hasUnsavedChanges()) {
                boolean saved = manager.saveTranslations();
                if (saved) {
                    mylog.log(TAG, "已保存模块 " + entry.getKey() + " 的翻译");
                } else {
                    allSaved = false;
                    mylog.error(TAG, "保存模块 " + entry.getKey() + " 的翻译失败", new Exception("保存失败"));
                }
            }
        }
        return allSaved;
    }
    
    /**
     * 重新加载所有翻译文件
     * @return 是否全部重新加载成功
     */
    public boolean reloadAllTranslations() {
        boolean allReloaded = true;
        for (Map.Entry<ItemTranslationConfig.TranslationModule, ItemTranslationManager> entry : moduleManagers.entrySet()) {
            ItemTranslationManager manager = entry.getValue();
            boolean reloaded = manager.reloadTranslations();
            if (reloaded) {
                mylog.log(TAG, "已重新加载模块 " + entry.getKey() + " 的翻译");
            } else {
                allReloaded = false;
                mylog.error(TAG, "重新加载模块 " + entry.getKey() + " 的翻译失败", new Exception("重新加载失败"));
            }
        }
        
        return allReloaded;
    }
    
    /**
     * 检查是否已初始化
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 检查是否有未保存的更改
     * @return 是否有未保存的更改
     */
    public boolean hasUnsavedChanges() {
        for (ItemTranslationManager manager : moduleManagers.values()) {
            if (manager.hasUnsavedChanges()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取物品翻译器
     * @return 物品翻译器
     */
    public ItemTranslator getItemTranslator() {
        if (itemTranslator == null) {
            itemTranslator = new ItemTranslator();
        }
        return itemTranslator;
    }
    
    /**
     * 获取配置路径
     * @return 配置路径
     */
    public String getConfigPath() {
        return ItemTranslationConfig.getConfigRoot();
    }
} 