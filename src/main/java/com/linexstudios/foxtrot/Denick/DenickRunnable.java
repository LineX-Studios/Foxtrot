package com.linexstudios.foxtrot.Denick;

import com.linexstudios.foxtrot.Handler.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
    private final String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "PIT" + EnumChatFormatting.GRAY + "] ";
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public DenickRunnable(String arg) {
        this.guyToDenick = arg;
    }

    @Override
    public void run() {
        if (guyToDenick == null) return;

        // Step 1: Find the target player (Must be on Main Thread)
        EntityPlayer target = null;
        for (EntityPlayer p : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (p != null) {
                if (p.getName().equalsIgnoreCase(guyToDenick)) {
                    target = p;
                    break;
                } else if (target == null && p.getName().toLowerCase().contains(guyToDenick.toLowerCase())) {
                    target = p; // fallback to partial match
                }
            }
        }

        if (target == null) {
            sendMessage(EnumChatFormatting.RED + "Player not found in render distance.");
            NickedManager.addNicked(guyToDenick, EnumChatFormatting.RED + "Not in range");
            return;
        }

        final String targetName = target.getName();

        // Step 2: Extract Nonce (Main Thread)
        int foundNonce = -1;
        List<ItemStack> items = new ArrayList<>();
        Collections.addAll(items, target.inventory.armorInventory);
        Collections.addAll(items, target.inventory.mainInventory);

        for (ItemStack item : items) {
            if (item != null && item.hasTagCompound()) {
                NBTTagCompound extra = item.getTagCompound().getCompoundTag("ExtraAttributes");
                if (extra.hasKey("Nonce")) {
                    int nonce = extra.getInteger("Nonce");
                    if (nonce > 0) {
                        foundNonce = nonce;
                        break;
                    }
                }
            }
        }

        if (foundNonce == -1) {
            sendMessage(EnumChatFormatting.RED + "No PIT item with a valid nonce found on " + targetName);
            NickedManager.addNicked(targetName, EnumChatFormatting.RED + "No Nonce");
            return;
        }

        // Step 3: Run API Request (Side Thread to prevent lag)
        final int finalNonce = foundNonce;
        sendDebug("Found Nonce: " + finalNonce + ". Resolving...");

        executor.submit(() -> {
            String result = resolveOwnerFromNonce(finalNonce);
            if (result != null) {
                sendMessage(EnumChatFormatting.GREEN + "Denicked! " + EnumChatFormatting.WHITE + result);
                
                // INTEGRATION: Immediately saves the valid real name to be displayed on NickedHUD
                NickedManager.addNicked(targetName, result);
            } else {
                sendClickableManualLink(finalNonce);
                
                // Updates the HUD to show it failed to find a valid match
                NickedManager.addNicked(targetName, EnumChatFormatting.RED + "Failed");
            }
        });
    }

    private String resolveOwnerFromNonce(int nonce) {
        HttpURLConnection conn = null;
        try {
            String fullUrl = "https://pitpal.rocks/api/listings/items/nonce" + nonce;
            sendDebug("Requesting URL: " + fullUrl);

            URL url = new URL(fullUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() != 200) {
                sendDebug("PitPal API returned error code: " + conn.getResponseCode());
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            String response = sb.toString();
            if (response.isEmpty() || !response.startsWith("{")) return null;

            JSONObject root = new JSONObject(response);

            // Handle both "items" and "data" arrays based on PitPal's formatting
            JSONArray itemsArr = null;
            if (root.has("items") && !root.isNull("items")) {
                itemsArr = root.getJSONArray("items");
            } else if (root.has("data") && !root.isNull("data")) {
                itemsArr = root.getJSONArray("data");
            }

            if (itemsArr != null) {
                for (int i = 0; i < itemsArr.length(); i++) {
                    JSONObject itemObj = itemsArr.getJSONObject(i);

                    String ownerName = null;
                    String ownerId = null;

                    if (itemObj.has("ownerusername") && !itemObj.isNull("ownerusername")) {
                        String raw = itemObj.getString("ownerusername");
                        ownerName = raw.replaceAll("§.", "").replaceAll("\\$.", "").trim();
                    }

                    if (itemObj.has("ownerId") && !itemObj.isNull("ownerId")) {
                        ownerId = itemObj.getString("ownerId").trim();
                    } else if (itemObj.has("ownerid") && !itemObj.isNull("ownerid")) {
                        ownerId = itemObj.getString("ownerid").trim();
                    }

                    if (ownerName != null && !ownerName.isEmpty()) {
                        sendDebug("Found potential owner: " + ownerName + ". Verifying existence...");
                        
                        // VALIDATION PATCH: Checks if it's a fake/junk name like "weaponlies"
                        if (isValidMinecraftName(ownerName)) {
                            sendDebug("Resolved valid owner: " + ownerName);
                            return ownerName;
                        } else {
                            sendDebug("Invalid/Non-existent name found: " + ownerName);
                            // Set to null so the code falls back to checking the UUID instead
                            ownerName = null;
                        }
                    }

                    if (ownerId != null && !ownerId.isEmpty()) {
                        String resolvedName = resolveUsernameFromUUID(ownerId);
                        if (resolvedName != null) {
                            sendDebug("Resolved UUID " + ownerId + " to IGN: " + resolvedName);
                            return resolvedName; 
                        } else {
                            sendDebug("Could not resolve UUID: " + ownerId);
                            return null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            sendDebug("Network error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    /**
     * Pings the official Mojang API. If Mojang returns a 204 (No Content), 
     * the account does not exist, meaning it's corrupted API junk.
     */
    private boolean isValidMinecraftName(String name) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // Mojang returns 200 for valid names, and 204 or 404 for fake/non-existent ones
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false; // Prevent junk names if the network fails
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String resolveUsernameFromUUID(String uuid) {
        HttpURLConnection conn = null;
        try {
            String fullUrl = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-", "");
            sendDebug("Resolving UUID via Mojang API: " + fullUrl);

            URL url = new URL(fullUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() != 200) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            String response = sb.toString();
            if (response.isEmpty() || !response.startsWith("{")) return null;

            JSONObject root = new JSONObject(response);
            if (root.has("name")) {
                return root.getString("name");
            }
        } catch (Exception e) {
            sendDebug("UUID resolution error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    private void sendClickableManualLink(int nonce) {
        String url = "https://pitpal.rocks/api/listings/items/nonce" + nonce;
        ChatComponentText msg = new ChatComponentText(prefix + EnumChatFormatting.RED + "denick failed. " + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + "[CLICK TO VIEW JSON]");

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
        // Only spams chat with debug info if you have /fx debug turned ON
        if (ConfigHandler.globalDebug) {
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