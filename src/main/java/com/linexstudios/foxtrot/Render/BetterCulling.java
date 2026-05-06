package com.linexstudios.foxtrot.Render;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.EnumChatFormatting;

public class BetterCulling {
    public static final BetterCulling instance = new BetterCulling();
    public static boolean isEventActive = false;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String msg = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        
        // Freeze HUD updates when an event starts
        if (msg.contains("MAJOR EVENT!") || msg.contains("MINOR EVENT!")) {
            isEventActive = true;
        } 
        // Resume HUD updates when it ends
        else if (msg.contains("EVENT OVER!") || msg.contains("Reward:")) {
            isEventActive = false;
        }
    }
}