package com.cedarxuesong.serverlocalizer.utils.translation;

import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;

/**
 * 物品翻译工具类
 */
public class ItemTranslator {
    private static final String TAG = "ItemTranslator";
    private final ItemTranslationManager translationManager;
    
    /**
     * 构造函数
     */
    public ItemTranslator() {
        // 获取物品翻译的管理器
        this.translationManager = ModuleTranslationManager.getInstance()
                .getTranslationManager(ItemTranslationConfig.TranslationModule.ITEM);
    }

    /**
     * 实例方法：翻译文本
     * @param originalText 原始文本
     * @return 翻译后的文本
     */
    public String translate(String originalText) {
        if (originalText == null || originalText.isEmpty()) {
            return originalText;
        }
        
        try {
            // 处理文本模板和动态内容
            TextProcessor.TemplateResult templateResult = TextProcessor.convertToTemplate(originalText);
            String template = templateResult.getTemplate();
            
            // 查找翻译
            String translatedTemplate = translationManager.findTranslation(template);
            
            // 如果没有找到翻译，添加一个新条目并返回原文
            if (translatedTemplate == null) {
                translationManager.addOrUpdateTranslation(template, "");
                return originalText;
            }
            
            // 如果翻译为空，也返回原文
            if (translatedTemplate.isEmpty()) {
                return originalText;
            }
            
            // 将动态内容填充到翻译后的模板中
            return TextProcessor.fillTemplate(translatedTemplate, templateResult.getDynamicContent());
            
        } catch (Exception e) {
            mylog.error(TAG, "翻译物品文本时发生错误: " + originalText, e);
            return originalText;
        }
    }

    /**
     * 获取翻译条目数量
     * @return 翻译条目数量
     */
    public int getTranslationCount() {
        return translationManager.getTranslationCount();
    }
} 