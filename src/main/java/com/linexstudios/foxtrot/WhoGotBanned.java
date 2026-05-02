package com.linexstudios.foxtrot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WhoGotBanned {
    public static final WhoGotBanned instance = new WhoGotBanned();
    private final Minecraft mc = Minecraft.getMinecraft();

    // Keeps track of the lobby from the previous tick
    private final Set<String> previousPlayers = new HashSet<>();

    // Memory bank of people who left in the last 5 seconds (Name -> Timestamp)
    private final Map<String, Long> recentlyLeft = new HashMap<>();

    private static final String PROXY_API_URL = "https://foxtrot-api.vercel.app/ban";

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.getNetHandler() == null)
            return;

        Set<String> currentPlayers = new HashSet<>();

        // Grab everyone currently in the Tab List
        for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
            if (info != null && info.getGameProfile() != null && info.getGameProfile().getName() != null) {
                String name = info.getGameProfile().getName();
                // Filter out NPCs/Holograms (they usually start with color codes in the tab
                // logic)
                if (!name.startsWith("§")) {
                    currentPlayers.add(name);
                }
            }
        }

        // If previousPlayers is empty, it means we just joined the lobby. Just sync and
        // return to avoid false positives.
        if (previousPlayers.isEmpty()) {
            previousPlayers.addAll(currentPlayers);
            return;
        }

        // Compare: Who was here a tick ago, but is gone now?
        for (String prev : previousPlayers) {
            if (!currentPlayers.contains(prev)) {
                // They vanished! Log their name and the exact millisecond they disappeared.
                recentlyLeft.put(prev, System.currentTimeMillis());
            }
        }

        // Update the previous list for the next tick calculation
        previousPlayers.clear();
        previousPlayers.addAll(currentPlayers);

        // Cleanup: Delete anyone from the memory bank who left more than 15 seconds ago
        // (expanded for lag safety)
        long now = System.currentTimeMillis();
        recentlyLeft.entrySet().removeIf(entry -> now - entry.getValue() > 15000);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type == 2)
            return; // Ignore action bar messages (like health or mana)

        // Strip the formatting so we can read the raw text easily
        String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());

        // Check for Hypixel's exact Watchdog ban messages
        if (unformatted.contains("A player has been removed from your game") ||
                unformatted.contains("A player has been removed from your lobby")) {

            // The ban message just hit! Let's find the person who vanished closest to this
            // exact millisecond.
            String bannedPlayer = "Unknown (Too Fast)";
            long closestTime = Long.MAX_VALUE;

            for (Map.Entry<String, Long> entry : recentlyLeft.entrySet()) {
                long timeDiff = System.currentTimeMillis() - entry.getValue();

                if (timeDiff < closestTime) {
                    closestTime = timeDiff;
                    bannedPlayer = entry.getKey();
                }
            }

            // --- TRIGGER THE CHAT ALERT ---
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GRAY + "[" +
                                EnumChatFormatting.RED + "Foxtrot" +
                                EnumChatFormatting.GRAY + "] " +
                                EnumChatFormatting.YELLOW + "\u26A0 " + // warning symbol
                                EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + bannedPlayer + " " +
                                EnumChatFormatting.GOLD + "Has been banned!"));
            }

            if (!bannedPlayer.equals("Unknown (Too Fast)")) {
                sendBanToDiscord(bannedPlayer);

                // FIX: Only remove the player we just banned!
                // Do NOT clear the whole map. If 3 people are banned at the same millisecond,
                // the next 2 chat messages will correctly match the remaining 2 players in the
                // map!
                recentlyLeft.remove(bannedPlayer);
            }
        }
    }

    private void sendBanToDiscord(String username) {
        if (username == null || username.isEmpty())
            return;

        new Thread(() -> {
            try {
                URL url = new URL(PROXY_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setDoOutput(true);

                String jsonInputString = "{\"username\": \"" + username + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    System.out.println("[Foxtrot] API rejected the ban payload. Code: " + responseCode);
                } else {
                    System.out.println("[Foxtrot] Successfully sent ban alert to Discord for: " + username);
                }

            } catch (Exception e) {
                System.out.println("[Foxtrot] Failed to connect to API: " + e.getMessage());
            }
        }).start();
    }
}