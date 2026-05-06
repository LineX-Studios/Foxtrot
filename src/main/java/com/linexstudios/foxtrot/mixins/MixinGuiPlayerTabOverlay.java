package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Handler.FoxtrotUsersManager;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    private static final net.minecraft.util.ResourceLocation FX_LOGO = new net.minecraft.util.ResourceLocation(
            "foxtrot", "icons/fx_logo.png");

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    public void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable<String> cir) {
        if (networkPlayerInfoIn != null && networkPlayerInfoIn.getGameProfile() != null) {
            if (FoxtrotUsersManager.isFoxtrotUser(networkPlayerInfoIn.getGameProfile().getId())) {
                String originalName = cir.getReturnValue();
                // \u200B is a zero-width space. We append 3 physical spaces after it to force
                // the Vanilla tablist layout engine to create the exact width we need for our
                // 9x9 icon!
                cir.setReturnValue("\u200B   " + originalName);
            }
        }
    }

    @org.spongepowered.asm.mixin.injection.Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int drawFoxtrotLogoTablist(net.minecraft.client.gui.FontRenderer fontRenderer, String text, float x,
            float y, int color) {
        if (text != null && text.startsWith("\u200B")) {
            // Found a Foxtrot user! The string has our hidden marker.
            String renderText = text.substring(1); // Remove the zero-width marker

            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            net.minecraft.client.renderer.GlStateManager.enableTexture2D();
            net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(FX_LOGO);

            // Draw the logo in the physical space we reserved!
            net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, 0, 0, 9, 9, 9, 9);

            net.minecraft.client.renderer.GlStateManager.popMatrix();

            // Draw the actual name, letting it naturally print the 3 spaces which moves the
            // text past our image!
            return fontRenderer.drawStringWithShadow(renderText, x, y, color);
        }

        return fontRenderer.drawStringWithShadow(text, x, y, color);
    }
}
