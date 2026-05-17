package com.linexstudios.foxtrot.Commands;

import com.linexstudios.foxtrot.Hud.*;
import com.linexstudios.foxtrot.Misc.*;
import com.linexstudios.foxtrot.Render.*;
import com.linexstudios.foxtrot.Denick.*;
import com.linexstudios.foxtrot.Combat.*;
import com.linexstudios.foxtrot.Util.*;
import com.linexstudios.foxtrot.Enemy.*;
import com.linexstudios.foxtrot.Dev.DevSession;
import com.linexstudios.foxtrot.Dev.DevCommandHandler;
import com.linexstudios.foxtrot.Handler.ConfigHandler;
import com.linexstudios.foxtrot.Handler.TeammateManager;
import com.linexstudios.foxtrot.Handler.FriendsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandFoxtrot extends CommandBase {
    @Override public String getCommandName() { return "foxtrot"; }
    @Override public List<String> getCommandAliases() { return Arrays.asList("fx"); }
    @Override public String getCommandUsage(ICommandSender s) { return "/foxtrot <friend|teammate|guild|add|remove|list|alerts|toggle|clear|denick|debug|esp|autodenick|hud|nickhud|enemyhud|denickentry|rank|focus|nickname|ring|deadlobby|venom|enchantnames|nonhighlighter|modidhider|rpc|users>"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override public void processCommand(ICommandSender s, String[] args) throws CommandException {
        if (args.length == 0) {
            String p = EnumChatFormatting.YELLOW + "/foxtrot ", g = EnumChatFormatting.GRAY + "- ";
            String[] h = {EnumChatFormatting.GRAY+"["+EnumChatFormatting.RED+"FOXTROT"+EnumChatFormatting.GRAY+"] "+EnumChatFormatting.RED+EnumChatFormatting.BOLD+"HELP MENU", "", p+"friend <add/remove/list> "+g+"Manage Friends", p+"teammate <add/remove/list> "+g+"Manage Team (Aliases: t, team, g, guild)", p+"add [name] "+g+"Add enemy", p+"remove [name] "+g+"Remove enemy", p+"list "+g+"View enemy list", p+"focus <name/remove/clear> "+g+"Hide all other players", p+"alerts "+g+"Toggle join alerts", p+"toggle "+g+"Toggle all HUDs", p+"esp "+g+"Toggle all ESP", p+"denick [name] "+g+"Scrape Player", p+"denickentry clear [name] "+g+"Clear cached nick", p+"autodenick "+g+"Toggle auto denicking", p+"hud "+g+"Open HUD Editor", p+"nickhud "+g+"Toggle NickedHUD", p+"enemyhud "+g+"Toggle EnemyHUD", p+"clear "+g+"Clear enemy list", p+"rank <prestige> <level> <rank> "+g+"Change Rank", p+"nickname <name|off> "+g+"Change username", p+"ring "+g+"Toggle Ring Helper", p+"deadlobby "+g+"Toggle Dead Lobby Finder", p+"venom "+g+"Toggle Venom Timer", p+"enchantnames "+g+"Toggle Enchant Names", p+"nonhighlighter "+g+"Toggle Non Highlighter ESP", p+"modidhider "+g+"Spoof client brand to vanilla", p+"rpc "+g+"Toggle Discord Rich Presence", p+"users reload "+g+"Sync global user list"};
            for(String str : h) s.addChatMessage(new ChatComponentText(str)); return;
        }
        String a = args[0].toLowerCase(); ConfigHandler.logDebug("User executed: /fx " + String.join(" ", args));
        switch (a) {
            case "nonhighlighter": NonHighlighter.toggle(); ConfigHandler.saveConfig(); break;
            case "venom": VenomTimer.enabled = !VenomTimer.enabled; ConfigHandler.saveConfig(); msg(s, "Venom Timer", VenomTimer.enabled); break;
            case "enchantnames": EnchantNames.enabled = !EnchantNames.enabled; ConfigHandler.saveConfig(); msg(s, "Enchant Names", EnchantNames.enabled); break;
            case "deadlobby": DeadLobbyFinder.toggle(); ConfigHandler.saveConfig(); break;
            case "ring": RingHelper.toggle(); ConfigHandler.saveConfig(); break;
            case "modidhider": ModIDHider.toggle(); ConfigHandler.saveConfig(); break;
            case "nickname": case "nick":
                if(args.length>=2){ if(args[1].matches("(?i)reset|off|clear")){ Ranks.changeName=false; Ranks.targetName=""; ConfigHandler.saveConfig(); msg(s, EnumChatFormatting.GREEN+"Nickname cleared!"); } else { Ranks.changeName=true; Ranks.targetName=args[1]; ConfigHandler.saveConfig(); msg(s, EnumChatFormatting.GREEN+"Nickname set to "+EnumChatFormatting.WHITE+args[1]); } } else msg(s, EnumChatFormatting.RED+"Usage: /fx nickname <name|off>"); break;
            case "focus":
                if(args.length==1) msg(s, FocusManager.focusList.isEmpty() ? EnumChatFormatting.RED+"Usage: /fx focus [name] or /fx focus remove [name]" : EnumChatFormatting.AQUA+"Focusing: "+String.join(", ", FocusManager.focusList));
                else if(args[1].equalsIgnoreCase("clear")){ FocusManager.focusList.clear(); msg(s, EnumChatFormatting.GREEN+"Cleared focus list."); }
                else if(args[1].equalsIgnoreCase("remove") && args.length>=3){ msg(s, FocusManager.focusList.removeIf(n->n.equalsIgnoreCase(args[2])) ? EnumChatFormatting.RED+"Removed "+args[2]+" from focus." : EnumChatFormatting.YELLOW+args[2]+" not in focus list."); }
                else if(args[1].equalsIgnoreCase("remove")) msg(s, EnumChatFormatting.RED+"Usage: /fx focus remove [name]");
                else { if(!FocusManager.focusList.contains(args[1].toLowerCase())){ FocusManager.focusList.add(args[1].toLowerCase()); msg(s, EnumChatFormatting.GREEN+"Now focusing on: "+args[1]); } else msg(s, EnumChatFormatting.RED+"Already focusing on "+args[1]+"!"); } break;
            case "rank":
                if(args.length==1){ Ranks.isEnabled = !Ranks.isEnabled; ConfigHandler.saveConfig(); msg(s, "Rank Changer", Ranks.isEnabled); return; }
                if(args.length>=4){ try{ int p=Integer.parseInt(args[1]), l=Integer.parseInt(args[2]); String r=args[3].toLowerCase(); if(!Arrays.asList("none","vip","vip+","mvp","mvp+","mvp++","youtube","staff","admin","owner").contains(r)){ msg(s, EnumChatFormatting.RED+"Invalid rank!"); return; } Ranks.targetPrestige=p; Ranks.targetLevel=l; Ranks.targetRank=r; Ranks.isEnabled=true; ConfigHandler.saveConfig(); msg(s, EnumChatFormatting.GREEN+"Rank updated! Prestige "+p+", Level "+l+", Rank: "+r.toUpperCase()); }catch(NumberFormatException e){ msg(s, EnumChatFormatting.RED+"Prestige/Level must be numbers!"); } } else msg(s, EnumChatFormatting.RED+"Usage: /fx rank <prestige> <level> <rank>"); break;
            case "denickentry":
                if(args.length>=2 && args[1].equalsIgnoreCase("clear")){
                    if(args.length>=3){
                        NickedHUD.nickedPlayers.removeIf(n->n.equalsIgnoreCase(args[2]));
                        CacheManager.removeFromCache(args[2]);
                        msg(s, EnumChatFormatting.RED+"[Foxtrot] "+EnumChatFormatting.GREEN+"Cleared "+args[2]+" from denick memory!");
                    } else {
                        NickedHUD.nickedPlayers.clear();
                        CacheManager.getCache().clear();
                        CacheManager.saveCache();
                        msg(s, EnumChatFormatting.RED+"[Foxtrot] "+EnumChatFormatting.GREEN+"Cleared ALL players from denick memory!");
                    }
                } else msg(s, EnumChatFormatting.RED+"Usage: /fx denickentry clear [name]"); break;
            case "debug": ConfigHandler.globalDebug = !ConfigHandler.globalDebug; ConfigHandler.saveConfig(); msg(s, "Global Debug", ConfigHandler.globalDebug); break;
            case "denick": if(args.length>1){ new Thread(new DenickRunnable(args[1])).start(); if(!NickedHUD.nickedPlayers.contains(args[1].toLowerCase())) NickedHUD.nickedPlayers.add(args[1].toLowerCase()); msg(s, EnumChatFormatting.GRAY+"["+EnumChatFormatting.GOLD+"Foxtrot"+EnumChatFormatting.GRAY+"] "+EnumChatFormatting.GREEN+"Scraping "+args[1]); } else msg(s, EnumChatFormatting.RED+"Usage: /fx denick [name]"); break;
            case "esp": EnemyESP.enabled = !EnemyESP.enabled; FriendsESP.enabled = EnemyESP.enabled; TeammateESP.enabled = EnemyESP.enabled; ConfigHandler.saveConfig(); msg(s, "All ESP", EnemyESP.enabled); break;
            case "teammate": case "team": case "t": case "guild": case "g":
                if(args.length<2){ msg(s, EnumChatFormatting.RED+"Usage: /fx teammate <add|remove|list> [name]"); break; }
                String tSub = args[1].toLowerCase();
                if(tSub.equals("add")){ if(args.length>2) TeammateManager.addTeammate(args[2]); else msg(s, EnumChatFormatting.RED+"Usage: /fx teammate add [name]"); }
                else if(tSub.equals("remove")){ if(args.length>2) TeammateManager.removeTeammate(args[2]); else msg(s, EnumChatFormatting.RED+"Usage: /fx teammate remove [name]"); }
                else if(tSub.equals("list")){ if(TeammateManager.teammateCache.isEmpty()) msg(s, EnumChatFormatting.RED+"Teammate list is empty."); else { msg(s, EnumChatFormatting.AQUA+"Teammate List ("+TeammateManager.teammateCache.size()+")\n"); for(String n : TeammateManager.teammateCache.values()) msg(s, EnumChatFormatting.GRAY+"- "+EnumChatFormatting.AQUA+n); } }
                break;
            case "friend": case "f":
                if(args.length<2){ msg(s, EnumChatFormatting.RED+"Usage: /fx friend <add|remove|list> [name]"); break; }
                String sub = args[1].toLowerCase();
                if(sub.equals("add")){ if(args.length>2){ if(!FriendsHUD.isFriend(null, args[2])){ FriendsHUD.friendsList.add(args[2]); FriendsManager.addFriend(args[2]); ConfigHandler.saveConfig(); } else msg(s, EnumChatFormatting.RED+args[2]+" is already a friend!"); } else msg(s, EnumChatFormatting.RED+"Usage: /fx friend add [name]"); }
                else if(sub.equals("remove")){ if(args.length>2){ if(FriendsHUD.friendsList.removeIf(n->n.equalsIgnoreCase(args[2]))){ FriendsManager.removeFriend(args[2]); ConfigHandler.saveConfig(); } else msg(s, EnumChatFormatting.YELLOW+args[2]+" not found."); } else msg(s, EnumChatFormatting.RED+"Usage: /fx friend remove [name]"); }
                else if(sub.equals("list")){ if(FriendsHUD.friendsList.isEmpty()) msg(s, EnumChatFormatting.RED+"Friends list is empty."); else { msg(s, EnumChatFormatting.GREEN+"Friends List ("+FriendsHUD.friendsList.size()+")\n"); for(String n : FriendsHUD.friendsList) msg(s, EnumChatFormatting.GRAY+"- "+EnumChatFormatting.GREEN+n); } }
                else msg(s, EnumChatFormatting.RED+"Usage: /fx friend <add|remove|list> [name]"); break;
            case "add":
                if(args.length>1){ if(EnemyHUD.targetList.stream().noneMatch(n->n.equalsIgnoreCase(args[1]))){ EnemyHUD.targetList.add(args[1]); EnemyManager.fetchSingleUUID(args[1]); ConfigHandler.saveConfig(); msg(s, EnumChatFormatting.GREEN+"Added "+args[1]+" to enemies."); } else msg(s, EnumChatFormatting.RED+args[1]+" is already on your list!"); } else msg(s, EnumChatFormatting.RED+"Usage: /fx add [name]"); break;
            case "remove":
                if(args.length>1){ if(EnemyHUD.targetList.removeIf(n->n.equalsIgnoreCase(args[1]))){ EnemyManager.removeEnemy(args[1]); ConfigHandler.saveConfig(); msg(s, EnumChatFormatting.RED+"Removed "+args[1]+" from enemies."); } else msg(s, EnumChatFormatting.YELLOW+args[1]+" not found."); } else msg(s, EnumChatFormatting.RED+"Usage: /fx remove [name]"); break;
            case "list":
                if(EnemyHUD.targetList.isEmpty()) msg(s, EnumChatFormatting.RED+"Your list is empty."); else { msg(s, EnumChatFormatting.AQUA+"Enemy List ("+EnemyHUD.targetList.size()+")\n"); for(String n : EnemyHUD.targetList) msg(s, EnumChatFormatting.GRAY+"- "+EnumChatFormatting.RED+n); } break;
            case "alerts": EnemyHUD.notificationsEnabled = !EnemyHUD.notificationsEnabled; ConfigHandler.saveConfig(); msg(s, "Alerts", EnemyHUD.notificationsEnabled); break;
            case "toggle": HUDController.setEnabled(!HUDController.enabled); ConfigHandler.saveConfig(); msg(s, "HUDs", HUDController.enabled); break;
            case "clear": EnemyHUD.targetList.clear(); ConfigHandler.saveConfig(); msg(s, EnumChatFormatting.RED+"List cleared."); break;
            case "autodenick": AutoDenick.enabled = !AutoDenick.enabled; ConfigHandler.saveConfig(); msg(s, "AutoDenick", AutoDenick.enabled); break;
            case "hud": new Thread(()->{ try{ Thread.sleep(100); Minecraft.getMinecraft().addScheduledTask(()->Minecraft.getMinecraft().displayGuiScreen(new EditHUDGui())); }catch(Exception ignored){} }).start(); break;
            case "nickhud": NickedHUD.enabled = !NickedHUD.enabled; ConfigHandler.saveConfig(); msg(s, "NickedHUD", NickedHUD.enabled); break;
            case "enemyhud": EnemyHUD.enabled = !EnemyHUD.enabled; ConfigHandler.saveConfig(); msg(s, "EnemyHUD", EnemyHUD.enabled); break;
            case "rpc": ConfigHandler.discordRpcEnabled = !ConfigHandler.discordRpcEnabled; ConfigHandler.saveConfig(); if(ConfigHandler.discordRpcEnabled) DiscordRPCManager.start(); else DiscordRPCManager.stop(); msg(s, "Discord RPC", ConfigHandler.discordRpcEnabled); break;
            case "users": if(args.length>1 && args[1].equalsIgnoreCase("reload")){ com.linexstudios.foxtrot.Handler.FoxtrotUsersManager.initialize(); msg(s, EnumChatFormatting.GREEN+"Syncing global user list..."); } else msg(s, EnumChatFormatting.RED+"Usage: /fx users reload"); break;
            case "dev":
                // /fx dev auth <key>  — validate against the Foxtrot API Manager
                if (args.length >= 3 && args[1].equalsIgnoreCase("auth")) {
                    final String licenseKey = args[2];
                    msg(s, EnumChatFormatting.GRAY + "Verifying license key...");
                    new Thread(() -> {
                        try {
                            URL url = new URL("https://foxtrot-api-manager.pages.dev/dev/auth");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setRequestProperty("X-Foxtrot-Key", licenseKey);
                            conn.setConnectTimeout(5000);
                            conn.setReadTimeout(5000);
                            int code = conn.getResponseCode();
                            if (code == 200) {
                                DevSession.unlock();
                                Minecraft.getMinecraft().addScheduledTask(() ->
                                    msg(s, EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + "Dev mode authenticated. Type /fx dev help."));
                            } else {
                                Minecraft.getMinecraft().addScheduledTask(() ->
                                    msg(s, EnumChatFormatting.RED + "License key rejected (" + code + "). Access denied."));
                            }
                        } catch (Exception e) {
                            Minecraft.getMinecraft().addScheduledTask(() ->
                                msg(s, EnumChatFormatting.RED + "Could not reach auth server: " + e.getMessage()));
                        }
                    }).start();
                } else if (!DevSession.isActive()) {
                    // Don't reveal anything useful — look like an unknown command
                    msg(s, EnumChatFormatting.RED + "Unknown command.");
                } else {
                    DevCommandHandler.handle(s, args);
                }
                break;
            default: msg(s, EnumChatFormatting.RED+"Unknown command."); break;
        }
    }

    @Override public List<String> addTabCompletionOptions(ICommandSender s, String[] args, BlockPos pos) {
        if(args.length==1) return getListOfStringsMatchingLastWord(args, "friend","f","teammate","team","t","guild","g","add","remove","list","alerts","toggle","clear","denick","debug","esp","autodenick","hud","nickhud","enemyhud","denickentry","rank","focus","nickname","nick","ring","deadlobby","venom","enchantnames","nonhighlighter","modidhider","rpc","users");
        if(args.length==2 && args[0].matches("(?i)nickname|nick")) return getListOfStringsMatchingLastWord(args, "off","reset","clear");
        if(args.length==2 && args[0].equalsIgnoreCase("focus")){ List<String> o=Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().stream().map(i->i.getGameProfile().getName()).collect(Collectors.toList()); o.add("remove"); o.add("clear"); return getListOfStringsMatchingLastWord(args, o); }
        if(args.length==3 && args[0].equalsIgnoreCase("focus") && args[1].equalsIgnoreCase("remove")) return getListOfStringsMatchingLastWord(args, FocusManager.focusList);
        if(args.length==4 && args[0].equalsIgnoreCase("rank")) return getListOfStringsMatchingLastWord(args, "none","vip","vip+","mvp","mvp+","mvp++","youtube","staff","admin","owner");
        if(args.length==2 && args[0].equalsIgnoreCase("denickentry")) return getListOfStringsMatchingLastWord(args, "clear");
        if(args.length==3 && args[0].equalsIgnoreCase("denickentry") && args[1].equalsIgnoreCase("clear")) return getListOfStringsMatchingLastWord(args, CacheManager.getCache().keySet());
        if(args.length==2 && args[0].matches("(?i)friend|f|teammate|team|t|guild|g")) return getListOfStringsMatchingLastWord(args, "add","remove","list");
        if((args.length==2 && args[0].matches("(?i)add|denick")) || (args.length==3 && args[0].matches("(?i)friend|f|teammate|team|t|guild|g") && args[1].equalsIgnoreCase("add"))) return getListOfStringsMatchingLastWord(args, Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().stream().map(i->i.getGameProfile().getName()).collect(Collectors.toList()));
        if(args.length==2 && args[0].equalsIgnoreCase("remove")) return getListOfStringsMatchingLastWord(args, EnemyHUD.targetList);
        if(args.length==3 && (args[0].matches("(?i)friend|f") && args[1].equalsIgnoreCase("remove"))) return getListOfStringsMatchingLastWord(args, FriendsHUD.friendsList);
        if(args.length==3 && (args[0].matches("(?i)teammate|team|t|guild|g") && args[1].equalsIgnoreCase("remove"))) return getListOfStringsMatchingLastWord(args, TeammateManager.teammateCache.values());
        return null;
    }
    private void msg(ICommandSender s, String m) { s.addChatMessage(new ChatComponentText(m)); }
    private void msg(ICommandSender s, String n, boolean b) { s.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + n + ": " + (b ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF"))); }
}
 // yep compressed this shit too so worth it went from over 450 lines of code to only 97 lines of code wow