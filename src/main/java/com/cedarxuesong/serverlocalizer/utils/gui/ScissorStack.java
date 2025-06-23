package com.cedarxuesong.serverlocalizer.utils.gui;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import java.util.Stack;
import net.minecraft.client.gui.ScaledResolution;

public class ScissorStack {
    private final Stack<ScissorBox> scissorStack = new Stack<>();
    
    public void push(int x, int y, int width, int height) {
        ScissorBox newScissor = new ScissorBox(x, y, width, height);
        
        if (!scissorStack.isEmpty()) {
            ScissorBox parentScissor = scissorStack.peek();
            newScissor = parentScissor.intersection(newScissor);
        }
        
        scissorStack.push(newScissor);
        applyScissor(newScissor);
    }

    public void pop() {
        if (!scissorStack.isEmpty()) {
            scissorStack.pop();
        }

        if (scissorStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            applyScissor(scissorStack.peek());
        }
    }

    private void applyScissor(ScissorBox box) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int scaleFactor = sr.getScaleFactor();

        GL11.glScissor(box.x * scaleFactor, mc.displayHeight - (box.y + box.height) * scaleFactor, box.width * scaleFactor, box.height * scaleFactor);
    }
} 