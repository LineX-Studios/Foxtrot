package com.linexstudios.foxtrot;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WhoGotBanned {
    public static final WhoGotBanned instance = new WhoGotBanned();
    private final Minecraft mc = Minecraft.getMinecraft();

    // Memory bank for players who just left (Mixin)
    private final Map<String, Long> recentlyLeft = new HashMap<>();
    private long banWindowOpenUntil = 0L;

    private static final String PROXY_API_URL = "https://foxtrot-api.vercel.app/ban";

    // --- CALLED INSTANTLY BY THE MIXIN ---
    public void logPlayerRemoval(String playerName) {
        long now = System.currentTimeMillis();

        // SCENARIO B: Chat message already arrived, waiting for packet
        if (now < banWindowOpenUntil) {
            triggerBanAlert(playerName);
            banWindowOpenUntil = 0L; // Close the window
        } else {
            // Normal disconnect. Store them just in case the chat packet is delayed.
            recentlyLeft.put(playerName, now);
        }

        // Cleanup old entries (older than 2.5 seconds) so no memory leaks yay
        recentlyLeft.entrySet().removeIf(entry -> now - entry.getValue() > 2500);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type == 2) return; 

        String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());

        if (unformatted.contains("A player has been removed from your game") ||
            unformatted.contains("A player has been removed from your lobby")) {

            // SCENARIO A: Tablist packet beat the chat packet, they are in our memory bank
            if (!recentlyLeft.isEmpty()) {
                String bannedPlayer = null;
                long closestTime = Long.MAX_VALUE;

                for (Map.Entry<String, Long> entry : recentlyLeft.entrySet()) {
                    long timeDiff = System.currentTimeMillis() - entry.getValue();
                    if (timeDiff < closestTime) {
                        closestTime = timeDiff;
                        bannedPlayer = entry.getKey();
                    }
                }

                if (bannedPlayer != null) {
                    triggerBanAlert(bannedPlayer);
                    recentlyLeft.clear();
                    return; 
                }
            }

            // SCENARIO B SETUP: Tablist packet hasn't arrived yet - this shit been a total
            banWindowOpenUntil = System.currentTimeMillis() + 2000L;
        }
    }

    private void triggerBanAlert(String bannedPlayer) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GRAY + "[" + 
                    EnumChatFormatting.RED + "Foxtrot" + 
                    EnumChatFormatting.GRAY + "] " + 
                    EnumChatFormatting.YELLOW + "\u26A0 " + 
                    EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + bannedPlayer + " " + 
                    EnumChatFormatting.GOLD + "Has been banned!"
            ));
        }
        sendBanToDiscord(bannedPlayer);
    }

    private void sendBanToDiscord(String username) {
        if (username == null) return;

        new Thread(() -> {
            try {
                URL url = new URL(PROXY_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = "{\"username\": \"" + username + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    System.out.println("[Foxtrot] Vercel API rejected the ban payload. Code: " + responseCode);
                }

            } catch (Exception e) {
                System.out.println("[Foxtrot] Failed to connect to Vercel API.");
            }
        }).start();
    }
}