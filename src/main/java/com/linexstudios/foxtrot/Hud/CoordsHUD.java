package com.linexstudios.foxtrot.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CoordsHUD extends DraggableHUD {
    public static CoordsHUD instance = new CoordsHUD();
    public static boolean enabled = true;
    
    // Independent Color Settings
    public static int axisColor = 0xFF5555;      // Color for X:, Y:, Z:
    public static int numberColor = 0xFFFFFF;    // Color for the values (-180, 50, etc)
    public static int directionColor = 0xFFFFFF; // Color for the Cardinal (N, NE) AND the + / - indicators

    private Minecraft mc = Minecraft.getMinecraft();

    public CoordsHUD() {
        super("Coordinates", 10, 10);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.currentScreen instanceof EditHUDGui) return;
        if (mc.currentScreen instanceof HUDSettingsGui) return;
        render(false, 0, 0); 
    }

    @Override
    public void draw(boolean isEditing) {
        if (!enabled || mc.thePlayer == null) return;

        // --- MATH: Exact Floor Logic ---
        int n = MathHelper.floor_double(mc.thePlayer.posX);
        int n2 = (int) mc.thePlayer.getEntityBoundingBox().minY;
        int n3 = MathHelper.floor_double(mc.thePlayer.posZ);

        // Standard string values (No attached + signs)
        String strX = String.valueOf(n);
        String strY = String.valueOf(n2);
        String strZ = String.valueOf(n3);

        // --- CARDINAL: Exact CheatBreaker 8-Way Logic ---
        String[] directions = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        double d = MathHelper.wrapAngleTo180_double(mc.thePlayer.rotationYaw) + 180.0D;
        d += 11.682692039868188 * 1.925926f;
        d %= 360;
        String cardinal = directions[MathHelper.floor_double(d / 45.0D)];

        // --- DYNAMIC + / - INDICATORS ---
        String signX = "";
        String signZ = "";
        
        if (cardinal.contains("E")) signX = "+";
        else if (cardinal.contains("W")) signX = "-";
        
        if (cardinal.contains("S")) signZ = "+";
        else if (cardinal.contains("N")) signZ = "-";

        if (isHorizontal) {
            // Horizontal format: (X, Y, Z) NW
            String text = String.format("(%s, %s, %s) ", strX, strY, strZ);
            mc.fontRendererObj.drawStringWithShadow(text, 2, 2, numberColor);
            int nextX = 2 + mc.fontRendererObj.getStringWidth(text);
            mc.fontRendererObj.drawStringWithShadow(cardinal, nextX, 2, directionColor);
            
            this.width = nextX + mc.fontRendererObj.getStringWidth(cardinal) + 2;
            this.height = mc.fontRendererObj.FONT_HEIGHT + 4;
        } else {
            // Vertical format with right-aligned cardinal indicators
            int rightCol = 45; // Default distance to the right side
            int maxValWidth = Math.max(mc.fontRendererObj.getStringWidth(strX), mc.fontRendererObj.getStringWidth(strZ));
            if (maxValWidth > 20) rightCol = 25 + maxValWidth; // Prevents overlapping if coordinates hit 10,000+

            // Row 1: X
            mc.fontRendererObj.drawStringWithShadow("X: ", 2, 2, axisColor);
            mc.fontRendererObj.drawStringWithShadow(strX, 2 + mc.fontRendererObj.getStringWidth("X: "), 2, numberColor);
            mc.fontRendererObj.drawStringWithShadow(signX, rightCol, 2, directionColor); // Tied to directionColor

            // Row 2: Y
            mc.fontRendererObj.drawStringWithShadow("Y: ", 2, 12, axisColor);
            mc.fontRendererObj.drawStringWithShadow(strY, 2 + mc.fontRendererObj.getStringWidth("Y: "), 12, numberColor);
            mc.fontRendererObj.drawStringWithShadow(cardinal, rightCol, 12, directionColor); // Tied to directionColor

            // Row 3: Z
            mc.fontRendererObj.drawStringWithShadow("Z: ", 2, 22, axisColor);
            mc.fontRendererObj.drawStringWithShadow(strZ, 2 + mc.fontRendererObj.getStringWidth("Z: "), 22, numberColor);
            mc.fontRendererObj.drawStringWithShadow(signZ, rightCol, 22, directionColor); // Tied to directionColor
            
            this.width = rightCol + mc.fontRendererObj.getStringWidth(cardinal) + 4;
            this.height = 34;
        }
    }
}