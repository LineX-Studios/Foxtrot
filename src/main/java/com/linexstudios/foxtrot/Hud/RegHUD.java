package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Handler.MapDetectionHandler;
import com.linexstudios.foxtrot.Handler.PlayerTrackerHandler;
import com.linexstudios.foxtrot.Render.BetterCulling;
import com.linexstudios.foxtrot.Util.SpawnRegions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegHUD extends DraggableHUD {
    public static final RegHUD instance = new RegHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;
    private final Map<String, String> regGearCache = new HashMap<>();

    public RegHUD() { super("Regularity List", 10, 180); }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) { regGearCache.clear(); }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.currentScreen instanceof EditHUDGui || mc.currentScreen instanceof HUDSettingsGui) return;
        render(false, 0, 0); 
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled || mc.theWorld == null || mc.thePlayer == null) return;

        MapDetectionHandler.updateMap();
        FontRenderer fr = mc.fontRendererObj;
        int currentY = 0; 
        int maxWidth = fr.getStringWidth("Regularity Players");
        boolean foundReg = false;

        List<HUDRow> rows = new ArrayList<>();
        for (PlayerTrackerHandler.TrackedPlayer player : PlayerTrackerHandler.activePlayers.values()) {
            if (player.name.equals(mc.thePlayer.getName())) continue;
            EntityOtherPlayerMP other = player.entity;

            if (other != null && !other.isDead && other.getHealth() > 0.0F) {
                String enchants = getRegEnchants(other);
                if (enchants != null) regGearCache.put(player.name, enchants);
                else regGearCache.remove(player.name);
            }

            if (!regGearCache.containsKey(player.name)) continue;

            String displayName = player.lastKnownNamePlate != null ? player.lastKnownNamePlate : (EnumChatFormatting.GRAY + "[?] " + EnumChatFormatting.RED + player.name);
            displayName = truncate(displayName, 32);
            String finalDisplayName = EnumChatFormatting.DARK_RED + "[" + EnumChatFormatting.RED + "R" + EnumChatFormatting.DARK_RED + "] " + EnumChatFormatting.RESET + displayName;
            String gearPart = regGearCache.get(player.name);

            String locStr = "";
            if (other != null) {
                int distance = Math.round(mc.thePlayer.getDistanceToEntity(other));
                EnumChatFormatting distColor = (distance >= 100) ? EnumChatFormatting.GREEN : (distance >= 60) ? EnumChatFormatting.YELLOW : (distance >= 30) ? EnumChatFormatting.GOLD : EnumChatFormatting.RED;
                locStr = SpawnRegions.getRegionString(other) + EnumChatFormatting.GRAY + " (" + distColor + distance + "m" + EnumChatFormatting.GRAY + ")";
            }

            rows.add(new HUDRow(finalDisplayName, gearPart, locStr));
        }

        if (rows.isEmpty()) {
            if (isEditing) {
                String placeholder = EnumChatFormatting.DARK_RED + "[" + EnumChatFormatting.RED + "R" + EnumChatFormatting.DARK_RED + "] " + EnumChatFormatting.GRAY + "[120] Player";
                rows.add(new HUDRow(placeholder, EnumChatFormatting.DARK_RED + "Reg" + EnumChatFormatting.WHITE + "/" + EnumChatFormatting.WHITE + "Mirror", EnumChatFormatting.GREEN + "SPAWN"));
            } else {
                this.width = 0; this.height = 0;
                return;
            }
        }

        int maxNameW = 0;
        int maxGearW = 0;
        for (HUDRow row : rows) {
            maxNameW = Math.max(maxNameW, fr.getStringWidth(row.name));
            if (!row.gear.isEmpty()) maxGearW = Math.max(maxGearW, fr.getStringWidth("- " + row.gear));
        }

        fr.drawStringWithShadow(EnumChatFormatting.DARK_RED + "" + EnumChatFormatting.BOLD + "Regularity Players:", 0, currentY, 0xFFFFFF);
        currentY += fr.FONT_HEIGHT + 2;

        int gearX = maxNameW + 10;
        int locX = gearX + maxGearW + 10;

        for (HUDRow row : rows) {
            fr.drawStringWithShadow(row.name, 0, currentY, 0xFFFFFF);
            if (!row.gear.isEmpty()) {
                fr.drawStringWithShadow(EnumChatFormatting.GRAY + "- " + row.gear, gearX, currentY, 0xFFFFFF);
            }
            if (!row.loc.isEmpty()) {
                fr.drawStringWithShadow(EnumChatFormatting.GRAY + "- " + row.loc, locX, currentY, 0xFFFFFF);
            }
            currentY += fr.FONT_HEIGHT;
        }

        // Recalculate true width
        for (HUDRow row : rows) {
            int w = locX + (row.loc.isEmpty() ? (row.gear.isEmpty() ? 0 : fr.getStringWidth("- " + row.gear)) : fr.getStringWidth("- " + row.loc));
            if (w > maxWidth) maxWidth = w;
        }
        this.width = maxWidth;
        this.height = currentY;
    }

    private static class HUDRow {
        String name, gear, loc;
        HUDRow(String n, String g, String l) { this.name = n; this.gear = g; this.loc = l; }
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
            case "immune_true_damage": case "mirror": case "reflection": return EnumChatFormatting.WHITE + "Mirror";
            case "respawn_absorption": case "respawn_with_absorption": return EnumChatFormatting.GOLD + "Abs";
            case "critically_funky": return EnumChatFormatting.DARK_AQUA + "Crit Funky";
            case "solitude": return EnumChatFormatting.RED + "Soli";
            case "venom": case "combo_venom": return EnumChatFormatting.DARK_PURPLE + "Venom";
            case "mind_assault": return EnumChatFormatting.DARK_PURPLE + "Mind Assaults";
            case "protection": return EnumChatFormatting.BLUE + "Prot";
            case "fractional_reserve": return EnumChatFormatting.BLUE + "Frac";
            case "not_gladiator": return EnumChatFormatting.BLUE + "Glad";
            default: return null;
        }
    }
}