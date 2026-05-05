package com.linexstudios.foxtrot.Handler.Telemetry;

import com.linexstudios.foxtrot.Handler.ConfigHandler;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraftforge.fml.common.Loader;

public class TelemetryManager {

    public static String anonymousClientId = "";
    private static Timer heartbeatTimer;

    public static void initialize() {
        if (!ConfigHandler.telemetryEnabled) {
            System.out.println("[Foxtrot] Telemetry is disabled by user. No data will be sent.");
            return;
        }

        if (anonymousClientId != null) {
            anonymousClientId = anonymousClientId.replace("\n", "").replace("\r", "").trim();
        }

        if (anonymousClientId == null || anonymousClientId.isEmpty()) {
            anonymousClientId = UUID.randomUUID().toString();
            ConfigHandler.saveConfig();
        }

        sendPing();

        // Only create the timer if it doesn't already exist to prevent duplicate timers if they toggle it on/off
        if (heartbeatTimer == null) {
            heartbeatTimer = new Timer(true);
            heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    sendPing();
                }
            }, 60000, 60000); 
        }
    }

    private static void sendPing() {
        // --- INSTANT ABORT ---
        // If the player turns off telemetry in the GUI mid-game, stop sending the pings!
        if (!ConfigHandler.telemetryEnabled) return;

        new Thread(() -> {
            try {
                URL url = new URL("https://foxtrot-api.vercel.app/ping");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setDoOutput(true);

                String modVersion = "Unknown";
                try {
                    modVersion = Loader.instance().getIndexedModList().get("foxtrot").getVersion();
                } catch (Exception e) {
                    modVersion = "0.7.4"; 
                }

                String playerHash = "";
                try {
                    String rawUuid = net.minecraft.client.Minecraft.getMinecraft().getSession().getPlayerID().replace("-", "").toLowerCase();
                    java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(rawUuid.getBytes(StandardCharsets.UTF_8));
                    StringBuilder hexString = new StringBuilder();
                    for (byte b : hash) {
                        String hex = Integer.toHexString(0xff & b);
                        if(hex.length() == 1) hexString.append('0');
                        hexString.append(hex);
                    }
                    playerHash = hexString.toString();
                } catch (Exception ignored) {}

                String jsonPayload = "{\"anonId\": \"" + anonymousClientId + "\", \"version\": \"" + modVersion + "\", \"hash\": \"" + playerHash + "\", \"cape\": \"" + com.linexstudios.foxtrot.Handler.ConfigHandler.selectedCape + "\"}";

                if (ConfigHandler.globalDebug) {
                    System.out.println("[Foxtrot-API] Sending ping payload: " + jsonPayload);
                }

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                conn.getResponseCode(); 
            } catch (Exception e) {
                // Fail silently
            }
        }).start();
    }
}