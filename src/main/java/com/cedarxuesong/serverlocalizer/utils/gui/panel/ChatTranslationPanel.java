package com.cedarxuesong.serverlocalizer.utils.gui.panel;

import com.cedarxuesong.serverlocalizer.utils.Lang;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernButton;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernSlider;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernSwitch;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernTextField;
import com.cedarxuesong.serverlocalizer.utils.gui.Theme;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * “聊天翻译”面板
 */
public class ChatTranslationPanel extends BasePanel {
    private static final String TAG = "ChatTranslationPanel";
    private static final String API_NAME = "ChatTranslationApi";

    private ModernSwitch chatTranslationToggle;
    private ModernSwitch streamToggle;
    private ModernTextField chatBaseUrlField;
    private ModernTextField chatApiKeyField;
    private ModernTextField chatModelField;
    private ModernSlider chatTemperatureSlider;
    private ModernTextField chatSystemPromptField;

    private int maxLabelWidth = 0;

    public ChatTranslationPanel(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initGui(int panelWidth) {
        this.buttons.clear();
        this.textFields.clear();

        this.maxLabelWidth = 0;
        List<String> labels = Arrays.asList(
            Lang.translate("gui.serverlocalizer.chat.enable"),
            Lang.translate("gui.serverlocalizer.chat.enable_stream"),
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

        this.chatTranslationToggle = new ModernSwitch(1, 0, 0, modConfig::isChatTranslationEnabled, (newValue) -> {
            modConfig.setChatTranslationEnabled(newValue);
            updateControlStates();
        });
        this.streamToggle = new ModernSwitch(2, 0, 0, modConfig::isChatStreamEnabled, modConfig::setChatStreamEnabled);

        this.chatBaseUrlField = new ModernTextField(4, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.chatBaseUrlField.setMaxStringLength(32767);
        this.chatApiKeyField = new ModernTextField(5, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.chatApiKeyField.setMaxStringLength(32767);
        this.chatModelField = new ModernTextField(6, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.chatModelField.setMaxStringLength(32767);
        this.chatTemperatureSlider = new ModernSlider(7, 0, 0, fieldWidth, 20, 0.0, 2.0, modConfig.getTemperature(API_NAME), "%.1f");
        this.chatSystemPromptField = new ModernTextField(9, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.chatSystemPromptField.setMaxStringLength(32767);

        this.buttons.add(this.chatTranslationToggle);
        this.buttons.add(this.streamToggle);
        this.buttons.add(this.chatTemperatureSlider);

        this.textFields.add(this.chatBaseUrlField);
        this.textFields.add(this.chatApiKeyField);
        this.textFields.add(this.chatModelField);
        this.textFields.add(this.chatSystemPromptField);

        resetConfig();
    }

    @Override
    public void drawPanel(int mouseX, int localMouseY, float partialTicks, int panelX, int panelY) {
        int contentX = panelX + 15;
        int y = panelY + 15;
        int controlX = contentX + maxLabelWidth + 15;

        // Enable/Disable
        drawString(Lang.translate("gui.serverlocalizer.chat.enable"), contentX, y + 6);
        this.chatTranslationToggle.xPosition = controlX;
        this.chatTranslationToggle.yPosition = y;
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.chat.enable_stream"), contentX, y + 6);
        this.streamToggle.xPosition = controlX;
        this.streamToggle.yPosition = y;
        y += 25;
        
        y += 10;

        // API Settings
        drawString(Lang.translate("gui.serverlocalizer.api.base_url"), contentX, y + 6);
        this.chatBaseUrlField.xPosition = controlX;
        this.chatBaseUrlField.yPosition = y;
        this.chatBaseUrlField.drawTextBox(partialTicks);
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.api.api_key"), contentX, y + 6);
        this.chatApiKeyField.xPosition = controlX;
        this.chatApiKeyField.yPosition = y;
        this.chatApiKeyField.drawTextBox(partialTicks);
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.api.model"), contentX, y + 6);
        this.chatModelField.xPosition = controlX;
        this.chatModelField.yPosition = y;
        this.chatModelField.drawTextBox(partialTicks);
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.api.temperature"), contentX, y + 6);
        this.chatTemperatureSlider.xPosition = controlX;
        this.chatTemperatureSlider.yPosition = y;
        y += 25;

        drawString(Lang.translate("gui.serverlocalizer.api.system_prompt"), contentX, y + 6);
        this.chatSystemPromptField.xPosition = controlX;
        this.chatSystemPromptField.yPosition = y;
        this.chatSystemPromptField.drawTextBox(partialTicks);
    }

    @Override
    public void addTooltips(int mouseX, int mouseY, int localMouseY) {
        if (isMouseOver(this.chatTranslationToggle, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.chat.tooltip.enable"), mouseX, mouseY);
        if (isMouseOver(this.streamToggle, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.chat.tooltip.enable_stream"), mouseX, mouseY);
        if (isMouseOver(this.chatBaseUrlField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.base_url"), mouseX, mouseY);
        if (isMouseOver(this.chatApiKeyField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.api_key"), mouseX, mouseY);
        if (isMouseOver(this.chatModelField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.model"), mouseX, mouseY);
        if (isMouseOver(this.chatTemperatureSlider, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.temperature"), mouseX, mouseY);
        if (isMouseOver(this.chatSystemPromptField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.system_prompt"), mouseX, mouseY);
    }

    @Override
    public int getContentHeight() {
        return 280;
    }

    @Override
    public void saveConfig() {
        modConfig.setBaseUrl(API_NAME, this.chatBaseUrlField.getText());
        modConfig.setApiKey(API_NAME, this.chatApiKeyField.getText());
        modConfig.setModel(API_NAME, this.chatModelField.getText());
        modConfig.setTemperature(API_NAME, this.chatTemperatureSlider.getValue());
        modConfig.setSystemPrompt(API_NAME, this.chatSystemPromptField.getText());
    }

    @Override
    public void resetConfig() {
        this.chatBaseUrlField.setText(modConfig.getBaseUrl(API_NAME));
        this.chatApiKeyField.setText(modConfig.getApiKey(API_NAME));
        this.chatModelField.setText(modConfig.getModel(API_NAME));
        this.chatTemperatureSlider.setValue(modConfig.getTemperature(API_NAME));
        this.chatSystemPromptField.setText(modConfig.getSystemPrompt(API_NAME));
        updateControlStates();
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        // The switches now handle their own logic via setters.
    }
    
    private void updateControlStates() {
        boolean chatEnabled = this.chatTranslationToggle.isOn();
        this.streamToggle.enabled = chatEnabled;
        this.chatBaseUrlField.setEnabled(chatEnabled);
        this.chatApiKeyField.setEnabled(chatEnabled);
        this.chatModelField.setEnabled(chatEnabled);
        this.chatTemperatureSlider.enabled = chatEnabled;
        this.chatSystemPromptField.setEnabled(chatEnabled);
    }
} 