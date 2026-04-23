package com.linexstudios.foxtrot.Util;

import com.linexstudios.foxtrot.Foxtrot; 
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class SpawnShortcut {
    
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Foxtrot.spawnKey.isPressed() && mc.thePlayer != null) {
            mc.thePlayer.sendChatMessage("/spawn");
        }
    }
}