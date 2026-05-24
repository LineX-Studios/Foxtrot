package com.linexstudios.foxtrot.Handler;

import com.linexstudios.foxtrot.Denick.NickedManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Handles the formatting of player names to show resolved nicks directly in-game.
 * Inspired by Roni's advanced denick display logic.
 */
public class NameFormatHandler {
    public static final NameFormatHandler instance = new NameFormatHandler();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNameFormat(PlayerEvent.NameFormat event) {
        String nick = event.username;
        String resolvedName = NickedManager.getResolvedIGN(nick);

        // If the player is denicked, append the real name to their display name
        if (resolvedName != null && !resolvedName.equals("Failed") && !resolvedName.contains("No Nonce") && !resolvedName.equals("Scraping")) {
            // Format: [N] Nick -> RealName
            event.displayname = event.displayname + " " + EnumChatFormatting.GRAY + "\u27A1 " + EnumChatFormatting.YELLOW + resolvedName;
        }
    }
}
