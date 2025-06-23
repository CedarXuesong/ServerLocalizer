package com.cedarxuesong.serverlocalizer.utils.gui.panel;

import com.cedarxuesong.serverlocalizer.Main;
import com.cedarxuesong.serverlocalizer.utils.gui.ConfigGui;
import com.cedarxuesong.serverlocalizer.utils.gui.GuiUtils;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import com.cedarxuesong.serverlocalizer.utils.Lang;
import com.cedarxuesong.serverlocalizer.utils.gui.Theme;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * “项目简介”面板
 */
public class ProjectInfoPanel extends BasePanel {
    private static final String TAG = "ProjectInfoPanel";
    private int lastContentHeight = 0;

    private final List<Rectangle> linkBounds = new ArrayList<>();
    private final List<String> linkUrls = new ArrayList<>();

    public ProjectInfoPanel(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initGui(int panelWidth) {
        // No controls to initialize
    }

    @Override
    public void drawPanel(int mouseX, int localMouseY, float partialTicks, int panelX, int panelY) {
        linkBounds.clear();
        linkUrls.clear();

        int padding = 15;
        int contentX = panelX + padding;
        int y = panelY + padding;
        int lineHeight = fontRendererObj.FONT_HEIGHT + 3;
        int sectionSpacing = 15;
        int contentWidth = this.mc.currentScreen.width - (panelX + padding * 2) - 100;

        // --- 标题和版本 ---
        String title = Lang.translate("gui.serverlocalizer.project.title");
        this.fontRendererObj.drawString(title, contentX, y, ConfigGui.COLOR_TEXT_HIGHLIGHT);
        String versionString = "v" + Main.VERSION;
        this.fontRendererObj.drawString(versionString, contentX + this.fontRendererObj.getStringWidth(title) + 5, y, ConfigGui.COLOR_TEXT_LABEL);
        y += lineHeight;

        String description = Lang.translate("gui.serverlocalizer.project.description");
        this.fontRendererObj.drawSplitString(description, contentX, y, contentWidth, ConfigGui.COLOR_TEXT_WHITE);
        y += this.fontRendererObj.listFormattedStringToWidth(description, contentWidth).size() * lineHeight;

        y += 5; // Padding before the line
        GuiUtils.drawHorizontalLine(contentX, contentX + contentWidth, y, Theme.COLOR_SEPARATOR);
        y += sectionSpacing;
        
        // --- 核心功能 ---
        y = drawSectionHeader(Lang.translate("gui.serverlocalizer.project.section.features"), contentX, y, lineHeight);
        y = drawBulletPoint(Lang.translate("gui.serverlocalizer.project.feature.item_translation"), contentX, y, lineHeight, contentWidth);
        y = drawBulletPoint(Lang.translate("gui.serverlocalizer.project.feature.chat_translation"), contentX, y, lineHeight, contentWidth);
        y = drawBulletPoint(Lang.translate("gui.serverlocalizer.project.feature.config_gui"), contentX, y, lineHeight, contentWidth);
        y = drawBulletPoint(Lang.translate("gui.serverlocalizer.project.feature.openai_compatible"), contentX, y, lineHeight, contentWidth);
        y += sectionSpacing;

        // --- 联系与支持 ---
        y = drawSectionHeader(Lang.translate("gui.serverlocalizer.project.section.contact"), contentX, y, lineHeight);
        drawLink(Lang.translate("gui.serverlocalizer.project.link.github"), Lang.translate("gui.serverlocalizer.project.link.github_text"), "https://github.com/CedarXuesong/ServerLocalizer", contentX, y, mouseX, localMouseY);
        y += lineHeight;
        drawLink(Lang.translate("gui.serverlocalizer.project.link.issues"), Lang.translate("gui.serverlocalizer.project.link.issues_text"), "https://github.com/CedarXuesong/ServerLocalizer/issues", contentX, y, mouseX, localMouseY);
        y += lineHeight;
        drawLink(Lang.translate("gui.serverlocalizer.project.link.bilibili"), Lang.translate("gui.serverlocalizer.project.link.bilibili_text"), "https://space.bilibili.com/473773611", contentX, y, mouseX, localMouseY);
        y += sectionSpacing;

        // --- 如何贡献 ---
        y = drawSectionHeader(Lang.translate("gui.serverlocalizer.project.section.contribute"), contentX, y, lineHeight);
        String contributeText = Lang.translate("gui.serverlocalizer.project.contribute.text");
        this.fontRendererObj.drawSplitString(contributeText, contentX, y, contentWidth, ConfigGui.COLOR_TEXT_WHITE);
        y += this.fontRendererObj.listFormattedStringToWidth(contributeText, contentWidth).size() * lineHeight;

        int newHeight = y - panelY + padding;
        if (newHeight != this.lastContentHeight) {
            this.lastContentHeight = newHeight;
            if (this.parent instanceof ConfigGui) {
                ((ConfigGui) this.parent).updateScrolling();
            }
        }
    }
    
    private int drawSectionHeader(String text, int x, int y, int lineHeight) {
        this.fontRendererObj.drawString(text, x, y, ConfigGui.COLOR_TEXT_WHITE);
        return y + lineHeight + 2;
    }

    private int drawBulletPoint(String text, int x, int y, int lineHeight, int wrapWidth) {
        this.fontRendererObj.drawString("•", x, y, Theme.COLOR_ACCENT);
        this.fontRendererObj.drawSplitString(text, x + 10, y, wrapWidth - 10, ConfigGui.COLOR_TEXT_WHITE);
        int lines = this.fontRendererObj.listFormattedStringToWidth(text, wrapWidth - 10).size();
        return y + (lineHeight - 3) * lines + 3;
    }

    private void drawLink(String label, String linkText, String url, int x, int y, int mouseX, int localMouseY) {
        this.fontRendererObj.drawString(label, x, y, ConfigGui.COLOR_TEXT_WHITE);
        int linkX = x + this.fontRendererObj.getStringWidth(label);
        int linkWidth = this.fontRendererObj.getStringWidth(linkText);
        
        boolean isHovered = mouseX >= linkX && mouseX < linkX + linkWidth && localMouseY >= y && localMouseY < y + this.fontRendererObj.FONT_HEIGHT;
        
        this.fontRendererObj.drawString(linkText, linkX, y, isHovered ? Theme.COLOR_ACCENT_HOVER : Theme.COLOR_ACCENT);
        if (isHovered) {
            GuiUtils.drawHorizontalLine(linkX, linkX + linkWidth, y + this.fontRendererObj.FONT_HEIGHT, Theme.COLOR_ACCENT_HOVER);
        }

        linkBounds.add(new Rectangle(linkX, y, linkWidth, this.fontRendererObj.FONT_HEIGHT));
        linkUrls.add(url);
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            mylog.error(TAG, "Failed to open URL: " + url, e);
        }
    }

    @Override
    public void addTooltips(int mouseX, int mouseY, int localMouseY) {
        for (int i = 0; i < linkBounds.size(); i++) {
            if (linkBounds.get(i).contains(mouseX, localMouseY)) {
                drawHoveringText(Collections.singletonList(EnumChatFormatting.GRAY + Lang.translate("gui.serverlocalizer.project.tooltip.click_to_open") + linkUrls.get(i)), mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public int getContentHeight() {
        return Math.max(280, this.lastContentHeight);
    }

    @Override
    public void saveConfig() {
        // No settings to save
    }

    @Override
    public void resetConfig() {
        // No settings to reset
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        // No buttons to handle
    }

    @Override
    public void mouseClicked(int mouseX, int localMouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, localMouseY, mouseButton);
        if (mouseButton == 0) {
            for (int i = 0; i < linkBounds.size(); i++) {
                if (linkBounds.get(i).contains(mouseX, localMouseY)) {
                    openUrl(linkUrls.get(i));
                    return;
                }
            }
        }
    }
} 