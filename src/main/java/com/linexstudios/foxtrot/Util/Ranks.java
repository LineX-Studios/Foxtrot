package com.linexstudios.foxtrot.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ranks {

    public static final Ranks instance = new Ranks();
    private final Minecraft mc = Minecraft.getMinecraft();

    // ==========================================
    //              MODULE SETTINGS
    // ==========================================
    public static boolean isEnabled = true;

    public static boolean changeName = false;
    public static String targetName = "";

    public static boolean changeLevel = true;
    public static int targetLevel = 120;

    public static boolean changePrestige = true;
    public static int targetPrestige = 30; 

    public static boolean changeRank = true;
    public static String targetRank = "admin"; 

    public static boolean hideLobby = true;

    private boolean wasEnabled = false;
    private String originalScoreboardTitle = null;
    private String originalTabTeam = null;

    public boolean isInPit() {
        if (mc.theWorld == null || mc.thePlayer == null) return false;
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return false;
        
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;
        
        String title = StringUtils.stripControlCodes(objective.getDisplayName());
        return title.contains("THE HYPIXEL PIT") || title.contains("PIT");
    }

    // ==========================================
    //                 CHAT REPLACER
    // ==========================================
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!isEnabled || !isInPit() || mc.thePlayer == null) return;

        String originalMessage = event.message.getFormattedText();
        String unformattedMessage = StringUtils.stripControlCodes(originalMessage);
        String realName = mc.thePlayer.getName();
        
        String displayUsername = (changeName && targetName != null && !targetName.isEmpty()) ? targetName : realName;

        if (unformattedMessage.contains(realName)) {
            
            // 1. Check if the player is the AUTHOR of this chat message
            int colonIdx = unformattedMessage.indexOf(':');
            boolean isAuthor = colonIdx != -1 && colonIdx < 60 && unformattedMessage.substring(0, colonIdx).contains(realName);

            if (isAuthor) {
                boolean isNetwork = isNetworkOrPrivateChat(unformattedMessage);

                // Match everything before the name, the name itself, the colon separator, and the message
                String nameRegex = "(?:\\u00A7[0-9a-fk-or])*" + realName;
                Matcher m = Pattern.compile("^(.*?)(" + nameRegex + ")((?:\\u00A7[0-9a-fk-or])*\\s*\\:)(.*)$").matcher(originalMessage);
                
                if (m.find()) {
                    String prefixArea = m.group(1);
                    String colonArea = m.group(3); 
                    String messageArea = m.group(4);
                    
                    String strippedPrefix = prefixArea;
                    
                    // Strip the Rank Bracket (Always the last bracket before the name)
                    if (changeRank) {
                        strippedPrefix = strippedPrefix.replaceAll("(?:\\u00A7[0-9a-fk-or])*\\[[^\\]]+\\](?:\\u00A7[0-9a-fk-or]|\\s)*$", "");
                    }
                    
                    // Strip the Pit Bracket (Which becomes the new last bracket if not in a network chat)
                    if (!isNetwork && (changeLevel || changePrestige)) {
                        strippedPrefix = strippedPrefix.replaceAll("(?:\\u00A7[0-9a-fk-or])*\\[[^\\]]+\\](?:\\u00A7[0-9a-fk-or]|\\s)*$", "");
                    }
                    
                    StringBuilder customPrefix = new StringBuilder(strippedPrefix);
                    
                    // Append Custom Pit Level
                    if (!isNetwork && (changeLevel || changePrestige)) {
                        customPrefix.append(getCustomChatPitBracket()).append(" ");
                    }
                    // Append Custom Rank
                    if (changeRank) {
                        customPrefix.append(getCustomRankPrefix());
                    }
                    
                    // Append Name and Colon
                    customPrefix.append(getRankColor(targetRank)).append(displayUsername).append(colonArea);
                    
                    // Force the message to adopt the rank's chat color
                    if (changeRank) {
                        customPrefix.append(getChatColor(targetRank));
                    }
                    
                    // Process any mentions of your name INSIDE your own message so they highlight in your rank color
                    String mentionRegex = "(?<!\\w)(?:\\u00A7[0-9a-fk-or])*" + realName + "(?!\\w)";
                    String mentionReplacement = getRankColor(targetRank) + displayUsername + EnumChatFormatting.RESET;
                    messageArea = messageArea.replaceAll(mentionRegex, mentionReplacement);
                    
                    event.message = new ChatComponentText(customPrefix.toString() + messageArea);
                    return;
                }
            }

            // 3. SAFE MENTION FALLBACK (If someone else mentions you, or a killfeed)
            // This safely swaps just the text of the name and applies the rank color!
            String fallbackRegex = "(?<!\\w)(?:\\u00A7[0-9a-fk-or])*" + realName + "(?!\\w)";
            String simpleReplacement = getRankColor(targetRank) + displayUsername + EnumChatFormatting.RESET;
            
            String replacedMessage = originalMessage.replaceAll(fallbackRegex, simpleReplacement);
            
            if (!originalMessage.equals(replacedMessage)) {
                event.message = new ChatComponentText(replacedMessage);
            }
        }
    }

    private boolean isNetworkOrPrivateChat(String unformattedMessage) {
        if (unformattedMessage == null) return false;
        String msg = unformattedMessage.trim();
        return msg.startsWith("Party >") || 
               msg.startsWith("Guild >") || 
               msg.startsWith("Officer >") || 
               msg.startsWith("Co-op >") ||
               msg.startsWith("Friend >") ||
               msg.startsWith("From ") ||
               msg.startsWith("To ");
    }

    // ==========================================
    //         TAB & SCOREBOARD REPLACER
    // ==========================================
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null) return;

        boolean pit = isInPit();

        if (isEnabled && pit) {
            wasEnabled = true;
            String realName = mc.thePlayer.getName();
            String displayUsername = (changeName && targetName != null && !targetName.isEmpty()) ? targetName : realName;

            // 1. Replace in Tab List
            for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                if (playerInfo.getGameProfile().getName().equals(realName)) {
                    String tabName = getRankColor(targetRank) + displayUsername; 
                    if (changeLevel || changePrestige) {
                        tabName = getCustomTabPitBracket() + " " + tabName;
                    }
                    playerInfo.setDisplayName(new ChatComponentText(tabName));
                }
            }

            if (mc.theWorld != null) {
                Scoreboard scoreboard = mc.theWorld.getScoreboard();
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                
                if (objective != null) {
                    // 2. Hide Lobby Name
                    if (hideLobby) {
                        String currentTitle = objective.getDisplayName();
                        String customTitle = EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "THE HYPIXEL PIT";
                        if (!currentTitle.equals(customTitle)) {
                            originalScoreboardTitle = currentTitle;
                            objective.setDisplayName(customTitle);
                        }
                    }

                    // 3. SCOREBOARD PRESTIGE INJECTOR
                    if (changePrestige) {
                        boolean hasPrestigeLine = false;
                        int levelScorePoints = -1;
                        Score existingBlankScore = null;
                        
                        for (Score score : scoreboard.getSortedScores(objective)) {
                            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                            if (team != null) {
                                String clean = StringUtils.stripControlCodes(team.formatString(""));
                                if (clean.contains("Prestige:")) hasPrestigeLine = true;
                                if (clean.contains("Level:")) levelScorePoints = score.getScorePoints();
                                
                                if (clean.trim().isEmpty() && score.getScorePoints() == levelScorePoints + 1) {
                                    existingBlankScore = score;
                                }
                            }
                        }
                        
                        String fakePlayer = EnumChatFormatting.BLACK + "" + EnumChatFormatting.RESET;
                        
                        if (targetPrestige > 0) {
                            if (!hasPrestigeLine && levelScorePoints != -1) {
                                if (existingBlankScore != null) existingBlankScore.setScorePoints(levelScorePoints + 2);
                                
                                Score fakeScore = scoreboard.getValueFromObjective(fakePlayer, objective);
                                fakeScore.setScorePoints(levelScorePoints + 1);
                                
                                ScorePlayerTeam fakeTeam = scoreboard.getTeam("FakePrestige");
                                if (fakeTeam == null) fakeTeam = scoreboard.createTeam("FakePrestige");
                                
                                fakeTeam.setNamePrefix(EnumChatFormatting.WHITE + "Prestige: " + EnumChatFormatting.YELLOW + toRoman(targetPrestige));
                                scoreboard.addPlayerToTeam(fakePlayer, "FakePrestige");
                                
                            } else if (hasPrestigeLine) {
                                ScorePlayerTeam fakeTeam = scoreboard.getTeam("FakePrestige");
                                if (fakeTeam != null) {
                                    fakeTeam.setNamePrefix(EnumChatFormatting.WHITE + "Prestige: " + EnumChatFormatting.YELLOW + toRoman(targetPrestige));
                                }
                            }
                        } else {
                            scoreboard.removeObjectiveFromEntity(fakePlayer, null);
                            ScorePlayerTeam fakeTeam = scoreboard.getTeam("FakePrestige");
                            if (fakeTeam != null) scoreboard.removeTeam(fakeTeam);
                        }
                    }
                }

                // 4. Dynamic Tablist Hierarchy
                if (changeLevel || changePrestige) {
                    ScorePlayerTeam currentTeam = scoreboard.getPlayersTeam(realName);
                    
                    char prestigeChar = (char) ('A' + (50 - Math.max(0, Math.min(50, targetPrestige))));
                    int lIndex = 120 - Math.max(0, Math.min(120, targetLevel));
                    String sortKey = String.format("!%c%03d", prestigeChar, lIndex); 
                    
                    String customTeamName = "Fx" + sortKey;

                    if (currentTeam != null && !currentTeam.getRegisteredName().startsWith("Fx")) {
                        originalTabTeam = currentTeam.getRegisteredName();
                    }
                    
                    ScorePlayerTeam customTeam = scoreboard.getTeam(customTeamName);
                    if (customTeam == null) {
                        customTeam = scoreboard.createTeam(customTeamName);
                    }
                    
                    customTeam.setNamePrefix(EnumChatFormatting.RESET + "");
                    customTeam.setNameSuffix("");
                    
                    scoreboard.addPlayerToTeam(realName, customTeamName);
                }
            }
        } 
        else if (wasEnabled) {
            wasEnabled = false;
            String realName = mc.thePlayer.getName();

            if (mc.getNetHandler() != null) {
                for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                    if (playerInfo.getGameProfile().getName().equals(realName)) {
                        playerInfo.setDisplayName(null);
                    }
                }
            }

            if (mc.theWorld != null) {
                Scoreboard scoreboard = mc.theWorld.getScoreboard();
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                
                if (objective != null && originalScoreboardTitle != null) {
                    objective.setDisplayName(originalScoreboardTitle);
                }

                if (originalTabTeam != null && !originalTabTeam.isEmpty()) {
                    scoreboard.addPlayerToTeam(realName, originalTabTeam);
                }

                String fakePlayer = EnumChatFormatting.BLACK + "" + EnumChatFormatting.RESET;
                scoreboard.removeObjectiveFromEntity(fakePlayer, null);
                ScorePlayerTeam fakeTeam = scoreboard.getTeam("FakePrestige");
                if (fakeTeam != null) scoreboard.removeTeam(fakeTeam);
            }
        }
    }

    // ==========================================
    //              XP MATH ENGINE
    // ==========================================
    public String getSpoofedNeededXP() {
        int baseXP = getBaseXp(targetLevel);
        double multiplier = getPrestigeMultiplier(targetPrestige);
        
        long neededXP = Math.round(baseXP * multiplier);
        return String.format("%,d", neededXP);
    }

    private int getBaseXp(int level) {
        if (level < 10) return 15;
        if (level < 20) return 30;
        if (level < 30) return 50;
        if (level < 40) return 75;
        if (level < 50) return 125;
        if (level < 60) return 300;
        if (level < 70) return 600;
        if (level < 80) return 800;
        if (level < 90) return 900;
        if (level < 100) return 1000;
        if (level < 110) return 1200;
        if (level < 120) return 1500;
        return 0; 
    }

    private double getPrestigeMultiplier(int prestige) {
        switch (prestige) {
            case 0: return 1.0;
            case 1: return 1.1; case 2: return 1.2; case 3: return 1.3; case 4: return 1.4; case 5: return 1.5;
            case 6: return 1.75;
            case 7: return 2.0; case 8: return 2.5; case 9: return 3.0;
            case 10: return 4.0; case 11: return 5.0; case 12: return 6.0; case 13: return 7.0; case 14: return 8.0; case 15: return 9.0; case 16: return 10.0;
            case 17: return 12.0; case 18: return 14.0; case 19: return 16.0; case 20: return 18.0; case 21: return 20.0;
            case 22: return 24.0; case 23: return 28.0; case 24: return 32.0; case 25: return 36.0; case 26: return 40.0;
            case 27: return 45.0; case 28: return 50.0;
            case 29: return 75.0;
            case 30: return 100.0;
            case 31: case 32: case 33: case 34: case 35: return 101.0;
            case 36: return 200.0; case 37: return 300.0; case 38: return 400.0; case 39: return 500.0;
            case 40: return 750.0;
            case 41: return 1000.0; case 42: return 1250.0; case 43: return 1500.0; case 44: return 1750.0; case 45: return 2000.0;
            case 46: return 3000.0;
            case 47: return 5000.0;
            case 48: return 10000.0;
            case 49: return 50000.0;
            case 50: return 100000.0;
            default: return 1.0;
        }
    }

    // ==========================================
    //             UTILITY & FORMATTING
    // ==========================================
    public String getCustomChatPitBracket() {
        EnumChatFormatting prestigeColor = getPrestigeColor(targetPrestige);
        EnumChatFormatting levelColor = getLevelColor(targetLevel);
        
        StringBuilder bracket = new StringBuilder();
        bracket.append(prestigeColor).append("[");
        
        if (targetPrestige > 0 && changePrestige) {
            bracket.append(EnumChatFormatting.YELLOW).append(toRoman(targetPrestige)).append(prestigeColor).append("-");
        }
        
        if (changeLevel) {
            bracket.append(levelColor).append(targetLevel);
        }
        bracket.append(prestigeColor).append("]");
        return bracket.toString();
    }

    public String getCustomTabPitBracket() {
        EnumChatFormatting prestigeColor = getPrestigeColor(targetPrestige);
        EnumChatFormatting levelColor = getLevelColor(targetLevel);
        
        StringBuilder bracket = new StringBuilder();
        bracket.append(prestigeColor).append("[");
        if (changeLevel) {
            bracket.append(levelColor).append(targetLevel);
        }
        bracket.append(prestigeColor).append("]");
        return bracket.toString();
    }

    public String getCustomRankPrefix() {
        switch (targetRank.toLowerCase()) {
            case "vip":     return EnumChatFormatting.GREEN + "[VIP] ";
            case "vip+":    return EnumChatFormatting.GREEN + "[VIP" + EnumChatFormatting.GOLD + "+" + EnumChatFormatting.GREEN + "] ";
            case "mvp":     return EnumChatFormatting.AQUA + "[MVP] ";
            case "mvp+":    return EnumChatFormatting.AQUA + "[MVP" + EnumChatFormatting.RED + "+" + EnumChatFormatting.AQUA + "] ";
            case "mvp++":   return EnumChatFormatting.GOLD + "[MVP" + EnumChatFormatting.RED + "++" + EnumChatFormatting.GOLD + "] ";
            case "youtube": return EnumChatFormatting.RED + "[" + EnumChatFormatting.WHITE + "YOUTUBE" + EnumChatFormatting.RED + "] ";
            case "staff":   return EnumChatFormatting.RED + "[" + EnumChatFormatting.GOLD + "\u12DE" + EnumChatFormatting.RED + "] ";
            case "admin":   return EnumChatFormatting.RED + "[ADMIN] ";
            case "none":
            default:        return ""; 
        }
    }

    public EnumChatFormatting getRankColor(String rank) {
        switch (rank.toLowerCase()) {
            case "vip": 
            case "vip+": return EnumChatFormatting.GREEN;
            case "mvp": 
            case "mvp+": return EnumChatFormatting.AQUA;
            case "mvp++": return EnumChatFormatting.GOLD;
            case "admin":
            case "youtube": 
            case "staff": return EnumChatFormatting.RED;
            case "none":
            default: return EnumChatFormatting.GRAY;
        }
    }

    public EnumChatFormatting getChatColor(String rank) {
        if (rank.equalsIgnoreCase("none")) return EnumChatFormatting.GRAY;
        return EnumChatFormatting.WHITE;
    }

    public String toRoman(int num) {
        int[] values = {100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] romanLetters = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                num -= values[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }

    public EnumChatFormatting getPrestigeColor(int prestige) {
        if (prestige >= 50) return EnumChatFormatting.DARK_GRAY;  
        if (prestige >= 48) return EnumChatFormatting.DARK_RED;   
        if (prestige >= 45) return EnumChatFormatting.BLACK;      
        if (prestige >= 40) return EnumChatFormatting.DARK_BLUE;  
        if (prestige >= 35) return EnumChatFormatting.AQUA;       
        if (prestige >= 30) return EnumChatFormatting.WHITE;      
        if (prestige >= 25) return EnumChatFormatting.LIGHT_PURPLE; 
        if (prestige >= 20) return EnumChatFormatting.DARK_PURPLE; 
        if (prestige >= 15) return EnumChatFormatting.RED;        
        if (prestige >= 10) return EnumChatFormatting.GOLD;       
        if (prestige >= 5)  return EnumChatFormatting.YELLOW;     
        if (prestige >= 1)  return EnumChatFormatting.BLUE;       
        return EnumChatFormatting.GRAY;                           
    }

    public EnumChatFormatting getLevelColor(int level) {
        if (level >= 120) return EnumChatFormatting.AQUA;
        if (level >= 110) return EnumChatFormatting.WHITE;
        if (level >= 100) return EnumChatFormatting.LIGHT_PURPLE;
        if (level >= 90) return EnumChatFormatting.DARK_PURPLE; 
        if (level >= 80) return EnumChatFormatting.DARK_RED;
        if (level >= 70) return EnumChatFormatting.RED;
        if (level >= 60) return EnumChatFormatting.GOLD;
        if (level >= 50) return EnumChatFormatting.YELLOW;
        if (level >= 40) return EnumChatFormatting.GREEN;
        if (level >= 30) return EnumChatFormatting.DARK_GREEN;
        if (level >= 20) return EnumChatFormatting.DARK_AQUA;
        if (level >= 10) return EnumChatFormatting.BLUE;
        return EnumChatFormatting.GRAY;
    }
}