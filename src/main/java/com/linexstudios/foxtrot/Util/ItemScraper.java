package com.linexstudios.foxtrot.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ItemScraper {

    public static class Enchant {
        public String key;
        public int level;
        public Enchant(String key, int level) { this.key = key; this.level = level; }
    }

    public static class Pant {
        public Set<Enchant> enchants = new HashSet<>();
        public boolean gem = false;
        public int maxLive = 0;
        public int nonce = 0;
        public String customEnchantsString = ""; 
    }

    // --- NONCE LOGIC ---
    public static ArrayList<Integer> getNoncesFromPlayer(EntityPlayer player) {
        ArrayList<Integer> nonces = new ArrayList<>();
        if (player == null) return nonces;

        ArrayList<ItemStack> items = new ArrayList<>();
        items.add(player.getHeldItem());
        items.add(player.inventory.armorInventory[0]); // Boots
        items.add(player.inventory.armorInventory[1]); // Leggings (Pants)
        items.add(player.inventory.armorInventory[2]); // Chestplate
        items.add(player.inventory.armorInventory[3]); // Helmet

        for (ItemStack item : items) {
            int nonce = getNonce(item);
            
            // STRICT FILTER: Ignores Nonce 0, 1, 2, 3, 4, 5, 6, 7, 8, and 9 (Rage Pants)
            // Only accepts valid large-digit nonces (e.g., 680163832)
            if (nonce > 9) { 
                nonces.add(nonce);
            }
        }
        return nonces;
    }

    public static int getNonce(ItemStack item) {
        if (item == null || !item.hasTagCompound()) return 0;
        NBTTagCompound extra = item.getTagCompound().getCompoundTag("ExtraAttributes");
        if (extra != null && extra.hasKey("Nonce")) {
            return extra.getInteger("Nonce");
        }
        return 0;
    }

    // --- ENCHANT LOGIC ---
    public static Pant getPantFromName(String name) {
        Pant pant = new Pant();
        if (Minecraft.getMinecraft().theWorld == null) return pant;
        EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);
        if (player == null) return pant;

        ItemStack leggings = player.inventory.armorInventory[1];
        if (leggings == null || !leggings.hasTagCompound()) return pant;

        NBTTagCompound extra = leggings.getTagCompound().getCompoundTag("ExtraAttributes");
        if (extra == null) return pant;

        pant.nonce = extra.hasKey("Nonce") ? extra.getInteger("Nonce") : 0;
        pant.gem = extra.hasKey("UpgradeGemsUses") || (extra.hasKey("UpgradeTier") && extra.getInteger("UpgradeTier") == 3);
        pant.maxLive = extra.hasKey("MaxLives") ? extra.getInteger("MaxLives") : 0;

        if (extra.hasKey("CustomEnchants")) {
            NBTTagList enchantsList = extra.getTagList("CustomEnchants", 10);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < enchantsList.tagCount(); i++) {
                NBTTagCompound enchTag = enchantsList.getCompoundTagAt(i);
                String key = enchTag.getString("Key");
                int level = enchTag.getInteger("Level");
                pant.enchants.add(new Enchant(key, level));
                
                if (sb.length() > 0) sb.append(",");
                sb.append(key).append(level);
            }
            pant.customEnchantsString = sb.toString();
        }
        return pant;
    }

    public static String getCompoundEnchants(Pant pant) {
        return pant.customEnchantsString;
    }
}