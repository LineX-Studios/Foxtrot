package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Handler.MapDetectionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;

public class PotionHUD extends DraggableHUD {
    public static final PotionHUD instance = new PotionHUD();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;
    public static int nameColor = 0xFFFFFF;
    public static int durationColor = 0xAAAAAA;

    public PotionHUD() { super("Potion Status", 10, 10); }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        render(false, 0, 0); 
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled || mc.theWorld == null || mc.thePlayer == null) return;

        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        
        if (isEditing && effects.isEmpty()) {
            drawDummyPotions();
            return;
        }

        if (effects.isEmpty()) {
            this.width = 0;
            this.height = 0;
            return;
        }

        MapDetectionHandler.updateMap();
        FontRenderer fr = mc.fontRendererObj;
        int currentY = 5;
        int maxWidth = 0;

        // Calculate dimensions first
        for (PotionEffect effect : effects) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            String name = net.minecraft.client.resources.I18n.format(potion.getName());
            if (effect.getAmplifier() == 1) name += " II";
            else if (effect.getAmplifier() == 2) name += " III";
            int w = 18 + fr.getStringWidth(name);
            if (w > maxWidth) maxWidth = w;
        }

        this.width = Math.max(80, maxWidth + 10);
        this.height = (effects.size() * 20) + 5;

        for (PotionEffect effect : effects) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            String name = net.minecraft.client.resources.I18n.format(potion.getName());
            if (effect.getAmplifier() == 1) name += " II";
            else if (effect.getAmplifier() == 2) name += " III";
            
            String duration = Potion.getDurationString(effect);

            // Draw Icon
            if (potion.hasStatusIcon()) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.enableTexture2D();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(new net.minecraft.util.ResourceLocation("textures/gui/container/inventory.png"));
                int iconIndex = potion.getStatusIconIndex();
                int iconX = (iconIndex % 8) * 18;
                int iconY = 198 + (iconIndex / 8) * 18;
                
                GlStateManager.scale(0.7f, 0.7f, 1.0f);
                net.minecraft.client.gui.Gui gui = new net.minecraft.client.gui.Gui();
                gui.drawTexturedModalRect((int)(5 / 0.7f), (int)(currentY / 0.7f) + 2, iconX, iconY, 18, 18);
                GlStateManager.popMatrix();
            }

            fr.drawStringWithShadow(name, 20, currentY, nameColor);
            fr.drawStringWithShadow(duration, 20, currentY + 9, durationColor);

            currentY += 20;
        }
    }



    private void drawDummyPotions() {
        FontRenderer fr = mc.fontRendererObj;
        int currentY = 5;
        int maxWidth = 0;

        String[] names = {"Speed II", "Regeneration I"};
        String[] durations = {"1:30", "0:45"};
        int[] iconIndices = {1, 10}; // Speed and Regen

        for (int i = 0; i < names.length; i++) {
            int w = 18 + fr.getStringWidth(names[i]);
            if (w > maxWidth) maxWidth = w;
        }
        this.width = Math.max(80, maxWidth + 10);
        this.height = (names.length * 20) + 5;

        for (int i = 0; i < names.length; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(new net.minecraft.util.ResourceLocation("textures/gui/container/inventory.png"));
            int iconX = (iconIndices[i] % 8) * 18;
            int iconY = 198 + (iconIndices[i] / 8) * 18;
            
            GlStateManager.scale(0.7f, 0.7f, 1.0f);
            net.minecraft.client.gui.Gui gui = new net.minecraft.client.gui.Gui();
            gui.drawTexturedModalRect((int)(5 / 0.7f), (int)(currentY / 0.7f) + 2, iconX, iconY, 18, 18);
            GlStateManager.popMatrix();

            fr.drawStringWithShadow(names[i], 20, currentY, nameColor);
            fr.drawStringWithShadow(durations[i], 20, currentY + 9, durationColor);

            currentY += 20;
        }
    }
}