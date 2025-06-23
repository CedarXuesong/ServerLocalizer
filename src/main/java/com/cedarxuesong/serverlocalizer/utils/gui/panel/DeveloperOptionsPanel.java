package com.cedarxuesong.serverlocalizer.utils.gui.panel;

import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.gui.ConfigGui;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernSwitch;
import com.cedarxuesong.serverlocalizer.utils.Lang;
import com.cedarxuesong.serverlocalizer.utils.gui.Theme;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.Arrays;

public class DeveloperOptionsPanel extends BasePanel {

    private ModernSwitch debugWindowToggle;
    private final ModConfig modConfig = ModConfig.getInstance();

    public DeveloperOptionsPanel(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initGui(int panelWidth) {
        this.buttons.clear();
        this.textFields.clear();

        this.debugWindowToggle = new ModernSwitch(10, 0, 0, modConfig::isDebugWindowEnabled, modConfig::setDebugWindowEnabled);
        this.buttons.add(this.debugWindowToggle);
    }

    @Override
    public void drawPanel(int mouseX, int localMouseY, float partialTicks, int panelX, int panelY) {
        int contentX = panelX + 15;
        int y = panelY + 15;
        
        // Debug Window Toggle
        int labelWidth = this.fontRendererObj.getStringWidth(Lang.translate("gui.serverlocalizer.developer.debug_window"));
        int controlX = contentX + labelWidth + 15;

        drawString(Lang.translate("gui.serverlocalizer.developer.debug_window"), contentX, y + 6);
        this.debugWindowToggle.xPosition = controlX;
        this.debugWindowToggle.yPosition = y;
    }

    @Override
    public void addTooltips(int mouseX, int mouseY, int localMouseY) {
        if (isMouseOver(this.debugWindowToggle, mouseX, localMouseY)) {
            if (parent instanceof ConfigGui) {
                ((ConfigGui) parent).drawPublicHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.developer.debug_window.tooltip"), mouseX, mouseY);
            }
        }
    }

    @Override
    public int getContentHeight() {
        return 50;
    }

    @Override
    public void saveConfig() {
        // The switch now directly modifies the config state, so no action is needed here.
    }

    @Override
    public void resetConfig() {
        // The switch's state is directly bound to the config, so no action is needed here.
    }

    @Override
    public void actionPerformed(GuiButton button) {
        // The switch handles its own state change, so no action is needed here.
    }
}