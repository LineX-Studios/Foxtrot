/*
 * ==============================================================================
 * Foxtrot-PIT - Open Source Bootstrapper & Auto-Updater
 * © 2026 Linex Studios & Foxtrot-PIT. All Rights Reserved.
 * * OPEN SOURCE LICENSE & LIABILITY WAIVER:
 * This code is open-source and provided "AS IS", without warranty of any kind, 
 * express or implied. In no event shall the authors or copyright holders (Linex 
 * Studios) be liable for any claim, damages, or other liability arising from, 
 * out of, or in connection with the software or the use of this software.
 * * ACCEPTABLE USE POLICY (ANTI-MALWARE):
 * While this code is open-source, this specific dynamic-injection and downloading 
 * architecture is highly sensitive. By viewing, copying, modifying, or distributing 
 * this code, you explicitly agree that it will NOT be repurposed, reverse-engineered, 
 * or utilized to download, execute, or inject unauthorized payloads, malware, 
 * remote access trojans (RATs), token loggers, or any malicious software.
 * ==============================================================================
 */
package com.linexstudios.foxtrot.Update;

import java.io.File;
import java.nio.file.*;

public class FoxtrotRelocator {
    public static void main(String[] args) {
        System.out.println("[FoxtrotRelocator] Starting background updater...");
        if (args.length < 3) { System.err.println("[FoxtrotRelocator] ERROR: Missing arguments."); System.exit(1); }
        File oldJar = new File(args[0]), tempJar = new File(args[1]), finalJar = new File(args[2]);
        System.out.println("[FoxtrotRelocator] Target Old Jar: " + oldJar.getAbsolutePath()); System.out.println("[FoxtrotRelocator] Target Final Jar: " + finalJar.getAbsolutePath());
        if (!tempJar.exists()) { System.err.println("[FoxtrotRelocator] ERROR: Downloaded temp jar does not exist."); System.exit(1); }
        System.out.println("[FoxtrotRelocator] Waiting for Minecraft to drop the file lock...");
        int attempts = 0;
        while (oldJar.exists() && attempts < 40) {
            try { Files.delete(oldJar.toPath()); System.out.println("[FoxtrotRelocator] SUCCESS: Old jar deleted natively."); break; } 
            catch (Exception e) { File oldRenamed = new File(oldJar.getAbsolutePath() + ".old"); if (oldJar.renameTo(oldRenamed)) { System.out.println("[FoxtrotRelocator] SUCCESS: Old jar renamed to .old as fallback."); break; } }
            attempts++; try { Thread.sleep(500); } catch (Exception e) {}
        }
        System.out.println("[FoxtrotRelocator] Proceeding to move new update into mods folder.");
        try { Files.move(tempJar.toPath(), finalJar.toPath(), StandardCopyOption.REPLACE_EXISTING); System.out.println("[FoxtrotRelocator] SUCCESS: New mod jar placed."); } 
        catch (Exception e) { System.err.println("[FoxtrotRelocator] WARNING: Files.move failed. Attempting fallback rename."); if (tempJar.renameTo(finalJar)) System.out.println("[FoxtrotRelocator] SUCCESS: Fallback rename completed."); else System.err.println("[FoxtrotRelocator] FATAL ERROR: Could not move temp jar."); }
        System.out.println("[FoxtrotRelocator] Execution complete. Exiting."); System.exit(0);
    }
}