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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;

public class FoxtrotUpdateWindow extends JFrame {
    private int progress = 0; private String dotAnimation = ""; private Image logoImage; private String targetVersion;
    private Font textFontLarge, textFontSmall;

    public FoxtrotUpdateWindow(String targetVersion) {
        this.targetVersion = targetVersion; setUndecorated(true); setSize(500, 300); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); setAlwaysOnTop(true);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
        try { InputStream is = getClass().getResourceAsStream("/foxtrot/textures/logo.png"); if (is != null) logoImage = ImageIO.read(is); } catch (Exception e) {}
        try {
            InputStream regularStream = getClass().getResourceAsStream("/foxtrot/fonts/Roboto-Regular.ttf"), thinStream = getClass().getResourceAsStream("/foxtrot/fonts/Roboto-Thin.ttf");
            if (regularStream != null && thinStream != null) {
                Font regularFont = Font.createFont(Font.TRUETYPE_FONT, regularStream), thinFont = Font.createFont(Font.TRUETYPE_FONT, thinStream);
                textFontSmall = regularFont.deriveFont(Font.PLAIN, 14f); textFontLarge = thinFont.deriveFont(Font.PLAIN, 22f); 
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); ge.registerFont(regularFont); ge.registerFont(thinFont);
            } else { textFontLarge = new Font(Font.SANS_SERIF, Font.PLAIN, 22); textFontSmall = new Font(Font.SANS_SERIF, Font.PLAIN, 14); }
        } catch (Exception e) { textFontLarge = new Font(Font.SANS_SERIF, Font.PLAIN, 22); textFontSmall = new Font(Font.SANS_SERIF, Font.PLAIN, 14); }
        Timer timer = new Timer(500, e -> { if (dotAnimation.length() >= 3) dotAnimation = ""; else dotAnimation += "."; repaint(); }); timer.start();
        setContentPane(new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(20, 20, 20)); g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (logoImage != null) { int logoW = 200, logoH = (logoImage.getHeight(null) * logoW) / logoImage.getWidth(null); g2d.drawImage(logoImage, (getWidth() - logoW) / 2, 40, logoW, logoH, null); }
                int barW = 300, barH = 25, barX = (getWidth() - barW) / 2, barY = 150;
                g2d.setColor(new Color(40, 40, 40)); g2d.fillRoundRect(barX, barY, barW, barH, 10, 10);
                g2d.setColor(new Color(230, 40, 40)); g2d.fillRoundRect(barX, barY, (int) (barW * (progress / 100.0)), barH, 10, 10);
                g2d.setColor(Color.WHITE); g2d.setFont(textFontSmall); String pct = progress + "%"; g2d.drawString(pct, barX + (barW - g2d.getFontMetrics().stringWidth(pct)) / 2, barY + 17);
                g2d.setColor(new Color(230, 40, 40)); g2d.setFont(textFontLarge); g2d.drawString("UPDATING" + dotAnimation, (getWidth() - g2d.getFontMetrics().stringWidth("UPDATING...")) / 2, barY + 60);
                g2d.setColor(new Color(150, 150, 150)); g2d.setFont(textFontSmall); g2d.drawString("Updating to " + targetVersion, (getWidth() - g2d.getFontMetrics().stringWidth("Updating to v" + targetVersion)) / 2, barY + 85);
                g2d.setColor(new Color(160, 160, 160)); g2d.setFont(textFontSmall.deriveFont(11f)); g2d.drawString((char) 169 + " 2026 LineX Studios. All Rights Reserved.", 10, getHeight() - 10);
            }
        });
    }
    public void setProgress(int progress) { this.progress = progress; repaint(); }
}