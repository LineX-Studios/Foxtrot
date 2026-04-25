package com.linexstudios.foxtrot.Handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeammateManager {
    // UUID -> Name
    public static final Map<String, String> teammateCache = new ConcurrentHashMap<>();
    // Name (lowercase) -> UUID
    public static final Map<String, String> nameToUuid = new ConcurrentHashMap<>();
    
    // Guild Member UUIDs (Separate to avoid saving them to manual list)
    public static final Set<String> guildMembers = Collections.synchronizedSet(new HashSet<>());

    private static final File cacheFile = new File("config/Foxtrot/teammate_cache.json");
    private static final File lastFetchFile = new File("config/Foxtrot/guild_last_fetch.txt");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        if (!cacheFile.exists()) return;
        try (FileReader reader = new FileReader(cacheFile)) {
            Map<String, String> loaded = gson.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
            if (loaded != null) {
                teammateCache.putAll(loaded);
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
                gson.toJson(teammateCache, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isTeammate(String uuid, String name) {
        if (uuid != null && (teammateCache.containsKey(uuid) || guildMembers.contains(uuid))) return true;
        if (name != null && nameToUuid.containsKey(name.toLowerCase())) return true;
        return false;
    }

    public static void addTeammate(String name) {
        if (nameToUuid.containsKey(name.toLowerCase())) return;
        new Thread(() -> {
            String uuid = fetchUUIDFromMojang(name);
            if (uuid != null) {
                String formattedUUID = formatUUID(uuid);
                teammateCache.put(formattedUUID, name);
                nameToUuid.put(name.toLowerCase(), formattedUUID);
                save();
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.AQUA + name + EnumChatFormatting.GRAY + " added to teammates."));
            } else {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.RED + "Could not find UUID for " + name));
            }
        }).start();
    }

    public static void removeTeammate(String name) {
        String uuid = nameToUuid.remove(name.toLowerCase());
        if (uuid != null) {
            teammateCache.remove(uuid);
            save();
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.AQUA + name + EnumChatFormatting.GRAY + " removed from teammates."));
        }
    }

    private static boolean hasSyncedThisSession = false;
    private static final File guildInfoFile = new File("config/Foxtrot/guild_info.txt");

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void onWorldJoin(net.minecraftforge.event.world.WorldEvent.Load event) {
        if (Minecraft.getMinecraft().thePlayer == null) return;
        
        // Always try to sync once per game launch
        if (!hasSyncedThisSession) {
            updateGuild(Minecraft.getMinecraft().thePlayer.getName(), true);
            hasSyncedThisSession = true;
            return;
        }

        // Otherwise, only sync if 24h have passed (handled inside updateGuild)
        updateGuild(Minecraft.getMinecraft().thePlayer.getName(), false);
    }

    public static void updateGuild(String username, boolean forceSessionSync) {
        long now = System.currentTimeMillis();
        
        if (!forceSessionSync) {
            if (lastFetchFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(lastFetchFile))) {
                    long last = Long.parseLong(reader.readLine());
                    if (now - last < 24 * 60 * 60 * 1000) {
                        return; // Already updated recently
                    }
                } catch (Exception ignored) {}
            }
        }

        new Thread(() -> {
            try {
                URL url = new URL("https://pitpal.rocks/api/ign/guild/" + username);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                    JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                    reader.close();

                    if (json.has("members")) {
                        String currentGuildId = json.has("guild_id") ? json.get("guild_id").getAsString() : "unknown";
                        
                        // Detect guild change
                        boolean guildChanged = true;
                        if (guildInfoFile.exists()) {
                            try (BufferedReader br = new BufferedReader(new FileReader(guildInfoFile))) {
                                String savedId = br.readLine();
                                if (currentGuildId.equals(savedId)) guildChanged = false;
                            } catch (Exception ignored) {}
                        }

                        // If guild changed, we always update even if not forced
                        if (guildChanged || forceSessionSync) {
                            JsonArray members = json.getAsJsonArray("members");
                            guildMembers.clear();
                            for (int i = 0; i < members.size(); i++) {
                                JsonObject m = members.get(i).getAsJsonObject();
                                if (m.has("uuid")) {
                                    guildMembers.add(formatUUID(m.get("uuid").getAsString()));
                                }
                            }
                            
                            // Save sync time and guild info
                            try (FileWriter writer = new FileWriter(lastFetchFile)) { writer.write(String.valueOf(now)); }
                            try (FileWriter writer = new FileWriter(guildInfoFile)) { writer.write(currentGuildId); }
                            
                            System.out.println("[Foxtrot] Smart Sync: Updated " + guildMembers.size() + " members for guild: " + currentGuildId);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
