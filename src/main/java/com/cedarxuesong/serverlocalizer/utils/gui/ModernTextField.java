package com.cedarxuesong.serverlocalizer.utils.gui;

import com.cedarxuesong.serverlocalizer.utils.gui.Theme;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModernTextField extends Gui {
    private static final double ANIMATION_SPEED = 0.03;

    private final int id;
    private final FontRenderer fontRendererInstance;
    public int xPosition;
    public int yPosition;
    public int width;
    public int height;
    private String text = "";
    private int maxStringLength = 32;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private boolean isEnabled = true;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;
    private boolean visible = true;
    private Predicate<String> validator = Predicates.alwaysTrue();
    private long lastFrameTime;

    // Animation fields
    private float scrollOffset;
    private float targetScrollOffset;
    private float cursorRenderX;
    private float targetCursorRenderX;
    private float cursorBlinkAnimation;

    public ModernTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        this.id = componentId;
        this.fontRendererInstance = fontrendererObj;
        this.xPosition = x;
        this.yPosition = y;
        this.width = par5Width;
        this.height = par6Height;
        this.setEnableBackgroundDrawing(false); // We draw our own background.
        this.lastFrameTime = System.currentTimeMillis();
        this.cursorRenderX = this.xPosition + 4;
        this.targetCursorRenderX = this.xPosition + 4;
    }

    private boolean isCharacterAllowed(char character) {
        // Disallow section sign and control characters, but allow others.
        return character != 167 && !Character.isISOControl(character);
    }

    private String filterAllowedCharacters(String input) {
        StringBuilder stringbuilder = new StringBuilder();
        for (char c0 : input.toCharArray()) {
            if (isCharacterAllowed(c0)) {
                stringbuilder.append(c0);
            }
        }
        return stringbuilder.toString();
    }

    public void updateCursorCounter() {
        this.cursorCounter++;
        this.cursorBlinkAnimation = (float) (0.5 * Math.sin(this.cursorCounter / 4.0) + 0.5);
    }

    public void setText(String newText) {
        if (this.validator.apply(newText)) {
            if (newText.length() > this.maxStringLength) {
                this.text = newText.substring(0, this.maxStringLength);
            } else {
                this.text = newText;
            }

            this.setCursorPositionEnd();
        }
    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(i, j);
    }

    public void setValidator(Predicate<String> theValidator) {
        this.validator = theValidator;
    }

    public void writeText(String textToWrite) {
        String newText = "";
        String filteredText = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
        int selectionStart = Math.min(this.cursorPosition, this.selectionEnd);
        int selectionEnd = Math.max(this.cursorPosition, this.selectionEnd);
        int freeSpace = this.maxStringLength - this.text.length() - (selectionStart - selectionEnd);

        if (this.text.length() > 0) {
            newText += this.text.substring(0, selectionStart);
        }

        int charsToWrite;
        if (freeSpace < filteredText.length()) {
            newText += filteredText.substring(0, freeSpace);
            charsToWrite = freeSpace;
        } else {
            newText += filteredText;
            charsToWrite = filteredText.length();
        }

        if (this.text.length() > 0 && selectionEnd < this.text.length()) {
            newText += this.text.substring(selectionEnd);
        }

        if (this.validator.apply(newText)) {
            this.text = newText;
            this.moveCursorBy(selectionStart - this.selectionEnd + charsToWrite);
        }
    }

    public void deleteWords(int numWords) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(numWords) - this.cursorPosition);
            }
        }
    }

    public void deleteFromCursor(int numChars) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean isDeletingBackward = numChars < 0;
                int start = isDeletingBackward ? this.cursorPosition + numChars : this.cursorPosition;
                int end = isDeletingBackward ? this.cursorPosition : this.cursorPosition + numChars;
                String newText = "";

                if (start >= 0) {
                    newText = this.text.substring(0, start);
                }

                if (end < this.text.length()) {
                    newText += this.text.substring(end);
                }

                if (this.validator.apply(newText)) {
                    this.text = newText;
                    if (isDeletingBackward) {
                        this.moveCursorBy(numChars);
                    }
                }
            }
        }
    }

    public int getId() {
        return this.id;
    }

    public int getNthWordFromCursor(int numWords) {
        return this.getNthWordFromPos(numWords, this.getCursorPosition());
    }

    public int getNthWordFromPos(int num, int position) {
        return this.findNthWord(num, position, true);
    }

    public int findNthWord(int num, int position, boolean skipBlanks) {
        int currentPos = position;
        boolean isMovingForward = num >= 0;
        int numToMove = Math.abs(num);

        for (int i = 0; i < numToMove; ++i) {
            if (isMovingForward) {
                int textLength = this.text.length();
                currentPos = this.text.indexOf(' ', currentPos);

                if (currentPos == -1) {
                    currentPos = textLength;
                } else {
                    while (skipBlanks && currentPos < textLength && this.text.charAt(currentPos) == ' ') {
                        ++currentPos;
                    }
                }
            } else {
                while (skipBlanks && currentPos > 0 && this.text.charAt(currentPos - 1) == ' ') {
                    --currentPos;
                }

                while (currentPos > 0 && this.text.charAt(currentPos - 1) != ' ') {
                    --currentPos;
                }
            }
        }

        return currentPos;
    }

    public void moveCursorBy(int offset) {
        this.setCursorPosition(this.cursorPosition + offset);
    }

    public void setCursorPosition(int pos) {
        this.cursorPosition = pos;
        int textLength = this.text.length();
        this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, textLength);
        this.setSelectionPos(this.cursorPosition);
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }

    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (!this.isFocused) {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.isEnabled) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isEnabled) {
                this.writeText("");
            }

            return true;
        } else {
            switch (keyCode) {
                case 14:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }
                    return true;
                case 199:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPositionZero();
                    }
                    return true;
                case 203:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }
                    return true;
                case 205:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }
                    return true;
                case 207:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    } else {
                        this.setCursorPositionEnd();
                    }
                    return true;
                case 211:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(1);
                    }
                    return true;
                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        if (this.isEnabled) {
                            this.writeText(Character.toString(typedChar));
                        }
                        return true;
                    } else {
                        return false;
                    }
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean isOver = mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;

        if (this.canLoseFocus) {
            this.setFocused(isOver);
        }

        if (this.isFocused && isOver && mouseButton == 0) {
            int clickPos = mouseX - (this.xPosition + 4) + (int)this.scrollOffset;
            String visibleText = this.fontRendererInstance.trimStringToWidth(this.text, this.getWidth());
            this.setCursorPosition(this.fontRendererInstance.trimStringToWidth(this.text, clickPos).length());
        }
    }

    public void drawTextBox(float parentScrollY) {
        if (this.getVisible()) {
            // Background
            GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 5, Theme.COLOR_ELEMENT_BACKGROUND);

            // --- Animation ---
            long currentTime = System.currentTimeMillis();
            long deltaTime = Math.max(1, currentTime - this.lastFrameTime); // Ensure deltaTime is at least 1 to prevent division by zero or stutter
            this.lastFrameTime = currentTime;
            
            float amountToMove = (float) (1.0 - Math.exp(-ANIMATION_SPEED * deltaTime));

            // Animate scroll
            if (Math.abs(targetScrollOffset - scrollOffset) > 0.1f) {
                scrollOffset += (targetScrollOffset - scrollOffset) * amountToMove;
            } else {
                scrollOffset = targetScrollOffset;
            }

            // Animate cursor
            int logicalCursorX = this.fontRendererInstance.getStringWidth(this.text.substring(0, this.cursorPosition));
            this.targetCursorRenderX = logicalCursorX;
            if (Math.abs(targetCursorRenderX - cursorRenderX) > 0.1f) {
                cursorRenderX += (targetCursorRenderX - cursorRenderX) * amountToMove;
            } else {
                cursorRenderX = targetCursorRenderX;
            }

            // --- Text & Cursor Rendering ---
            int textColor = this.isEnabled ? Theme.COLOR_TEXT_WHITE : Theme.COLOR_TEXT_DISABLED;
            int textStartX = this.xPosition + 4;
            int textStartY = this.yPosition + (this.height - 8) / 2;
            
            // The text field's on-screen Y position is its logical Y minus the parent's scroll.
            int onScreenY = this.yPosition - (int)parentScrollY;
            GuiUtils.SCISSOR_STACK.push(textStartX, onScreenY, this.width - 8, this.height);
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(-this.scrollOffset, 0, 0);

            // Draw text
            this.fontRendererInstance.drawStringWithShadow(this.text, (float)textStartX, (float)textStartY, textColor);

            // Selection & Cursor
            if (this.isFocused) {
                int alpha = (int)(this.cursorBlinkAnimation * 255.0F);
                int cursorColor = (alpha << 24) | 0xFFFFFF;
                int cursorDrawX = textStartX + (int)this.cursorRenderX;
                Gui.drawRect(cursorDrawX, textStartY - 1, cursorDrawX + 1, textStartY + 1 + this.fontRendererInstance.FONT_HEIGHT, cursorColor);
            }

            if (this.selectionEnd != this.cursorPosition) {
                int selectionStart = textStartX + this.fontRendererInstance.getStringWidth(this.text.substring(0, this.cursorPosition));
                int selectionEndPos = textStartX + this.fontRendererInstance.getStringWidth(this.text.substring(0, this.selectionEnd));
                this.drawSelectionBox(selectionStart, textStartY - 1, selectionEndPos, textStartY + 1 + this.fontRendererInstance.FONT_HEIGHT);
            }
            
            GlStateManager.popMatrix();
            GuiUtils.SCISSOR_STACK.pop();

            if (!this.isEnabled) {
                GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 5, Theme.COLOR_DISABLED_OVERLAY);
            }
        }
    }

    private void drawSelectionBox(int startX, int startY, int endX, int endY) {
        if (startX < endX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        if (startY < endY) {
            int j = startY;
            startY = endY;
            endY = j;
        }

        if (endX > this.xPosition + this.width) {
            endX = this.xPosition + this.width;
        }

        if (startX > this.xPosition + this.width) {
            startX = this.xPosition + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(5387);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)startX, (double)endY, 0.0D).endVertex();
        worldrenderer.pos((double)endX, (double)endY, 0.0D).endVertex();
        worldrenderer.pos((double)endX, (double)startY, 0.0D).endVertex();
        worldrenderer.pos((double)startX, (double)startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void setMaxStringLength(int length) {
        this.maxStringLength = length;

        if (this.text.length() > length) {
            this.text = this.text.substring(0, length);
        }
    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public int getCursorPosition() {
        return this.cursorPosition;
    }

    public boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    public void setEnableBackgroundDrawing(boolean enable) {
        this.enableBackgroundDrawing = enable;
    }

    public void setTextColor(int color) {
        this.enabledColor = color;
    }

    public void setDisabledTextColour(int color) {
        this.disabledColor = color;
    }

    public void setFocused(boolean focused) {
        if (focused && !this.isFocused) {
            this.cursorCounter = 0;
        }

        this.isFocused = focused;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void setEnabled(boolean p_146184_1_) {
        this.isEnabled = p_146184_1_;
    }
    
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public int getWidth() {
        return this.enableBackgroundDrawing ? this.width - 8 : this.width;
    }

    public void setSelectionPos(int position) {
        int textLength = this.text.length();

        if (position > textLength) {
            position = textLength;
        }

        if (position < 0) {
            position = 0;
        }

        this.selectionEnd = position;
        
        // Update scroll offset to keep cursor in view
        if (this.fontRendererInstance != null) {
            int cursorX = this.fontRendererInstance.getStringWidth(this.text.substring(0, position));
            int viewWidth = getWidth() - 8;
            if (cursorX < this.targetScrollOffset) {
                this.targetScrollOffset = cursorX;
            }
            if (cursorX > this.targetScrollOffset + viewWidth) {
                this.targetScrollOffset = cursorX - viewWidth;
            }
            float maxScroll = this.fontRendererInstance.getStringWidth(this.text) - viewWidth;
            if (maxScroll < 0) maxScroll = 0;
            this.targetScrollOffset = MathHelper.clamp_float(this.targetScrollOffset, 0, maxScroll);
        }
    }

    public void setCanLoseFocus(boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
} 