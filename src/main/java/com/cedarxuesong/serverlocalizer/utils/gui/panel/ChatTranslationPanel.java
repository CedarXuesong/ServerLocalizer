package com.cedarxuesong.serverlocalizer.utils.gui.panel;

import com.cedarxuesong.serverlocalizer.utils.Lang;
import com.cedarxuesong.serverlocalizer.utils.gui.ModernButton;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.Arrays;

/**
 * “聊天翻译”面板
 */
public class ChatTranslationPanel extends BasePanel {
    private static final String TAG = "ChatTranslationPanel";
    private static final String API_NAME = "ChatTranslationApi";

    private ModernButton chatTranslationToggle;
    private ModernButton streamToggle;
    private GuiTextField chatBaseUrlField;
    private GuiTextField chatApiKeyField;
    private GuiTextField chatModelField;
    private GuiTextField chatTemperatureField;
    private GuiTextField chatSystemPromptField;

    private int maxLabelWidth = 0;

    public ChatTranslationPanel(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initGui(int panelWidth) {
        this.buttons.clear();
        this.textFields.clear();

        int padding = 15;
        int contentWidth = panelWidth - padding * 2;
        int fieldWidth;

        // 计算API设置部分的最大标签宽度
        this.maxLabelWidth = 0;
        String[] apiLabels = {
                "gui.serverlocalizer.api.base_url",
                "gui.serverlocalizer.api.api_key",
                "gui.serverlocalizer.api.model",
                "gui.serverlocalizer.api.temperature",
                "gui.serverlocalizer.api.system_prompt"
        };
        for (String labelKey : apiLabels) {
            this.maxLabelWidth = Math.max(this.maxLabelWidth, this.fontRendererObj.getStringWidth(Lang.translate(labelKey)));
        }

        int labelControlSpacing = 8;
        fieldWidth = contentWidth - this.maxLabelWidth - labelControlSpacing;

        this.chatTranslationToggle = new ModernButton(203, 0, 0, 0, 20, ""); // 宽度稍后计算
        this.streamToggle = new ModernButton(204, 0, 0, 0, 20, ""); // 宽度稍后计算

        this.chatBaseUrlField = new GuiTextField(11, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.chatBaseUrlField.setMaxStringLength(32767);
        this.chatApiKeyField = new GuiTextField(5, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.chatApiKeyField.setMaxStringLength(32767);
        this.chatModelField = new GuiTextField(6, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.chatModelField.setMaxStringLength(32767);
        this.chatTemperatureField = new GuiTextField(7, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.chatTemperatureField.setMaxStringLength(32767);
        this.chatSystemPromptField = new GuiTextField(9, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.chatSystemPromptField.setMaxStringLength(32767);

        this.buttons.add(this.chatTranslationToggle);
        this.buttons.add(this.streamToggle);

        this.textFields.add(this.chatBaseUrlField);
        this.textFields.add(this.chatApiKeyField);
        this.textFields.add(this.chatModelField);
        this.textFields.add(this.chatTemperatureField);
        this.textFields.add(this.chatSystemPromptField);

        resetConfig();
    }

    @Override
    public void drawPanel(int mouseX, int localMouseY, float partialTicks, int panelX, int panelY) {
        int padding = 15;
        int contentX = panelX + padding;
        int y = panelY + padding;
        int labelControlSpacing = 8;
        int controlX = contentX + this.maxLabelWidth + labelControlSpacing;

        // 总开关
        this.chatTranslationToggle.xPosition = contentX;
        this.chatTranslationToggle.yPosition = y;
        y += 35;

        // Base URL
        drawString(Lang.translate("gui.serverlocalizer.api.base_url"), contentX, y + 6);
        this.chatBaseUrlField.xPosition = controlX;
        this.chatBaseUrlField.yPosition = y;
        this.chatBaseUrlField.drawTextBox();
        y += 25;

        // API Key
        drawString(Lang.translate("gui.serverlocalizer.api.api_key"), contentX, y + 6);
        this.chatApiKeyField.xPosition = controlX;
        this.chatApiKeyField.yPosition = y;
        this.chatApiKeyField.drawTextBox();
        y += 25;

        // 模型
        drawString(Lang.translate("gui.serverlocalizer.api.model"), contentX, y + 6);
        this.chatModelField.xPosition = controlX;
        this.chatModelField.yPosition = y;
        this.chatModelField.drawTextBox();
        y += 25;

        // 温度
        drawString(Lang.translate("gui.serverlocalizer.api.temperature"), contentX, y + 6);
        this.chatTemperatureField.xPosition = controlX;
        this.chatTemperatureField.yPosition = y;
        this.chatTemperatureField.drawTextBox();
        y += 25;

        // 系统提示词
        drawString(Lang.translate("gui.serverlocalizer.api.system_prompt"), contentX, y + 6);
        this.chatSystemPromptField.xPosition = controlX;
        this.chatSystemPromptField.yPosition = y;
        this.chatSystemPromptField.drawTextBox();
        y += 35;

        // 功能按钮
        this.streamToggle.xPosition = contentX;
        this.streamToggle.yPosition = y;
    }

    @Override
    public void addTooltips(int mouseX, int mouseY, int localMouseY) {
        if (isMouseOver(this.chatTranslationToggle, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.chat.tooltip.enable"), mouseX, mouseY);
        if (isMouseOver(this.streamToggle, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.chat.tooltip.enable_stream"), mouseX, mouseY);
        if (isMouseOver(this.chatBaseUrlField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.base_url"), mouseX, mouseY);
        if (isMouseOver(this.chatApiKeyField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.api_key"), mouseX, mouseY);
        if (isMouseOver(this.chatModelField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.model"), mouseX, mouseY);
        if (isMouseOver(this.chatTemperatureField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.temperature"), mouseX, mouseY);
        if (isMouseOver(this.chatSystemPromptField, mouseX, localMouseY)) drawHoveringText(Lang.getTranslatedLines("gui.serverlocalizer.api.tooltip.system_prompt"), mouseX, mouseY);
    }

    @Override
    public int getContentHeight() {
        return 300;
    }

    @Override
    public void saveConfig() {
        modConfig.setBaseUrl(API_NAME, this.chatBaseUrlField.getText());
        modConfig.setApiKey(API_NAME, this.chatApiKeyField.getText());
        modConfig.setModel(API_NAME, this.chatModelField.getText());
        try {
            modConfig.setTemperature(API_NAME, Double.parseDouble(this.chatTemperatureField.getText()));
        } catch (NumberFormatException e) {
            mylog.error(TAG, "Invalid temperature value for chat translation: " + this.chatTemperatureField.getText());
        }
        modConfig.setSystemPrompt(API_NAME, this.chatSystemPromptField.getText());
    }

    @Override
    public void resetConfig() {
        this.chatBaseUrlField.setText(modConfig.getBaseUrl(API_NAME));
        this.chatApiKeyField.setText(modConfig.getApiKey(API_NAME));
        this.chatModelField.setText(modConfig.getModel(API_NAME));
        this.chatTemperatureField.setText(String.valueOf(modConfig.getTemperature(API_NAME)));
        this.chatSystemPromptField.setText(modConfig.getSystemPrompt(API_NAME));
        updateButtonLabels();
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 203: // 聊天翻译开关
                boolean chatEnabled = !modConfig.isChatTranslationEnabled();
                modConfig.setChatTranslationEnabled(chatEnabled);
                break;
            case 204: // 流式响应开关
                boolean streamEnabled = !modConfig.isChatStreamEnabled();
                modConfig.setChatStreamEnabled(streamEnabled);
                break;
        }
        updateButtonLabels();
    }
    
    private void updateButtonLabels() {
        final int IDLE_ON = 0xFF5865F2;
        final int HOVER_ON = 0xFF4752C4;
        final int IDLE_OFF = 0xFF36393F;
        final int HOVER_OFF = 0xFF4A4D53;
        final int TEXT_COLOR = 0xFFFFFFFF;

        boolean chatEnabled = modConfig.isChatTranslationEnabled();
        this.chatTranslationToggle.displayString = Lang.translate("gui.serverlocalizer.chat.enable") + (chatEnabled ? "§a" + Lang.translate("options.on") : "§c" + Lang.translate("options.off"));
        this.chatTranslationToggle.width = this.fontRendererObj.getStringWidth(this.chatTranslationToggle.displayString) + 20;
        this.chatTranslationToggle.setColors(chatEnabled ? IDLE_ON : IDLE_OFF, chatEnabled ? HOVER_ON : HOVER_OFF);
        this.chatTranslationToggle.packedFGColour = TEXT_COLOR;

        boolean streamEnabled = modConfig.isChatStreamEnabled();
        this.streamToggle.displayString = Lang.translate("gui.serverlocalizer.chat.enable_stream") + (streamEnabled ? "§a" + Lang.translate("options.on") : "§c" + Lang.translate("options.off"));
        this.streamToggle.width = this.fontRendererObj.getStringWidth(this.streamToggle.displayString) + 20;
        this.streamToggle.setColors(streamEnabled ? IDLE_ON : IDLE_OFF, streamEnabled ? HOVER_ON : HOVER_OFF);
        this.streamToggle.packedFGColour = TEXT_COLOR;
        this.streamToggle.enabled = chatEnabled;
    }
} 