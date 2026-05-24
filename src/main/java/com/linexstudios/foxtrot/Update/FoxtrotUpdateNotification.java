package com.linexstudios.foxtrot.Update;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import com.linexstudios.foxtrot.Render.RenderUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lwjgl.opengl.GL11;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

public class FoxtrotUpdateNotification extends GuiScreen {
    private String targetVersion;
    private Runnable onAction;
    private long startTime;
    private final int DURATION = 10000;
    private boolean isRestartMode;
    private GuiScreen previousScreen;

    public FoxtrotUpdateNotification(String targetVersion, boolean isRestartMode, GuiScreen previousScreen, Runnable onAction) {
        this.targetVersion = targetVersion;
        this.isRestartMode = isRestartMode;
        this.previousScreen = previousScreen;
        this.onAction = onAction;
        this.startTime = System.currentTimeMillis();
    }

    public static void runIndependentCheck() {
        new Thread(() -> {
            boolean hasShown = false;
            
            while (true) {
                try {
                    if (!hasShown) {
                        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.github.com/repos/LineX-Studios/FOXTROT-PIT/releases/latest").openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("User-Agent", "Foxtrot-Updater");
                        
                        if (conn.getResponseCode() == 200) {
                            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                            JsonObject response = new JsonParser().parse(reader).getAsJsonObject();
                            reader.close();
                            
                            String latestVersion = response.get("tag_name").getAsString();
                            String downloadUrl = response.getAsJsonArray("assets").get(0).getAsJsonObject().get("browser_download_url").getAsString();

                            if (isNewerVersion(FoxtrotTweaker.CURRENT_VERSION.replace("v", ""), latestVersion.replace("v", ""))) {
                                FoxtrotTweaker.LATEST_VERSION = latestVersion;
                                FoxtrotTweaker.DOWNLOAD_URL = downloadUrl;
                                FoxtrotTweaker.UPDATE_AVAILABLE = true;

                                while (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().currentScreen == null) {
                                    Thread.sleep(1000);
                                }

                                Thread.sleep(2500);

                                Minecraft.getMinecraft().addScheduledTask(() -> {
                                    GuiScreen current = Minecraft.getMinecraft().currentScreen;
                                    if (!(current instanceof FoxtrotUpdateNotification)) {
                                        Minecraft.getMinecraft().displayGuiScreen(new FoxtrotUpdateNotification(latestVersion, false, current, () -> {
                                            FoxtrotManualUpdater.triggerManualUpdate();
                                        }));
                                    }
                                });
                                
                                hasShown = true;
                            }
                        }
                    }
                    Thread.sleep(60000);
                } catch (Exception e) {
                    try { Thread.sleep(60000); } catch (Exception ignored) {}
                }
            }
        }, "Foxtrot-Independent-API-Check").start();
    }

    private static boolean isNewerVersion(String current, String latest) {
        try {
            String[] cParts = current.replaceAll("[^0-9.]", "").split("\\."); 
            String[] lParts = latest.replaceAll("[^0-9.]", "").split("\\.");
            for (int i = 0; i < Math.max(cParts.length, lParts.length); i++) {
                int c = i < cParts.length && !cParts[i].isEmpty() ? Integer.parseInt(cParts[i]) : 0;
                int l = i < lParts.length && !lParts[i].isEmpty() ? Integer.parseInt(lParts[i]) : 0;
                if (l > c) return true; if (l < c) return false;
            }
        } catch (Exception e) {} return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.previousScreen != null) {
            this.previousScreen.drawScreen(mouseX, mouseY, partialTicks);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        
        if (elapsed >= DURATION) {
            mc.displayGuiScreen(previousScreen);
            if (isRestartMode && onAction != null) onAction.run(); 
            return;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        int width = 230;
        int height = 70;
        
        int x = sr.getScaledWidth() - width - 10;
        int y = sr.getScaledHeight() - height - 10;

        RenderUtils.drawRoundedRect(x, y, width, height, 5, 0xEE1A1A1A);
        RenderUtils.drawRoundedOutline(x, y, width, height, 5, 1.0f, 0x55FFFFFF);

        String titleColor = isRestartMode ? EnumChatFormatting.GREEN.toString() : EnumChatFormatting.AQUA.toString();
        String titleText = titleColor + EnumChatFormatting.BOLD.toString() + (isRestartMode ? "Update Ready!" : "Update Available!");
        mc.fontRendererObj.drawStringWithShadow(titleText, x + 12, y + 10, -1);
        
        int xBtnX = x + width - 20;
        int xBtnY = y + 8;
        boolean hoverX = mouseX >= xBtnX && mouseX <= xBtnX + 10 && mouseY >= xBtnY && mouseY <= xBtnY + 10;
        drawCross(x + width - 15, y + 13, 3.5f, 1.5f, hoverX ? 0xFFFF5555 : 0xFFAAAAAA);

        if (isRestartMode) {
            int secondsLeft = (int) Math.ceil((DURATION - elapsed) / 1000.0);
            mc.fontRendererObj.drawStringWithShadow("Restarting in " + secondsLeft + "s...", x + 12, y + 26, 0xBBBBBB);
        } else {
            mc.fontRendererObj.drawStringWithShadow("Version " + targetVersion + " is ready.", x + 12, y + 26, 0xBBBBBB);
        }

        int barHeight = 4;
        int paddingX = 6;
        int paddingY = 5;
        float progress = 1.0f - ((float) elapsed / DURATION);
        int maxBarWidth = width - (paddingX * 2);
        int barWidth = (int) (maxBarWidth * progress);
        
        if (barWidth > 2) {
            RenderUtils.drawRoundedRect(x + paddingX, y + height - barHeight - paddingY, barWidth, barHeight, 2.0f, 0xFFFF3333);
        }

        String btnText = isRestartMode ? "RESTART" : "UPDATE NOW";
        int btnW = isRestartMode ? 85 : 95;
        int btnH = 16;
        int btnX = x + width - btnW - 12;
        int btnY = y + 44;
        boolean hoverBtn = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        
        RenderUtils.drawRoundedRect(btnX, btnY, btnW, btnH, 3, hoverBtn ? 0xFF404040 : 0xFF2A2A2A);
        RenderUtils.drawRoundedOutline(btnX, btnY, btnW, btnH, 3, 1.0f, hoverBtn ? 0xFF666666 : 0xFF444444);
        mc.fontRendererObj.drawStringWithShadow(btnText, btnX + (btnW - mc.fontRendererObj.getStringWidth(btnText)) / 2.0f, btnY + 4, hoverBtn ? 0xFFFFFFFF : 0xFFDDDDDD);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) {
            if (this.previousScreen != null) {
                try {
                    java.lang.reflect.Method m = GuiScreen.class.getDeclaredMethod("mouseClicked", int.class, int.class, int.class);
                    m.setAccessible(true);
                    m.invoke(this.previousScreen, mouseX, mouseY, mouseButton);
                } catch (Exception ignored) {}
            }
            return;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        int width = 230;
        int height = 70;
        int x = sr.getScaledWidth() - width - 10;
        int y = sr.getScaledHeight() - height - 10;

        int xBtnX = x + width - 20;
        int xBtnY = y + 8;
        if (mouseX >= xBtnX && mouseX <= xBtnX + 10 && mouseY >= xBtnY && mouseY <= xBtnY + 10) {
            mc.displayGuiScreen(previousScreen); 
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            return;
        }

        int btnW = isRestartMode ? 85 : 95;
        int btnH = 16;
        int btnX = x + width - btnW - 12;
        int btnY = y + 44;
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            mc.displayGuiScreen(previousScreen);
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            if (onAction != null) onAction.run();
            return;
        }

        if (this.previousScreen != null) {
            try {
                java.lang.reflect.Method m = GuiScreen.class.getDeclaredMethod("mouseClicked", int.class, int.class, int.class);
                m.setAccessible(true);
                m.invoke(this.previousScreen, mouseX, mouseY, mouseButton);
            } catch (Exception ignored) {}
        }
    }

    private void drawCross(float cX, float cY, float s, float t, int c) {
        RenderUtils.setupSmoothRender(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        RenderUtils.setColor(c);
        GL11.glLineWidth(t);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(cX - s, cY - s);
        GL11.glVertex2f(cX + s, cY + s);
        GL11.glVertex2f(cX + s, cY - s);
        GL11.glVertex2f(cX - s, cY + s);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderUtils.endSmoothRender();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}