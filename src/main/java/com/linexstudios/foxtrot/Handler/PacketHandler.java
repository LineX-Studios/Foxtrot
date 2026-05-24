package com.linexstudios.foxtrot.Handler;

import com.linexstudios.foxtrot.Render.PitESP;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PacketHandler {
    public static final PacketHandler instance = new PacketHandler();
    private static final String HANDLER_NAME = "foxtrot_packet_handler";

    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (event.manager == null || event.manager.channel() == null) return;
        final ChannelPipeline pipeline = event.manager.channel().pipeline();
        
        if (pipeline == null || pipeline.get(HANDLER_NAME) != null) return;

        try {
            pipeline.addBefore("packet_handler", HANDLER_NAME, new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    try {
                        if (msg instanceof Packet) {
                            onPacketReceive((Packet<?>) msg);
                        }
                    } catch (Throwable t) {
                        // Use Throwable to catch EVERYTHING, including Error
                    }
                    super.channelRead(ctx, msg);
                }
            });
        } catch (Exception ignored) {}
    }

    private void onPacketReceive(Packet<?> packet) {
        if (packet == null || PitESP.instance == null) return;
        
        // Block changes are handled safely; no heavy logic allowed here
        if (packet instanceof S23PacketBlockChange) {
            PitESP.instance.onBlockChange((S23PacketBlockChange) packet);
        } else if (packet instanceof S22PacketMultiBlockChange) {
            PitESP.instance.onMultiBlockChange((S22PacketMultiBlockChange) packet);
        } else if (packet instanceof net.minecraft.network.play.server.S1DPacketEntityEffect) {
            handleEffectPacket((net.minecraft.network.play.server.S1DPacketEntityEffect) packet);
        }
    }

    private void handleEffectPacket(net.minecraft.network.play.server.S1DPacketEntityEffect packet) {
        net.minecraft.client.entity.EntityPlayerSP player = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
        if (player == null || player.getEntityId() != packet.getEntityId()) return;
        
        // Potion ID 19 is Poison (Venom in Pit)
        if (packet.getEffectId() == 19) {
            int duration = packet.getDuration();
            // Pit Venom duration is usually specific (e.g., 5s = 100 ticks, 12s = 240 ticks, 24s = 480 ticks)
            if (duration > 0) {
                com.linexstudios.foxtrot.Misc.AutoPantSwap.instance.onVenomPacketReceived();
            }
        }
    }
}
