package com.linexstudios.foxtrot.Hud;

import com.linexstudios.foxtrot.Misc.EnchantNames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class NameTags {
    public static final NameTags instance = new NameTags();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = false;
    public static boolean showHealth = true;
    public static boolean showItems = true;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!enabled || mc.thePlayer == null || mc.theWorld == null) return;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer || player.isDead || player.isInvisible() || player.getUniqueID().version() == 2) continue; 

            double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks - mc.getRenderManager().viewerPosX;
            double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks - mc.getRenderManager().viewerPosY;
            double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks - mc.getRenderManager().viewerPosZ;

            renderNameTag(player, x, y, z);
        }
    }

    private void renderNameTag(EntityPlayer player, double x, double y, double z) {
        float distance = mc.thePlayer.getDistanceToEntity(player);
        float scale = Math.max((distance / 4.0F) * 0.010F, 0.015F);

        GlStateManager.pushMatrix();

        // 1. OPENMYAU ANTI-DESYNC FIX: Force GL11 and GlStateManager to sync.
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.enableBlend();
        GL11.glDisable(GL11.GL_LIGHTING);
        GlStateManager.disableLighting();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GlStateManager.translate((float)x, (float)y + player.height + 0.6F, (float)z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        String name = player.getDisplayName().getFormattedText();
        if (showHealth) {
            float health = player.getHealth() + player.getAbsorptionAmount();
            float percentage = health / player.getMaxHealth();
            String colorCode = percentage <= 0.25f ? "\u00a7c" : percentage <= 0.50f ? "\u00a76" : percentage <= 0.75f ? "\u00a7e" : "\u00a7a"; 
            name = name + " " + colorCode + String.format("%.1f", health);
        }

        int width = mc.fontRendererObj.getStringWidth(name) / 2;

        String swordEnchants = EnchantNames.getEnchantsString(player.getHeldItem());
        String pantsEnchants = EnchantNames.getEnchantsString(player.getCurrentArmor(1)); 
        
        boolean hasSword = swordEnchants != null && !swordEnchants.isEmpty();
        boolean hasPants = pantsEnchants != null && !pantsEnchants.isEmpty();
        
        int swordWidth = hasSword ? mc.fontRendererObj.getStringWidth(swordEnchants) / 2 : 0;
        int pantsWidth = hasPants ? mc.fontRendererObj.getStringWidth(pantsEnchants) / 2 : 0;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.disableTexture2D();
        
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        
        drawRect(worldrenderer, -width - 2, -2, width + 2, 11);

        int currentYOffset = -14;
        if (hasSword) {
            drawRect(worldrenderer, -swordWidth - 2, currentYOffset, swordWidth + 2, currentYOffset + 13);
            currentYOffset -= 12;
        }
        if (hasPants) {
            drawRect(worldrenderer, -pantsWidth - 2, currentYOffset, pantsWidth + 2, currentYOffset + 13);
        }

        tessellator.draw();
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableTexture2D();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 
        
        mc.fontRendererObj.drawStringWithShadow(name, -width, 0, -1);
        
        currentYOffset = -12;
        if (hasSword) {
            mc.fontRendererObj.drawStringWithShadow(swordEnchants, -swordWidth, currentYOffset, -1);
            currentYOffset -= 12;
        }
        if (hasPants) {
            mc.fontRendererObj.drawStringWithShadow(pantsEnchants, -pantsWidth, currentYOffset, -1);
        }

        // RENDER ITEMS (Armor Layout Restored + Glint Sandboxed)
        if (showItems) {
            List<ItemStack> items = new ArrayList<>();
            if (player.getHeldItem() != null) items.add(player.getHeldItem());
            for (int i = 3; i >= 0; i--) { 
                if (player.inventory.armorInventory[i] != null) items.add(player.inventory.armorInventory[i]);
            }

            if (!items.isEmpty()) {
                int startX = -(items.size() * 16) / 2;
                int itemY = hasPants ? -44 : (hasSword ? -32 : -18); 

                GlStateManager.pushMatrix();

                // GLINT FIX: Push Texture Matrix to sandbox the glint texture from the hotbar
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);

                GlStateManager.enableRescaleNormal();
                RenderHelper.enableGUIStandardItemLighting();

                for (ItemStack item : items) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(startX, itemY, 0);
                    
                    // PERFECT ARMOR LAYOUT: Disabled depth and flattened scale like your old code
                    GlStateManager.scale(1.0F, 1.0F, 0.01F);
                    GlStateManager.disableDepth();
                    
                    float prevZ = mc.getRenderItem().zLevel;
                    mc.getRenderItem().zLevel = -150.0F; 
                    mc.getRenderItem().renderItemIntoGUI(item, 0, 0);
                    mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, item, 0, 0);
                    mc.getRenderItem().zLevel = prevZ;
                    
                    GlStateManager.popMatrix();
                    startX += 16;
                }

                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();

                // GLINT FIX: Pop the texture matrix to prevent it bleeding
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);

                GlStateManager.popMatrix();
            }
        }

        // 2. CLEANUP: Restore Vanilla State for Myau
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableTexture2D();
        // Lighting remains disabled as expected by RenderWorldLastEvent
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.disableBlend();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.popMatrix();
    }

    private void drawRect(WorldRenderer renderer, int minX, int minY, int maxX, int maxY) {
        renderer.pos(minX, minY, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        renderer.pos(minX, maxY, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        renderer.pos(maxX, maxY, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        renderer.pos(maxX, minY, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
    }

    @SubscribeEvent
    public void onRenderVanillaNametag(RenderLivingEvent.Specials.Pre event) {
        if (enabled && event.entity instanceof EntityPlayer && event.entity.getUniqueID().version() != 2) {
            event.setCanceled(true);
        }
    }
}