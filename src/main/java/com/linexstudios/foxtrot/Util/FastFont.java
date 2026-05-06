package com.linexstudios.foxtrot.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import java.util.LinkedHashMap;
import java.util.Map;

public class FastFont {
    private static final FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
    private static final int MAX_CACHE_SIZE = 1000;
    
    // LRU Cache for string widths
    private static final Map<String, Integer> widthCache = new LinkedHashMap<String, Integer>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    /**
     * Gets the width of a string with high-performance caching.
     * Prevents expensive character iteration and glyph lookups in dense HUDs.
     */
    public static int getWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        
        Integer cached = widthCache.get(text);
        if (cached != null) return cached;
        
        int width = fr.getStringWidth(text);
        widthCache.put(text, width);
        return width;
    }

    public static void clear() {
        widthCache.clear();
    }
}
