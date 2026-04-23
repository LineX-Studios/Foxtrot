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

public class DarksHUD extends DraggableHUD {
    public static final DarksHUD instance = new DarksHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;

    // ==========================================
    //       NBT MEMORY CACHE
    // ==========================================
    private final Map<String, CachedDarkData> darkCache = new HashMap<>();

    // Set the default position slightly below RegHUD
    public DarksHUD() { super("Darks List", 10, 240); }

    // Wipes the memory clean when you warp to a brand new lobby
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        darkCache.clear();
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
        int maxWidth = fr.getStringWidth("Dark Pants Players");
        boolean foundDark = false;

        // ==========================================
        //  STEP 1: SCAN LOADED PLAYERS & UPDATE CACHE
        // ==========================================
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (!(player instanceof EntityOtherPlayerMP)) continue;
            EntityOtherPlayerMP other = (EntityOtherPlayerMP) player;

            // Ghost & Disconnect Filter
            if (other.isDead || other.getHealth() <= 0.0F) continue;
            if (mc.getNetHandler().getPlayerInfo(other.getUniqueID()) == null) continue;

            String enchantsDisplay = getDarkEnchants(other);
            String name = other.getName();

            // If we physically see them wearing Darks, lock them into memory!
            if (enchantsDisplay != null) {
                String displayName = EnumChatFormatting.DARK_PURPLE + "[" + EnumChatFormatting.LIGHT_PURPLE + "D" + EnumChatFormatting.DARK_PURPLE + "] " + EnumChatFormatting.RESET + other.getDisplayName().getFormattedText();
                darkCache.put(name, new CachedDarkData(displayName, enchantsDisplay));
            } 
            // If we physically see them take the pants off, remove them
            else {
                darkCache.remove(name);
            }
        }

        // ==========================================
        //  STEP 2: DRAW EVERYONE FROM MEMORY
        // ==========================================
        Iterator<Map.Entry<String, CachedDarkData>> iterator = darkCache.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, CachedDarkData> entry = iterator.next();
            String playerName = entry.getKey();
            CachedDarkData data = entry.getValue();

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

            if (!foundDark) {
                fr.drawStringWithShadow(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "Dark Pants Players:", 0, currentY, 0xFFFFFF);
                currentY += fr.FONT_HEIGHT + 2;
                foundDark = true;
            }

            // 2. Are they in render distance, or out of range?
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

        // Placeholder for the GUI Editor
        if (!foundDark) {
            if (isEditing) {
                fr.drawStringWithShadow(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "Dark Pants Players:", 0, currentY, 0xFFFFFF);
                currentY += fr.FONT_HEIGHT + 2;
                String placeholder = EnumChatFormatting.DARK_PURPLE + "[" + EnumChatFormatting.LIGHT_PURPLE + "D" + EnumChatFormatting.DARK_PURPLE + "] " + EnumChatFormatting.GRAY + "[120] Player" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.DARK_PURPLE + "VENOM" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
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

    private String getDarkEnchants(EntityOtherPlayerMP player) {
        ItemStack pants = player.inventory.armorInventory[1];
        if (pants == null || !pants.hasTagCompound()) return null;

        NBTTagCompound extra = pants.getTagCompound().getCompoundTag("ExtraAttributes");
        if (extra == null || !extra.hasKey("CustomEnchants")) return null;

        NBTTagList enchants = extra.getTagList("CustomEnchants", 10);
        boolean isDarkPants = false;
        List<String> darkEnchantList = new ArrayList<>();
        List<String> sideEnchants = new ArrayList<>();

        for (int i = 0; i < enchants.tagCount(); i++) {
            String key = enchants.getCompoundTagAt(i).getString("Key").trim();
            String formatted = formatEnchant(key);

            if (formatted != null) {
                if (isPrimaryDarkEnchant(key)) {
                    isDarkPants = true;
                    darkEnchantList.add(formatted);
                } else {
                    sideEnchants.add(formatted);
                }
            }
        }

        if (!isDarkPants) return null;

        List<String> finalEnchants = new ArrayList<>();
        finalEnchants.addAll(darkEnchantList); // Put dark enchants first
        finalEnchants.addAll(sideEnchants);

        return String.join(EnumChatFormatting.WHITE + "/", finalEnchants);
    }

    /**
     * Checks if the enchant is one of the exclusive dark pants enchants.
     */
    private boolean isPrimaryDarkEnchant(String rawKey) {
        String key = rawKey.trim().toLowerCase();
        return key.equals("venom") || key.equals("sanguisuge") || 
               key.equals("grim_reaper") || key.equals("misery") || key.equals("spite") || 
               key.equals("nostalgia") || key.equals("golden_handcuffs") || key.equals("hedge_fund") || 
               key.equals("heartripper") || key.equals("needless_suffering") || 
               key.equals("mind_assault") || key.equals("lycanthropy");
    }

    public static String formatEnchant(String rawKey) {
        if (rawKey == null) return null;

        String key = rawKey.trim().toLowerCase();

        switch (key) {
            // --- Dark Enchants ---
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
            case "mind_assault": return EnumChatFormatting.DARK_PURPLE + "MIND ASSAULTS";
            case "lycanthropy": return EnumChatFormatting.DARK_RED + "LYCANTHROPY";
            default: return null;
        }
    }

    // A simple data container for our Memory Cache
    private static class CachedDarkData {
        String displayName;
        String enchants;

        CachedDarkData(String displayName, String enchants) {
            this.displayName = displayName;
            this.enchants = enchants;
        }
    }
}