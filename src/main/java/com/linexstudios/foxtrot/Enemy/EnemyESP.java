package com.linexstudios.foxtrot.Enemy;

import com.linexstudios.foxtrot.Hud.EnemyHUD;
import com.linexstudios.foxtrot.Render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class EnemyESP {
    private final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled = true;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!enabled || mc.theWorld == null || mc.thePlayer == null) return;

        boolean hasEnemies = false;
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player != mc.thePlayer && !player.isDead && player.getHealth() > 0 && !player.isInvisible() && EnemyHUD.isTarget(player.getName())) {
                hasEnemies = true;
                break;
            }
        }

        if (hasEnemies) {
            RenderUtils.setup3D();
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player == mc.thePlayer || player.isDead || player.getHealth() <= 0 || player.isInvisible()) continue;
                if (EnemyHUD.isTarget(player.getName())) {
                    renderESP(player, event.partialTicks);
                }
            }
            RenderUtils.end3D();
        }
    }

    private void renderESP(EntityPlayer player, float partialTicks) {
        AxisAlignedBB bb = getInterpolatedBB(player, partialTicks);
        RenderUtils.drawFilledBox(bb, 1.0F, 0.3F, 0.3F, 0.4F);
        RenderUtils.drawOutlinedBox(bb, 1.0F, 0.3F, 0.3F, 1.0F, 2.0F);
        renderSkeleton(player, partialTicks);
    }

    private void renderSkeleton(EntityPlayer player, float partialTicks) {
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.5F); 
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x, y + 0.4, z); GL11.glVertex3d(x, y + 1.6, z);
        GL11.glVertex3d(x - 0.35, y + 1.55, z); GL11.glVertex3d(x + 0.35, y + 1.55, z);
        GL11.glVertex3d(x - 0.35, y + 1.55, z); GL11.glVertex3d(x - 0.35, y + 0.9, z);
        GL11.glVertex3d(x + 0.35, y + 1.55, z); GL11.glVertex3d(x + 0.35, y + 0.9, z);
        GL11.glVertex3d(x - 0.2, y + 0.7, z); GL11.glVertex3d(x + 0.2, y + 0.7, z);
        GL11.glVertex3d(x - 0.2, y + 0.7, z); GL11.glVertex3d(x - 0.2, y + 0.1, z);
        GL11.glVertex3d(x + 0.2, y + 0.7, z); GL11.glVertex3d(x + 0.2, y + 0.1, z);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    private AxisAlignedBB getInterpolatedBB(EntityPlayer player, float partialTicks) {
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        float expand = 0.13f;
        float expandy = 0.11f;

        return new AxisAlignedBB(x - player.width / 2.0 - expand, y - expandy, z - player.width / 2.0 - expand, x + player.width / 2.0 + expand, y + player.height + expandy, z + player.width / 2.0 + expand);
    }
}