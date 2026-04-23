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
    public int width, height;
    public float scale = 1.0f;
    public boolean isHorizontal = false; 

    public final float MIN_SCALE = 0.5f;
    public final float MAX_SCALE = 1.5f;
    
    public String name = "HUD Element";

    public DraggableHUD(String name, int startX, int startY) {
        this.name = name;
        this.x = startX;
        this.y = startY;
        // Register itself upon creation
        if (!REGISTRY.contains(this)) {
            REGISTRY.add(this);
        }
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

    public void render(boolean isEditing, int mouseX, int mouseY) {
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
        Gui.drawRect((int)x, (int)y, (int)(x + w), (int)(y + h), bgColor);
        
        float alpha = (float)(borderColor >> 24 & 255) / 255.0F;
        float red = (float)(borderColor >> 16 & 255) / 255.0F;
        float green = (float)(borderColor >> 8 & 255) / 255.0F;
        float blue = (float)(borderColor & 255) / 255.0F;
        
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(red, green, blue, alpha);
        
        GL11.glLineWidth(1.0F); 
        
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
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
}