package com.linexstudios.foxtrot.Handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerTrackerHandler {
    public static final PlayerTrackerHandler instance = new PlayerTrackerHandler();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static class TrackedPlayer {
        public String name;
        public String uuid;
        public EntityOtherPlayerMP entity; 
        public String lastKnownGear;
        public String lastKnownNamePlate;

        public TrackedPlayer(String name, String uuid) {
            this.name = name;
            this.uuid = uuid;
            this.lastKnownGear = ""; 
            this.lastKnownNamePlate = null;
        }
    }

    public static final Map<String, TrackedPlayer> activePlayers = new HashMap<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) return;

        NetHandlerPlayClient netHandler = mc.getNetHandler();
        if (netHandler == null) return;

        Map<String, TrackedPlayer> newActive = new HashMap<>();

        for (NetworkPlayerInfo info : netHandler.getPlayerInfoMap()) {
            if (info == null || info.getGameProfile() == null) continue;
            
            String name = info.getGameProfile().getName();
            if (name == null || name.isEmpty() || name.startsWith("§")) continue; 

            String uuid = info.getGameProfile().getId().toString();

            // 1. Grab EXACT TabList Prefix
            String coloredName = null;
            if (info.getDisplayName() != null) {
                coloredName = info.getDisplayName().getFormattedText();
            } else if (info.getPlayerTeam() != null) {
                coloredName = ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), name);
            } else {
                coloredName = name;
            }

            // 2. DISCONNECT/GHOST FILTER:
            // Relaxed the ghost filter to allow tracking players even without formatting, 
            // especially useful in lobbies or single-player where the Pit prestige formatting doesn't exist.
            if (coloredName == null) {
                continue; 
            }

            // 3. BOUNTY & SUPPORTER STRIPPER: 
            // Removes gold bounty (e.g. " §6[5,000g]") and Pit Supporter icons (e.g. " §b§l\u272a")
            coloredName = coloredName.replaceAll("\\s*\\u00A7[0-9a-fk-or]\\[[0-9,]+g\\]", "");
            coloredName = coloredName.replaceAll("\\s*\\u00A7b\\u00A7l\\u272a", ""); // Remove blue star icon
            coloredName = coloredName.replaceAll("\\s*\\u00A7e\\u00A7l\\u272a", ""); // Remove gold star icon
            coloredName = coloredName.trim();

            TrackedPlayer tracked = activePlayers.getOrDefault(name, new TrackedPlayer(name, uuid));
            tracked.entity = null;
            tracked.lastKnownNamePlate = coloredName; // Safely set the clean, exact prefix

            // Link the physical entity if they are loaded nearby
            for (EntityPlayer p : mc.theWorld.playerEntities) {
                if (p instanceof EntityOtherPlayerMP && p.getName().equalsIgnoreCase(name)) {
                    tracked.entity = (EntityOtherPlayerMP) p;
                    break;
                }
            }

            newActive.put(name, tracked);
        }

        activePlayers.clear();
        activePlayers.putAll(newActive);
    }
}