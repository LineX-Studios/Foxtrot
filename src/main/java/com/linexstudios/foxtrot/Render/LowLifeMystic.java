package com.linexstudios.foxtrot.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class LowLifeMystic {
    
    public static final LowLifeMystic instance = new LowLifeMystic();
    private final Minecraft mc = Minecraft.getMinecraft();
    
    public static boolean enabled = true; 

    // Called by MixinRenderItem
    public void checkAndDraw(ItemStack item, int x, int y) {
        if (isTrueMystic(item)) {
            int currentLives = getMysticLives(item);
            
            // If it has 5 or fewer lives draw corner markers
            if (currentLives != -1 && currentLives <= 5) {
                drawDangerBorder(x, y);
            }
        }
    }

    private boolean isTrueMystic(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return false;
        
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt.hasKey("ExtraAttributes", 10)) { 
            NBTTagCompound extra = nbt.getCompoundTag("ExtraAttributes");
            return extra.hasKey("Nonce") || extra.hasKey("CustomEnchants");
        }
        return false;
    }

    private int getMysticLives(ItemStack stack) {
        try {
            List<String> tooltip = stack.getTooltip(mc.thePlayer, false);
            for (String line : tooltip) {
                String rawLine = EnumChatFormatting.getTextWithoutFormattingCodes(line);
                if (rawLine != null && rawLine.contains("Lives: ")) {
                    try {
                        String livesStr = rawLine.substring(rawLine.indexOf("Lives: ") + 7);
                        if (livesStr.contains("/")) {
                            String currentLivesStr = livesStr.split("/")[0].trim();
                            return Integer.parseInt(currentLivesStr);
                        }
                    } catch (Exception e) { }
                }
            }
        } catch (Exception e) { }
        return -1;
    }

    private void drawDangerBorder(int x, int y) {
        float alpha = 0.6F + (float)(Math.sin(System.currentTimeMillis() / 200.0) * 0.3F);

        // --- PREPARE 2D GL STATE ---
        RenderUtils.setup2D();
        
        GlStateManager.disableLighting();
        GlStateManager.disableDepth(); 
        
        // 1. Faint solid red inner box
        RenderUtils.drawRect(x, y, 16, 16, 1.0F, 0.0F, 0.0F, 0.25F);

        // 2. Pulse corner markers (t = thickness, l = length)
        float t = 2.0F;
        float l = 5.0F;
        
        // Top-Left
        RenderUtils.drawRect(x, y, l, t, 1.0F, 0.0F, 0.0F, alpha);             // Horizontal
        RenderUtils.drawRect(x, y + t, t, l - t, 1.0F, 0.0F, 0.0F, alpha);     // Vertical
        
        // Top-Right
        RenderUtils.drawRect(x + 16 - l, y, l, t, 1.0F, 0.0F, 0.0F, alpha);
        RenderUtils.drawRect(x + 16 - t, y + t, t, l - t, 1.0F, 0.0F, 0.0F, alpha);
        
        // Bottom-Left
        RenderUtils.drawRect(x, y + 16 - t, l, t, 1.0F, 0.0F, 0.0F, alpha);
        RenderUtils.drawRect(x, y + 16 - l, t, l - t, 1.0F, 0.0F, 0.0F, alpha);
        
        // Bottom-Right
        RenderUtils.drawRect(x + 16 - l, y + 16 - t, l, t, 1.0F, 0.0F, 0.0F, alpha);
        RenderUtils.drawRect(x + 16 - t, y + 16 - l, t, l - t, 1.0F, 0.0F, 0.0F, alpha);

        // Re-enable before cleaning up the 2D state
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        
        // --- RESTORE GL STATE ---
        RenderUtils.end2D();
    }
}