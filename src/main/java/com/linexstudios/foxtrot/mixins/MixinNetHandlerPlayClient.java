package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Hud.TelebowHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    // ==========================================
    //    SCOREBOARD NULL-SAFETY GUARD (NPE FIX)
    //  Vanilla 1.8.9 race condition: Hypixel fires
    //  S3B/S3E packets during server transfer while
    //  the local scoreboard has already been reset.
    // ==========================================
    @Inject(method = "handleScoreboardObjective", at = @At("HEAD"), cancellable = true)
    public void onHandleScoreboardObjective(S3BPacketScoreboardObjective packetIn, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.theWorld.getScoreboard() == null) {
            ci.cancel(); // Drop packet — scoreboard not ready yet
        }
    }

    @Inject(method = "handleTeams", at = @At("HEAD"), cancellable = true)
    public void onHandleTeams(S3EPacketTeams packetIn, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.theWorld.getScoreboard() == null) {
            ci.cancel(); // Drop packet — scoreboard not ready yet
        }
    }

    // ==========================================
    //           TELEBOW CHAT INJECTION
    // ==========================================
    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    public void onHandleChat(S02PacketChat packetIn, CallbackInfo ci) {
        if (packetIn == null || packetIn.getChatComponent() == null) return;

        if (packetIn.getType() == 2) {
            String cleanMessage = EnumChatFormatting.getTextWithoutFormattingCodes(packetIn.getChatComponent().getUnformattedText()).toLowerCase();
            
            if (cleanMessage.contains("telebow cooldown:") || cleanMessage.contains("you cannot use this right now!")) {
                ci.cancel(); 
            }
        } 
        else {
            if (!TelebowHUD.enabled) return;
            String cleanMessage = EnumChatFormatting.getTextWithoutFormattingCodes(packetIn.getChatComponent().getUnformattedText());
            
            if (cleanMessage.contains("NOPE! Can't teleport there") || 
                cleanMessage.contains("You died!") || 
                cleanMessage.contains("RESPAWNED!") ||
                cleanMessage.contains("DEATH!")) {
                
                TelebowHUD.instance.clearCooldown();
            }
        }
    }
}