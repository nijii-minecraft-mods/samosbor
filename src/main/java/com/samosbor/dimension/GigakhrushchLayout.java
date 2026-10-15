package com.samosbor.dimension;

public class GigakhrushchLayout {

    public static final int FLOOR_HEIGHT = 6;
    public static final int MAX_HEIGHT = 1024;
    public static final int TOTAL_FLOORS = MAX_HEIGHT / FLOOR_HEIGHT; // ~170 floors

    // Variable stair tier boundaries with short flights (2 to 7 floors) and medium flights (13 to 21 floors):
    // Spans: 3, 18, 4, 17, 5, 21, 2, 14, 6, 18, 3, 19, 7, 19, 4, 9
    public static final int[] TIER_BOUNDARIES = {1, 4, 22, 26, 43, 48, 69, 71, 85, 91, 109, 112, 131, 138, 157, 161, 170};

    public enum FloorArchetype {
        RESIDENTIAL,
        INDUSTRIAL,
        LABORATORY,
        ADMINISTRATION,
        FLOODED_TECHNICAL,
        PURGED
    }

    public enum FloorZone {
        DEEP_FOUNDATIONS(1, 35, "Глубинный фундамент и дренаж"),
        LOWER_INDUSTRIAL(36, 70, "Нижний промышленный сектор"),
        CENTRAL_HABITATION(71, 100, "Центральный жилой сектор (Точка входа)"),
        UPPER_RESEARCH(101, 135, "Верхний научный сектор НИИ"),
        PARTY_STRATUM(136, 170, "Стратум номенклатуры и парткома");

        private final int minFloor;
        private final int maxFloor;
        private final String title;

        FloorZone(int minFloor, int maxFloor, String title) {
            this.minFloor = minFloor;
            this.maxFloor = maxFloor;
            this.title = title;
        }

        public int getMinFloor() { return minFloor; }
        public int getMaxFloor() { return maxFloor; }
        public String getTitle() { return title; }
    }

    public static FloorZone getFloorZone(int floor) {
        if (floor <= 35) return FloorZone.DEEP_FOUNDATIONS;
        if (floor <= 70) return FloorZone.LOWER_INDUSTRIAL;
        if (floor <= 100) return FloorZone.CENTRAL_HABITATION;
        if (floor <= 135) return FloorZone.UPPER_RESEARCH;
        return FloorZone.PARTY_STRATUM;
    }

    public static int getFlightSpan(int tier) {
        if (tier < 0 || tier >= TIER_BOUNDARIES.length - 1) return 0;
        return TIER_BOUNDARIES[tier + 1] - TIER_BOUNDARIES[tier];
    }

    public static boolean isShortFlight(int tier) {
        int span = getFlightSpan(tier);
        return span >= 2 && span <= 7;
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

    public enum CorridorTopology {
        CROSS,              // Intersection of EW and NS corridors at lx 7..8, lz 7..8
        HALLWAY_EW,         // Longitudinal West-to-East corridor (lx 0..15, lz 7..8)
        HALLWAY_NS,         // Longitudinal North-to-South corridor (lx 7..8, lz 0..15)
        T_NORTH,            // T-junction: EW corridor with North branch
        T_SOUTH,            // T-junction: EW corridor with South branch
        T_EAST,             // T-junction: NS corridor with East branch
        T_WEST,             // T-junction: NS corridor with West branch
        CORNER_NE,          // Corner: North to East branch
        CORNER_NW,          // Corner: North to West branch
        CORNER_SE,          // Corner: South to East branch
        CORNER_SW           // Corner: South to West branch
    }

    public static CorridorTopology getCorridorTopology(int chunkX, int chunkZ, int floor) {
        if (isCourtyardChunk(chunkX, chunkZ)) return CorridorTopology.CROSS;
        if (isMetroFloor(floor) && isMetroLineChunk(chunkZ)) return CorridorTopology.HALLWAY_EW;

        // Stair access door is at lx=3, lz=6 facing south into lz=7..8.
        // It requires a corridor covering lx 1..8 at lz 7..8.
        if (isStairChunkForFloor(chunkX, chunkZ, floor)) {
            int mod = Math.floorMod(((long) chunkX * 7919L) ^ ((long) chunkZ * 6271L) ^ ((long) floor * 104729L), 3);
            return switch (mod) {
                case 0 -> CorridorTopology.HALLWAY_EW;
                case 1 -> CorridorTopology.T_SOUTH;
                default -> CorridorTopology.CROSS;
            };
        }

        // Elevator door is at lx=9, lz=12 facing west into lx=7..8.
        // It requires a corridor covering lz 7..15 at lx 7..8.
        if (isElevatorChunk(chunkX, chunkZ)) {
            int mod = Math.floorMod(((long) chunkX * 7919L) ^ ((long) chunkZ * 6271L) ^ ((long) floor * 104729L), 3);
            return switch (mod) {
                case 0 -> CorridorTopology.HALLWAY_NS;
                case 1 -> CorridorTopology.T_EAST;
                default -> CorridorTopology.CROSS;
            };
        }

        long seed = ((long) chunkX * 3129871L) ^ ((long) chunkZ * 6187913L) ^ ((long) floor * 1013904223L);

        // Major arterial grid: every 3 chunks is a cross intersection connecting all sectors
        boolean mainX = Math.floorMod(chunkX, 3) == 0;
        boolean mainZ = Math.floorMod(chunkZ, 3) == 0;

        if (mainX && mainZ) {
            return CorridorTopology.CROSS;
        }
        if (mainZ) {
            int r = Math.floorMod(seed >> 2, 4);
            if (r == 0) return CorridorTopology.T_NORTH;
            if (r == 1) return CorridorTopology.T_SOUTH;
            return CorridorTopology.HALLWAY_EW;
        }
        if (mainX) {
            int r = Math.floorMod(seed >> 2, 4);
            if (r == 0) return CorridorTopology.T_EAST;
            if (r == 1) return CorridorTopology.T_WEST;
            return CorridorTopology.HALLWAY_NS;
        }

        // Secondary residential wings with varied layouts
        int r = Math.floorMod(seed >> 4, 10);
        return switch (r) {
            case 0, 1 -> CorridorTopology.HALLWAY_EW;
            case 2, 3 -> CorridorTopology.HALLWAY_NS;
            case 4 -> CorridorTopology.T_NORTH;
            case 5 -> CorridorTopology.T_SOUTH;
            case 6 -> CorridorTopology.CORNER_NE;
            case 7 -> CorridorTopology.CORNER_NW;
            case 8 -> CorridorTopology.CORNER_SE;
            default -> CorridorTopology.CROSS;
        };
    }

    public static boolean isCorridorTile(int lx, int lz, CorridorTopology topology) {
        boolean inEW = (lz == 7 || lz == 8);
        boolean inNS = (lx == 7 || lx == 8);

        return switch (topology) {
            case CROSS -> inEW || inNS;
            case HALLWAY_EW -> inEW;
            case HALLWAY_NS -> inNS;
            case T_NORTH -> inEW || (inNS && lz <= 7);
            case T_SOUTH -> inEW || (inNS && lz >= 8);
            case T_EAST -> inNS || (inEW && lx >= 8);
            case T_WEST -> inNS || (inEW && lx <= 7);
            case CORNER_NE -> (inEW && lx >= 8) || (inNS && lz <= 7);
            case CORNER_NW -> (inEW && lx <= 7) || (inNS && lz <= 7);
            case CORNER_SE -> (inEW && lx >= 8) || (inNS && lz >= 8);
            case CORNER_SW -> (inEW && lx <= 7) || (inNS && lz >= 8);
        };
    }
}
