package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Hud.TelebowHUD;
import com.linexstudios.foxtrot.Handler.MapDetectionHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    @Inject(method = "sendChatMessage", at = @At("HEAD"))
    public void onSendChatMessage(String message, CallbackInfo ci) {
        TelebowHUD.instance.onPlayerCommand(message);
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onPlayerUpdate(CallbackInfo ci) {
        // FIXED: Calls the Centralized Map Detector
        MapDetectionHandler.updateMap();
    }
}