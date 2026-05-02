package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Handler.FoxtrotUsersManager;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RendererLivingEntity.class, priority = 1001)
public abstract class MixinRendererLivingEntity extends Render<EntityLivingBase> {

    protected MixinRendererLivingEntity() {
        super(null);
    }

    private static final ResourceLocation FX_LOGO = new ResourceLocation("foxtrot", "icons/fx_logo.png");

    @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At("HEAD"), cancellable = true)
    private void onRenderName(EntityLivingBase entityIn, double x, double y, double z, CallbackInfo ci) {
        if (entityIn instanceof AbstractClientPlayer && FoxtrotUsersManager.isFoxtrotUser(entityIn.getUniqueID())) {
            double d0 = entityIn.getDistanceSqToEntity(this.renderManager.livingPlayer);

            if (d0 <= (double)(64 * 64)) {
                String str = entityIn.getDisplayName().getFormattedText();
                net.minecraft.client.gui.FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
                float f = 1.6F;
                float f1 = 0.016666668F * f;
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)x + 0.0F, (float)y + entityIn.height + 0.5F, (float)z);
                org.lwjgl.opengl.GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-f1, -f1, f1);
                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                
                int i = fontrenderer.getStringWidth(str) / 2;
                int logoSpace = 11; 
                
                GlStateManager.disableTexture2D();
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos((double)(-i - 1 - logoSpace), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                worldrenderer.pos((double)(-i - 1 - logoSpace), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                worldrenderer.pos((double)(i + 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                worldrenderer.pos((double)(i + 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
                
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.bindTexture(FX_LOGO);
                net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture(-i - logoSpace, -1, 0, 0, 9, 9, 9, 9);
                
                fontrenderer.drawString(str, -i, 0, 553648127);
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                fontrenderer.drawString(str, -i, 0, -1);
                
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
                
                ci.cancel(); 
            }
        }
    }
}
