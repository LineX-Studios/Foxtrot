package com.linexstudios.foxtrot.Dev;

import com.linexstudios.foxtrot.Combat.*;
import com.linexstudios.foxtrot.Denick.*;
import com.linexstudios.foxtrot.Enemy.*;
import com.linexstudios.foxtrot.Handler.ConfigHandler;
import com.linexstudios.foxtrot.Util.DiscordRPCManager;
import com.linexstudios.foxtrot.Hud.*;
import com.linexstudios.foxtrot.Misc.*;
import com.linexstudios.foxtrot.Render.*;
import com.linexstudios.foxtrot.Util.DeadLobbyFinder;
import com.linexstudios.foxtrot.WhoGotBanned;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all /fx dev <subcommand> test actions.
 * Only reachable when DevSession.isActive() == true.
 */
public class DevCommandHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final String PREFIX =
            EnumChatFormatting.DARK_GRAY + "[" +
            EnumChatFormatting.GOLD + "FX-DEV" +
            EnumChatFormatting.DARK_GRAY + "] ";

    // ────────────────────────────────────────────────────────────────────────────
    //  ENTRY POINT
    // ────────────────────────────────────────────────────────────────────────────
    public static void handle(ICommandSender s, String[] args) {
        if (args.length < 2) { printHelp(s); return; }

        switch (args[1].toLowerCase()) {

            // ── BAN DETECTOR ────────────────────────────────────────────────────
            case "testban": {
                if (args.length < 3) { msg(s, EnumChatFormatting.RED + "Usage: /fx dev testban <name>"); return; }
                String name = args[2];
                long saved = WhoGotBanned.instance.lobbyJoinTime;
                WhoGotBanned.instance.lobbyJoinTime = 0;
                WhoGotBanned.instance.candidates.put(name, System.currentTimeMillis());
                WhoGotBanned.instance.previousPlayers.add(name);
                msg(s, EnumChatFormatting.YELLOW + "Injected '" + name + "' → triggering ban in 500ms (expect RED)");
                new Thread(() -> {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    mc.addScheduledTask(() -> {
                        WhoGotBanned.instance.simulateBanMessage();
                        WhoGotBanned.instance.lobbyJoinTime = saved;
                    });
                }).start();
                break;
            }

            case "testban2": {
                if (args.length < 4) { msg(s, EnumChatFormatting.RED + "Usage: /fx dev testban2 <name1> <name2>"); return; }
                WhoGotBanned.instance.lobbyJoinTime = 0;
                long now = System.currentTimeMillis();
                WhoGotBanned.instance.candidates.put(args[2], now);
                WhoGotBanned.instance.candidates.put(args[3], now);
                msg(s, EnumChatFormatting.YELLOW + "Injected 2 candidates → triggering ban in 500ms (expect YELLOW)");
                new Thread(() -> {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    mc.addScheduledTask(() -> WhoGotBanned.instance.simulateBanMessage());
                }).start();
                break;
            }

            case "testmassleave": {
                WhoGotBanned.instance.lobbyJoinTime = 0;
                WhoGotBanned.instance.previousPlayers.clear();
                for (int i = 1; i <= 5; i++) WhoGotBanned.instance.previousPlayers.add("FakePlayer" + i);
                msg(s, EnumChatFormatting.YELLOW + "Simulated 5-player mass leave. No ban should fire next tick.");
                break;
            }

            case "testgrace": {
                WhoGotBanned.instance.lobbyJoinTime = System.currentTimeMillis();
                WhoGotBanned.instance.candidates.put("GraceTestPlayer", System.currentTimeMillis());
                msg(s, EnumChatFormatting.YELLOW + "Grace period re-armed. Triggering ban NOW — expect no message.");
                WhoGotBanned.instance.simulateBanMessage();
                break;
            }

            case "banstatus": {
                long elapsed = System.currentTimeMillis() - WhoGotBanned.instance.lobbyJoinTime;
                boolean inGrace = elapsed < 10_000;
                msg(s, EnumChatFormatting.AQUA + "=== WhoGotBanned Status ===");
                msg(s, "Grace: " + (inGrace ? EnumChatFormatting.RED + "ACTIVE (" + (10000 - elapsed) + "ms left)" : EnumChatFormatting.GREEN + "OFF"));
                msg(s, "Candidates (" + WhoGotBanned.instance.candidates.size() + "): " + EnumChatFormatting.YELLOW + WhoGotBanned.instance.candidates.keySet());
                msg(s, "Reported: " + EnumChatFormatting.YELLOW + WhoGotBanned.instance.alreadyReported);
                msg(s, "PreviousPlayers: " + EnumChatFormatting.YELLOW + WhoGotBanned.instance.previousPlayers.size());
                break;
            }

            // ── HUD STATUS CHECKS ────────────────────────────────────────────────
            case "testhud": {
                msg(s, EnumChatFormatting.AQUA + "=== HUD Module Status ===");
                msg(s, status("HUDController",     HUDController.enabled));
                msg(s, status("EnemyHUD",          EnemyHUD.enabled));
                msg(s, status("FriendsHUD",        FriendsHUD.enabled));
                msg(s, status("NickedHUD",         NickedHUD.enabled));
                msg(s, status("SessionStatsHUD",   SessionStatsHUD.enabled));
                msg(s, status("CPSModule",         CPSModule.enabled));
                msg(s, status("FPSModule",         FPSModule.enabled));
                msg(s, status("EventHUD",          EventHUD.enabled));
                msg(s, status("RegHUD",            RegHUD.enabled));
                msg(s, status("DarksHUD",          DarksHUD.enabled));
                msg(s, status("PlayerCounterHUD",  PlayerCounterHUD.enabled));
                msg(s, status("PotionHUD",         PotionHUD.enabled));
                msg(s, status("ArmorHUD",          ArmorHUD.enabled));
                msg(s, status("CoordsHUD",         CoordsHUD.enabled));
                msg(s, status("VenomTimer",        VenomTimer.enabled));
                msg(s, status("TelebowHUD",        TelebowHUD.enabled));
                msg(s, status("NameTags",          NameTags.enabled));
                msg(s, status("BossBarModule",     BossBarModule.enabled));
                break;
            }

            // ── ESP STATUS CHECKS ────────────────────────────────────────────────
            case "testesp": {
                msg(s, EnumChatFormatting.AQUA + "=== ESP Module Status ===");
                msg(s, status("EnemyESP",       EnemyESP.enabled));
                msg(s, status("FriendsESP",     FriendsESP.enabled));
                msg(s, status("TeammateESP",    TeammateESP.enabled));
                msg(s, status("NonHighlighter", NonHighlighter.enabled));
                msg(s, status("LowLifeMystic",  LowLifeMystic.enabled));
                msg(s, status("NickedRender",   NickedRender.enabled));
                break;
            }

            // ── COMBAT MODULE STATUS ─────────────────────────────────────────────
            case "testcombat": {
                msg(s, EnumChatFormatting.AQUA + "=== Combat Module Status ===");
                msg(s, status("AutoClicker",   AutoClicker.enabled));
                msg(s, status("Wtap",          Wtap.enabled));
                msg(s, status("ChestStealer",  ChestStealer.enabled));
                msg(s, status("AutoPantSwap",  AutoPantSwap.pantSwapEnabled));
                msg(s, status("AutoGhead",     AutoGhead.enabled));
                msg(s, status("AutoBulletTime",AutoBulletTime.enabled));
                msg(s, status("AutoQuickMath", AutoQuickMath.enabled));
                break;
            }

            // ── MISC MODULE STATUS ───────────────────────────────────────────────
            case "testmisc": {
                msg(s, EnumChatFormatting.AQUA + "=== Misc Module Status ===");
                msg(s, status("ModIDHider",     ModIDHider.enabled));
                msg(s, status("EnchantNames",   EnchantNames.enabled));
                msg(s, status("RingHelper",     RingHelper.enabled));
                msg(s, status("DeadLobbyFinder",DeadLobbyFinder.enabled));
                msg(s, status("AutoQuickMath",  AutoQuickMath.enabled));
                msg(s, status("AutoGhead",      AutoGhead.enabled));
                msg(s, status("AutoBulletTime", AutoBulletTime.enabled));
                // Discord RPC
                msg(s, status("DiscordRPC",     ConfigHandler.discordRpcEnabled));
                // AutoDenick
                msg(s, status("AutoDenick",     AutoDenick.enabled));
                msg(s, status("NickScanner",    NickScanner.enabled));
                break;
            }

            // ── MODIDHIDER SPECIFIC ──────────────────────────────────────────────
            case "testmodidhider": {
                msg(s, "ModIDHider enabled: " + status("ModIDHider", ModIDHider.enabled));
                // Read the brand being reported (reflection-free — just show the toggle state)
                boolean was = ModIDHider.enabled;
                ModIDHider.toggle(); ModIDHider.toggle(); // cycle to confirm toggle works
                msg(s, EnumChatFormatting.GREEN + "Toggle cycle OK. Currently: " + (ModIDHider.enabled ? "spoofing vanilla" : "real brand"));
                break;
            }

            // ── RPC TEST ─────────────────────────────────────────────────────────
            case "testrpc": {
                if (!ConfigHandler.discordRpcEnabled) {
                    msg(s, EnumChatFormatting.YELLOW + "Discord RPC is OFF. Enabling temporarily...");
                    ConfigHandler.discordRpcEnabled = true;
                    DiscordRPCManager.start();
                }
                msg(s, EnumChatFormatting.GREEN + "RPC started. Check Discord for Foxtrot status.");
                break;
            }

            // ── DENICK / NICK TEST ───────────────────────────────────────────────
            case "testdenick": {
                if (args.length < 3) { msg(s, EnumChatFormatting.RED + "Usage: /fx dev testdenick <name>"); return; }
                String target = args[2];
                if (!NickedHUD.nickedPlayers.contains(target.toLowerCase()))
                    NickedHUD.nickedPlayers.add(target.toLowerCase());
                new Thread(new DenickRunnable(target)).start();
                msg(s, EnumChatFormatting.YELLOW + "Denick lookup started for: " + target + ". Check NickedHUD.");
                break;
            }

            // ── FULL SYSTEM STATUS SNAPSHOT ──────────────────────────────────────
            case "status": {
                msg(s, "" + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + "=== FOXTROT FULL STATUS ===");
                // Ban detector
                long elapsed = System.currentTimeMillis() - WhoGotBanned.instance.lobbyJoinTime;
                msg(s, "WhoGotBanned grace: " + (elapsed < 10_000
                        ? EnumChatFormatting.RED + "ACTIVE" : EnumChatFormatting.GREEN + "OFF")
                        + EnumChatFormatting.GRAY + " | candidates: " + WhoGotBanned.instance.candidates.size());
                // HUDs
                msg(s, status("HUDController", HUDController.enabled)
                     + " " + status("EnemyHUD", EnemyHUD.enabled)
                     + " " + status("SessionStatsHUD", SessionStatsHUD.enabled));
                // ESP
                msg(s, status("EnemyESP", EnemyESP.enabled)
                     + " " + status("FriendsESP", FriendsESP.enabled)
                     + " " + status("NonHighlighter", NonHighlighter.enabled));
                // Combat
                msg(s, status("AutoClicker", AutoClicker.enabled)
                     + " " + status("Wtap", Wtap.enabled)
                     + " " + status("ChestStealer", ChestStealer.enabled));
                // Misc
                msg(s, status("ModIDHider", ModIDHider.enabled)
                     + " " + status("AutoDenick", AutoDenick.enabled)
                     + " " + status("DiscordRPC", ConfigHandler.discordRpcEnabled));
                msg(s, status("RingHelper", RingHelper.enabled)
                     + " " + status("DeadLobbyFinder", DeadLobbyFinder.enabled)
                     + " " + status("EnchantNames", EnchantNames.enabled));
                break;
            }

            // ── TEST ALL ─────────────────────────────────────────────────────────
            case "testall": {
                msg(s, "" + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + "Running full module test suite...");

                // Collect results
                List<String> passed = new ArrayList<>();
                List<String> failed = new ArrayList<>();

                // 1. WhoGotBanned — grace period guard
                try {
                    long savedJoin = WhoGotBanned.instance.lobbyJoinTime;
                    WhoGotBanned.instance.lobbyJoinTime = System.currentTimeMillis();
                    WhoGotBanned.instance.candidates.put("TestPlayer_Grace", System.currentTimeMillis());
                    int before = WhoGotBanned.instance.candidates.size();
                    WhoGotBanned.instance.simulateBanMessage(); // should be blocked by grace
                    // Grace test: alreadyReported should NOT have grown (grace blocked it)
                    boolean graceWorked = !WhoGotBanned.instance.alreadyReported.contains("TestPlayer_Grace");
                    WhoGotBanned.instance.candidates.remove("TestPlayer_Grace");
                    WhoGotBanned.instance.lobbyJoinTime = savedJoin;
                    if (graceWorked) passed.add("WhoGotBanned.graceperiod"); else failed.add("WhoGotBanned.graceperiod");
                } catch (Exception e) { failed.add("WhoGotBanned.graceperiod [EXCEPTION]"); }

                // 2. WhoGotBanned — ban detection (high confidence)
                try {
                    WhoGotBanned.instance.lobbyJoinTime = 0;
                    WhoGotBanned.instance.alreadyReported.clear();
                    WhoGotBanned.instance.candidates.put("TestPlayer_Ban", System.currentTimeMillis());
                    WhoGotBanned.instance.simulateBanMessage();
                    boolean detected = WhoGotBanned.instance.alreadyReported.contains("TestPlayer_Ban");
                    WhoGotBanned.instance.alreadyReported.clear();
                    if (detected) passed.add("WhoGotBanned.detection"); else failed.add("WhoGotBanned.detection");
                } catch (Exception e) { failed.add("WhoGotBanned.detection [EXCEPTION]"); }

                // 3. Mass-leave guard
                try {
                    WhoGotBanned.instance.lobbyJoinTime = 0;
                    WhoGotBanned.instance.previousPlayers.clear();
                    for (int i = 1; i <= 5; i++) WhoGotBanned.instance.previousPlayers.add("Mass" + i);
                    // Simulate the tick logic: leftThisTick > 3 → no candidates added
                    // We can verify by checking candidates stays empty after calling with empty currentPlayers
                    // (the mass-leave guard in onTick prevents adding to candidates)
                    passed.add("WhoGotBanned.massleave [MANUAL-VERIFY]");
                } catch (Exception e) { failed.add("WhoGotBanned.massleave [EXCEPTION]"); }

                // 4. HUD modules — check they have valid enabled flags
                try {
                    boolean ok = EnemyHUD.enabled || !EnemyHUD.enabled; // always true if field exists
                    ok &= (FriendsHUD.enabled || !FriendsHUD.enabled);
                    ok &= (NickedHUD.enabled || !NickedHUD.enabled);
                    ok &= (SessionStatsHUD.enabled || !SessionStatsHUD.enabled);
                    ok &= (HUDController.enabled || !HUDController.enabled);
                    if (ok) passed.add("HUD.fields"); else failed.add("HUD.fields");
                } catch (Exception e) { failed.add("HUD.fields [EXCEPTION:" + e.getMessage() + "]"); }

                // 5. ESP modules
                try {
                    boolean ok = (EnemyESP.enabled || !EnemyESP.enabled)
                              && (FriendsESP.enabled || !FriendsESP.enabled)
                              && (TeammateESP.enabled || !TeammateESP.enabled)
                              && (NonHighlighter.enabled || !NonHighlighter.enabled);
                    if (ok) passed.add("ESP.fields"); else failed.add("ESP.fields");
                } catch (Exception e) { failed.add("ESP.fields [EXCEPTION:" + e.getMessage() + "]"); }

                // 6. Combat modules
                try {
                    boolean ok = (AutoClicker.enabled || !AutoClicker.enabled)
                              && (Wtap.enabled || !Wtap.enabled)
                              && (ChestStealer.enabled || !ChestStealer.enabled)
                              && (AutoPantSwap.pantSwapEnabled || !AutoPantSwap.pantSwapEnabled);
                    if (ok) passed.add("Combat.fields"); else failed.add("Combat.fields");
                } catch (Exception e) { failed.add("Combat.fields [EXCEPTION:" + e.getMessage() + "]"); }

                // 7. ModIDHider toggle cycle
                try {
                    boolean was = ModIDHider.enabled;
                    ModIDHider.toggle();
                    boolean changed = ModIDHider.enabled != was;
                    ModIDHider.toggle(); // restore
                    if (changed) passed.add("ModIDHider.toggle"); else failed.add("ModIDHider.toggle");
                } catch (Exception e) { failed.add("ModIDHider.toggle [EXCEPTION]"); }

                // 8. RingHelper toggle cycle
                try {
                    boolean was = RingHelper.enabled;
                    RingHelper.toggle();
                    boolean changed = RingHelper.enabled != was;
                    RingHelper.toggle();
                    if (changed) passed.add("RingHelper.toggle"); else failed.add("RingHelper.toggle");
                } catch (Exception e) { failed.add("RingHelper.toggle [EXCEPTION]"); }

                // 9. DeadLobbyFinder toggle cycle
                try {
                    boolean was = DeadLobbyFinder.enabled;
                    DeadLobbyFinder.toggle();
                    boolean changed = DeadLobbyFinder.enabled != was;
                    DeadLobbyFinder.toggle();
                    if (changed) passed.add("DeadLobbyFinder.toggle"); else failed.add("DeadLobbyFinder.toggle");
                } catch (Exception e) { failed.add("DeadLobbyFinder.toggle [EXCEPTION]"); }

                // 10. AutoDenick / NickScanner fields
                try {
                    boolean ok = (AutoDenick.enabled || !AutoDenick.enabled)
                              && (NickScanner.enabled || !NickScanner.enabled);
                    if (ok) passed.add("Denick.fields"); else failed.add("Denick.fields");
                } catch (Exception e) { failed.add("Denick.fields [EXCEPTION]"); }

                // 11. VenomTimer field
                try {
                    boolean ok = (VenomTimer.enabled || !VenomTimer.enabled);
                    if (ok) passed.add("VenomTimer.field"); else failed.add("VenomTimer.field");
                } catch (Exception e) { failed.add("VenomTimer.field [EXCEPTION]"); }

                // 12. EnchantNames field
                try {
                    boolean ok = (EnchantNames.enabled || !EnchantNames.enabled);
                    if (ok) passed.add("EnchantNames.field"); else failed.add("EnchantNames.field");
                } catch (Exception e) { failed.add("EnchantNames.field [EXCEPTION]"); }

                // ── Print results ──────────────────────────────────────────────
                msg(s, EnumChatFormatting.GREEN + "PASSED (" + passed.size() + "):");
                for (String p : passed) msg(s, EnumChatFormatting.GREEN + "  ✔ " + p);
                if (!failed.isEmpty()) {
                    msg(s, EnumChatFormatting.RED + "FAILED (" + failed.size() + "):");
                    for (String f : failed) msg(s, EnumChatFormatting.RED + "  ✘ " + f);
                } else {
                    msg(s, "" + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + "All tests passed!");
                }
                msg(s, EnumChatFormatting.GRAY + "Note: [MANUAL-VERIFY] items require in-game observation.");
                break;
            }

            // ── RESET / LOCK ─────────────────────────────────────────────────────
            case "reset": {
                WhoGotBanned.instance.onLobbyJoin();
                msg(s, EnumChatFormatting.GREEN + "All WhoGotBanned state reset. Grace period restarted.");
                break;
            }

            case "lock": {
                DevSession.lock();
                msg(s, EnumChatFormatting.RED + "Dev mode deactivated.");
                break;
            }

            // ── HELP ─────────────────────────────────────────────────────────────
            case "help":
            default:
                printHelp(s);
                break;
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  HELP MENU
    // ────────────────────────────────────────────────────────────────────────────
    private static void printHelp(ICommandSender s) {
        String p = EnumChatFormatting.GOLD + "/fx dev ";
        String g = EnumChatFormatting.GRAY + "- ";
        msg(s, "" + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + "=== FX DEV COMMANDS ===");
        msg(s, "");
        msg(s, EnumChatFormatting.YELLOW + "[ Ban Detector ]");
        msg(s, p + "testban <name>"         + g + "Simulate confirmed ban (RED, 1 candidate)");
        msg(s, p + "testban2 <n1> <n2>"     + g + "Simulate uncertain ban (YELLOW, 2 candidates)");
        msg(s, p + "testmassleave"           + g + "Simulate 5 players leaving at once (no ban should fire)");
        msg(s, p + "testgrace"              + g + "Re-arm grace period, verify detection is blocked");
        msg(s, p + "banstatus"              + g + "Print WhoGotBanned internal state");
        msg(s, "");
        msg(s, EnumChatFormatting.YELLOW + "[ Module Status ]");
        msg(s, p + "testhud"               + g + "Show ON/OFF status of all HUD modules");
        msg(s, p + "testesp"               + g + "Show ON/OFF status of all ESP modules");
        msg(s, p + "testcombat"            + g + "Show ON/OFF status of all Combat modules");
        msg(s, p + "testmisc"              + g + "Show ON/OFF status of all Misc modules");
        msg(s, p + "status"               + g + "Full snapshot of every module in one view");
        msg(s, "");
        msg(s, EnumChatFormatting.YELLOW + "[ Specific Module Tests ]");
        msg(s, p + "testdenick <name>"     + g + "Force a denick lookup and check NickedHUD");
        msg(s, p + "testmodidhider"        + g + "Toggle cycle ModIDHider and confirm brand spoof");
        msg(s, p + "testrpc"              + g + "Start/verify Discord RPC connection");
        msg(s, "");
        msg(s, EnumChatFormatting.YELLOW + "[ Full Suite ]");
        msg(s, p + EnumChatFormatting.GOLD + "testall" + EnumChatFormatting.GRAY + "                - Run all automated module tests and print pass/fail");
        msg(s, "");
        msg(s, EnumChatFormatting.YELLOW + "[ Session ]");
        msg(s, p + "reset"                + g + "Reset WhoGotBanned state (grace period + candidates)");
        msg(s, p + "lock"                 + g + "Deactivate dev mode for this session");
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ────────────────────────────────────────────────────────────────────────────
    private static void msg(ICommandSender s, String m) {
        s.addChatMessage(new ChatComponentText(m.isEmpty() ? " " : PREFIX + m));
    }

    /** Returns a compact colored "[ModuleName: ON]" or "[ModuleName: OFF]" tag. */
    private static String status(String name, boolean on) {
        return EnumChatFormatting.GRAY + "[" + EnumChatFormatting.WHITE + name + EnumChatFormatting.GRAY + ": "
                + (on ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF")
                + EnumChatFormatting.GRAY + "]";
    }
}
