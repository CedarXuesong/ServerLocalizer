package com.cedarxuesong.serverlocalizer.utils;

import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.translation.ModuleTranslationManager;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.GuiOpenEvent;
import com.cedarxuesong.serverlocalizer.utils.commands.ServerLocalizerCommand;
import com.cedarxuesong.serverlocalizer.utils.gui.ConfigGui;

/**
 * Forge事件处理器
 * 处理玩家连接/断开连接事件，以及翻译系统的保存操作
 */
@SideOnly(Side.CLIENT)
public class EventHandler {
    private static final String TAG = "EventHandler";
    private boolean isConnected = false;
    private boolean needsSaving = false;
    private int saveRetryCount = 0;
    private static final int MAX_SAVE_RETRIES = 3;

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        try {
            mylog.log(TAG, "客户端已连接到服务器，初始化翻译系统");
            isConnected = true;
            needsSaving = false;
            saveRetryCount = 0;
            
            // 初始化模块化翻译系统
            ModuleTranslationManager manager = ModuleTranslationManager.getInstance();
            if (!manager.isInitialized()) {
                boolean initialized = manager.initialize();
                if (!initialized) {
                    mylog.error(TAG, "翻译系统初始化失败", new Exception("初始化失败"));
                }
            }

            if(!ModConfig.getInstance().isChatTranslationEnabled()&!ModConfig.getInstance().isItemTranslationEnabled()){
                Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages("§r§e[ServerLocalizer] 当前未启用任何功能，请在§r§b模组配置§r§e中设置！");
            }

            // 注意：不需要在这里调用reloadAllTranslations
            // 因为initialize方法中已经加载了所有翻译
            
        } catch (Exception e) {
            mylog.error(TAG, "处理客户端连接事件时发生错误", e);
        }
    }

    @SubscribeEvent
    public void onClientDisconnectionFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        try {
            mylog.log(TAG, "检测到断开连接事件");
            handleDisconnection("断开连接事件");
        } catch (Exception e) {
            mylog.error(TAG, "处理客户端断开连接事件时发生错误", e);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        try {
            if (event.world.isRemote && isConnected) {
                mylog.log(TAG, "检测到世界卸载事件");
                handleDisconnection("世界卸载事件");
            }
        } catch (Exception e) {
            mylog.error(TAG, "处理世界卸载事件时发生错误", e);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && needsSaving && saveRetryCount < MAX_SAVE_RETRIES) {
            try {
                mylog.log(TAG, "尝试保存翻译文件 (重试次数: " + saveRetryCount + ")");
                ModuleTranslationManager manager = ModuleTranslationManager.getInstance();
                
                if (manager.hasUnsavedChanges()) {
                    boolean saved = manager.saveAllTranslations();
                if (saved) {
                        mylog.log(TAG, "所有翻译模块保存成功");
                        needsSaving = false;
                        saveRetryCount = 0;
                    } else {
                        saveRetryCount++;
                        mylog.warn(TAG, "部分翻译模块保存失败，将在下一个tick重试");
                    }
                } else {
                    needsSaving = false;
                    saveRetryCount = 0;
                }
            } catch (Exception e) {
                mylog.error(TAG, "在tick事件中保存翻译文件时发生错误", e);
                saveRetryCount++;
            }
        }

        if (event.phase == TickEvent.Phase.END) {
            if (ServerLocalizerCommand.shouldOpenConfigGui) {
                // Check if there is no screen currently open
                if (Minecraft.getMinecraft().currentScreen == null) {
                    mylog.log(TAG, "检测到GUI标志，正在打开ConfigGui...");
                    Minecraft.getMinecraft().displayGuiScreen(new ConfigGui(null));
                    ServerLocalizerCommand.shouldOpenConfigGui = false; // Reset the flag
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiChat) {
            return;
            //这么写就对了
        }
    }

    private void handleDisconnection(String trigger) {
        if (isConnected) {
            mylog.log(TAG, "触发保存操作 (来自: " + trigger + ")");
            isConnected = false;
            needsSaving = true;
            saveRetryCount = 0;
            
            try {
                ModuleTranslationManager manager = ModuleTranslationManager.getInstance();
                if (manager.hasUnsavedChanges()) {
                    boolean saved = manager.saveAllTranslations();
                if (saved) {
                        mylog.log(TAG, "所有翻译模块立即保存成功");
                    needsSaving = false;
                    } else {
                        mylog.warn(TAG, "部分翻译模块立即保存失败，已加入重试队列");
                    }
                } else {
                    needsSaving = false;
                }
            } catch (Exception e) {
                mylog.error(TAG, "立即保存翻译文件时发生错误，已加入重试队列", e);
            }
        }
    }
} 