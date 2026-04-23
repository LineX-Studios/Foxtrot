package com.linexstudios.foxtrot.Util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.HashMap;
import java.util.Map;

public class MysticItem {
    public final Map<EnchantData, Integer> enchants = new HashMap<>();

    public MysticItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return;

        NBTTagCompound tag = itemStack.getTagCompound();
        if (!tag.hasKey("ExtraAttributes")) return;

        NBTTagCompound extraAttributes = tag.getCompoundTag("ExtraAttributes");
        if (!extraAttributes.hasKey("CustomEnchants")) return;

        NBTTagList enchantList = extraAttributes.getTagList("CustomEnchants", 10);
        for (int i = 0; i < enchantList.tagCount(); i++) {
            NBTTagCompound enchantTag = enchantList.getCompoundTagAt(i);
            String key = enchantTag.getString("Key");
            int level = enchantTag.getInteger("Level");

            this.enchants.put(new EnchantData(key, level), level);
        }
    }
}