package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Handler.ConfigHandler;
import com.linexstudios.foxtrot.Render.RenderUtils;
import com.linexstudios.foxtrot.Combat.Wtap;
import com.linexstudios.foxtrot.Combat.ChestStealer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import java.io.IOException;

public class HUDSettingsGui extends GuiScreen {
    private final GuiScreen prev;
    private String[] modules = {"Potion Status", "Armor Status", "Coordinates", "Enemy List", "Nicked List", "Friend List", "Session Stats", "Event Tracker", "Regularity List", "Darks List", "Toggle Sprint", "CPS", "FPS", "Boss Bar", "Telebow Timer", "Player Counter", "Venom Timer", "W-Tap", "Chest Stealer"};
    private boolean inSettings = false, dragBox = false, dragHue = false;
    private int sel = -1, maxScr = 0, colorTgt = -1, pX = 0, pY = 0, aSld = -1;
    private float scrY = 0, tScrY = 0, cH = 0f, cS = 1f, cB = 1f;
    private int[] pal = {0xFFFFFF, 0xAAAAAA, 0x555555, 0xFF5555, 0x55FF55, 0x5555FF, 0xFFFF55, 0x55FFFF, 0xFFAA00, 0xFF55FF, 0x000000};
    private final int COLOR_ENABLED = 0xFF28A061, COLOR_DISABLED = 0xFFB82C35, COLOR_TEXT_SECONDARY = 0xFFAAAAAA, COLOR_SEPARATOR = 0x44FFFFFF, COLOR_CARD_BG = 0x44000000, COLOR_CARD_BG_HOVER = 0x66000000, COLOR_BTN_HOVER_OVERLAY = 0x22FFFFFF;

    public HUDSettingsGui(GuiScreen p) { this.prev = p; }
    public HUDSettingsGui(GuiScreen p, int d) { this.prev = p; this.sel = d; this.inSettings = true; }

    @Override public void initGui() { super.initGui(); Keyboard.enableRepeatEvents(true); }
    @Override public void handleMouseInput() throws IOException { super.handleMouseInput(); int dW = Mouse.getEventDWheel(); if(dW!=0 && !inSettings) tScrY += (dW>0?-45:45); }

    @Override public void drawScreen(int mX, int mY, float pT) {
        RenderUtils.drawSolidRect(0, 0, this.width, this.height, 0x99000000);
        GlStateManager.pushMatrix(); GlStateManager.enableTexture2D(); GlStateManager.enableAlpha(); GlStateManager.enableBlend(); GlStateManager.color(1, 1, 1, 1);
        DraggableHUD[] h = {PotionHUD.instance, ArmorHUD.instance, CoordsHUD.instance, EnemyHUD.instance, NickedHUD.instance, FriendsHUD.instance, SessionStatsHUD.instance, EventHUD.instance, RegHUD.instance, DarksHUD.instance, ToggleSprintModule.instance, CPSModule.instance, FPSModule.instance, BossBarModule.instance, TelebowHUD.instance, PlayerCounterHUD.instance, VenomTimer.instance};
        for(int i=0; i<h.length; i++) if(isE(i)) h[i].render(true, mX, mY);
        GlStateManager.popMatrix(); GlStateManager.disableLighting(); GlStateManager.enableBlend();

        int pW = 400, pH = 300, x = (this.width - pW)/2, y = (this.height - pH)/2;
        RenderUtils.drawGradientRoundedRect(x, y, pW, pH, 6, 0xCC1E1E1E, 0xCC141414); RenderUtils.drawRoundedOutline(x, y, pW, pH, 6, 1.0f, 0x44FF0000);

        if (!inSettings) {
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.RED + "Customization", x+15, y+14, -1);
            drawCross(x+pW-17, y+17, 3.5f, 1.5f, in(mX,mY,x+pW-25,y+10,15,15) ? COLOR_DISABLED : COLOR_TEXT_SECONDARY);
            RenderUtils.drawSolidRect(x+15, y+30, pW-30, 1, 0x1AFFFFFF);
            int cW=115, cH=110, sX=x+(pW-(3*cW+24))/2, sY=y+40, vH=pH-50;
            maxScr = Math.max(0, (((modules.length+2)/3)*(cH+12))-vH+15); tScrY = Math.max(0, Math.min(maxScr, tScrY)); scrY += (tScrY - scrY)*0.2f;
            doScissor(x, sY, pW, vH); GL11.glEnable(GL11.GL_SCISSOR_TEST);
            for (int i=0; i<modules.length; i++) {
                int cX=sX+(i%3)*(cW+12), cY=sY+(i/3)*(cH+12)-(int)scrY, bX=cX+6, bW=cW-12; if(cY+cH<sY || cY>sY+vH) continue;
                boolean e=isE(i), hC=in(mX,mY,cX,cY,cW,cH)&&mY>=sY&&mY<=sY+vH, hO=in(mX,mY,bX,cY+62,bW,20)&&mY>=sY&&mY<=sY+vH, hT=in(mX,mY,bX,cY+86,bW,20)&&mY>=sY&&mY<=sY+vH;
                RenderUtils.drawRoundedRect(cX,cY,cW,cH,4,hC?COLOR_CARD_BG_HOVER:COLOR_CARD_BG); RenderUtils.drawRoundedOutline(cX,cY,cW,cH,4,1f,0x22FFFFFF);
                fontRendererObj.drawStringWithShadow(modules[i], cX+(cW-fontRendererObj.getStringWidth(modules[i]))/2f, cY+27, e?-1:COLOR_TEXT_SECONDARY);
                drawInner(bX,cY+62,bW,20,3,hO?0x55FFFFFF:0x33FFFFFF,hO); fontRendererObj.drawStringWithShadow("OPTIONS", bX+(bW-fontRendererObj.getStringWidth("OPTIONS"))/2f, cY+68, -1);
                drawInner(bX,cY+86,bW,20,3,e?COLOR_ENABLED:COLOR_DISABLED,hT); fontRendererObj.drawStringWithShadow(e?"ENABLED":"DISABLED", bX+(bW-fontRendererObj.getStringWidth(e?"ENABLED":"DISABLED"))/2f, cY+92, -1);
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            if(maxScr>0) RenderUtils.drawRoundedRect(x+pW-8, sY+(scrY/maxScr)*(vH-Math.max(20,vH*(vH/(float)(((modules.length+2)/3)*(cH+12))))), 4, Math.max(20,vH*(vH/(float)(((modules.length+2)/3)*(cH+12)))), 2, 0x55FFFFFF);
        } else {
            drawInner(x+10, y+10, 60, 18, 3, 0x33000000, in(mX,mY,x+10,y+10,60,18)); fontRendererObj.drawStringWithShadow("< Back", x+22, y+15, -1); fontRendererObj.drawStringWithShadow(EnumChatFormatting.RED + modules[sel] + " Settings", x+85, y+15, -1);
            RenderUtils.drawSolidRect(x+15, y+35, pW-30, 1, 0x1AFFFFFF); int rX=x+20, rY=y+50;

            if(sel <= 16) { RenderUtils.drawRoundedRect(rX,rY,360,40,4,0x33000000); fontRendererObj.drawStringWithShadow("HUD Scale", rX+10, rY+16, 0xDDDDDD); drawSld(rX+80, rY+14, (sel<h.length)?h[sel].scale:1f, 0.5f, 1.5f, 210); drawInner(rX+300, rY+12, 50, 16, 3, 0x33FFFFFF, in(mX,mY,rX+300,rY+12,50,16)); fontRendererObj.drawStringWithShadow("Reset", rX+312, rY+16, -1); rY += 50; }

            switch(sel) {
                case 0: RenderUtils.drawRoundedRect(rX,rY,360,85,4,0x33000000); fontRendererObj.drawStringWithShadow("Name Color", rX+10, rY+10, 0xDDDDDD); drawPal(rX+10, rY+23, PotionHUD.nameColor, mX, mY, 1); fontRendererObj.drawStringWithShadow("Duration Color", rX+10, rY+50, 0xDDDDDD); drawPal(rX+10, rY+63, PotionHUD.durationColor, mX, mY, 2); break;
                case 1: RenderUtils.drawRoundedRect(rX,rY,360,75,4,0x33000000); drawTgl(rX+10, rY+10, 340, "Horizontal Layout", ArmorHUD.instance.isHorizontal, mX, mY); RenderUtils.drawSolidRect(rX+10, rY+32, 340, 1, 0x11FFFFFF); fontRendererObj.drawStringWithShadow("Durability Color", rX+10, rY+42, 0xDDDDDD); drawPal(rX+10, rY+55, ArmorHUD.durabilityColor, mX, mY, 3); break;
                case 2: RenderUtils.drawRoundedRect(rX,rY,360,135,4,0x33000000); drawTgl(rX+10, rY+10, 340, "Horizontal Layout", CoordsHUD.instance.isHorizontal, mX, mY); RenderUtils.drawSolidRect(rX+10, rY+30, 340, 1, 0x11FFFFFF); fontRendererObj.drawStringWithShadow("Axis Color", rX+10, rY+40, 0xDDDDDD); drawPal(rX+10, rY+52, CoordsHUD.axisColor, mX, mY, 4); fontRendererObj.drawStringWithShadow("Value Color", rX+10, rY+72, 0xDDDDDD); drawPal(rX+10, rY+84, CoordsHUD.numberColor, mX, mY, 5); fontRendererObj.drawStringWithShadow("Direction Color", rX+10, rY+104, 0xDDDDDD); drawPal(rX+10, rY+116, CoordsHUD.directionColor, mX, mY, 6); break;
                case 8: case 9: case 13: case 14: case 16: RenderUtils.drawRoundedRect(rX,rY,360,30,4,0x33000000); fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY+"Use scale to resize.", rX+10, rY+11, -1); break;
                case 10: RenderUtils.drawRoundedRect(rX,rY,360,160,4,0x33000000); drawTgl(rX+10, rY+10, 340, "Enable Module", ToggleSprintModule.instance.enabled, mX, mY); drawTgl(rX+10, rY+30, 340, "Toggle Sprint", ToggleSprintModule.instance.toggleSprint, mX, mY); drawTgl(rX+10, rY+50, 340, "Toggle Sneak", ToggleSprintModule.instance.toggleSneak, mX, mY); drawTgl(rX+10, rY+70, 340, "W-Tap", ToggleSprintModule.instance.wTapFix, mX, mY); drawTgl(rX+10, rY+90, 340, "Fly Boost", ToggleSprintModule.instance.flyBoost, mX, mY); RenderUtils.drawSolidRect(rX+10, rY+110, 340, 1, 0x11FFFFFF); fontRendererObj.drawStringWithShadow("Fly Boost", rX+10, rY+118, 0xDDDDDD); drawSld(rX+80, rY+116, ToggleSprintModule.instance.flyBoostAmount, 1, 10, 250); fontRendererObj.drawStringWithShadow("Text Color", rX+10, rY+140, 0xDDDDDD); drawPal(rX+80, rY+135, ToggleSprintModule.instance.textColor, mX, mY, 7); break;
                case 11: RenderUtils.drawRoundedRect(rX,rY,360,70,4,0x33000000); drawTgl(rX+10, rY+8, 340, "Show Background", CPSModule.showBackground, mX, mY); RenderUtils.drawSolidRect(rX+10, rY+28, 340, 1, 0x11FFFFFF); fontRendererObj.drawStringWithShadow("Text Color", rX+10, rY+36, 0xDDDDDD); drawPal(rX+10, rY+49, CPSModule.textColor, mX, mY, 8); break;
                case 12: RenderUtils.drawRoundedRect(rX,rY,360,70,4,0x33000000); drawTgl(rX+10, rY+8, 340, "Show Background", FPSModule.showBackground, mX, mY); RenderUtils.drawSolidRect(rX+10, rY+28, 340, 1, 0x11FFFFFF); fontRendererObj.drawStringWithShadow("Text Color", rX+10, rY+36, 0xDDDDDD); drawPal(rX+10, rY+49, FPSModule.textColor, mX, mY, 10); break;
                case 15: RenderUtils.drawRoundedRect(rX,rY,360,85,4,0x33000000); fontRendererObj.drawStringWithShadow("Prefix Color", rX+10, rY+10, 0xDDDDDD); drawPal(rX+10, rY+23, PlayerCounterHUD.prefixColor, mX, mY, 11); fontRendererObj.drawStringWithShadow("Number Color", rX+10, rY+50, 0xDDDDDD); drawPal(rX+10, rY+63, PlayerCounterHUD.countColor, mX, mY, 12); break;
                case 17: RenderUtils.drawRoundedRect(rX,rY,360,60,4,0x33000000); fontRendererObj.drawStringWithShadow("Delay (Ticks)", rX+10, rY+12, 0xDDDDDD); drawSld(rX+80, rY+10, Wtap.delay, 0, 10, 270); fontRendererObj.drawStringWithShadow("Duration (Ticks)", rX+10, rY+37, 0xDDDDDD); drawSld(rX+100, rY+35, Wtap.duration, 1, 5, 250); break;
                case 18: RenderUtils.drawRoundedRect(rX,rY,360,175,4,0x33000000); fontRendererObj.drawStringWithShadow("Min Delay", rX+10, rY+12, 0xDDDDDD); drawSld(rX+70, rY+10, ChestStealer.minDelay, 0, 20, 280); fontRendererObj.drawStringWithShadow("Max Delay", rX+10, rY+32, 0xDDDDDD); drawSld(rX+70, rY+30, ChestStealer.maxDelay, 0, 20, 280); fontRendererObj.drawStringWithShadow("Open Delay", rX+10, rY+52, 0xDDDDDD); drawSld(rX+75, rY+50, ChestStealer.openDelay, 0, 20, 275); drawTgl(rX+10, rY+70, 340, "Auto Close", ChestStealer.autoClose, mX, mY); drawTgl(rX+10, rY+90, 340, "Name Check", ChestStealer.nameCheck, mX, mY); drawTgl(rX+10, rY+110, 340, "Skip Trash", ChestStealer.skipTrash, mX, mY); drawTgl(rX+10, rY+130, 340, "More Armor", ChestStealer.moreArmor, mX, mY); drawTgl(rX+10, rY+150, 340, "More Sword", ChestStealer.moreSword, mX, mY); break;
            }
            if (colorTgt != -1) drawClr(pX, pY, mX, mY);
        } super.drawScreen(mX, mY, pT);
    }

    private boolean isE(int i) { boolean[] st={PotionHUD.enabled, ArmorHUD.enabled, CoordsHUD.enabled, EnemyHUD.enabled, NickedHUD.enabled, FriendsHUD.enabled, SessionStatsHUD.enabled, EventHUD.enabled, RegHUD.enabled, DarksHUD.enabled, ToggleSprintModule.instance.enabled, CPSModule.enabled, FPSModule.enabled, BossBarModule.enabled, TelebowHUD.enabled, PlayerCounterHUD.enabled, VenomTimer.enabled, Wtap.enabled, ChestStealer.enabled}; return i>=0&&i<st.length?st[i]:false; }
    private void tgl(int i) { if(i==0)PotionHUD.enabled=!PotionHUD.enabled; else if(i==1)ArmorHUD.enabled=!ArmorHUD.enabled; else if(i==2)CoordsHUD.enabled=!CoordsHUD.enabled; else if(i==3)EnemyHUD.enabled=!EnemyHUD.enabled; else if(i==4)NickedHUD.enabled=!NickedHUD.enabled; else if(i==5)FriendsHUD.enabled=!FriendsHUD.enabled; else if(i==6)SessionStatsHUD.enabled=!SessionStatsHUD.enabled; else if(i==7)EventHUD.enabled=!EventHUD.enabled; else if(i==8)RegHUD.enabled=!RegHUD.enabled; else if(i==9)DarksHUD.enabled=!DarksHUD.enabled; else if(i==10)ToggleSprintModule.instance.enabled=!ToggleSprintModule.instance.enabled; else if(i==11)CPSModule.enabled=!CPSModule.enabled; else if(i==12)FPSModule.enabled=!FPSModule.enabled; else if(i==13)BossBarModule.enabled=!BossBarModule.enabled; else if(i==14)TelebowHUD.enabled=!TelebowHUD.enabled; else if(i==15)PlayerCounterHUD.enabled=!PlayerCounterHUD.enabled; else if(i==16)VenomTimer.enabled=!VenomTimer.enabled; else if(i==17)Wtap.enabled=!Wtap.enabled; else if(i==18)ChestStealer.enabled=!ChestStealer.enabled; }

    @Override protected void mouseClicked(int mX, int mY, int b) throws IOException {
        if(b!=0) return; int pW=400, pH=300, x=(this.width-pW)/2, y=(this.height-pH)/2;
        if (!inSettings) {
            if(in(mX,mY,x+pW-25,y+10,15,15)) { ConfigHandler.saveConfig(); mc.displayGuiScreen(prev); return; }
            for (int i=0, sX=x+(pW-369)/2, sY=y+40; i<modules.length; i++) {
                int cX=sX+(i%3)*127, cY=sY+(i/3)*122-(int)scrY; if(cY+110<sY || cY>sY+pH-50) continue;
                if(in(mX,mY,cX+6,cY+86,103,20) && mY>=sY && mY<=sY+pH-50) { tgl(i); mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1F)); return; }
                if((in(mX,mY,cX+6,cY+62,103,20)||in(mX,mY,cX,cY,115,110)) && mY>=sY && mY<=sY+pH-50) { sel=i; inSettings=true; mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1F)); return; }
            }
        } else {
            if(colorTgt!=-1) { if(in(mX,mY,pX,pY,100,100)) dragBox=true; else if(in(mX,mY,pX+110,pY,10,100)) dragHue=true; else colorTgt=-1; return; }
            if(in(mX,mY,x+10,y+10,60,18)) { inSettings=false; mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1F)); return; }
            int rX=x+20, rY=y+50; 
            if(sel<=16 && in(mX,mY,rX+300,rY+12,50,16)) { DraggableHUD[] h={PotionHUD.instance, ArmorHUD.instance, CoordsHUD.instance, EnemyHUD.instance, NickedHUD.instance, FriendsHUD.instance, SessionStatsHUD.instance, EventHUD.instance, RegHUD.instance, DarksHUD.instance, ToggleSprintModule.instance, CPSModule.instance, FPSModule.instance, BossBarModule.instance, TelebowHUD.instance, PlayerCounterHUD.instance, VenomTimer.instance}; h[sel].scale=1f; return; } 
            if(sel<=16 && in(mX,mY,rX+80,rY+14,210,15)) { aSld=0; return; } 
            if(sel<=16) rY+=50; 
            switch(sel) {
                case 0: for(int i=0;i<11;i++) { if(in(mX,mY,rX+10+(i*22),rY+23,12,12)){if(i==10)oPal(1,rX+10+(i*22),rY+23,PotionHUD.nameColor);else PotionHUD.nameColor=pal[i];return;} if(in(mX,mY,rX+10+(i*22),rY+63,12,12)){if(i==10)oPal(2,rX+10+(i*22),rY+63,PotionHUD.durationColor);else PotionHUD.durationColor=pal[i];return;} } break;
                case 1: if(in(mX,mY,rX+10,rY+10,340,14)) ArmorHUD.instance.isHorizontal=!ArmorHUD.instance.isHorizontal; for(int i=0;i<11;i++) if(in(mX,mY,rX+10+(i*22),rY+55,12,12)){if(i==10)oPal(3,rX+10+(i*22),rY+55,ArmorHUD.durabilityColor);else ArmorHUD.durabilityColor=pal[i];return;} break;
                case 2: if(in(mX,mY,rX+10,rY+10,340,14)) CoordsHUD.instance.isHorizontal=!CoordsHUD.instance.isHorizontal; for(int i=0;i<11;i++) { if(in(mX,mY,rX+10+(i*22),rY+52,12,12)){if(i==10)oPal(4,rX+10+(i*22),rY+52,CoordsHUD.axisColor);else CoordsHUD.axisColor=pal[i];return;} if(in(mX,mY,rX+10+(i*22),rY+84,12,12)){if(i==10)oPal(5,rX+10+(i*22),rY+84,CoordsHUD.numberColor);else CoordsHUD.numberColor=pal[i];return;} if(in(mX,mY,rX+10+(i*22),rY+116,12,12)){if(i==10)oPal(6,rX+10+(i*22),rY+116,CoordsHUD.directionColor);else CoordsHUD.directionColor=pal[i];return;} } break;
                case 10: if(in(mX,mY,rX+10,rY+10,340,14)) ToggleSprintModule.instance.enabled=!ToggleSprintModule.instance.enabled; if(in(mX,mY,rX+10,rY+30,340,14)) ToggleSprintModule.instance.toggleSprint=!ToggleSprintModule.instance.toggleSprint; if(in(mX,mY,rX+10,rY+50,340,14)) ToggleSprintModule.instance.toggleSneak=!ToggleSprintModule.instance.toggleSneak; if(in(mX,mY,rX+10,rY+70,340,14)) ToggleSprintModule.instance.wTapFix=!ToggleSprintModule.instance.wTapFix; if(in(mX,mY,rX+10,rY+90,340,14)) ToggleSprintModule.instance.flyBoost=!ToggleSprintModule.instance.flyBoost; if(in(mX,mY,rX+80,rY+116,250,15)) { aSld=7; return; } for(int i=0;i<11;i++) if(in(mX,mY,rX+80+(i*22),rY+135,12,12)){if(i==10)oPal(7,rX+80+(i*22),rY+135,ToggleSprintModule.instance.textColor);else ToggleSprintModule.instance.textColor=pal[i];return;} break;
                case 11: if(in(mX,mY,rX+10,rY+8,340,14)) CPSModule.showBackground=!CPSModule.showBackground; for(int i=0;i<11;i++) if(in(mX,mY,rX+10+(i*22),rY+49,12,12)){if(i==10)oPal(8,rX+10+(i*22),rY+49,CPSModule.textColor);else CPSModule.textColor=pal[i];return;} break;
                case 12: if(in(mX,mY,rX+10,rY+8,340,14)) FPSModule.showBackground=!FPSModule.showBackground; for(int i=0;i<11;i++) if(in(mX,mY,rX+10+(i*22),rY+49,12,12)){if(i==10)oPal(10,rX+10+(i*22),rY+49,FPSModule.textColor);else FPSModule.textColor=pal[i];return;} break;
                case 15: for(int i=0;i<11;i++){ if(in(mX,mY,rX+10+(i*22),rY+23,12,12)){if(i==10)oPal(11,rX+10+(i*22),rY+23,PlayerCounterHUD.prefixColor);else PlayerCounterHUD.prefixColor=pal[i];return;} if(in(mX,mY,rX+10+(i*22),rY+63,12,12)){if(i==10)oPal(12,rX+10+(i*22),rY+63,PlayerCounterHUD.countColor);else PlayerCounterHUD.countColor=pal[i];return;} } break;
                case 17: if(in(mX,mY,rX+80,rY+14,270,15)) { aSld=1; return; } if(in(mX,mY,rX+100,rY+39,250,15)) { aSld=2; return; } break;
                case 18: if(in(mX,mY,rX+70,rY+14,280,15)) { aSld=3; return; } if(in(mX,mY,rX+70,rY+34,280,15)) { aSld=4; return; } if(in(mX,mY,rX+75,rY+54,275,15)) { aSld=5; return; } if(in(mX,mY,rX+10,rY+70,340,14)) ChestStealer.autoClose=!ChestStealer.autoClose; if(in(mX,mY,rX+10,rY+90,340,14)) ChestStealer.nameCheck=!ChestStealer.nameCheck; if(in(mX,mY,rX+10,rY+110,340,14)) ChestStealer.skipTrash=!ChestStealer.skipTrash; if(in(mX,mY,rX+10,rY+130,340,14)) ChestStealer.moreArmor=!ChestStealer.moreArmor; if(in(mX,mY,rX+10,rY+150,340,14)) ChestStealer.moreSword=!ChestStealer.moreSword; break;
            }
        }
    }

    @Override protected void mouseClickMove(int mX, int mY, int b, long t) { 
        if (!inSettings) return; int x=(this.width-400)/2, rX=x+20;
        if(dragBox) { cS=Math.max(0, Math.min(1, (mX-pX)/100f)); cB=Math.max(0, Math.min(1, 1f-((mY-pY)/100f))); int c=Color.HSBtoRGB(cH, cS, cB)&0xFFFFFF; if(colorTgt==1)PotionHUD.nameColor=c; else if(colorTgt==2)PotionHUD.durationColor=c; else if(colorTgt==3)ArmorHUD.durabilityColor=c; else if(colorTgt==4)CoordsHUD.axisColor=c; else if(colorTgt==5)CoordsHUD.numberColor=c; else if(colorTgt==6)CoordsHUD.directionColor=c; else if(colorTgt==7)ToggleSprintModule.instance.textColor=c; else if(colorTgt==8)CPSModule.textColor=c; else if(colorTgt==10)FPSModule.textColor=c; else if(colorTgt==11)PlayerCounterHUD.prefixColor=c; else if(colorTgt==12)PlayerCounterHUD.countColor=c; } 
        else if(dragHue) { cH=Math.max(0, Math.min(1, (mY-pY)/100f)); int c=Color.HSBtoRGB(cH, cS, cB)&0xFFFFFF; if(colorTgt==1)PotionHUD.nameColor=c; else if(colorTgt==2)PotionHUD.durationColor=c; else if(colorTgt==3)ArmorHUD.durabilityColor=c; else if(colorTgt==4)CoordsHUD.axisColor=c; else if(colorTgt==5)CoordsHUD.numberColor=c; else if(colorTgt==6)CoordsHUD.directionColor=c; else if(colorTgt==7)ToggleSprintModule.instance.textColor=c; else if(colorTgt==8)CPSModule.textColor=c; else if(colorTgt==10)FPSModule.textColor=c; else if(colorTgt==11)PlayerCounterHUD.prefixColor=c; else if(colorTgt==12)PlayerCounterHUD.countColor=c; } 
        else if(aSld != -1) {
            if(aSld==0) { DraggableHUD[] h={PotionHUD.instance, ArmorHUD.instance, CoordsHUD.instance, EnemyHUD.instance, NickedHUD.instance, FriendsHUD.instance, SessionStatsHUD.instance, EventHUD.instance, RegHUD.instance, DarksHUD.instance, ToggleSprintModule.instance, CPSModule.instance, FPSModule.instance, BossBarModule.instance, TelebowHUD.instance, PlayerCounterHUD.instance, VenomTimer.instance}; if(sel<h.length) h[sel].scale=0.5f+(Math.max(0, Math.min(1, (mX-(rX+80))/210f))); }
            else if(aSld==7) ToggleSprintModule.instance.flyBoostAmount=1f+(9f*Math.max(0, Math.min(1, (mX-(rX+80))/250f)));
            else if(aSld==1) Wtap.delay=Math.round(Math.max(0, Math.min(1, (mX-(rX+80))/270f))*10f);
            else if(aSld==2) Wtap.duration=Math.round(1f+Math.max(0, Math.min(1, (mX-(rX+100))/250f))*4f);
            else if(aSld==3) ChestStealer.minDelay=Math.round(Math.max(0, Math.min(1, (mX-(rX+70))/280f))*20f);
            else if(aSld==4) ChestStealer.maxDelay=Math.round(Math.max(0, Math.min(1, (mX-(rX+70))/280f))*20f);
            else if(aSld==5) ChestStealer.openDelay=Math.round(Math.max(0, Math.min(1, (mX-(rX+75))/275f))*20f);
        } 
    }
    @Override protected void mouseReleased(int mX, int mY, int s) { aSld=-1; dragBox=false; dragHue=false; ConfigHandler.saveConfig(); }

    private void drawClr(int x, int y, int mX, int mY) { RenderUtils.drawRoundedRect(x-5, y-5, 135, 110, 4, 0xFA1E1E1E); RenderUtils.setupSmoothRender(true); GL11.glBegin(GL11.GL_QUADS); RenderUtils.setColor(0xFFFFFFFF); GL11.glVertex2f(x, y); RenderUtils.setColor(0xFF000000); GL11.glVertex2f(x, y+100); RenderUtils.setColor(0xFF000000); GL11.glVertex2f(x+100, y+100); RenderUtils.setColor(Color.HSBtoRGB(cH, 1f, 1f)|0xFF000000); GL11.glVertex2f(x+100, y); GL11.glEnd(); RenderUtils.endSmoothRender(); float dX=x+(cS*100), dY=y+((1f-cB)*100); RenderUtils.drawCircle(dX, dY, 4f, 0xFF000000); RenderUtils.drawCircle(dX, dY, 3f, 0xFFFFFFFF); RenderUtils.setupSmoothRender(true); GL11.glBegin(GL11.GL_QUAD_STRIP); for(int i=0;i<=20;i++){float hS=i/20f;RenderUtils.setColor(Color.HSBtoRGB(hS, 1f, 1f)|0xFF000000);GL11.glVertex2f(x+110, y+(hS*100));GL11.glVertex2f(x+120, y+(hS*100));} GL11.glEnd(); RenderUtils.endSmoothRender(); float hY=y+(cH*100); RenderUtils.drawSolidRect(x+108, hY-1, 14, 3, 0xFFFFFFFF); RenderUtils.drawSolidRect(x+109, hY, 12, 1, 0xFF000000); }
    private void oPal(int i, int x, int y, int c) { colorTgt=i; pX=x+15; pY=y-45; float[] h=Color.RGBtoHSB((c>>16)&0xFF, (c>>8)&0xFF, c&0xFF, null); cH=h[0]; cS=h[1]; cB=h[2]; }
    public void drawMathGear(float x, float y, float r, int c, float rt) { GL11.glPushMatrix(); GL11.glTranslatef(x, y, 0); float s=r/3.5f; GL11.glScalef(s, s, 1); GlStateManager.enableTexture2D(); GlStateManager.enableBlend(); fontRendererObj.drawStringWithShadow("\u2630", -fontRendererObj.getStringWidth("\u2630")/2f, -fontRendererObj.FONT_HEIGHT/2f+1, c); GlStateManager.color(1, 1, 1, 1); GL11.glPopMatrix(); }
    private void drawCross(float cX, float cY, float s, float t, int c) { RenderUtils.setupSmoothRender(false); GL11.glEnable(GL11.GL_LINE_SMOOTH); GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST); RenderUtils.setColor(c); GL11.glLineWidth(t); GL11.glBegin(GL11.GL_LINES); GL11.glVertex2f(cX-s, cY-s); GL11.glVertex2f(cX+s, cY+s); GL11.glVertex2f(cX+s, cY-s); GL11.glVertex2f(cX-s, cY+s); GL11.glEnd(); GL11.glDisable(GL11.GL_LINE_SMOOTH); RenderUtils.endSmoothRender(); }
    private void doScissor(int x, int y, int w, int h) { Minecraft mc=Minecraft.getMinecraft(); int sf=1, k=mc.gameSettings.guiScale; if(k==0) k=1000; while(sf<k && mc.displayWidth/(sf+1)>=320 && mc.displayHeight/(sf+1)>=240) ++sf; GL11.glScissor(x*sf, mc.displayHeight-(y+h)*sf, w*sf, h*sf); }
    private void drawInner(float x, float y, float w, float h, float r, int c, boolean hv) { RenderUtils.drawRoundedRect(x, y, w, h, r, hv?c|COLOR_BTN_HOVER_OVERLAY:c); RenderUtils.drawRoundedOutline(x, y, w, h, r, 1f, 0x55FFFFFF); }
    private void drawTgl(float x, float y, float cW, String l, boolean o, int mX, int mY) { fontRendererObj.drawStringWithShadow(l, x, y+2, 0xDDDDDD); float sX=x+cW-28, sY=y+1; RenderUtils.drawRoundedRect(sX, sY, 24, 12, 6, o?COLOR_ENABLED:(in(mX,mY,sX,sY,24,12)?0xFF555555:0xFF444444)); RenderUtils.drawCircle(o?sX+18:sX+6, sY+6, 5, 0xFFFFFFFF); }
    private void drawSld(float x, float y, float v, float mn, float mx, float tW) { String vT=String.format("%.1f", v); fontRendererObj.drawStringWithShadow(vT, x+tW-fontRendererObj.getStringWidth(vT), y-6, 0xAAAAAA); RenderUtils.drawRoundedRect(x, y+4, tW, 4, 2, 0xFF333333); float fW=((v-mn)/(mx-mn))*tW; if(fW>4) RenderUtils.drawRoundedRect(x, y+4, fW, 4, 2, COLOR_DISABLED); RenderUtils.drawCircleOutline(x+fW, y+6, 5, 1, 0x88000000); RenderUtils.drawCircle(x+fW, y+6, 4, 0xFFFFFFFF); }
    private void drawPal(float x, float y, int cr, int mX, int mY, int tI) { for(int i=0;i<pal.length;i++){ float cX=x+(i*22)+6, cY=y+6; boolean hv=in(mX,mY,x+(i*22),y,12,12); if(i==10){ if(colorTgt==tI) RenderUtils.drawCircle(cX, cY, 7.5f, COLOR_DISABLED); else if(hv) RenderUtils.drawCircle(cX, cY, 7.5f, 0x55FFFFFF); RenderUtils.drawCircle(cX, cY, 6.5f, 0xFF222222); RenderUtils.drawCircle(cX, cY, 5.5f, 0xFF111111); fontRendererObj.drawStringWithShadow("+", cX-2.5f, cY-3.5f, 0xAAAAAA); } else { if(hv && pal[i]!=cr) RenderUtils.drawCircle(cX, cY, 7.5f, 0x55FFFFFF); if((pal[i]&0xFFFFFF)==(cr&0xFFFFFF)) RenderUtils.drawCircle(cX, cY, 7.5f, 0xFFFFFFFF); RenderUtils.drawCircle(cX, cY, 5.5f, pal[i]|0xFF000000); } } }
    private boolean in(float mX, float mY, float x, float y, float w, float h) { return mX>=x && mX<=x+w && mY>=y && mY<=y+h; }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); ConfigHandler.saveConfig(); }
    @Override public boolean doesGuiPauseGame() { return false; }
}