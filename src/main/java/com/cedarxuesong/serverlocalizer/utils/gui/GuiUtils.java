package com.cedarxuesong.serverlocalizer.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * GUI 绘图工具类
 */
public class GuiUtils {

    private static final ResourceLocation SHADOW_TEXTURE = new ResourceLocation("serverlocalizer", "textures/gui/shadow.png");

    /**
     * Calculates a point on a cubic Bézier curve.
     * @param t The position on the curve, from 0 to 1.
     * @param p0 Start point.
     * @param p1 First control point.
     * @param p2 Second control point.
     * @param p3 End point.
     * @return The calculated point on the curve.
     */
    private static float[] getCubicBezierPoint(float t, float[] p0, float[] p1, float[] p2, float[] p3) {
        float u = 1.0f - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        float[] p = new float[2];
        p[0] = uuu * p0[0] + 3.0f * uu * t * p1[0] + 3.0f * u * tt * p2[0] + ttt * p3[0];
        p[1] = uuu * p0[1] + 3.0f * uu * t * p1[1] + 3.0f * u * tt * p2[1] + ttt * p3[1];
        return p;
    }


    /**
     * 绘制一个带圆角的矩形。
     *
     * @param x      矩形左上角的 x 坐标
     * @param y      矩形左上角的 y 坐标
     * @param width  矩形的宽度
     * @param height 矩形的高度
     * @param radius 圆角的半径
     * @param color  矩形的颜色 (ARGB 格式)
     */
    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.color(r, g, b, a);

        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glBegin(GL11.GL_POLYGON);

        final float c = 0.552284749831f; // Magic number for cubic Bézier circle approximation
        int segments = 15;

        // Top-left corner (from top to left)
        float[] tl_p0 = {x + radius, y};
        float[] tl_p1 = {x + radius - c * radius, y};
        float[] tl_p2 = {x, y + radius - c * radius};
        float[] tl_p3 = {x, y + radius};
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            float[] pt = getCubicBezierPoint(t, tl_p0, tl_p1, tl_p2, tl_p3);
            GL11.glVertex2f(pt[0], pt[1]);
        }

        // Bottom-left corner (from left to bottom)
        float[] bl_p0 = {x, y + height - radius};
        float[] bl_p1 = {x, y + height - radius + c * radius};
        float[] bl_p2 = {x + radius - c * radius, y + height};
        float[] bl_p3 = {x + radius, y + height};
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            float[] pt = getCubicBezierPoint(t, bl_p0, bl_p1, bl_p2, bl_p3);
            GL11.glVertex2f(pt[0], pt[1]);
        }

        // Bottom-right corner (from bottom to right)
        float[] br_p0 = {x + width - radius, y + height};
        float[] br_p1 = {x + width - radius + c * radius, y + height};
        float[] br_p2 = {x + width, y + height - radius + c * radius};
        float[] br_p3 = {x + width, y + height - radius};
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            float[] pt = getCubicBezierPoint(t, br_p0, br_p1, br_p2, br_p3);
            GL11.glVertex2f(pt[0], pt[1]);
        }

        // Top-right corner (from right to top)
        float[] tr_p0 = {x + width, y + radius};
        float[] tr_p1 = {x + width, y + radius - c * radius};
        float[] tr_p2 = {x + width - radius + c * radius, y};
        float[] tr_p3 = {x + width - radius, y};
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            float[] pt = getCubicBezierPoint(t, tr_p0, tr_p1, tr_p2, tr_p3);
            GL11.glVertex2f(pt[0], pt[1]);
        }

        GL11.glEnd();
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    /**
     * Draws a horizontal line with the specified start and end X coordinates, Y position, and color.
     *
     * @param startX The starting X coordinate of the line.
     * @param endX   The ending X coordinate of the line.
     * @param y      The Y position of the line.
     * @param color  The color of the line in ARGB format.
     */
    public static void drawHorizontalLine(int startX, int endX, int y, int color) {
        if (endX < startX) {
            int i = startX;
            startX = endX;
            endX = i;
        }
        net.minecraft.client.gui.Gui.drawRect(startX, y, endX, y + 1, color);
    }

    /**
     * Draws a textured rectangle with custom dimensions. This is a custom implementation
     * that uses double-precision floating-point numbers for coordinates for higher precision.
     *
     * @param x x-coordinate to draw at
     * @param y y-coordinate to draw at
     * @param u u-coordinate on the texture
     * @param v v-coordinate on the texture
     * @param width width of the rectangle
     * @param height height of the rectangle
     * @param textureWidth total width of the texture
     * @param textureHeight total height of the texture
     */
    public static void drawModalRectWithCustomSizedTexture(double x, double y, float u, float v, double width, double height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex((double)(u * f), (double)(v * f1)).endVertex();
        tessellator.draw();
    }

    /**
     * Draws a 9-sliced shadow texture around a rectangle.
     * @param x x-coordinate of the rect
     * @param y y-coordinate of the rect
     * @param width width of the rect
     * @param height height of the rect
     * @param shadowWidth the width of the shadow border
     */
    public static void drawShadow(float x, float y, float width, float height, float shadowWidth) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(SHADOW_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        // Reset color to neutral (white, opaque) to ensure the texture's own colors and alpha are used without modulation.
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        // Draw the shadow twice to make it darker and more prominent
        drawShadowLayer(x, y, width, height, shadowWidth);
        drawShadowLayer(x, y, width, height, shadowWidth);

        GlStateManager.disableBlend();
    }

    private static void drawShadowLayer(float x, float y, float width, float height, float shadowWidth) {
        float texSize = 256.0f; // Size of the shadow texture
        float corner = shadowWidth;

        // Draw corners
        drawModalRectWithCustomSizedTexture(x - shadowWidth, y - shadowWidth, 0, 0, corner, corner, texSize, texSize); // Top-left
        drawModalRectWithCustomSizedTexture(x + width, y - shadowWidth, texSize - corner, 0, corner, corner, texSize, texSize); // Top-right
        drawModalRectWithCustomSizedTexture(x - shadowWidth, y + height, 0, texSize - corner, corner, corner, texSize, texSize); // Bottom-left
        drawModalRectWithCustomSizedTexture(x + width, y + height, texSize - corner, texSize - corner, corner, corner, texSize, texSize); // Bottom-right

        // Draw edges
        drawModalRectWithCustomSizedTexture(x, y - shadowWidth, corner, 0, width, corner, texSize, texSize); // Top
        drawModalRectWithCustomSizedTexture(x, y + height, corner, texSize - corner, width, corner, texSize, texSize); // Bottom
        drawModalRectWithCustomSizedTexture(x - shadowWidth, y, 0, corner, corner, height, texSize, texSize); // Left
        drawModalRectWithCustomSizedTexture(x + width, y, texSize - corner, corner, corner, height, texSize, texSize); // Right
    }
} 