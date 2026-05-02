package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;

public abstract class DraggableHUD {
    
    // --- AUTO-REGISTRY ---
    private static final List<DraggableHUD> REGISTRY = new ArrayList<>();

    public int x, y;
    public int startX, startY;
    public double relativeX = -1, relativeY = -1; // -1 means uninitialized
    public int width, height;
    public float scale = 1.0f;
    public boolean isHorizontal = false; 

    public final float MIN_SCALE = 0.5f;
    public final float MAX_SCALE = 1.5f;
    
    public String name = "HUD Element";

    public DraggableHUD(String name, int startX, int startY) {
        this.name = name;
        this.startX = startX;
        this.startY = startY;
        this.x = startX;
        this.y = startY;
        
        // Register itself upon creation
        if (!REGISTRY.contains(this)) {
            REGISTRY.add(this);
        }
    }

    /**
     * Updates the absolute pixel coordinates based on current screen size and relative positions.
     */
    public void updateAbsolutePos() {
        if (relativeX == -1 || relativeY == -1) {
            // First time or legacy config: calculate relative from current absolute
            saveRelativePos();
            return;
        }
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft());
        int calcX = (int) (relativeX * sr.getScaledWidth());
        int calcY = (int) (relativeY * sr.getScaledHeight());
        
        // Clamp to screen bounds to prevent off-screen modules
        int w = width > 0 ? (int)(width * scale) : 50;
        int h = height > 0 ? (int)(height * scale) : 50;
        this.x = Math.max(0, Math.min(calcX, sr.getScaledWidth() - w));
        this.y = Math.max(0, Math.min(calcY, sr.getScaledHeight() - h));
    }

    public void resetPosition() {
        this.x = startX;
        this.y = startY;
        this.relativeX = -1;
        this.relativeY = -1;
        this.scale = 1.0f;
        saveRelativePos();
    }

    /**
     * Saves the current absolute pixel coordinates as relative percentages.
     */
    public void saveRelativePos() {
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft());
        this.relativeX = (double) this.x / sr.getScaledWidth();
        this.relativeY = (double) this.y / sr.getScaledHeight();
    }

    public static List<DraggableHUD> getRegistry() {
        return REGISTRY;
    }

    // Auto-detects if the specific module is enabled using reflection
    public boolean isEnabled() {
        try {
            return this.getClass().getField("enabled").getBoolean(this);
        } catch (Exception e) {
            return true; // Default to true if no 'enabled' field exists
        }
    }

    public abstract void draw(boolean isEditing);

    private int lastScreenWidth = -1;
    private int lastScreenHeight = -1;

    public void render(boolean isEditing, int mouseX, int mouseY) {
        if (!HUDController.enabled) return;
        
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft());
        if (sr.getScaledWidth() != lastScreenWidth || sr.getScaledHeight() != lastScreenHeight) {
            if (lastScreenWidth != -1) {
                // Screen was resized, update positions based on percentages
                updateAbsolutePos();
            } else if (relativeX == -1 || relativeY == -1) {
                // First render and NO config loaded: save current defaults as relative
                saveRelativePos();
            } else {
                // First render but config WAS loaded: just calculate absolute
                updateAbsolutePos();
            }
            lastScreenWidth = sr.getScaledWidth();
            lastScreenHeight = sr.getScaledHeight();
        }
        
        boolean hovered = isHovered(mouseX, mouseY);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);

        if (isEditing) {
            GL11.glPushMatrix();
            GL11.glScalef(0.8f, 0.8f, 1.0f);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(name, 0, -11, 0xFFFFFF);
            GL11.glPopMatrix();
        }

        GL11.glScalef(scale, scale, 1);

        if (isEditing) {
            int bgColor = hovered ? 0x45FFFFFF : 0x25FFFFFF; 
            int borderColor = 0x55FFFFFF; 
            drawCleanBox(0, 0, width, height, bgColor, borderColor);
        }

        draw(isEditing); 

        GL11.glPopMatrix();

        if (isEditing && hovered) {
            int corner = getHoveredCorner(mouseX, mouseY);
            
            float boxSize = 2.0f; 
            float actualW = width * scale;
            float actualH = height * scale;

            // Draw perfectly centered corner nodes
            if (corner == 1) drawCornerBox(x, y, boxSize); 
            else if (corner == 2) drawCornerBox(x + actualW, y, boxSize); 
            else if (corner == 3) drawCornerBox(x, y + actualH, boxSize); 
            else if (corner == 4) drawCornerBox(x + actualW, y + actualH, boxSize); 
        }
    }

    private void drawCleanBox(float x, float y, float w, float h, int bgColor, int borderColor) {
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.disableDepth();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();
        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Gui.drawRect((int)x, (int)y, (int)(x + w), (int)(y + h), bgColor);
        
        float alpha = (float)(borderColor >> 24 & 255) / 255.0F;
        float red = (float)(borderColor >> 16 & 255) / 255.0F;
        float green = (float)(borderColor >> 8 & 255) / 255.0F;
        float blue = (float)(borderColor & 255) / 255.0F;
        
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glLineWidth(1.0F); 
        
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
        
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
        net.minecraft.client.renderer.GlStateManager.disableBlend();
        net.minecraft.client.renderer.GlStateManager.enableDepth();
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }

    private void drawCornerBox(float cx, float cy, float halfSize) {
        Gui.drawRect((int)(cx - halfSize), (int)(cy - halfSize), (int)(cx + halfSize), (int)(cy + halfSize), 0xFFFF0000); 
    }

    public boolean isHovered(int mouseX, int mouseY) {
        float actualW = width * scale;
        float actualH = height * scale;
        boolean inBody = mouseX >= x && mouseX <= x + actualW && mouseY >= y && mouseY <= y + actualH;
        return inBody || getHoveredCorner(mouseX, mouseY) != 0;
    }

    public boolean isHoveringCorner(int mouseX, int mouseY, int corner) {
        float actualW = width * scale;
        float actualH = height * scale;
        int hitBox = 5; 

        float cornerX = x;
        float cornerY = y;

        if (corner == 2) cornerX = x + actualW;
        if (corner == 3) cornerY = y + actualH;
        if (corner == 4) { cornerX = x + actualW; cornerY = y + actualH; }

        return mouseX >= cornerX - hitBox && mouseX <= cornerX + hitBox &&
               mouseY >= cornerY - hitBox && mouseY <= cornerY + hitBox;
    }

    public int getHoveredCorner(int mouseX, int mouseY) {
        if (isHoveringCorner(mouseX, mouseY, 1)) return 1;
        if (isHoveringCorner(mouseX, mouseY, 2)) return 2;
        if (isHoveringCorner(mouseX, mouseY, 3)) return 3;
        if (isHoveringCorner(mouseX, mouseY, 4)) return 4;
        return 0;
    }

    public void handleResize(int deltaX, int deltaY, int corner) {
        float sensitivity = 0.005f; 
        float scaleChange = 0;

        if (corner == 1) scaleChange = -(deltaX + deltaY) * sensitivity;
        else if (corner == 2) scaleChange = (deltaX - deltaY) * sensitivity;
        else if (corner == 3) scaleChange = (-deltaX + deltaY) * sensitivity;
        else if (corner == 4) scaleChange = (deltaX + deltaY) * sensitivity;

        this.scale += scaleChange;
        if (this.scale < MIN_SCALE) this.scale = MIN_SCALE;
        if (this.scale > MAX_SCALE) this.scale = MAX_SCALE;
    }

    public String truncate(String text, int maxChars) {
        if (text == null) return "";
        String plain = net.minecraft.util.EnumChatFormatting.getTextWithoutFormattingCodes(text);
        if (plain.length() <= maxChars) return text;
        
        int length = 0;
        StringBuilder sb = new StringBuilder();
        boolean inFormat = false;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00A7') {
                sb.append(c);
                inFormat = true;
                continue;
            }
            if (inFormat) {
                sb.append(c);
                inFormat = false;
                continue;
            }
            if (length < maxChars) {
                sb.append(c);
                length++;
            } else {
                sb.append("..");
                break;
            }
        }
        return sb.toString();
    }
}