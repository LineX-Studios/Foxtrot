package com.linexstudios.foxtrot.Combat;

import com.linexstudios.foxtrot.Foxtrot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.enchantment.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.util.*;
import java.util.Random;

public class ChestStealer {
    public static final ChestStealer instance = new ChestStealer();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final Random rand = new Random();

    public static boolean enabled = false, autoClose = false, nameCheck = true, skipTrash = true, moreArmor = false, moreSword = false;
    public static float minDelay = 1f, maxDelay = 2f, openDelay = 1f;

    private int clickDelay = 0, oDelay = 0;
    private boolean inChest = false, warnedFull = false;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Foxtrot.toggleChestStealerKey != null && Foxtrot.toggleChestStealerKey.isPressed()) {
            enabled = !enabled;
            if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.GRAY + "Chest Stealer: " + (enabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF")));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || !enabled) return;
        if (clickDelay > 0) clickDelay--;
        if (oDelay > 0) oDelay--;

        if (!(mc.currentScreen instanceof GuiChest)) { inChest = false; return; }
        Container container = ((GuiChest) mc.currentScreen).inventorySlots;
        if (!(container instanceof ContainerChest)) { inChest = false; return; }

        if (!inChest) { inChest = true; warnedFull = false; oDelay = (int)openDelay + 1; }

        if (oDelay <= 0 && clickDelay <= 0) {
            IInventory inv = ((ContainerChest) container).getLowerChestInventory();
            if (nameCheck && !inv.getName().toLowerCase().contains("chest")) return;

            if (mc.thePlayer.inventory.getFirstEmptyStack() == -1) {
                if (!warnedFull) { mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.RED + "Inventory full!")); warnedFull = true; }
                if (autoClose) mc.thePlayer.closeScreen();
            } else {
                if (skipTrash) {
                    int bestSword = -1, bestPick = -1, bestShovel = -1, bestAxe = -1, bestBow = -1;
                    double bestDmg = 0.0, bestBowDmg = 0.0;
                    float bestPickEff = 1.0f, bestShovelEff = 1.0f, bestAxeEff = 1.0f;
                    int[] bestArmorSlots = {-1, -1, -1, -1};
                    double[] bestArmorProt = {0.0, 0.0, 0.0, 0.0};

                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        if (container.getSlot(i).getHasStack()) {
                            ItemStack stack = container.getSlot(i).getStack();
                            Item item = stack.getItem();
                            if (item instanceof ItemSword) { double d = getDmg(stack); if (bestSword == -1 || d > bestDmg) { bestSword = i; bestDmg = d; } }
                            else if (item instanceof ItemArmor) { int t = ((ItemArmor) item).armorType; double p = getProt(stack); if (bestArmorSlots[t] == -1 || p > bestArmorProt[t]) { bestArmorSlots[t] = i; bestArmorProt[t] = p; } }
                            else if (item instanceof ItemPickaxe) { float e = getEff(stack); if (bestPick == -1 || e > bestPickEff) { bestPick = i; bestPickEff = e; } }
                            else if (item instanceof ItemSpade) { float e = getEff(stack); if (bestShovel == -1 || e > bestShovelEff) { bestShovel = i; bestShovelEff = e; } }
                            else if (item instanceof ItemAxe) { float e = getEff(stack); if (bestAxe == -1 || e > bestAxeEff) { bestAxe = i; bestAxeEff = e; } }
                            else if (item instanceof ItemBow) { double d = getBowDmg(stack); if (bestBow == -1 || d > bestBowDmg) { bestBow = i; bestBowDmg = d; } }
                        }
                    }

                    if (bestDmg > getPlayerBestSword()) { shiftClick(container.windowId, bestSword); return; }
                    for (int i = 0; i < 4; i++) { if (bestArmorProt[i] > getPlayerBestArmor(i)) { shiftClick(container.windowId, bestArmorSlots[i]); return; } }
                    if (bestPickEff > getPlayerBestTool(ItemPickaxe.class)) { shiftClick(container.windowId, bestPick); return; }
                    if (bestShovelEff > getPlayerBestTool(ItemSpade.class)) { shiftClick(container.windowId, bestShovel); return; }
                    if (bestAxeEff > getPlayerBestTool(ItemAxe.class)) { shiftClick(container.windowId, bestAxe); return; }
                    if (bestBowDmg > getPlayerBestBow()) { shiftClick(container.windowId, bestBow); return; }
                }

                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    if (container.getSlot(i).getHasStack()) {
                        ItemStack stack = container.getSlot(i).getStack();
                        if (!skipTrash || isValuable(stack)) { shiftClick(container.windowId, i); return; }
                    }
                }
                if (autoClose) mc.thePlayer.closeScreen();
            }
        }
    }

    private void shiftClick(int wId, int slot) { mc.playerController.windowClick(wId, slot, 0, 1, mc.thePlayer); clickDelay = (int)minDelay + rand.nextInt((int)Math.max(1, maxDelay - minDelay + 1)); }
    private double getDmg(ItemStack s) { if (s==null||!(s.getItem() instanceof ItemSword)) return 0; return ((ItemSword)s.getItem()).getDamageVsEntity() + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, s) * 1.25; }
    private double getProt(ItemStack s) { if (s==null||!(s.getItem() instanceof ItemArmor)) return 0; return ((ItemArmor)s.getItem()).damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, s) * 1.5; }
    private float getEff(ItemStack s) { if (s==null||!(s.getItem() instanceof ItemTool)) return 0; String m = ((ItemTool)s.getItem()).getToolMaterialName(); float e = m.equals("GOLD") ? 12f : m.equals("EMERALD") ? 8f : m.equals("IRON") ? 6f : m.equals("STONE") ? 4f : 2f; int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, s); return e + (l>0?l*l+1:0); }
    private double getBowDmg(ItemStack s) { if (s==null||!(s.getItem() instanceof ItemBow)) return 0; return EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, s) * 1.5; }
    private double getPlayerBestSword() { double b=0; for(int i=0;i<mc.thePlayer.inventory.getSizeInventory();i++){ double d=getDmg(mc.thePlayer.inventory.getStackInSlot(i)); if(d>b)b=d; } return b; }
    private double getPlayerBestArmor(int t) { double b=0; for(int i=0;i<mc.thePlayer.inventory.getSizeInventory();i++){ ItemStack s=mc.thePlayer.inventory.getStackInSlot(i); if(s!=null&&s.getItem() instanceof ItemArmor&&((ItemArmor)s.getItem()).armorType==t){ double p=getProt(s); if(p>b)b=p; } } return b; }
    private float getPlayerBestTool(Class<?> c) { float b=1.0f; for(int i=0;i<mc.thePlayer.inventory.getSizeInventory();i++){ ItemStack s=mc.thePlayer.inventory.getStackInSlot(i); if(s!=null&&c.isInstance(s.getItem())){ float e=getEff(s); if(e>b)b=e; } } return b; }
    private double getPlayerBestBow() { double b=0; for(int i=0;i<mc.thePlayer.inventory.getSizeInventory();i++){ double d=getBowDmg(mc.thePlayer.inventory.getStackInSlot(i)); if(d>b)b=d; } return b; }
    private boolean isValuable(ItemStack stack) { Item i = stack.getItem(); if (i instanceof ItemEnderPearl || i instanceof ItemFood || i instanceof ItemPotion || i instanceof ItemBlock || i instanceof ItemBow || i.getClass().getSimpleName().equals("ItemArrow")) return true; if (moreArmor && i instanceof ItemArmor) { ItemArmor.ArmorMaterial m = ((ItemArmor)i).getArmorMaterial(); return m == ItemArmor.ArmorMaterial.DIAMOND || (m == ItemArmor.ArmorMaterial.IRON && stack.isItemEnchanted()); } if (moreSword && i instanceof ItemSword) { String n = ((ItemSword)i).getToolMaterialName(); return n.equals("EMERALD") || EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) > 0 || (n.equals("IRON") && stack.isItemEnchanted()); } return false; }
}