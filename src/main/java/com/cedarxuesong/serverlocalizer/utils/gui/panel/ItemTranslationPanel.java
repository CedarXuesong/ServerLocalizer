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

    private int maxLabelWidth = 0;

    public ItemTranslationPanel(GuiScreen parent) {
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

        // 留出标签和控件之间的间距
        int labelControlSpacing = 8;
        fieldWidth = contentWidth - this.maxLabelWidth - labelControlSpacing;

        // 初始化按钮和文本框
        this.itemTranslationToggle = new ModernButton(200, 0, 0, 0, 20, ""); // 宽度稍后计算
        this.itemNameToggle = new ModernButton(201, 0, 0, 0, 20, ""); // 宽度稍后计算
        this.itemLoreToggle = new ModernButton(202, 0, 0, 0, 20, ""); // 宽度稍后计算

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

        this.buttons.add(this.itemTranslationToggle);
        this.buttons.add(this.itemNameToggle);
        this.buttons.add(this.itemLoreToggle);

        this.textFields.add(this.itemBaseUrlField);
        this.textFields.add(this.itemApiKeyField);
        this.textFields.add(this.itemModelField);
        this.textFields.add(this.itemTemperatureField);
        this.textFields.add(this.itemSystemPromptField);

        resetConfig(); // 这会调用 updateButtonLabels
    }

    @Override
    public void drawPanel(int mouseX, int localMouseY, float partialTicks, int panelX, int panelY) {
        int padding = 15;
        int contentX = panelX + padding;
        int y = panelY + padding;
        int labelControlSpacing = 8;
        int controlX = contentX + this.maxLabelWidth + labelControlSpacing;
        int toggleButtonSpacing = 10;

        // 总开关
        this.itemTranslationToggle.xPosition = contentX;
        this.itemTranslationToggle.yPosition = y;
        y += 35;

        // Base URL
        drawString(Lang.translate("gui.serverlocalizer.api.base_url"), contentX, y + 6);
        this.itemBaseUrlField.xPosition = controlX;
        this.itemBaseUrlField.yPosition = y;
        this.itemBaseUrlField.drawTextBox();
        y += 25;

        // API Key
        drawString(Lang.translate("gui.serverlocalizer.api.api_key"), contentX, y + 6);
        this.itemApiKeyField.xPosition = controlX;
        this.itemApiKeyField.yPosition = y;
        this.itemApiKeyField.drawTextBox();
        y += 25;

        // 模型
        drawString(Lang.translate("gui.serverlocalizer.api.model"), contentX, y + 6);
        this.itemModelField.xPosition = controlX;
        this.itemModelField.yPosition = y;
        this.itemModelField.drawTextBox();
        y += 25;

        // 温度
        drawString(Lang.translate("gui.serverlocalizer.api.temperature"), contentX, y + 6);
        this.itemTemperatureField.xPosition = controlX;
        this.itemTemperatureField.yPosition = y;
        this.itemTemperatureField.drawTextBox();
        y += 25;

        // 系统提示词
        drawString(Lang.translate("gui.serverlocalizer.api.system_prompt"), contentX, y + 6);
        this.itemSystemPromptField.xPosition = controlX;
        this.itemSystemPromptField.yPosition = y;
        this.itemSystemPromptField.drawTextBox();
        y += 35;

        // 功能按钮
        this.itemNameToggle.xPosition = contentX;
        this.itemNameToggle.yPosition = y;
        this.itemLoreToggle.xPosition = itemNameToggle.xPosition + itemNameToggle.width + toggleButtonSpacing;
        this.itemLoreToggle.yPosition = y;
        y += 35;

        // 显示累计Token使用量
        long accumulatedTokens = modConfig.getAccumulatedTokens(API_NAME);
        String tokenUsageText = Lang.translate("gui.serverlocalizer.item.token_usage") + " " + accumulatedTokens;
        drawString(tokenUsageText, contentX, y);
    }

    @Override
    public void addTooltips(int mouseX, int mouseY, int localMouseY) {
        if (isMouseOver(this.itemTranslationToggle, mouseX, localMouseY)) drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.item.tooltip.enable").split("\n")), mouseX, mouseY);
        if (isMouseOver(this.itemBaseUrlField, mouseX, localMouseY)) drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.api.tooltip.base_url").split("\n")), mouseX, mouseY);
        if (isMouseOver(this.itemApiKeyField, mouseX, localMouseY)) drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.api.tooltip.api_key").split("\n")), mouseX, mouseY);
        if (isMouseOver(this.itemModelField, mouseX, localMouseY)) drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.api.tooltip.model").split("\n")), mouseX, mouseY);
        if (isMouseOver(this.itemTemperatureField, mouseX, localMouseY)) drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.api.tooltip.temperature").split("\n")), mouseX, mouseY);
        if (isMouseOver(this.itemSystemPromptField, mouseX, localMouseY)) drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.api.tooltip.system_prompt").split("\n")), mouseX, mouseY);
        if (isMouseOver(this.itemNameToggle, mouseX, localMouseY)) drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.item.tooltip.enable_name").split("\n")), mouseX, mouseY);
        if (isMouseOver(this.itemLoreToggle, mouseX, localMouseY)) drawHoveringText(Arrays.asList(Lang.translate("gui.serverlocalizer.item.tooltip.enable_lore").split("\n")), mouseX, mouseY);
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
            mylog.error(TAG, "Invalid temperature value for item translation: " + this.itemTemperatureField.getText());
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
        this.itemTranslationToggle.displayString = Lang.translate("gui.serverlocalizer.item.enable") + (itemEnabled ? "§a" + Lang.translate("options.on") : "§c" + Lang.translate("options.off"));
        this.itemTranslationToggle.width = this.fontRendererObj.getStringWidth(this.itemTranslationToggle.displayString) + 20;
        this.itemTranslationToggle.setColors(itemEnabled ? IDLE_ON : IDLE_OFF, itemEnabled ? HOVER_ON : HOVER_OFF);
        this.itemTranslationToggle.packedFGColour = TEXT_COLOR;

        boolean nameEnabled = modConfig.isItemNameTranslationEnabled();
        this.itemNameToggle.displayString = Lang.translate("gui.serverlocalizer.item.enable_name") + (nameEnabled ? "§a" + Lang.translate("options.on") : "§c" + Lang.translate("options.off"));
        this.itemNameToggle.width = this.fontRendererObj.getStringWidth(this.itemNameToggle.displayString) + 20;
        this.itemNameToggle.setColors(nameEnabled ? IDLE_ON : IDLE_OFF, nameEnabled ? HOVER_ON : HOVER_OFF);
        this.itemNameToggle.packedFGColour = TEXT_COLOR;
        this.itemNameToggle.enabled = itemEnabled;

        boolean loreEnabled = modConfig.isItemLoreTranslationEnabled();
        this.itemLoreToggle.displayString = Lang.translate("gui.serverlocalizer.item.enable_lore") + (loreEnabled ? "§a" + Lang.translate("options.on") : "§c" + Lang.translate("options.off"));
        this.itemLoreToggle.width = this.fontRendererObj.getStringWidth(this.itemLoreToggle.displayString) + 20;
        this.itemLoreToggle.setColors(loreEnabled ? IDLE_ON : IDLE_OFF, loreEnabled ? HOVER_ON : HOVER_OFF);
        this.itemLoreToggle.packedFGColour = TEXT_COLOR;
        this.itemLoreToggle.enabled = itemEnabled;
    }
} 