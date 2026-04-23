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
        GlStateManager.translate((float)x, (float)y + player.height + 0.6F, (float)z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth(); 
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        String name = player.getDisplayName().getFormattedText();
        if (showHealth) {
            float health = player.getHealth() + player.getAbsorptionAmount();
            float percentage = health / player.getMaxHealth();
            String colorCode = percentage <= 0.25f ? "\u00a7c" : percentage <= 0.50f ? "\u00a76" : percentage <= 0.75f ? "\u00a7e" : "\u00a7a"; 
            name = name + " " + colorCode + String.format("%.1f", health);
        }

        int width = mc.fontRendererObj.getStringWidth(name) / 2;

        // FETCH mystic enchants
        String swordEnchants = EnchantNames.getEnchantsString(player.getHeldItem());
        String pantsEnchants = EnchantNames.getEnchantsString(player.getCurrentArmor(1)); // Slot 1 is Leggings
        
        boolean hasSword = swordEnchants != null && !swordEnchants.isEmpty();
        boolean hasPants = pantsEnchants != null && !pantsEnchants.isEmpty();
        
        int swordWidth = hasSword ? mc.fontRendererObj.getStringWidth(swordEnchants) / 2 : 0;
        int pantsWidth = hasPants ? mc.fontRendererObj.getStringWidth(pantsEnchants) / 2 : 0;

        GlStateManager.disableTexture2D();
        
        // optimizing the shit out of this
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        
        // Background for Main Name (Bottom)
        drawRect(worldrenderer, -width - 2, -2, width + 2, 11);

        // Dynamic Stacking Logic for Backgrounds
        int currentYOffset = -14;
        if (hasSword) {
            drawRect(worldrenderer, -swordWidth - 2, currentYOffset, swordWidth + 2, currentYOffset + 13);
            currentYOffset -= 12;
        }
        if (hasPants) {
            drawRect(worldrenderer, -pantsWidth - 2, currentYOffset, pantsWidth + 2, currentYOffset + 13);
        }

        tessellator.draw();
        
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 
        
        // Draw Text
        mc.fontRendererObj.drawStringWithShadow(name, -width, 0, -1);
        
        currentYOffset = -12;
        if (hasSword) {
            mc.fontRendererObj.drawStringWithShadow(swordEnchants, -swordWidth, currentYOffset, -1);
            currentYOffset -= 12;
        }
        if (hasPants) {
            mc.fontRendererObj.drawStringWithShadow(pantsEnchants, -pantsWidth, currentYOffset, -1);
        }

        // RENDER ITEMS
        if (showItems) {
            List<ItemStack> items = new ArrayList<>();
            if (player.getHeldItem() != null) items.add(player.getHeldItem());
            for (int i = 3; i >= 0; i--) { 
                if (player.inventory.armorInventory[i] != null) items.add(player.inventory.armorInventory[i]);
            }

            if (!items.isEmpty()) {
                int startX = -(items.size() * 16) / 2;
                int itemY = hasPants ? -44 : (hasSword ? -32 : -18); 

                GlStateManager.enableRescaleNormal();
                RenderHelper.enableGUIStandardItemLighting();

                // FIX: Enable depth and depthMask so the glint pass can use GL_EQUAL depth masking properly
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);

                for (ItemStack item : items) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(startX, itemY, 0);
                    GlStateManager.scale(1.0F, 1.0F, 0.01F);
                    
                    // FIX: Use GL_ALWAYS so items still render through walls but populate the depth buffer correctly
                    GlStateManager.depthFunc(GL11.GL_ALWAYS);
                    
                    float prevZ = mc.getRenderItem().zLevel;
                    mc.getRenderItem().zLevel = -150.0F; 
                    mc.getRenderItem().renderItemIntoGUI(item, 0, 0);
                    mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, item, 0, 0);
                    mc.getRenderItem().zLevel = prevZ;
                    
                    GlStateManager.popMatrix();
                    startX += 16;
                }
                
                // Restore standard depth function
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
                
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                
                // Restore state back to how the rest of the nametag code expects it
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
            }
        }

        // clean clean mr clean.
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
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