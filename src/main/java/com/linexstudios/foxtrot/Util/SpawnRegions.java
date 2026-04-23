package com.linexstudios.foxtrot.Util;

import com.linexstudios.foxtrot.Handler.MapDetectionHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpawnRegions {
    public static class BoundingBox {
        public final int minX, minY, minZ, maxX, maxY, maxZ;
        public BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = Math.min(minX, maxX); this.minY = Math.min(minY, maxY); this.minZ = Math.min(minZ, maxZ);
            this.maxX = Math.max(minX, maxX); this.maxY = Math.max(minY, maxY); this.maxZ = Math.max(minZ, maxZ);
        }
        public boolean contains(double x, double y, double z) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }
    }

    public static final List<BoundingBox> GENESIS_SPAWN = Collections.singletonList(new BoundingBox(-20, 86, -20, 20, 255, 20));
    public static final List<BoundingBox> GENESIS_ANGEL_SPAWN = Collections.singletonList(new BoundingBox(-80, 59, 63, -61, 79, 80));
    public static final List<BoundingBox> GENESIS_DEMON_SPAWN = Arrays.asList(new BoundingBox(57, 65, -75, 75, 85, -57));
    public static final List<BoundingBox> GENESIS_BADLANDS = Collections.singletonList(new BoundingBox(-140, 0, -140, 0, 255, 0));
    public static final List<BoundingBox> GENESIS_FORTRESS = Collections.singletonList(new BoundingBox(0, 0, -140, 140, 255, 0));
    public static final List<BoundingBox> GENESIS_GARDEN = Collections.singletonList(new BoundingBox(0, 0, 0, 140, 255, 140));
    public static final List<BoundingBox> GENESIS_PALACE = Collections.singletonList(new BoundingBox(-140, 0, 0, 0, 255, 140));

    public static final List<BoundingBox> FOUR_SEASONS_SPAWN = Collections.singletonList(new BoundingBox(-25, 92, -25, 25, 255, 25));
    public static final List<BoundingBox> FOUR_SEASONS_SUMMER = Collections.singletonList(new BoundingBox(-155, 0, -155, 0, 255, 0));
    public static final List<BoundingBox> FOUR_SEASONS_WINTER = Collections.singletonList(new BoundingBox(0, 0, 0, 155, 255, 155));
    public static final List<BoundingBox> FOUR_SEASONS_AUTUMN = Collections.singletonList(new BoundingBox(0, 0, -155, 155, 255, 0));
    public static final List<BoundingBox> FOUR_SEASONS_SPRING = Collections.singletonList(new BoundingBox(-155, 0, 0, 0, 255, 155));

    public static final List<BoundingBox> ELEMENTS_SPAWN = Collections.singletonList(new BoundingBox(-25, 114, -25, 25, 255, 25));
    public static final List<BoundingBox> ELEMENTS_LAVA = Collections.singletonList(new BoundingBox(0, 0, 0, 175, 255, 175));
    public static final List<BoundingBox> ELEMENTS_WATER = Collections.singletonList(new BoundingBox(-175, 0, -175, 0, 255, 0));
    public static final List<BoundingBox> ELEMENTS_SKY = Collections.singletonList(new BoundingBox(-175, 0, 0, 0, 255, 175));
    public static final List<BoundingBox> ELEMENTS_MOUNTAINS = Collections.singletonList(new BoundingBox(0, 0, -175, 175, 255, 0));

    public static final List<BoundingBox> CORALS_SPAWN = Collections.singletonList(new BoundingBox(-25, 113, -25, 25, 255, 25));
    public static final List<BoundingBox> CORALS_SHIPWRECK = Collections.singletonList(new BoundingBox(-140, 0, 0, 0, 255, 140));
    public static final List<BoundingBox> CORALS_SEAWEED = Collections.singletonList(new BoundingBox(-140, 0, -140, 0, 255, 0));
    public static final List<BoundingBox> CORALS_TEMPLE = Collections.singletonList(new BoundingBox(0, 0, -140, 140, 255, 0));
    public static final List<BoundingBox> CORALS_GEYSER = Collections.singletonList(new BoundingBox(0, 0, 0, 140, 255, 140));

    public static final List<BoundingBox> CASTLE_SPAWN = Collections.singletonList(new BoundingBox(-25, 85, -25, 25, 255, 25));
    public static final List<BoundingBox> CASTLE_DOCKS = Collections.singletonList(new BoundingBox(-150, 0, 0, 0, 255, 150));
    public static final List<BoundingBox> CASTLE_FOREST = Collections.singletonList(new BoundingBox(-150, 0, -150, 0, 255, 0));
    public static final List<BoundingBox> CASTLE_CITY = Collections.singletonList(new BoundingBox(0, 0, 0, 150, 255, 150));
    public static final List<BoundingBox> CASTLE_FARM = Collections.singletonList(new BoundingBox(0, 0, -150, 150, 255, 0));

    public static String getRegionString(EntityPlayer player) {
        if (player == null) return "";
        double x = player.posX, y = player.posY, z = player.posZ;

        switch (MapDetectionHandler.currentMap) {
            case FOUR_SEASONS:
                if (y >= 100) return EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN"; 
                if (isInside(x, y, z, FOUR_SEASONS_SPAWN)) return EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
                if (x >= -30 && x <= 24 && z >= -36 && z <= 27) return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "MID"; 
                if (isInside(x, y, z, FOUR_SEASONS_SPRING)) return EnumChatFormatting.LIGHT_PURPLE + "" + EnumChatFormatting.BOLD + "SPRING";
                if (isInside(x, y, z, FOUR_SEASONS_SUMMER)) return EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "SUMMER";
                if (isInside(x, y, z, FOUR_SEASONS_AUTUMN)) return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "AUTUMN";
                if (isInside(x, y, z, FOUR_SEASONS_WINTER)) return EnumChatFormatting.WHITE + "" + EnumChatFormatting.BOLD + "WINTER";
                break;
            case ELEMENTS:
                if (isInside(x, y, z, ELEMENTS_SPAWN)) return EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
                if (x >= -23 && x <= 23 && z >= -23 && z <= 23) return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "MID";
                if (isInside(x, y, z, ELEMENTS_LAVA)) return EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "LAVA";
                if (isInside(x, y, z, ELEMENTS_WATER)) return EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD + "WATER";
                if (isInside(x, y, z, ELEMENTS_SKY)) return EnumChatFormatting.WHITE + "" + EnumChatFormatting.BOLD + "SKY";
                if (isInside(x, y, z, ELEMENTS_MOUNTAINS)) return EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD + "MOUNTAINS";
                break;
            case GENESIS:
                if (isInside(x, y, z, GENESIS_SPAWN)) return EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
                if (isInside(x, y, z, GENESIS_ANGEL_SPAWN)) return EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD + "ANGEL";
                if (isInside(x, y, z, GENESIS_DEMON_SPAWN)) return EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "DEMON";
                if (x >= -20 && x <= 20 && z >= -20 && z <= 20) return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "MID";
                if (isInside(x, y, z, GENESIS_PALACE)) return EnumChatFormatting.WHITE + "" + EnumChatFormatting.BOLD + "PALACE";
                if (isInside(x, y, z, GENESIS_FORTRESS)) return EnumChatFormatting.DARK_RED + "" + EnumChatFormatting.BOLD + "FORTRESS";
                if (isInside(x, y, z, GENESIS_GARDEN)) return EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + "GARDEN";
                if (isInside(x, y, z, GENESIS_BADLANDS)) return EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "BADLANDS";
                break;
            case CASTLE:
                if (isInside(x, y, z, CASTLE_SPAWN)) return EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
                if (x >= -15 && x <= 15 && z >= -15 && z <= 15) return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "MID";
                if (isInside(x, y, z, CASTLE_DOCKS)) return EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "DOCKS";
                if (isInside(x, y, z, CASTLE_CITY)) return EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD + "CITY";
                if (isInside(x, y, z, CASTLE_FARM)) return EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "FARM";
                if (isInside(x, y, z, CASTLE_FOREST)) return EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + "FOREST";
                break;
            case CORALS:
                if (isInside(x, y, z, CORALS_SPAWN)) return EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "SPAWN";
                if (x >= -20 && x <= 20 && z >= -20 && z <= 20) return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "MID";
                if (isInside(x, y, z, CORALS_TEMPLE)) return EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "TEMPLE";
                if (isInside(x, y, z, CORALS_SEAWEED)) return EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + "SEAWEED";
                if (isInside(x, y, z, CORALS_GEYSER)) return EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "GEYSER";
                if (isInside(x, y, z, CORALS_SHIPWRECK)) return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "SHIPWRECK";
                break;
        }
        return EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD + "OUTSKIRTS";
    }

    private static boolean isInside(double x, double y, double z, List<BoundingBox> region) {
        for (BoundingBox box : region) {
            if (box.contains(x, y, z)) return true;
        }
        return false;
    }
}