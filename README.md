# Foxtrot-PIT

[![Foxtrot Version](https://img.shields.io/github/v/tag/LineX-Studios/Foxtrot?label=Foxtrot&color=blue&style=flat-square)](https://github.com/LineX-Studios/Foxtrot/releases/latest)
[![Build Status](https://github.com/LineX-Studios/FOXTROT-PIT/actions/workflows/build.yml/badge.svg)](https://github.com/LineX-Studios/Foxtrot/actions)
![Minecraft Version](https://img.shields.io/badge/Minecraft-1.8.9-gray?style=flat-square)
[![Total Downloads](https://img.shields.io/github/downloads/LineX-Studios/Foxtrot/total.svg)]([https://github.com/linexstudios/foxtrot/](https://github.com/LineX-Studios/Foxtrot/)releases)

[![Discord](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/3mDn6vcTuK)


# END OF LIFE - NO MORE UPDATES / MOD SUPPORT AFTER VERSION 1.0.3 RELEASES.

Foxtrot is a highly optimized, open-source utility mod  exclusively for 1.8.9 Forge. Built or Hypixel Pit, it adds quality-of-life features, including, automated ban tracking, Enemy List, Friends List, Darks List and more.


### Installation notes
* **Minecraft 1.8.9 Forge** is required.
* **jdk 8** is required (Standard for 1.8.9).

### Support & Links
* **Support & discussion:** [Discord](https://discord.gg/ExfhRcVJnU)
* **Report issues:** [Report any Issues here](https://github.com/LineX-Studios/FOXTROT/issues)
* **Source Code:** [GitHub Repository](https://github.com/LineX-Studios/FOXTROT)
* **Live Telemetry:** [Website](https://linex-studios.github.io/)

---

## Features

<details>
  <summary><b>Custom HUD & GUI</b></summary>
  <ul>
    <li><b>Customization</b> - A fully draggable custom GUI. Press your Edit HUD keybind to click, drag, and snap any module anywhere on your screen.</li>
    <li><b>HUD Modules</b> - <b>FPS</b>, <b>CPS</b>, and <b>Coordinates</b>.</li>
    <li><b>ToggleSprint</b> - Built-in toggle sprint.</li>
  </ul>
</details>

<details>
  <summary><b>Pit Utils</b></summary>
  <ul>
    <li><b>Session Stats</b> - Tracks your kills, deaths, gold, and XP for your current Pit session.</li>
    <li><b>Upcoming Events</b> - HUD module that tracks and displays upcoming Pit events so.</li>
    <li><b>Telebow Timer</b> - HUD cooldown tracker for your Telebow.</li>
    <li><b>Low Life Mystics Alert</b> - Visual alerts in your inventory/Enderchest to highlight mystic items that are low on lives.</li>
    <li><b>Ban Detection</b> - Instantly detects Watchdog ban messages and shows you exactly which player was removed from your lobby, revealing their name in your chat.</li>
  </ul>
</details>

<details>
  <summary><b>Quality-Of-Life</b></summary>
  <ul>
    <li><b>Friends List, Enemy List, Regularity List, Dark Pants Users.</b> - A Hud Element that lists all current friends/enemies in your lobby And Easily add Friends or Enemies in your Lists to help you improve your gameplay and know all threats in your lobby</li>
    <li><b>Nicked Players List</b> -  HUD element that lists all currently nicked players  in your lobby.</li>
    <li><b>Focus Mode</b> - Cluttered lobby? Turn on Focus Mode to completely hide all other players except your specific target, giving you perfect visibility during a 1v1.</li>
  </ul>
</details>

<details>
  <summary><b>Denick</b></summary>
  <ul>
    <li><b>Auto Denick</b> - Automatically scans the lobby and reveals nicked players in real-time.</li>
    <li><b>Manual Denick</b> - Run a command to scrape and reveal a specific player. all Nicked Users will appear on your HUD. and show you the Revealed name on the hud aswell.</li>
    <li><b>Ranks Changer</b> - change and customize your displayed Hypixel Rank, Prestige, and Level.</li>
    <li><b>Nickname Changer</b> - change your own username display.</li>
  </ul>
</details>

---

## Commands

Foxtrot features a comprehensive command system. You can use `/foxtrot` or the shorter `/fx` alias for all commands.

**HUD & Visuals:**
* `/fx hud` - Opens the Custom HUD Editor to drag and drop your modules.
* `/fx toggle` - Quickly toggles all Foxtrot HUDs on or off.
* `/fx nickhud` - Toggles the Nicked Players List HUD.
* `/fx enemyhud` - Toggles the Enemy List HUD.

**Tracking & Lists:**
* `/fx friend <add/remove/list> [name]` - Manage your Friends list.
* `/fx add [name]` - Add a player to your Enemy list.
* `/fx remove [name]` - Remove a player from your Enemy list.
* `/fx list` - View your active Enemy list.
* `/fx clear` - Clears your entire Enemy list.
* `/fx alerts` - Toggles chat alerts for when enemies join your lobby.

**Focus Mode:**
* `/fx focus [name]` - Hides all players except the specified name.
* `/fx focus remove [name]` - Removes a player from your focus target list.
* `/fx focus clear` - Disables Focus Mode and reveals all players again.

**Denicking:**
* `/fx autodenick` - Toggles Auto-Denick.
* `/fx denick [name]` - Manually scrape and denick a specific player.
* `/fx denickentry clear [name]` - Clears a specific player from the nick cache.

**Cosmetics:**
* `/fx rank <prestige> <level> <rank>` - Changes your Prestige, Level, and Rank *(Options: none, vip, vip+, mvp, mvp+, mvp++, youtube, staff, admin)*.
* `/fx nickname <name|off>` - Changes your  username display (or use `off` to revert to your real name).

**Miscs:**
* `/fx ringhelper` - Draws a 11x11 ring around mid to help you ring mid faster.
* `/fx deadlobby` - Helps you find dead lobbies in pit.
---
