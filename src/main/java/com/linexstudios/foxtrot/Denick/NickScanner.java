package com.linexstudios.foxtrot.Denick;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NickScanner {
    public static final NickScanner instance = new NickScanner();
    public static boolean enabled = true; // Standalone scanner is always on
    private final Minecraft mc = Minecraft.getMinecraft();
    private long lastScan = 0;
    
    // Keeps track of who we've already pushed to the HUD so we don't spam it
    public static final Set<String> detectedNicks = new HashSet<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled || mc.theWorld == null || mc.thePlayer == null || event.phase != TickEvent.Phase.END) return;

        // Scan the Tab List every 1 second (1000ms)
        if (System.currentTimeMillis() - lastScan < 1000) return;
        lastScan = System.currentTimeMillis();

        if (mc.getNetHandler() == null) return;

        for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
            if (info == null || info.getGameProfile() == null || info.getGameProfile().getId() == null) continue;

            UUID playerUUID = info.getGameProfile().getId();

            if (playerUUID.version() == 1) {
                String nickName = info.getGameProfile().getName();

                // Instantly detect them and push to your HUD/Manager before we even have their nonce
                if (!detectedNicks.contains(nickName)) {
                    detectedNicks.add(nickName);
                    
                    // Assuming you are still using your custom NickedManager for the HUD rendering:
                    NickedManager.addNicked(nickName, "\u00a77Scraping");
                }
            }
        }
    }
}