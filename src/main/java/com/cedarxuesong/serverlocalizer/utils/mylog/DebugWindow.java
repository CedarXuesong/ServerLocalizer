package com.cedarxuesong.serverlocalizer.utils.mylog;

import com.cedarxuesong.serverlocalizer.Main;
import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.translation.ModuleTranslationManager;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugWindow {
    private static JFrame frame;
    private static JTextArea textArea;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static boolean isInitialized = false;
    private static boolean isWindowVisible = false;
    private static final Object LOCK = new Object();
    private static final int MAX_LINES = 1000; // 最大日志行数
    
    /**
     * 初始化调试窗口，由Mixin或日志系统调用
     */
    public static void init() {
        synchronized (LOCK) {
            if (isInitialized && isWindowVisible) {
                return;
            }
            
            // 在事件分发线程中创建和显示GUI
            SwingUtilities.invokeLater(() -> {
                try {
                    if (frame == null || !isWindowVisible) {
                        frame = new JFrame("调试信息");
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        frame.setSize(1080, 1080);
                        
                        textArea = new JTextArea();
                        textArea.setEditable(false);
                        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                        
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        frame.add(scrollPane);
                        
                        // 创建底部面板放置按钮
                        JPanel bottomPanel = getJPanel();

                        frame.add(bottomPanel, BorderLayout.SOUTH);
                        
                        // 设置窗口位置在屏幕中间
                        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                        frame.setLocation(screenSize.width /2 - frame.getWidth() /2, 
                                        screenSize.height /2 - frame.getHeight() /2);
                        
                        // 添加窗口关闭监听器
                        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                                synchronized (LOCK) {
                                    isWindowVisible = false;
                                }
                            }
                        });
                        
                        // 添加初始化信息
                        String initMessage = String.format("%s [%s] [%s]: %s%n", 
                            dateFormat.format(new Date()), 
                            LogLevel.INFO.name(), 
                            "DebugWindow", 
                            "调试窗口已初始化");
                        textArea.append(initMessage);
                        
                        frame.setVisible(true);
                        isWindowVisible = true;
                        isInitialized = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static JPanel getJPanel() {
        JPanel bottomPanel = new JPanel();

        // 添加清除按钮
        JButton clearButton = new JButton("清除日志");
        clearButton.addActionListener(e -> clearLog());
        bottomPanel.add(clearButton);

        // 添加保存翻译按钮
        JButton saveButton = new JButton("保存翻译");
        saveButton.addActionListener(e -> saveTranslations());
        bottomPanel.add(saveButton);

        // 添加加载翻译按钮
        JButton loadButton = new JButton("加载翻译");
        loadButton.addActionListener(e -> loadTranslations());
        bottomPanel.add(loadButton);

        // 添加翻译更多按钮
        JButton statusButton = new JButton("更多");
        statusButton.addActionListener(e -> showTranslationStatus());
        bottomPanel.add(statusButton);
        return bottomPanel;
    }

    /**
     * 保存翻译文件
     */
    private static void saveTranslations() {
        try {
            // 保存翻译
            ModuleTranslationManager manager = ModuleTranslationManager.getInstance();
            boolean translationResult = manager.saveAllTranslations();
            
            // 保存API配置
            boolean apiConfigResult = ModConfig.getInstance().saveConfig();
            
            String message;
            LogLevel level;
            
            if (translationResult && apiConfigResult) {
                message = "所有翻译文件和API配置已成功保存";
                level = LogLevel.INFO;
            } else if (!translationResult && !apiConfigResult) {
                message = "翻译文件和API配置保存失败";
                level = LogLevel.ERROR;
            } else if (!translationResult) {
                message = "翻译文件保存失败，API配置已成功保存";
                level = LogLevel.WARN;
            } else {
                message = "翻译文件已成功保存，API配置保存失败";
                level = LogLevel.WARN;
            }
            
            addLog("DebugWindow", message, level);
        } catch (Exception e) {
            addLog("DebugWindow", "保存时发生异常: " + e.getMessage(), LogLevel.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * 加载翻译文件
     */
    private static void loadTranslations() {
        try {
            // 重新加载翻译
            ModuleTranslationManager manager = ModuleTranslationManager.getInstance();
            boolean translationResult = manager.reloadAllTranslations();
            
            // 重新加载API配置
            ModConfig modConfig = ModConfig.getInstance();
            boolean apiConfigResult = modConfig.initialize();
            
            String message;
            LogLevel level;
            
            if (translationResult && apiConfigResult) {
                message = "所有翻译文件和API配置已成功加载";
                level = LogLevel.INFO;
            } else if (!translationResult && !apiConfigResult) {
                message = "翻译文件和API配置加载失败";
                level = LogLevel.ERROR;
            } else if (!translationResult) {
                message = "翻译文件加载失败，API配置已成功加载";
                level = LogLevel.WARN;
            } else {
                message = "翻译文件已成功加载，API配置加载失败";
                level = LogLevel.WARN;
            }
            
            addLog("DebugWindow", message, level);
        } catch (Exception e) {
            addLog("DebugWindow", "加载时发生异常: " + e.getMessage(), LogLevel.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * 显示更多翻译状态
     */
    private static void showTranslationStatus() {
        try {
            ModuleTranslationManager manager = ModuleTranslationManager.getInstance();
            boolean isInitialized = manager.isInitialized();
            boolean hasUnsavedChanges = manager.hasUnsavedChanges();
            
            ModConfig modConfig = ModConfig.getInstance();
            String chatApiUrl = modConfig.getBaseUrl("ChatTranslationApi");
            String itemApiUrl = modConfig.getBaseUrl("ItemTranslationApi");

            StringBuilder status = new StringBuilder();
            status.append("管理器状态:\n");
            status.append("- 版本: ").append(Main.VERSION).append("\n");
            status.append("- 初始化: ").append(isInitialized ? "true" : "false").append("\n");
            status.append("- 未保存更改: ").append(hasUnsavedChanges ? "true" : "false").append("\n\n");

            // 获取各个模块的状态
            status.append("模块状态:\n");
            status.append("- 物品翻译: ").append(manager.getItemTranslator().getTranslationCount()).append(" 条\n\n");

            // API配置状态
            status.append("API配置状态:\n");
            status.append("- 聊天翻译API: ").append(chatApiUrl.isEmpty() ? "未配置" : chatApiUrl).append("\n");
            status.append("- 物品翻译API: ").append(itemApiUrl.isEmpty() ? "未配置" : itemApiUrl).append("\n\n");

            status.append("配置目录: ").append(manager.getConfigPath());
            
            JOptionPane.showMessageDialog(frame, status.toString(), "更多信息", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            addLog("DebugWindow", "获取状态时发生异常: " + e.getMessage(), LogLevel.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * 添加日志信息到窗口
     * @param tag 日志标签
     * @param message 日志消息
     * @param level 日志级别（INFO, WARN, ERROR）
     */
    public static void addLog(String tag, String message, LogLevel level) {
        synchronized (LOCK) {
            if (!isWindowVisible) {
                init();
            }
            
            if (frame != null && textArea != null) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String timestamp = dateFormat.format(new Date());
                        String colorCode = level.getColor();
                        String logEntry = String.format("%s [%s] [%s]: %s%n", 
                            timestamp, level.name(), tag, message);
                        
                        // 检查并限制日志行数
                        int lineCount = textArea.getLineCount();
                        if (lineCount >= MAX_LINES) {
                            try {
                                int endOfFirstLine = textArea.getLineEndOffset(0);
                                textArea.replaceRange("", 0, endOfFirstLine);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        
                        textArea.append(logEntry);
                        // 自动滚动到底部
                        textArea.setCaretPosition(textArea.getDocument().getLength());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
    
    /**
     * 清除日志内容
     */
    public static void clearLog() {
        synchronized (LOCK) {
            if (textArea != null) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        textArea.setText("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
    
    /**
     * 关闭调试窗口
     */
    public static void close() {
        synchronized (LOCK) {
            if (frame != null) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        frame.dispose();
                        isWindowVisible = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
    
    /**
     * 检查窗口是否已初始化并可见
     * @return 窗口是否已初始化并可见
     */
    public static boolean isWindowInitialized() {
        synchronized (LOCK) {
            return isInitialized && isWindowVisible;
        }
    }

    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        INFO("黑色"),
        WARN("橙色"),
        ERROR("红色");
        
        private final String color;
        
        LogLevel(String color) {
            this.color = color;
        }
        
        public String getColor() {
            return color;
        }
    }
} 