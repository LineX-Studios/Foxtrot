package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ArmorHUD extends DraggableHUD {
    public static ArmorHUD instance = new ArmorHUD();
    public static boolean enabled = true;
    public static int durabilityColor = 0xFFFFFF; 

    private Minecraft mc = Minecraft.getMinecraft();

    public ArmorHUD() {
        super("Armor Status", 10, 100);
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
        if (!enabled || mc.thePlayer == null) return;

        List<ItemStack> items = new ArrayList<>();
        if (mc.thePlayer.getHeldItem() != null) items.add(mc.thePlayer.getHeldItem());
        for (int i = 3; i >= 0; i--) {
            if (mc.thePlayer.inventory.armorInventory[i] != null) items.add(mc.thePlayer.inventory.armorInventory[i]);
        }

        if (isEditing && items.isEmpty()) {
            items.add(new ItemStack(net.minecraft.init.Items.diamond_sword));
            items.add(new ItemStack(net.minecraft.init.Items.diamond_helmet));
            items.add(new ItemStack(net.minecraft.init.Items.diamond_chestplate));
            items.add(new ItemStack(net.minecraft.init.Items.diamond_leggings));
            items.add(new ItemStack(net.minecraft.init.Items.diamond_boots));
        }

        if (items.isEmpty()) { this.width = 0; this.height = 0; return; }

        FontRenderer fr = mc.fontRendererObj;
        int maxDurabilityWidth = 0;
        for (ItemStack item : items) {
            if (item.isItemStackDamageable()) {
                int damage = item.getMaxDamage() - item.getItemDamage();
                int w = fr.getStringWidth(String.valueOf(damage));
                if (w > maxDurabilityWidth) maxDurabilityWidth = w;
            }
        }

        int currentX = isHorizontal ? 2 : maxDurabilityWidth + 6; 
        int currentY = 2;
        int maxW = 0;
        int maxH = 0;

        RenderHelper.enableGUIStandardItemLighting();

        for (ItemStack item : items) {
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getRenderItem().renderItemAndEffectIntoGUI(item, currentX, currentY);
            
            if (item.isItemStackDamageable()) {
                int max = item.getMaxDamage();
                int dmg = max - item.getItemDamage();
                String dmgStr = String.valueOf(dmg);
                
                GlStateManager.disableDepth();
                GlStateManager.disableLighting();
                
                // Using the dynamic durabilityColor instead of standard hex overrides
                if (isHorizontal) {
                    fr.drawStringWithShadow(dmgStr, currentX + 8 - fr.getStringWidth(dmgStr) / 2, currentY + 18, durabilityColor);
                } else {
                    fr.drawStringWithShadow(dmgStr, currentX - fr.getStringWidth(dmgStr) - 4, currentY + 4, durabilityColor);
                }
                
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
            GlStateManager.popMatrix();

            if (isHorizontal) { currentX += 24; maxH = 28; } 
            else { currentY += 20; maxW = Math.max(maxW, currentX + 18); }
        }
        RenderHelper.disableStandardItemLighting();

        this.width = isHorizontal ? currentX : maxW;
        this.height = isHorizontal ? maxH : currentY + 2;
    }
}