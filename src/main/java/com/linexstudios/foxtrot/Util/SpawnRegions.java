package com.linexstudios.foxtrot.Util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpawnRegions {

    public enum PitMap {
        ELEMENTS, CASTLE, CORALS, GENESIS, FOUR_SEASONS
    }

    public static class BoundingBox {
        public final int minX, minY, minZ, maxX, maxY, maxZ;

        public BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        }

        public boolean contains(double x, double y, double z) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }
    }

    // ==========================================
    //          GENESIS FACTION REGIONS
    // ==========================================

    public static final List<BoundingBox> GENESIS_ANGEL_SPAWN = Collections.singletonList(
            new BoundingBox(-20, 0, -20, 20, 255, 20)
    );

    public static final List<BoundingBox> GENESIS_DEMON_SPAWN = Arrays.asList(
            new BoundingBox(-160, 0, -160, -20, 255, 0),
            new BoundingBox(-20, 0, -160, 0, 255, -20)
    );

    public static final List<BoundingBox> GENESIS_SPAWN = Collections.singletonList(
            new BoundingBox(-20, 86, -20, 20, 223, 20)
    );

    // ==========================================
    //              OTHER MAP SPAWNS
    // ==========================================

    public static final List<BoundingBox> FOUR_SEASONS_SPAWN = Collections.singletonList(
            new BoundingBox(-23, 92, -23, 23, 255, 23)
    );

    public static final List<BoundingBox> ELEMENTS_SPAWN = Collections.singletonList(
            new BoundingBox(-20, 114, -20, 20, 255, 20)
    );

    public static final List<BoundingBox> CORALS_SPAWN = Collections.singletonList(
            new BoundingBox(-20, 215, -20, 20, 255, 20)
    );

    public static final List<BoundingBox> CASTLE_SPAWN = Arrays.asList(
            new BoundingBox(-300, 0, -300, 35, 100, 35),
            new BoundingBox(-120, 0, -120, 35, 100, 35)
    );

    // ==========================================
    //              CORE LOGIC
    // ==========================================

    public static String getRegionString(EntityPlayer player) {
        if (player == null) return "";

        double x = player.posX;
        double y = player.posY;
        double z = player.posZ;

        // Bypasses the broken math completely. If they are in ANY of these boxes, they are in spawn!
        if (isInside(x, y, z, GENESIS_SPAWN) || isInside(x, y, z, FOUR_SEASONS_SPAWN) || 
            isInside(x, y, z, ELEMENTS_SPAWN) || isInside(x, y, z, CORALS_SPAWN) || 
            isInside(x, y, z, CASTLE_SPAWN)) {
            return EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
        }

        if (isInside(x, y, z, GENESIS_ANGEL_SPAWN)) {
            return EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD + "ANGEL";
        }
        if (isInside(x, y, z, GENESIS_DEMON_SPAWN)) {
            return EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "DEMON";
        }

        return "";
    }

    private static boolean isInside(double x, double y, double z, List<BoundingBox> region) {
        for (BoundingBox box : region) {
            if (box.contains(x, y, z)) return true;
        }
        return false;
    }

    // ==========================================
    //        DYNAMIC DISTANCE / HUD FORMATTER
    // ==========================================

    public static String getLocationFormat(EntityPlayer localPlayer, EntityPlayer targetPlayer) {
        if (targetPlayer == null || localPlayer == null) return "";

        // 1. Check if they are in a defined region
        String region = getRegionString(targetPlayer);
        if (!region.isEmpty()) {
            return region;
        }

        // 2. If out in the map, calculate exact distance
        int distance = (int) localPlayer.getDistanceToEntity(targetPlayer);
        EnumChatFormatting distColor;

        if (distance >= 100) {
            distColor = EnumChatFormatting.GREEN;
        } else if (distance >= 50) {
            distColor = EnumChatFormatting.YELLOW;
        } else if (distance >= 20) {
            distColor = EnumChatFormatting.GOLD;
        } else {
            distColor = EnumChatFormatting.RED;
        }

        return distColor + String.valueOf(distance) + "m";
    }
}