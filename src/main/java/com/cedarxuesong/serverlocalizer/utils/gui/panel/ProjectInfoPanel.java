package com.cedarxuesong.serverlocalizer.utils.gui.panel;

import com.cedarxuesong.serverlocalizer.Main;
import com.cedarxuesong.serverlocalizer.utils.gui.ConfigGui;
import com.cedarxuesong.serverlocalizer.utils.gui.GuiUtils;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
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
    private static final int LINK_COLOR = 0xFF589DF6;
    private static final int LINK_HOVER_COLOR = 0xFF83B5F7;

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
        this.fontRendererObj.drawString("CedarServerLocalizer", contentX, y, ConfigGui.COLOR_TEXT_HIGHLIGHT);
        String versionString = "v" + Main.VERSION;
        this.fontRendererObj.drawString(versionString, contentX + this.fontRendererObj.getStringWidth("CedarServerLocalizer") + 5, y, ConfigGui.COLOR_TEXT_LABEL);
        y += lineHeight;
        this.fontRendererObj.drawSplitString("一个强大的 Minecraft 服务器内容本地化翻译模组，让您轻松畅玩国际服务器！", contentX, y, contentWidth, ConfigGui.COLOR_TEXT_WHITE);
        y += lineHeight * 3;
        GuiUtils.drawHorizontalLine(contentX, contentX + contentWidth, y, 0x50FFFFFF);
        y += sectionSpacing;
        
        // --- 核心功能 ---
        y = drawSectionHeader("核心功能", contentX, y, lineHeight);
        y = drawBulletPoint("实时翻译物品名称和描述", contentX, y, lineHeight, contentWidth);
        y = drawBulletPoint("智能翻译聊天内容，支持流式响应", contentX, y, lineHeight, contentWidth);
        y = drawBulletPoint("可视化配置界面，支持热重载", contentX, y, lineHeight, contentWidth);
        y = drawBulletPoint("兼容所有 OpenAI 接口标准的 API", contentX, y, lineHeight, contentWidth);
        y += sectionSpacing;

        // --- 联系与支持 ---
        y = drawSectionHeader("联系与支持", contentX, y, lineHeight);
        drawLink("GitHub 仓库: ", "CedarXuesong/ServerLocalizer", "https://github.com/CedarXuesong/ServerLocalizer", contentX, y, mouseX, localMouseY);
        y += lineHeight;
        drawLink("问题与反馈: ", "提交 Issue", "https://github.com/CedarXuesong/ServerLocalizer/issues", contentX, y, mouseX, localMouseY);
        y += lineHeight;
        drawLink("Bilibili: ", "雪松CedarXuesong", "https://space.bilibili.com/473773611", contentX, y, mouseX, localMouseY);
        y += sectionSpacing;

        // --- 如何贡献 ---
        y = drawSectionHeader("如何贡献", contentX, y, lineHeight);
        this.fontRendererObj.drawSplitString("我们欢迎各种形式的贡献，包括提交Bug、改进翻译或添加新功能。请通过 Fork 仓库并发起 Pull Request 来提交您的更改。", contentX, y, contentWidth, ConfigGui.COLOR_TEXT_WHITE);
    }
    
    private int drawSectionHeader(String text, int x, int y, int lineHeight) {
        this.fontRendererObj.drawString(EnumChatFormatting.YELLOW + text, x, y, ConfigGui.COLOR_TEXT_HIGHLIGHT);
        return y + lineHeight + 2;
    }

    private int drawBulletPoint(String text, int x, int y, int lineHeight, int wrapWidth) {
        this.fontRendererObj.drawString("•", x, y, ConfigGui.COLOR_TEXT_HIGHLIGHT);
        this.fontRendererObj.drawSplitString(text, x + 10, y, wrapWidth - 10, ConfigGui.COLOR_TEXT_WHITE);
        int lines = this.fontRendererObj.listFormattedStringToWidth(text, wrapWidth - 10).size();
        return y + lineHeight * lines;
    }

    private void drawLink(String label, String linkText, String url, int x, int y, int mouseX, int localMouseY) {
        this.fontRendererObj.drawString(label, x, y, ConfigGui.COLOR_TEXT_WHITE);
        int linkX = x + this.fontRendererObj.getStringWidth(label);
        int linkWidth = this.fontRendererObj.getStringWidth(linkText);
        
        boolean isHovered = mouseX >= linkX && mouseX < linkX + linkWidth && localMouseY >= y && localMouseY < y + this.fontRendererObj.FONT_HEIGHT;
        
        this.fontRendererObj.drawString(linkText, linkX, y, isHovered ? LINK_HOVER_COLOR : LINK_COLOR);
        if (isHovered) {
            GuiUtils.drawHorizontalLine(linkX, linkX + linkWidth, y + this.fontRendererObj.FONT_HEIGHT, LINK_HOVER_COLOR);
        }

        linkBounds.add(new Rectangle(linkX, y, linkWidth, this.fontRendererObj.FONT_HEIGHT));
        linkUrls.add(url);
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            mylog.error(TAG, "无法打开链接: " + url, e);
        }
    }

    @Override
    public void addTooltips(int mouseX, int mouseY, int localMouseY) {
        for (int i = 0; i < linkBounds.size(); i++) {
            if (linkBounds.get(i).contains(mouseX, localMouseY)) {
                drawHoveringText(Collections.singletonList(EnumChatFormatting.GRAY + "点击打开: " + linkUrls.get(i)), mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public int getContentHeight() {
        return 280;
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