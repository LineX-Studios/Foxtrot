package com.linexstudios.foxtrot.Util;

import net.minecraft.util.EnumChatFormatting;

public class EnchantData {
    private final String rawKey;
    private final int level;

    public EnchantData(String key, int level) {
        this.rawKey = key;
        this.level = level;
    }

    public String getFormattedName() {
        if (this.rawKey == null || this.rawKey.trim().isEmpty()) return null;

        String cleanName = "";
        String color = EnumChatFormatting.AQUA.toString(); // Default color for common enchants

        switch(this.rawKey.toLowerCase()) {
            // RARE SWORDS
            case "billionaire": cleanName = "Bill"; color = EnumChatFormatting.GOLD.toString(); break;
            case "combo_perun": cleanName = "Perun"; color = EnumChatFormatting.YELLOW.toString(); break;
            case "executioner": cleanName = "Exec"; color = EnumChatFormatting.RED.toString(); break;
            case "combo_stun": cleanName = "Stun"; color = EnumChatFormatting.YELLOW.toString(); break;
            case "gambler": cleanName = "Gamble"; color = EnumChatFormatting.GOLD.toString(); break;
            case "lifesteal": cleanName = "LS"; color = EnumChatFormatting.RED.toString(); break;
            
            // COMMON SWORDS
            case "punisher": cleanName = "Pun"; break;
            case "beat_the_spammers": cleanName = "BTS"; break;
            case "bounty_reaper": cleanName = "Reaper"; break;
            case "bruiser": cleanName = "Bruiser"; break;
            case "bullet_time": cleanName = "Bullet"; break;
            case "crush": cleanName = "Crush"; break;
            case "diamond_stomp": cleanName = "Stomp"; break;
            case "duelist": cleanName = "Duelist"; break;
            case "fancy": cleanName = "Fancy"; break;
            case "frostbite": cleanName = "Frost"; break;
            case "gold_and_boosted": cleanName = "GAB"; break;
            case "gold_bump": cleanName = "Bump"; break;
            case "grasshopper": cleanName = "Grass"; break;
            case "guts": cleanName = "Guts"; break;
            case "heal": cleanName = "Heal"; break;
            case "kings_quest": cleanName = "KQ"; break;
            case "knockback": cleanName = "KB"; break;
            case "moctezuma": cleanName = "Moct"; break;
            case "pain_focus": cleanName = "PF"; break;
            case "revitalize": cleanName = "Rev"; break;
            case "shark": cleanName = "Shark"; break;
            case "sharp": cleanName = "Sharp"; break;
            case "smash": cleanName = "Smash"; break;
            case "speedy_hit": cleanName = "Speedy"; break;
            case "combo_damage": cleanName = "Strike"; break;
            case "combo_swift": cleanName = "Swift"; break;
            case "sweaty": cleanName = "Sweaty"; break;

            // RARE BOWS
            case "megalongbow": cleanName = "Mega"; color = EnumChatFormatting.LIGHT_PURPLE.toString(); break;
            case "telebow": cleanName = "Telebow"; color = EnumChatFormatting.DARK_PURPLE.toString(); break;
            case "robin_hood": cleanName = "Robin"; color = EnumChatFormatting.BLUE.toString(); break;
            case "volley": cleanName = "Volley"; color = EnumChatFormatting.WHITE.toString(); break;
            case "explosive": cleanName = "Explosive"; color = EnumChatFormatting.RED.toString(); break;
            case "true_shot": cleanName = "True Shot"; color = EnumChatFormatting.RED.toString(); break;
            case "lucky_shot": cleanName = "Lucky"; color = EnumChatFormatting.RED.toString(); break;

            // COMMON BOWS
            case "chipping": cleanName = "Chip"; break;
            case "fletching": cleanName = "Fletch"; break;
            case "jumpspammer": cleanName = "Jump"; break;
            case "parasite": cleanName = "Para"; break;
            case "pin_down": cleanName = "Pin"; break;
            case "push_comes_to_shove": cleanName = "PCTS"; break;
            case "sprintsmash": cleanName = "Sprint"; break;
            case "wasp": cleanName = "Wasp"; break;
            case "pullbow": cleanName = "Pull"; break;

            // PANTS
            case "regularity": cleanName = "Reg"; color = EnumChatFormatting.DARK_RED.toString(); break;
            case "solitude": cleanName = "Soli"; color = EnumChatFormatting.RED.toString(); break;
            case "singularity": cleanName = "Singu"; color = EnumChatFormatting.DARK_GREEN.toString(); break;
            case "fractional_reserve": cleanName = "Frac"; color = EnumChatFormatting.BLUE.toString(); break;
            case "critically_funky": cleanName = "Crit Funky"; color = EnumChatFormatting.DARK_AQUA.toString(); break;
            case "protection": cleanName = "Prot"; color = EnumChatFormatting.BLUE.toString(); break;
            case "gottagofast": cleanName = "GTGF"; color = EnumChatFormatting.AQUA.toString(); break; 
            case "immune_true_damage":
            case "reflection":
            case "mirror": cleanName = "Mirror"; color = EnumChatFormatting.WHITE.toString(); break;
            case "respawn_absorption":
            case "respawn_with_absorption": cleanName = "Abs"; color = EnumChatFormatting.GOLD.toString(); break;
            case "mind_assault": cleanName = "Mind Assaults"; color = EnumChatFormatting.DARK_PURPLE.toString(); break;
            case "combo_venom": case "venom": cleanName = "Venom"; color = EnumChatFormatting.DARK_PURPLE.toString(); break;    

            // PANTS
            case "booboo": cleanName = "Booboo"; break;
            case "crickets": cleanName = "Crickets"; break;
            case "danger_close": cleanName = "Danger Close"; break;
            case "david_and_goliath": cleanName = "D&G"; break;
            case "electrolytes": cleanName = "Electro"; break;
            case "escape_pod": cleanName = "Pod"; color = EnumChatFormatting.DARK_RED.toString(); break;
            case "hearts": cleanName = "Hearts"; color = EnumChatFormatting.RED.toString(); break;
            case "instaboom": cleanName = "Boom"; break;
            case "last_stand": cleanName = "LS"; break;
            case "mcswimmer": cleanName = "Swim"; break;
            case "not_gladiator": cleanName = "Glad"; color = EnumChatFormatting.BLUE.toString(); break;
            case "pebble": cleanName = "Pebble"; break;
            case "peroxide": cleanName = "Pero"; color = EnumChatFormatting.RED.toString(); break;
            case "prick": cleanName = "Prick"; color = EnumChatFormatting.RED.toString(); break;
            case "ring_armor": cleanName = "Ring"; break;
            case "tnt": cleanName = "TNT"; break;
            case "damage_reduction": cleanName = "Huntsman"; break;

            // IF THE ENCHANT IS NOT IN THIS LIST IGNORE IT ENTIRELY
            default: 
                return null; 
        }
        
        if (cleanName.isEmpty()) return null;

        return color + cleanName + " " + toRoman(level);
    }

    private String toRoman(int num) {
        String[] roman = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (num > 0 && num <= 10) ? roman[num - 1] : String.valueOf(num);
    }

    public int getLevel() {
        return this.level;
    }
}