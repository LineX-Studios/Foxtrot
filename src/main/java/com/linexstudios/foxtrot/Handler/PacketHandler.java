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
        ChannelPipeline pipeline = event.manager.channel().pipeline();
        
        // Safety: If the pipeline is already closed or handler exists, skip
        if (pipeline == null || pipeline.get(HANDLER_NAME) != null) return;

        try {
            pipeline.addBefore("packet_handler", HANDLER_NAME, new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    try {
                        if (msg instanceof Packet) {
                            onPacketReceive((Packet<?>) msg);
                        }
                    } catch (Exception e) {
                        // CRITICAL: Never let an exception escape the Netty thread or it will crash the game
                        System.err.println("[Foxtrot] Error in PacketHandler channelRead: " + e.getMessage());
                        e.printStackTrace();
                    }
                    super.channelRead(ctx, msg);
                }
            });
        } catch (Exception e) {
            System.err.println("[Foxtrot] Failed to inject Netty packet handler: " + e.getMessage());
        }
    }

    private void onPacketReceive(Packet<?> packet) {
        // We only listen for simple block changes to avoid heavy NBT processing on the network thread
        if (packet instanceof S23PacketBlockChange) {
            PitESP.instance.onBlockChange((S23PacketBlockChange) packet);
        } else if (packet instanceof S22PacketMultiBlockChange) {
            PitESP.instance.onMultiBlockChange((S22PacketMultiBlockChange) packet);
        }
    }
}
