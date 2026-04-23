/*
 * ==============================================================================
 * Foxtrot-PIT - Open Source Bootstrapper & Manual-Updater
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import javax.swing.SwingUtilities;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class FoxtrotManualUpdater {
    public static volatile boolean isManualUpdateRunning = false;

    public static boolean triggerManualUpdate() {
        if (isManualUpdateRunning) return false;
        if (FoxtrotTweaker.DOWNLOAD_URL == null || FoxtrotTweaker.DOWNLOAD_URL.trim().isEmpty()) return false;
        isManualUpdateRunning = true;
        new Thread(() -> runManualUpdate(FoxtrotTweaker.DOWNLOAD_URL)).start();
        return true;
    }

    public static void runManualUpdate(String downloadUrl) {
        FoxtrotUpdateWindow window = new FoxtrotUpdateWindow(FoxtrotTweaker.LATEST_VERSION);
        SwingUtilities.invokeLater(() -> window.setVisible(true));
        
        try {
            long ts = System.currentTimeMillis();
            File tempJar = new File(FoxtrotTweaker.foxtrotDir, "update_temp_" + ts + ".jar");
            File updaterJar = new File(FoxtrotTweaker.foxtrotDir, "updater_" + ts + ".jar");
            
            HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection(); 
            conn.setRequestProperty("User-Agent", "Foxtrot-Updater");
            int totalBytes = conn.getContentLength();
            
            try (InputStream in = new BufferedInputStream(conn.getInputStream()); OutputStream out = new BufferedOutputStream(new FileOutputStream(tempJar))) {
                byte[] buffer = new byte[8192]; int bytesRead = 0, nRead;
                while ((nRead = in.read(buffer)) != -1) { out.write(buffer, 0, nRead); bytesRead += nRead; window.setProgress((int) (((double) bytesRead / totalBytes) * 100)); }
            }
            Thread.sleep(500); 
            
           File currentJar = FoxtrotTweaker.getCurrentJar();
            
            if (currentJar.isDirectory() || FoxtrotTweaker.DEV_TEST_MODE) {
                SwingUtilities.invokeLater(window::dispose);
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    GuiScreen current = Minecraft.getMinecraft().currentScreen;
                    Minecraft.getMinecraft().displayGuiScreen(new FoxtrotUpdateNotification(FoxtrotTweaker.LATEST_VERSION, true, current, () -> gracefulShutdown()));
                });
                return;
            }

            File destinationInMods = new File(currentJar.getParentFile(), "Foxtrot-" + FoxtrotTweaker.LATEST_VERSION.replace("v", "") + ".jar");
            Files.copy(currentJar.toPath(), updaterJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + (System.getProperty("os.name").toLowerCase().contains("win") ? "javaw.exe" : "java");
            ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", updaterJar.getAbsolutePath(), "com.linexstudios.foxtrot.Update.FoxtrotRelocator", currentJar.getAbsolutePath(), tempJar.getAbsolutePath(), destinationInMods.getAbsolutePath(), new File(FoxtrotTweaker.foxtrotDir, "update_debug.log").getAbsolutePath());
            pb.redirectError(new File(FoxtrotTweaker.foxtrotDir, "updater_error.log")); pb.redirectOutput(new File(FoxtrotTweaker.foxtrotDir, "updater_output.log")); pb.start();
            
            SwingUtilities.invokeLater(window::dispose);
            Minecraft.getMinecraft().addScheduledTask(() -> {
                GuiScreen current = Minecraft.getMinecraft().currentScreen;
                Minecraft.getMinecraft().displayGuiScreen(new FoxtrotUpdateNotification(FoxtrotTweaker.LATEST_VERSION, true, current, () -> gracefulShutdown()));
            });

        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(window::dispose);
        } finally {
            isManualUpdateRunning = false;
        }
    }

    private static void gracefulShutdown() {
        try {
            Minecraft.getMinecraft().shutdown();
        } catch (Throwable t) {}
    }
}