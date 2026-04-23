package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Render.LowLifeMystic;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class MixinRenderItem {

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("TAIL"))
    private void onRenderItemOverlay(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        // If the module is on, tell it to check this specific item!
        if (LowLifeMystic.enabled && stack != null) {
            LowLifeMystic.instance.checkAndDraw(stack, xPosition, yPosition);
        }
    }
}