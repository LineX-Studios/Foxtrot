package com.linexstudios.foxtrot.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Hides all other players except the ones in your Focus list and Friends list.
 */
public class FocusManager {
    public static final FocusManager instance = new FocusManager();
    
    // Kept session-based (not saved to config) so you don't accidentally log in 
    // the next day and wonder why everyone in the lobby is invisible!
    public static List<String> focusList = new ArrayList<>();

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre event) {
        // If the focus list is empty, don't hide anyone
        if (focusList.isEmpty()) return;

        EntityPlayer player = event.entityPlayer;
        Minecraft mc = Minecraft.getMinecraft();

        // Don't hide yourself
        if (player == mc.thePlayer) return;

        // Foxtrot Exclusive: Never hide people on your friends list!
        if (com.linexstudios.foxtrot.Hud.FriendsHUD.isFriend(player.getName())) return;

        // Check if the rendered player is in the focus list
        boolean isFocused = false;
        for (String focusedName : focusList) {
            if (player.getName().equalsIgnoreCase(focusedName)) {
                isFocused = true;
                break;
            }
        }

        // If they aren't focused and aren't a friend, completely hide them from rendering
        if (!isFocused) {
            event.setCanceled(true);
        }
    }
}