package com.cedarxuesong.serverlocalizer.utils.gui;

import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.BasePanel;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.ChatTranslationPanel;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.DeveloperOptionsPanel;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.ItemTranslationPanel;
import com.cedarxuesong.serverlocalizer.utils.gui.panel.ProjectInfoPanel;
import com.cedarxuesong.serverlocalizer.utils.Lang;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 配置GUI界面
 */
public class ConfigGui extends GuiScreen {
    private static final String TAG = "ConfigGui";
    
    // --- Animation Speeds ---
    private static final double ANIMATION_SPEED_DEFAULT = 0.015;
    
    // --- Theming ---
    public static final int COLOR_BACKGROUND = 0xFF202225;
    public static final int COLOR_PANEL_BACKGROUND = 0xFF2F3136;
    public static final int COLOR_TEXT_WHITE = 0xFFFFFFFF;
    public static final int COLOR_TEXT_HEADER = 0xFFFFFFFF;
    public static final int COLOR_TEXT_LABEL = 0xFF8E9297;
    public static final int COLOR_TEXT_HIGHLIGHT = 0xFFF9A825;
    public static final int COLOR_SCROLLBAR_BG = 0x50000000;
    public static final int COLOR_SCROLLBAR_HANDLE = 0x80FFFFFF;
    public static final int COLOR_SCROLLBAR_HANDLE_HOVER = new Color(136, 136, 136, 226).getRGB();
    
    // 滚动相关
    private float scrollOffset = 0.0f;
    private float targetScrollOffset = 0.0f;
    private float maxScrollOffset = 0.0f;
    private boolean isDraggingScrollbar = false;
    private int scrollbarDragStartY = 0;
    private float scrollbarDragStartOffset = 0.0f;
    private boolean isScrollbarHovered = false;
    private long lastFrameTime = 0L;
    
    // 惯性滚动与回弹物理模拟
    private float scrollVelocity = 0.0f;
    private final List<long[]> dragVelocityTracker = new ArrayList<>(); // [timestamp, mouseY]
    private static final float FRICTION = 0.96f;
    private static final float MOUSE_WHEEL_SCROLL_AMOUNT = 20.0f;
    private static final float FLING_VELOCITY_SCALE = 1.0f;

    // 滚动条滑块动画
    private float scrollbarHandleWidth;
    private float targetScrollbarHandleWidth;
    private float scrollbarHandleHeight;
    private float targetScrollbarHandleHeight;
    
    // 滚动条常量
    private static final int SCROLLBAR_WIDTH_NORMAL = 6;
    private static final int SCROLLBAR_WIDTH_DRAGGING = 10;
    private static final int SCROLLBAR_HEIGHT_GROWTH = 4;
    private static final int SCROLLBAR_PADDING = 4;
    
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
        
        this.scrollbarHandleWidth = SCROLLBAR_WIDTH_NORMAL;
        this.targetScrollbarHandleWidth = SCROLLBAR_WIDTH_NORMAL;
        
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
        this.resetButton = new ModernButton(102, 10, bottomButtonY, bottomButtonWidth, bottomButtonHeight, Lang.translate("gui.serverlocalizer.reset"), Theme.COLOR_HIGHLIGHT_YELLOW, Theme.COLOR_HIGHLIGHT_YELLOW_HOVER);
        this.saveButton = new ModernButton(100, 10 + bottomButtonWidth + bottomButtonSpacing, bottomButtonY, bottomButtonWidth, bottomButtonHeight, Lang.translate("gui.serverlocalizer.save"), Theme.COLOR_HIGHLIGHT_GREEN, Theme.COLOR_HIGHLIGHT_GREEN_HOVER);
        this.cancelButton = new ModernButton(101, 10 + (bottomButtonWidth + bottomButtonSpacing) * 2, bottomButtonY, bottomButtonWidth, bottomButtonHeight, Lang.translate("gui.serverlocalizer.cancel"), Theme.COLOR_HIGHLIGHT_RED, Theme.COLOR_HIGHLIGHT_RED_HOVER);
        this.buttonList.add(this.saveButton);
        this.buttonList.add(this.cancelButton);
        this.buttonList.add(this.resetButton);
        
        // 设置初始面板
        setCurrentPanel(this.selectedCategory);
        updateCategoryButtonVisuals();
    }
    
    @Override
    public void handleKeyboardInput() throws IOException {
        char eventChar = Keyboard.getEventCharacter();
        int eventKey = Keyboard.getEventKey();

        if ((eventKey == 0 && eventChar >= ' ') || Keyboard.getEventKeyState()) {
            this.keyTyped(eventChar, eventKey);
        }

        this.mc.dispatchKeypresses();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (currentPanel != null) {
            for (ModernTextField textField : currentPanel.textFields) {
                textField.updateCursorCounter();
            }
        }
    }
    
    private void setCurrentPanel(int categoryIndex) {
        this.selectedCategory = categoryIndex;
        this.currentPanel = panels.get(categoryIndex);
        
        // 更新滚动范围
        calculateMaxScrollOffset();
        this.scrollOffset = 0;
        this.targetScrollOffset = 0;
        this.scrollVelocity = 0;

        // 初始化滑块高度
        if (this.currentPanel != null) {
            int contentHeight = this.currentPanel.getContentHeight();
            int visibleHeight = this.height - 80;
            int trackHeight = visibleHeight - (SCROLLBAR_PADDING * 2);
            if (contentHeight > visibleHeight) {
                float visibleRatio = (float)visibleHeight / (float)contentHeight;
                int baseThumbHeight = Math.max(20, (int)(trackHeight * visibleRatio));
                this.scrollbarHandleHeight = baseThumbHeight;
                this.targetScrollbarHandleHeight = baseThumbHeight;
            }
        }
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
     * 外部调用的滚动更新方法
     */
    public void updateScrolling() {
        calculateMaxScrollOffset();
    }
    
    /**
     * 更新动画
     */
    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - this.lastFrameTime;
        this.lastFrameTime = currentTime;

        if (deltaTime > 50L) deltaTime = 50L;
        
        float amountToMove = (float) (1.0 - Math.exp(-ANIMATION_SPEED_DEFAULT * deltaTime));

        // --- 物理模拟 ---
        if (!isDraggingScrollbar) {
            // 应用速度和摩擦力
            targetScrollOffset += scrollVelocity;
            scrollVelocity *= FRICTION;
            
            // 如果速度足够小，则停止运动
            if (Math.abs(scrollVelocity) < 0.1f) {
                scrollVelocity = 0;
            }
            
            // 检查是否越界，如果越界则吸附到边界 (无回弹)
            if (targetScrollOffset > maxScrollOffset) {
                targetScrollOffset = maxScrollOffset;
                scrollVelocity = 0; // 撞墙后速度清零
            } else if (targetScrollOffset < 0) {
                targetScrollOffset = 0;
                scrollVelocity = 0; // 撞墙后速度清零
            }
        }

        // 滚动动画 (视觉跟随物理)
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
        
        // 滚动条滑块尺寸动画
        if (Math.abs(targetScrollbarHandleWidth - scrollbarHandleWidth) > 0.01f) {
            scrollbarHandleWidth += (targetScrollbarHandleWidth - scrollbarHandleWidth) * amountToMove;
        } else {
            scrollbarHandleWidth = targetScrollbarHandleWidth;
        }
        if (Math.abs(targetScrollbarHandleHeight - scrollbarHandleHeight) > 0.01f) {
            scrollbarHandleHeight += (targetScrollbarHandleHeight - scrollbarHandleHeight) * amountToMove;
        } else {
            scrollbarHandleHeight = targetScrollbarHandleHeight;
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
                // 停止任何现有的惯性，直接修改目标偏移量
                this.scrollVelocity = 0;
                this.targetScrollOffset -= (dWheel > 0 ? MOUSE_WHEEL_SCROLL_AMOUNT : -MOUSE_WHEEL_SCROLL_AMOUNT);
                // 立即将滚动目标限制在边界内
                this.targetScrollOffset = Math.max(0, Math.min(this.targetScrollOffset, this.maxScrollOffset));
            }
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, Theme.COLOR_BACKGROUND);
        updateAnimations();

        this.drawCenteredString(this.fontRendererObj, Lang.translate("gui.serverlocalizer.title"), this.width / 2, 15, Theme.COLOR_TEXT_WHITE);

        int leftPanelWidth = 100;
        int panelTopMargin = 35;
        int panelBottomMargin = 40;
        
        GuiUtils.drawRoundedRect(5, panelTopMargin, leftPanelWidth, this.height - panelTopMargin - panelBottomMargin, 8, Theme.COLOR_PANEL_BACKGROUND);
        
        // 绘制分类按钮下方的选择指示器
        ModernButton[] categoryButtons = {this.projectInfoButton, this.itemTranslationButton, this.chatTranslationButton, this.developerOptionsButton};
        int indicatorHeight = 2; // 指示器高度
        int indicatorColor = Theme.COLOR_ACCENT;

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
        GuiUtils.drawRoundedRect(rightPanelX, panelTopMargin, this.width - rightPanelX - 10, this.height - panelTopMargin - panelBottomMargin, 8, Theme.COLOR_PANEL_BACKGROUND);
        
        // --- 绘制右侧滚动面板 ---
        GuiUtils.SCISSOR_STACK.push(rightPanelX, panelTopMargin, this.width - rightPanelX - 15, this.height - panelTopMargin - panelBottomMargin);
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -this.scrollOffset, 0.0F);

        int localMouseY = mouseY + (int)this.scrollOffset;
        
        if (currentPanel != null) {
            // Start content from the top of the panel
            currentPanel.drawPanel(mouseX, localMouseY, partialTicks, rightPanelX, panelTopMargin);
            
            // 绘制面板中的按钮和文本框
            for (GuiButton button : currentPanel.buttons) {
                 button.drawButton(this.mc, mouseX, localMouseY);
            }
            for (ModernTextField textField : currentPanel.textFields) {
                textField.drawTextBox(this.scrollOffset);
            }
        }
        
        GlStateManager.popMatrix();
        GuiUtils.SCISSOR_STACK.pop();
        
        if (maxScrollOffset > 0) {
            drawScrollbar(mouseX, mouseY, rightPanelX, panelTopMargin, this.width - 10, this.height - panelBottomMargin);
        }
        
        // 绘制固定按钮
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        // 绘制 Tooltips
        drawTooltips(mouseX, mouseY);
    }
    
    /**
     * 绘制滚动条
     */
    private void drawScrollbar(int mouseX, int mouseY, int panelLeft, int panelTop, int panelRight, int panelBottom) {
        if (currentPanel == null || maxScrollOffset <= 0) return;

        // --- 计算滑轨和滑块基础尺寸 ---
        int trackX = panelRight - SCROLLBAR_WIDTH_DRAGGING - SCROLLBAR_PADDING;
        int trackY = panelTop + SCROLLBAR_PADDING;
        int trackHeight = panelBottom - panelTop - (SCROLLBAR_PADDING * 2);
        
        // --- 绘制滑轨 ---
        GuiUtils.drawRoundedRect(trackX + (SCROLLBAR_WIDTH_DRAGGING - SCROLLBAR_WIDTH_NORMAL) / 2, trackY, SCROLLBAR_WIDTH_NORMAL, trackHeight, 3, Theme.COLOR_SCROLLBAR_TRACK);

        // --- 计算滑块 ---
        float visibleRatio = (float)(panelBottom - panelTop) / (float)currentPanel.getContentHeight();
        int baseThumbHeight = Math.max(20, (int)(trackHeight * visibleRatio));
        baseThumbHeight = Math.min(trackHeight, baseThumbHeight);

        float scrollRatio = maxScrollOffset > 0 ? scrollOffset / maxScrollOffset : 0;
        int thumbY = trackY + (int) ((trackHeight - baseThumbHeight) * scrollRatio);

        // --- 悬浮检测 ---
        float currentHandleX = trackX + (SCROLLBAR_WIDTH_DRAGGING - scrollbarHandleWidth) / 2;
        float currentHandleY = thumbY - (scrollbarHandleHeight - baseThumbHeight) / 2;
        this.isScrollbarHovered = mouseX >= currentHandleX && mouseX < currentHandleX + scrollbarHandleWidth && 
                                  mouseY >= currentHandleY && mouseY < currentHandleY + scrollbarHandleHeight;

        // --- 更新动画目标 ---
        if (this.isDraggingScrollbar) {
            this.targetScrollbarHandleWidth = SCROLLBAR_WIDTH_DRAGGING;
            this.targetScrollbarHandleHeight = baseThumbHeight + SCROLLBAR_HEIGHT_GROWTH;
        } else {
            this.targetScrollbarHandleWidth = SCROLLBAR_WIDTH_NORMAL;
            this.targetScrollbarHandleHeight = baseThumbHeight;
        }
        
        // --- 决定颜色 ---
        int handleColor = this.isDraggingScrollbar ? Theme.COLOR_ACCENT : (this.isScrollbarHovered ? Theme.COLOR_SCROLLBAR_HANDLE_HOVER : Theme.COLOR_SCROLLBAR_HANDLE);
        
        // --- 绘制滑块 ---
        GuiUtils.drawRoundedRect(currentHandleX, currentHandleY, scrollbarHandleWidth, scrollbarHandleHeight, (int)Math.floor(scrollbarHandleWidth / 2f), handleColor);
    }
    
    /**
     * 绘制所有控件的悬浮提示
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    private void drawTooltips(int mouseX, int mouseY) {
        // 固定按钮 Tooltips
        if (this.saveButton.isMouseOver()) this.drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.tooltip.save").split("\n")), mouseX, mouseY);
        if (this.cancelButton.isMouseOver()) this.drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.tooltip.cancel").split("\n")), mouseX, mouseY);
        if (this.resetButton.isMouseOver()) this.drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.tooltip.reset").split("\n")), mouseX, mouseY);
        if (this.projectInfoButton.isMouseOver()) this.drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.category.project_info")), mouseX, mouseY);
        if (this.itemTranslationButton.isMouseOver()) this.drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.category.item_translation")), mouseX, mouseY);
        if (this.chatTranslationButton.isMouseOver()) this.drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.category.chat_translation")), mouseX, mouseY);
        if (this.developerOptionsButton.isMouseOver()) this.drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.category.developer_options")), mouseX, mouseY);

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

        if (mouseButton == 0 && this.isScrollbarHovered) {
            this.isDraggingScrollbar = true;
            this.scrollbarDragStartY = mouseY;
            this.scrollbarDragStartOffset = this.targetScrollOffset;
            this.scrollVelocity = 0; // "抓住"滚动条时，停止其运动
            this.dragVelocityTracker.clear();
            return;
        }

        // 面板内点击
        int rightPanelX = 115;
        int panelTopMargin = 35;
        int panelBottomMargin = 40;
        if (mouseX >= rightPanelX - 5 && mouseX < this.width - 10 &&
            mouseY >= panelTopMargin && mouseY < this.height - panelBottomMargin && currentPanel != null) {
            
            if (this.isDraggingScrollbar) return;
            
            int localMouseY = mouseY + (int) scrollOffset;
            currentPanel.mouseClicked(mouseX, localMouseY, mouseButton);
            
            if (mouseButton == 0) {
                 for (GuiButton button : currentPanel.buttons) {
                    if (button.mousePressed(this.mc, mouseX, localMouseY)) {
                        this.actionPerformed(button);
                        return;
                    }
                }
                for (ModernTextField textField : currentPanel.textFields) {
                    textField.mouseClicked(mouseX, localMouseY, mouseButton);
                }
            }
        }
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.isDraggingScrollbar) {
            // 追踪拖动速度
            this.dragVelocityTracker.add(new long[]{System.currentTimeMillis(), mouseY});
            if (this.dragVelocityTracker.size() > 5) {
                this.dragVelocityTracker.remove(0);
            }

            int panelTop = 35;
            int panelBottom = this.height - 40;
            int trackHeight = panelBottom - panelTop - (SCROLLBAR_PADDING * 2);

            float visibleRatio = (float)(panelBottom - panelTop) / (float)currentPanel.getContentHeight();
            int thumbHeight = Math.max(20, (int)(trackHeight * visibleRatio));
            thumbHeight = Math.min(trackHeight, thumbHeight);
            
            float scrollableTrackHeight = trackHeight - thumbHeight;

            if (scrollableTrackHeight > 1) {
                float deltaY = mouseY - this.scrollbarDragStartY;
                float scrollPercentDelta = deltaY / scrollableTrackHeight;

                this.targetScrollOffset = this.scrollbarDragStartOffset + scrollPercentDelta * this.maxScrollOffset;
            }
        }

        if (clickedMouseButton == 0 && currentPanel != null) {
            int localMouseY = mouseY + (int)scrollOffset;
            for (GuiButton button : currentPanel.buttons) {
                if (button instanceof ModernSlider) {
                    ModernSlider slider = (ModernSlider)button;
                    if (slider.dragging) {
                        slider.updateValueFromMouse(mouseX);
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0) {
            if (this.isDraggingScrollbar) {
                this.isDraggingScrollbar = false;
                
                // --- 计算并应用甩动速度 ---
                if (!this.dragVelocityTracker.isEmpty()) {
                    long firstTimestamp = this.dragVelocityTracker.get(0)[0];
                    long lastTimestamp = this.dragVelocityTracker.get(this.dragVelocityTracker.size() - 1)[0];
                    long timeDelta = lastTimestamp - firstTimestamp;
                    
                    if (timeDelta > 10 && timeDelta < 200) { // 只在快速甩动时计算
                        long firstY = this.dragVelocityTracker.get(0)[1];
                        long lastY = this.dragVelocityTracker.get(this.dragVelocityTracker.size() - 1)[1];
                        long mouseDeltaY = lastY - firstY;

                        float pixelsPerMs = (float)mouseDeltaY / (float)timeDelta;

                        int panelTop = 35;
                        int panelBottom = this.height - 40;
                        int trackHeight = panelBottom - panelTop - (SCROLLBAR_PADDING * 2);
                        float scrollableTrackHeight = trackHeight - this.targetScrollbarHandleHeight;
                        
                        if (scrollableTrackHeight > 1) {
                            float scrollUnitsPerPixel = maxScrollOffset / scrollableTrackHeight;
                            // 1ms = 1/20tick, so multiply by 20 to get pixels/tick, then scale
                            this.scrollVelocity = pixelsPerMs * scrollUnitsPerPixel * 20.0f * FLING_VELOCITY_SCALE;
                        }
                    }
                }
                this.dragVelocityTracker.clear();
            }

            if (currentPanel != null) {
                int localMouseY = mouseY + (int)this.scrollOffset;
                for (GuiButton button : currentPanel.buttons) {
                    button.mouseReleased(mouseX, localMouseY);
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
        this.projectInfoButton.displayString = Lang.translate("gui.serverlocalizer.category.project_info");
        this.itemTranslationButton.displayString = Lang.translate("gui.serverlocalizer.category.item_translation");
        this.chatTranslationButton.displayString = Lang.translate("gui.serverlocalizer.category.chat_translation");
        this.developerOptionsButton.displayString = Lang.translate("gui.serverlocalizer.category.developer_options");
        
        this.projectInfoButton.packedFGColour = (this.selectedCategory == 0) ? Theme.COLOR_TEXT_WHITE : Theme.COLOR_TEXT_GRAY;
        this.itemTranslationButton.packedFGColour = (this.selectedCategory == 1) ? Theme.COLOR_TEXT_WHITE : Theme.COLOR_TEXT_GRAY;
        this.chatTranslationButton.packedFGColour = (this.selectedCategory == 2) ? Theme.COLOR_TEXT_WHITE : Theme.COLOR_TEXT_GRAY;
        this.developerOptionsButton.packedFGColour = (this.selectedCategory == 3) ? Theme.COLOR_TEXT_WHITE : Theme.COLOR_TEXT_GRAY;

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