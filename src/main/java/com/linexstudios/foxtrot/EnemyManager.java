package com.linexstudios.foxtrot.Enemy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.linexstudios.foxtrot.Hud.EnemyHUD;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnemyManager {
    // UUID -> Name
    public static final Map<String, String> enemyCache = new ConcurrentHashMap<>();
    // Name (lowercase) -> UUID
    public static final Map<String, String> nameToUuid = new ConcurrentHashMap<>();

    private static final File cacheFile = new File("config/Foxtrot/enemyuuid_cache.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void loadCache() {
        if (!cacheFile.exists()) return;
        try (FileReader reader = new FileReader(cacheFile)) {
            Map<String, String> loaded = gson.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
            if (loaded != null) {
                enemyCache.putAll(loaded);
                for (Map.Entry<String, String> entry : loaded.entrySet()) {
                    nameToUuid.put(entry.getValue().toLowerCase(), entry.getKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveCache() {
        try {
            if (!cacheFile.getParentFile().exists()) cacheFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(cacheFile)) {
                gson.toJson(enemyCache, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUUIDFromName(String name) {
        return nameToUuid.get(name.toLowerCase());
    }

    // Called on startup to bulk-fetch anyone missing from the JSON
    public static void fetchMissingUUIDs() {
        new Thread(() -> {
            boolean updated = false;
            for (String target : EnemyHUD.targetList) {
                if (!nameToUuid.containsKey(target.toLowerCase())) {
                    String uuid = fetchUUIDFromMojang(target);
                    if (uuid != null) {
                        String formattedUUID = formatUUID(uuid);
                        enemyCache.put(formattedUUID, target);
                        nameToUuid.put(target.toLowerCase(), formattedUUID);
                        updated = true;
                        System.out.println("[Foxtrot] Cached UUID for " + target);
                    }
                    // Sleep to prevent Mojang API rate-limiting us
                    try { Thread.sleep(600); } catch (InterruptedException ignored) {}
                }
            }
            if (updated) saveCache();
        }).start();
    }

    // Called when you do /fx add <name>
    public static void fetchSingleUUID(String name) {
        if (nameToUuid.containsKey(name.toLowerCase())) return;
        new Thread(() -> {
            String uuid = fetchUUIDFromMojang(name);
            if (uuid != null) {
                String formattedUUID = formatUUID(uuid);
                enemyCache.put(formattedUUID, name);
                nameToUuid.put(name.toLowerCase(), formattedUUID);
                saveCache();
                System.out.println("[Foxtrot] Cached UUID for new enemy: " + name);
            }
        }).start();
    }

    // Called when you do /fx remove <name>
    public static void removeEnemy(String name) {
        String uuid = nameToUuid.remove(name.toLowerCase());
        if (uuid != null) {
            enemyCache.remove(uuid);
            saveCache();
        }
    }

    private static String fetchUUIDFromMojang(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();
                return json.get("id").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Minecraft entity UUIDs use dashes, but Mojang API returns them without dashes. This fixes it.
    private static String formatUUID(String dashless) {
        if (dashless == null || dashless.length() != 32) return dashless;
        return dashless.replaceFirst(
            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
            "$1-$2-$3-$4-$5"
        );
    }
}