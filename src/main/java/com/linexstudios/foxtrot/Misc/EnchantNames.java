package com.linexstudios.foxtrot.Misc;

import com.linexstudios.foxtrot.Util.EnchantData;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class EnchantNames {

    public static final EnchantNames instance = new EnchantNames();
    public static boolean enabled = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!enabled || event.itemStack == null) return;
        
        String enchants = getEnchantsString(event.itemStack);
        if (enchants != null && !enchants.isEmpty()) {
            event.toolTip.set(0, enchants); 
        }
    }

    public static String getEnchantsString(ItemStack itemStack) {
        if (!enabled || itemStack == null || itemStack.getItem() == null || !itemStack.hasTagCompound()) return null;

        // ONLY process Swords and Pants (Armor Slot 2 = Leggings)
        boolean isSword = itemStack.getItem() instanceof ItemSword;
        boolean isPants = itemStack.getItem() instanceof ItemArmor && ((ItemArmor) itemStack.getItem()).armorType == 2;
        if (!isSword && !isPants) return null;

        NBTTagCompound tag = itemStack.getTagCompound();
        if (!tag.hasKey("ExtraAttributes")) return null;

        NBTTagCompound extra = tag.getCompoundTag("ExtraAttributes");
        if (!extra.hasKey("CustomEnchants")) return null;

        NBTTagList enchants = extra.getTagList("CustomEnchants", 10);
        if (enchants.tagCount() == 0) return null;

        List<String> enchantNames = new ArrayList<>();
        for (int i = 0; i < enchants.tagCount(); i++) {
            
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            String rawKey = enchant.getString("Key");
            int level = enchant.getInteger("Level");
            
            EnchantData data = new EnchantData(rawKey, level);
            String formatted = data.getFormattedName();

            if (formatted != null && !formatted.isEmpty() && !formatted.contains("null")) { // return null (empty) if enchant not in enchantdata.java
                // limit to only show 4 enchants only
                if (enchantNames.size() >= 4) break;
                enchantNames.add(formatted);
            }
        }

        // Return null entirely if NO valid enchants were found (prevents empty grey boxes)
        if (enchantNames.isEmpty()) return null;

        return String.join(EnumChatFormatting.GRAY + " \u2502 ", enchantNames);
    }

    public static void clearCache() {}
}