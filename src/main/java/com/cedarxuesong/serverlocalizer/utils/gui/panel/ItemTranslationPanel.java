package com.cedarxuesong.serverlocalizer.utils.gui.panel;

import com.cedarxuesong.serverlocalizer.utils.Lang;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernButton;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernSlider;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernSwitch;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernTextField;
import com.cedarxuesong.serverlocalizer.utils.gui.Theme;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * "物品翻译"面板
 */
public class ItemTranslationPanel extends BasePanel {
    private static final String TAG = "ItemTranslationPanel";
    private static final String API_NAME = "ItemTranslationApi";

    private ModernSwitch itemTranslationToggle;
    private ModernTextField itemBaseUrlField;
    private ModernTextField itemApiKeyField;
    private ModernTextField itemModelField;
    private ModernSlider itemTemperatureSlider;
    private ModernTextField itemSystemPromptField;
    private ModernSwitch itemNameToggle;
    private ModernSwitch itemLoreToggle;

    private int maxLabelWidth = 0;

    public ItemTranslationPanel(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initGui(int panelWidth) {
        this.buttons.clear();
        this.textFields.clear();

        this.maxLabelWidth = 0;
        List<String> labels = Arrays.asList(
            Lang.translate("gui.serverlocalizer.item.enable"),
            Lang.translate("gui.serverlocalizer.item.enable_name"),
            Lang.translate("gui.serverlocalizer.item.enable_lore"),
            Lang.translate("gui.serverlocalizer.api.base_url"),
            Lang.translate("gui.serverlocalizer.api.api_key"),
            Lang.translate("gui.serverlocalizer.api.model"),
            Lang.translate("gui.serverlocalizer.api.temperature"),
            Lang.translate("gui.serverlocalizer.api.system_prompt")
        );
        for (String label : labels) {
            this.maxLabelWidth = Math.max(this.maxLabelWidth, this.fontRendererObj.getStringWidth(label));
        }

        int padding = 15;
        int labelControlSpacing = 15;
        int fieldWidth = panelWidth - padding * 2 - maxLabelWidth - labelControlSpacing;
        if (fieldWidth < 100) {
            fieldWidth = 100;
        }

        this.itemTranslationToggle = new ModernSwitch(10, 0, 0, modConfig::isItemTranslationEnabled, (newValue) -> {
            modConfig.setItemTranslationEnabled(newValue);
            updateControlStates();
        });
        this.itemNameToggle = new ModernSwitch(4, 0, 0, modConfig::isItemNameTranslationEnabled, modConfig::setItemNameTranslationEnabled);
        this.itemLoreToggle = new ModernSwitch(5, 0, 0, modConfig::isItemLoreTranslationEnabled, modConfig::setItemLoreTranslationEnabled);

        this.itemBaseUrlField = new ModernTextField(0, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.itemBaseUrlField.setMaxStringLength(32767);
        this.itemApiKeyField = new ModernTextField(1, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.itemApiKeyField.setMaxStringLength(32767);
        this.itemModelField = new ModernTextField(2, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.itemModelField.setMaxStringLength(32767);
        this.itemTemperatureSlider = new ModernSlider(3, 0, 0, fieldWidth, 20, 0.0, 2.0, modConfig.getTemperature(API_NAME), "%.1f");
        this.itemSystemPromptField = new ModernTextField(8, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.itemSystemPromptField.setMaxStringLength(32767);

        this.buttons.add(this.itemTranslationToggle);
        this.buttons.add(this.itemNameToggle);
        this.buttons.add(this.itemLoreToggle);
        this.buttons.add(this.itemTemperatureSlider);

        this.textFields.add(this.itemBaseUrlField);
        this.textFields.add(this.itemApiKeyField);
        this.textFields.add(this.itemModelField);
        this.textFields.add(this.itemSystemPromptField);

        resetConfig();
    }

    @Override
    public void drawPanel(int mouseX, int localMouseY, float partialTicks, int panelX, int panelY) {
        int contentX = panelX + 15;
        int y = panelY + 15;
        int controlX = contentX + maxLabelWidth + 15;

        // Enable/Disable
        drawString(Lang.translate("gui.serverlocalizer.item.enable"), contentX, y + 6);
        this.itemTranslationToggle.xPosition = controlX;
        this.itemTranslationToggle.yPosition = y;
        y += 25;

        // API Settings
        drawString(Lang.translate("gui.serverlocalizer.api.base_url"), contentX, y + 6);
        this.itemBaseUrlField.xPosition = controlX;
        this.itemBaseUrlField.yPosition = y;
        this.itemBaseUrlField.drawTextBox(partialTicks);
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.api.api_key"), contentX, y + 6);
        this.itemApiKeyField.xPosition = controlX;
        this.itemApiKeyField.yPosition = y;
        this.itemApiKeyField.drawTextBox(partialTicks);
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.api.model"), contentX, y + 6);
        this.itemModelField.xPosition = controlX;
        this.itemModelField.yPosition = y;
        this.itemModelField.drawTextBox(partialTicks);
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.api.temperature"), contentX, y + 6);
        this.itemTemperatureSlider.xPosition = controlX;
        this.itemTemperatureSlider.yPosition = y;
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.api.system_prompt"), contentX, y + 6);
        this.itemSystemPromptField.xPosition = controlX;
        this.itemSystemPromptField.yPosition = y;
        this.itemSystemPromptField.drawTextBox(partialTicks);
        y += 25;

        y += 10;

        // Name/Lore Toggles
        drawString(Lang.translate("gui.serverlocalizer.item.enable_name"), contentX, y + 6);
        this.itemNameToggle.xPosition = controlX;
        this.itemNameToggle.yPosition = y;
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.item.enable_lore"), contentX, y + 6);
        this.itemLoreToggle.xPosition = controlX;
        this.itemLoreToggle.yPosition = y;
    }

    @Override
    public void addTooltips(int mouseX, int mouseY, int localMouseY) {
        if (isMouseOver(this.itemTranslationToggle, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.item.tooltip.enable"), mouseX, mouseY);
        if (isMouseOver(this.itemBaseUrlField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.base_url"), mouseX, mouseY);
        if (isMouseOver(this.itemApiKeyField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.api_key"), mouseX, mouseY);
        if (isMouseOver(this.itemModelField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.model"), mouseX, mouseY);
        if (isMouseOver(this.itemTemperatureSlider, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.temperature"), mouseX, mouseY);
        if (isMouseOver(this.itemSystemPromptField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.system_prompt"), mouseX, mouseY);
        if (isMouseOver(this.itemNameToggle, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.item.tooltip.enable_name"), mouseX, mouseY);
        if (isMouseOver(this.itemLoreToggle, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.item.tooltip.enable_lore"), mouseX, mouseY);
    }

    @Override
    public int getContentHeight() {
        return 280;
    }

    @Override
    public void saveConfig() {
        modConfig.setBaseUrl(API_NAME, this.itemBaseUrlField.getText());
        modConfig.setApiKey(API_NAME, this.itemApiKeyField.getText());
        modConfig.setModel(API_NAME, this.itemModelField.getText());
        modConfig.setTemperature(API_NAME, this.itemTemperatureSlider.getValue());
        modConfig.setSystemPrompt(API_NAME, this.itemSystemPromptField.getText());
    }

    @Override
    public void resetConfig() {
        this.itemBaseUrlField.setText(modConfig.getBaseUrl(API_NAME));
        this.itemApiKeyField.setText(modConfig.getApiKey(API_NAME));
        this.itemModelField.setText(modConfig.getModel(API_NAME));
        this.itemTemperatureSlider.setValue(modConfig.getTemperature(API_NAME));
        this.itemSystemPromptField.setText(modConfig.getSystemPrompt(API_NAME));
        updateControlStates();
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        // The switches now handle their own logic via setters.
    }
    
    private void updateControlStates() {
        boolean itemTranslationEnabled = this.itemTranslationToggle.isOn();
        this.itemBaseUrlField.setEnabled(itemTranslationEnabled);
        this.itemApiKeyField.setEnabled(itemTranslationEnabled);
        this.itemModelField.setEnabled(itemTranslationEnabled);
        this.itemTemperatureSlider.enabled = itemTranslationEnabled;
        this.itemSystemPromptField.setEnabled(itemTranslationEnabled);
        this.itemNameToggle.enabled = itemTranslationEnabled;
        this.itemLoreToggle.enabled = itemTranslationEnabled;
    }
} 