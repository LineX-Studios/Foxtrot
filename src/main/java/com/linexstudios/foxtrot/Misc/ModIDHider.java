package com.linexstudios.foxtrot.Misc;

import com.linexstudios.foxtrot.Event.PacketEvent;
import com.linexstudios.foxtrot.mixins.IAccessorC17PacketCustomPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * ModIDHider — Intercepts the MC|Brand handshake packet and rewrites the
 * client brand string to "vanilla", making Foxtrot invisible to Hypixel's
 * mod-detection and anti-cheat systems.
 */
public class ModIDHider {
    public static final ModIDHider instance = new ModIDHider();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = true;
    public static String spoofedBrand = "vanilla";

    private ModIDHider() {}

    @SubscribeEvent
    public void onPacketSend(PacketEvent event) {
        if (!enabled) return;

        if (event.getPacket() instanceof C17PacketCustomPayload) {
            C17PacketCustomPayload packet = (C17PacketCustomPayload) event.getPacket();
            if ("MC|Brand".equals(packet.getChannelName())) {
                IAccessorC17PacketCustomPayload accessor = (IAccessorC17PacketCustomPayload) packet;
                accessor.setData(new PacketBuffer(Unpooled.buffer()).writeString(spoofedBrand));
            }
        }
    }

    public static void toggle() {
        enabled = !enabled;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            String state = enabled
                    ? EnumChatFormatting.GREEN + "ON"
                    : EnumChatFormatting.RED + "OFF";
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot"
                            + EnumChatFormatting.GRAY + "] "
                            + EnumChatFormatting.GRAY + "ModIDHider: " + state
            ));
        }
    }
}
