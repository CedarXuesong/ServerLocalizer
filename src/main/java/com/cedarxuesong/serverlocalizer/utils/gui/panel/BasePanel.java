package com.cedarxuesong.serverlocalizer.utils.gui.panel;

import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.gui.ConfigGui;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernTextField;
import com.cedarxuesong.serverlocalizer.utils.gui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有配置面板的抽象基类
 */
public abstract class BasePanel {

    protected final GuiScreen parent;
    protected final Minecraft mc;
    protected final FontRenderer fontRendererObj;
    protected final ModConfig modConfig = ModConfig.getInstance();

    public final List<GuiButton> buttons = new ArrayList<>();
    public final List<ModernTextField> textFields = new ArrayList<>();

    public BasePanel(GuiScreen parent) {
        this.parent = parent;
        this.mc = Minecraft.getMinecraft();
        this.fontRendererObj = this.mc.fontRendererObj;
    }

    /**
     * 初始化面板中的GUI控件
     * @param panelWidth 面板的可用宽度
     */
    public abstract void initGui(int panelWidth);

    /**
     * 绘制面板内容
     * @param mouseX 鼠标X坐标（全局）
     * @param localMouseY 鼠标Y坐标（相对于滚动面板）
     * @param partialTicks 部分ticks
     * @param panelX 面板的起始X坐标
     * @param panelY 面板的起始Y坐标（带滚动偏移）
     */
    public abstract void drawPanel(int mouseX, int localMouseY, float partialTicks, int panelX, int panelY);

    /**
     * 添加此面板的悬浮提示
     * @param mouseX 鼠标X坐标（全局）
     * @param mouseY 鼠标Y坐标（全局）
     * @param localMouseY 鼠标Y坐标（相对于滚动面板）
     */
    public abstract void addTooltips(int mouseX, int mouseY, int localMouseY);

    /**
     * 获取此面板的内容总高度，用于计算滚动条
     * @return 内容高度
     */
    public abstract int getContentHeight();

    /**
     * 保存此面板相关的配置
     */
    public abstract void saveConfig();

    /**
     * 重置此面板的配置到GUI控件上
     */
    public abstract void resetConfig();

    /**
     * 处理此面板的按钮点击事件
     * @param button 被点击的按钮
     */
    public abstract void actionPerformed(GuiButton button) throws IOException;

    /**
     * 处理键盘输入事件
     * @param typedChar 键入的字符
     * @param keyCode 键码
     */
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        for (ModernTextField textField : this.textFields) {
            if (textField.isFocused()) {
                textField.textboxKeyTyped(typedChar, keyCode);
            }
        }
    }

    /**
     * 处理鼠标点击事件
     * @param mouseX 鼠标X坐标（相对于滚动面板）
     * @param localMouseY 鼠标Y坐标（相对于滚动面板）
     * @param mouseButton 鼠标按钮
     */
    public void mouseClicked(int mouseX, int localMouseY, int mouseButton) throws IOException {
        for (ModernTextField textField : this.textFields) {
            textField.mouseClicked(mouseX, localMouseY, mouseButton);
        }
    }
    
    // 辅助方法
    protected void drawHoveringText(List<String> textLines, int x, int y) {
        if (parent instanceof ConfigGui) {
            ((ConfigGui) parent).drawPublicHoveringText(textLines, x, y);
        }
    }

    protected boolean isMouseOver(GuiButton button, int mouseX, int localMouseY) {
        return button.visible && mouseX >= button.xPosition && localMouseY >= button.yPosition && mouseX < button.xPosition + button.width && localMouseY < button.yPosition + button.height;
    }

    protected boolean isMouseOver(ModernTextField field, int mouseX, int localMouseY) {
        return field.getVisible() && mouseX >= field.xPosition && localMouseY >= field.yPosition && mouseX < field.xPosition + field.width && localMouseY < field.yPosition + field.height;
    }
    
    protected void drawString(String text, int x, int y) {
        this.fontRendererObj.drawString(text, x, y, Theme.COLOR_TEXT_GRAY);
    }

    protected void drawString(String text, int x, int y, int color) {
        this.fontRendererObj.drawString(text, x, y, color);
    }
} 