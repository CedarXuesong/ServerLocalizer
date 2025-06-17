package com.cedarxuesong.serverlocalizer.utils.mylog;

import javax.swing.SwingUtilities;

public class mylog {
    private static final Object lock = new Object();
    private static volatile boolean windowInitialized = false;

    public static void initialize() {
        if (com.cedarxuesong.serverlocalizer.utils.ai.ModConfig.getInstance().isDebugWindowEnabled()) {
            SwingUtilities.invokeLater(() -> {
                synchronized (lock) {
                    DebugWindow.init();
                    windowInitialized = true;
                    lock.notifyAll();
                }
            });
        } else {
            // If the window is not enabled, we still need to mark it as "initialized"
            // to prevent blocking in waitForDebugWindow.
            synchronized (lock) {
                windowInitialized = true;
                lock.notifyAll();
            }
        }
    }

    public static void waitForDebugWindow() {
        if (!com.cedarxuesong.serverlocalizer.utils.ai.ModConfig.getInstance().isDebugWindowEnabled()) {
            return;
        }
        
        synchronized (lock) {
            while (!windowInitialized) {
                try {
                    lock.wait(5000); // Wait for 5 seconds max
                    if (!windowInitialized) {
                        System.err.println("[mylog] 等待调试窗口初始化超时");
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("[mylog] 等待调试窗口时被中断");
                    break;
                }
            }
        }
    }

    public static void log(String tag, String message) {
        if (com.cedarxuesong.serverlocalizer.utils.ai.ModConfig.getInstance().isDebugWindowEnabled()) {
            DebugWindow.addLog(tag, message, DebugWindow.LogLevel.INFO);
        }
    }

    public static void error(String tag, String message, Throwable e) {
        if (com.cedarxuesong.serverlocalizer.utils.ai.ModConfig.getInstance().isDebugWindowEnabled()) {
            String fullMessage = message;
            if (e != null) {
                fullMessage += "\n" + e;
            }
            DebugWindow.addLog(tag, fullMessage, DebugWindow.LogLevel.ERROR);
        }
    }

    public static void error(String tag, String message) {
        error(tag, message, null);
    }
    
    public static void warn(String tag, String message) {
        if (com.cedarxuesong.serverlocalizer.utils.ai.ModConfig.getInstance().isDebugWindowEnabled()) {
            DebugWindow.addLog(tag, message, DebugWindow.LogLevel.WARN);
        }
    }
    
    public static void closeDebugWindow() {
        if (com.cedarxuesong.serverlocalizer.utils.ai.ModConfig.getInstance().isDebugWindowEnabled()) {
            DebugWindow.close();
        }
    }

    public static void debug(String tag, String s) {
        log(tag, s);
    }
}
