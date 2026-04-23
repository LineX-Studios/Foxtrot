package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Util.SpawnRegions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RegHUD extends DraggableHUD {
    public static final RegHUD instance = new RegHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;

    // ==========================================
    //       NBT MEMORY CACHE
    // ==========================================
    private final Map<String, CachedRegData> regCache = new HashMap<>();

    public RegHUD() { super("Regularity List", 10, 180); }

    // Wipes the memory clean when you warp to a brand new lobby
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        regCache.clear();
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.currentScreen instanceof EditHUDGui) return;
        if (mc.currentScreen instanceof HUDSettingsGui) return;
        render(false, 0, 0); 
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled || mc.theWorld == null) return;

        FontRenderer fr = mc.fontRendererObj;
        int currentY = 0; 
        int maxWidth = fr.getStringWidth("Regularity Players");
        boolean foundReg = false;

        // ==========================================
        //  STEP 1: SCAN LOADED PLAYERS & UPDATE CACHE
        // ==========================================
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (!(player instanceof EntityOtherPlayerMP)) continue;
            EntityOtherPlayerMP other = (EntityOtherPlayerMP) player;

            if (other.isDead || other.getHealth() <= 0.0F) continue;
            if (mc.getNetHandler().getPlayerInfo(other.getUniqueID()) == null) continue;

            String enchantsDisplay = getRegEnchants(other);
            String name = other.getName();

            // If we physically see them wearing Reg, lock them into memory!
            if (enchantsDisplay != null) {
                String displayName = EnumChatFormatting.DARK_RED + "[" + EnumChatFormatting.RED + "R" + EnumChatFormatting.DARK_RED + "] " + EnumChatFormatting.RESET + other.getDisplayName().getFormattedText();
                regCache.put(name, new CachedRegData(displayName, enchantsDisplay));
            } 
            // If we physically see them take the pants off, remove them so the list stays clean
            else {
                regCache.remove(name);
            }
        }

        // ==========================================
        //  STEP 2: DRAW EVERYONE FROM MEMORY
        // ==========================================
        Iterator<Map.Entry<String, CachedRegData>> iterator = regCache.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, CachedRegData> entry = iterator.next();
            String playerName = entry.getKey();
            CachedRegData data = entry.getValue();

            // 1. Did they 100% leave the lobby?
            boolean isStillInLobby = false;
            for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equalsIgnoreCase(playerName)) {
                    isStillInLobby = true;
                    break;
                }
            }
            
            // If they disconnected, delete them and don't draw
            if (!isStillInLobby) {
                iterator.remove();
                continue;
            }

            if (!foundReg) {
                fr.drawStringWithShadow(EnumChatFormatting.DARK_RED + "" + EnumChatFormatting.BOLD + "Regularity Players:", 0, currentY, 0xFFFFFF);
                currentY += fr.FONT_HEIGHT + 2;
                foundReg = true;
            }

            // 2. Are they in render distance, or 500 blocks away?
            EntityPlayer physicalPlayer = mc.theWorld.getPlayerEntityByName(playerName);
            String dist;
            
            if (physicalPlayer != null) {
                // They are close to us! Update live location
                dist = SpawnRegions.getLocationFormat(mc.thePlayer, physicalPlayer);
            } else {
                // They are out of render distance! Keep them on screen, but mark as OOR
                dist = EnumChatFormatting.DARK_GRAY + "OOR"; 
            }

            String fullLine = data.displayName + EnumChatFormatting.GRAY + " - " + data.enchants + EnumChatFormatting.GRAY + " - " + dist;
            fr.drawStringWithShadow(fullLine, 0, currentY, 0xFFFFFF);

            int lineWidth = fr.getStringWidth(fullLine);
            if (lineWidth > maxWidth) maxWidth = lineWidth;
            currentY += fr.FONT_HEIGHT;
        }

        if (!foundReg) {
            if (isEditing) {
                fr.drawStringWithShadow(EnumChatFormatting.DARK_RED + "" + EnumChatFormatting.BOLD + "Regularity Players:", 0, currentY, 0xFFFFFF);
                currentY += fr.FONT_HEIGHT + 2;
                String placeholder = EnumChatFormatting.DARK_RED + "[" + EnumChatFormatting.RED + "R" + EnumChatFormatting.DARK_RED + "] " + EnumChatFormatting.GRAY + "[120] Player" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.DARK_RED + "Reg" + EnumChatFormatting.WHITE + "/" + EnumChatFormatting.WHITE + "Mirror" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
                fr.drawStringWithShadow(placeholder, 0, currentY, 0xFFFFFF);
                currentY += fr.FONT_HEIGHT;
                maxWidth = Math.max(maxWidth, fr.getStringWidth(placeholder));
            } else {
                this.width = 0; this.height = 0;
                return;
            }
        }

        this.width = maxWidth;
        this.height = currentY;
    }

    private String getRegEnchants(EntityOtherPlayerMP player) {
        ItemStack pants = player.inventory.armorInventory[1];
        if (pants == null || !pants.hasTagCompound()) return null;

        NBTTagCompound extra = pants.getTagCompound().getCompoundTag("ExtraAttributes");
        if (extra == null || !extra.hasKey("CustomEnchants")) return null;

        NBTTagList enchants = extra.getTagList("CustomEnchants", 10);
        boolean hasRegularity = false;
        String regString = "";
        List<String> sideEnchants = new ArrayList<>();

        for (int i = 0; i < enchants.tagCount(); i++) {
            String key = enchants.getCompoundTagAt(i).getString("Key").trim();
            String formatted = formatEnchant(key);

            if (formatted != null) {
                if (key.equalsIgnoreCase("regularity")) {
                    hasRegularity = true;
                    regString = formatted;
                } else {
                    sideEnchants.add(formatted);
                }
            }
        }

        if (!hasRegularity) return null;

        List<String> finalEnchants = new ArrayList<>();
        finalEnchants.add(regString);
        finalEnchants.addAll(sideEnchants);

        return String.join(EnumChatFormatting.WHITE + "/", finalEnchants);
    }

    public static String formatEnchant(String rawKey) {
        if (rawKey == null) return null;
        String key = rawKey.trim().toLowerCase();
        switch (key) {
            case "regularity": return EnumChatFormatting.DARK_RED + "Reg";
            case "immune_true_damage": 
            case "mirror":
            case "reflection": return EnumChatFormatting.WHITE + "Mirror";
            case "respawn_absorption": 
            case "respawn_with_absorption": return EnumChatFormatting.GOLD + "Abs";
            case "critically_funky": return EnumChatFormatting.DARK_AQUA + "Crit Funky";
            case "solitude": return EnumChatFormatting.RED + "Soli";
            case "protection": return EnumChatFormatting.BLUE + "Prot";
            case "fractional_reserve": return EnumChatFormatting.BLUE + "Frac";
            case "less_damage_nearby_players":
            case "not_gladiator": return EnumChatFormatting.BLUE + "Glad";
            case "hunt_the_hunter": return EnumChatFormatting.GOLD + "Hunter";
            case "regen_when_hit":
            case "peroxide": return EnumChatFormatting.RED + "Pero";
            case "assassin": return EnumChatFormatting.LIGHT_PURPLE + "Assasin";
            case "escape_pod": return EnumChatFormatting.RED + "Pods";
            case "phoenix": return EnumChatFormatting.GOLD + "Phoenix";
            case "rgm":
            case "retro_gravity_microcosm": return EnumChatFormatting.RED + "RGM";
            case "singularity": return EnumChatFormatting.RED + "Sing";
            case "regen_when_ooc":
            case "gomraws_heart": return EnumChatFormatting.RED + "Gomraw";
            case "resistance_when_low":
            case "last_stand": return EnumChatFormatting.AQUA + "Stand";
            case "perma_speed":
            case "gotta_go_fast": return EnumChatFormatting.DARK_PURPLE + "GTGF";
            case "diamond_allergy": return EnumChatFormatting.AQUA + "Diamond Allergy";
            case "less_damage_vs_bounties":
            case "david_and_goliath": return EnumChatFormatting.YELLOW + "D&G";
            case "heigh_ho": return EnumChatFormatting.RED + "HeighHo";
            default: return null;
        }
    }

    // A simple data container for our Memory Cache
    private static class CachedRegData {
        String displayName;
        String enchants;

        CachedRegData(String displayName, String enchants) {
            this.displayName = displayName;
            this.enchants = enchants;
        }
    }
}