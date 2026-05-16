package com.linexstudios.foxtrot;

import com.linexstudios.foxtrot.Commands.*;
import com.linexstudios.foxtrot.Denick.*;
import com.linexstudios.foxtrot.Enemy.*;
import com.linexstudios.foxtrot.Handler.*;
import com.linexstudios.foxtrot.Handler.Telemetry.TelemetryManager;
import com.linexstudios.foxtrot.Hud.*;
import com.linexstudios.foxtrot.Render.*;
import com.linexstudios.foxtrot.Combat.*;
import com.linexstudios.foxtrot.Misc.*;
import com.linexstudios.foxtrot.Misc.ModIDHider;
import com.linexstudios.foxtrot.Util.*;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;

@Mod(modid = "foxtrot", name = "Foxtrot", version = "${version}", acceptedMinecraftVersions = "[1.8.9]")
public class Foxtrot {

    public static KeyBinding toggleCombatKey;
    public static KeyBinding toggleInvFillKey;
    public static KeyBinding toggleWtapKey;
    public static KeyBinding toggleChestStealerKey;
    public static KeyBinding spawnKey;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Silence the "Security Seal" and "Not Secure" warnings in logs
        System.setProperty("sun.misc.URLClassPath.disableJarChecking", "true");

        ConfigHandler.loadConfig();
        PitDataHandler.init();
        com.linexstudios.foxtrot.Handler.FoxtrotUsersManager.initialize();
        com.linexstudios.foxtrot.Handler.CapeManager.loadEmbeddedCapes();
        TelemetryManager.initialize();
        KeybindHandler.init();
        if (ConfigHandler.discordRpcEnabled)
            DiscordRPCManager.start();

        toggleCombatKey = new KeyBinding("Toggle Auto-Clicker", Keyboard.KEY_DOWN, "Foxtrot");
        toggleInvFillKey = new KeyBinding("Toggle Inventory-Fill", Keyboard.KEY_RIGHT, "Foxtrot");
        toggleWtapKey = new KeyBinding("Toggle W-Tap", Keyboard.KEY_NONE, "Foxtrot");
        toggleChestStealerKey = new KeyBinding("Toggle Chest-Stealer", Keyboard.KEY_NONE, "Foxtrot");
        spawnKey = new KeyBinding("Toggle /Spawn Shortcut", Keyboard.KEY_NONE, "Foxtrot");

        ClientRegistry.registerKeyBinding(toggleCombatKey);
        ClientRegistry.registerKeyBinding(toggleInvFillKey);
        ClientRegistry.registerKeyBinding(toggleWtapKey);
        ClientRegistry.registerKeyBinding(toggleChestStealerKey);
        ClientRegistry.registerKeyBinding(spawnKey);

        // --- TRACKER & CULLING REGISTRATIONS ---
        FMLCommonHandler.instance().bus().register(PlayerTrackerHandler.instance);
        FMLCommonHandler.instance().bus().register(PacketHandler.instance);
        MinecraftForge.EVENT_BUS.register(PlayerTrackerHandler.instance);
        MinecraftForge.EVENT_BUS.register(BetterCulling.instance);

        // --- STANDARD MODULE REGISTRATIONS ---
        MinecraftForge.EVENT_BUS.register(EnemyHUD.instance);
        MinecraftForge.EVENT_BUS.register(NickedHUD.instance);
        MinecraftForge.EVENT_BUS.register(FriendsHUD.instance);
        MinecraftForge.EVENT_BUS.register(CPSModule.instance);
        MinecraftForge.EVENT_BUS.register(FPSModule.instance);
        MinecraftForge.EVENT_BUS.register(EventHUD.instance);
        MinecraftForge.EVENT_BUS.register(RegHUD.instance);
        MinecraftForge.EVENT_BUS.register(DarksHUD.instance);
        MinecraftForge.EVENT_BUS.register(PlayerCounterHUD.instance);
        MinecraftForge.EVENT_BUS.register(PotionHUD.instance);
        MinecraftForge.EVENT_BUS.register(ArmorHUD.instance);
        MinecraftForge.EVENT_BUS.register(CoordsHUD.instance);
        MinecraftForge.EVENT_BUS.register(ToggleSprintModule.instance);
        MinecraftForge.EVENT_BUS.register(NameTags.instance);
        MinecraftForge.EVENT_BUS.register(SessionStatsHUD.instance);
        MinecraftForge.EVENT_BUS.register(TelebowHUD.instance);
        MinecraftForge.EVENT_BUS.register(VenomTimer.instance);
        MinecraftForge.EVENT_BUS.register(new EnemyESP());
        MinecraftForge.EVENT_BUS.register(new FriendsESP());
        MinecraftForge.EVENT_BUS.register(new TeammateESP());
        MinecraftForge.EVENT_BUS.register(new TeammateManager());
        MinecraftForge.EVENT_BUS.register(new NickedRender());
        MinecraftForge.EVENT_BUS.register(PitESP.instance);
        MinecraftForge.EVENT_BUS.register(LowLifeMystic.instance);
        MinecraftForge.EVENT_BUS.register(FocusManager.instance);
        MinecraftForge.EVENT_BUS.register(AutoClicker.instance);
        MinecraftForge.EVENT_BUS.register(Wtap.instance);
        MinecraftForge.EVENT_BUS.register(ChestStealer.instance);
        MinecraftForge.EVENT_BUS.register(AutoPantSwap.instance);
        MinecraftForge.EVENT_BUS.register(AutoGhead.instance);
        MinecraftForge.EVENT_BUS.register(AutoBulletTime.instance);
        MinecraftForge.EVENT_BUS.register(AutoQuickMath.instance);
        MinecraftForge.EVENT_BUS.register(RingHelper.instance);
        MinecraftForge.EVENT_BUS.register(NonHighlighter.instance);
        MinecraftForge.EVENT_BUS.register(EnchantNames.instance);

        MinecraftForge.EVENT_BUS.register(WhoGotBanned.instance);

        MinecraftForge.EVENT_BUS.register(AutoDenick.instance);
        MinecraftForge.EVENT_BUS.register(NickScanner.instance);
        MinecraftForge.EVENT_BUS.register(new EnemyAlert());
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
        MinecraftForge.EVENT_BUS.register(new HUDController());
        MinecraftForge.EVENT_BUS.register(Ranks.instance);
        MinecraftForge.EVENT_BUS.register(new SpawnShortcut());
        MinecraftForge.EVENT_BUS.register(DeadLobbyFinder.instance);
        MinecraftForge.EVENT_BUS.register(WorldLoadListener.instance);
        MinecraftForge.EVENT_BUS.register(ModIDHider.instance);
        MinecraftForge.EVENT_BUS.register(MapDetectionHandler.instance);
        MinecraftForge.EVENT_BUS.register(NameFormatHandler.instance);

        ClientCommandHandler.instance.registerCommand(new CommandFoxtrot());
        System.out.println("[Foxtrot] Loaded.");
    }
}