package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Handler.MapDetectionHandler;
import com.linexstudios.foxtrot.Handler.PlayerTrackerHandler;
import com.linexstudios.foxtrot.Util.SpawnRegions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class FriendsHUD extends DraggableHUD {
    public static final FriendsHUD instance = new FriendsHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;
    public static List<String> friendsList = new ArrayList<>();

    public FriendsHUD() { super("Friend List", 400, 80); }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.currentScreen instanceof EditHUDGui || mc.currentScreen instanceof HUDSettingsGui) return;
        render(false, 0, 0); 
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled || mc.theWorld == null || mc.thePlayer == null) return;
        
        MapDetectionHandler.updateMap();

        FontRenderer fr = mc.fontRendererObj;
        int currentY = 0; 
        int maxWidth = fr.getStringWidth("Friend List");

        List<HUDRow> rows = new ArrayList<>();
        for (PlayerTrackerHandler.TrackedPlayer player : PlayerTrackerHandler.activePlayers.values()) {
            if (player.name.equals(mc.thePlayer.getName())) continue;
            if (!isFriend(player.uuid, player.name)) continue;

            EntityOtherPlayerMP other = player.entity;
            String displayName = player.lastKnownNamePlate != null ? player.lastKnownNamePlate : (EnumChatFormatting.GRAY + "[?] " + EnumChatFormatting.GREEN + player.name);
            displayName = truncate(displayName, 32);

            String finalDisplayName = EnumChatFormatting.DARK_GREEN + "[" + EnumChatFormatting.GREEN + "F" + EnumChatFormatting.DARK_GREEN + "] " + EnumChatFormatting.RESET + displayName;

            String locStr = "";
            if (other != null) {
                int distance = Math.round(mc.thePlayer.getDistanceToEntity(other));
                EnumChatFormatting distColor = (distance >= 100) ? EnumChatFormatting.GREEN : (distance >= 60) ? EnumChatFormatting.YELLOW : (distance >= 30) ? EnumChatFormatting.GOLD : EnumChatFormatting.RED;
                locStr = SpawnRegions.getRegionString(other) + EnumChatFormatting.GRAY + " (" + distColor + distance + "m" + EnumChatFormatting.GRAY + ")";
            }

            rows.add(new HUDRow(finalDisplayName, locStr));
        }

        if (rows.isEmpty()) {
            if (isEditing) {
                String placeholder = EnumChatFormatting.DARK_GREEN + "[" + EnumChatFormatting.GREEN + "F" + EnumChatFormatting.DARK_GREEN + "] " + EnumChatFormatting.GRAY + "[120] Friend";
                rows.add(new HUDRow(placeholder, EnumChatFormatting.GREEN + "SPAWN (15m)"));
            } else {
                this.width = 0; this.height = 0;
                return;
            }
        }

        int maxNameW = 0;
        for (HUDRow row : rows) {
            maxNameW = Math.max(maxNameW, fr.getStringWidth(row.name));
        }

        fr.drawStringWithShadow(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "Friend List:", 0, currentY, 0xFFFFFF);
        currentY += fr.FONT_HEIGHT + 2;

        int locX = maxNameW + 10;

        for (HUDRow row : rows) {
            fr.drawStringWithShadow(row.name, 0, currentY, 0xFFFFFF);
            if (!row.loc.isEmpty()) {
                fr.drawStringWithShadow(EnumChatFormatting.GRAY + "- " + row.loc, locX, currentY, 0xFFFFFF);
            }
            currentY += fr.FONT_HEIGHT;
        }

        for (HUDRow row : rows) {
            int w = locX + (row.loc.isEmpty() ? 0 : fr.getStringWidth("- " + row.loc));
            if (w > maxWidth) maxWidth = w;
        }
        
        this.width = maxWidth;
        this.height = currentY;
    }

    public static boolean isFriend(String uuid, String name) {
        if (uuid != null && com.linexstudios.foxtrot.Handler.FriendsManager.friendCache.containsKey(uuid)) return true;
        return friendsList.stream().anyMatch(f -> f.equalsIgnoreCase(name));
    }

    private static class HUDRow {
        String name, loc;
        HUDRow(String n, String l) { this.name = n; this.loc = l; }
    }
}