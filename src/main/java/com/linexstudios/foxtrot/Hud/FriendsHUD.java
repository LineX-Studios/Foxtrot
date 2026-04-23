package com.linexstudios.foxtrot.Hud;

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

public class FriendsHUD extends DraggableHUD {
    public static final FriendsHUD instance = new FriendsHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;
    public static boolean debugMode = false;
    public static boolean notificationsEnabled = true;

    public static List<String> friendsList = new ArrayList<>();

    public FriendsHUD() { super("Friends List", 350, 80); }

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
        int maxWidth = fr.getStringWidth("Friends List");
        boolean foundFriend = false;

        Set<String> renderedFriends = new HashSet<>();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (!(player instanceof EntityOtherPlayerMP)) continue;
            EntityOtherPlayerMP other = (EntityOtherPlayerMP) player;
            String name = other.getName();

            if (!isFriend(name)) continue;
            if (renderedFriends.contains(name.toLowerCase())) continue;
            renderedFriends.add(name.toLowerCase());

            if (!foundFriend) {
                fr.drawStringWithShadow(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "Friends List:", 0, currentY, 0xFFFFFF);
                currentY += fr.FONT_HEIGHT + 2;
                foundFriend = true;
            }

            String displayName;
            String rawFormatted = other.getDisplayName().getFormattedText();
            int nameIndex = rawFormatted.indexOf(name);
            if (nameIndex >= 0) {
                displayName = rawFormatted.substring(0, nameIndex + name.length());
            } else {
                displayName = EnumChatFormatting.GRAY + "[?] " + EnumChatFormatting.GREEN + name;
            }

            displayName = EnumChatFormatting.DARK_GREEN + "[" + EnumChatFormatting.GREEN + "F" + EnumChatFormatting.DARK_GREEN + "] " + EnumChatFormatting.RESET + displayName;

            String gear = getShortEnchants(other);
            String dist = SpawnRegions.getLocationFormat(mc.thePlayer, other);

            String fullLine = displayName + EnumChatFormatting.GRAY + " - " + gear + EnumChatFormatting.GRAY + " - " + dist;
            fr.drawStringWithShadow(fullLine, 0, currentY, 0xFFFFFF);

            int lineWidth = fr.getStringWidth(fullLine);
            if (lineWidth > maxWidth) maxWidth = lineWidth;
            currentY += fr.FONT_HEIGHT;
        }

        if (!foundFriend) {
            if (isEditing) {
                fr.drawStringWithShadow(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "Friends List:", 0, currentY, 0xFFFFFF);
                currentY += fr.FONT_HEIGHT + 2;
                String placeholder = EnumChatFormatting.DARK_GREEN + "[" + EnumChatFormatting.GREEN + "F" + EnumChatFormatting.DARK_GREEN + "] " + EnumChatFormatting.GRAY + "[120] Player" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
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
            case "respawn_absorption": return EnumChatFormatting.GOLD + "Abs";
            case "mirror": return EnumChatFormatting.WHITE + "Mirror";
            case "critically_funky": return EnumChatFormatting.DARK_AQUA + "Crit Funky";
            case "venom": case "combo_venom": return EnumChatFormatting.DARK_PURPLE + "Venom";
            case "mind_assault": return EnumChatFormatting.DARK_PURPLE + "Assaults";
            case "solitude": return EnumChatFormatting.RED + "Soli";
            case "protection": return EnumChatFormatting.BLUE + "Prot";
            case "fractional_reserve": return EnumChatFormatting.BLUE + "Frac";
            case "not_gladiator": return EnumChatFormatting.BLUE + "Glad";
            default: return null;
        }
    }

    public static boolean isFriend(String name) {
        return name != null && friendsList.stream().anyMatch(t -> t.equalsIgnoreCase(name));
    }
}