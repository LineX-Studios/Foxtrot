package com.linexstudios.foxtrot.Util;

import com.linexstudios.foxtrot.Enemy.EnemyManager;
import com.linexstudios.foxtrot.Handler.ConfigHandler;
import com.linexstudios.foxtrot.Hud.EnemyHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;

public class EnemyAlert {
    private final Set<String> alertedThisLobby = new HashSet<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !EnemyHUD.notificationsEnabled || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;

        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (player == Minecraft.getMinecraft().thePlayer) continue;

            String currentName = player.getName();
            String uuid = player.getUniqueID().toString();

            // FIXED: Pass the player's name as a String
            if (EnemyHUD.isTarget(currentName) && !alertedThisLobby.contains(uuid)) {
                alertedThisLobby.add(uuid);

                String cachedName = EnemyManager.enemyCache.get(uuid);

                if (cachedName != null && !cachedName.equals(currentName)) {
                    String alert = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] "
                            + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "NAME CHANGE: "
                            + EnumChatFormatting.GRAY + cachedName + EnumChatFormatting.GOLD + " is now " + EnumChatFormatting.GREEN + currentName + EnumChatFormatting.GREEN + "!";
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(alert));

                    if (!EnemyHUD.targetList.contains(currentName)) {
                        EnemyHUD.targetList.add(currentName);
                        ConfigHandler.saveConfig();
                    }
                } else {
                    String alert = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] "
                            + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "ENEMY: "
                            + EnumChatFormatting.RED + currentName + EnumChatFormatting.YELLOW + " has entered your lobby!";
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(alert));
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        alertedThisLobby.clear();
    }
}