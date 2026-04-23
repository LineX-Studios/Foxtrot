package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;

public class PotionHUD extends DraggableHUD {
    public static PotionHUD instance = new PotionHUD();
    public static boolean enabled = true;

    public static int nameColor = 0xFFFFFF;
    public static int durationColor = 0xAAAAAA;

    private Minecraft mc = Minecraft.getMinecraft();
    private static final ResourceLocation INVENTORY_RESOURCE = new ResourceLocation("textures/gui/container/inventory.png");
    private int ticks = 0;

    public PotionHUD() {
        super("Potion Status", 10, 50); 
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ticks++;
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.currentScreen instanceof EditHUDGui) return;
        if (mc.currentScreen instanceof HUDSettingsGui) return;
        render(false, 0, 0); 
    }

    private String getLevelName(int level) {
        switch (level) {
            case 0: return "";
            case 1: return " II";
            case 2: return " III";
            case 3: return " IV";
            case 4: return " V";
            case 5: return " VI";
            case 6: return " VII";
            case 7: return " VIII";
            case 8: return " IX";
            case 9: return " X";
        }
        if (level > 9) {
            return " " + (level + 1);
        }
        return "";
    }

    private boolean shouldBlink(int duration) {
        if (duration <= 200) { // 10 seconds
            if (this.ticks > 20) {
                this.ticks = 0;
            }
            return this.ticks <= 10;
        }
        return true;
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled || mc.theWorld == null) return;

        FontRenderer fr = mc.fontRendererObj;
        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        
        // ==========================================
        //         ISOLATED DUMMY RENDERING
        // ==========================================
        if (isEditing && effects.isEmpty()) {
            this.width = 80;
            this.height = 45;
            
            // STRICT STATE RESET FOR DUMMY RENDERING
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            
            // Dummy Icon 1 (Speed)
            mc.getTextureManager().bindTexture(INVENTORY_RESOURCE);
            mc.ingameGUI.drawTexturedModalRect(5, 5, 0, 198, 18, 18);
            fr.drawStringWithShadow("Speed II", 27, 5, nameColor);
            fr.drawStringWithShadow("1:30", 27, 15, durationColor);
            
            // Dummy Icon 2 (Strength)
            mc.getTextureManager().bindTexture(INVENTORY_RESOURCE);
            mc.ingameGUI.drawTexturedModalRect(5, 25, 18, 198, 18, 18);
            fr.drawStringWithShadow("Strength I", 27, 25, nameColor);
            fr.drawStringWithShadow("0:45", 27, 35, durationColor);
            return;
        }

        if (effects.isEmpty()) {
            this.width = 0;
            this.height = 0;
            return;
        }

        int currentY = 5;
        int maxWidth = 50; 

        // ==========================================
        //           REAL POTION RENDERING
        // ==========================================
        for (PotionEffect effect : effects) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            String name = I18n.format(potion.getName()) + getLevelName(effect.getAmplifier());
            String duration = Potion.getDurationString(effect);
            boolean blink = shouldBlink(effect.getDuration());

            // STRICT STATE RESET PER ICON
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            
            mc.getTextureManager().bindTexture(INVENTORY_RESOURCE);
            
            if (potion.hasStatusIcon()) {
                int iconIndex = potion.getStatusIconIndex();
                int u = iconIndex % 8 * 18;
                int v = 198 + iconIndex / 8 * 18;
                mc.ingameGUI.drawTexturedModalRect(5, currentY, u, v, 18, 18);
            }

            int textX = 27; 
            fr.drawStringWithShadow(name, textX, currentY, nameColor);
            if (blink) {
                fr.drawStringWithShadow(duration, textX, currentY + 10, durationColor);
            }

            int w = textX + Math.max(fr.getStringWidth(name), fr.getStringWidth(duration));
            if (w > maxWidth) maxWidth = w;

            currentY += 22; 
        }

        this.width = maxWidth + 5;
        this.height = currentY;
    }
}