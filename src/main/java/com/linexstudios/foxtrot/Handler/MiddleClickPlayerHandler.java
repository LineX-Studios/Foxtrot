package com.linexstudios.foxtrot.Handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MiddleClickPlayerHandler {
    public static final MiddleClickPlayerHandler instance = new MiddleClickPlayerHandler();

    private final Minecraft mc = Minecraft.getMinecraft();
    private long lastMiddleClickTime = 0;

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        // Button 2 is middle click, buttonstate true means pressed down
        if (event.button == 2 && event.buttonstate) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMiddleClickTime < 1000) {
                return; // Prevent spamming within 1 second
            }

            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.ENTITY) {
                if (mc.objectMouseOver.entityHit instanceof EntityPlayer) {
                    EntityPlayer target = (EntityPlayer) mc.objectMouseOver.entityHit;
                    String uuid = target.getUniqueID().toString().replace("-", "");
                    String currentName = target.getName();
                    
                    lastMiddleClickTime = currentTime;
                    
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Fetching owner/name history for " + EnumChatFormatting.YELLOW + currentName + EnumChatFormatting.GRAY + "..."));

                    // Fetch asynchronously to avoid freezing the client
                    new Thread(() -> {
                        try {
                            URL url = new URL("https://api.mojang.com/user/profiles/" + uuid + "/names");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(5000);
                            connection.setReadTimeout(5000);
                            
                            if (connection.getResponseCode() != 200) {
                                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to fetch name history for " + currentName + " (Response: " + connection.getResponseCode() + ")"));
                                return;
                            }

                            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                            JsonArray array = new JsonParser().parse(reader).getAsJsonArray();
                            reader.close();

                            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "--- Owner / Name History for " + currentName + " ---"));
                            
                            for (int i = 0; i < array.size(); i++) {
                                JsonObject obj = array.get(i).getAsJsonObject();
                                String name = obj.get("name").getAsString();
                                String time = "";
                                if (obj.has("changedToAt")) {
                                    long timestamp = obj.get("changedToAt").getAsLong();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                    time = EnumChatFormatting.GRAY + " (Changed: " + sdf.format(new Date(timestamp)) + ")";
                                } else if (i == 0) {
                                    time = EnumChatFormatting.GRAY + " (Original Name)";
                                }
                                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "- " + name + time));
                            }
                            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "--------------------------------------"));

                        } catch (Exception e) {
                            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error occurred while fetching name history for " + currentName + "."));
                        }
                    }).start();
                }
            }
        }
    }
}
