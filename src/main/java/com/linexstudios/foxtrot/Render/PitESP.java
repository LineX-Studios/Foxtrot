package com.linexstudios.foxtrot.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class PitESP {
    public static final PitESP instance = new PitESP();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean espChests = true, espDragonEggs = true, espRaffleTickets = true, espMystics = true;
    private final List<BlockPos> dragonEggs = new ArrayList<>();
    private int scanTimer = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) return;
        if (espDragonEggs && ++scanTimer >= 2) {
            scanTimer = 0; dragonEggs.clear();
            BlockPos p = mc.thePlayer.getPosition();
            for (int x = -70; x <= 70; x++)
                for (int y = -10; y <= 10; y++)
                    for (int z = -70; z <= 70; z++) {
                        BlockPos pos = p.add(x, y, z);
                        if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.dragon_egg) dragonEggs.add(pos);
                    }
        } else if (!espDragonEggs) dragonEggs.clear();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        double vX = mc.getRenderManager().viewerPosX, vY = mc.getRenderManager().viewerPosY, vZ = mc.getRenderManager().viewerPosZ;
        ICamera cam = new Frustum(); cam.setPosition(vX, vY, vZ);

        if (espChests) {
            for (TileEntity tile : mc.theWorld.loadedTileEntityList) {
                if (tile instanceof TileEntityChest) {
                    BlockPos p = tile.getPos(); int cx = p.getX(), cy = p.getY(), cz = p.getZ();
                    if (cx >= 0 && cx <= 117 && cy >= 47 && cy <= 63 && cz >= -23 && cz <= 95) {
                        AxisAlignedBB bb = new AxisAlignedBB(cx - vX + 0.0625, cy - vY, cz - vZ + 0.0625, cx + 0.9375 - vX, cy + 0.875 - vY, cz + 0.9375 - vZ);
                        if (cam.isBoundingBoxInFrustum(bb.offset(vX, vY, vZ))) {
                            RenderUtils.setup3D();
                            RenderUtils.drawFilledBox(bb, 1.0f, 0.0f, 0.0f, 0.24f);
                            RenderUtils.drawOutlinedBox(bb, 1.0f, 0.0f, 0.0f, 1.0f, 2.5f);
                            RenderUtils.end3D();
                        }
                    }
                }
            }
        }

        if (espRaffleTickets || espMystics) {
            for (Entity e : mc.theWorld.loadedEntityList) {
                if (e instanceof EntityItem && cam.isBoundingBoxInFrustum(e.getEntityBoundingBox())) {
                    ItemStack s = ((EntityItem)e).getEntityItem(); if (s == null || s.getItem() == null) continue;
                    if (espRaffleTickets && s.getItem() instanceof ItemNameTag) renderItem(e, event.partialTicks, 0xFFFFAA00, "Raffle Ticket");
                    else if (espMystics && s.hasTagCompound()) {
                        NBTTagCompound n = s.getTagCompound();
                        if (n.hasKey("ExtraAttributes") && (n.getCompoundTag("ExtraAttributes").hasKey("Nonce") || n.getCompoundTag("ExtraAttributes").hasKey("CustomEnchants")))
                            renderItem(e, event.partialTicks, 0xFFFFFF55, "Mystic Drop");
                    }
                }
            }
        }

        if (espDragonEggs) {
            for (BlockPos p : dragonEggs) {
                AxisAlignedBB bb = new AxisAlignedBB(p.getX() - vX + 0.0625, p.getY() - vY, p.getZ() - vZ + 0.0625, p.getX() + 0.9375 - vX, p.getY() + 1 - vY, p.getZ() + 0.9375 - vZ);
                if (cam.isBoundingBoxInFrustum(bb.offset(vX, vY, vZ))) {
                    RenderUtils.setup3D();
                    RenderUtils.drawFilledBox(bb, 0.66f, 0.0f, 1.0f, 0.24f); // 0.66 Red + 1.0 Blue = Purple
                    RenderUtils.drawOutlinedBox(bb, 0.66f, 0.0f, 1.0f, 1.0f, 2.5f);
                    RenderUtils.end3D();
                }
            }
        }
    }

    private void renderItem(Entity e, float pt, int c, String l) {
        double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * pt - mc.getRenderManager().viewerPosX;
        double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * pt - mc.getRenderManager().viewerPosY;
        double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * pt - mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix(); GlStateManager.disableDepth(); GlStateManager.depthMask(false);
        mc.getRenderManager().setRenderShadow(false);
        mc.getRenderManager().doRenderEntity(e, x, y, z, e.rotationYaw, pt, false);
        mc.getRenderManager().setRenderShadow(true); GlStateManager.depthMask(true); GlStateManager.enableDepth(); GlStateManager.popMatrix();
        renderText(l, x, y + 0.6, z, c);
    }

    private void renderText(String t, double x, double y, double z, int c) {
        FontRenderer fr = mc.fontRendererObj; float s = 0.02666667F;
        GlStateManager.pushMatrix(); GlStateManager.translate(x, y, z); GL11.glNormal3f(0, 1, 0);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0); GlStateManager.rotate(mc.getRenderManager().playerViewX, 1, 0, 0);
        GlStateManager.scale(-s, -s, s); GlStateManager.disableLighting(); GlStateManager.depthMask(false); GlStateManager.disableDepth();
        GlStateManager.enableBlend(); GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        fr.drawStringWithShadow(t, -fr.getStringWidth(t) / 2, 0, c);
        GlStateManager.enableDepth(); GlStateManager.depthMask(true); GlStateManager.enableLighting(); GlStateManager.disableBlend(); GlStateManager.popMatrix();
    }
}