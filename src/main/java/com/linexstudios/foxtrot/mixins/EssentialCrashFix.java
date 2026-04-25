package com.linexstudios.foxtrot.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EssentialCrashFix — Patches a bug in Essential.gg where it tries to send 
 * chat messages while the player is null (e.g., on the Main Menu during a screenshot).
 */
@Pseudo
@Mixin(targets = "gg.essential.universal.wrappers.UPlayer", remap = false)
public class EssentialCrashFix {

    @Inject(method = "sendClientSideMessage(Lgg/essential/universal/wrappers/message/UTextComponent;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onSendClientSideMessage(Object message, CallbackInfo ci) {
        // If the player is null (we are on a menu), cancel the message to prevent NPE
        if (Minecraft.getMinecraft().thePlayer == null) {
            ci.cancel();
        }
    }
}
