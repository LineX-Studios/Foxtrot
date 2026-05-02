package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Handler.FoxtrotUsersManager;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LayerCape.class, priority = 2000)
public abstract class MixinLayerCape {

    @Redirect(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getLocationCape()Lnet/minecraft/util/ResourceLocation;"))
    private ResourceLocation redirectCapeTexture(AbstractClientPlayer entitylivingbaseIn) {
        if (com.linexstudios.foxtrot.Handler.ConfigHandler.showFoxtrotCapes) {
            String cape = FoxtrotUsersManager.getUserCape(entitylivingbaseIn.getUniqueID());
            if (cape != null) {
                return new ResourceLocation("foxtrot", "capes/" + cape + ".png");
            }
        }
        return entitylivingbaseIn.getLocationCape();
    }
}
