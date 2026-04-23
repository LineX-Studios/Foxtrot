package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class VenomTimer extends DraggableHUD {

    public static final VenomTimer instance = new VenomTimer();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = false;

    private EntityPlayer targetPlayer = null;
    private long venomEndTime = 0;

    public VenomTimer() {
        super("Venom Timer", 10, 50);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!enabled || mc.thePlayer == null || !(event.target instanceof EntityPlayer)) return;

        ItemStack pants = mc.thePlayer.getCurrentArmor(1);
        if (pants != null && pants.hasTagCompound()) {
            NBTTagCompound extra = pants.getTagCompound().getCompoundTag("ExtraAttributes");
            if (extra != null && extra.hasKey("CustomEnchants")) {
                NBTTagList enchants = extra.getTagList("CustomEnchants", 10);
                boolean hasVenom = false;
                for (int i = 0; i < enchants.tagCount(); i++) {
                    if (enchants.getCompoundTagAt(i).getString("Key").toLowerCase().contains("venom")) {
                        hasVenom = true;
                        break;
                    }
                }
                if (hasVenom) {
                    targetPlayer = (EntityPlayer) event.target;
                    venomEndTime = System.currentTimeMillis() + 12000L;
                }
            }
        }
    }

    @SubscribeEvent
    public void onNameFormat(PlayerEvent.NameFormat event) {
        if (!enabled || targetPlayer == null || event.entityPlayer != targetPlayer) return;

        long timeLeft = venomEndTime - System.currentTimeMillis();
        if (timeLeft > 0) {
            int seconds = (int) Math.ceil(timeLeft / 1000.0);
            event.displayname = event.displayname + EnumChatFormatting.DARK_PURPLE + "Venomed: " + EnumChatFormatting.RED + seconds + "s";
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (targetPlayer != null && System.currentTimeMillis() > venomEndTime) {
            targetPlayer = null; 
        }
    }

    @SubscribeEvent
    public void onRenderHUD(RenderGameOverlayEvent.Post event) {
        if (!enabled || event.type != RenderGameOverlayEvent.ElementType.TEXT || mc.thePlayer == null) return;
        if (mc.currentScreen instanceof EditHUDGui) return;
        
        this.render(false, 0, 0);
    }

    @Override
    public void draw(boolean isEditing) {
        long timeLeft = venomEndTime - System.currentTimeMillis();
        String text;

        if (isEditing) {
            text = EnumChatFormatting.DARK_PURPLE + "Venomed: " + EnumChatFormatting.RED + "12.0s";
        } else if (targetPlayer != null && timeLeft > 0) {
            double seconds = Math.round((timeLeft / 1000.0) * 10.0) / 10.0;
            text = EnumChatFormatting.DARK_PURPLE + "Venomed: " + EnumChatFormatting.RED + seconds + "s";
        } else {
            return;
        }

        mc.fontRendererObj.drawStringWithShadow(text, 0, 0, 0xFFFFFF);
        this.width = mc.fontRendererObj.getStringWidth(text);
        this.height = mc.fontRendererObj.FONT_HEIGHT;
    }
    
    @Override
    public boolean isEnabled() { return enabled; }
}