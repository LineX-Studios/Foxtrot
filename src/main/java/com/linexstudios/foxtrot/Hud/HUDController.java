package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class HUDController {
    public static boolean enabled = true;
    public static boolean dragMode = false;

    public static final KeyBinding dragHudKey = new KeyBinding("key.foxtrot.draghud", Keyboard.KEY_U, "key.categories.foxtrot");

    static {
        ClientRegistry.registerKeyBinding(dragHudKey);
    }

    public static void setEnabled(boolean state) {
        enabled = state;
    }

    // Opens the Drag GUI when the hotkey is pressed
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (dragHudKey.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            // Prevents opening multiple GUIs on top of each other
            if (!(mc.currentScreen instanceof EditHUDGui)) {
                mc.displayGuiScreen(new EditHUDGui());
            }
        }
    }
}