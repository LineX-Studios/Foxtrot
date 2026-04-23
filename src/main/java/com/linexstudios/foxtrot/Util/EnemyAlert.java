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

            // ONLY alert if EnemyHUD fully validates them (Checks for UUID spoofing)
            if (EnemyHUD.isTarget(player) && !alertedThisLobby.contains(uuid)) {
                alertedThisLobby.add(uuid);

                String cachedName = EnemyManager.enemyCache.get(uuid);

                // If we have a cached UUID for them, check if their name changed!
                if (cachedName != null && !cachedName.equals(currentName)) {
                    String alert = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] "
                            + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "NAME CHANGE: "
                            + EnumChatFormatting.GRAY + cachedName + EnumChatFormatting.GOLD + " is now " + EnumChatFormatting.GREEN + currentName + EnumChatFormatting.GREEN + "!";
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(alert));

                    // Automatically update the main enemies.txt list so commands still work on their new name
                    if (!EnemyHUD.targetList.contains(currentName)) {
                        EnemyHUD.targetList.add(currentName);
                        ConfigHandler.saveConfig();
                    }
                    
                    // NOTE: We DO NOT update enemyCache here anymore! 
                    // Leaving the original name in the cache allows EnemyHUD to read it and display the Alias!
                    
                } else {
                    // Standard Join Alert
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