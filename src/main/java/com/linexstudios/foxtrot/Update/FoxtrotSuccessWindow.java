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
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;

public class FoxtrotSuccessWindow extends JFrame {
    private Image logoImage;
    private String targetVersion;
    private Font textFontLarge, textFontSmall; 
    private Rectangle okButtonBounds;
    private boolean isHoveringOK = false;

    public FoxtrotSuccessWindow(String targetVersion, Runnable onConfirm) {
        this.targetVersion = targetVersion;
        setUndecorated(true); setSize(500, 300); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); setAlwaysOnTop(true);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
        try { InputStream is = getClass().getResourceAsStream("/foxtrot/textures/logo.png"); if (is != null) logoImage = ImageIO.read(is); } catch (Exception e) {}
        try {
            InputStream regularStream = getClass().getResourceAsStream("/foxtrot/fonts/Roboto-Regular.ttf"), thinStream = getClass().getResourceAsStream("/foxtrot/fonts/Roboto-Thin.ttf");
            if (regularStream != null && thinStream != null) {
                Font regularFont = Font.createFont(Font.TRUETYPE_FONT, regularStream), thinFont = Font.createFont(Font.TRUETYPE_FONT, thinStream);
                textFontSmall = regularFont.deriveFont(Font.PLAIN, 15f); textFontLarge = thinFont.deriveFont(Font.PLAIN, 24f); 
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); ge.registerFont(regularFont); ge.registerFont(thinFont);
            } else { textFontLarge = new Font(Font.SANS_SERIF, Font.PLAIN, 24); textFontSmall = new Font(Font.SANS_SERIF, Font.PLAIN, 15); }
        } catch (Exception e) { textFontLarge = new Font(Font.SANS_SERIF, Font.PLAIN, 24); textFontSmall = new Font(Font.SANS_SERIF, Font.PLAIN, 15); }
        okButtonBounds = new Rectangle((getWidth() - 140) / 2, 220, 140, 35);
        setContentPane(new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(20, 20, 20)); g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (logoImage != null) { int logoW = 200, logoH = (logoImage.getHeight(null) * logoW) / logoImage.getWidth(null); g2d.drawImage(logoImage, (getWidth() - logoW) / 2, 30, logoW, logoH, null); }
                g2d.setColor(new Color(230, 40, 40)); g2d.setFont(textFontLarge); g2d.drawString("UPDATE COMPLETE", (getWidth() - g2d.getFontMetrics().stringWidth("UPDATE COMPLETE")) / 2, 120);
                g2d.setColor(Color.WHITE); g2d.setFont(textFontSmall); 
                String l1 = "Foxtrot v" + targetVersion + " has been successfully downloaded.", l2 = "Minecraft will now close to safely apply the update.", l3 = "Please relaunch your game!";
                g2d.drawString(l1, (getWidth() - g2d.getFontMetrics().stringWidth(l1)) / 2, 155); g2d.drawString(l2, (getWidth() - g2d.getFontMetrics().stringWidth(l2)) / 2, 175); g2d.drawString(l3, (getWidth() - g2d.getFontMetrics().stringWidth(l3)) / 2, 195);
                g2d.setColor(isHoveringOK ? new Color(255, 60, 60) : new Color(230, 40, 40)); g2d.fillRoundRect(okButtonBounds.x, okButtonBounds.y, okButtonBounds.width, okButtonBounds.height, 10, 10);
                g2d.setColor(Color.WHITE); g2d.setFont(textFontSmall.deriveFont(Font.BOLD, 14f)); g2d.drawString("CLOSE GAME", okButtonBounds.x + (okButtonBounds.width - g2d.getFontMetrics().stringWidth("CLOSE GAME")) / 2, okButtonBounds.y + 22);
                g2d.setColor(new Color(100, 100, 100)); g2d.setFont(textFontSmall.deriveFont(11f)); g2d.drawString((char) 169 + " 2026 LineX Studios. All Rights Reserved.", 10, getHeight() - 10);
            }
        });
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { boolean wasHovering = isHoveringOK; isHoveringOK = okButtonBounds.contains(e.getPoint()); if (wasHovering != isHoveringOK) repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if (okButtonBounds.contains(e.getPoint())) { dispose(); if (onConfirm != null) onConfirm.run(); Runtime.getRuntime().halt(0); } }
        };
        getContentPane().addMouseListener(mouseAdapter); getContentPane().addMouseMotionListener(mouseAdapter);
    }
}