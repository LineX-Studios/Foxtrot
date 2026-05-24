package com.linexstudios.foxtrot.mixins;

import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinOldServerPinger — Catch and prevent vanilla Minecraft 1.8.9 NullPointerException
 * crashes when pinging servers with invalid/empty/hijacked IP configurations during multiplayer GUI updates.
 */
@Mixin(OldServerPinger.class)
public class MixinOldServerPinger {

    @Inject(method = "tryCompatibilityPing", at = @At("HEAD"), cancellable = true)
    private void onTryCompatibilityPing(ServerData serverDataIn, CallbackInfo ci) {
        try {
            if (serverDataIn == null || serverDataIn.serverIP == null || serverDataIn.serverIP.trim().isEmpty()) {
                ci.cancel();
                return;
            }
            // Safely validate ServerAddress before Netty tries to resolve or connect to it
            ServerAddress serveraddress = ServerAddress.fromString(serverDataIn.serverIP);
            if (serveraddress == null || serveraddress.getIP() == null || serveraddress.getIP().trim().isEmpty()) {
                ci.cancel();
            }
        } catch (Exception e) {
            // Cancel legacy compatibility ping if any DNS resolution/address parsing exception occurs, avoiding client crash
            ci.cancel();
        }
    }
}
