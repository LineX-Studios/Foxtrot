package com.linexstudios.foxtrot.Handler;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MapDetectionHandler {
    public static final MapDetectionHandler instance = new MapDetectionHandler();
    public enum PitMap { ELEMENTS, FOUR_SEASONS, GENESIS, CORALS, CASTLE, UNKNOWN }
    
    public static PitMap currentMap = PitMap.UNKNOWN;
    private static long lastCheck = 0;
    
    public static boolean manualOverride = false;
    private static boolean wasInPit = false;
    public static boolean waitingForAutoMap = false;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String msg = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        
        if (msg.startsWith("You are currently playing on ")) {
            String mapName = msg.substring("You are currently playing on ".length()).trim().toLowerCase();
            
            if (mapName.contains("season")) {
                currentMap = PitMap.FOUR_SEASONS;
            } else if (mapName.contains("abyss") || mapName.contains("coral")) {
                currentMap = PitMap.CORALS;
            } else if (mapName.contains("genesis")) {
                currentMap = PitMap.GENESIS;
            } else if (mapName.contains("element")) {
                currentMap = PitMap.ELEMENTS;
            } else if (mapName.contains("castle")) {
                currentMap = PitMap.CASTLE;
            }
            
            manualOverride = true; 
            
            if (waitingForAutoMap) {
                event.setCanceled(true);
                waitingForAutoMap = false;
            }
        }
    }

    public static boolean isInPit() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return false;
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return false;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;

        String rawTitle = objective.getDisplayName();
        if (rawTitle == null) return false; 

        String title = StringUtils.stripControlCodes(rawTitle);
        return title.contains("THE HYPIXEL PIT") || title.contains("PIT");
    }

    public static void updateMap() {
        Minecraft mc = Minecraft.getMinecraft();
        boolean currentlyInPit = isInPit();

        if (currentlyInPit && !wasInPit) {
            if (mc.thePlayer != null) {
                mc.thePlayer.sendChatMessage("/wtfmap");
                waitingForAutoMap = true;
            }
            wasInPit = true;
        } 
        else if (!currentlyInPit && wasInPit) {
            currentMap = PitMap.UNKNOWN;
            manualOverride = false;
            wasInPit = false;
            waitingForAutoMap = false;
        }

        if (manualOverride) return; 
        if (!currentlyInPit || mc.theWorld == null || mc.thePlayer == null) return;

        long now = System.currentTimeMillis();
        if (now - lastCheck < 1000) return;
        lastCheck = now;

        int playerY = (int) mc.thePlayer.posY;

        if (!mc.theWorld.isAirBlock(new BlockPos(0, 43, 0)) || playerY < 60) {
            currentMap = PitMap.GENESIS;
        } else if (!mc.theWorld.isAirBlock(new BlockPos(0, 71, 0)) || (playerY >= 60 && playerY < 80)) {
            currentMap = PitMap.ELEMENTS;
        } else {
            if (!mc.theWorld.isAirBlock(new BlockPos(0, 82, 11))) {
                currentMap = PitMap.FOUR_SEASONS;
            } else if (!mc.theWorld.isAirBlock(new BlockPos(0, 82, 10))) {
                currentMap = PitMap.CORALS;
            } else if (playerY > 80 && playerY < 100 && mc.theWorld.isAirBlock(new BlockPos(0, 82, 11))) {
                currentMap = PitMap.CASTLE;
            } else {
                currentMap = PitMap.UNKNOWN;
            }
        }
    }
}