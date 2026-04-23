package com.linexstudios.foxtrot.Denick;

import com.linexstudios.foxtrot.Hud.EnemyHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DenickRunnable implements Runnable {

    private final String guyToDenick;
    private final String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] ";
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public DenickRunnable(String arg) {
        this.guyToDenick = arg;
    }

    @Override
    public void run() {
        if (guyToDenick == null) return;

        EntityPlayer target = null;
        for (EntityPlayer p : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (p != null) {
                if (p.getName().equalsIgnoreCase(guyToDenick)) {
                    target = p;
                    break;
                } else if (target == null && p.getName().toLowerCase().contains(guyToDenick.toLowerCase())) {
                    target = p; 
                }
            }
        }

        if (target == null) {
            sendMessage(EnumChatFormatting.RED + "Player not found in render distance.");
            NickedManager.addNicked(guyToDenick, EnumChatFormatting.RED + "Not in range");
            return;
        }

        final String targetName = target.getName();
        final UUID targetUUID = target.getUniqueID();

        sendMessage(EnumChatFormatting.YELLOW + "Attempting to denick " + targetName + "...");

        int foundNonce = -1;
        String nbtRealName = null;
        
        List<ItemStack> items = new ArrayList<>();
        Collections.addAll(items, target.inventory.armorInventory);
        Collections.addAll(items, target.inventory.mainInventory);

        for (ItemStack item : items) {
            if (item != null && item.hasTagCompound() && item.getTagCompound().hasKey("ExtraAttributes", 10)) {
                
                if (nbtRealName == null) {
                    nbtRealName = getRealNameFromNBT(item);
                }

                NBTTagCompound extra = item.getTagCompound().getCompoundTag("ExtraAttributes");
                if (extra.hasKey("nonce") || extra.hasKey("Nonce")) {
                    int nonce = extra.hasKey("nonce") ? extra.getInteger("nonce") : extra.getInteger("Nonce");
                    if (nonce > 0 && nonce != 5 && nonce != 6 && nonce != 9) {
                        foundNonce = nonce;
                    }
                }
            }
        }

        final int finalNonce = foundNonce;
        final String finalNbtName = nbtRealName;

        executor.submit(() -> {
            String realName = null;
            String methodUsed = "";

            try {
                if (finalNbtName != null && !finalNbtName.isEmpty()) {
                    realName = finalNbtName;
                    methodUsed = "NBT Data";
                }

                if (realName == null && finalNonce != -1) {
                    realName = fetchNameFromPitpalNonce(finalNonce);
                    if (realName != null) methodUsed = "Pitpal Nonce";
                }

                if (realName == null) {
                    realName = fetchNameFromPitpalIGN(targetName);
                    if (realName != null) methodUsed = "Pitpal IGN";
                }

                if (realName == null) {
                    realName = fetchNameFromPitpalUUID(targetUUID);
                    if (realName != null) methodUsed = "Pitpal UUID";
                }

                if (realName == null) {
                    realName = fetchNameFromPitpandaUUID(targetUUID);
                    if (realName != null) methodUsed = "Pitpanda UUID";
                }

                if (realName != null && isValidMinecraftName(realName)) {
                    sendMessage(EnumChatFormatting.GREEN + "Denicked! " + EnumChatFormatting.WHITE + realName + EnumChatFormatting.GRAY + " (" + methodUsed + ")");
                    CacheManager.addToCache(targetName, realName);
                    NickedManager.addNicked(targetName, realName);
                } else {
                    sendClickableManualLink(finalNonce, targetName);
                    NickedManager.addNicked(targetName, finalNonce == -1 ? EnumChatFormatting.RED + "Failed" : EnumChatFormatting.RED + "No Nonce");
                }

            } catch (Exception e) {
                sendDebug("Manual resolution error for " + targetName + ": " + e.getMessage());
                sendClickableManualLink(finalNonce, targetName);
            }
        });
    }

    private String getRealNameFromNBT(ItemStack itemStack) {
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

    private String fetchNameFromPitpalNonce(int nonce) {
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

    private String fetchNameFromPitpalIGN(String ign) {
        long timestamp = System.currentTimeMillis();
        return fetchJsonName("https://pitpal.rocks/api/ign/lookup/" + ign + "?t=" + timestamp);
    }

    private String fetchNameFromPitpalUUID(UUID uuid) {
        String formattedUUID = uuid.toString().replace("-", "");
        return fetchJsonName("https://pitpal.rocks/api/players/" + formattedUUID);
    }

    private String fetchNameFromPitpandaUUID(UUID uuid) {
        String formattedUUID = uuid.toString().replace("-", "");
        return fetchJsonName("https://pitpanda.rocks/api/players/" + formattedUUID);
    }

    private String fetchJsonName(String urlString) {
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

    private boolean isValidMinecraftName(String name) {
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

    private void sendClickableManualLink(int nonce, String ign) {
        String url = nonce != -1 
                ? "https://pitpal.rocks/api/listings/items/nonce" + nonce 
                : "https://pitpal.rocks/api/ign/lookup/" + ign + "?t=" + System.currentTimeMillis();

        ChatComponentText msg = new ChatComponentText(prefix + EnumChatFormatting.RED + "Denick failed. " + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + "[CLICK TO VIEW JSON]");

        ChatStyle style = new ChatStyle();
        style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Open: " + url)));

        msg.setChatStyle(style);
        addChatMessage(msg);
    }

    private void sendMessage(String msg) {
        addChatMessage(new ChatComponentText(prefix + msg));
    }

    private void sendDebug(String msg) {
        if (EnemyHUD.debugMode) {
            addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[DEBUG] " + msg));
        }
    }

    private void addChatMessage(IChatComponent component) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(component);
            }
        });
    }
}