package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventHUD extends DraggableHUD {
    public static final EventHUD instance = new EventHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;

    private int tickTimer = 0;

    // Thread-safe list to store our API results
    private final List<PitEvent> upcomingEvents = new ArrayList<>();
    private final Object eventsLock = new Object();

    public EventHUD() {
        super("Event Tracker", 10, 250); // FIXED CONSTRUCTOR
        fetchEvents();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return;

        tickTimer++;
        if (tickTimer >= 6000) {
            tickTimer = 0;
            fetchEvents();
        }
    }

    private void fetchEvents() {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/BrookeAFK/brookeafk-api/main/events.js");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(content.toString());
                    List<PitEvent> parsedEvents = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject eventObj = jsonArray.getJSONObject(i);
                        String eventName = eventObj.optString("event", "");
                        long timestamp = eventObj.getLong("timestamp");

                        if (isTrackedEvent(eventName)) {
                            parsedEvents.add(new PitEvent(eventName, timestamp, getColorForEvent(eventName)));
                        }
                    }

                    parsedEvents.sort(Comparator.comparingLong(e -> e.timestamp));

                    synchronized (eventsLock) {
                        upcomingEvents.clear();
                        upcomingEvents.addAll(parsedEvents);
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                System.out.println("[Foxtrot] Failed to fetch Pit Events.");
                e.printStackTrace();
            }
        }).start();
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.currentScreen instanceof EditHUDGui) return;
        if (mc.currentScreen instanceof HUDSettingsGui) return;
        render(false, 0, 0); 
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled || mc.theWorld == null) return;

        FontRenderer fr = mc.fontRendererObj;
        int currentY = 0; 

        String header = EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "Upcoming Events:";
        fr.drawStringWithShadow(header, 0, currentY, 0xFFFFFF);
        int maxWidth = fr.getStringWidth(header);
        currentY += fr.FONT_HEIGHT + 2;

        synchronized (eventsLock) {
            int displayedCount = 0;
            long currentTime = System.currentTimeMillis();

            for (PitEvent e : upcomingEvents) {
                if (e.timestamp <= currentTime) continue;

                String timeStr = formatTime(e.timestamp, currentTime);
                String line = e.color + e.name + EnumChatFormatting.GRAY + " in " + EnumChatFormatting.WHITE + timeStr;

                fr.drawStringWithShadow(line, 0, currentY, 0xFFFFFF);
                maxWidth = Math.max(maxWidth, fr.getStringWidth(line));
                currentY += fr.FONT_HEIGHT + 2;

                displayedCount++;
                if (displayedCount >= 6) break; 
            }

            if (displayedCount == 0 && !isEditing) {
                String noneText = EnumChatFormatting.GRAY + "Loading events...";
                fr.drawStringWithShadow(noneText, 0, currentY, 0xFFFFFF);
                maxWidth = Math.max(maxWidth, fr.getStringWidth(noneText));
                currentY += fr.FONT_HEIGHT + 2;
            } else if (displayedCount == 0 && isEditing) {
                String dummyText1 = EnumChatFormatting.DARK_PURPLE + "Dragon Egg" + EnumChatFormatting.GRAY + " in " + EnumChatFormatting.WHITE + "5m03s";
                String dummyText2 = EnumChatFormatting.AQUA + "Squads" + EnumChatFormatting.GRAY + " in " + EnumChatFormatting.WHITE + "59s";

                fr.drawStringWithShadow(dummyText1, 0, currentY, 0xFFFFFF);
                maxWidth = Math.max(maxWidth, fr.getStringWidth(dummyText1));
                currentY += fr.FONT_HEIGHT + 2;

                fr.drawStringWithShadow(dummyText2, 0, currentY, 0xFFFFFF);
                maxWidth = Math.max(maxWidth, fr.getStringWidth(dummyText2));
                currentY += fr.FONT_HEIGHT + 2;
            }
        }

        this.width = maxWidth;
        this.height = currentY;
    }

    private String formatTime(long timestamp, long currentTime) {
        long diff = timestamp - currentTime;
        if (diff <= 60000) {
            long seconds = diff / 1000;
            return seconds + "s";
        } else if (diff <= 3600000) {
            long minutes = diff / 60000;
            long seconds = (diff / 1000) % 60;
            return String.format("%dm%02ds", minutes, seconds);
        } else {
            long hours = diff / 3600000;
            long minutes = (diff / 60000) % 60;
            return String.format("%dh%02dm", hours, minutes);
        }
    }

    private boolean isTrackedEvent(String name) {
        String lower = name.toLowerCase();
        return lower.equals("blockhead") || lower.equals("pizza") || lower.equals("beast") ||
                lower.equals("robbery") || lower.equals("spire") || lower.equals("squads") ||
                lower.equals("team deathmatch") || lower.equals("raffle") || lower.equals("rage pit") ||
                lower.equals("2x rewards") || lower.equals("giant cake") || lower.equals("kotl") ||
                lower.equals("dragon egg") || lower.equals("auction") || lower.equals("quick maths") ||
                lower.equals("koth") || lower.equals("care package") || lower.equals("all bounty");
    }

    private EnumChatFormatting getColorForEvent(String name) {
        switch (name.toLowerCase()) {
            case "blockhead": return EnumChatFormatting.GOLD;
            case "pizza": return EnumChatFormatting.RED;
            case "beast": return EnumChatFormatting.GREEN;
            case "robbery": return EnumChatFormatting.GOLD;
            case "spire": return EnumChatFormatting.DARK_PURPLE;
            case "squads": return EnumChatFormatting.AQUA;
            case "team deathmatch": return EnumChatFormatting.DARK_PURPLE;
            case "raffle": return EnumChatFormatting.GOLD;
            case "rage pit": return EnumChatFormatting.RED;
            case "2x rewards": return EnumChatFormatting.DARK_GREEN;
            case "giant cake": return EnumChatFormatting.LIGHT_PURPLE;
            case "kotl": return EnumChatFormatting.GREEN;
            case "dragon egg": return EnumChatFormatting.DARK_PURPLE;
            case "auction": return EnumChatFormatting.YELLOW;
            case "quick maths": return EnumChatFormatting.DARK_PURPLE;
            case "koth": return EnumChatFormatting.AQUA;
            case "care package": return EnumChatFormatting.GOLD;
            case "all bounty": return EnumChatFormatting.GOLD;
            default: return EnumChatFormatting.WHITE;
        }
    }

    private static class PitEvent {
        public final String name;
        public final long timestamp;
        public final EnumChatFormatting color;

        public PitEvent(String name, long timestamp, EnumChatFormatting color) {
            this.name = name;
            this.timestamp = timestamp;
            this.color = color;
        }
    }
}