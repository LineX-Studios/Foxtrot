package com.linexstudios.foxtrot.Util;

import com.linexstudios.foxtrot.Util.discord.DiscordIPC;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordRPCManager {

    private static final String APP_ID = "1485017587875189037";
    private static DiscordIPC ipc;
    private static boolean running = false;
    private static long startTime = 0;
    private static ScheduledExecutorService scheduler;

    public static void start() {
        if (running || !com.linexstudios.foxtrot.Handler.ConfigHandler.discordRpcEnabled)
            return;

        try {
            ipc = new DiscordIPC(APP_ID);
            ipc.connect();

            startTime = System.currentTimeMillis() / 1000;
            running = true;

            updatePresence();

            // Periodically refresh the presence (every 20 seconds) to ensure it stays
            // visible
            // This is what 'Activity Status' toggle mods do to remain active.
            // Use a daemon thread so the JVM can exit without blocking when the game is closed
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("Foxtrot-Discord-RPC");
                return t;
            });
            scheduler.scheduleAtFixedRate(DiscordRPCManager::updatePresence, 20, 20, TimeUnit.SECONDS);
            // Register a shutdown hook to clean up Discord RPC when the JVM exits
            Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPCManager::stop));

            System.out.println("Foxtrot: Discord RPC started successfully.");
        } catch (Exception e) {
            System.err.println("Foxtrot: Failed to start Discord RPC: " + e.getMessage());
        }
    }

    public static void stop() {
        if (!running)
            return;

        running = false;
        // Run cleanup in a separate thread to avoid hanging the shutdown hook
        Thread cleanupThread = new Thread(() -> {
            try {
                if (scheduler != null) {
                    scheduler.shutdownNow();
                    scheduler = null;
                }
                if (ipc != null) {
                    ipc.close();
                    ipc = null;
                }
            } catch (Exception e) {
                // Ignore errors during final exit
            }
        }, "Foxtrot-Shutdown-Cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public static void updatePresence() {
        if (!running || ipc == null)
            return;

        try {
            ipc.setPresence(
                    "Foxtrot 1.8.9 Pit Mod",
                    "Playing Hypixel Pit",
                    startTime,
                    "foxtrot_logo_512x512",
                    "Foxtrot");
        } catch (Exception e) {
            // Silently attempt to reconnect in the next interval
        }
    }
}
