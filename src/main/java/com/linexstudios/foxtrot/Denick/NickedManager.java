package com.linexstudios.foxtrot.Denick;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NickedManager {
    public static final NickedManager instance = new NickedManager();

    private static final Map<String, String> resolvedNicks = new ConcurrentHashMap<>();

    public static void addNicked(String nick, String realName) {
        if (nick == null || realName == null) return;
        resolvedNicks.put(nick.toLowerCase(), realName);
    }

    public static void updateNicked(String nick, String realName) {
        if (nick == null || realName == null) return;
        resolvedNicks.put(nick.toLowerCase(), realName);
    }

    public static String getResolvedIGN(String nick) {
        if (nick == null) return null;
        return resolvedNicks.get(nick.toLowerCase());
    }

    public static boolean isResolved(String nick) {
        return resolvedNicks.containsKey(nick.toLowerCase());
    }

    public static Map<String, String> getAllNicks() {
        return resolvedNicks;
    }

    public static void clear() {
        resolvedNicks.clear();
    }

    @SubscribeEvent
    public void onNameFormat(PlayerEvent.NameFormat event) {
        String username = event.username;
        String display = event.displayname;

        // 1. Friends Check
        if (com.linexstudios.foxtrot.Hud.FriendsHUD.isFriend(username)) {
            display = EnumChatFormatting.DARK_GREEN + "[" + EnumChatFormatting.GREEN + "F" + EnumChatFormatting.DARK_GREEN + "] " + EnumChatFormatting.RESET + display;
        } 
        // 2. Enemy Check
        else if (com.linexstudios.foxtrot.Hud.EnemyHUD.isTarget(username)) {
            display = EnumChatFormatting.DARK_RED + "[" + EnumChatFormatting.RED + "E" + EnumChatFormatting.DARK_RED + "] " + EnumChatFormatting.RESET + display;
        }

        // 3. Nicked Check
        if (com.linexstudios.foxtrot.Denick.AutoDenick.isNicked(event.entityPlayer.getUniqueID())) {
            
            if (!display.contains("[N]")) {
                display = EnumChatFormatting.DARK_BLUE + "[" + EnumChatFormatting.BLUE + "N" + EnumChatFormatting.DARK_BLUE + "] " + EnumChatFormatting.RESET + display;
            }

            String realName = CacheManager.getFromCache(username);
            if (realName == null) {
                realName = getResolvedIGN(username); 
            }
            
            // Apply the same fix: Strip color codes before checking
            if (realName != null) {
                String cleanName = EnumChatFormatting.getTextWithoutFormattingCodes(realName).trim();
                
                if (!cleanName.equalsIgnoreCase("Scraping") && 
                    !cleanName.equalsIgnoreCase("Scraping...") && 
                    !cleanName.equalsIgnoreCase("Failed") && 
                    !cleanName.equalsIgnoreCase("No Nonce")) {
                    
                    if (!display.contains("(" + realName + ")")) {
                        display = display + " " + EnumChatFormatting.YELLOW + "(" + realName + ")";
                    }
                }
            }
        }

        event.displayname = display;
    }
}