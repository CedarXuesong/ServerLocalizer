package com.cedarxuesong.serverlocalizer;

import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.translation.ModuleTranslationManager;
import com.cedarxuesong.serverlocalizer.utils.EventHandler;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import com.cedarxuesong.serverlocalizer.utils.commands.ServerLocalizerCommand;
import com.cedarxuesong.serverlocalizer.utils.commands.ChatTranslateCommand;
import com.cedarxuesong.serverlocalizer.utils.translation.ItemTranslationOnTick;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

@Mod(modid = Main.MODID, version = Main.VERSION, guiFactory = "com.cedarxuesong.serverlocalizer.utils.gui.ConfigGuiFactory")
public class Main {
    public static final String MODID = "serverlocalizer";
    public static final String VERSION = "0.4-beta";
    private static final String TAG = "Main";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // 获取配置目录
        File configDir = new File(event.getModConfigurationDirectory(), MODID);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 初始化API配置
        try {
            ModConfig.getInstance(); // 现在可以处理初始加载了
            mylog.log(TAG, "API配置初始化完成");
        } catch (Exception e) {
            mylog.error(TAG, "API配置初始化失败", e);
        }

        // 根据配置初始化调试窗口
        mylog.initialize();
        
        // 等待调试窗口初始化完成
        mylog.waitForDebugWindow();
        
        // 使用mylog示例
        mylog.log(TAG, "模组预初始化");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // 注册事件监听器
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandler()); // 注册玩家事件处理器
        MinecraftForge.EVENT_BUS.register(new ItemTranslationOnTick()); // 注册物品翻译计时器
        
        // 初始化翻译管理器
        ModuleTranslationManager.getInstance().initialize();
        
        // 注册客户端命令
        ClientCommandHandler.instance.registerCommand(new ServerLocalizerCommand());
        mylog.log(TAG, "已注册客户端命令处理器");
        
        mylog.log(TAG, "模组初始化完成");
    }
    
    @Mod.EventHandler
    public void onDisable(FMLModDisabledEvent event) {
        try {
            // 关闭翻译线程池
            ChatTranslateCommand.shutdown();
            mylog.log(TAG, "已关闭翻译线程池");
            
            // 保存所有翻译
            ModuleTranslationManager.getInstance().saveAllTranslations();
            mylog.log(TAG, "已保存所有翻译数据");
            
            // 保存API配置
            ModConfig.getInstance().saveConfig();
            mylog.log(TAG, "已保存API配置");
        } catch (Exception e) {
            mylog.error(TAG, "模组卸载时发生错误", e);
        }
    }
}
 