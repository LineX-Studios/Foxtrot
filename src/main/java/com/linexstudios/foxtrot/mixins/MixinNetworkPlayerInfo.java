package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Handler.FoxtrotUsersManager;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.authlib.GameProfile;

@Mixin(value = NetworkPlayerInfo.class, priority = 2000)
public abstract class MixinNetworkPlayerInfo {

    @Shadow
    public abstract GameProfile getGameProfile();

    @Inject(method = "getLocationCape", at = @At("RETURN"), cancellable = true)
    public void getCape(CallbackInfoReturnable<ResourceLocation> cir) {
        if (com.linexstudios.foxtrot.Handler.ConfigHandler.showFoxtrotCapes) {
            if (this.getGameProfile() != null && FoxtrotUsersManager.isFoxtrotUser(this.getGameProfile().getId())) {
                cir.setReturnValue(new ResourceLocation("foxtrot", "capes/" + com.linexstudios.foxtrot.Handler.ConfigHandler.selectedCape + ".png"));
            }
        }
    }
}
