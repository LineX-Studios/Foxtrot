package com.linexstudios.foxtrot.Handler;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CapeManager {
    public static List<String> availableCapes = new ArrayList<>();

    public static void loadEmbeddedCapes() {
        availableCapes.clear();
        try {
            URL url = CapeManager.class.getResource("/assets/foxtrot/capes/");
            if (url != null) {
                if (url.getProtocol().equals("jar")) {
                    String jarPath = url.getPath().substring(5, url.getPath().indexOf("!")); 
                    try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(java.net.URLDecoder.decode(jarPath, "UTF-8"))) {
                        java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            java.util.zip.ZipEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.startsWith("assets/foxtrot/capes/") && name.endsWith(".png")) {
                                String capeName = name.substring("assets/foxtrot/capes/".length(), name.length() - 4);
                                if (!capeName.isEmpty() && !availableCapes.contains(capeName)) {
                                    availableCapes.add(capeName);
                                }
                            }
                        }
                    }
                } else if (url.getProtocol().equals("file")) {
                    File folder = new File(url.toURI());
                    File[] files = folder.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().endsWith(".png")) {
                                availableCapes.add(f.getName().replace(".png", ""));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Foxtrot: Failed to load embedded capes");
            e.printStackTrace();
        }
        
        // Fallback safety
        if (availableCapes.isEmpty()) {
            availableCapes.add("fx_white");
            availableCapes.add("fx_original");
        }
    }
}
