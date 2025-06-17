package com.cedarxuesong.serverlocalizer.utils.gui.panel;

import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.gui.ConfigGui;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernButton;
import net.minecraft.client.gui.GuiButton;

import java.util.Arrays;

public class DeveloperOptionsPanel extends BasePanel {

    private ModernButton debugWindowToggle;
    private final ModConfig modConfig = ModConfig.getInstance();

    public DeveloperOptionsPanel(ConfigGui parent) {
        super(parent);
    }

    @Override
    public void initGui(int panelWidth) {
        this.buttons.clear();
        this.textFields.clear();

        debugWindowToggle = new ModernButton(400, 0, 0, 90, 20, "");
        this.buttons.add(debugWindowToggle);

        updateButtonStates();
    }

    @Override
    public void drawPanel(int mouseX, int mouseY, float partialTicks, int panelX, int panelY) {
        int padding = 15;
        int contentX = panelX + padding;
        int y = panelY + padding;

        this.debugWindowToggle.xPosition = contentX;
        this.debugWindowToggle.yPosition = y;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 400) {
            modConfig.setDebugWindowEnabled(!modConfig.isDebugWindowEnabled());
            updateButtonStates();
        }
    }

    @Override
    public void saveConfig() {
        // 设置通过ModConfig实例直接保存，此处无需特定操作
    }

    @Override
    public void resetConfig() {
        // 如有需要，可在此实现重置为默认值的功能
        updateButtonStates();
    }

    @Override
    public int getContentHeight() {
        return 50; // 按需调整
    }

    @Override
    public void addTooltips(int mouseX, int guiMouseY, int localMouseY) {
        if (debugWindowToggle.isMouseOver()) {
            if (parent instanceof ConfigGui) {
                ((ConfigGui) parent).drawPublicHoveringText(Arrays.asList("§e调试窗口", "开启后将显示一个实时日志窗口，", "用于开发和问题排查。", "§c需要重启游戏才能生效。"), mouseX, guiMouseY);
            }
        }
    }

    private void updateButtonStates() {
        boolean enabled = modConfig.isDebugWindowEnabled();
        debugWindowToggle.displayString = "调试窗口: " + (enabled ? "§a开启" : "§c关闭");
    }
}