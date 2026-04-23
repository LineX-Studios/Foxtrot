package com.linexstudios.foxtrot.Combat;

import com.linexstudios.foxtrot.Foxtrot;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Wtap {
    public static final Wtap instance = new Wtap();
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    public static boolean enabled = false;
    public static float delay = 5.5F;
    public static float duration = 1.5F;

    private boolean active = false;
    private boolean stopForward = false;
    private long delayTicks = 0L;
    private long durationTicks = 0L;
    private long lastTime = 0L;

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
            if (this.active) {
                if (!this.stopForward && !this.canTrigger()) {
                    this.active = false;
                    while (this.delayTicks > 0L) {
                        this.delayTicks -= 50L;
                    }
                    while (this.durationTicks > 0L) {
                        this.durationTicks -= 50L;
                    }
                } else if (this.delayTicks > 0L) {
                    this.delayTicks -= 50L;
                } else {
                    if (this.durationTicks > 0L) {
                        this.durationTicks -= 50L;
                        this.stopForward = true;
                        mc.thePlayer.movementInput.moveForward = 0.0F;
                    }
                    if (this.durationTicks <= 0L) {
                        this.active = false;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (enabled && !event.isCanceled() && event.entityPlayer == mc.thePlayer) {
            if (!this.active && (System.currentTimeMillis() - this.lastTime >= 500L) && mc.thePlayer.isSprinting()) {
                this.lastTime = System.currentTimeMillis();
                this.active = true;
                this.stopForward = false;
                this.delayTicks = this.delayTicks + (long) (50.0F * delay);
                this.durationTicks = this.durationTicks + (long) (50.0F * duration);
            }
        }
    }
}