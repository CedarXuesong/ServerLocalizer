package com.cedarxuesong.serverlocalizer.utils.gui;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import net.minecraft.util.MathHelper;
import net.minecraft.client.Minecraft;

import static net.minecraft.client.gui.Gui.drawRect;

/**
 * GUI 绘图工具类
 */
public class GuiUtils {

    public static final ScissorStack SCISSOR_STACK = new ScissorStack();

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
        drawRect(startX, y, endX + 1, y + 1, color);
    }
    
    /**
     * Interpolates between two colors.
     * @param fraction The fraction to interpolate by. 0.0 means color1, 1.0 means color2.
     */
    public static int interpolateColor(int color1, int color2, float fraction) {
        fraction = MathHelper.clamp_float(fraction, 0.0f, 1.0f);
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int)(a1 + (a2 - a1) * fraction);
        int r = (int)(r1 + (r2 - r1) * fraction);
        int g = (int)(g1 + (g2 - g1) * fraction);
        int b = (int)(b1 + (b2 - b1) * fraction);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void glScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;
        if (k == 0) {
            k = 1000;
        }
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor);
    }
} 