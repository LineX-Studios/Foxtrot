package com.linexstudios.foxtrot.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DeadLobbyFinder {

    public static final DeadLobbyFinder instance = new DeadLobbyFinder();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = false;
    public static int targetPlayers = 15;      
    public static long warpDelayMs = 4500;     

    private enum State { IDLE, WAITING_IN_HUB, WAITING_IN_PIT }
    private State currentState = State.IDLE;
    private long actionTimer = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !enabled) return;
        if (mc.thePlayer == null || mc.theWorld == null || mc.getNetHandler() == null) return;

        long now = System.currentTimeMillis();
        
        // =====================================
        // STATE 1: WAITING IN HUB -> GO TO PIT
        // =====================================
        if (currentState == State.WAITING_IN_HUB) {
            if (now - actionTimer > 4500) { // Wait 4.5s in hub before joining pit
                mc.thePlayer.sendChatMessage("/play pit");
                currentState = State.WAITING_IN_PIT;
                actionTimer = now;
            }
            return;
        }

        // =====================================
        // STATE 2: WAITING IN PIT -> SCAN LOBBY
        // =====================================
        if (now - actionTimer < warpDelayMs) return;

        if (!Ranks.instance.isInPit()) {
            mc.thePlayer.sendChatMessage("/play pit");
            actionTimer = now;
            return;
        }

        int currentPlayers = mc.getNetHandler().getPlayerInfoMap().size();

        if (currentPlayers <= targetPlayers) {
            sendMessage(EnumChatFormatting.GREEN + "Found a dead lobby with " + currentPlayers + " players!");
            toggle(); 
            return;
        }

        if (isInCombat()) {
            actionTimer = now;
            sendMessage(EnumChatFormatting.RED + "Lobby has " + currentPlayers + " players, but you are in combat! Waiting...");
            return;
        }

        // ========================================
        //  STATE 3: TOO MANY PLAYERS -> GO TO HUB
        // ========================================
        sendMessage(EnumChatFormatting.YELLOW + "Lobby has " + currentPlayers + " players. Switching lobbies...");
        mc.thePlayer.sendChatMessage("/hub");
        currentState = State.WAITING_IN_HUB;
        actionTimer = now;
    }

    private boolean isInCombat() {
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return false;
        
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;

        for (Score score : scoreboard.getSortedScores(objective)) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            if (team != null) {
                String cleanLine = StringUtils.stripControlCodes(team.formatString("")).toLowerCase();
                if (cleanLine.contains("fighting") || cleanLine.contains("combat") || cleanLine.contains("bountied")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void toggle() {
        enabled = !enabled;
        if (Minecraft.getMinecraft().thePlayer != null) {
            String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] ";
            String status = enabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(prefix + EnumChatFormatting.YELLOW + "Dead Lobby Finder: " + status));
        }
        
        if (enabled) {
            instance.currentState = State.IDLE;
            instance.actionTimer = System.currentTimeMillis();
        }
    }

    private void sendMessage(String text) {
        String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] ";
        mc.thePlayer.addChatMessage(new ChatComponentText(prefix + text));
    }
}