package com.linexstudios.foxtrot.Misc;

import com.linexstudios.foxtrot.Render.RenderUtils;
import com.linexstudios.foxtrot.Util.SpawnRegions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemBlock;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RingHelper {

    public static final RingHelper instance = new RingHelper();
    private final Minecraft mc = Minecraft.getMinecraft();

    public static boolean enabled = false;
    public static boolean preventMisplace = true;
    public static boolean renderRing = true;

    private static final Map<SpawnRegions.PitMap, List<BlockPos>> RINGS_BY_MAP = new EnumMap<>(SpawnRegions.PitMap.class);
    
    // Master list containing ALL ring coordinates for flawless blocking regardless of map detection
    private static final List<BlockPos> MASTER_RING_LIST = new ArrayList<>();

    public List<BlockPos> activeRingCache = Collections.emptyList();
    private long lastMapCheck = 0;
    private WorldClient lastDetectedWorld = null;
    private SpawnRegions.PitMap activeMap = null;

    static {
        // ====================
        //      ELEMENTS MAP (Y: 72)
        // ====================
        int[][] elementsCoords = {
            {6,72,-7},{5,72,-8},{4,72,-8},{3,72,-9},{2,72,-9},{1,72,-9},{0,72,-9},{-1,72,-9},{-3,72,-8},{-2,72,-9},{-5,72,-7},{-4,72,-8},{-6,72,-6},{-7,72,-5},{-8,72,-4},{-8,72,-3},{-9,72,-1},{-9,72,0},{-9,72,1},{-9,72,3},{-9,72,2},{-9,72,-2},{-8,72,4},{-8,72,5},{-7,72,6},{-6,72,7},{-5,72,8},{-4,72,9},{-3,72,9},{-2,73,10},{-1,73,10},{0,73,10},{1,73,10},{2,73,10},{3,73,10},{5,72,9},{4,72,9},{7,72,7},{6,72,8},{8,72,6},{9,72,5},{9,72,4},{10,73,3},{10,73,2},{10,73,1},{10,73,0},{10,73,-1},{10,73,-2},{9,72,-3},{9,72,-4},{8,72,-5},{7,72,-6}
        };

        // ====================
        //    CORALS / ABYSS (Y: 83)
        // ====================
        int[][] coralsCoords = {
            {-1,83,10},{0,83,10},{1,83,10},{2,83,10},{5,83,9},{3,83,10},{4,83,9},{7,83,7},{6,83,8},{8,83,6},{9,83,5},{9,83,4},{10,83,0},{10,83,1},{10,83,3},{10,83,2},{10,83,-1},{10,83,-2},{9,83,-3},{9,83,-4},{8,83,-5},{7,83,-6},{6,83,-7},{5,83,-8},{4,83,-8},{3,83,-9},{2,83,-9},{1,83,-9},{-1,83,-9},{-2,83,-9},{0,83,-9},{-3,83,-8},{-5,83,-7},{-6,83,-6},{-7,83,-5},{-8,83,-3},{-9,83,3},{-8,83,4},{-8,83,5},{-7,83,6},{-6,83,7},{-5,83,8},{-4,83,9},{-3,83,9},{-2,83,10},{-9,83,2},{-9,83,1},{-9,83,0},{-9,83,-1},{-9,83,-2},{-8,83,-4},{-4,83,-8}
        };

        // ====================
        //      GENESIS MAP (Y: 44)
        // ====================
        int[][] genesisCoords = {
            {-3,44,11}, {-2,44,11}, {-1,44,11}, {0,44,11}, {1,44,11}, {2,44,11}, {3,44,11},
            {4,44,10}, {5,44,10}, {6,44,9}, {7,44,9}, {8,44,8}, {9,44,7}, {9,44,6}, {10,44,5}, {10,44,4},
            {11,44,3}, {11,44,2}, {11,44,1}, {11,44,0}, {11,44,-1}, {11,44,-2}, {11,44,-3},
            {10,44,-4}, {10,44,-5}, {9,44,-6}, {9,44,-7}, {8,44,-8}, {7,44,-9}, {6,44,-9}, {5,44,-10}, {4,44,-10},
            {3,44,-11}, {2,44,-11}, {1,44,-11}, {0,44,-11}, {-1,44,-11}, {-2,44,-11}, {-3,44,-11},
            {-4,44,-10}, {-5,44,-10}, {-6,44,-9}, {-7,44,-9}, {-8,44,-8}, {-9,44,-7}, {-9,44,-6}, {-10,44,-5}, {-10,44,-4},
            {-11,44,-3}, {-11,44,-2}, {-11,44,-1}, {-11,44,0}, {-11,44,1}, {-11,44,2}, {-11,44,3},
            {-10,44,4}, {-10,44,5}, {-9,44,6}, {-9,44,7}, {-8,44,8}, {-7,44,9}, {-6,44,9}, {-5,44,10}, {-4,44,10}
        };

        // ====================
        //    FOUR SEASONS MAP (Y: 83)
        // ====================
        int[][] fourSeasonsCoords = {
            {-3,83,11}, {-2,83,11}, {-1,83,11}, {0,83,11}, {1,83,11}, {2,83,11}, {3,83,11},
            {4,83,10}, {5,83,10}, {6,83,9}, {7,83,9}, {8,83,8}, {9,83,7}, {9,83,6}, {10,83,5}, {10,83,4},
            {11,83,3}, {11,83,2}, {11,83,1}, {11,83,0}, {11,83,-1}, {11,83,-2}, {11,83,-3},
            {10,83,-4}, {10,83,-5}, {9,83,-6}, {9,83,-7}, {8,83,-8}, {7,83,-9}, {6,83,-9}, {5,83,-10}, {4,83,-10},
            {3,83,-11}, {2,83,-11}, {1,83,-11}, {0,83,-11}, {-1,83,-11}, {-2,83,-11}, {-3,83,-11},
            {-4,83,-10}, {-5,83,-10}, {-6,83,-9}, {-7,83,-9}, {-8,83,-8}, {-9,83,-7}, {-9,83,-6}, {-10,83,-5}, {-10,83,-4},
            {-11,83,-3}, {-11,83,-2}, {-11,83,-1}, {-11,83,0}, {-11,83,1}, {-11,83,2}, {-11,83,3},
            {-10,83,4}, {-10,83,5}, {-9,83,6}, {-9,83,7}, {-8,83,8}, {-7,83,9}, {-6,83,9}, {-5,83,10}, {-4,83,10}
        };

        RINGS_BY_MAP.put(SpawnRegions.PitMap.ELEMENTS, toBlockPosList(elementsCoords));
        RINGS_BY_MAP.put(SpawnRegions.PitMap.CASTLE, Collections.emptyList()); 
        RINGS_BY_MAP.put(SpawnRegions.PitMap.CORALS, toBlockPosList(coralsCoords));
        RINGS_BY_MAP.put(SpawnRegions.PitMap.GENESIS, toBlockPosList(genesisCoords));
        RINGS_BY_MAP.put(SpawnRegions.PitMap.FOUR_SEASONS, toBlockPosList(fourSeasonsCoords));
    }

    private static List<BlockPos> toBlockPosList(int[][] coords) {
        List<BlockPos> list = new ArrayList<>(coords.length);
        for (int[] c : coords) {
            BlockPos pos = new BlockPos(c[0], c[1], c[2]);
            list.add(pos);
            MASTER_RING_LIST.add(pos); 
        }
        return list;
    }

    private boolean isInPit() {
        if (mc.theWorld == null || mc.thePlayer == null) return false;
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return false;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;

        // FIXED: Safe null-checks to prevent the exact NPE in the crash log
        String rawTitle = objective.getDisplayName();
        if (rawTitle == null) return false; 

        String title = StringUtils.stripControlCodes(rawTitle);
        return title != null && (title.contains("THE HYPIXEL PIT") || title.contains("PIT"));
    }

    public void updateMapDetection() {
        if (!enabled || mc.theWorld == null || mc.thePlayer == null || !isInPit()) {
            activeRingCache = Collections.emptyList();
            return;
        }

        if (mc.theWorld != lastDetectedWorld) {
            lastDetectedWorld = mc.theWorld;
            lastMapCheck = 0;
            activeMap = null;
            activeRingCache = Collections.emptyList();
        }

        long now = System.currentTimeMillis();
        if (now - lastMapCheck < 1000) return;
        lastMapCheck = now;

        int playerY = (int) mc.thePlayer.posY;

        if (playerY < 60) {
            activeMap = SpawnRegions.PitMap.GENESIS;
        } else if (playerY < 78) {
            activeMap = SpawnRegions.PitMap.ELEMENTS;
        } else {
            if (!mc.theWorld.isAirBlock(new BlockPos(0, 82, 11))) {
                activeMap = SpawnRegions.PitMap.FOUR_SEASONS;
            } else if (!mc.theWorld.isAirBlock(new BlockPos(0, 82, 10))) {
                activeMap = SpawnRegions.PitMap.CORALS;
            } else {
                activeMap = SpawnRegions.PitMap.CORALS; 
            }
        }

        if (activeMap != null) {
            activeRingCache = RINGS_BY_MAP.getOrDefault(activeMap, Collections.emptyList());
        }
    }

    public boolean shouldBlockPlacement(BlockPos clickedPos, EnumFacing face) {
        if (!enabled || !preventMisplace || mc.thePlayer == null || mc.theWorld == null || !isInPit()) {
            return false; 
        }
        
        if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)) {
            return false; 
        }

        Block clickedBlock = mc.theWorld.getBlockState(clickedPos).getBlock();
        if (!mc.thePlayer.isSneaking() && (clickedBlock instanceof BlockChest || clickedBlock instanceof BlockEnderChest)) {
            return false; 
        }

        BlockPos targetPos = clickedPos.offset(face);

        for (BlockPos ringBlock : MASTER_RING_LIST) {
            if (targetPos.getX() == ringBlock.getX() && targetPos.getZ() == ringBlock.getZ()) {
                if (targetPos.getY() >= ringBlock.getY() + 1 && targetPos.getY() <= ringBlock.getY() + 3) {
                    return true; 
                }
            }
        }
        return false; 
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        updateMapDetection();
        
        // FIXED: Extra safety checks for the active ring cache and RenderManager
        if (!enabled || !renderRing || mc.thePlayer == null || mc.theWorld == null || !isInPit()) return;
        if (activeRingCache == null || activeRingCache.isEmpty()) return;
        if (mc.getRenderManager() == null) return;

        for (BlockPos pos : activeRingCache) {
            if (pos == null) continue; // Ultimate safety check
            
            double x = pos.getX() - mc.getRenderManager().viewerPosX;
            double y = pos.getY() - mc.getRenderManager().viewerPosY;
            double z = pos.getZ() - mc.getRenderManager().viewerPosZ;

            AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);

            RenderUtils.setup3D();
            RenderUtils.drawFilledBox(bb, 1.0f, 0.33f, 1.0f, 0.20f);
            RenderUtils.drawOutlinedBox(bb, 1.0f, 0.33f, 1.0f, 1.0f, 2.0f);
            RenderUtils.end3D();
        }
    }

    public static void toggle() {
        enabled = !enabled;
        if (Minecraft.getMinecraft().thePlayer != null) {
            String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] ";
            String status = enabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(prefix + EnumChatFormatting.YELLOW + "Ring Helper: " + status));
        }
    }

    public void onWorldChanged() {
        lastDetectedWorld = null;
        lastMapCheck = 0;
        activeMap = null;
        activeRingCache = Collections.emptyList();
    }
}