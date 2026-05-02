# Foxtrot Mod - Command Guide

## Base Command
- `/foxtrot` or `/fx`

## Player Management

### ⚔️ Enemies
- `/fx add [name]` - Add a player to the enemy list.
- `/fx remove [name]` - Remove a player from the enemy list.
- `/fx list` - View the current enemy list.
- `/fx clear` - Clear all enemies.

### 🤝 Friends
- `/fx friend add [name]` - Add a player to the friend list (UUID-based).
- `/fx friend remove [name]` - Remove a player from the friend list.
- `/fx friend list` - View the current friend list.

### 🛡️ Teammates / Guild
- `/fx team <add|remove|list> [name]` - Manage your teammates.
- `/fx guild <add|remove|list> [name]` - Alias for teammate command.
- `/fx t <add|remove|list> [name]` - Short alias.
- `/fx g <add|remove|list> [name]` - Short alias.
- *Note: Guild members are automatically synced once a day via PitPal API.*

## Visuals & ESP
- `/fx esp` - Master toggle for all ESP (Enemy, Friend, Teammate).
- `/fx nonhighlighter` - Toggle Non-Highlighter ESP.
- `/fx nickhud` - Toggle the Nicked HUD list.
- `/fx enemyhud` - Toggle the Enemy HUD list.
- `/fx hud` - Open the visual HUD editor.
- `/fx alerts` - Toggle join notifications for enemies.
- `/fx toggle` - Global toggle for all HUD elements.

## Denicking
- `/fx denick [name]` - Manually scrape and denick nicked players (Mystic items on hotbar/Armor Slot required).
- `/fx denickentry clear [name]` - Clear cached nick info for a player.
- `/fx denickentry clear` - clear ALL cached nick entries.
- `/fx autodenick` - Toggle automatic denicking (Mystic-Only).

## Utilities
- `/fx focus <name|remove|clear>` - Hide all players except the target.
- `/fx rank <prestige> <level> <rank>` - Set your spoofed rank visuals.
- `/fx nickname <name|off>` - Spoof your in-game name.
- `/fx ring` - Toggle the Ring Helper module.
- `/fx deadlobby` - Toggle the Dead Lobby Finder.
- `/fx venom` - Toggle the Venom Timer HUD.
- `/fx enchantnames` - Toggle custom enchantment names on items on top of the player's nametag.

## Debugging
- `/fx debug` - Toggle global debug logging (For Developers only).

## Stealth
- `/fx modidhider` - Toggle spoofing the client brand to "vanilla" to avoid mod detection on Hypixel.
- `/fx rpc` - Toggle the Discord Rich Presence (Alias for the setting button).
