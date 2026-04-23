package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Handler.APIHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionStatsHUD extends DraggableHUD {
    public static final SessionStatsHUD instance = new SessionStatsHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;

    private long sessionStartTime = 0;
    private boolean isInPit = false;

    // --- Tracking Variables ---
    private long sessionXpGained = 0;
    private double sessionGoldGained = 0; // Gold uses decimals in Pit!
    
    private static final Pattern XP_PATTERN = Pattern.compile("\\+(\\d+)XP");
    private static final Pattern GOLD_PATTERN = Pattern.compile("\\+([0-9.]+)g");

    public SessionStatsHUD() {
        super("Session Stats", 10, 150);
    }

    /**
     * Resets all tracking variables to start a brand new session.
     */
    public void resetSession() {
        sessionStartTime = 0; 
        sessionXpGained = 0;
        sessionGoldGained = 0;
        if (mc.thePlayer != null) {
            APIHandler.updateStats(mc.thePlayer);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        resetSession();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) return;

        checkIfInPit();

        if (isInPit) {
            APIHandler.updateStats(mc.thePlayer);
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!enabled || !isInPit || !APIHandler.isLoaded) return;

        String message = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        
        // --- DEATH OR /SPAWN RESET TRIGGER ---
        if (message.contains("You died!") || message.contains("RESPAWNED!") || message.contains("Teleporting to spawn...")) {
            resetSession();
            return; 
        }
        
        boolean gainedStats = false;
        String cleanMessage = message.replace(",", "");

        // --- XP PARSING ---
        Matcher xpMatcher = XP_PATTERN.matcher(cleanMessage);
        if (xpMatcher.find()) {
            long xpEarned = Long.parseLong(xpMatcher.group(1));
            sessionXpGained += xpEarned;
            APIHandler.pitPandaXpCurrent += xpEarned;
            gainedStats = true;
            
            if (APIHandler.pitPandaXpGoal > 0) {
                APIHandler.pitPandaXpPercent = ((double) APIHandler.pitPandaXpCurrent / APIHandler.pitPandaXpGoal);
                APIHandler.pitPandaXpDescription = String.format("%.2fk/%.2fk", APIHandler.pitPandaXpCurrent / 1000.0, APIHandler.pitPandaXpGoal / 1000.0);
            }
        }

        // --- GOLD PARSING ---
        Matcher goldMatcher = GOLD_PATTERN.matcher(cleanMessage);
        if (goldMatcher.find()) {
            double goldEarned = Double.parseDouble(goldMatcher.group(1));
            sessionGoldGained += goldEarned;
            gainedStats = true;
        }

        // Start the timer EXACTLY when they get their first kill/xp/gold!
        if (gainedStats && sessionStartTime == 0) {
            sessionStartTime = System.currentTimeMillis();
        }
    }

    private void checkIfInPit() {
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard != null) {
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1); 
            if (objective != null) {
                String title = EnumChatFormatting.getTextWithoutFormattingCodes(objective.getDisplayName());
                if (title != null && title.toUpperCase().contains("PIT")) {
                    isInPit = true;
                    return;
                }
            }
        }
        isInPit = false;
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.currentScreen instanceof EditHUDGui) return;
        if (mc.currentScreen instanceof HUDSettingsGui) return;
        render(false, 0, 0); 
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled || mc.theWorld == null) return;
        if (!isInPit && !isEditing) return;

        FontRenderer fr = mc.fontRendererObj;
        int currentY = 0; 
        int maxWidth = fr.getStringWidth("Session Stats");

        fr.drawStringWithShadow(EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "Session Stats", 0, currentY, 0xFFFFFF);
        currentY += fr.FONT_HEIGHT + 2;

        if (isEditing && !APIHandler.isLoaded) {
            String timeStr = EnumChatFormatting.WHITE + "Playtime: " + EnumChatFormatting.GRAY + "01h 15m";
            String xpStr = EnumChatFormatting.WHITE + "XP Progress: " + EnumChatFormatting.AQUA + "25.93k/98.94k " + EnumChatFormatting.GRAY + "(26.2%)";
            String reqStr = EnumChatFormatting.WHITE + "Gold Needed: " + EnumChatFormatting.GOLD + "10,000g";
            String xpPerHourStr = EnumChatFormatting.WHITE + "XP/Hour: " + EnumChatFormatting.AQUA + "15,000";
            String goldPerHourStr = EnumChatFormatting.WHITE + "Gold/Hour: " + EnumChatFormatting.GOLD + "12,500.0g";
            String sessionXpStr = EnumChatFormatting.WHITE + "Session XP: " + EnumChatFormatting.AQUA + "3,500";
            String sessionGoldStr = EnumChatFormatting.WHITE + "Session Gold: " + EnumChatFormatting.GOLD + "1,250.5g";

            fr.drawStringWithShadow(timeStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(timeStr));
            currentY += fr.FONT_HEIGHT;

            fr.drawStringWithShadow(xpStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(xpStr));
            currentY += fr.FONT_HEIGHT;

            fr.drawStringWithShadow(reqStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(reqStr));
            currentY += fr.FONT_HEIGHT;

            fr.drawStringWithShadow(xpPerHourStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(xpPerHourStr));
            currentY += fr.FONT_HEIGHT;

            fr.drawStringWithShadow(goldPerHourStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(goldPerHourStr));
            currentY += fr.FONT_HEIGHT;

            fr.drawStringWithShadow(sessionXpStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(sessionXpStr));
            currentY += fr.FONT_HEIGHT;

            fr.drawStringWithShadow(sessionGoldStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(sessionGoldStr));
            currentY += fr.FONT_HEIGHT;

        } else if (APIHandler.isLoaded) {
            
            long elapsed = (sessionStartTime == 0) ? 0 : System.currentTimeMillis() - sessionStartTime;
            if (elapsed < 0) elapsed = 0;
            
            long hours = elapsed / 3600000;
            long minutes = (elapsed % 3600000) / 60000;
            long seconds = ((elapsed % 3600000) % 60000) / 1000;

            String timeFormatted;
            if (hours > 0) timeFormatted = String.format("%02dh %02dm", hours, minutes);
            else timeFormatted = String.format("%02dm %02ds", minutes, seconds);

            String timeStr = EnumChatFormatting.WHITE + "Playtime: " + EnumChatFormatting.GRAY + timeFormatted;
            fr.drawStringWithShadow(timeStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(timeStr));
            currentY += fr.FONT_HEIGHT;

            double percentCompleted = APIHandler.pitPandaXpPercent * 100.0;
            percentCompleted = Math.max(0.0, Math.min(100.0, percentCompleted));
            String percentFormatted = String.format("%.1f%%", percentCompleted);
            
            String xpStr = EnumChatFormatting.WHITE + "XP Progress: " + EnumChatFormatting.AQUA + APIHandler.pitPandaXpDescription + EnumChatFormatting.DARK_AQUA + " (" + percentFormatted + ")";
            fr.drawStringWithShadow(xpStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(xpStr));
            currentY += fr.FONT_HEIGHT;

            String goldDisplay = APIHandler.isGoldReqMet() ? EnumChatFormatting.GREEN + "Done" : EnumChatFormatting.GOLD + APIHandler.getFormattedGoldLeft() + "g";
            String reqStr = EnumChatFormatting.WHITE + "Gold Needed: " + goldDisplay;
            fr.drawStringWithShadow(reqStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(reqStr));
            currentY += fr.FONT_HEIGHT;

            // ============================================
            //      XP/HOUR CALCULATION
            // ============================================
            long displayXpPerHour = 0;
            if (sessionXpGained > 0) {
                long timeDivisor = Math.max(elapsed, 180000); 
                long liveSessionXpPerHour = (long) ((sessionXpGained / (double) timeDivisor) * 3600000);

                if (elapsed < 180000 && APIHandler.pitPandaXpHourly > 0) {
                    double progress = elapsed / 180000.0; 
                    displayXpPerHour = (long) ((APIHandler.pitPandaXpHourly * (1.0 - progress)) + (liveSessionXpPerHour * progress));
                } else {
                    displayXpPerHour = liveSessionXpPerHour;
                }
            }

            String xpPerHourStr = EnumChatFormatting.WHITE + "XP/Hour: " + EnumChatFormatting.AQUA + String.format("%,d", displayXpPerHour);
            fr.drawStringWithShadow(xpPerHourStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(xpPerHourStr));
            currentY += fr.FONT_HEIGHT;

            // ============================================
            //      GOLD/HOUR CALCULATION
            // ============================================
            double displayGoldPerHour = 0;
            if (sessionGoldGained > 0) {
                long timeDivisor = Math.max(elapsed, 180000); 
                displayGoldPerHour = (sessionGoldGained / (double) timeDivisor) * 3600000;
            }

            String goldPerHourStr = EnumChatFormatting.WHITE + "Gold/Hour: " + EnumChatFormatting.GOLD + String.format("%,.1f", displayGoldPerHour) + "g";
            fr.drawStringWithShadow(goldPerHourStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(goldPerHourStr));
            currentY += fr.FONT_HEIGHT;

            // --- SESSION TOTALS ---
            String sessionXpStr = EnumChatFormatting.WHITE + "Session XP: " + EnumChatFormatting.AQUA + String.format("%,d", sessionXpGained);
            fr.drawStringWithShadow(sessionXpStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(sessionXpStr));
            currentY += fr.FONT_HEIGHT;

            String sessionGoldStr = EnumChatFormatting.WHITE + "Session Gold: " + EnumChatFormatting.GOLD + String.format("%,.1f", sessionGoldGained) + "g";
            fr.drawStringWithShadow(sessionGoldStr, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(sessionGoldStr));
            currentY += fr.FONT_HEIGHT;

        } else {
            String loading = EnumChatFormatting.YELLOW + "Loading API...";
            fr.drawStringWithShadow(loading, 0, currentY, 0xFFFFFF);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(loading));
            currentY += fr.FONT_HEIGHT;
        }

        this.width = maxWidth;
        this.height = currentY;
    }
}