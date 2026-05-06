package com.linexstudios.foxtrot.Misc;

import com.linexstudios.foxtrot.Handler.MapDetectionHandler;
import com.linexstudios.foxtrot.Render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;

public class NonHighlighter {

    public static final NonHighlighter instance = new NonHighlighter();
    private final Minecraft mc = Minecraft.getMinecraft();
    
    public static boolean enabled = false;

    private static final float HIGHLIGHT_HP_THRESHOLD = 8.0f;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        // FIXED: Replaced Ranks.instance.isInPit() with MapDetectionHandler.isInPit()
        if (!enabled || mc.thePlayer == null || mc.theWorld == null || !MapDetectionHandler.isInPit()) return;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer || player.isDead || player.getHealth() <= 0 || player.isInvisible()) continue;
            if (Math.abs(player.posX) > 25 || Math.abs(player.posZ) > 25) continue;
            float hp = player.getHealth();
            if (hp > HIGHLIGHT_HP_THRESHOLD) continue;
            if (!isNon(player)) continue;
            renderNonESP(player, hp, event.partialTicks);
        }
    }

    private boolean isNon(EntityPlayer player) {
        for (int i = 0; i < 4; i++) {
            ItemStack armorPiece = player.getCurrentArmor(i);
            if (armorPiece != null && armorPiece.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor) armorPiece.getItem();
                ItemArmor.ArmorMaterial mat = armor.getArmorMaterial();
                if (mat == ItemArmor.ArmorMaterial.DIAMOND || mat == ItemArmor.ArmorMaterial.LEATHER) {
                    return false;
                }
            }
        }
        return true;
    }

    private void renderNonESP(EntityPlayer player, float hp, float partialTicks) {
        RenderManager renderManager = mc.getRenderManager();
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - renderManager.viewerPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - renderManager.viewerPosY;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - renderManager.viewerPosZ;

        float r, g, b;
        EnumChatFormatting textColor;

        if (hp <= 4.0f) {
            r = 1.0f; g = 0.0f; b = 0.0f;
            textColor = EnumChatFormatting.RED;
        } else {
            r = 1.0f; g = 0.5f; b = 0.0f;
            textColor = EnumChatFormatting.GOLD;
        }

        AxisAlignedBB bb = player.getEntityBoundingBox().offset(-player.posX + x, -player.posY + y, -player.posZ + z);

        RenderUtils.setup3D();
        RenderUtils.drawFilledBox(bb, r, g, b, 0.25F);
        RenderUtils.drawOutlinedBox(bb, r, g, b, 1.0F, 2.0F);
        RenderUtils.end3D();

        GlStateManager.pushMatrix();
        GlStateManager.enableTexture2D();
        String hpText = textColor + new DecimalFormat("0.1").format(hp) + " HP";
        
        y += player.height + 0.6D;
        
        FontRenderer fontRenderer = renderManager.getFontRenderer();
        float scale = 0.02666667F; 
        
        GlStateManager.translate((float) x, (float) y, (float) z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        int textWidth = fontRenderer.getStringWidth(hpText);
        
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.5F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(-textWidth / 2.0 - 2, -2);
        GL11.glVertex2d(-textWidth / 2.0 - 2, 9);
        GL11.glVertex2d(textWidth / 2.0 + 2, 9);
        GL11.glVertex2d(textWidth / 2.0 + 2, -2);
        GL11.glEnd();
        
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        fontRenderer.drawStringWithShadow(hpText, -textWidth / 2, 0, 0xFFFFFF);

        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void toggle() {
        enabled = !enabled;
        if (Minecraft.getMinecraft().thePlayer != null) {
            String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] ";
            String status = enabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(prefix + EnumChatFormatting.YELLOW + "Non Highlighter: " + status));
        }
    }
}