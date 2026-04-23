package com.linexstudios.foxtrot.Misc;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class AutoBulletTime {
    public static final AutoBulletTime instance = new AutoBulletTime();
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    public static boolean enabled = false;

    private static int oldSlot = -1;
    private static boolean didSwap = false;
    private static boolean waitingToSwapBack = false;
    private static long swapTime = 0L;
    private static int randomizedDelay = 0;

    private static final Random random = new Random();

    /**
     * Checks if the given item contains the Bullet Time (blocking_cancels_projectiles) enchant.
     */
    public static boolean hasBulletTime(ItemStack item) {
        if (item == null || !item.hasTagCompound()) return false;
        NBTTagCompound extra = item.getTagCompound().getCompoundTag("ExtraAttributes");
        if (extra == null || !extra.hasKey("CustomEnchants")) return false;

        NBTTagList enchants = extra.getTagList("CustomEnchants", 10);
        for (int i = 0; i < enchants.tagCount(); i++) {
            String key = enchants.getCompoundTagAt(i).getString("Key");
            if ("blocking_cancels_projectiles".equals(key)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!enabled || mc.currentScreen != null || mc.thePlayer == null) return;

        EntityPlayer player = mc.thePlayer;
        
        // Right Click Pressed
        if (event.button == 1 && event.buttonstate) {
            ItemStack heldItem = player.getHeldItem();
            
            // Only trigger if holding a sword that DOES NOT have Bullet Time
            if (heldItem != null && heldItem.getItem() instanceof ItemSword && !hasBulletTime(heldItem)) {
                
                // Scan hotbar for a Bullet Time sword
                for (int i = 0; i <= 8; i++) {
                    ItemStack item = player.inventory.getStackInSlot(i);
                    if (item != null && hasBulletTime(item)) {
                        oldSlot = player.inventory.currentItem;
                        player.inventory.currentItem = i;
                        mc.playerController.updateController();
                        
                        // Instantly send the blocking packet to the server
                        heldItem = player.getHeldItem();
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, heldItem);
                        
                        didSwap = true;
                        waitingToSwapBack = false;
                        swapTime = System.currentTimeMillis();
                        randomizedDelay = 100 + random.nextInt(151); // 100ms - 250ms random human delay
                        break;
                    }
                }
            }
        }
        
        // Right Click Released
        if (event.button == 1 && !event.buttonstate && didSwap && oldSlot != -1) {
            // Queue the swap back. (It will execute in the TickEvent once the delay is satisfied)
            waitingToSwapBack = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null) return;

        // Ensure we wait for the humanized delay before snapping back to the old sword
        if (didSwap && waitingToSwapBack && oldSlot != -1) {
            long elapsed = System.currentTimeMillis() - swapTime;
            if (elapsed >= randomizedDelay) {
                mc.thePlayer.inventory.currentItem = oldSlot;
                mc.playerController.updateController();
                
                didSwap = false;
                waitingToSwapBack = false;
                oldSlot = -1;
                randomizedDelay = 0;
            }
        }
    }
}