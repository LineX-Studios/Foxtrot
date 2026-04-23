package com.linexstudios.foxtrot.Handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.linexstudios.foxtrot.Hud.FriendsHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsManager {
    // UUID -> Name
    public static final Map<String, String> friendCache = new ConcurrentHashMap<>();
    // Name (lowercase) -> UUID
    public static final Map<String, String> nameToUuid = new ConcurrentHashMap<>();

    private static final File cacheFile = new File("config/Foxtrot/friend_cache.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        if (!cacheFile.exists()) return;
        try (FileReader reader = new FileReader(cacheFile)) {
            Map<String, String> loaded = gson.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
            if (loaded != null) {
                friendCache.putAll(loaded);
                for (Map.Entry<String, String> entry : loaded.entrySet()) {
                    nameToUuid.put(entry.getValue().toLowerCase(), entry.getKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            if (!cacheFile.getParentFile().exists()) cacheFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(cacheFile)) {
                gson.toJson(friendCache, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUUIDFromName(String name) {
        return nameToUuid.get(name.toLowerCase());
    }

    public static void addFriend(String name) {
        if (nameToUuid.containsKey(name.toLowerCase())) return;
        new Thread(() -> {
            String uuid = fetchUUIDFromMojang(name);
            if (uuid != null) {
                String formattedUUID = formatUUID(uuid);
                friendCache.put(formattedUUID, name);
                nameToUuid.put(name.toLowerCase(), formattedUUID);
                save();
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.GREEN + name + EnumChatFormatting.GRAY + " added to friends."));
            } else {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.RED + "Could not find UUID for " + name));
            }
        }).start();
    }

    public static void removeFriend(String name) {
        String uuid = nameToUuid.remove(name.toLowerCase());
        if (uuid != null) {
            friendCache.remove(uuid);
            save();
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.GREEN + name + EnumChatFormatting.GRAY + " removed from friends."));
        }
    }

    public static void fetchMissingUUIDs() {
        new Thread(() -> {
            boolean updated = false;
            for (String target : FriendsHUD.friendsList) {
                if (!nameToUuid.containsKey(target.toLowerCase())) {
                    String uuid = fetchUUIDFromMojang(target);
                    if (uuid != null) {
                        String formattedUUID = formatUUID(uuid);
                        friendCache.put(formattedUUID, target);
                        nameToUuid.put(target.toLowerCase(), formattedUUID);
                        updated = true;
                    }
                    try { Thread.sleep(600); } catch (InterruptedException ignored) {}
                }
            }
            if (updated) save();
        }).start();
    }

    private static String fetchUUIDFromMojang(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();
                return json.get("id").getAsString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String formatUUID(String dashless) {
        if (dashless == null || dashless.length() != 32) return dashless;
        return dashless.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
    }
}
