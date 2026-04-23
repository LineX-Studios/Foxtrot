package com.linexstudios.foxtrot.Misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class AutoGhead {
    public static final AutoGhead instance = new AutoGhead();
    public static Minecraft mc = Minecraft.getMinecraft();
    private final Random rand = new Random();
    
    // GUI Toggles
    public static boolean enabled = true;
    
    // --- ORGANIC RANDOMIZATION ---
    // Instead of a static number, we generate a new random threshold every time we heal
    private double currentHealthThreshold = generateNewThreshold();

    public static int oldSlot = -1;
    public static int gHeadSlot = -1;
    
    // Timers and Delays
    private static int tickDelay = 0;
    private static int targetDelay = 0;
    private static int timeoutTimer = 0; 
    
    public static int eggCooldown = 0; // 30s timer for First-Aid Egg
    public static int healCooldown = 0; // Small buffer between heals to prevent spam
    private static boolean usingEgg = false;
    
    private enum State {IDLE, SWAP, EAT, SWAPBACK}
    private static State state = State.IDLE;

    /**
     * Generates a random health threshold between 3 and 6 hearts (6.0 to 12.0 HP)
     */
    private double generateNewThreshold() {
        return 6.0 + rand.nextInt(7); 
    }

    /**
     * Generates a random tick delay between min and max
     */
    private int getRandomDelay(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    public static boolean isGhead(ItemStack item) {
        if (item == null || !item.hasTagCompound()) return false;
        NBTTagCompound tag = item.getTagCompound();
        if (tag.hasKey("display")) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("Name")) {
                String cleanName = EnumChatFormatting.getTextWithoutFormattingCodes(display.getString("Name"));
                return cleanName.contains("Golden Head");
            }
        }
        return false;
    }

    public static boolean isEgg(ItemStack item) {
        if (item == null || !item.hasTagCompound()) return false;
        NBTTagCompound tag = item.getTagCompound();
        if (tag.hasKey("display")) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("Name")) {
                String cleanName = EnumChatFormatting.getTextWithoutFormattingCodes(display.getString("Name"));
                return cleanName.contains("First-Aid Egg");
            }
        }
        return false;
    }

    private void forceReset() {
        state = State.IDLE;
        tickDelay = 0;
        timeoutTimer = 0;
        targetDelay = 0;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
    }

    /**
     * Listens for Pit Kill messages to lower the First-Aid Egg cooldown to 5 seconds
     */
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (mc.thePlayer == null || !enabled) return;
        
        String msg = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        
        // Only drops cooldown on an actual KILL
        if (msg.contains("KILL!")) {
            // Drop cooldown to 5 seconds (100 ticks) if it was higher
            if (eggCooldown > 100) {
                eggCooldown = 100;
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return;

        // Process cooldowns
        if (eggCooldown > 0) eggCooldown--;
        if (healCooldown > 0) healCooldown--;
        
        if (state == State.IDLE) {
            // Check against our dynamic humanized threshold!
            if (enabled && healCooldown <= 0 && mc.thePlayer.getHealth() <= currentHealthThreshold && mc.currentScreen == null) {
                
                gHeadSlot = -1;
                usingEgg = false;
                
                int foundEggSlot = -1;
                int foundHeadSlot = -1;
                
                // Scan hotbar
                for (int i = 0; i <= 8; i++) {
                    ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
                    if (isGhead(item) && foundHeadSlot == -1) {
                        foundHeadSlot = i;
                    } else if (isEgg(item) && foundEggSlot == -1) {
                        foundEggSlot = i;
                    }
                }
                
                // Prioritize Golden Head over Egg if both exist
                if (foundHeadSlot != -1) {
                    gHeadSlot = foundHeadSlot;
                    usingEgg = false;
                } else if (foundEggSlot != -1 && eggCooldown <= 0) {
                    gHeadSlot = foundEggSlot;
                    usingEgg = true;
                }
                
                // If a valid healing item was found, start the sequence
                if (gHeadSlot != -1) {
                    state = State.SWAP;
                    timeoutTimer = 0;
                    tickDelay = 0;
                    targetDelay = getRandomDelay(2, 4); // Wait 2-4 ticks before swapping
                }
            }
        }

        if (state != State.IDLE) {
            timeoutTimer++;
            // Extended failsafe to 35 ticks to account for the new random humanized delays
            if (timeoutTimer > 35) {
                forceReset();
                return;
            }

            switch (state) {
                case SWAP:
                    tickDelay++;
                    if (tickDelay >= targetDelay) {
                        if (mc.currentScreen != null) {
                            forceReset();
                            break;
                        }
                        oldSlot = mc.thePlayer.inventory.currentItem;
                        mc.thePlayer.inventory.currentItem = gHeadSlot;
                        
                        state = State.EAT;
                        tickDelay = 0;
                        targetDelay = getRandomDelay(2, 5); // Wait 2-5 ticks before clicking to look human
                    }
                    break;

                case EAT:
                    tickDelay++;
                    if (tickDelay == targetDelay) {
                        // Press right click down
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                    } else if (tickDelay >= targetDelay + 3) {
                        // Release right click 3 ticks later
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                        
                        if (usingEgg) {
                            eggCooldown = 600; // 30 seconds (30 * 20 ticks)
                        }
                        healCooldown = 20; // 1 second buffer before we can heal anything again
                        
                        // --- ORGANIC RANDOMIZATION UPDATE ---
                        // Roll the dice for the NEXT heal so we don't heal at the same HP twice in a row!
                        currentHealthThreshold = generateNewThreshold();
                        
                        state = State.SWAPBACK;
                        tickDelay = 0;
                        targetDelay = getRandomDelay(2, 5); // Wait 2-5 ticks before swapping back to weapon
                    }
                    break;

                case SWAPBACK:
                    tickDelay++;
                    if (tickDelay >= targetDelay) {
                        if (oldSlot != -1) {
                            mc.thePlayer.inventory.currentItem = oldSlot;
                        }
                        forceReset();
                    }
                    break;
            }
        }
    }
}