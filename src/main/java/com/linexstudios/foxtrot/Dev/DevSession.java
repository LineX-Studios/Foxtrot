package com.linexstudios.foxtrot.Dev;

/**
 * Developer session gate.
 * Activated by /fx dev auth <key> which validates against the Foxtrot API Manager.
 * State is memory-only — NEVER written to disk. Auto-clears when the game exits.
 */
public class DevSession {

    private static boolean active = false;

    public static boolean isActive() {
        return active;
    }

    /**
     * Called by CommandFoxtrot after the API Manager confirms the license key is valid.
     */
    public static void unlock() {
        active = true;
    }

    /**
     * Deactivates dev mode for this session.
     */
    public static void lock() {
        active = false;
    }
}
