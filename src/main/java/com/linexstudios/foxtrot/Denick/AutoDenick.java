package com.linexstudios.foxtrot.Denick;

import com.linexstudios.foxtrot.Hud.EnemyHUD;
import com.linexstudios.foxtrot.Hud.NickedHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutoDenick {
    public static final AutoDenick instance = new AutoDenick();
    
    private static final String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] ";
    
    private static final Set<String> resolvingNicks = ConcurrentHashMap.newKeySet();
    private static final Set<String> notifiedScraping = new HashSet<>(); 
    private static final Map<String, Long> retryCooldowns = new ConcurrentHashMap<>(); 
    
    public static Minecraft mc = Minecraft.getMinecraft();
    public static Set<String> lastNickedSet = new HashSet<>();
    public static boolean enabled = true; 
    
    private int tickTimer = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled || mc.theWorld == null || mc.getNetHandler() == null || event.phase != TickEvent.Phase.END) return;

        tickTimer++;
        if (tickTimer >= 40) { // Runs every 2 seconds
            tickTimer = 0;
            detectIfPlayerIsNicked();
        }
    }

    public static boolean isNicked(UUID playerUUID) {
        return playerUUID.version() == 1;
    }

    public static void detectIfPlayerIsNicked() {
        Set<String> currentNickedSet = new HashSet<>();
        
        for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
            if (info == null || info.getGameProfile() == null || info.getGameProfile().getId() == null) continue;
            
            UUID playerUUID = info.getGameProfile().getId();
            if (isNicked(playerUUID)) {
                String nick = info.getGameProfile().getName();
                currentNickedSet.add(nick);
                
                EntityPlayer p = mc.theWorld.getPlayerEntityByName(nick);
                if (p == null) continue; // Waiting for them to render physically
                
                String currentStatus = NickedManager.getResolvedIGN(nick);
                boolean needsDenick = currentStatus == null || currentStatus.equals("Failed") || currentStatus.equals("No Nonce") || currentStatus.equals("Scraping");
                
                if (needsDenick) {
                    
                    if (resolvingNicks.contains(nick)) {
                        long lastAttempt = retryCooldowns.getOrDefault(nick, 0L);
                        if (System.currentTimeMillis() - lastAttempt > 15000) {
                             resolvingNicks.remove(nick); 
                        } else {
                             continue;
                        }
                    }

                    long lastAttempt = retryCooldowns.getOrDefault(nick, 0L);
                    
                    if (System.currentTimeMillis() - lastAttempt >= 20000) {
                        
                        try {
                            ItemStack pants = p.inventory.armorInventory != null ? p.inventory.armorInventory[1] : null;

                            // 1. INSTANT NBT CHECK
                            String nbtName = getRealNameFromNBT(pants);
                            if (nbtName != null && !nbtName.isEmpty() && isValidMinecraftName(nbtName)) {
                                CacheManager.addToCache(nick, nbtName);
                                NickedManager.updateNicked(nick, nbtName);
                                sendChatMsg(prefix + EnumChatFormatting.GREEN + "Denicked " + EnumChatFormatting.GRAY + "\u27A1 " + EnumChatFormatting.DARK_AQUA + "[" + EnumChatFormatting.AQUA + "N" + EnumChatFormatting.DARK_AQUA + "] " + EnumChatFormatting.AQUA + nick + " " + EnumChatFormatting.GRAY + "\u2192 " + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + nbtName + " " + EnumChatFormatting.GRAY + "(NBT Data)");
                                retryCooldowns.put(nick, System.currentTimeMillis() + 60000); 
                                continue;
                            }

                            // --- SMART INVENTORY SCANNER ---
                            int foundNonce = -1;
                            List<ItemStack> itemsToCheck = new ArrayList<>();
                            
                            if (p.getHeldItem() != null) itemsToCheck.add(p.getHeldItem());
                            
                            if (p.inventory.armorInventory != null) {
                                for (ItemStack armorItem : p.inventory.armorInventory) {
                                    if (armorItem != null) itemsToCheck.add(armorItem);
                                }
                            }

                            for (ItemStack item : itemsToCheck) {
                                if (item != null && item.hasTagCompound() && item.getTagCompound().hasKey("ExtraAttributes", 10)) {
                                    NBTTagCompound extra = item.getTagCompound().getCompoundTag("ExtraAttributes");
                                    
                                    // Fixed Casing Issue: Checks for both lowercase 'nonce' and 'Nonce'
                                    if (extra.hasKey("nonce") || extra.hasKey("Nonce")) {
                                        int nonce = extra.hasKey("nonce") ? extra.getInteger("nonce") : extra.getInteger("Nonce");
                                        
                                        // Item must have custom enchants to be a valid scraped nonce
                                        if (extra.hasKey("CustomEnchants") && nonce != 0 && nonce != 5 && nonce != 6 && nonce != 9) {
                                            foundNonce = nonce;
                                            break;
                                        }
                                    }
                                }
                            }
                            
                            // Step 2: Prepare for API Scrape
                            resolvingNicks.add(nick);
                            retryCooldowns.put(nick, System.currentTimeMillis()); 
                            
                            if (!NickedHUD.nickedPlayers.contains(nick.toLowerCase())) {
                                NickedHUD.nickedPlayers.add(nick.toLowerCase());
                            }
                            
                            if (!notifiedScraping.contains(nick)) {
                                sendChatMsg(prefix + EnumChatFormatting.YELLOW + "Scraping " + EnumChatFormatting.DARK_AQUA + "[" + EnumChatFormatting.AQUA + "N" + EnumChatFormatting.DARK_AQUA + "] " + EnumChatFormatting.AQUA + nick + EnumChatFormatting.YELLOW + "...");
                                notifiedScraping.add(nick);
                            }
                            
                            // Step 3: Run Multi-API Request Pipeline
                            final int finalNonce = foundNonce;
                            final UUID finalUUID = playerUUID;
                            final long millisStarted = System.currentTimeMillis();
                            
                            new Thread(() -> {
                                try {
                                    String realName = null;
                                    String methodUsed = "";

                                    // A. PITPAL NONCE ITEM LOOKUP
                                    if (finalNonce != -1) {
                                        realName = fetchNameFromPitpalNonce(finalNonce);
                                        if (realName != null) methodUsed = "Pitpal Nonce";
                                    }

                                    // B. PITPAL IGN LOOKUP
                                    if (realName == null) {
                                        realName = fetchNameFromPitpalIGN(nick);
                                        if (realName != null) methodUsed = "Pitpal IGN";
                                    }

                                    // C. PITPAL UUID FALLBACK
                                    if (realName == null) {
                                        realName = fetchNameFromPitpalUUID(finalUUID);
                                        if (realName != null) methodUsed = "Pitpal UUID";
                                    }

                                    // D. PITPANDA UUID FALLBACK
                                    if (realName == null) {
                                        realName = fetchNameFromPitpandaUUID(finalUUID);
                                        if (realName != null) methodUsed = "Pitpanda UUID";
                                    }

                                    long time = System.currentTimeMillis() - millisStarted;
                                    
                                    synchronized (CacheManager.class) {
                                        if (realName != null && isValidMinecraftName(realName)) {
                                            CacheManager.addToCache(nick, realName);
                                            NickedManager.updateNicked(nick, realName);
                                            
                                            sendChatMsg(prefix + EnumChatFormatting.GREEN + "Denicked " + EnumChatFormatting.GRAY + "\u27A1 " + EnumChatFormatting.DARK_AQUA + "[" + EnumChatFormatting.AQUA + "N" + EnumChatFormatting.DARK_AQUA + "] " + EnumChatFormatting.AQUA + nick + " " + EnumChatFormatting.GRAY + "\u2192 " + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + realName + " " + EnumChatFormatting.GRAY + "(" + EnumChatFormatting.WHITE + time + "ms - " + methodUsed + EnumChatFormatting.GRAY + ")");
                                        } else {
                                            // Passing the IGN 'nick' so it creates a fallback link if the nonce is -1
                                            sendClickableManualLink(finalNonce, nick);
                                            NickedManager.updateNicked(nick, finalNonce == -1 ? "Failed" : "No Nonce");
                                        }
                                        
                                        mc.addScheduledTask(() -> {
                                            EntityPlayer targetEntity = mc.theWorld.getPlayerEntityByName(nick);
                                            if (targetEntity != null) {
                                                targetEntity.refreshDisplayName();
                                            }
                                        });
                                    }
                                } finally {
                                    resolvingNicks.remove(nick);
                                }
                            }).start();
                            
                        } catch (Exception e) {
                            sendDebug("Internal error while scanning inventory: " + e.getMessage());
                            retryCooldowns.put(nick, System.currentTimeMillis());
                        }
                    }
                }
            }
        }
        
        for (String name : currentNickedSet) {
            if (!lastNickedSet.contains(name)) {
                sendChatMsg(prefix + EnumChatFormatting.YELLOW + "\u26A0 " + EnumChatFormatting.GOLD + "Nicked Player Detected " + EnumChatFormatting.GRAY + "\u27A1 " + EnumChatFormatting.DARK_AQUA + "[" + EnumChatFormatting.AQUA + "N" + EnumChatFormatting.DARK_AQUA + "] " + EnumChatFormatting.AQUA + name);
            }
        }
        lastNickedSet.clear();
        lastNickedSet.addAll(currentNickedSet);
    }

    private static String getRealNameFromNBT(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return null;

        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag.hasKey("ExtraAttributes", 10)) {
            NBTTagCompound extra = tag.getCompoundTag("ExtraAttributes");

            if (extra.hasKey("OriginalOwner")) return extra.getString("OriginalOwner");
            if (extra.hasKey("RealName")) return extra.getString("RealName");
            
            if (tag.hasKey("display", 10)) {
                NBTTagCompound display = tag.getCompoundTag("display");
                if (display.hasKey("Lore", 9)) {
                    NBTTagList lore = display.getTagList("Lore", 8);
                    for (int i = 0; i < lore.tagCount(); i++) {
                        String line = EnumChatFormatting.getTextWithoutFormattingCodes(lore.getStringTagAt(i));
                        if (line != null && line.contains("Owner: ")) {
                            String[] split = line.split("Owner: ");
                            if (split.length > 1) return split[1].trim();
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String fetchNameFromPitpalNonce(int nonce) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://pitpal.rocks/api/listings/items/nonce" + nonce);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject root = new JSONObject(sb.toString());
                JSONArray itemsArr = null;
                if (root.has("items") && !root.isNull("items")) itemsArr = root.getJSONArray("items");
                else if (root.has("data") && !root.isNull("data")) itemsArr = root.getJSONArray("data");

                if (itemsArr != null && itemsArr.length() > 0) {
                    JSONObject itemObj = itemsArr.getJSONObject(0);
                    if (itemObj.has("ownerusername") && !itemObj.isNull("ownerusername")) {
                        String raw = itemObj.getString("ownerusername");
                        String cleanName = raw.replaceAll("§.", "").replaceAll("\\$.", "").trim();
                        String[] parts = cleanName.split(" ");
                        return parts[parts.length - 1];
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    private static String fetchNameFromPitpalIGN(String ign) {
        long timestamp = System.currentTimeMillis();
        return fetchJsonName("https://pitpal.rocks/api/ign/lookup/" + ign + "?t=" + timestamp);
    }

    private static String fetchNameFromPitpalUUID(UUID uuid) {
        String formattedUUID = uuid.toString().replace("-", "");
        return fetchJsonName("https://pitpal.rocks/api/players/" + formattedUUID);
    }

    private static String fetchNameFromPitpandaUUID(UUID uuid) {
        String formattedUUID = uuid.toString().replace("-", "");
        return fetchJsonName("https://pitpanda.rocks/api/players/" + formattedUUID);
    }

    private static String fetchJsonName(String urlString) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject response = new JSONObject(sb.toString());
                if ((response.has("success") && response.getBoolean("success")) || 
                    (response.has("sucess") && response.getBoolean("sucess"))) {
                    if (response.has("data") && !response.isNull("data")) {
                        JSONObject data = response.getJSONObject("data");
                        if (data.has("name") && !data.isNull("name")) {
                            return data.getString("name");
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    private static boolean isValidMinecraftName(String name) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false; 
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static void sendClickableManualLink(int nonce, String ign) {
        // If nonce is found, link to item data. If naked/no-nonce, link directly to PitPal's IGN lookup!
        String url = nonce != -1 
                ? "https://pitpal.rocks/api/listings/items/nonce" + nonce 
                : "https://pitpal.rocks/api/ign/lookup/" + ign + "?t=" + System.currentTimeMillis();

        ChatComponentText msg = new ChatComponentText(prefix + EnumChatFormatting.RED + "Auto-denick failed. " + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + "[CLICK TO VIEW JSON]");

        ChatStyle style = new ChatStyle();
        style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Open: " + url)));

        msg.setChatStyle(style);
        sendRawChatMessage(msg);
    }

    private static void sendChatMsg(String msg) {
        sendRawChatMessage(new ChatComponentText(msg));
    }

    private static void sendDebug(String msg) {
        if (EnemyHUD.debugMode) {
            sendRawChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[DEBUG] " + msg));
        }
    }

    private static void sendRawChatMessage(IChatComponent component) {
        mc.addScheduledTask(() -> {
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(component);
            }
        });
    }
}