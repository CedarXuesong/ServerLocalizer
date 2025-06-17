package com.cedarxuesong.serverlocalizer.utils.gui;

import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.BasePanel;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.ChatTranslationPanel;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.DeveloperOptionsPanel;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.ItemTranslationPanel;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.ProjectInfoPanel;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 配置GUI界面
 */
public class ConfigGui extends GuiScreen {
    private static final String TAG = "ConfigGui";
    
    // --- Theming ---
    public static final int COLOR_BACKGROUND = 0xFF202225;
    public static final int COLOR_PANEL_BACKGROUND = 0xFF2F3136;
    public static final int COLOR_TEXT_WHITE = 0xFFFFFFFF;
    public static final int COLOR_TEXT_HEADER = 0xFFFFFFFF;
    public static final int COLOR_TEXT_LABEL = 0xFF8E9297;
    public static final int COLOR_TEXT_HIGHLIGHT = 0xFFF9A825;
    public static final int COLOR_SCROLLBAR_BG = 0x50000000;
    public static final int COLOR_SCROLLBAR_HANDLE = 0x80FFFFFF;
    
    // 滚动相关
    private float scrollOffset = 0.0f;
    private float targetScrollOffset = 0.0f;
    private float maxScrollOffset = 0.0f;
    private boolean isDragging = false;
    private int lastMouseY = 0;
    private long lastFrameTime = 0L;
    
    // Category selection indicator animation
    // 分类选择指示器动画
    private float[] categoryIndicatorWidths = new float[4]; // 指示器当前宽度
    private float[] targetCategoryIndicatorWidths = new float[4]; // 指示器目标宽度
    
    // 分类按钮
    private ModernButton projectInfoButton;
    private ModernButton itemTranslationButton;
    private ModernButton chatTranslationButton;
    private ModernButton developerOptionsButton;
    
    // GUI控件
    private GuiTextField itemBaseUrlField;
    private GuiTextField itemApiKeyField;
    private GuiTextField itemModelField;
    private GuiTextField itemTemperatureField;
    private GuiTextField itemSystemPromptField;
    
    private GuiTextField chatBaseUrlField;
    private GuiTextField chatApiKeyField;
    private GuiTextField chatModelField;
    private GuiTextField chatTemperatureField;
    private GuiTextField chatSystemPromptField;
    
    private ModernButton saveButton;
    private ModernButton cancelButton;
    private ModernButton resetButton;
    
    // 开关按钮
    private GuiButton itemTranslationToggle;
    private GuiButton itemNameToggle;
    private GuiButton itemLoreToggle;
    private GuiButton chatTranslationToggle;
    
    // 配置管理器
    private final ModConfig modConfig;
    
    // 分类和面板管理
    private int selectedCategory = 0;
    private final List<BasePanel> panels = new ArrayList<>();
    private BasePanel currentPanel;
    
    private final GuiScreen parentScreen;
    
    public ConfigGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        this.modConfig = ModConfig.getInstance();
        // 创建所有面板实例
        panels.add(new ProjectInfoPanel(this));
        panels.add(new ItemTranslationPanel(this));
        panels.add(new ChatTranslationPanel(this));
        panels.add(new DeveloperOptionsPanel(this));
    }
    
    public ConfigGui() {
        this(null);
    }
    
    @Override
    public void initGui() {
        mylog.log(TAG, "ConfigGui 开始初始化 (initGui)...");
        super.initGui();
        this.lastFrameTime = System.currentTimeMillis();
        this.buttonList.clear();
        
        int leftPanelWidth = 100;
        int rightPanelX = leftPanelWidth + 20;
        int rightPanelWidth = this.width - rightPanelX - 15;
        
        // 初始化所有面板
        for (BasePanel panel : panels) {
            panel.initGui(rightPanelWidth);
        }
        
        // 初始化指示器动画状态
        int categoryButtonWidth = 90;
        int indicatorTargetWidth = (int) (categoryButtonWidth * 0.6f); // 指示器展开后的目标宽度，为按钮宽度的60%
        for (int i = 0; i < 4; i++) {
            this.targetCategoryIndicatorWidths[i] = (i == this.selectedCategory) ? indicatorTargetWidth : 0;
            this.categoryIndicatorWidths[i] = this.targetCategoryIndicatorWidths[i];
        }
        
        // 左侧分类按钮
        int categoryButtonX = 10;
        int categoryButtonHeight = 20;
        int categoryStartY = 40;
        this.projectInfoButton = new ModernButton(300, categoryButtonX, categoryStartY, categoryButtonWidth, categoryButtonHeight, "");
        this.itemTranslationButton = new ModernButton(301, categoryButtonX, categoryStartY + 25, categoryButtonWidth, categoryButtonHeight, "");
        this.chatTranslationButton = new ModernButton(302, categoryButtonX, categoryStartY + 50, categoryButtonWidth, categoryButtonHeight, "");
        this.developerOptionsButton = new ModernButton(303, categoryButtonX, categoryStartY + 75, categoryButtonWidth, categoryButtonHeight, "");
        this.buttonList.add(this.projectInfoButton);
        this.buttonList.add(this.itemTranslationButton);
        this.buttonList.add(this.chatTranslationButton);
        this.buttonList.add(this.developerOptionsButton);
        
        // 左下角按钮
        int bottomButtonWidth = 60;
        int bottomButtonHeight = 20;
        int bottomButtonY = this.height - 30;
        int bottomButtonSpacing = 5;
        this.resetButton = new ModernButton(102, 10, bottomButtonY, bottomButtonWidth, bottomButtonHeight, "重置", 0xFFB08C4A, 0xFF8C6D3A);
        this.saveButton = new ModernButton(100, 10 + bottomButtonWidth + bottomButtonSpacing, bottomButtonY, bottomButtonWidth, bottomButtonHeight, "保存", 0xFF558E74, 0xFF436E5A);
        this.cancelButton = new ModernButton(101, 10 + (bottomButtonWidth + bottomButtonSpacing) * 2, bottomButtonY, bottomButtonWidth, bottomButtonHeight, "取消", 0xFFB35959, 0xFF8C4545);
        this.buttonList.add(this.saveButton);
        this.buttonList.add(this.cancelButton);
        this.buttonList.add(this.resetButton);
        
        // 设置初始面板
        setCurrentPanel(this.selectedCategory);
        updateCategoryButtonVisuals();
    }
    
    private void setCurrentPanel(int categoryIndex) {
        this.selectedCategory = categoryIndex;
        this.currentPanel = panels.get(categoryIndex);
        
        // 更新滚动范围
        calculateMaxScrollOffset();
    }
    
    /**
     * 计算最大滚动偏移量
     */
    private void calculateMaxScrollOffset() {
        if (this.currentPanel == null) return;
        int contentHeight = this.currentPanel.getContentHeight();
        int visibleHeight = this.height - 80; // 减去顶部和底部边距
        this.maxScrollOffset = Math.max(0, contentHeight - visibleHeight);
    }
    
    /**
     * 更新动画
     */
    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - this.lastFrameTime;
        this.lastFrameTime = currentTime;

        if (deltaTime > 50L) deltaTime = 50L;
        
        double k = 0.02; // 动画平滑系数，值越小动画越慢
        float amountToMove = (float) (1.0 - Math.exp(-k * deltaTime));

        // 滚动动画
        if (Math.abs(targetScrollOffset - scrollOffset) > 0.1f) {
            scrollOffset += (targetScrollOffset - scrollOffset) * amountToMove;
        } else {
            scrollOffset = targetScrollOffset;
        }

        // 分类指示器宽度动画
        for (int i = 0; i < 4; i++) {
            if (Math.abs(targetCategoryIndicatorWidths[i] - categoryIndicatorWidths[i]) > 0.01f) {
                // 使用非线性插值使宽度平滑过渡到目标宽度
                categoryIndicatorWidths[i] += (targetCategoryIndicatorWidths[i] - categoryIndicatorWidths[i]) * amountToMove;
            } else {
                categoryIndicatorWidths[i] = targetCategoryIndicatorWidths[i];
            }
        }
        
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScrollOffset));
    }
    
    /**
     * 处理鼠标滚轮
     */
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int rightPanelX = 115;
        if (mouseX >= rightPanelX && mouseX < this.width - 10) {
            int dWheel = Mouse.getEventDWheel();
            if (dWheel != 0) {
                targetScrollOffset -= (dWheel > 0 ? 20.0f : -20.0f);
            }
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, COLOR_BACKGROUND);
        updateAnimations();

        this.drawCenteredString(this.fontRendererObj, "ServerLocalizer 配置", this.width / 2, 15, COLOR_TEXT_HEADER);

        int leftPanelWidth = 100;
        int panelTopMargin = 35;
        int panelBottomMargin = 40;
        
        GuiUtils.drawRoundedRect(5, panelTopMargin, leftPanelWidth, this.height - panelTopMargin - panelBottomMargin, 8, COLOR_PANEL_BACKGROUND);
        
        // 绘制分类按钮下方的选择指示器
        ModernButton[] categoryButtons = {this.projectInfoButton, this.itemTranslationButton, this.chatTranslationButton, this.developerOptionsButton};
        int indicatorHeight = 2; // 指示器高度
        int indicatorColor = 0xFF5865F2; // 指示器颜色 (蓝色)

        for (int i = 0; i < categoryButtons.length; i++) {
            ModernButton button = categoryButtons[i];
            float currentWidth = this.categoryIndicatorWidths[i];
            if (currentWidth > 0.5f) { // 当前宽度大于0.5时才进行绘制
                // 从按钮中心点开始计算，使其实现中心展开/收缩的效果
                float x = button.xPosition + (button.width / 2.0f) - (currentWidth / 2.0f);
                float y = button.yPosition + button.height + 2; // 绘制在按钮下方，有2像素间距
                GuiUtils.drawRoundedRect(x, y, currentWidth, indicatorHeight, 1, indicatorColor);
            }
        }
        
        int rightPanelX = leftPanelWidth + 15;
        GuiUtils.drawRoundedRect(rightPanelX, panelTopMargin, this.width - rightPanelX - 10, this.height - panelTopMargin - panelBottomMargin, 8, COLOR_PANEL_BACKGROUND);
        
        // --- 绘制右侧滚动面板 ---
        GlStateManager.pushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        
        int scale = this.mc.gameSettings.guiScale == 0 ? 4 : this.mc.gameSettings.guiScale;
        int scissorX = (rightPanelX - 5);
        int scissorY = (this.height - (this.height - panelBottomMargin));
        int scissorWidth = (this.width - 10 - scissorX);
        int scissorHeight = (this.height - panelTopMargin - panelBottomMargin);
        
        GL11.glScissor(
            scissorX * this.mc.displayWidth / this.width,
            scissorY * this.mc.displayHeight / this.height,
            scissorWidth * this.mc.displayWidth / this.width,
            scissorHeight * this.mc.displayHeight / this.height
        );
        
        GlStateManager.translate(0.0F, -this.scrollOffset, 0.0F);

        int localMouseY = mouseY + (int)this.scrollOffset;
        
        if (currentPanel != null) {
            // Start content from the top of the panel
            currentPanel.drawPanel(mouseX, localMouseY, partialTicks, rightPanelX, panelTopMargin);
            
            // 绘制面板中的按钮和文本框
            for (GuiButton button : currentPanel.buttons) {
                 button.drawButton(this.mc, mouseX, localMouseY);
            }
            for (GuiTextField textField : currentPanel.textFields) {
                textField.drawTextBox();
            }
        }
        
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        
        if (maxScrollOffset > 0) {
            drawScrollbar(rightPanelX, panelTopMargin, this.width - 10, this.height - panelBottomMargin);
        }
        
        // 绘制固定按钮
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        // 绘制 Tooltips
        drawTooltips(mouseX, mouseY);
    }
    
    /**
     * 绘制滚动条
     */
    private void drawScrollbar(int panelLeft, int panelTop, int panelRight, int panelBottom) {
        if (currentPanel == null || maxScrollOffset <= 0) return;

        int scrollbarWidth = 6;
        int padding = 4; // 面板边缘的边距

        // 滚动条滑道
        int trackX = panelRight - scrollbarWidth - padding;
        int trackY = panelTop + padding;
        int trackHeight = panelBottom - panelTop - (padding * 2);
        GuiUtils.drawRoundedRect(trackX, trackY, scrollbarWidth, trackHeight, 3, COLOR_SCROLLBAR_BG);

        // 滚动条滑块
        float visibleRatio = (float)(panelBottom - panelTop) / (float)currentPanel.getContentHeight();
        int thumbHeight = Math.max(20, (int)(trackHeight * visibleRatio));
        thumbHeight = Math.min(trackHeight, thumbHeight);

        float scrollRatio = scrollOffset / maxScrollOffset;
        int thumbY = trackY + (int) ((trackHeight - thumbHeight) * scrollRatio);

        GuiUtils.drawRoundedRect(trackX, thumbY, scrollbarWidth, thumbHeight, 3, COLOR_SCROLLBAR_HANDLE);
    }
    
    /**
     * 绘制所有控件的悬浮提示
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    private void drawTooltips(int mouseX, int mouseY) {
        // 固定按钮 Tooltips
        if (this.saveButton.isMouseOver()) this.drawHoveringText(Arrays.asList("§e保存", "保存所有更改并关闭界面。"), mouseX, mouseY);
        if (this.cancelButton.isMouseOver()) this.drawHoveringText(Arrays.asList("§e取消", "放弃所有未保存的更改并关闭界面。"), mouseX, mouseY);
        if (this.resetButton.isMouseOver()) this.drawHoveringText(Arrays.asList("§e重置", "将界面中的所有设置恢复为", "上次保存时的状态。"), mouseX, mouseY);
        if (this.projectInfoButton.isMouseOver()) this.drawHoveringText(Arrays.asList("§e项目简介", "查看模组的基本信息、功能介绍和作者。"), mouseX, mouseY);
        if (this.itemTranslationButton.isMouseOver()) this.drawHoveringText(Arrays.asList("§e物品翻译设置", "配置与物品名称和描述翻译相关的所有选项。"), mouseX, mouseY);
        if (this.chatTranslationButton.isMouseOver()) this.drawHoveringText(Arrays.asList("§e聊天翻译设置", "配置与聊天内容翻译相关的所有选项。"), mouseX, mouseY);
        if (this.developerOptionsButton.isMouseOver()) this.drawHoveringText(Arrays.asList("§e开发者选项", "用于配置模组的调试和开发功能。"), mouseX, mouseY);

        // 面板内 Tooltips
        int rightPanelX = 115;
        int panelTopMargin = 35;
        int panelBottomMargin = 40;
        if (mouseX >= rightPanelX - 5 && mouseX < this.width - 10 &&
            mouseY >= panelTopMargin && mouseY < this.height - panelBottomMargin && currentPanel != null) {
            int localMouseY = mouseY + (int)scrollOffset;
            currentPanel.addTooltips(mouseX, mouseY, localMouseY);
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
        if (currentPanel != null) {
            currentPanel.keyTyped(typedChar, keyCode);
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        // 分类切换按钮
        if (button.id >= 300 && button.id <= 303) {
            int newCategory = button.id - 300;
            if (this.selectedCategory != newCategory) {
                setCurrentPanel(newCategory);
                this.scrollOffset = 0;
                this.targetScrollOffset = 0;
                // 当切换分类时，更新所有指示器的目标宽度，触发并行动画
                int indicatorTargetWidth = (int) (this.projectInfoButton.width * 0.6f);
                for (int i = 0; i < 4; i++) {
                    // 新选中的分类目标宽度为展开宽度，其他分类目标宽度为0（收缩）
                    this.targetCategoryIndicatorWidths[i] = (i == this.selectedCategory) ? indicatorTargetWidth : 0;
                }
                updateCategoryButtonVisuals();
            }
            return;
        }

        // 底部按钮
        switch (button.id) {
            case 100: // 保存
                saveConfig();
                break;
            case 101: // 取消
                this.mc.displayGuiScreen(this.parentScreen);
                break;
            case 102: // 重置
                modConfig.resetToDefaults();
                // Re-initialize the GUI to reflect the changes
                this.mc.displayGuiScreen(new ConfigGui(this.parentScreen));
                break;
        }
        
        // 委托给当前面板处理
        if (currentPanel != null) {
            currentPanel.actionPerformed(button);
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // 面板内点击
        int rightPanelX = 115;
        int panelTopMargin = 35;
        int panelBottomMargin = 40;
        if (mouseX >= rightPanelX - 5 && mouseX < this.width - 10 &&
            mouseY >= panelTopMargin && mouseY < this.height - panelBottomMargin && currentPanel != null) {
            
            int localMouseY = mouseY + (int) scrollOffset;
            currentPanel.mouseClicked(mouseX, localMouseY, mouseButton);
            
            if (mouseButton == 0) {
                 for (GuiButton button : currentPanel.buttons) {
                    if (button.mousePressed(this.mc, mouseX, localMouseY)) {
                        this.actionPerformed(button);
                        return;
                    }
                }
            }
        }
    }
    
    private void saveConfig() {
        try {
            for(BasePanel panel : panels) {
                panel.saveConfig();
            }
            if (modConfig.saveConfig()) {
                mylog.log(TAG, "配置保存成功");
                modConfig.initialize();
                this.mc.displayGuiScreen(this.parentScreen);
            } else {
                mylog.error(TAG, "配置保存失败");
            }
        } catch (Exception e) {
            mylog.error(TAG, "保存配置时发生错误", e);
        }
    }
    
    /**
     * 重置配置
     */
    private void resetConfig() {
        for(BasePanel panel : panels) {
            panel.resetConfig();
        }
        mylog.log(TAG, "配置已重置");
    }
    
    private void updateCategoryButtonVisuals() {
        this.projectInfoButton.displayString = "项目简介";
        this.itemTranslationButton.displayString = "物品翻译";
        this.chatTranslationButton.displayString = "聊天翻译";
        this.developerOptionsButton.displayString = "开发者选项";
        
        this.projectInfoButton.packedFGColour = (this.selectedCategory == 0) ? COLOR_TEXT_HIGHLIGHT : COLOR_TEXT_WHITE;
        this.itemTranslationButton.packedFGColour = (this.selectedCategory == 1) ? COLOR_TEXT_HIGHLIGHT : COLOR_TEXT_WHITE;
        this.chatTranslationButton.packedFGColour = (this.selectedCategory == 2) ? COLOR_TEXT_HIGHLIGHT : COLOR_TEXT_WHITE;
        this.developerOptionsButton.packedFGColour = (this.selectedCategory == 3) ? COLOR_TEXT_HIGHLIGHT : COLOR_TEXT_WHITE;

        // For category buttons, we use a transparent background and rely on text color and position to show selection
        this.projectInfoButton.setColors(0x00000000, 0x30FFFFFF);
        this.itemTranslationButton.setColors(0x00000000, 0x30FFFFFF);
        this.chatTranslationButton.setColors(0x00000000, 0x30FFFFFF);
        this.developerOptionsButton.setColors(0x00000000, 0x30FFFFFF);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    public void drawPublicHoveringText(List<String> textLines, int x, int y) {
        super.drawHoveringText(textLines, x, y);
    }

    @Override
    public void onGuiClosed() {
        mylog.log(TAG, "ConfigGui 正在关闭 (onGuiClosed)...");
        super.onGuiClosed();
    }
} 