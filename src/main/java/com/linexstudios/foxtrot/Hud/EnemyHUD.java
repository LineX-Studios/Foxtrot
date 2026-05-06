package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Enemy.EnemyManager;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class EnemyHUD extends DraggableHUD {
    public static final EnemyHUD instance = new EnemyHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;
    public static boolean notificationsEnabled = true;

    public static List<String> targetList = new ArrayList<>();

    public EnemyHUD() { super("Enemy List", 200, 80); }

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
        int maxWidth = fr.getStringWidth("Enemy List");
        boolean foundEnemy = false;

        List<HUDRow> rows = new ArrayList<>();
        for (PlayerTrackerHandler.TrackedPlayer player : PlayerTrackerHandler.activePlayers.values()) {
            if (player.name.equals(mc.thePlayer.getName())) continue;
            if (!isTarget(player.name)) continue;

            EntityOtherPlayerMP other = player.entity;
            if (other != null) {
                player.lastKnownGear = getShortEnchants(other);
            }

            String displayName = player.lastKnownNamePlate != null ? player.lastKnownNamePlate : (EnumChatFormatting.GRAY + "[?] " + EnumChatFormatting.RED + player.name);
            String currentUUID = player.uuid;
            String cachedName = EnemyManager.enemyCache.get(currentUUID);
            if (cachedName != null && !cachedName.equalsIgnoreCase(player.name)) {
                displayName += EnumChatFormatting.YELLOW + " (" + EnumChatFormatting.YELLOW + cachedName + EnumChatFormatting.YELLOW + ")";
            }
            displayName = truncate(displayName, 32);
            String finalDisplayName = EnumChatFormatting.DARK_RED + "[" + EnumChatFormatting.RED + "E" + EnumChatFormatting.DARK_RED + "] " + EnumChatFormatting.RESET + displayName;
            
            String gearPart = (player.lastKnownGear != null && !player.lastKnownGear.isEmpty()) ? player.lastKnownGear : "";
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
                String placeholder = EnumChatFormatting.DARK_RED + "[" + EnumChatFormatting.RED + "E" + EnumChatFormatting.DARK_RED + "] " + EnumChatFormatting.GRAY + "[120] Player" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
                rows.add(new HUDRow(placeholder, "DIAMOND", "SPAWN (15m)"));
            } else {
                this.width = 0; this.height = 0;
                return;
            }
        }

        fr.drawStringWithShadow(EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "Enemy List:", 0, currentY, 0xFFFFFF);
        currentY += fr.FONT_HEIGHT + 2;

        for (HUDRow row : rows) {
            String fullLine = row.name;
            if (!row.gear.isEmpty()) fullLine += EnumChatFormatting.GRAY + " - " + row.gear;
            if (!row.loc.isEmpty()) fullLine += EnumChatFormatting.GRAY + " - " + row.loc;
            
            fr.drawStringWithShadow(fullLine, 0, currentY, 0xFFFFFF);
            int lineWidth = com.linexstudios.foxtrot.Util.FastFont.getWidth(fullLine);
            if (lineWidth > maxWidth) maxWidth = lineWidth;
            
            currentY += fr.FONT_HEIGHT;
        }

        this.width = maxWidth;
        this.height = currentY;
    }

    private static class HUDRow {
        String name, gear, loc;
        HUDRow(String n, String g, String l) { this.name = n; this.gear = g; this.loc = l; }
    }

    private String getShortEnchants(EntityOtherPlayerMP player) {
        ItemStack pants = player.inventory.armorInventory[1];
        if (pants != null) {
            if (pants.hasTagCompound()) {
                NBTTagCompound extra = pants.getTagCompound().getCompoundTag("ExtraAttributes");
                if (extra != null && extra.hasKey("CustomEnchants")) {
                    NBTTagList enchants = extra.getTagList("CustomEnchants", 10);
                    List<String> shortNames = new ArrayList<>();
                    for (int i = 0; i < enchants.tagCount(); i++) {
                        String formatted = formatEnchant(enchants.getCompoundTagAt(i).getString("Key"));
                        if (formatted != null) shortNames.add(formatted);
                    }
                    if (!shortNames.isEmpty()) return String.join(EnumChatFormatting.WHITE + "/", shortNames);
                }
                if (pants.hasDisplayName() && pants.getDisplayName().contains("Dark Pants")) return EnumChatFormatting.DARK_PURPLE + "Darks";
            }
            if (pants.getItem() == net.minecraft.init.Items.diamond_leggings) return EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD + "DIAMOND";
        }
        return EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD + "SHOP";
    }

    public static String formatEnchant(String key) {
        if (key == null) return null;
        switch (key.toLowerCase()) {
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

    public static boolean isTarget(String name) {
        if (name == null) return false;
        String expectedUUID = EnemyManager.getUUIDFromName(name);
        if (expectedUUID != null && EnemyManager.enemyCache.containsKey(expectedUUID)) return true;
        return targetList.stream().anyMatch(t -> t.equalsIgnoreCase(name));
    }
}