package com.linexstudios.foxtrot.Util;

import com.linexstudios.foxtrot.Misc.EnchantNames;
import com.linexstudios.foxtrot.Misc.RingHelper;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldLoadListener {
    
    public static final WorldLoadListener instance = new WorldLoadListener();

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        EnchantNames.clearCache();
        RingHelper.instance.onWorldChanged();
    }
}