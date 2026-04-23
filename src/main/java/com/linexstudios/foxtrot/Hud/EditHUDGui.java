package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Handler.*;
import com.linexstudios.foxtrot.Denick.*;
import com.linexstudios.foxtrot.Combat.*;
import com.linexstudios.foxtrot.Render.*;
import com.linexstudios.foxtrot.Misc.*;
import com.linexstudios.foxtrot.Enemy.EnemyESP;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import java.io.IOException;

public class EditHUDGui extends GuiScreen {
    public static int collapsedX = -1, collapsedY = -1;
    public static boolean panelCollapsed = true, randomDropdownExpanded = false, mathDropdownExpanded = false;
    private int mainPanelX, mainPanelY, selectedTab = 0, lastX, lastY, dragOffsetX = 0, dragOffsetY = 0, activeSlider = 0, resizingCorner = 0;
    private final int panelW = 380, panelH = 240; 
    private boolean draggingPanel = false, isSnappedX = false, isSnappedY = false;
    private DraggableHUD draggingModule = null, resizingModule = null, lastClickedHUD = null;
    private long lastClickTime = 0;
    private String[] tabs = {"Combat", "Render", "Denick", "HUD", "Misc", "Telemetry"}; // Removed "Updates"
    private GuiTextField whitelistField;
    private String currentTooltip = null;
    private float guideAlphaX = 0.0f, guideAlphaY = 0.0f;
    private final int COLOR_ENABLED = 0xFF28A061, COLOR_DISABLED = 0xFFB82C35, COLOR_BTN_HOVER_OVERLAY = 0x22FFFFFF;

    @Override public void initGui() {
        super.initGui(); Keyboard.enableRepeatEvents(true); if (this.width <= 0) return;
        mainPanelX = (this.width - panelW) / 2; mainPanelY = (this.height - panelH) / 2;
        if (collapsedX == -1) { collapsedX = mainPanelX + panelW - 115; collapsedY = mainPanelY - 20; }
        if (whitelistField == null) { whitelistField = new GuiTextField(100, this.fontRendererObj, 0, 0, 125, 12); whitelistField.setMaxStringLength(256); whitelistField.setText(String.join(", ", AutoClicker.itemWhitelist)); whitelistField.setVisible(false); }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int mX = mouseX, mY = mouseY; this.drawDefaultBackground(); currentTooltip = null;
        float tAx = 0, tAy = 0; int cx = this.width / 2, cy = this.height / 2;
        if (draggingModule != null) { int mCx = draggingModule.x + (int)(draggingModule.width * draggingModule.scale) / 2, mCy = draggingModule.y + (int)(draggingModule.height * draggingModule.scale) / 2; if (Math.abs(mCx - cx) <= 15) tAx = 0.4f; if (Math.abs(mCy - cy) <= 15) tAy = 0.4f; }
        guideAlphaX += (tAx - guideAlphaX) * 0.2f; guideAlphaY += (tAy - guideAlphaY) * 0.2f;
        if (guideAlphaX > 0.01f) RenderUtils.drawGuideLine(cx - 0.5f, 0, 1.0f, this.height, isSnappedX ? 1.0f : 0.33f, isSnappedX ? 0.33f : 1.0f, 1.0f, guideAlphaX);
        if (guideAlphaY > 0.01f) RenderUtils.drawGuideLine(0, cy - 0.5f, this.width, 1.0f, isSnappedY ? 1.0f : 0.33f, isSnappedY ? 0.33f : 1.0f, 1.0f, guideAlphaY);

        GlStateManager.pushMatrix(); GlStateManager.enableTexture2D(); GlStateManager.enableAlpha(); GlStateManager.enableBlend(); GlStateManager.color(1, 1, 1, 1); RenderHelper.disableStandardItemLighting();
        for (DraggableHUD h : DraggableHUD.getRegistry()) if (h.isEnabled()) h.render(true, mX, mY);
        GlStateManager.popMatrix(); GlStateManager.disableLighting(); GlStateManager.enableBlend();

        if (panelCollapsed) {
            RenderUtils.drawGradientRoundedRect(collapsedX, collapsedY, 115, 18, 4, 0xCC1E1E1E, 0xCC141414); RenderUtils.drawRoundedOutline(collapsedX, collapsedY, 115, 18, 4, 1.0f, 0x44FF0000);
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.RED + "Foxtrot Settings", collapsedX + 8, collapsedY + 5, -1);
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + "+", collapsedX + 102, collapsedY + 5, isInside(mX, mY, collapsedX + 95, collapsedY, 20, 18) ? COLOR_ENABLED : 0xFFAAAAAA);
        } else {
            RenderUtils.drawGradientRoundedRect(mainPanelX, mainPanelY, panelW, panelH, 6, 0xCC1E1E1E, 0xCC141414); RenderUtils.drawRoundedOutline(mainPanelX, mainPanelY, panelW, panelH, 6, 1.0f, 0x44FF0000); RenderUtils.drawSolidRect(mainPanelX + 80, mainPanelY + 15, 1, panelH - 30, 0x1AFFFFFF);
            fontRendererObj.drawStringWithShadow(EnumChatFormatting.RED + "Foxtrot", mainPanelX + 8, mainPanelY + 8, -1); 
            drawCross(mainPanelX + panelW - 12, mainPanelY + 12, 3.5f, 1.5f, isInside(mX, mY, mainPanelX + panelW - 18, mainPanelY + 8, 12, 12) ? COLOR_DISABLED : 0xFFAAAAAA);
            for (int i = 0, y = mainPanelY + 25; i < tabs.length; i++, y += 20) { boolean hov = isInside(mX, mY, mainPanelX + 6, y, 70, 16); if (selectedTab == i) { RenderUtils.drawRoundedRect(mainPanelX + 6, y, 70, 16, 3, COLOR_DISABLED); RenderUtils.drawRoundedOutline(mainPanelX + 6, y, 70, 16, 3, 1f, 0x55FFFFFF); fontRendererObj.drawStringWithShadow(tabs[i], mainPanelX + 10, y + 4, -1); } else { drawInnerRoundedRect(mainPanelX + 6, y, 70, 16, 3, 0x33000000, hov); fontRendererObj.drawStringWithShadow(tabs[i], mainPanelX + 10, y + 4, 0xAAAAAA); } }

            int c1 = mainPanelX + 90, c2 = mainPanelX + 235, y1 = mainPanelY + 25, y2 = y1; fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + tabs[selectedTab], c1, mainPanelY + 10, -1); if (whitelistField != null) whitelistField.setVisible(false);

            if (selectedTab == 0) {
                drawSettingsCard(c1, y1, 135, AutoClicker.limitItems ? 165 : 145); fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + "Autoclicker", c1 + 5, y1 + 5, -1); y1 += 16;
                drawIOSToggle(c1 + 5, y1, 125, "Enabled", AutoClicker.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Left Click", AutoClicker.leftClick, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Hold Click", AutoClicker.holdToClick, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Fast Place", AutoClicker.fastPlaceEnabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Break Blocks", AutoClicker.breakBlocks, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Inventory Fill", AutoClicker.inventoryFill, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Limit Items", AutoClicker.limitItems, mX, mY);
                if (AutoClicker.limitItems) { whitelistField.xPosition = c1 + 5; whitelistField.yPosition = y1 + 20; whitelistField.setVisible(true); GlStateManager.color(1, 1, 1, 1); whitelistField.drawTextBox(); }
                drawSettingsCard(c2, y2, 135, randomDropdownExpanded ? 135 : 90); drawIOSSlider(c2 + 5, y2 + 5, "Min CPS", AutoClicker.minCps, 1, 20, 125); drawIOSSlider(c2 + 5, y2 + 25, "Max CPS", AutoClicker.maxCps, 1, 20, 125); drawIOSSlider(c2 + 5, y2 + 45, "Fill CPS", AutoClicker.inventoryFillCps, 5, 20, 125); 
                drawIOSButton(c2 + 5, y2 + 71, 125, 14, "Mode: " + (AutoClicker.randomMode == 0 ? "Normal" : AutoClicker.randomMode == 1 ? "Extra" : "Extra+"), mX, mY);
                if (randomDropdownExpanded) { drawIOSButton(c2 + 5, y2 + 87, 125, 12, (AutoClicker.randomMode == 0 ? EnumChatFormatting.RED : EnumChatFormatting.GRAY) + "Normal", mX, mY); drawIOSButton(c2 + 5, y2 + 101, 125, 12, (AutoClicker.randomMode == 1 ? EnumChatFormatting.RED : EnumChatFormatting.GRAY) + "Extra", mX, mY); drawIOSButton(c2 + 5, y2 + 115, 125, 12, (AutoClicker.randomMode == 2 ? EnumChatFormatting.RED : EnumChatFormatting.GRAY) + "Extra+", mX, mY); }
            } else if (selectedTab == 1) {
                drawSettingsCard(c1, y1, 135, 88); fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + "NameTags", c1 + 5, y1 + 5, -1); drawIOSToggle(c1 + 5, y1 += 18, 125, "Enabled", NameTags.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Show Health", NameTags.showHealth, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Show Items", NameTags.showItems, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Enchant Names", EnchantNames.enabled, mX, mY);
                drawSettingsCard(c1, y1 += 22, 135, 52); fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + "Player ESP", c1 + 5, y1 + 5, -1); drawIOSToggle(c1 + 5, y1 += 18, 125, "Enemy ESP", EnemyESP.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Friends ESP", FriendsESP.enabled, mX, mY);
                drawSettingsCard(c2, y2, 135, 108); fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + "Pit Misc", c2 + 5, y2 + 5, -1); drawIOSToggle(c2 + 5, y2 += 18, 125, "Sewer Chests", PitESP.espChests, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Dragon Eggs", PitESP.espDragonEggs, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Raffle Tickets", PitESP.espRaffleTickets, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Mystic Drops", PitESP.espMystics, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Low Life Mystic", LowLifeMystic.enabled, mX, mY);
            } else if (selectedTab == 2) {
                drawSettingsCard(c1, y1, 135, 50); drawIOSToggle(c1 + 5, y1 + 6, 125, "Auto Denick", AutoDenick.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 + 24, 125, "Nicked Tags", NickedRender.enabled, mX, mY);
            } else if (selectedTab == 3) {
                drawSettingsCard(c1, y1, 135, 162); drawIOSToggle(c1 + 5, y1 += 6, 125, "Enemy HUD", EnemyHUD.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Nicked HUD", NickedHUD.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Friends HUD", FriendsHUD.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Session Stats", SessionStatsHUD.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Event Tracker", EventHUD.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "Boss Bar", BossBarModule.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "CPS HUD", CPSModule.enabled, mX, mY); drawIOSToggle(c1 + 5, y1 += 18, 125, "FPS HUD", FPSModule.enabled, mX, mY);
                drawSettingsCard(c2, y2, 135, 162); drawIOSToggle(c2 + 5, y2 += 6, 125, "Reg HUD", RegHUD.enabled, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Darks HUD", DarksHUD.enabled, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Potion HUD", PotionHUD.enabled, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Armor HUD", ArmorHUD.enabled, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Coords HUD", CoordsHUD.enabled, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Telebow Timer", TelebowHUD.enabled, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Player Counter", PlayerCounterHUD.enabled, mX, mY); drawIOSToggle(c2 + 5, y2 += 18, 125, "Venom Timer", VenomTimer.enabled, mX, mY);
                drawIOSButton(mainPanelX + 155, mainPanelY + 205, 160, 16, "Customization", mX, mY);
            } else if (selectedTab == 4) {
                drawSettingsCard(c1, y1, 135, 126); fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + "Auto Use", c1 + 5, y1 + 5, -1);
                drawIOSToggle(c1 + 5, y1 += 18, 125, "Pant Swap", AutoPantSwap.pantSwapEnabled, mX, mY); if (isInside(mX, mY, c1 + 5, y1, 125, 12)) currentTooltip = EnumChatFormatting.RED + "\u26A0 " + EnumChatFormatting.YELLOW + "USE AT YOUR OWN RISK." + EnumChatFormatting.GRAY + " - Auto Swap / Hold right-click while holding over pants in your inventory to instantly equip it.";
                drawIOSToggle(c1 + 5, y1 += 18, 125, "Venom Swap", AutoPantSwap.venomSwapEnabled, mX, mY); if (isInside(mX, mY, c1 + 5, y1, 125, 12)) currentTooltip = EnumChatFormatting.RED + "\u26A0 " + EnumChatFormatting.YELLOW + "USE AT YOUR OWN RISK." + EnumChatFormatting.GRAY + " - Automatically swaps to diamond pants if you get venomed.";
                drawIOSToggle(c1 + 5, y1 += 18, 125, "Auto Heal", AutoGhead.enabled, mX, mY); if (isInside(mX, mY, c1 + 5, y1, 125, 12)) currentTooltip = EnumChatFormatting.RED + "\u26A0 " + EnumChatFormatting.YELLOW + "USE AT YOUR OWN RISK." + EnumChatFormatting.GRAY + " - Automatically use Ghead or First Aid Egg.";
                drawIOSToggle(c1 + 5, y1 += 18, 125, "Auto Pod", AutoPantSwap.autoPodEnabled, mX, mY); if (isInside(mX, mY, c1 + 5, y1, 125, 12)) currentTooltip = EnumChatFormatting.RED + "\u26A0 " + EnumChatFormatting.YELLOW + "USE AT YOUR OWN RISK." + EnumChatFormatting.GRAY + " - Automatically use Escape Pods when you are at low health";
                drawIOSToggle(c1 + 5, y1 += 18, 125, "Auto Bullet Time", AutoBulletTime.enabled, mX, mY); if (isInside(mX, mY, c1 + 5, y1, 125, 12)) currentTooltip = EnumChatFormatting.YELLOW + "\u26A0 " + EnumChatFormatting.RED + "USE AT YOUR OWN RISK, POSSIBLE BAN." + EnumChatFormatting.GRAY + " - Automatically switch to Bullet Time when you right click on any sword.";
                
                int mathH = mathDropdownExpanded ? 115 : 75; drawSettingsCard(c2, y2, 135, mathH); fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + "Chat", c2 + 5, y2 + 5, -1);
                drawIOSToggle(c2 + 5, y2 + 18, 125, "Auto Quick Math", AutoQuickMath.enabled, mX, mY); drawIOSSlider(c2 + 5, y2 + 36, "Delay (ms)", AutoQuickMath.baseDelayMs, 0, 5000, 125); drawIOSButton(c2 + 5, y2 + 58, 125, 14, "Mode: " + (AutoQuickMath.randomMode == 0 ? "Normal" : AutoQuickMath.randomMode == 1 ? "Extra" : "Extra+"), mX, mY);
                if (mathDropdownExpanded) { drawIOSButton(c2 + 5, y2 + 74, 125, 12, (AutoQuickMath.randomMode == 0 ? EnumChatFormatting.RED : EnumChatFormatting.GRAY) + "Normal", mX, mY); drawIOSButton(c2 + 5, y2 + 88, 125, 12, (AutoQuickMath.randomMode == 1 ? EnumChatFormatting.RED : EnumChatFormatting.GRAY) + "Extra", mX, mY); drawIOSButton(c2 + 5, y2 + 102, 125, 12, (AutoQuickMath.randomMode == 2 ? EnumChatFormatting.RED : EnumChatFormatting.GRAY) + "Extra+", mX, mY); }
                
                int wY = mainPanelY + 25 + mathH + 4; drawSettingsCard(c2, wY, 135, 55); fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + "W-Tap", c2 + 5, wY + 5, -1);
                drawIOSToggle(c2 + 5, wY + 18, 125, "Enabled", Wtap.enabled, mX, mY); drawIOSButton(c2 + 5, wY + 36, 125, 14, "W-Tap Settings", mX, mY);
                
                int cY = wY + 59; drawSettingsCard(c2, cY, 135, 55); fontRendererObj.drawStringWithShadow(EnumChatFormatting.BOLD + "Chest Stealer", c2 + 5, cY + 5, -1);
                drawIOSToggle(c2 + 5, cY + 18, 125, "Enabled", ChestStealer.enabled, mX, mY); drawIOSButton(c2 + 5, cY + 36, 125, 14, "Stealer Settings", mX, mY);
            } else if (selectedTab == 5) {
                drawSettingsCard(c1, y1, 280, 58); 
                drawIOSToggle(c1 + 5, y1 + 6, 270, "Enable Telemetry Stats", ConfigHandler.telemetryEnabled, mX, mY); 
                drawIOSToggle(c1 + 5, y1 + 24, 270, "Global Debug Mode", ConfigHandler.globalDebug, mX, mY);
                fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Help us improve Foxtrot PIT!", c1 + 5, y1 + 42, -1);
                
                drawSettingsCard(c1, y1 += 63, 280, 125); fontRendererObj.drawStringWithShadow(EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "Privacy & Anonymity", c1 + 5, y1 += 5, -1); fontRendererObj.drawString(EnumChatFormatting.GRAY + "All telemetry data is " + EnumChatFormatting.WHITE + "100% Anonymous.", c1 + 5, y1 += 15, -1); fontRendererObj.drawString(EnumChatFormatting.GRAY + "We CANNOT track or collect:", c1 + 5, y1 += 12, -1); fontRendererObj.drawString(EnumChatFormatting.RED + "\u2718 " + EnumChatFormatting.GRAY + "Your Minecraft Name or UUID", c1 + 10, y1 += 10, -1); fontRendererObj.drawString(EnumChatFormatting.RED + "\u2718 " + EnumChatFormatting.GRAY + "Your Session Token or Passwords", c1 + 10, y1 += 10, -1); fontRendererObj.drawString(EnumChatFormatting.RED + "\u2718 " + EnumChatFormatting.GRAY + "Your IP Address or Location", c1 + 10, y1 += 10, -1); fontRendererObj.drawString(EnumChatFormatting.RED + "\u2718 " + EnumChatFormatting.GRAY + "Your Chat Logs or Inventories", c1 + 10, y1 += 10, -1); fontRendererObj.drawString(EnumChatFormatting.GRAY + "We only track active player counts to", c1 + 5, y1 += 15, -1); fontRendererObj.drawString(EnumChatFormatting.GRAY + "display live stats on linex-studios.github.io", c1 + 5, y1 += 10, -1);
            }
        }
        if (currentTooltip != null) { int sw = fontRendererObj.getStringWidth(currentTooltip), dx = mX + 10, dy = mY - 10; if (dx + sw + 8 > this.width) dx = this.width - sw - 8; RenderUtils.drawRoundedRect(dx, dy, sw + 8, fontRendererObj.FONT_HEIGHT + 8, 3, 0xF9111111); RenderUtils.drawRoundedOutline(dx, dy, sw + 8, fontRendererObj.FONT_HEIGHT + 8, 3, 1, 0x55FFFFFF); fontRendererObj.drawStringWithShadow(currentTooltip, dx + 4, dy + 4, -1); }
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int mX = mouseX, mY = mouseY, b = mouseButton; if(b!=0 && b!=2) return; 
        if (b == 0) {
            if(panelCollapsed) { if(isInside(mX, mY, collapsedX + 95, collapsedY, 20, 18)) { panelCollapsed=false; return; } if(isInside(mX, mY, collapsedX, collapsedY, 95, 18)) { draggingPanel=true; lastX=mX; lastY=mY; return; } } 
            else {
                if(isInside(mX, mY, mainPanelX + panelW - 18, mainPanelY + 8, 12, 12)) { panelCollapsed=true; return; }
                for(int i=0, y=mainPanelY+25; i<tabs.length; i++, y+=20) if(isInside(mX, mY, mainPanelX+6, y, 70, 16)) { selectedTab=i; return; }
                int c1=mainPanelX+90, c2=mainPanelX+235, y1=mainPanelY+25, y2=y1;
                if(selectedTab==0) {
                    if(AutoClicker.limitItems && whitelistField!=null) whitelistField.mouseClicked(mX, mY, b);
                    if(isInside(mX, mY, c1+5, y1+=16, 125, 12)) AutoClicker.enabled=!AutoClicker.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoClicker.leftClick=!AutoClicker.leftClick; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoClicker.holdToClick=!AutoClicker.holdToClick; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoClicker.fastPlaceEnabled=!AutoClicker.fastPlaceEnabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoClicker.breakBlocks=!AutoClicker.breakBlocks; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoClicker.inventoryFill=!AutoClicker.inventoryFill; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoClicker.limitItems=!AutoClicker.limitItems;
                    int s1 = y2 + 5; int s2 = y2 + 25; int s3 = y2 + 45; int bY = y2 + 71; boolean cD=false; 
                    if(randomDropdownExpanded){ if(isInside(mX, mY, c2+5, y2+87, 125, 12)){ AutoClicker.randomMode=0; randomDropdownExpanded=false; cD=true; } if(isInside(mX, mY, c2+5, y2+101, 125, 12)){ AutoClicker.randomMode=1; randomDropdownExpanded=false; cD=true; } if(isInside(mX, mY, c2+5, y2+115, 125, 12)){ AutoClicker.randomMode=2; randomDropdownExpanded=false; cD=true; } }
                    if(!cD){ if(isInside(mX, mY, c2+5, bY, 125, 14)) randomDropdownExpanded=!randomDropdownExpanded; else if(!randomDropdownExpanded){ if(isInside(mX, mY, c2+5, s1, 125, 15)){ activeSlider=1; updateEditSlider(1, mX, c2+5, 125); } else if(isInside(mX, mY, c2+5, s2, 125, 15)){ activeSlider=2; updateEditSlider(2, mX, c2+5, 125); } else if(isInside(mX, mY, c2+5, s3, 125, 15)){ activeSlider=3; updateEditSlider(3, mX, c2+5, 125); } } }
                } else if(selectedTab==1) {
                    if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) NameTags.enabled=!NameTags.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) NameTags.showHealth=!NameTags.showHealth; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) NameTags.showItems=!NameTags.showItems; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) EnchantNames.enabled=!EnchantNames.enabled;
                    if(isInside(mX, mY, c1+5, y1+=36, 125, 12)) EnemyESP.enabled=!EnemyESP.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) FriendsESP.enabled=!FriendsESP.enabled;
                    if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) PitESP.espChests=!PitESP.espChests; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) PitESP.espDragonEggs=!PitESP.espDragonEggs; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) PitESP.espRaffleTickets=!PitESP.espRaffleTickets; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) PitESP.espMystics=!PitESP.espMystics; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) LowLifeMystic.enabled=!LowLifeMystic.enabled;
                } else if(selectedTab==2) { if(isInside(mX, mY, c1+5, y1+6, 125, 12)) AutoDenick.enabled=!AutoDenick.enabled; if(isInside(mX, mY, c1+5, y1+24, 125, 12)) NickedRender.enabled=!NickedRender.enabled;
                } else if(selectedTab==3) {
                    if(isInside(mX, mY, c1+5, y1+=6, 125, 12)) EnemyHUD.enabled=!EnemyHUD.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) NickedHUD.enabled=!NickedHUD.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) FriendsHUD.enabled=!FriendsHUD.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) SessionStatsHUD.enabled=!SessionStatsHUD.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) EventHUD.enabled=!EventHUD.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) BossBarModule.enabled=!BossBarModule.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) CPSModule.enabled=!CPSModule.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) FPSModule.enabled=!FPSModule.enabled;
                    if(isInside(mX, mY, c2+5, y2+=6, 125, 12)) RegHUD.enabled=!RegHUD.enabled; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) DarksHUD.enabled=!DarksHUD.enabled; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) PotionHUD.enabled=!PotionHUD.enabled; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) ArmorHUD.enabled=!ArmorHUD.enabled; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) CoordsHUD.enabled=!CoordsHUD.enabled; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) TelebowHUD.enabled=!TelebowHUD.enabled; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) PlayerCounterHUD.enabled=!PlayerCounterHUD.enabled; if(isInside(mX, mY, c2+5, y2+=18, 125, 12)) VenomTimer.enabled=!VenomTimer.enabled;
                    if(isInside(mX, mY, mainPanelX + 155, mainPanelY + 205, 160, 16)) mc.displayGuiScreen(new HUDSettingsGui(this));
                } else if(selectedTab==4) {
                    if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoPantSwap.pantSwapEnabled=!AutoPantSwap.pantSwapEnabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoPantSwap.venomSwapEnabled=!AutoPantSwap.venomSwapEnabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoGhead.enabled=!AutoGhead.enabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoPantSwap.autoPodEnabled=!AutoPantSwap.autoPodEnabled; if(isInside(mX, mY, c1+5, y1+=18, 125, 12)) AutoBulletTime.enabled=!AutoBulletTime.enabled;
                    if(isInside(mX, mY, c2+5, y2+18, 125, 12)) AutoQuickMath.enabled=!AutoQuickMath.enabled;
                    int sY = y2 + 36; int bY = y2 + 58; boolean mD=false; 
                    if(mathDropdownExpanded){ if(isInside(mX, mY, c2+5, y2+74, 125, 12)){ AutoQuickMath.randomMode=0; mathDropdownExpanded=false; mD=true; } if(isInside(mX, mY, c2+5, y2+88, 125, 12)){ AutoQuickMath.randomMode=1; mathDropdownExpanded=false; mD=true; } if(isInside(mX, mY, c2+5, y2+102, 125, 12)){ AutoQuickMath.randomMode=2; mathDropdownExpanded=false; mD=true; } }
                    if(!mD){ if(isInside(mX, mY, c2+5, bY, 125, 14)) mathDropdownExpanded=!mathDropdownExpanded; else if(!mathDropdownExpanded){ if(isInside(mX, mY, c2+5, sY, 125, 15)){ activeSlider=4; updateEditSlider(4, mX, c2+5, 125); } } }
                    int mathH = mathDropdownExpanded ? 115 : 75; int wY = mainPanelY + 25 + mathH + 4;
                    if(isInside(mX, mY, c2+5, wY+18, 125, 12)) Wtap.enabled = !Wtap.enabled; if(isInside(mX, mY, c2+5, wY+36, 125, 14)) mc.displayGuiScreen(new HUDSettingsGui(this, 17));
                    int cY = wY + 59;
                    if(isInside(mX, mY, c2+5, cY+18, 125, 12)) ChestStealer.enabled = !ChestStealer.enabled; if(isInside(mX, mY, c2+5, cY+36, 125, 14)) mc.displayGuiScreen(new HUDSettingsGui(this, 18));
                } else if(selectedTab==5) { 
                    if(isInside(mX, mY, c1+5, y1+6, 270, 12)) { ConfigHandler.telemetryEnabled=!ConfigHandler.telemetryEnabled; if(ConfigHandler.telemetryEnabled) TelemetryManager.initialize(); }
                    if(isInside(mX, mY, c1+5, y1+24, 270, 12)) { ConfigHandler.globalDebug=!ConfigHandler.globalDebug; }
                }
            }
        } super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!panelCollapsed && isInside(mX, mY, mainPanelX, mainPanelY, panelW, panelH)) return; 
        
        for(DraggableHUD h : DraggableHUD.getRegistry()) { 
            if(!h.isEnabled()) continue; 
            if(b==2 && h.isHovered(mX, mY)) { h.scale=1; ConfigHandler.saveConfig(); return; } 
            
            if(b==0 && h.isHovered(mX, mY)) { 
                long now = System.currentTimeMillis(); 
                int cr = h.getHoveredCorner(mX, mY);
                
                // RESIZING
                if(cr != 0) {
                    resizingModule = h;
                    resizingCorner = cr;
                    lastX = mX;
                    lastY = mY;
                    return;
                }
                
                // DOUBLE CLICK
                if(h == lastClickedHUD && (now - lastClickTime < 300)) { 
                    mc.displayGuiScreen(new HUDSettingsGui(this, getIdx(h.name))); 
                    return; 
                } 
                lastClickTime = now; 
                lastClickedHUD = h; 
                
                // DRAGGING
                draggingModule = h; 
                dragOffsetX = mX - h.x; 
                dragOffsetY = mY - h.y; 
                return; 
            } 
        }
    }

    private int getIdx(String n) { String l = n.toLowerCase(); return l.contains("potion")?0 : l.contains("armor")?1 : l.contains("coord")?2 : l.contains("enemy")?3 : l.contains("nick")?4 : l.contains("friend")?5 : l.contains("session")?6 : l.contains("event")?7 : l.contains("reg")?8 : l.contains("dark")?9 : l.contains("sprint")?10 : l.contains("cps")?11 : l.contains("fps")?12 : l.contains("boss")?13 : l.contains("telebow")?14 : l.contains("counter")?15 : l.contains("venom")?16 : 0; }

    @Override protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        int mX = mouseX, mY = mouseY, dX = mX - lastX, dY = mY - lastY;
        if(draggingPanel && panelCollapsed){ collapsedX=Math.max(0, Math.min(width-115, collapsedX+dX)); collapsedY=Math.max(0, Math.min(height-18, collapsedY+dY)); }
        else if(activeSlider!=0 && !panelCollapsed) updateEditSlider(activeSlider, mX, mainPanelX + (activeSlider == 4 ? 240 : 240), 125); 
        else if(resizingModule!=null) resizingModule.handleResize(dX, dY, resizingCorner);
        else if(draggingModule!=null) { int tX=mX-dragOffsetX, tY=mY-dragOffsetY, sW=(int)(draggingModule.width*draggingModule.scale), sH=(int)(draggingModule.height*draggingModule.scale), mCx=tX+sW/2, mCy=tY+sH/2, cX=width/2, cY=height/2; isSnappedX=Math.abs(mCx-cX)<=6; draggingModule.x = isSnappedX ? cX-sW/2 : Math.max(0, Math.min(width-sW, tX)); isSnappedY=Math.abs(mCy-cY)<=6; draggingModule.y = isSnappedY ? cY-sH/2 : Math.max(0, Math.min(height-sH, tY)); } lastX = mX; lastY = mY;
    }

    @Override protected void mouseReleased(int mouseX, int mouseY, int state) { draggingPanel=false; activeSlider=0; draggingModule=null; resizingModule=null; resizingCorner=0; isSnappedX=isSnappedY=false; ConfigHandler.saveConfig(); }
    private void drawCross(float cX, float cY, float s, float t, int c) { RenderUtils.setupSmoothRender(false); GL11.glEnable(GL11.GL_LINE_SMOOTH); GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST); RenderUtils.setColor(c); GL11.glLineWidth(t); GL11.glBegin(GL11.GL_LINES); GL11.glVertex2f(cX - s, cY - s); GL11.glVertex2f(cX + s, cY + s); GL11.glVertex2f(cX + s, cY - s); GL11.glVertex2f(cX - s, cY + s); GL11.glEnd(); GL11.glDisable(GL11.GL_LINE_SMOOTH); RenderUtils.endSmoothRender(); }
    private void drawInnerRoundedRect(float x, float y, float w, float h, float r, int c, boolean hv) { RenderUtils.drawRoundedRect(x, y, w, h, r, hv?c|COLOR_BTN_HOVER_OVERLAY:c); RenderUtils.drawRoundedOutline(x, y, w, h, r, 1f, 0x55FFFFFF); }
    private void updateEditSlider(int s, int mX, int sX, int w) { float p = Math.max(0, Math.min(1, (mX - sX) / (float)w)); if (s == 1) AutoClicker.minCps = 1 + (p * 19); if (s == 2) AutoClicker.maxCps = 1 + (p * 19); if (s == 3) AutoClicker.inventoryFillCps = 5 + (p * 15); if (s == 4) AutoQuickMath.baseDelayMs = p * 5000; }
    private void drawSettingsCard(int x, int y, int w, int h) { RenderUtils.drawRoundedRect(x, y, w, h, 4, 0x33000000); }
    private void drawIOSSlider(float x, float y, String l, float v, float min, float max, float tW) { fontRendererObj.drawStringWithShadow(l, x, y, 0xDDDDDD); String vT = String.format("%.1f", v); fontRendererObj.drawStringWithShadow(vT, x+tW-fontRendererObj.getStringWidth(vT), y, 0xAAAAAA); float tY = y+12; RenderUtils.drawRoundedRect(x, tY, tW, 4, 2, 0xFF333333); float fW = ((v-min)/(max-min))*tW; if(fW>4) RenderUtils.drawRoundedRect(x, tY, fW, 4, 2, COLOR_DISABLED); RenderUtils.drawCircleOutline(x+fW, tY+2, 5, 1, 0x88000000); RenderUtils.drawCircle(x+fW, tY+2, 4, 0xFFFFFFFF); }
    private void drawIOSToggle(float x, float y, float cW, String l, boolean o, int mX, int mY) { fontRendererObj.drawStringWithShadow(l, x, y+2, 0xDDDDDD); float sX = x+cW-28, sY = y+1; RenderUtils.drawRoundedRect(sX, sY, 24, 12, 6, o?COLOR_ENABLED:(isInside(mX,mY,sX,sY,24,12)?0xFF555555:0xFF444444)); RenderUtils.drawCircle(o?sX+18:sX+6, sY+6, 5, 0xFFFFFFFF); }
    private void drawIOSButton(float x, float y, float w, float h, String t, int mX, int mY) { drawInnerRoundedRect(x, y, w, h, 3, 0x33FFFFFF, isInside(mX, mY, x, y, w, h)); fontRendererObj.drawStringWithShadow(t, x+(w-fontRendererObj.getStringWidth(t))/2f, y+(h-8)/2f, -1); }
    private boolean isInside(float mX, float mY, float x, float y, float w, float h) { return mX>=x && mX<=x+w && mY>=y && mY<=y+h; }
    @Override public void onGuiClosed() { 
        Keyboard.enableRepeatEvents(false); 
        if (whitelistField != null) {
            String txt = whitelistField.getText().trim();
            AutoClicker.itemWhitelist = new java.util.ArrayList<>(java.util.Arrays.asList(txt.split("\\s*,\\s*")));
        }
        ConfigHandler.saveConfig(); 
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}