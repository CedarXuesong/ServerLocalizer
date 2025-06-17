package com.cedarxuesong.serverlocalizer.utils.translation;

import net.minecraft.util.IChatComponent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息缓存类，用于存储消息ID和原始消息的映射关系
 */
public class ChatMessageCache {
    private static ChatMessageCache instance;
    private final ConcurrentHashMap<String, IChatComponent> messageCache = new ConcurrentHashMap<>();
    
    private ChatMessageCache() {
        // 私有构造函数
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized ChatMessageCache getInstance() {
        if (instance == null) {
            instance = new ChatMessageCache();
        }
        return instance;
    }
    
    /**
     * 缓存消息
     * @param messageId 消息ID
     * @param component 聊天组件
     */
    public void cacheMessage(String messageId, IChatComponent component) {
        messageCache.put(messageId, component);
    }
    
    /**
     * 获取缓存的消息
     * @param messageId 消息ID
     * @return 对应的IChatComponent，如果不存在则返回null
     */
    public IChatComponent getCachedMessage(String messageId) {
        return messageCache.get(messageId);
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        messageCache.clear();
    }
} 