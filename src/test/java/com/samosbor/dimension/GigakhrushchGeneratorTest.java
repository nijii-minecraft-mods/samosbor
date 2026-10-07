package com.samosbor.dimension;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GigakhrushchGeneratorTest {

    @Test
    public void testStairTierBoundariesAndVariableHeights() {
        int[] boundaries = GigakhrushchLayout.TIER_BOUNDARIES;
        assertTrue(boundaries.length >= 10, "Should have at least 10 tier boundaries");
        assertEquals(1, boundaries[0], "First tier must begin at Floor 1");
        assertEquals(GigakhrushchLayout.TOTAL_FLOORS, boundaries[boundaries.length - 1], "Last tier must reach TOTAL_FLOORS");

        for (int i = 0; i < boundaries.length - 1; i++) {
            int span = boundaries[i + 1] - boundaries[i];
            if (i < boundaries.length - 2) {
                assertTrue(span >= 13 && span <= 23,
                        "Intermediate tier flight span must be variable between 13 and 23 floors (got " + span + " for tier " + i + ")");
            }
        }
    }

    @Test
    public void testFloorToTierMapping() {
        int totalFloors = GigakhrushchLayout.TOTAL_FLOORS;
        for (int floor = 1; floor <= totalFloors; floor++) {
            int tier = GigakhrushchLayout.getStairTierForFloor(floor);
            assertTrue(tier >= 0 && tier < GigakhrushchLayout.TIER_BOUNDARIES.length - 1,
                    "Tier must be within valid index range for floor " + floor);
        }
    }

    @Test
    public void testBoundaryFloors() {
        int[] boundaries = GigakhrushchLayout.TIER_BOUNDARIES;
        for (int i = 1; i < boundaries.length; i++) {
            assertTrue(GigakhrushchLayout.isBoundaryFloor(boundaries[i]),
                    "Floor " + boundaries[i] + " must be recognized as boundary floor");
        }
        assertFalse(GigakhrushchLayout.isBoundaryFloor(2));
        assertFalse(GigakhrushchLayout.isBoundaryFloor(10));
        assertFalse(GigakhrushchLayout.isBoundaryFloor(100));
    }

    @Test
    public void testConsecutiveTierStairwellDisjointness() {
        // Test that across a 100x100 grid of chunks, no chunk has both tier T and tier T-1 stairwells
        for (int cx = -50; cx <= 50; cx++) {
            for (int cz = -50; cz <= 50; cz++) {
                for (int tier = 1; tier < GigakhrushchLayout.TIER_BOUNDARIES.length - 1; tier++) {
                    boolean isCurrent = GigakhrushchLayout.isStairForTier(cx, cz, tier);
                    boolean isPrevious = GigakhrushchLayout.isStairForTier(cx, cz, tier - 1);
                    assertFalse(isCurrent && isPrevious,
                            "Chunk (" + cx + ", " + cz + ") must NOT have consecutive stair tiers " + tier + " and " + (tier - 1));
                }
            }
        }
    }

    @Test
    public void testCourtyardTypeVariety() {
        Set<GigakhrushchLayout.CourtyardType> seen = new HashSet<>();
        for (int cx = -30; cx <= 30; cx++) {
            for (int cz = -30; cz <= 30; cz++) {
                if (GigakhrushchLayout.isCourtyardChunk(cx, cz)) {
                    seen.add(GigakhrushchLayout.getCourtyardType(cx, cz));
                }
            }
        }
        assertEquals(4, seen.size(), "All 4 courtyard types must be generated across the megastructure");
        assertTrue(seen.contains(GigakhrushchLayout.CourtyardType.LIGHTWELL_4X4));
        assertTrue(seen.contains(GigakhrushchLayout.CourtyardType.STANDARD_8X8));
        assertTrue(seen.contains(GigakhrushchLayout.CourtyardType.CHASM_12X12));
        assertTrue(seen.contains(GigakhrushchLayout.CourtyardType.STEPPED_TERRACE));
    }

    @Test
    public void testMetroTransitTopology() {
        assertTrue(GigakhrushchLayout.isMetroFloor(3));
        assertTrue(GigakhrushchLayout.isMetroFloor(43));
        assertTrue(GigakhrushchLayout.isMetroFloor(83));
        assertTrue(GigakhrushchLayout.isMetroFloor(123));
        assertFalse(GigakhrushchLayout.isMetroFloor(1));
        assertFalse(GigakhrushchLayout.isMetroFloor(10));

        // Test metro lines along Z
        assertTrue(GigakhrushchLayout.isMetroLineChunk(0));
        assertTrue(GigakhrushchLayout.isMetroLineChunk(6));
        assertTrue(GigakhrushchLayout.isMetroLineChunk(12));
        assertFalse(GigakhrushchLayout.isMetroLineChunk(1));
        assertFalse(GigakhrushchLayout.isMetroLineChunk(5));

        // Test metro stations along X
        assertTrue(GigakhrushchLayout.isMetroStationChunk(0, 0));
        assertTrue(GigakhrushchLayout.isMetroStationChunk(4, 0));
        assertTrue(GigakhrushchLayout.isMetroStationChunk(8, 6));
        assertFalse(GigakhrushchLayout.isMetroStationChunk(1, 0));
        assertFalse(GigakhrushchLayout.isMetroStationChunk(2, 6));
    }

    @Test
    public void testFloorArchetypes() {
        assertEquals(GigakhrushchLayout.FloorArchetype.ADMINISTRATION, GigakhrushchLayout.getFloorArchetype(6));
        assertEquals(GigakhrushchLayout.FloorArchetype.INDUSTRIAL, GigakhrushchLayout.getFloorArchetype(12));
        assertEquals(GigakhrushchLayout.FloorArchetype.LABORATORY, GigakhrushchLayout.getFloorArchetype(18));
        assertEquals(GigakhrushchLayout.FloorArchetype.FLOODED_TECHNICAL, GigakhrushchLayout.getFloorArchetype(24));
        assertEquals(GigakhrushchLayout.FloorArchetype.PURGED, GigakhrushchLayout.getFloorArchetype(39));
        assertEquals(GigakhrushchLayout.FloorArchetype.RESIDENTIAL, GigakhrushchLayout.getFloorArchetype(1));
        assertEquals(GigakhrushchLayout.FloorArchetype.RESIDENTIAL, GigakhrushchLayout.getFloorArchetype(2));
    }
}
