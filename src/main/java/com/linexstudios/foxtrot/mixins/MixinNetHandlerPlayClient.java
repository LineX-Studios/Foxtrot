package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Hud.TelebowHUD;
import com.linexstudios.foxtrot.WhoGotBanned;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    // ==========================================
    //           TELEBOW CHAT INJECTION
    // ==========================================
    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    public void onHandleChat(S02PacketChat packetIn, CallbackInfo ci) {
        if (packetIn == null || packetIn.getChatComponent() == null) return;

        if (packetIn.getType() == 2) {
            String cleanMessage = EnumChatFormatting.getTextWithoutFormattingCodes(packetIn.getChatComponent().getUnformattedText()).toLowerCase();

            if (cleanMessage.contains("telebow") && cleanMessage.contains("cooldown")) {
                ci.cancel();

                if (TelebowHUD.enabled) {
                    Matcher m = Pattern.compile("(\\d+)s").matcher(cleanMessage);
                    if (!m.find()) m = Pattern.compile("(\\d+) seconds").matcher(cleanMessage);
                    
                    if (m.find()) {
                        long seconds = Long.parseLong(m.group(1));
                        TelebowHUD.instance.setCooldown(seconds);
                    }
                }
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

    // ==========================================
    //        WHO GOT BANNED TABLIST INJECTION
    // ==========================================
    @Inject(method = "handlePlayerListItem", at = @At("HEAD"))
    public void onPlayerListUpdate(S38PacketPlayerListItem packetIn, CallbackInfo ci) {
        // FIXED: Using 1.8.9 MCP mapping names
        if (packetIn.func_179768_b() == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
            
            for (S38PacketPlayerListItem.AddPlayerData data : packetIn.func_179767_a()) {
                if (data.getProfile() != null && data.getProfile().getName() != null) {
                    String name = data.getProfile().getName();
                    
                    if (!name.startsWith("§")) {
                        WhoGotBanned.instance.logPlayerRemoval(name);
                    }
                }
            }
        }
    }
}