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
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
    }

    public static void end3D() {
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 
        GL11.glLineWidth(1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawFilledBox(AxisAlignedBB bb, float r, float g, float b, float a) {
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

    // ==========================================
    //        2D RENDERING [LowLifeMystics]
    // ==========================================

    public static void setup2D() {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    public static void end2D() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {
        GlStateManager.color(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
    }

    // ==========================================
    //           HUD / MENU RENDERING 
    // ==========================================

    public static void setupSmoothRender(boolean isGradient) { GlStateManager.pushMatrix(); GlStateManager.enableBlend(); GlStateManager.disableTexture2D(); GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0); GL11.glDisable(GL11.GL_LINE_SMOOTH); GL11.glDisable(GL11.GL_POLYGON_SMOOTH); GL11.glDisable(GL11.GL_CULL_FACE); if(isGradient) GlStateManager.shadeModel(GL11.GL_SMOOTH); }
    public static void endSmoothRender() { GlStateManager.shadeModel(GL11.GL_FLAT); GL11.glEnable(GL11.GL_CULL_FACE); GlStateManager.enableTexture2D(); GlStateManager.disableBlend(); GlStateManager.color(1, 1, 1, 1); GlStateManager.popMatrix(); }
    public static void setColor(int c) { GlStateManager.color((c >> 16 & 0xFF) / 255f, (c >> 8 & 0xFF) / 255f, (c & 0xFF) / 255f, (c >> 24 & 0xFF) / 255f); }
    
    public static void drawGuideLine(float x, float y, float w, float h, float r, float g, float b, float a) { setupSmoothRender(false); GlStateManager.color(r, g, b, a); GL11.glBegin(GL11.GL_QUADS); GL11.glVertex2f(x, y); GL11.glVertex2f(x, y + h); GL11.glVertex2f(x + w, y + h); GL11.glVertex2f(x + w, y); GL11.glEnd(); endSmoothRender(); }
    public static void drawNeonGlow(float x, float y, float w, float h, float r, float sp, int c) { float a = (c >> 24 & 0xFF) / 255f; for(float s=sp; s>0; s-=1f) drawRoundedOutline(x-s, y-s, w+(s*2), h+(s*2), r+s, 1, ((int)((a*(1-(s/sp)))*255)<<24)|(c&0xFFFFFF)); }
    public static void drawGradientRoundedRect(float x, float y, float w, float h, float r, int tC, int bC) { setupSmoothRender(true); float x1=x+w, y1=y+h; GL11.glBegin(GL11.GL_POLYGON); setColor(tC); for(int i=180;i<=270;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=270;i<=360;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); setColor(bC); for(int i=0;i<=90;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); for(int i=90;i<=180;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
    public static void drawRoundedRect(float x, float y, float w, float h, float r, int c) { setupSmoothRender(false); setColor(c); float x1=x+w, y1=y+h; GL11.glBegin(GL11.GL_POLYGON); for(int i=180;i<=270;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=270;i<=360;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=0;i<=90;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); for(int i=90;i<=180;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
    public static void drawRoundedOutline(float x, float y, float w, float h, float r, float lW, int c) { setupSmoothRender(false); setColor(c); GL11.glLineWidth(lW); float x1=x+w, y1=y+h; GL11.glBegin(GL11.GL_LINE_LOOP); for(int i=180;i<=270;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=270;i<=360;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y+r+Math.sin(Math.toRadians(i))*r)); for(int i=0;i<=90;i+=5)GL11.glVertex2f((float)(x1-r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); for(int i=90;i<=180;i+=5)GL11.glVertex2f((float)(x+r+Math.cos(Math.toRadians(i))*r),(float)(y1-r+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
    public static void drawSolidRect(float x, float y, float w, float h, int c) { drawRoundedRect(x, y, w, h, 0, c); }
    public static void drawCircle(float cX, float cY, float r, int c) { setupSmoothRender(false); setColor(c); GL11.glBegin(GL11.GL_POLYGON); for(int i=0;i<=360;i+=5)GL11.glVertex2f((float)(cX+Math.cos(Math.toRadians(i))*r),(float)(cY+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
    public static void drawCircleOutline(float cX, float cY, float r, float lW, int c) { setupSmoothRender(false); setColor(c); GL11.glLineWidth(lW); GL11.glBegin(GL11.GL_LINE_LOOP); for(int i=0;i<=360;i+=5)GL11.glVertex2f((float)(cX+Math.cos(Math.toRadians(i))*r),(float)(cY+Math.sin(Math.toRadians(i))*r)); GL11.glEnd(); endSmoothRender(); }
}