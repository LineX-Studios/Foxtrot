package com.linexstudios.foxtrot.Misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoPantSwap {
    public static final AutoPantSwap instance = new AutoPantSwap();
    private static final Minecraft mc = Minecraft.getMinecraft();

    // GUI Toggles
    public static boolean pantSwapEnabled = true;
    public static boolean venomSwapEnabled = true;
    public static boolean autoPodEnabled = true;
    public static double defaultHealthValuePod = 8.0;

    private enum State { IDLE, OPEN_INV, SWAP, CLOSE_INV }
    private enum StateAutoPod { IDLE, OPEN_INV, SWAP1, SWAP2, SWAP3, SWAP4, CLOSE_INV }
    private enum StateSwapIfVenomed { IDLE, OPEN_INV, SWAP1, SWAP2, SWAP3, SWAP4, CLOSE_INV}
    
    public static boolean alreadyDidPod = false;
    public static boolean alreadyDidSwap = false;
    
    private static StateSwapIfVenomed stateSwapIfVenomed = StateSwapIfVenomed.IDLE;
    private static StateAutoPod stateAutoPod = StateAutoPod.IDLE;
    private static State state = State.IDLE;
    
    private static int tickDelay = 0;
    private static int tickDelay2 = 0;
    private static int tickDelay3 = 0;
    
    // Failsafe timers
    private static int podTimeout = 0;
    private static int venomTimeout = 0;
    private static int normalSwapTimeout = 0;
    
    private int oldInvSlot = -1;

    public static boolean hasPodInInv(){
        for (int i = 0; i <= 35; i++) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
            if (item != null && item.hasTagCompound() && item.getTagCompound().toString().contains("escape_pod")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hadDiamondPantsInInv(){
        for (int i = 0; i <= 35; i++) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
            if (item != null && item.getItem() == Items.diamond_leggings) {
                return true;
            }
        }
        return false;
    }

    private void resetVenomState() {
        stateSwapIfVenomed = StateSwapIfVenomed.IDLE;
        tickDelay3 = 0;
        venomTimeout = 0;
        if (mc.currentScreen instanceof GuiInventory) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(0));
            mc.displayGuiScreen(null);
        }
    }

    private void resetPodState() {
        stateAutoPod = StateAutoPod.IDLE;
        tickDelay2 = 0;
        podTimeout = 0;
        if (mc.currentScreen instanceof GuiInventory) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(0));
            mc.displayGuiScreen(null);
        }
    }

    private void resetNormalSwap() {
        state = State.IDLE;
        tickDelay = 0;
        normalSwapTimeout = 0;
        if (mc.currentScreen instanceof GuiInventory) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(0));
            mc.displayGuiScreen(null);
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!pantSwapEnabled || mc.currentScreen != null || event.button != 1 || !event.buttonstate) return;
        EntityPlayer player = mc.thePlayer;
        if (player == null || player.inventory.armorItemInSlot(1) == null) return;
        ItemStack heldLeggings = player.inventory.getStackInSlot(player.inventory.currentItem);
        if (heldLeggings == null || !(heldLeggings.getItem() instanceof ItemArmor)) return;
        ItemArmor armor = (ItemArmor) heldLeggings.getItem();
        if (armor.armorType != 2) return;
        
        start(1);
    }

    public static void tryToSwapIfVenomed(){
        if (!venomSwapEnabled || mc.currentScreen != null || mc.thePlayer == null) return;
        EntityPlayer player = mc.thePlayer;
        boolean isPoisoned = player.getActivePotionEffect(Potion.poison) != null;
        
        // UNLOCK LOGIC: If we swapped to diamond pants, but poison wore off, we can swap back
        if (!isPoisoned && alreadyDidSwap) {
            start(3);
        }
        
        // TRIGGER LOGIC: If we get poisoned, and haven't swapped yet
        if (isPoisoned && !alreadyDidSwap && player.inventory.armorItemInSlot(1) != null && player.inventory.armorItemInSlot(1).getItem() == Items.leather_leggings) {
            if (hadDiamondPantsInInv()) {
                alreadyDidSwap = true;
                start(3);
            }
        }
    }

    public static void start(int swap) {
        switch (swap){
            case 1:
                if (state != State.IDLE) return;
                tickDelay = 0;
                normalSwapTimeout = 0;
                state = State.OPEN_INV;
                break;
            case 2:
                if (stateAutoPod != StateAutoPod.IDLE) return;
                tickDelay2 = 0;
                podTimeout = 0;
                stateAutoPod = StateAutoPod.OPEN_INV;
                break;
            case 3:
                if (stateSwapIfVenomed != StateSwapIfVenomed.IDLE) return;
                tickDelay3 = 0;
                venomTimeout = 0;
                stateSwapIfVenomed = StateSwapIfVenomed.OPEN_INV;
                break;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null) return;
        
        tryToSwapIfVenomed();

        // --- VENOM SWAP LOGIC ---
        if (stateSwapIfVenomed != StateSwapIfVenomed.IDLE){
            tickDelay3++;
            venomTimeout++;
            
            if (venomTimeout > 40) { // 2 second failsafe
                resetVenomState();
                return;
            }

            switch (stateSwapIfVenomed){
                case OPEN_INV:
                    if (tickDelay3 >= 1){
                        mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                        mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                        if (alreadyDidSwap && mc.thePlayer.getActivePotionEffect(Potion.poison)==null) {
                            stateSwapIfVenomed = StateSwapIfVenomed.SWAP3;
                        } else {
                            stateSwapIfVenomed = StateSwapIfVenomed.SWAP1;
                        }
                        tickDelay3 = 0;
                    }
                    break;
                case SWAP1:
                    if (tickDelay3 >= 3) {
                        if (mc.currentScreen instanceof GuiInventory) {
                            int diamPantsSlot = -1;
                            for (int i = 0; i <= 35; i++) {
                                ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
                                if (item != null && item.getItem() == Items.diamond_leggings) {
                                    diamPantsSlot=i;
                                    break;
                                }
                            }
                            if (diamPantsSlot == -1){
                                stateSwapIfVenomed = StateSwapIfVenomed.CLOSE_INV;
                                break;
                            }
                            if (diamPantsSlot <= 8) { // Hotbar
                                mc.playerController.windowClick(0, 7, diamPantsSlot, 2, mc.thePlayer);
                                oldInvSlot = diamPantsSlot;
                                tickDelay3 = 0;
                                stateSwapIfVenomed = StateSwapIfVenomed.CLOSE_INV;
                                break;
                            }
                            mc.playerController.windowClick(0, diamPantsSlot, 5, 2, mc.thePlayer);
                            oldInvSlot = diamPantsSlot;
                            tickDelay3 = 0;
                            stateSwapIfVenomed = StateSwapIfVenomed.SWAP2;
                        } else resetVenomState();
                    }
                    break;
                case SWAP2:
                    if (tickDelay3 >= 3) {
                        if (mc.currentScreen instanceof GuiInventory) {
                            mc.playerController.windowClick(0, 7, 5, 2, mc.thePlayer);
                            tickDelay3 = 0;
                            stateSwapIfVenomed = StateSwapIfVenomed.CLOSE_INV;
                        } else resetVenomState();
                    }
                    break;
                case SWAP3: // Swapping Back
                    if (tickDelay3 >= 3) {
                        if (mc.currentScreen instanceof GuiInventory) {
                            if (oldInvSlot <= 8) {
                                mc.playerController.windowClick(0, 7, oldInvSlot, 2, mc.thePlayer);
                                stateSwapIfVenomed = StateSwapIfVenomed.CLOSE_INV;
                                alreadyDidSwap = false;
                                tickDelay3 = 0;
                                break;
                            }
                            mc.playerController.windowClick(0, 7, 5, 2, mc.thePlayer);
                            stateSwapIfVenomed = StateSwapIfVenomed.SWAP4;
                            tickDelay3 = 0;
                        } else resetVenomState();
                    }
                    break;
                case SWAP4:
                    if (tickDelay3 >= 3) {
                        if (mc.currentScreen instanceof GuiInventory) {
                            mc.playerController.windowClick(0, oldInvSlot, 5, 2, mc.thePlayer);
                            stateSwapIfVenomed = StateSwapIfVenomed.CLOSE_INV;
                            alreadyDidSwap = false;
                            tickDelay3 = 0;
                        } else resetVenomState();
                    }
                    break;
                case CLOSE_INV:
                    if (tickDelay3 >= 2) {
                        resetVenomState();
                    }
                    break;
            }
        }

        // --- NORMAL PANT SWAP (Right Click) ---
        if (state != State.IDLE) {
            tickDelay++;
            normalSwapTimeout++;
            
            if (normalSwapTimeout > 30) {
                resetNormalSwap();
                return;
            }

            switch (state) {
                case OPEN_INV:
                    if (tickDelay >= 1) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                        mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                        state = State.SWAP;
                        tickDelay = 0;
                    }
                    break;
                case SWAP:
                    if (tickDelay >= 3) {
                        if (mc.currentScreen instanceof GuiInventory) {
                            mc.playerController.windowClick(0, 7, mc.thePlayer.inventory.currentItem, 2, mc.thePlayer);
                            tickDelay = 0;
                            state = State.CLOSE_INV;
                        } else resetNormalSwap();
                    }
                    break;
                case CLOSE_INV:
                    if (tickDelay >= 2) {
                        resetNormalSwap();
                    }
                    break;
            }
        }
    }
}