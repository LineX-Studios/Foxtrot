package com.linexstudios.foxtrot.Handler;

import com.linexstudios.foxtrot.Hud.*;
import com.linexstudios.foxtrot.Misc.*;
import com.linexstudios.foxtrot.Render.*;
import com.linexstudios.foxtrot.Denick.*;
import com.linexstudios.foxtrot.Combat.*;
import com.linexstudios.foxtrot.Util.*;
import com.linexstudios.foxtrot.Enemy.EnemyManager;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.util.*;

public class ConfigHandler {
    public static boolean telemetryEnabled = true, autoUpdateEnabled = true, globalDebug = false, showFoxtrotCapes = true, discordRpcEnabled = true;
    public static String selectedCape = "fx_original";
    private static final File configDir = new File("config/Foxtrot"), enemyFile = new File(configDir, "enemies.txt"), friendsFile = new File(configDir, "friends.txt"), settingsFile = new File(configDir, "settings.txt");
    private static final File updaterDir = new File(Minecraft.getMinecraft().mcDataDir, "Foxtrot_Updates"), updaterSettingsFile = new File(updaterDir, "update_settings.txt");
    
    private static int getInt(Properties p, String k, int d) { try { return (int)Float.parseFloat(p.getProperty(k, String.valueOf(d))); } catch(Exception e) { return d; } }
    private static double getDouble(Properties p, String k, double d) { try { return Double.parseDouble(p.getProperty(k, String.valueOf(d))); } catch(Exception e) { return d; } }
    private static float getFloat(Properties p, String k, float d) { try { return Float.parseFloat(p.getProperty(k, String.valueOf(d))); } catch(Exception e) { return d; } }
    private static boolean getBool(Properties p, String k, boolean d) { try { return Boolean.parseBoolean(p.getProperty(k, String.valueOf(d))); } catch(Exception e) { return d; } }
    private static void initHUDs() { Object[] f = {PotionHUD.instance, ArmorHUD.instance, CoordsHUD.instance, EnemyHUD.instance, NickedHUD.instance, FriendsHUD.instance, SessionStatsHUD.instance, EventHUD.instance, RegHUD.instance, DarksHUD.instance, ToggleSprintModule.instance, CPSModule.instance, FPSModule.instance, BossBarModule.instance, TelebowHUD.instance, PlayerCounterHUD.instance, VenomTimer.instance}; }

    private static void applyFirstRunDefaults() {
        ToggleSprintModule.instance.enabled = false; AutoClicker.enabled = false; Wtap.enabled = false; ChestStealer.enabled = false; AutoDenick.enabled = true; NickedRender.enabled = true; PitESP.espChests = false; PitESP.espDragonEggs = false; PitESP.espRaffleTickets = false; PitESP.espMystics = false; LowLifeMystic.enabled = true; AutoPantSwap.pantSwapEnabled = false; AutoPantSwap.venomSwapEnabled = false; AutoPantSwap.autoPodEnabled = false; AutoGhead.enabled = false; AutoQuickMath.enabled = false; AutoBulletTime.enabled = true; EnemyHUD.enabled = true; EnemyHUD.notificationsEnabled = true; NickedHUD.enabled = true; SessionStatsHUD.enabled = false; EventHUD.enabled = false; RegHUD.enabled = true; DarksHUD.enabled = true; PotionHUD.enabled = false; ArmorHUD.enabled = false; CoordsHUD.enabled = false; BossBarModule.enabled = false; CPSModule.enabled = false; FPSModule.enabled = false; TelebowHUD.enabled = true; PlayerCounterHUD.enabled = false; NameTags.enabled = false; FriendsHUD.enabled = true; FriendsESP.enabled = true; Ranks.isEnabled = false; RingHelper.enabled = false; DeadLobbyFinder.enabled = false; NonHighlighter.enabled = false; VenomTimer.enabled = true; EnchantNames.enabled = false; showFoxtrotCapes = true; discordRpcEnabled = true;
    }

    public static void loadConfig() {
        initHUDs(); EnemyManager.loadCache();
        FriendsManager.load(); TeammateManager.load();
        try {
            if (!configDir.exists()) configDir.mkdirs();
            if (enemyFile.exists()) { BufferedReader r = new BufferedReader(new FileReader(enemyFile)); String l; EnemyHUD.targetList.clear(); while ((l=r.readLine())!=null) if (!l.trim().isEmpty()) EnemyHUD.targetList.add(l.trim()); r.close(); }
            if (friendsFile.exists()) { BufferedReader r = new BufferedReader(new FileReader(friendsFile)); String l; FriendsHUD.friendsList.clear(); while ((l=r.readLine())!=null) if (!l.trim().isEmpty()) FriendsHUD.friendsList.add(l.trim()); r.close(); }
            if (settingsFile.exists()) {
                Properties p = new Properties(); try (FileInputStream i = new FileInputStream(settingsFile)) { p.load(i); }
                for (DraggableHUD h : DraggableHUD.getRegistry()) { 
                    String n = h.name.replaceAll("\\s+", ""); 
                    h.relativeX = getDouble(p, n + "RelX", -1.0); 
                    h.relativeY = getDouble(p, n + "RelY", -1.0); 
                    
                    // LEGACY MIGRATION: If no relative pos exists, load the old pixel pos
                    if (h.relativeX == -1.0) {
                        h.x = getInt(p, n + "X", h.x);
                        h.y = getInt(p, n + "Y", h.y);
                    }
                    
                    h.scale = getFloat(p, n + "Scale", h.scale);
                }
                PotionHUD.instance.isHorizontal = getBool(p, "potionHorizontal", false); PotionHUD.nameColor = getInt(p, "potionNameColor", 16777215); PotionHUD.durationColor = getInt(p, "potionDurationColor", 11184810); ArmorHUD.durabilityColor = getInt(p, "armorDurabilityColor", 16777215); ArmorHUD.instance.isHorizontal = getBool(p, "armorHorizontal", false); CoordsHUD.instance.isHorizontal = getBool(p, "coordsHorizontal", false); CoordsHUD.axisColor = getInt(p, "coordsAxisColor", 16733525); CoordsHUD.numberColor = getInt(p, "coordsNumberColor", 16777215);
                ToggleSprintModule.instance.enabled = getBool(p, "toggleSprintEnabled", false); ToggleSprintModule.instance.toggleSprint = getBool(p, "tsSprint", true); ToggleSprintModule.instance.toggleSneak = getBool(p, "tsSneak", false); ToggleSprintModule.instance.wTapFix = getBool(p, "tsWTapFix", true); ToggleSprintModule.instance.flyBoost = getBool(p, "tsFlyBoost", true); ToggleSprintModule.instance.flyBoostAmount = getFloat(p, "tsFlyBoostAmount", 4.0f); ToggleSprintModule.instance.textColor = getInt(p, "tsTextColor", 16777215);
                CPSModule.showBackground = getBool(p, "cpsShowBg", true); CPSModule.textColor = getInt(p, "cpsTextColor", 16777215); FPSModule.showBackground = getBool(p, "fpsShowBg", true); FPSModule.textColor = getInt(p, "fpsTextColor", 16777215);
                EditHUDGui.collapsedX = getInt(p, "panelX", -1); EditHUDGui.collapsedY = getInt(p, "panelY", -1); EditHUDGui.panelCollapsed = getBool(p, "panelCollapsed", false);
                AutoClicker.enabled = getBool(p, "clickerEnabled", false); AutoClicker.leftClick = getBool(p, "clickerLeft", true); AutoClicker.fastPlaceEnabled = getBool(p, "fastPlace", false); AutoClicker.holdToClick = getBool(p, "clickerHoldToClick", true); AutoClicker.inventoryFill = getBool(p, "clickerInvFill", true); AutoClicker.breakBlocks = getBool(p, "clickerBreakBlocks", true); AutoClicker.limitItems = getBool(p, "clickerLimitItems", true); AutoClicker.inventoryFillCps = getFloat(p, "clickerInvFillCps", 15.0f); AutoClicker.minCps = getFloat(p, "clickerMinCps", 9.0f); AutoClicker.maxCps = getFloat(p, "clickerMaxCps", 13.0f); AutoClicker.randomMode = getInt(p, "clickerRandomMode", 1); AutoClicker.itemWhitelist = new ArrayList<>(Arrays.asList(p.getProperty("clickerWhitelist", "sword,axe,pickaxe").split(",")));
                Wtap.enabled = getBool(p, "wtapEnabled", false); Wtap.delay = getFloat(p, "wtapDelay", 5.5f); Wtap.duration = getFloat(p, "wtapDuration", 1.5f);
                ChestStealer.enabled = getBool(p, "csEnabled", false); ChestStealer.minDelay = getFloat(p, "csMinDelay", 1.0f); ChestStealer.maxDelay = getFloat(p, "csMaxDelay", 2.0f); ChestStealer.openDelay = getFloat(p, "csOpenDelay", 1.0f); ChestStealer.autoClose = getBool(p, "csAutoClose", false); ChestStealer.nameCheck = getBool(p, "csNameCheck", true); ChestStealer.skipTrash = getBool(p, "csSkipTrash", true); ChestStealer.moreArmor = getBool(p, "csMoreArmor", false); ChestStealer.moreSword = getBool(p, "csMoreSword", false);
                AutoDenick.enabled = getBool(p, "autoDenick", false); NickedRender.enabled = getBool(p, "nickedNametags", false); PitESP.espChests = getBool(p, "pitEspChests", false); PitESP.espDragonEggs = getBool(p, "pitEspDragonEggs", false); PitESP.espRaffleTickets = getBool(p, "pitEspRaffleTickets", false); PitESP.espMystics = getBool(p, "pitEspMystics", false); LowLifeMystic.enabled = getBool(p, "lowLifeMysticEnabled", false);
                AutoPantSwap.pantSwapEnabled = getBool(p, "autoPantSwap", false); AutoPantSwap.venomSwapEnabled = getBool(p, "autoVenomSwap", false); AutoPantSwap.autoPodEnabled = getBool(p, "autoPod", false); AutoGhead.enabled = getBool(p, "autoGhead", false); AutoQuickMath.enabled = getBool(p, "autoQuickMath", false); AutoBulletTime.enabled = getBool(p, "autoBulletTime", false); AutoQuickMath.randomMode = getInt(p, "aqmRandomMode", 1); AutoQuickMath.baseDelayMs = getFloat(p, "aqmBaseDelay", 1500f);
                EnemyHUD.enabled = getBool(p, "enemyHudEnabled", false); EnemyHUD.notificationsEnabled = getBool(p, "enemyHudAlerts", false); globalDebug = getBool(p, "globalDebug", false); NickedHUD.enabled = getBool(p, "nickedHudEnabled", false); SessionStatsHUD.enabled = getBool(p, "sessionStatsEnabled", false); EventHUD.enabled = getBool(p, "eventHudEnabled", false); RegHUD.enabled = getBool(p, "regHudEnabled", false); DarksHUD.enabled = getBool(p, "darksHudEnabled", false); PotionHUD.enabled = getBool(p, "potionHudEnabled", false); ArmorHUD.enabled = getBool(p, "armorHudEnabled", false); CoordsHUD.enabled = getBool(p, "coordsHudEnabled", false); BossBarModule.enabled = getBool(p, "bossBarEnabled", false); CPSModule.enabled = getBool(p, "cpsEnabled", false); FPSModule.enabled = getBool(p, "fpsEnabled", false); TelebowHUD.enabled = getBool(p, "telebowHudEnabled", false); PlayerCounterHUD.enabled = getBool(p, "playerCounterEnabled", false); PlayerCounterHUD.prefixColor = getInt(p, "playerCounterPrefixColor", 0xFFFFFF); PlayerCounterHUD.countColor = getInt(p, "playerCounterCountColor", 0xAAAAAA);
                NameTags.enabled = getBool(p, "nameTagsEnabled", false); NameTags.showHealth = getBool(p, "nameTagsShowHealth", false); NameTags.showItems = getBool(p, "nameTagsShowItems", false); FriendsHUD.enabled = getBool(p, "friendsHudEnabled", false); FriendsESP.enabled = getBool(p, "friendsEspEnabled", false); TeammateESP.enabled = getBool(p, "teammateEspEnabled", true);
                Ranks.isEnabled = getBool(p, "ranksEnabled", false); Ranks.changeLevel = getBool(p, "ranksChangeLevel", false); Ranks.targetLevel = getInt(p, "ranksTargetLevel", 120); Ranks.changePrestige = getBool(p, "ranksChangePrestige", false); Ranks.targetPrestige = getInt(p, "ranksTargetPrestige", 30); Ranks.changeRank = getBool(p, "ranksChangeRank", false); Ranks.targetRank = p.getProperty("ranksTargetRank", "admin"); Ranks.changeName = getBool(p, "ranksChangeName", false); Ranks.targetName = p.getProperty("ranksTargetName", "");
                RingHelper.enabled = getBool(p, "ringHelperEnabled", false); DeadLobbyFinder.enabled = getBool(p, "deadLobbyEnabled", false); DeadLobbyFinder.targetPlayers = getInt(p, "deadLobbyTarget", 10); DeadLobbyFinder.warpDelayMs = getInt(p, "deadLobbyDelay", 4500); NonHighlighter.enabled = getBool(p, "nonHighlighterEnabled", false); VenomTimer.enabled = getBool(p, "venomTimerEnabled", false); EnchantNames.enabled = getBool(p, "enchantNamesEnabled", false);
                telemetryEnabled = getBool(p, "telemetryEnabled", true); TelemetryManager.anonymousClientId = p.getProperty("telemetryId", ""); autoUpdateEnabled = getBool(p, "autoUpdateEnabled", true); showFoxtrotCapes = getBool(p, "showFoxtrotCapes", true); selectedCape = p.getProperty("selectedCape", "fx_original"); discordRpcEnabled = getBool(p, "discordRpcEnabled", true);
            } else { applyFirstRunDefaults(); saveConfig(); }
            EnemyManager.fetchMissingUUIDs(); FriendsManager.fetchMissingUUIDs();
            if (Minecraft.getMinecraft().thePlayer != null) TeammateManager.updateGuild(Minecraft.getMinecraft().thePlayer.getName(), true);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void saveConfig() {
        initHUDs(); EnemyManager.saveCache(); FriendsManager.save(); TeammateManager.save();
        try {
            if (!configDir.exists()) configDir.mkdirs();
            try(PrintWriter w = new PrintWriter(new FileWriter(enemyFile))){ for(String n : EnemyHUD.targetList) w.println(n); }
            try(PrintWriter w = new PrintWriter(new FileWriter(friendsFile))){ for(String n : FriendsHUD.friendsList) w.println(n); }
            Properties p = new Properties();
            for (DraggableHUD h : DraggableHUD.getRegistry()) { 
                String n = h.name.replaceAll("\\s+", ""); 
                h.saveRelativePos();
                p.setProperty(n + "RelX", String.valueOf(h.relativeX)); 
                p.setProperty(n + "RelY", String.valueOf(h.relativeY)); 
                p.setProperty(n + "Scale", String.valueOf(h.scale)); 
            }
            p.setProperty("potionHorizontal", String.valueOf(PotionHUD.instance.isHorizontal)); p.setProperty("potionNameColor", String.valueOf(PotionHUD.nameColor)); p.setProperty("potionDurationColor", String.valueOf(PotionHUD.durationColor)); p.setProperty("armorDurabilityColor", String.valueOf(ArmorHUD.durabilityColor)); p.setProperty("armorHorizontal", String.valueOf(ArmorHUD.instance.isHorizontal)); p.setProperty("coordsHorizontal", String.valueOf(CoordsHUD.instance.isHorizontal)); p.setProperty("coordsAxisColor", String.valueOf(CoordsHUD.axisColor)); p.setProperty("coordsNumberColor", String.valueOf(CoordsHUD.numberColor));
            p.setProperty("toggleSprintEnabled", String.valueOf(ToggleSprintModule.instance.enabled)); p.setProperty("tsSprint", String.valueOf(ToggleSprintModule.instance.toggleSprint)); p.setProperty("tsSneak", String.valueOf(ToggleSprintModule.instance.toggleSneak)); p.setProperty("tsWTapFix", String.valueOf(ToggleSprintModule.instance.wTapFix)); p.setProperty("tsFlyBoost", String.valueOf(ToggleSprintModule.instance.flyBoost)); p.setProperty("tsFlyBoostAmount", String.valueOf(ToggleSprintModule.instance.flyBoostAmount)); p.setProperty("tsTextColor", String.valueOf(ToggleSprintModule.instance.textColor));
            p.setProperty("cpsShowBg", String.valueOf(CPSModule.showBackground)); p.setProperty("cpsTextColor", String.valueOf(CPSModule.textColor)); p.setProperty("fpsShowBg", String.valueOf(FPSModule.showBackground)); p.setProperty("fpsTextColor", String.valueOf(FPSModule.textColor));
            p.setProperty("panelX", String.valueOf(EditHUDGui.collapsedX)); p.setProperty("panelY", String.valueOf(EditHUDGui.collapsedY)); p.setProperty("panelCollapsed", String.valueOf(EditHUDGui.panelCollapsed));
            p.setProperty("clickerEnabled", String.valueOf(AutoClicker.enabled)); p.setProperty("clickerLeft", String.valueOf(AutoClicker.leftClick)); p.setProperty("fastPlace", String.valueOf(AutoClicker.fastPlaceEnabled)); p.setProperty("clickerHoldToClick", String.valueOf(AutoClicker.holdToClick)); p.setProperty("clickerInvFill", String.valueOf(AutoClicker.inventoryFill)); p.setProperty("clickerBreakBlocks", String.valueOf(AutoClicker.breakBlocks)); p.setProperty("clickerLimitItems", String.valueOf(AutoClicker.limitItems)); p.setProperty("clickerInvFillCps", String.valueOf(AutoClicker.inventoryFillCps)); p.setProperty("clickerMinCps", String.valueOf(AutoClicker.minCps)); p.setProperty("clickerMaxCps", String.valueOf(AutoClicker.maxCps)); p.setProperty("clickerRandomMode", String.valueOf(AutoClicker.randomMode)); p.setProperty("clickerWhitelist", String.join(",", AutoClicker.itemWhitelist));
            p.setProperty("wtapEnabled", String.valueOf(Wtap.enabled)); p.setProperty("wtapDelay", String.valueOf(Wtap.delay)); p.setProperty("wtapDuration", String.valueOf(Wtap.duration));
            p.setProperty("csEnabled", String.valueOf(ChestStealer.enabled)); p.setProperty("csMinDelay", String.valueOf(ChestStealer.minDelay)); p.setProperty("csMaxDelay", String.valueOf(ChestStealer.maxDelay)); p.setProperty("csOpenDelay", String.valueOf(ChestStealer.openDelay)); p.setProperty("csAutoClose", String.valueOf(ChestStealer.autoClose)); p.setProperty("csNameCheck", String.valueOf(ChestStealer.nameCheck)); p.setProperty("csSkipTrash", String.valueOf(ChestStealer.skipTrash)); p.setProperty("csMoreArmor", String.valueOf(ChestStealer.moreArmor)); p.setProperty("csMoreSword", String.valueOf(ChestStealer.moreSword));
            p.setProperty("autoDenick", String.valueOf(AutoDenick.enabled)); p.setProperty("nickedNametags", String.valueOf(NickedRender.enabled)); p.setProperty("pitEspChests", String.valueOf(PitESP.espChests)); p.setProperty("pitEspDragonEggs", String.valueOf(PitESP.espDragonEggs)); p.setProperty("pitEspRaffleTickets", String.valueOf(PitESP.espRaffleTickets)); p.setProperty("pitEspMystics", String.valueOf(PitESP.espMystics)); p.setProperty("lowLifeMysticEnabled", String.valueOf(LowLifeMystic.enabled));
            p.setProperty("autoPantSwap", String.valueOf(AutoPantSwap.pantSwapEnabled)); p.setProperty("autoVenomSwap", String.valueOf(AutoPantSwap.venomSwapEnabled)); p.setProperty("autoPod", String.valueOf(AutoPantSwap.autoPodEnabled)); p.setProperty("autoGhead", String.valueOf(AutoGhead.enabled)); p.setProperty("autoQuickMath", String.valueOf(AutoQuickMath.enabled)); p.setProperty("autoBulletTime", String.valueOf(AutoBulletTime.enabled)); p.setProperty("aqmRandomMode", String.valueOf(AutoQuickMath.randomMode)); p.setProperty("aqmBaseDelay", String.valueOf(AutoQuickMath.baseDelayMs));
            p.setProperty("enemyHudEnabled", String.valueOf(EnemyHUD.enabled)); p.setProperty("enemyHudAlerts", String.valueOf(EnemyHUD.notificationsEnabled)); p.setProperty("globalDebug", String.valueOf(globalDebug)); p.setProperty("nickedHudEnabled", String.valueOf(NickedHUD.enabled)); p.setProperty("sessionStatsEnabled", String.valueOf(SessionStatsHUD.enabled)); p.setProperty("eventHudEnabled", String.valueOf(EventHUD.enabled)); p.setProperty("regHudEnabled", String.valueOf(RegHUD.enabled)); p.setProperty("darksHudEnabled", String.valueOf(DarksHUD.enabled)); p.setProperty("potionHudEnabled", String.valueOf(PotionHUD.enabled)); p.setProperty("armorHudEnabled", String.valueOf(ArmorHUD.enabled)); p.setProperty("coordsHudEnabled", String.valueOf(CoordsHUD.enabled)); p.setProperty("bossBarEnabled", String.valueOf(BossBarModule.enabled)); p.setProperty("cpsEnabled", String.valueOf(CPSModule.enabled)); p.setProperty("fpsEnabled", String.valueOf(FPSModule.enabled)); p.setProperty("telebowHudEnabled", String.valueOf(TelebowHUD.enabled)); p.setProperty("playerCounterEnabled", String.valueOf(PlayerCounterHUD.enabled)); p.setProperty("playerCounterPrefixColor", String.valueOf(PlayerCounterHUD.prefixColor)); p.setProperty("playerCounterCountColor", String.valueOf(PlayerCounterHUD.countColor));
            p.setProperty("nameTagsEnabled", String.valueOf(NameTags.enabled)); p.setProperty("nameTagsShowHealth", String.valueOf(NameTags.showHealth)); p.setProperty("nameTagsShowItems", String.valueOf(NameTags.showItems)); p.setProperty("friendsHudEnabled", String.valueOf(FriendsHUD.enabled)); p.setProperty("friendsEspEnabled", String.valueOf(FriendsESP.enabled)); p.setProperty("teammateEspEnabled", String.valueOf(TeammateESP.enabled)); p.setProperty("ranksEnabled", String.valueOf(Ranks.isEnabled)); p.setProperty("ranksChangeLevel", String.valueOf(Ranks.changeLevel)); p.setProperty("ranksTargetLevel", String.valueOf(Ranks.targetLevel)); p.setProperty("ranksChangePrestige", String.valueOf(Ranks.changePrestige)); p.setProperty("ranksTargetPrestige", String.valueOf(Ranks.targetPrestige)); p.setProperty("ranksChangeRank", String.valueOf(Ranks.changeRank)); p.setProperty("ranksTargetRank", Ranks.targetRank); p.setProperty("ranksChangeName", String.valueOf(Ranks.changeName)); p.setProperty("ranksTargetName", Ranks.targetName != null ? Ranks.targetName : ""); p.setProperty("ringHelperEnabled", String.valueOf(RingHelper.enabled)); p.setProperty("deadLobbyEnabled", String.valueOf(DeadLobbyFinder.enabled)); p.setProperty("deadLobbyTarget", String.valueOf(DeadLobbyFinder.targetPlayers)); p.setProperty("deadLobbyDelay", String.valueOf(DeadLobbyFinder.warpDelayMs)); p.setProperty("nonHighlighterEnabled", String.valueOf(NonHighlighter.enabled)); p.setProperty("venomTimerEnabled", String.valueOf(VenomTimer.enabled)); p.setProperty("enchantNamesEnabled", String.valueOf(EnchantNames.enabled)); p.setProperty("telemetryEnabled", String.valueOf(telemetryEnabled)); if (TelemetryManager.anonymousClientId != null && !TelemetryManager.anonymousClientId.isEmpty()) p.setProperty("telemetryId", TelemetryManager.anonymousClientId); p.setProperty("autoUpdateEnabled", String.valueOf(autoUpdateEnabled)); p.setProperty("showFoxtrotCapes", String.valueOf(showFoxtrotCapes)); p.setProperty("selectedCape", selectedCape); p.setProperty("discordRpcEnabled", String.valueOf(discordRpcEnabled));

            try (FileOutputStream o = new FileOutputStream(settingsFile)) { p.store(o, "Foxtrot Settings"); }
            
            try {
                if (!updaterDir.exists()) updaterDir.mkdirs();
                try (PrintWriter pw = new PrintWriter(new FileWriter(updaterSettingsFile))) {
                    pw.println("autoUpdateEnabled=" + autoUpdateEnabled);
                }
            } catch(Exception ignored) {}
            
        } catch (Exception e) { e.printStackTrace(); }
    }
    public static void logDebug(String m) { if (globalDebug) System.out.println("[Foxtrot-Debug] " + m); }
}