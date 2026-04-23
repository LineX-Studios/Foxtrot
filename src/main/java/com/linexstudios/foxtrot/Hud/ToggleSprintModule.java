package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ToggleSprintModule extends DraggableHUD {
    
    public static final ToggleSprintModule instance = new ToggleSprintModule();
    private final Minecraft mc = Minecraft.getMinecraft();

    // --- GUI Settings ---
    public boolean enabled = true;
    public boolean toggleSprint = true;
    public boolean toggleSneak = false;
    public boolean showHudText = true;
    public boolean showWhileTyping = true;
    public boolean wTapFix = true; 
    public boolean flyBoost = true;
    public float flyBoostAmount = 4.0F; 
    public int textColor = 0xFFFFFF; 

    // --- Internal State Variables ---
    public boolean sprintToggled = false;
    public boolean sneakToggled = false;
    private String currentText = "";

    // --- Dont change these Strings ---
    private final String flyBoostString = "[Flying (%sx boost)]";
    private final String flyString = "[Flying]";
    private final String ridingString = "[Riding]";
    private final String decendString = "[Descending]";
    private final String sneakHeldString = "[Sneaking (Key Held)]";
    private final String sprintHeldString = "[Sprinting (Key Held)]";
    private final String sprintToggledString = "[Sprinting (Toggled)]";
    private final String sneakToggledString = "[Sneaking (Toggled)]";

    public ToggleSprintModule() {
        super("Toggle Sprint", 2, 12);
    }

    // Standard 1.8.9 Permission Check
    private boolean isPlayerOP() {
        return mc.thePlayer != null && mc.thePlayer.canCommandSenderUseCommand(2, "");
    }

    // Checks the PHYSICAL keyboard/mouse, completely ignoring ToggleSprint's spoofed key states
    private boolean isPhysicalKeyDown(KeyBinding keyBinding) {
        int keyCode = keyBinding.getKeyCode();
        if (keyCode < 0) {
            return Mouse.isButtonDown(keyCode + 100);
        } else {
            return Keyboard.isKeyDown(keyCode);
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.currentScreen != null || !enabled) return;

        if (toggleSprint && mc.gameSettings.keyBindSprint.isPressed()) {
            sprintToggled = !sprintToggled;
        }
        
        if (toggleSneak && mc.gameSettings.keyBindSneak.isPressed()) {
            if (mc.isSingleplayer() || isPlayerOP()) {
                sneakToggled = !sneakToggled;
            } else {
                sneakToggled = false;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.thePlayer != event.player || !enabled) return;

        boolean isOppedOrSP = mc.isSingleplayer() || isPlayerOP();
        boolean canFlyBoost = isOppedOrSP || mc.thePlayer.capabilities.isCreativeMode;
        
        // Use the PHYSICAL key checks so Fly Boost doesn't get stuck on CTRL without holding it
        boolean isPhysicalSprintHeld = isPhysicalKeyDown(mc.gameSettings.keyBindSprint);
        boolean isPhysicalSneakHeld = isPhysicalKeyDown(mc.gameSettings.keyBindSneak);

        // --- 1. FLY LOGIC ---
        if (mc.thePlayer.capabilities.isFlying) {
            // ONLY boosts if the physical CTRL key is held down
            if (canFlyBoost && flyBoost && isPhysicalSprintHeld) {
                mc.thePlayer.capabilities.setFlySpeed(0.05F * flyBoostAmount);
            } else {
                mc.thePlayer.capabilities.setFlySpeed(0.05F); 
            }
        } else {
            mc.thePlayer.capabilities.setFlySpeed(0.05F); 
        }

        // --- 2. SPRINT/SNEAK ENFORCEMENT ---
        if (!isOppedOrSP) sneakToggled = false; 
        
        if (toggleSneak && sneakToggled) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        }

        if (toggleSprint && sprintToggled) {
            if (wTapFix) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            } else {
                if (mc.thePlayer.moveForward > 0 && !mc.thePlayer.isCollidedHorizontally) {
                    mc.thePlayer.setSprinting(true);
                }
            }
        }

        // --- 3. HUD STRING LOGIC ---
        if (mc.thePlayer.capabilities.isFlying) {
            if (isPhysicalSneakHeld) {
                currentText = decendString;
            } else if (canFlyBoost && flyBoost && isPhysicalSprintHeld) {
                currentText = String.format(flyBoostString, (int)flyBoostAmount);
            } else {
                currentText = flyString;
            }
        } else if (mc.thePlayer.isRiding()) {
            currentText = ridingString;
        } else if (isPhysicalSneakHeld || sneakToggled) {
            currentText = sneakToggled ? sneakToggledString : sneakHeldString;
        } else if (sprintToggled) {
            currentText = sprintToggledString;
        } else if (isPhysicalSprintHeld && !sprintToggled) {
            currentText = sprintHeldString;
        } else {
            currentText = "";
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (!enabled || !showHudText || mc.theWorld == null) return;
        if (mc.currentScreen instanceof EditHUDGui || mc.currentScreen instanceof HUDSettingsGui) return;
        if (!showWhileTyping && mc.currentScreen instanceof GuiChat) return;

        render(false, 0, 0); 
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled) return;

        String textToDraw = currentText;
        if (isEditing && textToDraw.isEmpty()) textToDraw = sprintToggledString;

        if (!textToDraw.isEmpty()) {
            mc.fontRendererObj.drawStringWithShadow(textToDraw, 0, 0, textColor);
            this.width = mc.fontRendererObj.getStringWidth(textToDraw);
            this.height = mc.fontRendererObj.FONT_HEIGHT;
        } else {
            this.width = 0; this.height = 0;
        }
    }
}