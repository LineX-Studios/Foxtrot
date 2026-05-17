package com.linexstudios.foxtrot.Util;

import com.linexstudios.foxtrot.Misc.EnchantNames;
import com.linexstudios.foxtrot.WhoGotBanned;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldLoadListener {

    public static final WorldLoadListener instance = new WorldLoadListener();

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        EnchantNames.clearCache();

        // -- CHANGE 7: Reset WhoGotBanned state on every world/lobby load --
        // This starts the 10-second grace period, clears all candidates,
        // and resets the already-reported cache. Mirrors Ronimod's onEnable() reset.
        WhoGotBanned.instance.onLobbyJoin();
    }
}