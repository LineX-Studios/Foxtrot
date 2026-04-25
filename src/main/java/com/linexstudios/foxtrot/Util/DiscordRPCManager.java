package com.linexstudios.foxtrot.Util;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;

public class DiscordRPCManager {

    private static final String APP_ID = "1485017587875189037";
    private static boolean running = false;
    private static long startTime = 0;

    public static void start() {
        if (running)
            return;

        try {
            // Tell JNA where to look for natives inside our jar
            System.setProperty("jna.library.path", "");

            DiscordRPC lib = DiscordRPC.INSTANCE;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            lib.Discord_Initialize(APP_ID, handlers, true, null);

            startTime = System.currentTimeMillis() / 1000;
            running = true;

            new Thread(() -> {
                while (running) {
                    try {
                        lib.Discord_RunCallbacks();
                        updatePresence();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Foxtrot-RPC-Callback").start();
        } catch (Throwable t) {
            System.err.println("Foxtrot: Failed to start Discord RPC.");
            t.printStackTrace();

            // Send a message to the user if they are in-game
            new Thread(() -> {
                try {
                    // Wait for player to exist
                    for (int i = 0; i < 20; i++) {
                        if (Minecraft.getMinecraft().thePlayer != null) {
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                                    "§c[Foxtrot] §7Discord RPC failed to start: §f" + t.getMessage()));
                            break;
                        }
                        Thread.sleep(2000);
                    }
                } catch (Exception ignored) {
                }
            }).start();
        }
    }

    public static void stop() {
        if (!running)
            return;
        running = false;
        DiscordRPC.INSTANCE.Discord_Shutdown();
    }

    public static void updatePresence() {
        DiscordRichPresence discordPresence = new DiscordRichPresence();
        discordPresence.state = "Playing Hypixel Pit";
        discordPresence.details = "Foxtrot - Hypixel Pit Mod";
        discordPresence.startTimestamp = startTime;
        discordPresence.largeImageKey = "foxtrot_logo_512x512";
        discordPresence.largeImageText = "Foxtrot";

        DiscordRPC.INSTANCE.Discord_UpdatePresence(discordPresence);
    }
}
