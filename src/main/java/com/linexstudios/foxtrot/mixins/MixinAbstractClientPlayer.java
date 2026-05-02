package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Handler.FoxtrotUsersManager;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractClientPlayer.class, priority = 9999)
public abstract class MixinAbstractClientPlayer {

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    public void getCape(CallbackInfoReturnable<ResourceLocation> cir) {
        if (com.linexstudios.foxtrot.Handler.ConfigHandler.showFoxtrotCapes) {
            if (FoxtrotUsersManager.isFoxtrotUser(((AbstractClientPlayer)(Object)this).getUniqueID())) {
                cir.setReturnValue(new ResourceLocation("foxtrot", "capes/" + com.linexstudios.foxtrot.Handler.ConfigHandler.selectedCape + ".png"));
            }
        }
    }
}
