package com.linexstudios.foxtrot.Util;

import com.linexstudios.foxtrot.Handler.MapDetectionHandler;
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

    public static boolean isEnabled = true;
    public static boolean changeName = false;
    public static String targetName = "";
    public static boolean changeLevel = true;
    public static int targetLevel = 120;
    public static boolean changePrestige = true;
    public static int targetPrestige = 30; 
    public static boolean changeRank = true;
    public static String targetRank = "owner"; 
    public static boolean hideLobby = true;

    private boolean wasEnabled = false;
    private String originalScoreboardTitle = null;
    private String originalTabTeam = null;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!isEnabled || !MapDetectionHandler.isInPit() || mc.thePlayer == null) return;

        String originalMessage = event.message.getFormattedText();
        String unformattedMessage = StringUtils.stripControlCodes(originalMessage);
        String realName = mc.thePlayer.getName();
        
        String displayUsername = (changeName && targetName != null && !targetName.isEmpty()) ? targetName : realName;

        if (unformattedMessage.contains(realName)) {
            
            int colonIdx = unformattedMessage.indexOf(':');
            boolean isAuthor = colonIdx != -1 && colonIdx < 60 && unformattedMessage.substring(0, colonIdx).contains(realName);

            if (isAuthor) {
                boolean isNetwork = isNetworkOrPrivateChat(unformattedMessage);
                String nameRegex = "(?:\\u00A7[0-9a-fk-or])*" + realName;
                Matcher m = Pattern.compile("^(.*?)(" + nameRegex + ")((?:\\u00A7[0-9a-fk-or])*\\s*\\:)(.*)$").matcher(originalMessage);
                
                if (m.find()) {
                    String prefixArea = m.group(1);
                    String colonArea = m.group(3); 
                    String messageArea = m.group(4);
                    
                    String strippedPrefix = prefixArea;
                    
                    if (changeRank) {
                        strippedPrefix = strippedPrefix.replaceAll("(?:\\u00A7[0-9a-fk-or])*\\[[^\\]]+\\](?:\\u00A7[0-9a-fk-or]|\\s)*$", "");
                    }
                    if (!isNetwork && (changeLevel || changePrestige)) {
                        strippedPrefix = strippedPrefix.replaceAll("(?:\\u00A7[0-9a-fk-or])*\\[[^\\]]+\\](?:\\u00A7[0-9a-fk-or]|\\s)*$", "");
                    }
                    
                    StringBuilder customPrefix = new StringBuilder(strippedPrefix);
                    
                    if (!isNetwork && (changeLevel || changePrestige)) customPrefix.append(getCustomChatPitBracket()).append(" ");
                    if (changeRank) customPrefix.append(getCustomRankPrefix());
                    
                    customPrefix.append(getRankColor(targetRank)).append(displayUsername).append(colonArea);
                    if (changeRank) customPrefix.append(getChatColor(targetRank));
                    
                    // Allow normal messages to just map the name without wiping colors
                    if (changeName) {
                        messageArea = messageArea.replace(realName, displayUsername);
                    }
                    
                    event.message = new ChatComponentText(customPrefix.toString() + messageArea);
                    return;
                }
            }

            // FIX: If someone else is speaking, let Hypixel keep its native Yellow highlight on mentions
            if (changeName) {
                String replacedMessage = originalMessage.replace(realName, displayUsername);
                if (!originalMessage.equals(replacedMessage)) {
                    event.message = new ChatComponentText(replacedMessage);
                }
            }
        }
    }

    private boolean isNetworkOrPrivateChat(String unformattedMessage) {
        if (unformattedMessage == null) return false;
        String msg = unformattedMessage.trim();
        return msg.startsWith("Party >") || msg.startsWith("Guild >") || msg.startsWith("Officer >") || msg.startsWith("Co-op >") || msg.startsWith("Friend >") || msg.startsWith("From ") || msg.startsWith("To ");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null) return;

        if (isEnabled && MapDetectionHandler.isInPit()) {
            wasEnabled = true;
            String realName = mc.thePlayer.getName();
            String displayUsername = (changeName && targetName != null && !targetName.isEmpty()) ? targetName : realName;

            for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                if (playerInfo.getGameProfile().getName().equals(realName)) {
                    String tabName = getRankColor(targetRank) + displayUsername; 
                    if (changeLevel || changePrestige) tabName = getCustomTabPitBracket() + " " + tabName;
                    playerInfo.setDisplayName(new ChatComponentText(tabName));
                }
            }

            if (mc.theWorld != null) {
                Scoreboard scoreboard = mc.theWorld.getScoreboard();
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                
                if (objective != null) {
                    if (hideLobby) {
                        String currentTitle = objective.getDisplayName();
                        String customTitle = EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "THE HYPIXEL PIT";
                        if (!currentTitle.equals(customTitle)) {
                            originalScoreboardTitle = currentTitle;
                            objective.setDisplayName(customTitle);
                        }
                    }

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
                                if (clean.trim().isEmpty() && score.getScorePoints() == levelScorePoints + 1) existingBlankScore = score;
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
                                if (fakeTeam != null) fakeTeam.setNamePrefix(EnumChatFormatting.WHITE + "Prestige: " + EnumChatFormatting.YELLOW + toRoman(targetPrestige));
                            }
                        } else {
                            scoreboard.removeObjectiveFromEntity(fakePlayer, null);
                            ScorePlayerTeam fakeTeam = scoreboard.getTeam("FakePrestige");
                            if (fakeTeam != null) scoreboard.removeTeam(fakeTeam);
                        }
                    }
                }

                if (changeLevel || changePrestige) {
                    ScorePlayerTeam currentTeam = scoreboard.getPlayersTeam(realName);
                    char prestigeChar = (char) ('A' + (50 - Math.max(0, Math.min(50, targetPrestige))));
                    int lIndex = 120 - Math.max(0, Math.min(120, targetLevel));
                    String sortKey = String.format("!%c%03d", prestigeChar, lIndex); 
                    String customTeamName = "Fx" + sortKey;

                    if (currentTeam != null && !currentTeam.getRegisteredName().startsWith("Fx")) originalTabTeam = currentTeam.getRegisteredName();
                    
                    ScorePlayerTeam customTeam = scoreboard.getTeam(customTeamName);
                    if (customTeam == null) customTeam = scoreboard.createTeam(customTeamName);
                    
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
                    if (playerInfo.getGameProfile().getName().equals(realName)) playerInfo.setDisplayName(null);
                }
            }

            if (mc.theWorld != null) {
                Scoreboard scoreboard = mc.theWorld.getScoreboard();
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                if (objective != null && originalScoreboardTitle != null) objective.setDisplayName(originalScoreboardTitle);
                if (originalTabTeam != null && !originalTabTeam.isEmpty()) scoreboard.addPlayerToTeam(realName, originalTabTeam);
                String fakePlayer = EnumChatFormatting.BLACK + "" + EnumChatFormatting.RESET;
                scoreboard.removeObjectiveFromEntity(fakePlayer, null);
                ScorePlayerTeam fakeTeam = scoreboard.getTeam("FakePrestige");
                if (fakeTeam != null) scoreboard.removeTeam(fakeTeam);
            }
        }
    }

    public String getCustomChatPitBracket() {
        EnumChatFormatting prestigeColor = getPrestigeColor(targetPrestige);
        EnumChatFormatting levelColor = getLevelColor(targetLevel);
        StringBuilder bracket = new StringBuilder();
        bracket.append(prestigeColor).append("[");
        if (targetPrestige > 0 && changePrestige) bracket.append(EnumChatFormatting.YELLOW).append(toRoman(targetPrestige)).append(prestigeColor).append("-");
        if (changeLevel) bracket.append(levelColor).append(targetLevel);
        bracket.append(prestigeColor).append("]");
        return bracket.toString();
    }

    public String getCustomTabPitBracket() {
        EnumChatFormatting prestigeColor = getPrestigeColor(targetPrestige);
        EnumChatFormatting levelColor = getLevelColor(targetLevel);
        StringBuilder bracket = new StringBuilder();
        bracket.append(prestigeColor).append("[");
        if (changeLevel) bracket.append(levelColor).append(targetLevel);
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
            case "owner":   return EnumChatFormatting.RED + "[OWNER] ";   
            case "none": default: return ""; 
        }
    }

    public EnumChatFormatting getRankColor(String rank) {
        switch (rank.toLowerCase()) {
            case "vip": case "vip+": return EnumChatFormatting.GREEN;
            case "mvp": case "mvp+": return EnumChatFormatting.AQUA;
            case "mvp++": return EnumChatFormatting.GOLD;
            case "owner": case "admin": case "youtube": case "staff": return EnumChatFormatting.RED;
            case "none": default: return EnumChatFormatting.GRAY;
        }
    }

    public EnumChatFormatting getChatColor(String rank) { return rank.equalsIgnoreCase("none") ? EnumChatFormatting.GRAY : EnumChatFormatting.WHITE; }

    public String toRoman(int num) {
        int[] values = {100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] romanLetters = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) { num -= values[i]; roman.append(romanLetters[i]); }
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

    public String getSpoofedNeededXP() {
        return "2,500";
    }

    // FIX: Tightened Regex so [OWNER] doesn't get injected into random messages!
    public static String replace(String text) {
        if (!isEnabled || text == null || text.isEmpty()) return text;
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getSession() == null) return text;

        String realName = mc.getSession().getUsername();
        if (!text.contains(realName)) return text;

        String result = text;
        String displayUsername = (changeName && targetName != null && !targetName.isEmpty()) ? targetName : realName;

        if (changeRank) {
            String customRankFormatted = instance.getCustomRankPrefix(); 
            String rankColor = instance.getRankColor(targetRank).toString();

            // Only matches your name if it is directly attached to a prefix like "[VIP] Name" or "§7Name"
            String rankRegex = "^(?:\\u00A7[0-9a-fk-or])*\\[[A-Z+]+] (?:\\u00A7[0-9a-fk-or])*" + realName;
            String nonRegex = "^\\u00A77" + realName;

            Matcher rankMatcher = Pattern.compile(rankRegex).matcher(result);
            if (rankMatcher.find()) {
                result = rankMatcher.replaceAll(customRankFormatted + rankColor + displayUsername);
            } 
            else {
                Matcher nonMatcher = Pattern.compile(nonRegex).matcher(result);
                if (nonMatcher.find()) {
                    result = nonMatcher.replaceAll(customRankFormatted + rankColor + displayUsername);
                } 
                else if (result.equals(realName)) {
                    result = customRankFormatted + rankColor + displayUsername;
                } 
                else if (changeName) {
                    result = result.replace(realName, displayUsername);
                }
            }
        } else if (changeName) {
            result = result.replace(realName, displayUsername);
        }

        return result;
    }
}