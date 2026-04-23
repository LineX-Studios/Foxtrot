package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FPSModule extends DraggableHUD {
    public static final FPSModule instance = new FPSModule();
    private final Minecraft mc = Minecraft.getMinecraft();

    // --- GUI Settings ---
    public static boolean enabled = true;
    public static boolean showBackground = true;
    public static int textColor = -1; // White
    public static int backgroundColor = 0x6F000000; // CheatBreaker Default Black Translucent

    public FPSModule() {
        super("FPS", 10, 130);
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
        int currentFPS = Minecraft.getDebugFPS();
        
        if (showBackground) {
            this.width = 56;
            this.height = 16; 
            
            // Draw CheatBreaker style background
            Gui.drawRect(0, 0, this.width, this.height, backgroundColor);
            
            String text = currentFPS + " FPS";
            int textWidth = fr.getStringWidth(text);
            
            // Draw centered string without shadow
            fr.drawString(text, (this.width / 2) - (textWidth / 2), 4, textColor);
        } else {
            String text = "[" + currentFPS + " FPS]";
            this.width = fr.getStringWidth(text);
            this.height = fr.FONT_HEIGHT + 2;
            
            // Draw text with shadow when background is disabled
            fr.drawStringWithShadow(text, 0, 2, textColor);
        }
    }
}