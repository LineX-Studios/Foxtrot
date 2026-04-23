package com.linexstudios.foxtrot.Handler;

import net.minecraft.entity.player.EntityPlayer;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class APIHandler {

    // --- Pitmart Data (Used for Gold Remaining) ---
    public static int prestige = 0;
    public static int level = 0;
    public static double goldLeft = 0.0;
    public static double goldProportion = 0.0;

    // --- Pit Panda Data (Used for XP and XP/Hour) ---
    public static double pitPandaXpHourly = 0.0;
    public static double pitPandaXpPercent = 0.0;
    public static String pitPandaXpDescription = "";
    public static long pitPandaXpCurrent = 0;
    public static long pitPandaXpGoal = 0;

    public static boolean isLoaded = false;
    private static long lastFetchTime = 0;

    public static void updateStats(EntityPlayer player) {
        if (player == null) return;

        long currentTime = System.currentTimeMillis();
        // 30 seconds cooldown to prevent rate limiting
        if (currentTime - lastFetchTime < 30000) return;

        lastFetchTime = currentTime;

        // Run async so Minecraft doesn't freeze
        new Thread(() -> {
            try {
                String uuid = player.getUniqueID().toString().replace("-", "");

                // 1. Fetch from Pitmart
                URL pitmartUrl = new URL("https://pitmart.net/api/player/" + uuid);
                HttpURLConnection conn1 = (HttpURLConnection) pitmartUrl.openConnection();
                conn1.setRequestMethod("GET");
                conn1.setRequestProperty("User-Agent", "Mozilla/5.0");

                if (conn1.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) content.append(inputLine);
                    in.close();

                    JSONObject json = new JSONObject(content.toString());
                    prestige = json.optInt("prestige", 0);
                    level = json.optInt("level", 1);
                    goldLeft = json.optDouble("prestigeGoldLeft", 0.0);
                    goldProportion = json.optDouble("prestigeGoldReqProportion", 0.0);
                }
                conn1.disconnect();

                // 2. Fetch from Pit Panda
                URL pandaUrl = new URL("https://pitpanda.rocks/api/players/" + uuid);
                HttpURLConnection conn2 = (HttpURLConnection) pandaUrl.openConnection();
                conn2.setRequestMethod("GET");
                conn2.setRequestProperty("User-Agent", "Mozilla/5.0");

                if (conn2.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) content.append(inputLine);
                    in.close();

                    JSONObject json = new JSONObject(content.toString());
                    
                    if (json.has("data")) {
                        JSONObject data = json.getJSONObject("data");
                        
                        if (data.has("xpHourly")) pitPandaXpHourly = data.optDouble("xpHourly", 0.0);
                        
                        if (data.has("xpProgress")) {
                            JSONObject xpProgress = data.getJSONObject("xpProgress");
                            
                            // ANTI-LAG: Only accept the API's current XP if it is HIGHER than our live local tracking
                            long apiCurrent = xpProgress.optLong("displayCurrent", 0);
                            if (apiCurrent > pitPandaXpCurrent || pitPandaXpCurrent == 0) {
                                pitPandaXpCurrent = apiCurrent;
                            }
                            
                            pitPandaXpGoal = xpProgress.optLong("displayGoal", 0);
                            
                            // Re-calculate the percent and description immediately after an API update
                            if (pitPandaXpGoal > 0) {
                                pitPandaXpPercent = ((double) pitPandaXpCurrent / pitPandaXpGoal);
                                pitPandaXpDescription = String.format("%.2fk/%.2fk", pitPandaXpCurrent / 1000.0, pitPandaXpGoal / 1000.0);
                            }
                        }
                    }
                }
                conn2.disconnect();

                isLoaded = true;
            } catch (Exception e) {
                System.out.println("[Foxtrot] Failed to fetch data from APIs.");
                e.printStackTrace();
            }
        }).start();
    }

    public static boolean isGoldReqMet() {
        return goldLeft <= 0;
    }

    public static String getFormattedGoldLeft() {
        if (isGoldReqMet()) {
            return "Met!";
        } else {
            return String.format("%,.0f", goldLeft);
        }
    }
}