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

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FoxtrotAutoUpdater {

    public static void runAutoUpdate(String downloadUrl, String latestVersion, File currentJar) {
        if (downloadUrl == null || downloadUrl.isEmpty()) return;

        // Spawn the Progress Bar UI
        FoxtrotUpdateWindow window = new FoxtrotUpdateWindow(latestVersion);
        window.setVisible(true);

        try {
            long ts = System.currentTimeMillis();
            File tempJar = new File(FoxtrotTweaker.foxtrotDir, "update_temp_" + ts + ".jar");
            File updaterJar = new File(FoxtrotTweaker.foxtrotDir, "updater_" + ts + ".jar");

            HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
            conn.setRequestProperty("User-Agent", "Foxtrot-Updater");
            int fileSize = conn.getContentLength();

            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(tempJar))) {
                
                byte[] buffer = new byte[8192];
                int nRead;
                long totalRead = 0;
                
                while ((nRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, nRead);
                    totalRead += nRead;
                    if (fileSize > 0) {
                        int progress = (int) ((totalRead * 100) / fileSize);
                        // Fixed: Calls the new setProgress method
                        SwingUtilities.invokeLater(() -> window.setProgress(progress));
                    }
                }
            }

            Thread.sleep(500);

            // Safety catch for IDE environments
            if (currentJar.isDirectory() || FoxtrotTweaker.DEV_TEST_MODE) {
                SwingUtilities.invokeLater(window::dispose);
                return;
            }

            File destinationInMods = new File(currentJar.getParentFile(), "Foxtrot-" + latestVersion.replace("v", "") + ".jar");
            Files.copy(currentJar.toPath(), updaterJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + (System.getProperty("os.name").toLowerCase().contains("win") ? "javaw.exe" : "java");
            
            // Execute the Relocator background process
            ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", updaterJar.getAbsolutePath(), "com.linexstudios.foxtrot.Update.FoxtrotRelocator", currentJar.getAbsolutePath(), tempJar.getAbsolutePath(), destinationInMods.getAbsolutePath());
            pb.redirectError(new File(FoxtrotTweaker.foxtrotDir, "updater_error.log"));
            pb.redirectOutput(new File(FoxtrotTweaker.foxtrotDir, "updater_output.log"));
            pb.start();

            // Display the Success Window
            SwingUtilities.invokeLater(() -> {
                window.dispose();
                new FoxtrotSuccessWindow(latestVersion, null).setVisible(true);
            });

            // Halt the JVM to allow the Relocator to replace the locked JAR file
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (Exception e) {}
                Runtime.getRuntime().halt(0);
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(window::dispose);
        }
    }
}