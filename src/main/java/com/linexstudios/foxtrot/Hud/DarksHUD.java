package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Handler.MapDetectionHandler;
import com.linexstudios.foxtrot.Handler.PlayerTrackerHandler;
import com.linexstudios.foxtrot.Util.SpawnRegions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DarksHUD extends DraggableHUD {
    public static final DarksHUD instance = new DarksHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;
    private final Map<String, String> darkGearCache = new HashMap<>();

    public DarksHUD() { super("Darks List", 10, 240); }

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
        int maxWidth = fr.getStringWidth("Darks List");

        List<HUDRow> rows = new ArrayList<>();
        for (PlayerTrackerHandler.TrackedPlayer player : PlayerTrackerHandler.activePlayers.values()) {
            if (player.name.equals(mc.thePlayer.getName())) continue;
            EntityOtherPlayerMP other = player.entity;

            if (other != null && !other.isDead && other.getHealth() > 0.0F) {
                String enchants = getDarkEnchants(other);
                if (enchants != null) darkGearCache.put(player.name, enchants);
                else darkGearCache.remove(player.name);
            }

            if (!darkGearCache.containsKey(player.name)) continue;

            String displayName = player.lastKnownNamePlate != null ? player.lastKnownNamePlate : (EnumChatFormatting.GRAY + "[?] " + EnumChatFormatting.DARK_PURPLE + player.name);
            displayName = truncate(displayName, 32);
            String gearPart = darkGearCache.get(player.name);

            String locStr = "";
            if (other != null) {
                int distance = Math.round(mc.thePlayer.getDistanceToEntity(other));
                EnumChatFormatting distColor = (distance >= 100) ? EnumChatFormatting.GREEN : (distance >= 60) ? EnumChatFormatting.YELLOW : (distance >= 30) ? EnumChatFormatting.GOLD : EnumChatFormatting.RED;
                locStr = SpawnRegions.getRegionString(other) + EnumChatFormatting.GRAY + " (" + distColor + distance + "m" + EnumChatFormatting.GRAY + ")";
            }

            rows.add(new HUDRow(displayName, gearPart, locStr));
        }

        if (rows.isEmpty()) {
            if (isEditing) {
                String placeholder = EnumChatFormatting.GRAY + "[?] " + EnumChatFormatting.DARK_PURPLE + "Player";
                rows.add(new HUDRow(placeholder, EnumChatFormatting.DARK_PURPLE + "Darks", EnumChatFormatting.GREEN + "SPAWN"));
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

        fr.drawStringWithShadow(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "Darks List:", 0, currentY, 0xFFFFFF);
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

    private String getDarkEnchants(EntityOtherPlayerMP player) {
        ItemStack pants = player.inventory.armorInventory[1];
        if (pants != null && pants.hasTagCompound()) {
            NBTTagCompound extra = pants.getTagCompound().getCompoundTag("ExtraAttributes");
            if (extra != null && extra.hasKey("CustomEnchants")) {
                NBTTagList enchants = extra.getTagList("CustomEnchants", 10);
                List<String> darkNames = new ArrayList<>();
                for (int i = 0; i < enchants.tagCount(); i++) {
                    String key = enchants.getCompoundTagAt(i).getString("Key");
                    if (isDarkEnchant(key)) {
                        String formatted = formatEnchant(key);
                        if (formatted != null) darkNames.add(formatted);
                    }
                }
                if (!darkNames.isEmpty()) return String.join(EnumChatFormatting.WHITE + "/", darkNames);
            }
        }
        return null;
    }

    private boolean isDarkEnchant(String rawKey) {
        String key = rawKey.trim().toLowerCase();
        return key.equals("venom") || key.equals("sanguisuge") || key.equals("grim_reaper") || key.equals("misery") || key.equals("spite") || key.equals("nostalgia") || key.equals("golden_handcuffs") || key.equals("hedge_fund") || key.equals("heartripper") || key.equals("needless_suffering") || key.equals("mind_assault") || key.equals("lycanthropy");
    }

    public static String formatEnchant(String rawKey) {
        if (rawKey == null) return null;
        String key = rawKey.trim().toLowerCase();
        switch (key) {
            case "venom": return EnumChatFormatting.DARK_PURPLE+ "VENOM";
            case "sanguisuge": return EnumChatFormatting.RED + "SANGUISUGE";
            case "grim_reaper": return EnumChatFormatting.GRAY + "GRIM REAPER";
            case "misery": return EnumChatFormatting.DARK_PURPLE + "MISERY";
            case "spite": return EnumChatFormatting.DARK_PURPLE + "SPITE";
            case "nostalgia": return EnumChatFormatting.BLUE + "NOSTALGIA";
            case "golden_handcuffs": return EnumChatFormatting.GOLD + "GOLDEN CUFFS";
            case "hedge_fund": return EnumChatFormatting.GOLD + "HEDGE FUND";
            case "heartripper": return EnumChatFormatting.RED + "HEART RIPPER";
            case "needless_suffering": return EnumChatFormatting.YELLOW + "NEEDLESS SUFFERING";
            case "mind_assault": return EnumChatFormatting.DARK_PURPLE + "MIND ASSAULT";
            case "lycanthropy": return EnumChatFormatting.DARK_RED + "LYCANTHROPY";
            default: return null;
        }
    }
}