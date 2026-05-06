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

    public static final Map<String, TrackedPlayer> activePlayers = new java.util.concurrent.ConcurrentHashMap<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) return;

        NetHandlerPlayClient netHandler = mc.getNetHandler();
        if (netHandler == null) return;

        // Build a single lookup map of entities ONCE per tick to avoid O(N^2) complexity
        Map<String, EntityOtherPlayerMP> entityLookup = new HashMap<>();
        for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (p instanceof EntityOtherPlayerMP) {
                entityLookup.put(p.getName().toLowerCase(), (EntityOtherPlayerMP) p);
            }
        }

        Map<String, TrackedPlayer> newActive = new HashMap<>();
        for (NetworkPlayerInfo info : netHandler.getPlayerInfoMap()) {
            if (info == null || info.getGameProfile() == null) continue;
            
            String name = info.getGameProfile().getName();
            if (name == null || name.isEmpty() || name.startsWith("§")) continue; 

            String uuid = info.getGameProfile().getId().toString();
            String coloredName = (info.getDisplayName() != null) ? info.getDisplayName().getFormattedText() : 
                                (info.getPlayerTeam() != null) ? ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), name) : name;

            if (coloredName == null) continue; 

            // BOUNTY & SUPPORTER STRIPPER
            coloredName = coloredName.replaceAll("\\s*\\u00A7[0-9a-fk-or]\\[[0-9,]+g\\]", "");
            coloredName = coloredName.replaceAll("\\s*\\u00A7b\\u00A7l\\u272a", ""); 
            coloredName = coloredName.replaceAll("\\s*\\u00A7e\\u00A7l\\u272a", ""); 
            coloredName = coloredName.trim();

            // GHOST & NPC FILTER: If they have no team color and no active entity, they are likely a ghost or irrelevant NPC
            if (info.getPlayerTeam() == null && entityLookup.get(name.toLowerCase()) == null) continue;

            TrackedPlayer tracked = activePlayers.getOrDefault(name, new TrackedPlayer(name, uuid));
            tracked.entity = entityLookup.get(name.toLowerCase()); // Instant O(1) lookup
            tracked.lastKnownNamePlate = coloredName;

            newActive.put(name, tracked);
        }

        activePlayers.clear();
        activePlayers.putAll(newActive);
    }
}