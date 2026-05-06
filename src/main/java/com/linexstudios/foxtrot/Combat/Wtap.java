package com.linexstudios.foxtrot.Combat;

import com.linexstudios.foxtrot.Foxtrot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class Wtap {
    public static final Wtap instance = new Wtap();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final Random rand = new Random();
    
    public static boolean enabled = false;
    public static float chance = 100.0F; 
    public static float releaseDelay = 20.0F; 
    public static float repressDelay = 20.0F; 
    public static boolean selectHits = false;

    private boolean active = false;
    private boolean releasing = false;
    private long hitTime = 0L;
    private long releaseTime = 0L;

    private boolean canTrigger() {
        return !(mc.thePlayer.movementInput.moveForward < 0.8F)
                && !mc.thePlayer.isCollidedHorizontally
                && (!((float) mc.thePlayer.getFoodStats().getFoodLevel() <= 6.0F) || mc.thePlayer.capabilities.allowFlying) && (mc.thePlayer.isSprinting()
                || !mc.thePlayer.isUsingItem() && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.gameSettings.keyBindSprint.isKeyDown());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Foxtrot.toggleWtapKey != null && Foxtrot.toggleWtapKey.isPressed()) {
            enabled = !enabled;
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.GRAY + "W-Tap: " + (enabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF")));
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (enabled && event.phase == TickEvent.Phase.END && mc.thePlayer != null) {
            if (active) {
                long now = System.currentTimeMillis();
                if (!canTrigger()) {
                    active = false;
                    releasing = false;
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), org.lwjgl.input.Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                    return;
                }
                
                if (!releasing) {
                    if (now - hitTime >= (long)releaseDelay) {
                        releasing = true;
                        releaseTime = now;
                    }
                } else {
                    if (now - releaseTime >= (long)repressDelay) {
                        active = false;
                        releasing = false;
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), org.lwjgl.input.Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                    } else {
                        mc.thePlayer.movementInput.moveForward = 0.0F;
                        mc.thePlayer.setSprinting(false);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false); 
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (enabled && !event.isCanceled() && event.entityPlayer == mc.thePlayer) {
            if (active) return; 

            if (selectHits && event.target instanceof EntityLivingBase) {
                EntityLivingBase target = (EntityLivingBase) event.target;
                if (target.hurtResistantTime > 10) {
                    return; 
                }
            }
            
            if (rand.nextFloat() * 100.0F <= chance) {
                if (mc.thePlayer.isSprinting() || mc.gameSettings.keyBindForward.isKeyDown()) {
                    hitTime = System.currentTimeMillis();
                    active = true;
                    releasing = false;
                }
            }
        }
    }
}