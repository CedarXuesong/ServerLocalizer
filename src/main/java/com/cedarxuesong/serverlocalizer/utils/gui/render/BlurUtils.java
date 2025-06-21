package com.cedarxuesong.serverlocalizer.utils.gui.render;

import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Ported from Rise Client.
 * Utility for applying a blur effect to the background.
 * Uses reflection to access private fields in Minecraft's code.
 */
public class BlurUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final ResourceLocation resourceLocation = new ResourceLocation("serverlocalizer", "shader/blur.json");
    private ShaderGroup shaderGroup;
    private Framebuffer framebuffer;
    private int lastFactor;
    private int lastWidth;
    private int lastHeight;

    // --- Reflection ---
    private static Field listShadersField;
    private static Field mainFramebufferField;
    private static Field timerField;

    static {
        try {
            // Use reflection to access private fields, with SRG names for built environments
            // net.minecraft.client.shader.ShaderGroup#field_148031_d -> listShaders
            listShadersField = ShaderGroup.class.getDeclaredField("listShaders");
            listShadersField.setAccessible(true);
            // net.minecraft.client.shader.ShaderGroup#field_148035_a -> mainFramebuffer
            mainFramebufferField = ShaderGroup.class.getDeclaredField("mainFramebuffer");
            mainFramebufferField.setAccessible(true);
            // net.minecraft.client.Minecraft#field_71428_T -> timer
            timerField = Minecraft.class.getDeclaredField("timer");
            timerField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            System.err.println("Failed to initialize BlurUtils via reflection. Blur will not work.");
            e.printStackTrace();
        }
    }
    // --- End Reflection ---


    public void init() {
        try {
            this.shaderGroup = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), this.resourceLocation);
            this.shaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
            if (mainFramebufferField != null) {
                this.framebuffer = (Framebuffer) mainFramebufferField.get(this.shaderGroup);
            }
        }
        catch (JsonSyntaxException | IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setValues(int strength) {
        if (shaderGroup == null || listShadersField == null) return;
        try {
            @SuppressWarnings("unchecked")
            List<Shader> listShaders = (List<Shader>) listShadersField.get(this.shaderGroup);
            // In blur.json, we have two passes, so we set the radius for two shaders
            listShaders.get(0).getShaderManager().getShaderUniform("Radius").set((float)strength);
            listShaders.get(1).getShaderManager().getShaderUniform("Radius").set((float)strength);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onGuiClosed() {
        if (this.shaderGroup != null) {
            this.shaderGroup.deleteShaderGroup();
            this.shaderGroup = null;
        }
    }

    public final void blur(int blurStrength) {
        if (mc.theWorld == null || mc.thePlayer == null || timerField == null) {
            return;
        }

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int scaleFactor = scaledResolution.getScaleFactor();
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        if (this.sizeHasChanged(scaleFactor, width, height) || this.framebuffer == null || this.shaderGroup == null) {
            this.init();
        }

        this.lastFactor = scaleFactor;
        this.lastWidth = width;
        this.lastHeight = height;

        this.setValues(blurStrength);
        
        try {
            Timer timer = (Timer) timerField.get(mc);
            if (timer != null) {
                this.shaderGroup.loadShaderGroup(timer.renderPartialTicks);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.enableAlpha();
    }


    private boolean sizeHasChanged(int scaleFactor, int width, int height) {
        return this.lastFactor != scaleFactor || this.lastWidth != width || this.lastHeight != height;
    }
} 