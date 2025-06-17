package com.cedarxuesong.serverlocalizer.utils.gui.panel;

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

    public ChatTranslationPanel(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initGui(int panelWidth) {
        this.buttons.clear();
        this.textFields.clear();

        int padding = 15;
        int contentWidth = panelWidth - padding * 2;
        int labelWidth = 70;
        int fieldWidth = contentWidth - labelWidth;

        this.chatTranslationToggle = new ModernButton(203, 0, 0, 90, 20, "");
        this.streamToggle = new ModernButton(204, 0, 0, 90, 20, "");
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
        int labelWidth = 70;

        // 总开关
        this.chatTranslationToggle.xPosition = contentX;
        this.chatTranslationToggle.yPosition = y;
        y += 35;

        // Base URL
        drawString("Base URL:", contentX, y + 5);
        this.chatBaseUrlField.xPosition = contentX + labelWidth;
        this.chatBaseUrlField.yPosition = y;
        this.chatBaseUrlField.drawTextBox();
        y += 25;

        // API Key
        drawString("API Key:", contentX, y + 5);
        this.chatApiKeyField.xPosition = contentX + labelWidth;
        this.chatApiKeyField.yPosition = y;
        this.chatApiKeyField.drawTextBox();
        y += 25;

        // 模型
        drawString("模型:", contentX, y + 5);
        this.chatModelField.xPosition = contentX + labelWidth;
        this.chatModelField.yPosition = y;
        this.chatModelField.drawTextBox();
        y += 25;

        // 温度
        drawString("温度:", contentX, y + 5);
        this.chatTemperatureField.xPosition = contentX + labelWidth;
        this.chatTemperatureField.yPosition = y;
        this.chatTemperatureField.drawTextBox();
        y += 25;

        // 系统提示词
        drawString("系统提示词:", contentX, y + 5);
        this.chatSystemPromptField.xPosition = contentX + labelWidth;
        this.chatSystemPromptField.yPosition = y;
        this.chatSystemPromptField.drawTextBox();
        y += 35;

        // 功能按钮
        this.streamToggle.xPosition = contentX;
        this.streamToggle.yPosition = y;
    }

    @Override
    public void addTooltips(int mouseX, int mouseY, int localMouseY) {
        if (isMouseOver(this.chatTranslationToggle, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e聊天翻译总开关", "启用或禁用所有聊天内容的翻译功能。"), mouseX, mouseY);
        if (isMouseOver(this.streamToggle, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e流式响应", "启用后，翻译结果将逐字显示，", "无需等待完整响应，实时查看。"), mouseX, mouseY);
        if (isMouseOver(this.chatBaseUrlField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§eBase URL", "你的API服务器地址。", "例如: §7https://api.openai.com/v1"), mouseX, mouseY);
        if (isMouseOver(this.chatApiKeyField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§eAPI Key", "你的API密钥。", "例如: §7sk-xxxxxxxx"), mouseX, mouseY);
        if (isMouseOver(this.chatModelField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e模型", "所使用的AI模型。", "例如: §7gpt-4o, deepseek-v3"), mouseX, mouseY);
        if (isMouseOver(this.chatTemperatureField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e温度 (0.0 - 2.0)", "控制生成文本的随机性。", "§7较低值 (如0.2) -> 更稳定、确定", "§7较高值 (如0.8) -> 更具创造性"), mouseX, mouseY);
        if (isMouseOver(this.chatSystemPromptField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e系统提示词 (不建议修改)", "给AI设定的初始指令或角色，", "用于指导其翻译行为。"), mouseX, mouseY);
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
            mylog.error(TAG, "无效的聊天翻译温度值: " + this.chatTemperatureField.getText());
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
        this.chatTranslationToggle.displayString = "聊天翻译: " + (chatEnabled ? "§a开" : "§c关");
        this.chatTranslationToggle.setColors(chatEnabled ? IDLE_ON : IDLE_OFF, chatEnabled ? HOVER_ON : HOVER_OFF);
        this.chatTranslationToggle.packedFGColour = TEXT_COLOR;

        boolean streamEnabled = modConfig.isChatStreamEnabled();
        this.streamToggle.displayString = "流式响应: " + (streamEnabled ? "§a开" : "§c关");
        this.streamToggle.setColors(streamEnabled ? IDLE_ON : IDLE_OFF, streamEnabled ? HOVER_ON : HOVER_OFF);
        this.streamToggle.packedFGColour = TEXT_COLOR;
        this.streamToggle.enabled = chatEnabled;
    }
} 