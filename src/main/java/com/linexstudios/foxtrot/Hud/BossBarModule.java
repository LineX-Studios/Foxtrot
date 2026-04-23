package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.entity.boss.BossStatus;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BossBarModule extends DraggableHUD {
    public static final BossBarModule instance = new BossBarModule();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;

    public BossBarModule() {
        super("Boss Bar", 100, 10);
        this.width = 182;
        this.height = 16;
    }

    @SubscribeEvent
    public void onRenderPre(RenderGameOverlayEvent.Pre event) {
        if (enabled && event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH) {
            
            event.setCanceled(true);
            
            if (BossStatus.bossName != null && BossStatus.statusBarTime > 0) {
                BossStatus.statusBarTime--;
                
                render(false, 0, 0); 
            }
        }
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled && !isEditing) return;

        boolean hasRealBoss = BossStatus.bossName != null && BossStatus.statusBarTime > 0;
        if (!hasRealBoss && !isEditing) return;

        String bossName = hasRealBoss ? BossStatus.bossName : EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "BOSS BAR";
        float bossHealth = hasRealBoss ? BossStatus.healthScale : 1.0f;

        GlStateManager.pushMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        
        mc.getTextureManager().bindTexture(Gui.icons);
        FontRenderer fr = mc.fontRendererObj;
        
        int barWidth = 182;
        int filledWidth = (int)(bossHealth * (float)barWidth);
        int barY = 10;
        
        mc.ingameGUI.drawTexturedModalRect(0, barY, 0, 74, barWidth, 5);
        mc.ingameGUI.drawTexturedModalRect(0, barY, 0, 74, barWidth, 5);
        
        if (filledWidth > 0) {
            mc.ingameGUI.drawTexturedModalRect(0, barY, 0, 79, filledWidth, 5);
        }

        fr.drawStringWithShadow(bossName, (barWidth / 2.0f) - (fr.getStringWidth(bossName) / 2.0f), 0, 0xFFFFFF);
        
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        
        this.width = 182;
        this.height = 16;
    }
}