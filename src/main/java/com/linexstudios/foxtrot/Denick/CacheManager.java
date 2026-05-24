package com.linexstudios.foxtrot.Denick;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private static final File CONFIG_DIR = new File("config/Foxtrot");
    private static final File CACHE_FILE = new File(CONFIG_DIR, "denicked.json");
    private static final Gson gson = new Gson();
    private static Map<String, String> cache = new ConcurrentHashMap<>();

    public static void loadCache() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        if (!CACHE_FILE.exists()) return;

        try (Reader reader = new InputStreamReader(new FileInputStream(CACHE_FILE), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                cache.putAll(loaded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveCache() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(CACHE_FILE), StandardCharsets.UTF_8)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToCache(String nick, String realName) {
        cache.put(nick, realName);
        saveCache();
    }

    /**
     * Removes a player from the denicked JSON cache.
     * This is used by the /fx denickentry clear command.
     */
    public static void removeFromCache(String nick) {
        if (nick == null || cache == null) return;

        // Remove the entry (check for both exact and lowercase to be safe)
        cache.remove(nick);
        cache.remove(nick.toLowerCase());

        // Save the updated map back to denicked.json
        saveCache();
    }

    public static boolean nickInCache(String nick) {
        return cache.containsKey(nick);
    }

    public static String getFromCache(String nick) {
        return cache.get(nick);
    }

    public static Map<String, String> getCache() {
        return cache;
    }
}