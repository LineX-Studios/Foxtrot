package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class CPSModule extends DraggableHUD {
    public static final CPSModule instance = new CPSModule();
    private final Minecraft mc = Minecraft.getMinecraft();

    // --- GUI Settings ---
    public static boolean enabled = true;
    public static boolean showBackground = true;
    public static int textColor = -1; // White
    public static int backgroundColor = 0x6F000000;

    private final List<Long> clicks = new ArrayList<>();

    public CPSModule() {
        super("CPS", 10, 110);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Remove any clicks that are older than 1000ms (1 second)
            clicks.removeIf(time -> time < System.currentTimeMillis() - 1000L);
        }
    }

    @SubscribeEvent
    public void onClick(MouseEvent event) {
        // Detects Left Clicks (button == 0) and only when pressed down (buttonstate == true)
        if (event.button == 0 && event.buttonstate) {
            clicks.add(System.currentTimeMillis());
        }
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
        if (!enabled && !isEditing) return;

        FontRenderer fr = mc.fontRendererObj;
        
        if (showBackground) {
            this.width = 56;
            this.height = 16; 
            
            Gui.drawRect(0, 0, this.width, this.height, backgroundColor);
            
            String text = clicks.size() + " CPS";
            int textWidth = fr.getStringWidth(text);
            
            // Draw centered string without shadow (CheatBreaker style)
            fr.drawString(text, (this.width / 2) - (textWidth / 2), 4, textColor);
        } else {
            String text = "[" + clicks.size() + " CPS]";
            this.width = fr.getStringWidth(text);
            this.height = fr.FONT_HEIGHT + 2;
            
            // Draw text with shadow when background is disabled
            fr.drawStringWithShadow(text, 0, 2, textColor);
        }
    }
}