package com.linexstudios.foxtrot.Render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class RenderUtils {

    // ==========================================
    //          3D WORLD RENDERING (ESP)
    // ==========================================

    public static void setup3D() {
        GlStateManager.pushMatrix();
        
        // ANTI-DESYNC: Force GL11 and GlStateManager to sync
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.disableTexture2D();
        
        GL11.glDisable(GL11.GL_LIGHTING);
        GlStateManager.disableLighting();
        
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GlStateManager.disableDepth();
        
        GL11.glDisable(GL11.GL_CULL_FACE);
        GlStateManager.disableCull();
        
        GlStateManager.depthMask(false);
    }

    public static void end3D() {
        // ANTI-DESYNC: Restore to vanilla defaults safely
        GL11.glEnable(GL11.GL_CULL_FACE);
        GlStateManager.enableCull();
        
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.enableDepth();
        
        // Lighting remains disabled as expected by RenderWorldLastEvent
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableTexture2D();
        
        GlStateManager.depthMask(true);
        
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.disableBlend();
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 
        
        GL11.glLineWidth(1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawFilledBox(AxisAlignedBB bb, float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
        GlStateManager.color(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ); GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        GL11.glEnd();
    }

    public static void drawOutlinedBox(AxisAlignedBB bb, float r, float g, float b, float a, float lineWidth) {
        GL11.glColor4f(r, g, b, a);
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        GL11.glEnd();
    }

    // ===========================
    //         2D RENDERING
    // ===========================

    public static void setup2D() {
        GlStateManager.pushMatrix();
        
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.enableBlend();
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.disableTexture2D();
        
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    public static void end2D() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableTexture2D();
        
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.disableBlend();
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        GlStateManager.popMatrix();
    }

    public static void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
        GlStateManager.color(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
    }

    // ==========================================
    //            HUD / MENU RENDERING 
    // ==========================================

    public static void setupSmoothRender(boolean isGradient) { 
        GlStateManager.pushMatrix(); 
        
        // Synchronized Setup
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.enableBlend(); 
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.disableTexture2D(); 
        
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0); 
        
        GL11.glDisable(GL11.GL_LINE_SMOOTH); 
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH); 
        
        GL11.glDisable(GL11.GL_CULL_FACE); 
        GlStateManager.disableCull();
        
        if(isGradient) GlStateManager.shadeModel(GL11.GL_SMOOTH); 
    }
    
    public static void endSmoothRender() { 
        GlStateManager.shadeModel(GL11.GL_FLAT); 
        
        // Synchronized Teardown
        GL11.glEnable(GL11.GL_CULL_FACE);
        GlStateManager.enableCull(); 
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableTexture2D(); 
        
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.disableBlend(); 
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(1, 1, 1, 1); 
        
        GlStateManager.popMatrix(); 
    }
    
    public static void setColor(int c) { 
        float a = (c >> 24 & 0xFF) / 255f;
        float r = (c >> 16 & 0xFF) / 255f;
        float g = (c >> 8 & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;
        GL11.glColor4f(r, g, b, a);
        GlStateManager.color(r, g, b, a); 
    }
    
    public static void drawGuideLine(float x, float y, float w, float h, float r, float g, float b, float a) { setupSmoothRender(false); GL11.glColor4f(r, g, b, a); GlStateManager.color(r, g, b, a); GL11.glBegin(GL11.GL_QUADS); GL11.glVertex2f(x, y); GL11.glVertex2f(x, y + h); GL11.glVertex2f(x + w, y + h); GL11.glVertex2f(x + w, y); GL11.glEnd(); endSmoothRender(); }
    public static void drawNeonGlow(float x, float y, float w, float h, float r, float sp, int c) { float a = (c >> 24 & 0xFF) / 255f; for(float s=sp; s>0; s-=1f) drawRoundedOutline(x-s, y-s, w+(s*2), h+(s*2), r+s, 1, ((int)((a*(1-(s/sp)))*255)<<24)|(c&0xFFFFFF)); }
    public static void drawGradientRoundedRect(float x, float y, float w, float h, float r, int tC, int bC) { setupSmoothRender(true); float x1=x+w, y1=y+h; GL11.glBegin(GL11.GL_POLYGON); setColor(tC); for(int i=180;i<=270;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=270;i<=360;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); setColor(bC); for(int i=0;i<=90;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); for(int i=90;i<=180;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
    public static void drawRoundedRect(float x, float y, float w, float h, float r, int c) { setupSmoothRender(false); setColor(c); float x1=x+w, y1=y+h; GL11.glBegin(GL11.GL_POLYGON); for(int i=180;i<=270;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=270;i<=360;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=0;i<=90;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); for(int i=90;i<=180;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
    public static void drawRoundedOutline(float x, float y, float w, float h, float r, float lW, int c) { setupSmoothRender(false); setColor(c); GL11.glLineWidth(lW); float x1=x+w, y1=y+h; GL11.glBegin(GL11.GL_LINE_LOOP); for(int i=180;i<=270;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=270;i<=360;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=0;i<=90;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); for(int i=90;i<=180;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
    public static void drawSolidRect(float x, float y, float w, float h, int c) { drawRoundedRect(x, y, w, h, 0, c); }
    public static void drawCircle(float cX, float cY, float r, int c) { setupSmoothRender(false); setColor(c); GL11.glBegin(GL11.GL_POLYGON); for(int i=0;i<=360;i+=5)GL11.glVertex2f((float)(cX+Math.cos(Math.toRadians(i))*r),(float)(cY+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
    public static void drawCircleOutline(float cX, float cY, float r, float lW, int c) { setupSmoothRender(false); setColor(c); GL11.glLineWidth(lW); GL11.glBegin(GL11.GL_LINE_LOOP); for(int i=0;i<=360;i+=5)GL11.glVertex2f((float)(cX+Math.cos(Math.toRadians(i))*r),(float)(cY+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
}