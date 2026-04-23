# Foxtrot Mod - Feature List

## 🖼️ HUD System
- **Visual Editor**: Click-and-drag positioning with resizing and double-click to configuration.
- **Modules**:
    - **Enemy/Friend/Teammate Lists**: tracking of specific player groups.
    - **Nicked List**: Tracks "nicked" players in the lobby.
    - **Reg/Darks HUD**: tracking for Regularity (rage pants) And Dark Pants users.
    - **Potion/Armor/Coords**: Clean, customizable status overlays.
    - **Session Stats**: Tracks XP, gold, and performance per session.

## 👁️ ESP & Visuals
- **Multi-Color Highlighting**:
    - **Red**: Enemies.
    - **Green**: Friends.
    - **Cyan**: Teammates & Guild members.

- **Performance**: Uses Netty packet interception for zero-lag ESP and event tracking.

## 🔍 Denicking & Identification
- **Auto-Denick**: Automatically scrapes Hypixel API/History to identify nicked players.
- **UUID Persistence**: Tracks players by UUID to ensure they remain identified even if they change their username.
- **PitPal Integration**: Synchronizes guild rosters automatically to be added to teamates works as long as you are in a guild or you can manually add a player by using /fx t <add|remove|list> [name] - /fx g <add|remove|list> [name].

## ⚔️ Combat
- **Auto-Clicker**: Safe, randomized clicking with inventory-fill and block-breaking support.
- **ChestStealer**: Configurable cheststealer with GUI-safe toggling on and off with keybindings in the control menu in your minecraft.
- **W-Tap**: Automated W-TAP for better combos.
- **Auto-Ghead**: Automatic Golden Head consumption based on health / Automatic First Aid Egg based on health.
- **Auto-PantSwap**: pant swapping hold right click on the pants in your hotbar and they will be automatically equipped, and also for venomswap it will automatically take off venoms when you venom someone switching to diamond pants and once venom wears off it automatically wears venoms again.
- **Auto-BulletTime**: have a bulletTime Sword in your hotbar and if the module is enabled just hold right click with any sword and it will automatically switch to BulletTime Sword.
## ⚡ Performance & Stability
- **FastFont Engine**: LRU-cached string width calculations for massive FPS boost in full 80+ lobbies.
- **Ghost Filter**: "Double-Verification" system that purges disconnected players and irrelevant NPCs from HUD lists in case they manage to get stuck by accident in any lists they will automatically be removed. and no more whitenames if a player disconnects.
- **Async Scraper Throttling**: Efficient thread management for background denicking tasks.
- **Netty Hardening**: Throwable-guarded packet processing to prevent native memory crashes.

## 🛠️ Utilities
- **Dead Lobby Finder**: Finds and warps you into low-player lobbies.
- **Ring Helper**: Assists you with a ring to be able to block up mid properly.
- **Focus Mode**: Hides all players except the one you are focusing on.
- **Rank/Name Spoofing**: Customize your local appearance.
- **Venom Timer**: Track your venom cooldowns.
- **ModIDHider**: Intercepts the `MC|Brand` handshake packet and rewrites it to `vanilla` — hides Foxtrot from Hypixel server-side mod detection.
