package com.cedarxuesong.serverlocalizer.mixins;

import com.cedarxuesong.serverlocalizer.utils.translation.ModuleTranslationManager;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 处理游戏关闭事件
 */
@Mixin(Minecraft.class)
public class GameShutdownHandler {
    @Unique
    private static final String cedarServerLocalizer$TAG = "GameShutdown";
    
    /**
     * 游戏关闭时触发
     */
    @Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
    public void onGameShutdown(CallbackInfo ci) {
        try {
            mylog.log(cedarServerLocalizer$TAG, "游戏正在关闭，保存翻译文件");
            
            // 保存翻译到文件
            boolean saved = ModuleTranslationManager.getInstance().saveAllTranslations();
            if (saved) {
                mylog.log(cedarServerLocalizer$TAG, "所有翻译文件已保存");
            } else {
                mylog.warn(cedarServerLocalizer$TAG, "部分或全部翻译文件保存失败");
            }
            
        } catch (Exception e) {
            mylog.error(cedarServerLocalizer$TAG, "处理游戏关闭事件时发生错误", e);
        }
    }
} 