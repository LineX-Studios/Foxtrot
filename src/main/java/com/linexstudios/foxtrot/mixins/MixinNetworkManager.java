package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Event.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(net.minecraft.network.Packet<?> packet, CallbackInfo ci) {
        // --- STEALTH BRAND SPOOFING ---
        if (packet instanceof net.minecraft.network.play.client.C17PacketCustomPayload) {
            net.minecraft.network.play.client.C17PacketCustomPayload p = (net.minecraft.network.play.client.C17PacketCustomPayload) packet;
            if ("MC|Brand".equals(p.getChannelName())) {
                // Only spoof if we are in a world (avoids main menu auth issues)
                if (net.minecraft.client.Minecraft.getMinecraft().theWorld != null) {
                    IAccessorC17PacketCustomPayload accessor = (IAccessorC17PacketCustomPayload) p;
                    accessor.setData(new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer()).writeString("vanilla"));
                }
            }
        }

        PacketEvent event = new PacketEvent(packet);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        PacketEvent event = new PacketEvent(packet);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
