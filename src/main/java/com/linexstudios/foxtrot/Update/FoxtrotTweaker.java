/*
 * ==============================================================================
 * Foxtrot-PIT - Open Source FoxtrotTweaker
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

import com.google.gson.*;
import net.minecraft.launchwrapper.*;
import javax.swing.SwingUtilities;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.List;

public class FoxtrotTweaker implements ITweaker {
    public static final String CURRENT_VERSION = "${version}";
    public static boolean UPDATE_AVAILABLE = false;
    public static String LATEST_VERSION = "", DOWNLOAD_URL = "";
    public static boolean DEV_TEST_MODE = false, isChecking = false;
    public static File foxtrotDir;

    /**
     * SAFE JAR RESOLVER:
     * Fixes the "URI is not hierarchical" crash by safely stripping Forge's "jar:file:" wrappers
     */
    public static File getCurrentJar() {
        try {
            String path = FoxtrotTweaker.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "UTF-8");
            if (path.startsWith("file:")) path = path.substring(5);
            if (path.contains("!")) path = path.substring(0, path.indexOf("!"));
            return new File(path);
        } catch (Exception e) {
            return null;
        }
    }
                                    // ================================================
    @SuppressWarnings("unchecked") // mixintweaker DO NOT REMOVE!!!!!!!!!!!!!!!!!!!!  =
    @Override                      // =================================================
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        List<String> tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");
        if (tweakClasses != null && !tweakClasses.contains("org.spongepowered.asm.launch.MixinTweaker")) {
            tweakClasses.add("org.spongepowered.asm.launch.MixinTweaker");
        }
        
        foxtrotDir = new File(gameDir, "Foxtrot_Updates");
        if (!foxtrotDir.exists()) foxtrotDir.mkdirs();
        
        try { File installerJar = new File(foxtrotDir, "updater.jar"); if (installerJar.exists()) installerJar.delete(); } catch (Exception e) {}
        try {
            File currentJar = getCurrentJar();
            if (currentJar != null) {
                File actualModsDir = currentJar.getParentFile(); 
                if (actualModsDir.exists() && actualModsDir.isDirectory()) {
                    File[] oldFiles = actualModsDir.listFiles((dir, name) -> name.endsWith(".old"));
                    if (oldFiles != null) for (File old : oldFiles) old.delete();
                    File[] duplicateJars = actualModsDir.listFiles((dir, name) -> name.toLowerCase().startsWith("foxtrot-") && name.endsWith(".jar") && !name.equals(currentJar.getName()));
                    if (duplicateJars != null) for (File dup : duplicateJars) try { if (!dup.delete()) dup.renameTo(new File(dup.getAbsolutePath() + ".old")); } catch (Exception e) {}
                }
            }
        } catch (Exception e) {}
    }

    public static void checkUpdatesAsync() {
        if (isChecking) return;
        isChecking = true; LATEST_VERSION = "Checking...";
        new Thread(() -> {
            if (DEV_TEST_MODE) { UPDATE_AVAILABLE = true; LATEST_VERSION = "v1.0.0-TEST"; isChecking = false; return; }
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL("https://api.github.com/repos/LineX-Studios/FOXTROT-PIT/releases/latest").openConnection();
                conn.setRequestMethod("GET"); conn.setRequestProperty("User-Agent", "Foxtrot-Updater"); 
                if (conn.getResponseCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                    JsonObject response = new JsonParser().parse(reader).getAsJsonObject(); reader.close();
                    LATEST_VERSION = response.get("tag_name").getAsString();
                    if (isNewerVersion(CURRENT_VERSION.replace("v", ""), LATEST_VERSION.replace("v", ""))) {
                        UPDATE_AVAILABLE = true; DOWNLOAD_URL = response.getAsJsonArray("assets").get(0).getAsJsonObject().get("browser_download_url").getAsString();
                        boolean auto = true;
                        try {
                            File file = new File(foxtrotDir, "update_settings.txt");
                            if (file.exists()) { BufferedReader br = new BufferedReader(new FileReader(file)); String line; while ((line = br.readLine()) != null) if (line.replace(" ", "").contains("autoUpdateEnabled=false")) auto = false; br.close(); }
                        } catch (Exception e) {}
                        
                        // Triggers the download if Auto-Updates are enabled!
                        if (auto) runRealDownload(DOWNLOAD_URL);
                    }
                } else LATEST_VERSION = "API Limit";
            } catch (Throwable t) { LATEST_VERSION = "Failed"; }
            isChecking = false;
        }).start();
    }

    private static boolean isNewerVersion(String current, String latest) {
        try {
            String[] cParts = current.replaceAll("[^0-9.]", "").split("\\."); String[] lParts = latest.replaceAll("[^0-9.]", "").split("\\.");
            for (int i = 0; i < Math.max(cParts.length, lParts.length); i++) {
                int c = i < cParts.length && !cParts[i].isEmpty() ? Integer.parseInt(cParts[i]) : 0;
                int l = i < lParts.length && !lParts[i].isEmpty() ? Integer.parseInt(lParts[i]) : 0;
                if (l > c) return true; if (l < c) return false;
            }
        } catch (Exception e) {} return false;
    }

    public static void runRealDownload(String downloadUrl) {
        FoxtrotUpdateWindow window = new FoxtrotUpdateWindow(LATEST_VERSION);
        SwingUtilities.invokeLater(() -> window.setVisible(true));
        try {
            File tempJar = new File(foxtrotDir, "update_temp.jar"), updaterJar = new File(foxtrotDir, "updater.jar");
            HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection(); conn.setRequestProperty("User-Agent", "Foxtrot-Updater");
            int totalBytes = conn.getContentLength();
            try (InputStream in = new BufferedInputStream(conn.getInputStream()); OutputStream out = new BufferedOutputStream(new FileOutputStream(tempJar))) {
                byte[] buffer = new byte[8192]; int bytesRead = 0, nRead;
                while ((nRead = in.read(buffer)) != -1) { out.write(buffer, 0, nRead); bytesRead += nRead; window.setProgress((int) (((double) bytesRead / totalBytes) * 100)); }
            }
            Thread.sleep(500); 
            File currentJar = getCurrentJar();
            if (currentJar == null || !currentJar.exists()) {
                SwingUtilities.invokeLater(window::dispose);
                return;
            }
            File destinationInMods = new File(currentJar.getParentFile(), "Foxtrot-" + LATEST_VERSION.replace("v", "") + ".jar");
            Files.copy(currentJar.toPath(), updaterJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + (System.getProperty("os.name").toLowerCase().contains("win") ? "javaw.exe" : "java");
            ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", updaterJar.getAbsolutePath(), "com.linexstudios.foxtrot.Update.FoxtrotRelocator", currentJar.getAbsolutePath(), tempJar.getAbsolutePath(), destinationInMods.getAbsolutePath());
            pb.redirectError(new File(foxtrotDir, "updater_error.log")); pb.redirectOutput(new File(foxtrotDir, "updater_output.log")); pb.start();
            SwingUtilities.invokeLater(() -> { window.dispose(); new FoxtrotSuccessWindow(LATEST_VERSION, null).setVisible(true); });
            new Thread(() -> { try { Thread.sleep(3000); } catch (Exception e) {} Runtime.getRuntime().halt(0); }).start();
        } catch (Exception e) { SwingUtilities.invokeLater(window::dispose); }
    }

    public static void triggerManualUpdate() { new Thread(() -> runRealDownload(DOWNLOAD_URL)).start(); }

    @Override public void injectIntoClassLoader(LaunchClassLoader classLoader) {}
    @Override public String getLaunchTarget() { return "net.minecraft.client.main.Main"; }
    @Override public String[] getLaunchArguments() { return new String[0]; }
}