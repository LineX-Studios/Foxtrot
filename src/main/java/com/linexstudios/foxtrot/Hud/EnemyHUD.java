package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Enemy.EnemyManager;
import com.linexstudios.foxtrot.Util.SpawnRegions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnemyHUD extends DraggableHUD {
    public static final EnemyHUD instance = new EnemyHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;
    public static boolean debugMode = false;
    public static boolean notificationsEnabled = true;

    public static List<String> targetList = new ArrayList<>();

    public EnemyHUD() { super("Enemy List", 200, 80); }

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
        int maxWidth = fr.getStringWidth("Enemy List");
        boolean foundEnemy = false;

        Set<String> renderedEnemies = new HashSet<>();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (!(player instanceof EntityOtherPlayerMP)) continue;
            EntityOtherPlayerMP other = (EntityOtherPlayerMP) player;

            if (!isTarget(other)) continue;
            
            String name = other.getName();
            if (renderedEnemies.contains(name.toLowerCase())) continue;
            renderedEnemies.add(name.toLowerCase());

            if (!foundEnemy) {
                fr.drawStringWithShadow(EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "Enemy List:", 0, currentY, 0xFFFFFF);
                currentY += fr.FONT_HEIGHT + 2;
                foundEnemy = true;
            }

            String displayName;
            String rawFormatted = other.getDisplayName().getFormattedText();
            int nameIndex = rawFormatted.indexOf(name);
            if (nameIndex >= 0) {
                displayName = rawFormatted.substring(0, nameIndex + name.length());
            } else {
                displayName = EnumChatFormatting.GRAY + "[?] " + EnumChatFormatting.RED + name;
            }

            String currentUUID = other.getUniqueID().toString();
            String cachedName = EnemyManager.enemyCache.get(currentUUID);
            
            if (cachedName != null && !cachedName.equalsIgnoreCase(name)) {
                displayName += EnumChatFormatting.YELLOW + " (" + EnumChatFormatting.YELLOW + cachedName + EnumChatFormatting.YELLOW + ")";
            }

            displayName = EnumChatFormatting.DARK_RED + "[" + EnumChatFormatting.RED + "E" + EnumChatFormatting.DARK_RED + "] " + EnumChatFormatting.RESET + displayName;

            String gear = getShortEnchants(other);
            String dist = SpawnRegions.getLocationFormat(mc.thePlayer, other);

            String fullLine = displayName + EnumChatFormatting.GRAY + " - " + gear + EnumChatFormatting.GRAY + " - " + dist;
            fr.drawStringWithShadow(fullLine, 0, currentY, 0xFFFFFF);

            int lineWidth = fr.getStringWidth(fullLine);
            if (lineWidth > maxWidth) maxWidth = lineWidth;
            currentY += fr.FONT_HEIGHT;
        }

        if (!foundEnemy) {
            if (isEditing) {
                fr.drawStringWithShadow(EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "Enemy List:", 0, currentY, 0xFFFFFF);
                currentY += fr.FONT_HEIGHT + 2;
                String placeholder = EnumChatFormatting.DARK_RED + "[" + EnumChatFormatting.RED + "E" + EnumChatFormatting.DARK_RED + "] " + EnumChatFormatting.GRAY + "[120] Player " + EnumChatFormatting.YELLOW + "(" + EnumChatFormatting.YELLOW + "OLD_USERNAME" + EnumChatFormatting.YELLOW + ")" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
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
            if (pants.getItem() == net.minecraft.init.Items.diamond_leggings) {
                return EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD + "DIAMOND";
            }
        }
        return EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD + "SHOP";
    }

    public static String formatEnchant(String key) {
        if (key == null) return null;
        switch (key.toLowerCase()) {
            case "regularity": return EnumChatFormatting.DARK_RED + "Reg";
            case "immune_true_damage": 
            case "mirror":
            case "reflection": return EnumChatFormatting.WHITE + "Mirror";
            case "respawn_absorption": 
            case "respawn_with_absorption": return EnumChatFormatting.GOLD + "Abs";
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

    public static boolean isTarget(EntityPlayer player) {
        if (player == null) return false;
        
        String currentName = player.getName();
        String currentUUID = player.getUniqueID().toString();

        if (EnemyManager.enemyCache.containsKey(currentUUID)) {
            return true;
        }

        if (targetList.stream().anyMatch(t -> t.equalsIgnoreCase(currentName))) {
            String expectedUUID = EnemyManager.getUUIDFromName(currentName);
            if (expectedUUID != null && !expectedUUID.equals(currentUUID)) {
                return false; 
            }
            return true;
        }
        return false;
    }

    public static boolean isTarget(String name) {
        if (name == null) return false;
        return targetList.stream().anyMatch(t -> t.equalsIgnoreCase(name));
    }
}