package com.linexstudios.foxtrot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WhoGotBanned {
    public static final WhoGotBanned instance = new WhoGotBanned();
    private final Minecraft mc = Minecraft.getMinecraft();

    // All collections are thread-safe to prevent ConcurrentModificationException

    // Snapshot of the tab list from the previous detection window
    public final Set<String> previousPlayers = ConcurrentHashMap.newKeySet();

    // Players who vanished from tab in the current detection window (Name ->
    // Timestamp)
    // Cleared after every ban chat event is processed
    public final Map<String, Long> candidates = new ConcurrentHashMap<>();

    // Long-term cache: prevents reporting the same player twice in one session
    public final Set<String> alreadyReported = ConcurrentHashMap.newKeySet();

    // Lobby-join grace period: detection is disabled for 10 seconds after joining
    public long lobbyJoinTime = 0;
    private static final long GRACE_PERIOD_MS = 10_000; // 10 seconds

    // Detection tuning constants
    // How long a candidate stays valid after they left the tab list
    private static final long CANDIDATE_TTL_MS = 3_000;
    // Mass-leave threshold — if more than this many left at once, it's a reshuffle
    // not a ban
    private static final int MASS_LEAVE_THRESHOLD = 5;

    private static final String PROXY_API_URL = "https://foxtrot-api.vercel.app/ban";

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.getNetHandler() == null)
            return;

        // Skip all detection during the grace period after joining a lobby
        if (System.currentTimeMillis() - lobbyJoinTime < GRACE_PERIOD_MS) {
            return;
        }

        // Build a fresh snapshot of the current tab list
        Set<String> currentPlayers = new HashSet<>();
        for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
            if (info != null && info.getGameProfile() != null && info.getGameProfile().getName() != null) {
                String name = info.getGameProfile().getName();
                // Filter out NPCs/holograms (they start with color codes)
                if (!name.startsWith("\u00a7")) {
                    currentPlayers.add(name);
                }
            }
        }

        // Seed the previous list on first tick after grace period ends
        if (previousPlayers.isEmpty()) {
            previousPlayers.addAll(currentPlayers);
            return;
        }

        // Find who was in the previous snapshot but is gone now
        Set<String> leftThisTick = new HashSet<>(previousPlayers);
        leftThisTick.removeAll(currentPlayers);

        // Mass-leave spam guard: if too many people left at once it's a reshuffle, not
        // a ban.
        if (leftThisTick.size() > MASS_LEAVE_THRESHOLD) {
            previousPlayers.clear();
            previousPlayers.addAll(currentPlayers);
            // Don't add anyone to candidates — this was a mass leave event
            return;
        }

        // Record each leaver as a candidate with the exact millisecond they
        // disappeared.
        // Timestamp here and correlate with the ban chat message in onChat.
        long now = System.currentTimeMillis();
        for (String left : leftThisTick) {
            candidates.put(left, now);
        }

        // Update previous snapshot for next tick
        previousPlayers.clear();
        previousPlayers.addAll(currentPlayers);

        // Evict stale candidates that are older than the TTL window
        candidates.entrySet().removeIf(entry -> now - entry.getValue() > CANDIDATE_TTL_MS);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type == 2)
            return; // Ignore action bar messages

        String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());

        // Hypixel's exact Watchdog ban messages
        if (!unformatted.contains("A player has been removed from your game") &&
                !unformatted.contains("A player has been removed from your lobby")) {
            return;
        }

        runBanDetection();
    }

    /**
     * Core ban detection logic — shared between the real chat event and the dev test trigger.
     */
    public void runBanDetection() {
        // --- BAN MESSAGE DETECTED ---
        long now = System.currentTimeMillis();
        java.util.List<Map.Entry<String, Long>> sortedCandidates = new java.util.ArrayList<>();

        for (Map.Entry<String, Long> entry : candidates.entrySet()) {
            String name = entry.getKey();
            // Skip anyone already reported this session
            if (alreadyReported.contains(name)) continue;

            // Only include candidates who left recently enough to be plausible
            if (now - entry.getValue() <= CANDIDATE_TTL_MS) {
                sortedCandidates.add(entry);
            }
        }

        if (sortedCandidates.isEmpty()) {
            // No valid candidate found
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" +
                                EnumChatFormatting.GRAY + "] " +
                                EnumChatFormatting.DARK_GRAY + "Could not find banned player name."));
            }
            return;
        }

        // Sort candidates so the ones who left CLOSEST to the ban message time are first
        sortedCandidates.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        // Mitigation: If there are still too many candidates, instead of aborting,
        // we just take the top closest ones up to the threshold.
        if (sortedCandidates.size() > MASS_LEAVE_THRESHOLD) {
            sortedCandidates = sortedCandidates.subList(0, MASS_LEAVE_THRESHOLD);
        }

        // Color communicates confidence — no extra label text:
        // Exactly 1 candidate = HIGH confidence → RED
        // Multiple candidates = MEDIUM confidence → YELLOW
        boolean isHighConfidence = (sortedCandidates.size() == 1);

        for (Map.Entry<String, Long> entry : sortedCandidates) {
            String bannedPlayer = entry.getKey();
            // Mark as reported so we never double-report
            alreadyReported.add(bannedPlayer);
            candidates.remove(bannedPlayer);

            // Display in chat — color tells the story, no confidence label shown
            if (mc.thePlayer != null) {
                // RED = confirmed, YELLOW = uncertain
                EnumChatFormatting nameColor = isHighConfidence
                        ? EnumChatFormatting.RED
                        : EnumChatFormatting.YELLOW;

                mc.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GRAY + "[" +
                                EnumChatFormatting.RED + "Foxtrot" +
                                EnumChatFormatting.GRAY + "] " +
                                nameColor + "\u26A0 " +
                                EnumChatFormatting.BOLD + bannedPlayer +
                                EnumChatFormatting.RESET + nameColor + " has been banned!"));
            }

            // Send to API with confidence flag
            sendBanToDiscord(bannedPlayer, isHighConfidence);
        }
    }

    /**
     * Dev-only: directly triggers the ban detection logic as if Hypixel sent
     * "A player has been removed from your lobby" in chat.
     * Called from DevCommandHandler — never reachable in production.
     */
    public void simulateBanMessage() {
        runBanDetection();
    }

    /**
     * Resets all detection state when the player joins a new world/lobby.
     * Called from WorldLoadListener.
     */
    public void onLobbyJoin() {
        previousPlayers.clear();
        candidates.clear();

        alreadyReported.clear();
        lobbyJoinTime = System.currentTimeMillis(); // Start the 10-second grace period
    }

    // Sends { "usernames": ["Name"], "confident": true/false } to the Foxtrot ban
    // API
    private void sendBanToDiscord(String username, boolean isHighConfidence) {
        if (username == null || username.isEmpty())
            return;

        new Thread(() -> {
            try {
                URL url = new URL(PROXY_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // Array-based payload with confidence flag
                String jsonPayload = "{\"usernames\": [\"" + username + "\"], \"confident\": " + isHighConfidence + "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    System.out.println("[Foxtrot] API rejected ban payload. Code: " + responseCode);
                } else {
                    System.out.println(
                            "[Foxtrot] Ban alert sent for: " + username + " (confident=" + isHighConfidence + ")");
                }

            } catch (Exception e) {
                System.out.println("[Foxtrot] Failed to send ban alert: " + e.getMessage());
            }
        }).start();
    }
}