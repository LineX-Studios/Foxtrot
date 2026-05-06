package com.linexstudios.foxtrot.Util;

import com.linexstudios.foxtrot.Handler.PitDataHandler;
import net.minecraft.util.EnumChatFormatting;

public class EnchantData {
    private final String rawKey;
    private final int level;

    public EnchantData(String key, int level) {
        this.rawKey = key;
        this.level = level;
    }

    public String getFormattedName() {
        if (this.rawKey == null || this.rawKey.trim().isEmpty())
            return null;

        PitDataHandler.MysticData mystic = PitDataHandler.getMystic(this.rawKey);
        if (mystic != null) {
            String name = mystic.Name;
            // The JSON name often includes color codes like "§9" or "§dRARE! §9"
            // We append the level in Roman numerals
            return name + " " + toRoman(level);
        }

        // Fallback for some hardcoded keys if not in JSON (unlikely but safe)
        String cleanName = "";
        String color = EnumChatFormatting.AQUA.toString();

        switch (this.rawKey.toLowerCase()) {
            case "billionaire": cleanName = "Bill"; color = EnumChatFormatting.GOLD.toString(); break;
            case "combo_perun": cleanName = "Perun"; color = EnumChatFormatting.YELLOW.toString(); break;
            case "executioner": cleanName = "Exec"; color = EnumChatFormatting.RED.toString(); break;
            case "regularity": cleanName = "Reg"; color = EnumChatFormatting.DARK_RED.toString(); break;
            case "solitude": cleanName = "Soli"; color = EnumChatFormatting.RED.toString(); break;
            default: return null;
        }

        return color + cleanName + " " + toRoman(level);
    }

    private String toRoman(int num) {
        String[] roman = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };
        return (num > 0 && num <= 10) ? roman[num - 1] : String.valueOf(num);
    }

    public int getLevel() {
        return this.level;
    }
}