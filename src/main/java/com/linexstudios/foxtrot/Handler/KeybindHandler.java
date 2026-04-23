package com.linexstudios.foxtrot.Handler;

import com.linexstudios.foxtrot.Hud.EditHUDGui;
import com.linexstudios.foxtrot.Hud.NameTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeybindHandler {
    public static KeyBinding toggleNameTags;
    public static KeyBinding openHudEditor;

    public static void init() {
        toggleNameTags = new KeyBinding("Toggle NameTags", Keyboard.KEY_X, "Foxtrot");
        openHudEditor = new KeyBinding("Open HUD Editor", Keyboard.KEY_RSHIFT, "Foxtrot");
        
        ClientRegistry.registerKeyBinding(toggleNameTags);
        ClientRegistry.registerKeyBinding(openHudEditor);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        // Null check to prevent crashes during world transitions
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (openHudEditor.isPressed()) {
            mc.displayGuiScreen(new EditHUDGui());
            return;
        }

        if (toggleNameTags.isPressed()) {
            NameTags.enabled = !NameTags.enabled;
            ConfigHandler.saveConfig();

            String status = NameTags.enabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "FOXTROT" + EnumChatFormatting.GRAY + "] ";
            
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    prefix + EnumChatFormatting.YELLOW + "NameTags: " + status
            ));
        }
    }
}