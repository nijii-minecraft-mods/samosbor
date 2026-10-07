package com.samosbor.dimension;

public class GigakhrushchLayout {

    public static final int FLOOR_HEIGHT = 6;
    public static final int MAX_HEIGHT = 1024;
    public static final int TOTAL_FLOORS = MAX_HEIGHT / FLOOR_HEIGHT; // ~170 floors

    // Variable stair tier boundaries with procedural flight spans between 13 and 23 floors:
    // Spans: 15, 21, 13, 18, 23, 16, 19, 14, 22, 8
    public static final int[] TIER_BOUNDARIES = {1, 16, 37, 50, 68, 91, 107, 126, 140, 162, 170};

    public enum FloorArchetype {
        RESIDENTIAL,
        INDUSTRIAL,
        LABORATORY,
        ADMINISTRATION,
        FLOODED_TECHNICAL,
        PURGED
    }

    public enum CourtyardType {
        LIGHTWELL_4X4,
        STANDARD_8X8,
        CHASM_12X12,
        STEPPED_TERRACE
    }

    public static int getStairTierForFloor(int floor) {
        for (int i = 0; i < TIER_BOUNDARIES.length - 1; i++) {
            if (floor >= TIER_BOUNDARIES[i] && floor < TIER_BOUNDARIES[i + 1]) {
                return i;
            }
        }
        return TIER_BOUNDARIES.length - 2;
    }

    public static boolean isBoundaryFloor(int floor) {
        for (int i = 1; i < TIER_BOUNDARIES.length; i++) {
            if (floor == TIER_BOUNDARIES[i]) return true;
        }
        return false;
    }

    public static boolean isCourtyardChunk(int chunkX, int chunkZ) {
        return Math.floorMod(chunkX * 7 + chunkZ * 11, 14) == 5;
    }

    public static CourtyardType getCourtyardType(int chunkX, int chunkZ) {
        long hash = ((long) chunkX * 492876847L) ^ ((long) chunkZ * 539828949L);
        int typeIndex = Math.floorMod(hash >> 4, 4);
        if (typeIndex == 0) return CourtyardType.LIGHTWELL_4X4;
        if (typeIndex == 1) return CourtyardType.CHASM_12X12;
        if (typeIndex == 2) return CourtyardType.STEPPED_TERRACE;
        return CourtyardType.STANDARD_8X8;
    }

    public static boolean isMetroLineChunk(int chunkZ) {
        return Math.floorMod(chunkZ, 6) == 0;
    }

    public static boolean isMetroFloor(int floor) {
        return floor == 3 || floor == 43 || floor == 83 || floor == 123;
    }

    public static boolean isMetroStationChunk(int chunkX, int chunkZ) {
        return isMetroLineChunk(chunkZ) && Math.floorMod(chunkX, 4) == 0;
    }

    public static boolean isStairForTier(int chunkX, int chunkZ, int tier) {
        if (isCourtyardChunk(chunkX, chunkZ)) return false;
        if (isMetroLineChunk(chunkZ)) return false; // Keeps stairwells in residential sectors
        long seed = ((long) chunkX * 3129871L) ^ ((long) chunkZ * 6187913L) ^ ((long) tier * 1013904223L);
        if (Math.floorMod(seed, 8) != 0) return false;
        if (tier > 0) {
            long prevSeed = ((long) chunkX * 3129871L) ^ ((long) chunkZ * 6187913L) ^ ((long) (tier - 1) * 1013904223L);
            if (Math.floorMod(prevSeed, 8) == 0) return false;
        }
        return true;
    }

    public static boolean isStairChunkForFloor(int chunkX, int chunkZ, int floor) {
        if (isCourtyardChunk(chunkX, chunkZ)) return false;
        if (isMetroLineChunk(chunkZ)) return false;
        int tier = getStairTierForFloor(floor);
        boolean isCurrent = isStairForTier(chunkX, chunkZ, tier);
        boolean isPreviousTop = isBoundaryFloor(floor) && isStairForTier(chunkX, chunkZ, tier - 1);
        return isCurrent || isPreviousTop;
    }

    public static boolean isElevatorChunk(int chunkX, int chunkZ) {
        if (isCourtyardChunk(chunkX, chunkZ)) return false;
        if (isMetroLineChunk(chunkZ)) return false;
        return Math.floorMod(chunkX * 13 + chunkZ * 19, 16) == 3;
    }

    public static FloorArchetype getFloorArchetype(int floor) {
        if (floor % 40 == 39) return FloorArchetype.PURGED;
        if (floor % 24 == 0 && floor > 0) return FloorArchetype.FLOODED_TECHNICAL;
        if (floor % 24 == 6) return FloorArchetype.ADMINISTRATION;
        if (floor % 24 == 12) return FloorArchetype.INDUSTRIAL;
        if (floor % 24 == 18) return FloorArchetype.LABORATORY;
        return FloorArchetype.RESIDENTIAL;
    }
}
