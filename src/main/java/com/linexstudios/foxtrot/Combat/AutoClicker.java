package com.linexstudios.foxtrot.Combat;

import com.linexstudios.foxtrot.Foxtrot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AutoClicker {
    public static final AutoClicker instance = new AutoClicker();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random rand = new Random();
    
    // --- Reflection Fields & Methods ---
    private Field leftClickCounterField;
    private Field isHittingBlockField; 
    private Method mouseClickedMethod;

    // --- Toggles ---
    public static boolean enabled = false;
    public static boolean debugMode = false;
    public static boolean leftClick = true;
    public static boolean fastPlaceEnabled = false; 
    public static boolean holdToClick = true;

    // --- Inventory Fill ---
    public static boolean inventoryFill = true;
    public static float inventoryFillCps = 15.0F;
    private long lastInvClick = 0L;

    // --- Settings ---
    public static float minCps = 9.0F;
    public static float maxCps = 13.0F;
    public static int randomMode = 1; 
    public static boolean breakBlocks = true;

    // --- Whitelist (Strictly for Left Click) ---
    public static boolean limitItems = false;
    public static List<String> itemWhitelist = new ArrayList<>(Arrays.asList("swords", "axes", "pickaxes"));

    private long lastLeftClickTime = 0L;
    private long nextLeftDelay = 0L;
    private long lastRightClickTime = 0L;
    private long nextRightDelay = 0L;

    public AutoClicker() {
        try {
            leftClickCounterField = Minecraft.class.getDeclaredField("leftClickCounter");
        } catch (NoSuchFieldException e) {
            try { leftClickCounterField = Minecraft.class.getDeclaredField("field_71429_W"); } catch (Exception ex) {}
        }
        if (leftClickCounterField != null) leftClickCounterField.setAccessible(true);

        try {
            isHittingBlockField = PlayerControllerMP.class.getDeclaredField("isHittingBlock");
        } catch (NoSuchFieldException e) {
            try { isHittingBlockField = PlayerControllerMP.class.getDeclaredField("field_78778_j"); } catch (Exception ex) {}
        }
        if (isHittingBlockField != null) isHittingBlockField.setAccessible(true);

        // NATIVE GUI CLICKING SETUP
        try {
            mouseClickedMethod = GuiScreen.class.getDeclaredMethod("mouseClicked", int.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            try { mouseClickedMethod = GuiScreen.class.getDeclaredMethod("func_73864_a", int.class, int.class, int.class); } catch (Exception ex) {}
        }
        if (mouseClickedMethod != null) {
            mouseClickedMethod.setAccessible(true);
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Foxtrot.toggleCombatKey != null && Foxtrot.toggleCombatKey.isPressed()) {
            enabled = !enabled;
            if (mc.thePlayer != null) {
                String state = enabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.GRAY + "Auto-Clicker: " + state));
            }
        }
        if (Foxtrot.toggleInvFillKey != null && Foxtrot.toggleInvFillKey.isPressed()) {
            inventoryFill = !inventoryFill;
            if (mc.thePlayer != null) {
                String state = inventoryFill ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.GRAY + "Inventory-Fill: " + state));
            }
        }
    }

    private boolean getIsActivelyMining() {
        if (mc.playerController == null || isHittingBlockField == null) return false;
        try {
            return isHittingBlockField.getBoolean(mc.playerController);
        } catch (Exception e) {
            return false;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // INV FILL
        if (mc.currentScreen instanceof GuiContainer) {
            boolean isShiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            
            if (inventoryFill && Mouse.isButtonDown(0) && isShiftDown) {
                long invDelay = inventoryFillCps >= 20.0F ? 20 : (long)(1000.0F / inventoryFillCps);
                
                if (System.currentTimeMillis() - lastInvClick >= invDelay) {
                    GuiContainer gui = (GuiContainer) mc.currentScreen;
                    
                    int mouseX = Mouse.getX() * gui.width / mc.displayWidth;
                    int mouseY = gui.height - Mouse.getY() * gui.height / mc.displayHeight - 1;
                    
                    try {
                        if (mouseClickedMethod != null) {
                            mouseClickedMethod.invoke(gui, mouseX, mouseY, 0);
                        }
                    } catch (Exception e) {}
                    
                    lastInvClick = System.currentTimeMillis();
                }
            }
            return; 
        }

        if (!enabled) return;

        int attackKey = mc.gameSettings.keyBindAttack.getKeyCode();
        int useKey = mc.gameSettings.keyBindUseItem.getKeyCode();
        
        ItemStack held = mc.thePlayer.getHeldItem();
        boolean holdingBlock = (held != null && held.getItem() instanceof ItemBlock);
        boolean isActivelyMining = getIsActivelyMining();

        if (event.phase == TickEvent.Phase.START) {
            if (leftClick && holdToClick && Mouse.isButtonDown(0)) {
                if (breakBlocks && isActivelyMining) {
                    KeyBinding.setKeyBindState(attackKey, true); 
                } else {
                    KeyBinding.setKeyBindState(attackKey, false); 
                }
            }
            if (fastPlaceEnabled && holdToClick && Mouse.isButtonDown(1)) {
                if (holdingBlock) KeyBinding.setKeyBindState(useKey, false);
            }
            return;
        }

        if (mc.currentScreen != null) return;

        // --- LEFT CLICK LOGIC ---
        if (leftClick) {
            boolean shouldLeftClick = !holdToClick || Mouse.isButtonDown(0);
            
            if (breakBlocks && isActivelyMining) {
                shouldLeftClick = false; 
            }
            
            if (limitItems && !isHoldingWhitelistedItem()) {
                shouldLeftClick = false;
            }

            if (shouldLeftClick && System.currentTimeMillis() - lastLeftClickTime >= nextLeftDelay) {
                try {
                    if (leftClickCounterField != null) leftClickCounterField.set(mc, 0); 
                    
                    KeyBinding.setKeyBindState(attackKey, true);
                    KeyBinding.onTick(attackKey);
                    
                    net.minecraftforge.client.event.MouseEvent fakeEvent = new net.minecraftforge.client.event.MouseEvent();
                    Field btnField = net.minecraftforge.client.event.MouseEvent.class.getDeclaredField("button");
                    btnField.setAccessible(true);
                    btnField.set(fakeEvent, 0); 
                    Field stateField = net.minecraftforge.client.event.MouseEvent.class.getDeclaredField("buttonstate");
                    stateField.setAccessible(true);
                    stateField.set(fakeEvent, true); 
                    net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(fakeEvent);
                    net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent());
                } catch (Exception e) {}
                
                lastLeftClickTime = System.currentTimeMillis();
                nextLeftDelay = generateNextDelay();
            }
        }

        // --- RIGHT CLICK LOGIC (FAST PLACE) ---
        if (fastPlaceEnabled) {
            boolean shouldRightClick = (!holdToClick || Mouse.isButtonDown(1)) && holdingBlock;
            if (shouldRightClick && System.currentTimeMillis() - lastRightClickTime >= nextRightDelay) {
                KeyBinding.setKeyBindState(useKey, true);
                KeyBinding.onTick(useKey);
                lastRightClickTime = System.currentTimeMillis();
                nextRightDelay = generateNextDelay();
            }
        }
    }

    private long generateNextDelay() {
        float targetCps = minCps + (rand.nextFloat() * (maxCps - minCps));
        long baseDelay = (long) (1000.0F / targetCps);
        long randomOffset = 0;
        switch (randomMode) {
            case 0: randomOffset = (long) ((rand.nextGaussian() * 15) - 7); break;
            case 1: randomOffset = (long) ((rand.nextGaussian() * 25) - 10); break;
            case 2: randomOffset = (long) ((rand.nextGaussian() * 35) - 15); break;
        }
        return Math.max(20, baseDelay + randomOffset);
    }

    private boolean isHoldingWhitelistedItem() {
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null) return false;
        Item item = held.getItem();
        
        for (String w : itemWhitelist) {
            String check = w.toLowerCase().trim();
            if (check.isEmpty()) continue;
            
            if (check.equals("swords") || check.equals("sword")) {
                if (item instanceof ItemSword) return true;
            } else if (check.equals("axes") || check.equals("axe")) {
                if (item instanceof ItemAxe) return true;
            } else if (check.equals("pickaxes") || check.equals("pickaxe")) {
                if (item instanceof ItemPickaxe) return true;
            } else if (check.equals("shovels") || check.equals("shovel") || check.equals("spades") || check.equals("spade")) {
                if (item instanceof ItemSpade) return true;
            } else if (check.equals("blocks") || check.equals("block")) {
                if (item instanceof ItemBlock) return true;
            } else if (item.getUnlocalizedName().toLowerCase().contains(check)) {
                return true;
            }
        }
        return false;
    }
}