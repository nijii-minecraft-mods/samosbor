package com.samosbor.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.samosbor.block.CastIronRadiatorBlock;
import com.samosbor.block.ElevatorPanelBlock;
import com.samosbor.block.GasStoveBlock;
import com.samosbor.block.HermeticDoorBlock;
import com.samosbor.block.ModBlocks;
import com.samosbor.block.OldTvBlock;
import com.samosbor.block.RadioReceiverBlock;
import com.samosbor.block.WallCarpetBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GigakhrushchChunkGenerator extends ChunkGenerator {

    public static final MapCodec<GigakhrushchChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, instance.stable(GigakhrushchChunkGenerator::new))
    );

    public static final int FLOOR_HEIGHT = 6;
    public static final int MAX_HEIGHT = 1024;
    public static final int TOTAL_FLOORS = MAX_HEIGHT / FLOOR_HEIGHT; // ~170 floors

    public GigakhrushchChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    public static void register() {
        Registry.register(
                Registries.CHUNK_GENERATOR,
                Identifier.of(ModDimensions.MOD_ID, "gigakhrushch"),
                CODEC
        );
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        generateGigakhrushch(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    private enum FloorArchetype {
        RESIDENTIAL,
        INDUSTRIAL,
        LABORATORY,
        ADMINISTRATION,
        FLOODED_TECHNICAL,
        PURGED
    }

    private FloorArchetype getFloorArchetype(int floor) {
        if (floor % 40 == 39) return FloorArchetype.PURGED;
        if (floor % 24 == 0 && floor > 0) return FloorArchetype.FLOODED_TECHNICAL;
        if (floor % 24 == 6) return FloorArchetype.ADMINISTRATION;
        if (floor % 24 == 12) return FloorArchetype.INDUSTRIAL;
        if (floor % 24 == 18) return FloorArchetype.LABORATORY;
        return FloorArchetype.RESIDENTIAL;
    }

    private void generateGigakhrushch(Chunk chunk) {
        ChunkPos cpos = chunk.getPos();
        int chunkX = cpos.x;
        int chunkZ = cpos.z;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        // Cached BlockStates
        BlockState bedrock = Blocks.BEDROCK.getDefaultState();
        BlockState p46 = ModBlocks.P46_PANEL.getDefaultState();
        BlockState painted = ModBlocks.PAINTED_PANEL.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState chains = Blocks.CHAIN.getDefaultState();
        BlockState ironBars = Blocks.IRON_BARS.getDefaultState();
        BlockState fluorLamp = ModBlocks.FLUORESCENT_LAMP.getDefaultState();
        BlockState emergLamp = ModBlocks.EMERGENCY_LAMP.getDefaultState();

        BlockState hermeticDoorLowerN = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.NORTH);
        BlockState hermeticDoorUpperN = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.NORTH);
        BlockState hermeticDoorLowerS = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH);
        BlockState hermeticDoorUpperS = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH);
        BlockState hermeticDoorLowerE = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.EAST);
        BlockState hermeticDoorUpperE = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.EAST);
        BlockState hermeticDoorLowerW = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.WEST);
        BlockState hermeticDoorUpperW = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.WEST);

        BlockState elevatorDoorLower = ModBlocks.ELEVATOR_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.EAST);
        BlockState elevatorDoorUpper = ModBlocks.ELEVATOR_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.EAST);

        boolean isStairChunk = Math.floorMod(chunkX + chunkZ, 5) == 0;
        boolean isElevatorChunk = Math.floorMod(chunkX + chunkZ, 5) == 2;

        // Foundation at Y=0..2
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                for (int y = 0; y <= 2; y++) {
                    chunk.setBlockState(mutable.set(lx, y, lz), bedrock, false);
                }
                // Megastructure ceiling at Y=1020..1023
                for (int y = 1020; y < MAX_HEIGHT; y++) {
                    chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                }
            }
        }

        // Generate each floor from Y=6 up to Y=1014
        for (int floor = 1; floor < TOTAL_FLOORS && (floor * FLOOR_HEIGHT + FLOOR_HEIGHT) <= 1020; floor++) {
            int baseY = floor * FLOOR_HEIGHT;
            FloorArchetype archetype = getFloorArchetype(floor);
            long floorSeed = ((long) chunkX * 3129871L) ^ ((long) chunkZ * 6187913L) ^ ((long) floor * 1013904223L);

            // 1. Generate floor slab at baseY
            generateFloorSlab(chunk, mutable, baseY, archetype, isStairChunk, isElevatorChunk, p46, air);

            // 2. Generate corridors (lx=7..8 or lz=7..8) and walls (lx=6,9 or lz=6,9)
            generateCorridorsAndWalls(chunk, mutable, baseY, floor, archetype, p46, painted, air, fluorLamp, emergLamp,
                    hermeticDoorLowerN, hermeticDoorUpperN, hermeticDoorLowerS, hermeticDoorUpperS,
                    hermeticDoorLowerE, hermeticDoorUpperE, hermeticDoorLowerW, hermeticDoorUpperW);

            // 3. Generate Stairwell in Quadrant 1 (lx in 2..5, lz in 2..5) if stair chunk
            if (isStairChunk) {
                generateStairwell(chunk, mutable, baseY, p46, painted, air, ironBars, emergLamp);
            }

            // 4. Generate Elevator Shaft & Cabin in Quadrant 4 (lx in 10..14, lz in 10..14) if elevator chunk
            if (isElevatorChunk) {
                generateElevatorShaft(chunk, mutable, baseY, floor, p46, air, chains, ironBars, elevatorDoorLower, elevatorDoorUpper, emergLamp);
            }

            // 5. Generate Room Interiors in remaining quadrants
            generateRoomQuadrants(chunk, mutable, baseY, floor, floorSeed, archetype, isStairChunk, isElevatorChunk, p46, air);
        }
    }

    private void generateFloorSlab(Chunk chunk, BlockPos.Mutable mutable, int baseY, FloorArchetype archetype,
                                  boolean isStairChunk, boolean isElevatorChunk, BlockState p46, BlockState air) {
        BlockState floorState = switch (archetype) {
            case PURGED -> Blocks.BLACKSTONE.getDefaultState();
            case LABORATORY -> Blocks.WHITE_CONCRETE.getDefaultState();
            case ADMINISTRATION -> Blocks.POLISHED_ANDESITE.getDefaultState();
            case FLOODED_TECHNICAL -> Blocks.MOSSY_STONE_BRICKS.getDefaultState();
            default -> p46;
        };

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                // Stair opening only at lx in 2..5, lz in 1..4 (lz 5..6 is solid landing floor!)
                boolean isStairOpening = isStairChunk && (lx >= 2 && lx <= 5 && lz >= 1 && lz <= 4);
                // Elevator hoistway opening at lx in 11..13, lz == 13 (lx in 11..13, lz in 11..12 is cabin landing platform)
                boolean isElevatorOpening = isElevatorChunk && (lx >= 11 && lx <= 13 && lz == 13);

                if (isStairOpening || isElevatorOpening) {
                    chunk.setBlockState(mutable.set(lx, baseY, lz), air, false);
                } else {
                    chunk.setBlockState(mutable.set(lx, baseY, lz), floorState, false);
                }
            }
        }
    }

    private void generateCorridorsAndWalls(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, FloorArchetype archetype,
                                           BlockState p46, BlockState painted, BlockState air, BlockState fluorLamp, BlockState emergLamp,
                                           BlockState doorLN, BlockState doorUN, BlockState doorLS, BlockState doorUS,
                                           BlockState doorLE, BlockState doorUE, BlockState doorLW, BlockState doorUW) {
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                boolean isCorridorX = (lx == 7 || lx == 8);
                boolean isCorridorZ = (lz == 7 || lz == 8);
                boolean isCorridor = isCorridorX || isCorridorZ;

                if (isCorridor) {
                    // Clear corridor air (y = baseY + 1 .. baseY + 5)
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }

                    // Corridor Floor dressing
                    if (archetype == FloorArchetype.ADMINISTRATION) {
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), Blocks.RED_CARPET.getDefaultState(), false);
                    } else if (archetype == FloorArchetype.FLOODED_TECHNICAL) {
                        if ((lx + lz) % 3 == 0) {
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), Blocks.WATER.getDefaultState(), false);
                        }
                    }

                    // Corridor ceiling lighting (every 4 blocks)
                    if ((lx + lz) % 4 == 0) {
                        BlockState lamp = (archetype == FloorArchetype.PURGED || archetype == FloorArchetype.FLOODED_TECHNICAL || floor % 4 == 0)
                                ? emergLamp : fluorLamp;
                        chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, lz), lamp, false);
                    }
                }

                // Corridor boundary walls and apartment doorways
                boolean isWallX = (lx == 6 || lx == 9);
                boolean isWallZ = (lz == 6 || lz == 9);

                if ((isWallX && !isCorridorZ) || (isWallZ && !isCorridorX)) {
                    // Check doorway locations into apartments
                    boolean isDoor = (isWallX && (lz == 3 || lz == 12)) || (isWallZ && (lx == 3 || lx == 12));

                    if (isDoor) {
                        // Place sealed blast door with directional facing
                        BlockState lowerDoor = isWallX ? (lx == 6 ? doorLE : doorLW) : (lz == 6 ? doorLS : doorLN);
                        BlockState upperDoor = isWallX ? (lx == 6 ? doorUE : doorUW) : (lz == 6 ? doorUS : doorUN);

                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), lowerDoor, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), upperDoor, false);
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    } else {
                        // Authentic two-tone wall: lower 2 blocks painted, upper 3 blocks concrete
                        BlockState lowerWall = (archetype == FloorArchetype.PURGED) ? Blocks.POLISHED_BLACKSTONE.getDefaultState() : painted;
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), lowerWall, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), lowerWall, false);
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    }
                }

                // Outer chunk party walls (lx==0, 15 or lz==0, 15)
                if (lx == 0 || lx == 15 || lz == 0 || lz == 15) {
                    if (!isCorridor) {
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), painted, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), painted, false);
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    }
                }
            }
        }
    }

    private void generateStairwell(Chunk chunk, BlockPos.Mutable mutable, int baseY, BlockState p46, BlockState painted,
                                  BlockState air, BlockState ironBars, BlockState emergLamp) {
        // Enclosing walls around stairwell (lx in 1..6, lz in 1..6)
        for (int lx = 1; lx <= 6; lx++) {
            for (int lz = 1; lz <= 6; lz++) {
                boolean isPerimeter = (lx == 1 || lx == 6 || lz == 1 || lz == 6);
                if (isPerimeter) {
                    if (lz == 6 && lx == 3) {
                        // Stairwell access doorway with sealed blast door
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.NORTH), false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.NORTH), false);
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                        continue;
                    }
                    chunk.setBlockState(mutable.set(lx, baseY + 1, lz), painted, false);
                    chunk.setBlockState(mutable.set(lx, baseY + 2, lz), painted, false);
                    for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                    }
                } else {
                    // Clear stairwell interior air
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }
                }
            }
        }

        // Two-flight Soviet staircase:
        // Main landing at lz in 5..6, lx in 2..5 is already solid floor at baseY!

        // Flight 1: Ascends North along lx in 2..3 from baseY to intermediate landing at baseY+3
        BlockState stairsNorth = Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
        BlockState stairsSouth = Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);

        // Step 1 at lz=4, y=baseY+1
        chunk.setBlockState(mutable.set(2, baseY + 1, 4), stairsNorth, false);
        chunk.setBlockState(mutable.set(3, baseY + 1, 4), stairsNorth, false);

        // Step 2 at lz=3, y=baseY+2
        chunk.setBlockState(mutable.set(2, baseY + 2, 3), stairsNorth, false);
        chunk.setBlockState(mutable.set(3, baseY + 2, 3), stairsNorth, false);

        // Intermediate landing at lz in 1..2, lx in 2..5, y=baseY+3
        for (int lx = 2; lx <= 5; lx++) {
            for (int lz = 1; lz <= 2; lz++) {
                chunk.setBlockState(mutable.set(lx, baseY + 3, lz), p46, false);
            }
        }

        // Flight 2: Ascends South along lx in 4..5 from baseY+3 up to baseY+6
        // Step 3 at lz=3, y=baseY+4
        chunk.setBlockState(mutable.set(4, baseY + 4, 3), stairsSouth, false);
        chunk.setBlockState(mutable.set(5, baseY + 4, 3), stairsSouth, false);

        // Step 4 at lz=4, y=baseY+5
        chunk.setBlockState(mutable.set(4, baseY + 5, 4), stairsSouth, false);
        chunk.setBlockState(mutable.set(5, baseY + 5, 4), stairsSouth, false);
        // Landing at lz=5, y=baseY+6 connects seamlessly to the upper floor slab!

        // Safety Railings in the central gap between Flight 1 and Flight 2
        chunk.setBlockState(mutable.set(3, baseY + 3, 3), ironBars, false);
        chunk.setBlockState(mutable.set(3, baseY + 3, 4), ironBars, false);
        chunk.setBlockState(mutable.set(4, baseY + 3, 2), ironBars, false);

        // Landing illumination
        chunk.setBlockState(mutable.set(3, baseY + 5, 2), emergLamp, false);
        chunk.setBlockState(mutable.set(4, baseY + 5, 5), ModBlocks.FLUORESCENT_LAMP.getDefaultState(), false);
    }

    private void generateElevatorShaft(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, BlockState p46, BlockState air,
                                      BlockState chains, BlockState ironBars, BlockState doorLower, BlockState doorUpper, BlockState emergLamp) {
        // Enclose elevator shaft at lx in 10..14, lz in 10..14
        for (int lx = 10; lx <= 14; lx++) {
            for (int lz = 10; lz <= 14; lz++) {
                boolean isShaftWall = (lx == 10 || lx == 14 || lz == 10 || lz == 14);
                if (isShaftWall) {
                    if (lx == 10 && lz == 12) {
                        // Elevator landing door from corridor side
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), doorLower, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), doorUpper, false);
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    } else if (lx == 14 && (lz == 11 || lz == 12)) {
                        // Inspection mesh windows into the shaft
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), p46, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), ironBars, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 3, lz), ironBars, false);
                        for (int y = baseY + 4; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    } else {
                        // Solid reinforced concrete shaft wall
                        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    }
                } else {
                    // Inside the shaft (lx in 11..13, lz in 11..13)
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }
                }
            }
        }

        // Hanging suspension cables down the open rear hoistway at lz=13
        for (int y = baseY; y < baseY + FLOOR_HEIGHT; y++) {
            chunk.setBlockState(mutable.set(12, y, 13), chains, false);
        }

        // Cabin Landing platform: Polished andesite floor at lx in 11..13, lz in 11..12
        for (int lx = 11; lx <= 13; lx++) {
            for (int lz = 11; lz <= 12; lz++) {
                chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_ANDESITE.getDefaultState(), false);
            }
        }

        // Inside cabin: Elevator Control Panel on the back wall (lx=12, lz=12, y=baseY+2, facing NORTH)
        BlockState panelState = ModBlocks.ELEVATOR_PANEL.getDefaultState().with(ElevatorPanelBlock.FACING, Direction.NORTH);
        chunk.setBlockState(mutable.set(12, baseY + 2, 12), panelState, false);

        // Cabin interior ceiling light
        chunk.setBlockState(mutable.set(12, baseY + 4, 11), emergLamp, false);

        // Outside call button on the corridor side
        chunk.setBlockState(mutable.set(9, baseY + 2, 12), Blocks.STONE_BUTTON.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST), false);

        // Long-range inter-dimensional elevator station every 16 floors (accessible in landing vestibule at lx=9, lz=11)
        if (floor % 16 == 0) {
            chunk.setBlockState(mutable.set(9, baseY + 1, 11), ModBlocks.ELEVATOR_STATION.getDefaultState(), false);
        }
    }

    private void generateRoomQuadrants(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, long floorSeed,
                                       FloorArchetype archetype, boolean isStairChunk, boolean isElevatorChunk,
                                       BlockState p46, BlockState air) {
        // Quadrant 1: lx in 1..5, lz in 1..5 (North-West)
        if (!isStairChunk) {
            generateApartment(chunk, mutable, baseY, floor, floorSeed ^ 101L, archetype, 1, 5, 1, 5);
        }

        // Quadrant 2: lx in 10..14, lz in 1..5 (North-East)
        generateApartment(chunk, mutable, baseY, floor, floorSeed ^ 202L, archetype, 10, 14, 1, 5);

        // Quadrant 3: lx in 1..5, lz in 10..14 (South-West)
        generateApartment(chunk, mutable, baseY, floor, floorSeed ^ 303L, archetype, 1, 5, 10, 14);

        // Quadrant 4: lx in 10..14, lz in 10..14 (South-East)
        if (!isElevatorChunk) {
            generateApartment(chunk, mutable, baseY, floor, floorSeed ^ 404L, archetype, 10, 14, 10, 14);
        }
    }

    private void generateApartment(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, long seed,
                                  FloorArchetype archetype, int minX, int maxX, int minZ, int maxZ) {
        // 1. Clear interior air
        for (int lx = minX; lx <= maxX; lx++) {
            for (int lz = minZ; lz <= maxZ; lz++) {
                for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                    chunk.setBlockState(mutable.set(lx, y, lz), Blocks.AIR.getDefaultState(), false);
                }
            }
        }

        int variant = Math.abs((int) (seed % 6));

        switch (archetype) {
            case RESIDENTIAL -> generateResidentialVariant(chunk, mutable, baseY, variant, minX, maxX, minZ, maxZ);
            case INDUSTRIAL -> generateIndustrialHall(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
            case LABORATORY -> generateLaboratory(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
            case ADMINISTRATION -> generateAdministration(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
            case FLOODED_TECHNICAL -> generateFloodedTechnical(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
            case PURGED -> generatePurgedZone(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
        }
    }

    private void generateResidentialVariant(Chunk chunk, BlockPos.Mutable mutable, int baseY, int variant,
                                           int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        switch (variant) {
            case 0 -> {
                // Classic Soviet Living Room (Зал с ковром, телевизором «Рекорд» и радиоприёмником «Маяк»)
                // Parquet floor
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.DARK_OAK_PLANKS.getDefaultState(), false);
                    }
                }
                // Wall Carpet on back wall
                BlockState carpet = ModBlocks.WALL_CARPET.getDefaultState().with(WallCarpetBlock.FACING, Direction.SOUTH);
                chunk.setBlockState(mutable.set(midX, baseY + 2, minZ), carpet, false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 2, minZ), carpet, false);

                // Table with TV and Radio
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.OLD_TV.getDefaultState().with(OldTvBlock.FACING, Direction.NORTH), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), ModBlocks.RADIO_RECEIVER.getDefaultState().with(RadioReceiverBlock.FACING, Direction.NORTH), false);

                // Radiator under the exterior wall
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ), ModBlocks.CAST_IRON_RADIATOR.getDefaultState().with(CastIronRadiatorBlock.FACING, Direction.EAST), false);

                // Bookshelf and sofa
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 2, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.RED_WOOL.getDefaultState(), false);
            }
            case 1 -> {
                // Soviet Communal Kitchen (Газовая плита, ржавая раковина, запасы концентрата)
                // Kitchen linoleum / tile floor
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        BlockState tile = ((lx + lz) % 2 == 0) ? Blocks.WHITE_TERRACOTTA.getDefaultState() : Blocks.LIGHT_GRAY_TERRACOTTA.getDefaultState();
                        chunk.setBlockState(mutable.set(lx, baseY, lz), tile, false);
                    }
                }
                // Gas stove with exhaust pipe
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), ModBlocks.GAS_STOVE.getDefaultState().with(GasStoveBlock.FACING, Direction.SOUTH), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), Blocks.IRON_BARS.getDefaultState(), false);

                // Rusty enameled sink
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, minZ), ModBlocks.RUSTY_SINK.getDefaultState(), false);

                // Food counters and barrels
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.BARREL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, midZ), Blocks.CRAFTING_TABLE.getDefaultState(), false);

                // Cast Iron radiator
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), ModBlocks.CAST_IRON_RADIATOR.getDefaultState().with(CastIronRadiatorBlock.FACING, Direction.NORTH), false);
            }
            case 2 -> {
                // Barricaded Survivor Safehouse (Убежище с запасами, колючей проволокой и лежанкой)
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.COBWEB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ), Blocks.HAY_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ + 1), Blocks.HAY_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.BARREL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), ModBlocks.FOOD_DISPENSER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ), Blocks.IRON_BARS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, minZ), Blocks.IRON_BARS.getDefaultState(), false);
            }
            case 3 -> {
                // Liquidator Decon Post / Arms Stash (Санпропускник)
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.CAULDRON.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.ANVIL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.CHEST.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 3, minZ), ModBlocks.EMERGENCY_LAMP.getDefaultState(), false);
            }
            case 4 -> {
                // Workshop / Radio Station (Ремонтный бокс деталей ЖП-11-У)
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.RADIO_RECEIVER.getDefaultState().with(RadioReceiverBlock.FACING, Direction.EAST), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ), Blocks.STONECUTTER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, midZ), Blocks.HOPPER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ), Blocks.BARREL.getDefaultState(), false);
            }
            case 5 -> {
                // Biomass Contaminated / Abandoned room (Прорыв слизи)
                chunk.setBlockState(mutable.set(midX, baseY, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.TOXIC_MUSHROOM.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), ModBlocks.TOXIC_MUSHROOM.getDefaultState(), false);
            }
        }
    }

    private void generateIndustrialHall(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        // Heavy industrial support column
        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
            chunk.setBlockState(mutable.set(midX, y, midZ), ModBlocks.P46_PANEL.getDefaultState(), false);
        }
        // Machine machinery and conveyors
        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BLAST_FURNACE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ + 1), Blocks.ANVIL.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.HOPPER.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), ModBlocks.FOOD_DISPENSER.getDefaultState(), false);
        chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.BARREL.getDefaultState(), false);
        // Overhead hanging chains
        chunk.setBlockState(mutable.set(midX, baseY + 4, minZ), Blocks.CHAIN.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 4, maxZ), Blocks.CHAIN.getDefaultState(), false);
    }

    private void generateLaboratory(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        // Containment cube with glass and Biomass specimen
        chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.TOXIC_MUSHROOM.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.TINTED_GLASS.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), Blocks.TINTED_GLASS.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 1, midZ + 1), Blocks.TINTED_GLASS.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 1, midZ - 1), Blocks.TINTED_GLASS.getDefaultState(), false);

        // Analysis benches
        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BREWING_STAND.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.CAULDRON.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.CHEST.getDefaultState(), false);
    }

    private void generateAdministration(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        // Archive bookshelves along walls
        for (int lx = minX; lx <= maxX; lx++) {
            chunk.setBlockState(mutable.set(lx, baseY + 1, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
            chunk.setBlockState(mutable.set(lx, baseY + 2, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
        }

        // Party desk and chair
        chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.POLISHED_ANDESITE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.HEAVY_CORE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 1, maxZ), Blocks.CHISELED_BOOKSHELF.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.BARREL.getDefaultState(), false);
    }

    private void generateFloodedTechnical(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        // Murky water pool on floor
        for (int lx = minX; lx <= maxX; lx++) {
            for (int lz = minZ; lz <= maxZ; lz++) {
                if ((lx + lz) % 2 == 0) {
                    chunk.setBlockState(mutable.set(lx, baseY + 1, lz), Blocks.WATER.getDefaultState(), false);
                }
            }
        }

        // Leaking rusty copper pipes overhead
        chunk.setBlockState(mutable.set(midX, baseY + 4, midZ), Blocks.COPPER_GRATE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 4, minZ), Blocks.COPPER_BLOCK.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 4, maxZ), Blocks.COPPER_BLOCK.getDefaultState(), false);

        // Cobwebs and broken valves
        chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), Blocks.COBWEB.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.CAULDRON.getDefaultState(), false);
    }

    private void generatePurgedZone(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        // Charred basalt and magma residue
        chunk.setBlockState(mutable.set(midX, baseY, midZ), Blocks.MAGMA_BLOCK.getDefaultState(), false);
        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BASALT.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.POLISHED_BLACKSTONE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.COAL_BLOCK.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 2, minZ), Blocks.IRON_BARS.getDefaultState(), false);
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
        // Enclosed Megastructure, no carvers
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public int getMinimumY() {
        return 0;
    }

    @Override
    public int getWorldHeight() {
        return MAX_HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return MAX_HEIGHT;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(0, new BlockState[]{Blocks.BEDROCK.getDefaultState()});
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
        int floor = pos.getY() / FLOOR_HEIGHT;
        text.add("Gigakhrushchyovka 1024m Floor: " + floor + " [" + getFloorArchetype(floor).name() + "]");
    }
}
