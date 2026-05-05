package com.linexstudios.foxtrot.Handler;

import net.minecraft.client.Minecraft;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;

public class FoxtrotUsersManager {
    private static final Map<String, String> users = new HashMap<>();
    private static final String USERS_URL = "https://foxtrot-api.vercel.app/users";

    public static void initialize() {
        // Initial fetch
        fetchUsers();

        // Refresh every 60 seconds so users see each other much faster!
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchUsers();
            }
        }, 60000, 60000);
    }

    private static void fetchUsers() {
        new Thread(() -> {
            try {
                URL url = new URL(USERS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null)
                        sb.append(line);
                    in.close();

                    JSONObject json = new JSONObject(sb.toString());
                    JSONArray usersArray = json.getJSONArray("users");

                    synchronized (users) {
                        users.clear();
                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject u = usersArray.getJSONObject(i);
                            try {
                                String hash = u.getString("hash");
                                String cape = u.optString("cape", "fx_original");
                                users.put(hash, cape);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Silently fail, we'll keep the old list or stay empty
            }
        }).start();
    }

    public static boolean isFoxtrotUser(UUID uuid) {
        if (uuid == null)
            return false;

        // Safety: Always count the current player as a user
        try {
            if (Minecraft.getMinecraft().thePlayer != null
                    && Minecraft.getMinecraft().thePlayer.getUniqueID().equals(uuid))
                return true;
        } catch (Exception ignored) {
        }

        String hash = getHash(uuid);
        synchronized (users) {
            return users.containsKey(hash);
        }
    }

    public static String getUserCape(UUID uuid) {
        if (uuid == null)
            return null;

        // Return current player's selected cape if it's them
        try {
            if (Minecraft.getMinecraft().thePlayer != null
                    && Minecraft.getMinecraft().thePlayer.getUniqueID().equals(uuid)) {
                return ConfigHandler.selectedCape;
            }
        } catch (Exception ignored) {
        }

        String hash = getHash(uuid);
        synchronized (users) {
            return users.getOrDefault(hash, null);
        }
    }

    private static String getHash(UUID uuid) {
        try {
            String rawUuid = uuid.toString().replace("-", "").toLowerCase();
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawUuid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
