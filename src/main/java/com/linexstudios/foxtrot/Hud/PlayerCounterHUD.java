package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PlayerCounterHUD extends DraggableHUD {
    public static PlayerCounterHUD instance = new PlayerCounterHUD();
    public static boolean enabled = true;

    // Independent colors
    public static int prefixColor = 0xFFFFFF; // Color for "Current Players: "
    public static int countColor = 0xAAAAAA;  // Color for "21/81"

    private int maxPlayers = 81;
    private int players = 0;
    private int ticks = 0;
    private Minecraft mc = Minecraft.getMinecraft();

    public PlayerCounterHUD() {
        super("Player Counter", 10, 70); 
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && mc.theWorld != null) {
            ticks++;
            // Scan every 20 ticks (1 second) to prevent network lag
            if (ticks >= 20) {
                ticks = 0;
                // Only update the player count if we are actually in the pit
                if (isInPit() && mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfoMap() != null) {
                    players = mc.getNetHandler().getPlayerInfoMap().size();
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.currentScreen instanceof EditHUDGui) return;
        if (mc.currentScreen instanceof HUDSettingsGui) return;
        render(false, 0, 0); 
    }

    // --- HYPIXEL PIT DETECTOR ---
    public boolean isInPit() {
        if (mc.theWorld == null || mc.thePlayer == null) return false;
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return false;
        
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;
        
        String title = StringUtils.stripControlCodes(objective.getDisplayName());
        return title.contains("THE HYPIXEL PIT") || title.contains("PIT");
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled || mc.theWorld == null) return;
        
        // Hides the HUD if you are NOT in the Pit (unless you are editing the HUD!)
        if (!isEditing && !isInPit()) return;

        FontRenderer fr = mc.fontRendererObj;
        String prefix = "Current Players: ";
        String countText;

        if (isEditing) {
            countText = "50/81"; // Dummy text for GUI
        } else {
            countText = players + "/" + maxPlayers; // Real text in-game
        }

        int prefixWidth = fr.getStringWidth(prefix);
        int countWidth = fr.getStringWidth(countText);

        // Update the bounding box for DraggableHUD so it can be clicked/dragged
        this.width = prefixWidth + countWidth;
        this.height = fr.FONT_HEIGHT;

        // Draw the two parts independently with their own colors
        fr.drawStringWithShadow(prefix, 0, 0, prefixColor);
        fr.drawStringWithShadow(countText, prefixWidth, 0, countColor);
    }
}