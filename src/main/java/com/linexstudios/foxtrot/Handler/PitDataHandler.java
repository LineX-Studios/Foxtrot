package com.linexstudios.foxtrot.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import java.nio.charset.StandardCharsets;

public class PitDataHandler {

    private static JsonObject data;
    private static final Map<Integer, PrestigeData> PRESTIGES = new HashMap<>();
    private static final Map<String, MysticData> MYSTICS = new HashMap<>();
    private static final List<LevelData> LEVELS = new ArrayList<>();
    private static final Map<String, String> RANK_PREFIXES = new HashMap<>();

    public static void init() {
        File configFolder = new File(Minecraft.getMinecraft().mcDataDir, "config/Foxtrot");
        if (!configFolder.exists()) configFolder.mkdirs();
        
        File configFile = new File(configFolder, "pitMaster.json");
        
        // 1. TRY TO LOAD FROM CONFIG FOLDER
        if (configFile.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new java.io.FileInputStream(configFile), StandardCharsets.UTF_8)) {
                data = new JsonParser().parse(reader).getAsJsonObject();
                System.out.println("[Foxtrot] Loaded pitMaster.json from config (UTF-8).");
            } catch (Exception e) {
                System.err.println("[Foxtrot] Failed to read pitMaster.json from config, trying resources...");
            }
        }

        // 2. FALLBACK TO RESOURCES (AND EXTRACT)
        if (data == null) {
            try (InputStream is = PitDataHandler.class.getResourceAsStream("/pitMaster.json")) {
                if (is != null) {
                    try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        data = new JsonParser().parse(isr).getAsJsonObject();
                        System.out.println("[Foxtrot] Loaded pitMaster.json from internal resources (UTF-8).");
                        
                        // Extract it so the user can see/edit it
                        Files.copy(PitDataHandler.class.getResourceAsStream("/pitMaster.json"), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("[Foxtrot] Extracted internal pitMaster.json to config folder.");
                    }
                } else {
                    // Final fallback: Check development path
                    File devFile = new File("w:/FOXTROT-ALL BRANCH/pitMaster.json");
                    if (devFile.exists()) {
                        try (InputStreamReader fr = new InputStreamReader(new java.io.FileInputStream(devFile), StandardCharsets.UTF_8)) {
                            data = new JsonParser().parse(fr).getAsJsonObject();
                            System.out.println("[Foxtrot] Loaded pitMaster.json from dev path (UTF-8).");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[Foxtrot] Critical failure loading pitMaster.json!");
                e.printStackTrace();
            }
        }

        if (data != null) {
            loadPrestiges();
            loadMystics();
            loadLevels();
            loadExtra();
        } else {
            System.err.println("[Foxtrot] No pitMaster.json found anywhere! Legacy data will be used.");
        }
    }

    private static void loadPrestiges() {
        if (data == null || !data.has("Pit") || !data.getAsJsonObject("Pit").has("Prestiges")) return;
        List<PrestigeData> list = new Gson().fromJson(data.getAsJsonObject("Pit").get("Prestiges"), new TypeToken<List<PrestigeData>>(){}.getType());
        PRESTIGES.clear();
        for (int i = 0; i < list.size(); i++) {
            PRESTIGES.put(i, list.get(i));
        }
    }

    private static void loadMystics() {
        if (data == null || !data.has("Pit") || !data.getAsJsonObject("Pit").has("Mystics")) return;
        Map<String, MysticData> map = new Gson().fromJson(data.getAsJsonObject("Pit").get("Mystics"), new TypeToken<Map<String, MysticData>>(){}.getType());
        MYSTICS.clear();
        for (Map.Entry<String, MysticData> entry : map.entrySet()) {
            MysticData md = entry.getValue();
            md.Name = sanitize(md.Name);
            if (md.Descriptions != null) {
                for (List<String> tier : md.Descriptions) {
                    for (int i = 0; i < tier.size(); i++) {
                        tier.set(i, sanitize(tier.get(i)));
                    }
                }
            }
            MYSTICS.put(entry.getKey().toLowerCase(), md);
        }
    }

    private static void loadLevels() {
        if (data == null || !data.has("Pit") || !data.getAsJsonObject("Pit").has("Levels")) return;
        List<LevelData> list = new Gson().fromJson(data.getAsJsonObject("Pit").get("Levels"), new TypeToken<List<LevelData>>(){}.getType());
        LEVELS.clear();
        for (LevelData ld : list) {
            ld.ColorCode = sanitize(ld.ColorCode);
            LEVELS.add(ld);
        }
    }

    private static void loadExtra() {
        if (data == null || !data.has("Extra") || !data.getAsJsonObject("Extra").has("RankPrefixes")) return;
        Map<String, String> map = new Gson().fromJson(data.getAsJsonObject("Extra").get("RankPrefixes"), new TypeToken<Map<String, String>>(){}.getType());
        RANK_PREFIXES.clear();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            RANK_PREFIXES.put(entry.getKey(), sanitize(entry.getValue()));
        }
    }

    /**
     * Sanitizes strings from JSON to prevent rendering issues like "smile faces".
     * Replaces standard emojis with readable symbols or strips them.
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        
        // 1. Convert alternate color codes
        String output = input.replace("&", "\u00a7");
        
        // 2. Fix heart emojis (commonly cause smile faces in 1.8.9)
        // \u2764 is the heart symbol. Minecraft 1.8.9 often fails on it.
        output = output.replace("\u2764", "\u00a7c\u2764"); // Force red color for hearts
        
        // 3. Remove non-printable/control characters that aren't \u00a7
        StringBuilder sb = new StringBuilder();
        for (char c : output.toCharArray()) {
            if (c == '\u00a7' || (c >= 32 && c < 127) || (c >= 160 && c <= 255) || c == '\u2764' || c == '\u27a1') {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }

    public static PrestigeData getPrestige(int index) {
        return PRESTIGES.get(index);
    }

    public static MysticData getMystic(String key) {
        if (key == null) return null;
        return MYSTICS.get(key.toLowerCase());
    }

    public static LevelData getLevel(int level) {
        if (LEVELS.isEmpty()) return null;
        // In Pit, colors change every 10 levels. 
        // 1-9 = Gray, 10-19 = Blue, etc.
        int idx = level / 10; 
        if (idx < 0) idx = 0;
        if (idx >= LEVELS.size()) idx = LEVELS.size() - 1;
        return LEVELS.get(idx);
    }

    public static String getRankPrefix(String rank) {
        String key = rank.toUpperCase().replace("+", "_PLUS");
        return RANK_PREFIXES.getOrDefault(key, "");
    }

    public static class PrestigeData {
        public double Multiplier;
        public int TotalXp;
        public int SumXp;
        public int GoldReq;
        public int Renown;
        public String Color;
        public String ColorCode;
    }

    public static class MysticData {
        public String Name;
        public String Type;
        public List<List<String>> Descriptions;
        public List<String> Classes;
    }

    public static class LevelData {
        public int Xp;
        public String Color;
        public String ColorCode;
    }

    // Legacy support methods for other classes
    public static String getFormattedEnchantName(String key, int level) {
        MysticData mystic = getMystic(key);
        if (mystic == null) return null;
        
        String name = mystic.Name;
        // The JSON name often includes prefixes like "§dRARE! §9"
        // We might want to clean it or keep it.
        return name + " " + toRoman(level);
    }

    private static String toRoman(int num) {
        String[] roman = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };
        return (num > 0 && num <= 10) ? roman[num - 1] : String.valueOf(num);
    }
}