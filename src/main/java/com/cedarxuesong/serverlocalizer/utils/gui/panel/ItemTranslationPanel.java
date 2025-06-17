package com.cedarxuesong.serverlocalizer.utils.gui.panel;

import com.cedarxuesong.serverlocalizer.utils.gui.ModernButton;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.Arrays;

/**
 * "物品翻译"面板
 */
public class ItemTranslationPanel extends BasePanel {
    private static final String TAG = "ItemTranslationPanel";
    private static final String API_NAME = "ItemTranslationApi";

    private ModernButton itemTranslationToggle;
    private GuiTextField itemBaseUrlField;
    private GuiTextField itemApiKeyField;
    private GuiTextField itemModelField;
    private GuiTextField itemTemperatureField;
    private GuiTextField itemSystemPromptField;
    private ModernButton itemNameToggle;
    private ModernButton itemLoreToggle;

    public ItemTranslationPanel(GuiScreen parent) {
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

        this.itemTranslationToggle = new ModernButton(200, 0, 0, 90, 20, "");
        this.itemBaseUrlField = new GuiTextField(10, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.itemBaseUrlField.setMaxStringLength(32767);
        this.itemApiKeyField = new GuiTextField(1, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.itemApiKeyField.setMaxStringLength(32767);
        this.itemModelField = new GuiTextField(2, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.itemModelField.setMaxStringLength(32767);
        this.itemTemperatureField = new GuiTextField(3, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.itemTemperatureField.setMaxStringLength(32767);
        this.itemSystemPromptField = new GuiTextField(8, this.fontRendererObj, 0, 0, fieldWidth, 20);
        this.itemSystemPromptField.setMaxStringLength(32767);
        this.itemNameToggle = new ModernButton(201, 0, 0, 90, 20, "");
        this.itemLoreToggle = new ModernButton(202, 0, 0, 90, 20, "");

        this.buttons.add(this.itemTranslationToggle);
        this.buttons.add(this.itemNameToggle);
        this.buttons.add(this.itemLoreToggle);

        this.textFields.add(this.itemBaseUrlField);
        this.textFields.add(this.itemApiKeyField);
        this.textFields.add(this.itemModelField);
        this.textFields.add(this.itemTemperatureField);
        this.textFields.add(this.itemSystemPromptField);

        resetConfig();
    }

    @Override
    public void drawPanel(int mouseX, int localMouseY, float partialTicks, int panelX, int panelY) {
        int padding = 15;
        int contentX = panelX + padding;
        int y = panelY + padding;
        int labelWidth = 70;
        int toggleWidth = 90;

        // 总开关
        this.itemTranslationToggle.xPosition = contentX;
        this.itemTranslationToggle.yPosition = y;
        y += 35;

        // Base URL
        drawString("Base URL:", contentX, y + 5);
        this.itemBaseUrlField.xPosition = contentX + labelWidth;
        this.itemBaseUrlField.yPosition = y;
        this.itemBaseUrlField.drawTextBox();
        y += 25;

        // API Key
        drawString("API Key:", contentX, y + 5);
        this.itemApiKeyField.xPosition = contentX + labelWidth;
        this.itemApiKeyField.yPosition = y;
        this.itemApiKeyField.drawTextBox();
        y += 25;

        // 模型
        drawString("模型:", contentX, y + 5);
        this.itemModelField.xPosition = contentX + labelWidth;
        this.itemModelField.yPosition = y;
        this.itemModelField.drawTextBox();
        y += 25;

        // 温度
        drawString("温度:", contentX, y + 5);
        this.itemTemperatureField.xPosition = contentX + labelWidth;
        this.itemTemperatureField.yPosition = y;
        this.itemTemperatureField.drawTextBox();
        y += 25;

        // 系统提示词
        drawString("系统提示词:", contentX, y + 5);
        this.itemSystemPromptField.xPosition = contentX + labelWidth;
        this.itemSystemPromptField.yPosition = y;
        this.itemSystemPromptField.drawTextBox();
        y += 35;

        // 功能按钮
        this.itemNameToggle.xPosition = contentX;
        this.itemNameToggle.yPosition = y;
        this.itemLoreToggle.xPosition = contentX + toggleWidth + 10;
        this.itemLoreToggle.yPosition = y;
        y += 25;

        // 显示累计Token使用量
        long accumulatedTokens = modConfig.getAccumulatedTokens(API_NAME);
        String tokenUsageText = "累计使用Tokens: " + accumulatedTokens;
        drawString(tokenUsageText, contentX, y);
    }

    @Override
    public void addTooltips(int mouseX, int mouseY, int localMouseY) {
        if (isMouseOver(this.itemTranslationToggle, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e物品翻译总开关", "启用或禁用所有物品（名称和描述）的翻译功能。"), mouseX, mouseY);
        if (isMouseOver(this.itemBaseUrlField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§eBase URL", "你的API服务器地址。", "例如: §7https://api.openai.com/v1"), mouseX, mouseY);
        if (isMouseOver(this.itemApiKeyField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§eAPI Key", "你的API密钥。", "例如: §7sk-xxxxxxxx"), mouseX, mouseY);
        if (isMouseOver(this.itemModelField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e模型", "所使用的AI模型。", "例如: §7gpt-4o, deepseek-v3"), mouseX, mouseY);
        if (isMouseOver(this.itemTemperatureField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e温度 (0.0 - 2.0)", "控制生成文本的随机性。", "§7较低值 (如0.2) -> 更稳定、确定", "§7较高值 (如0.8) -> 更具创造性"), mouseX, mouseY);
        if (isMouseOver(this.itemSystemPromptField, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e系统提示词 (不建议修改)", "给AI设定的初始指令或角色，", "用于指导其翻译行为。"), mouseX, mouseY);
        if (isMouseOver(this.itemNameToggle, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e物品名称翻译", "单独控制物品名称的翻译功能。"), mouseX, mouseY);
        if (isMouseOver(this.itemLoreToggle, mouseX, localMouseY)) drawHoveringText(Arrays.asList("§e物品描述翻译", "单独控制物品Lore(描述)的翻译功能。"), mouseX, mouseY);
    }

    @Override
    public int getContentHeight() {
        return 340;
    }

    @Override
    public void saveConfig() {
        modConfig.setBaseUrl(API_NAME, this.itemBaseUrlField.getText());
        modConfig.setApiKey(API_NAME, this.itemApiKeyField.getText());
        modConfig.setModel(API_NAME, this.itemModelField.getText());
        try {
            modConfig.setTemperature(API_NAME, Double.parseDouble(this.itemTemperatureField.getText()));
        } catch (NumberFormatException e) {
            mylog.error(TAG, "无效的物品翻译温度值: " + this.itemTemperatureField.getText());
        }
        modConfig.setSystemPrompt(API_NAME, this.itemSystemPromptField.getText());
    }

    @Override
    public void resetConfig() {
        this.itemBaseUrlField.setText(modConfig.getBaseUrl(API_NAME));
        this.itemApiKeyField.setText(modConfig.getApiKey(API_NAME));
        this.itemModelField.setText(modConfig.getModel(API_NAME));
        this.itemTemperatureField.setText(String.valueOf(modConfig.getTemperature(API_NAME)));
        this.itemSystemPromptField.setText(modConfig.getSystemPrompt(API_NAME));
        updateButtonLabels();
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 200: // 物品翻译开关
                boolean itemEnabled = !modConfig.isItemTranslationEnabled();
                modConfig.setItemTranslationEnabled(itemEnabled);
                break;
            case 201: // 物品名称开关
                boolean nameEnabled = !modConfig.isItemNameTranslationEnabled();
                modConfig.setItemNameTranslationEnabled(nameEnabled);
                break;
            case 202: // 物品描述开关
                boolean loreEnabled = !modConfig.isItemLoreTranslationEnabled();
                modConfig.setItemLoreTranslationEnabled(loreEnabled);
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

        boolean itemEnabled = modConfig.isItemTranslationEnabled();
        this.itemTranslationToggle.displayString = "物品翻译: " + (itemEnabled ? "§a开" : "§c关");
        this.itemTranslationToggle.setColors(itemEnabled ? IDLE_ON : IDLE_OFF, itemEnabled ? HOVER_ON : HOVER_OFF);
        this.itemTranslationToggle.packedFGColour = TEXT_COLOR;

        boolean nameEnabled = modConfig.isItemNameTranslationEnabled();
        this.itemNameToggle.displayString = "物品名: " + (nameEnabled ? "§a开" : "§c关");
        this.itemNameToggle.setColors(nameEnabled ? IDLE_ON : IDLE_OFF, nameEnabled ? HOVER_ON : HOVER_OFF);
        this.itemNameToggle.packedFGColour = TEXT_COLOR;
        this.itemNameToggle.enabled = itemEnabled;

        boolean loreEnabled = modConfig.isItemLoreTranslationEnabled();
        this.itemLoreToggle.displayString = "物品描述: " + (loreEnabled ? "§a开" : "§c关");
        this.itemLoreToggle.setColors(loreEnabled ? IDLE_ON : IDLE_OFF, loreEnabled ? HOVER_ON : HOVER_OFF);
        this.itemLoreToggle.packedFGColour = TEXT_COLOR;
        this.itemLoreToggle.enabled = itemEnabled;
    }
} 