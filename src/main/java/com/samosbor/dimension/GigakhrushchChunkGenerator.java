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

    public static boolean isCourtyardChunk(int chunkX, int chunkZ) {
        return Math.floorMod(chunkX * 7 + chunkZ * 11, 14) == 5;
    }

    public static boolean isStairForSegment(int chunkX, int chunkZ, int segment) {
        if (isCourtyardChunk(chunkX, chunkZ)) return false;
        long seed = ((long) chunkX * 3129871L) ^ ((long) chunkZ * 6187913L) ^ ((long) segment * 1013904223L);
        return Math.floorMod(seed, 9) == 0;
    }

    public static boolean isStairChunkForFloor(int chunkX, int chunkZ, int floor) {
        if (isCourtyardChunk(chunkX, chunkZ)) return false;
        int segment = floor / 10;
        boolean isCurrent = isStairForSegment(chunkX, chunkZ, segment);
        boolean isPreviousTop = (floor % 10 == 0) && isStairForSegment(chunkX, chunkZ, segment - 1);
        return isCurrent || isPreviousTop;
    }

    public static boolean isElevatorChunk(int chunkX, int chunkZ) {
        if (isCourtyardChunk(chunkX, chunkZ)) return false;
        return Math.floorMod(chunkX * 13 + chunkZ * 19, 16) == 3;
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

        boolean isCourtyard = isCourtyardChunk(chunkX, chunkZ);
        boolean isElevator = isElevatorChunk(chunkX, chunkZ);

        // Foundation at Y=0..2
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                for (int y = 0; y <= 2; y++) {
                    chunk.setBlockState(mutable.set(lx, y, lz), bedrock, false);
                }
                // Megastructure reinforced ceiling at Y=1020..1023
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

            int currentSeg = floor / 10;
            boolean isCurrentSeg = isStairForSegment(chunkX, chunkZ, currentSeg);
            boolean isPrevSegTop = (floor % 10 == 0) && isStairForSegment(chunkX, chunkZ, currentSeg - 1);
            boolean isStair = isCurrentSeg || isPrevSegTop;

            if (isCourtyard) {
                // Vertical courtyard-well lightwell
                generateCourtyardChunk(chunk, mutable, baseY, floor, p46, air, ironBars, chains, emergLamp);
                continue;
            }

            // 1. Generate floor slab at baseY
            generateFloorSlab(chunk, mutable, baseY, floor, archetype, isStair, isCurrentSeg, isPrevSegTop, isElevator, p46, air);

            // 2. Generate corridors and perimeter walls
            generateCorridorsAndWalls(chunk, mutable, baseY, floor, archetype, p46, painted, air, fluorLamp, emergLamp,
                    hermeticDoorLowerN, hermeticDoorUpperN, hermeticDoorLowerS, hermeticDoorUpperS,
                    hermeticDoorLowerE, hermeticDoorUpperE, hermeticDoorLowerW, hermeticDoorUpperW);

            // 3. Generate Segmented Stairwell in Quadrant 1 (lx in 1..6, lz in 1..6)
            if (isStair) {
                generateSegmentedStairwell(chunk, mutable, baseY, floor, isCurrentSeg, isPrevSegTop, p46, painted, air, ironBars, emergLamp);
            }

            // 4. Generate Elevator Shaft & Cabin in Quadrant 4 (lx in 10..14, lz in 10..14)
            if (isElevator) {
                generateElevatorShaft(chunk, mutable, baseY, floor, p46, air, chains, ironBars, elevatorDoorLower, elevatorDoorUpper, emergLamp);
            }

            // 5. Generate Room Interiors in remaining quadrants with rich 12-variant catalog
            generateRoomQuadrants(chunk, mutable, baseY, floor, floorSeed, archetype, isStair, isElevator, p46, air);
        }
    }

    private void generateCourtyardChunk(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor,
                                        BlockState p46, BlockState air, BlockState ironBars, BlockState chains, BlockState emergLamp) {
        BlockState glass = Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.getDefaultState();
        BlockState planks = Blocks.OAK_PLANKS.getDefaultState();

        // Floor slab: Central void is strictly 4..11 (perimeter 0..3 and 12..15 is solid floor)
        // This ensures facade walls at lx=3,12 and lz=3,12 sit firmly on a solid slab with no air gaps!
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                boolean isCenterVoid = (lx >= 4 && lx <= 11 && lz >= 4 && lz <= 11);
                if (isCenterVoid) {
                    if (floor % 12 == 0) {
                        // Safety catch-net every 12 floors
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.COBWEB.getDefaultState(), false);
                        if (lx == 7 && lz == 7) {
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), ModBlocks.OLD_TV.getDefaultState(), false);
                        } else if (lx == 5 && lz == 9) {
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), Blocks.SKELETON_SKULL.getDefaultState(), false);
                        }
                    } else {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), air, false);
                    }
                } else {
                    chunk.setBlockState(mutable.set(lx, baseY, lz), p46, false);
                }
            }
        }

        boolean hasCatwalk = (floor % 24 == 12);

        // Facade walls looking into the courtyard at lx=3, lx=12, lz=3, lz=12
        for (int lx = 3; lx <= 12; lx++) {
            for (int lz = 3; lz <= 12; lz++) {
                boolean isFacade = (lx == 3 || lx == 12 || lz == 3 || lz == 12);
                if (isFacade) {
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        // Catwalk access doorway at lz=7
                        if (hasCatwalk && lz == 7 && (lx == 3 || lx == 12) && (y == baseY + 1 || y == baseY + 2)) {
                            chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                            continue;
                        }

                        // Window placement
                        boolean isWindowPos = (y == baseY + 2 || y == baseY + 3) && (lx % 3 == 0 || lz % 3 == 0);
                        if (isWindowPos) {
                            BlockState windowState = (lx + lz + floor) % 5 == 0 ? planks : ((lx + lz) % 2 == 0 ? glass : ironBars);
                            chunk.setBlockState(mutable.set(lx, y, lz), windowState, false);
                        } else {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    }
                } else {
                    // Courtyard interior air
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }
                }
            }
        }

        // Clotheslines and laundry stretched across courtyard
        if (floor % 3 == 0) {
            int lineZ = 5 + (floor % 6);
            for (int lx = 4; lx <= 11; lx++) {
                chunk.setBlockState(mutable.set(lx, baseY + 3, lineZ), chains, false);
                if (lx == 6 || lx == 9) {
                    chunk.setBlockState(mutable.set(lx, baseY + 2, lineZ), Blocks.WHITE_CARPET.getDefaultState(), false);
                }
            }
        }

        // Service catwalk crossing the courtyard
        if (hasCatwalk) {
            for (int lx = 4; lx <= 11; lx++) {
                chunk.setBlockState(mutable.set(lx, baseY + 1, 7), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(lx, baseY + 2, 6), ironBars, false);
                chunk.setBlockState(mutable.set(lx, baseY + 2, 8), ironBars, false);
            }
        }

        // Surrounding perimeter corridors
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                if (lx < 3 || lx > 12 || lz < 3 || lz > 12) {
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }
                    if ((lx + lz) % 6 == 0) {
                        chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, lz), emergLamp, false);
                    }
                }
            }
        }
    }

    private void generateFloorSlab(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, FloorArchetype archetype,
                                  boolean isStair, boolean isCurrentSeg, boolean isPrevSegTop, boolean isElevatorChunk, BlockState p46, BlockState air) {
        BlockState floorState = switch (archetype) {
            case PURGED -> Blocks.BLACKSTONE.getDefaultState();
            case LABORATORY -> Blocks.WHITE_CONCRETE.getDefaultState();
            case ADMINISTRATION -> Blocks.POLISHED_ANDESITE.getDefaultState();
            case FLOODED_TECHNICAL -> Blocks.MOSSY_STONE_BRICKS.getDefaultState();
            default -> p46;
        };

        // Floor 1 or bottom floor of a segment (floor % 10 == 0) has a solid floor slab so players don't fall into the void!
        boolean isBottomLanding = (floor == 1) || (isCurrentSeg && (floor % 10 == 0));

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                // Stair opening only at lx in 2..5, lz in 1..4 (lz 5..6 remains solid landing!)
                boolean isStairOpening = isStair && !isBottomLanding && (lx >= 2 && lx <= 5 && lz >= 1 && lz <= 4);
                // Elevator hoistway opening at lx in 11..13, lz == 13
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
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }

                    // Overhead cable trays
                    if (isCorridorX && lz % 2 == 0) {
                        chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, lz), Blocks.CHAIN.getDefaultState(), false);
                    } else if (isCorridorZ && lx % 2 == 0) {
                        chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, lz), Blocks.CHAIN.getDefaultState(), false);
                    }

                    // Corridor Floor dressing
                    if (archetype == FloorArchetype.ADMINISTRATION) {
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), Blocks.RED_CARPET.getDefaultState(), false);
                    } else if (archetype == FloorArchetype.FLOODED_TECHNICAL) {
                        if ((lx + lz) % 3 == 0) {
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), Blocks.WATER.getDefaultState(), false);
                        }
                    }

                    // Ceiling lighting
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
                    boolean isDoor = (isWallX && (lz == 3 || lz == 12)) || (isWallZ && (lx == 3 || lx == 12));
                    boolean isGarbageChutePos = (isWallX && lz == 5) || (isWallZ && lx == 10);

                    if (isGarbageChutePos) {
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), ModBlocks.GARBAGE_CHUTE.getDefaultState(), false);
                        for (int y = baseY + 2; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    } else if (isDoor) {
                        BlockState lowerDoor = isWallX ? (lx == 6 ? doorLE : doorLW) : (lz == 6 ? doorLS : doorLN);
                        BlockState upperDoor = isWallX ? (lx == 6 ? doorUE : doorUW) : (lz == 6 ? doorUS : doorUN);

                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), lowerDoor, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), upperDoor, false);
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    } else {
                        boolean isFireCabinet = (isWallX && lz == 2) || (isWallZ && lx == 13);
                        if (isFireCabinet && floor % 3 == 0) {
                            chunk.setBlockState(mutable.set(lx, baseY + 2, lz), Blocks.RED_TERRACOTTA.getDefaultState(), false);
                        } else {
                            BlockState lowerWall = (archetype == FloorArchetype.PURGED) ? Blocks.POLISHED_BLACKSTONE.getDefaultState() : painted;
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), lowerWall, false);
                            chunk.setBlockState(mutable.set(lx, baseY + 2, lz), lowerWall, false);
                        }
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    }
                }

                // Outer party walls
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

    private void generateSegmentedStairwell(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor,
                                           boolean isCurrentSeg, boolean isPrevSegTop, BlockState p46, BlockState painted,
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
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }
                }
            }
        }

        // Solid ceiling if top landing of a segment
        if (isPrevSegTop) {
            for (int lx = 2; lx <= 5; lx++) {
                for (int lz = 1; lz <= 5; lz++) {
                    chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT, lz), p46, false);
                }
            }
            // Arrival landing room decorations
            chunk.setBlockState(mutable.set(5, baseY + 1, 5), ModBlocks.GARBAGE_CHUTE.getDefaultState(), false);
            chunk.setBlockState(mutable.set(3, baseY + 5, 2), emergLamp, false);
            chunk.setBlockState(mutable.set(4, baseY + 5, 5), ModBlocks.FLUORESCENT_LAMP.getDefaultState(), false);
            return;
        }

        // Two-flight Soviet staircase:
        // Main landing at lz in 5..6, lx in 2..5 is solid floor at baseY (surface baseY + 1.0)
        BlockState stairsNorth = Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
        BlockState stairsSouth = Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);

        // Flight 1: Ascends North along lx in 2..3 from baseY to intermediate landing
        // Step 1 at lz=4, y=baseY+1 (top surface baseY + 2.0)
        chunk.setBlockState(mutable.set(2, baseY + 1, 4), stairsNorth, false);
        chunk.setBlockState(mutable.set(3, baseY + 1, 4), stairsNorth, false);

        // Step 2 at lz=3, y=baseY+2 (top surface baseY + 3.0)
        chunk.setBlockState(mutable.set(2, baseY + 2, 3), stairsNorth, false);
        chunk.setBlockState(mutable.set(3, baseY + 2, 3), stairsNorth, false);

        // Intermediate landing at lz in 1..2, lx in 2..5, y=baseY+2 (top surface baseY + 3.0) - EXACTLY LEVEL WITH STEP 2!
        for (int lx = 2; lx <= 5; lx++) {
            for (int lz = 1; lz <= 2; lz++) {
                chunk.setBlockState(mutable.set(lx, baseY + 2, lz), p46, false);
            }
        }

        // Flight 2: Ascends South along lx in 4..5 from intermediate landing to next floor
        // Step 3 at lz=3, y=baseY+3 (top surface baseY + 4.0)
        chunk.setBlockState(mutable.set(4, baseY + 3, 3), stairsSouth, false);
        chunk.setBlockState(mutable.set(5, baseY + 3, 3), stairsSouth, false);

        // Step 4 at lz=4, y=baseY+4 (top surface baseY + 5.0)
        chunk.setBlockState(mutable.set(4, baseY + 4, 4), stairsSouth, false);
        chunk.setBlockState(mutable.set(5, baseY + 4, 4), stairsSouth, false);

        // Step 5 at lz=5, y=baseY+5 (top surface baseY + 6.0) - EXACTLY LEVEL WITH UPPER FLOOR SLAB!
        chunk.setBlockState(mutable.set(4, baseY + 5, 5), stairsSouth, false);
        chunk.setBlockState(mutable.set(5, baseY + 5, 5), stairsSouth, false);

        // Safety Railings in central gap
        chunk.setBlockState(mutable.set(3, baseY + 3, 3), ironBars, false);
        chunk.setBlockState(mutable.set(3, baseY + 3, 4), ironBars, false);

        // Landing fixtures
        chunk.setBlockState(mutable.set(5, baseY + 1, 5), ModBlocks.GARBAGE_CHUTE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(3, baseY + 5, 2), emergLamp, false);
        chunk.setBlockState(mutable.set(2, baseY + 5, 5), ModBlocks.FLUORESCENT_LAMP.getDefaultState(), false);
    }

    private void generateElevatorShaft(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, BlockState p46, BlockState air,
                                      BlockState chains, BlockState ironBars, BlockState doorLower, BlockState doorUpper, BlockState emergLamp) {
        for (int lx = 10; lx <= 14; lx++) {
            for (int lz = 10; lz <= 14; lz++) {
                boolean isShaftWall = (lx == 10 || lx == 14 || lz == 10 || lz == 14);
                if (isShaftWall) {
                    if (lx == 10 && lz == 12) {
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), doorLower, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), doorUpper, false);
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    } else if (lx == 14 && (lz == 11 || lz == 12)) {
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), p46, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), ironBars, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 3, lz), ironBars, false);
                        for (int y = baseY + 4; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    } else {
                        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    }
                } else {
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }
                }
            }
        }

        // Hanging suspension cables down rear hoistway
        for (int y = baseY; y < baseY + FLOOR_HEIGHT; y++) {
            chunk.setBlockState(mutable.set(12, y, 13), chains, false);
        }

        // Cabin Landing platform
        for (int lx = 11; lx <= 13; lx++) {
            for (int lz = 11; lz <= 12; lz++) {
                chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_ANDESITE.getDefaultState(), false);
            }
        }

        // Inside cabin: Elevator Control Panel
        BlockState panelState = ModBlocks.ELEVATOR_PANEL.getDefaultState().with(ElevatorPanelBlock.FACING, Direction.NORTH);
        chunk.setBlockState(mutable.set(12, baseY + 2, 12), panelState, false);
        chunk.setBlockState(mutable.set(12, baseY + 4, 11), emergLamp, false);
        chunk.setBlockState(mutable.set(9, baseY + 2, 12), Blocks.STONE_BUTTON.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST), false);

        if (floor % 16 == 0) {
            chunk.setBlockState(mutable.set(9, baseY + 1, 11), ModBlocks.ELEVATOR_STATION.getDefaultState(), false);
        }
    }

    private void generateRoomQuadrants(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, long floorSeed,
                                       FloorArchetype archetype, boolean isStairChunk, boolean isElevatorChunk,
                                       BlockState p46, BlockState air) {
        if (!isStairChunk) {
            generateApartment(chunk, mutable, baseY, floor, floorSeed ^ 101L, archetype, 1, 5, 1, 5, 6, 3);
        }
        generateApartment(chunk, mutable, baseY, floor, floorSeed ^ 202L, archetype, 10, 14, 1, 5, 9, 3);
        generateApartment(chunk, mutable, baseY, floor, floorSeed ^ 303L, archetype, 1, 5, 10, 14, 6, 12);
        if (!isElevatorChunk) {
            generateApartment(chunk, mutable, baseY, floor, floorSeed ^ 404L, archetype, 10, 14, 10, 14, 9, 12);
        }
    }

    private void generateApartment(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, long seed,
                                  FloorArchetype archetype, int minX, int maxX, int minZ, int maxZ, int doorX, int doorZ) {
        for (int lx = minX; lx <= maxX; lx++) {
            for (int lz = minZ; lz <= maxZ; lz++) {
                for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                    chunk.setBlockState(mutable.set(lx, y, lz), Blocks.AIR.getDefaultState(), false);
                }
            }
        }

        int variant = Math.abs((int) (seed % 12));

        switch (archetype) {
            case RESIDENTIAL -> generateResidentialVariant(chunk, mutable, baseY, variant, minX, maxX, minZ, maxZ, doorX, doorZ);
            case INDUSTRIAL -> generateIndustrialHall(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
            case LABORATORY -> generateLaboratory(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
            case ADMINISTRATION -> generateAdministration(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
            case FLOODED_TECHNICAL -> generateFloodedTechnical(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
            case PURGED -> generatePurgedZone(chunk, mutable, baseY, minX, maxX, minZ, maxZ);
        }
    }

    private void generateResidentialVariant(Chunk chunk, BlockPos.Mutable mutable, int baseY, int variant,
                                           int minX, int maxX, int minZ, int maxZ, int doorX, int doorZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        switch (variant) {
            case 0 -> {
                // 1. Classic Soviet Living Room
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.DARK_OAK_PLANKS.getDefaultState(), false);
                    }
                }
                BlockState carpet = ModBlocks.WALL_CARPET.getDefaultState().with(WallCarpetBlock.FACING, Direction.SOUTH);
                chunk.setBlockState(mutable.set(midX, baseY + 2, minZ), carpet, false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 2, minZ), carpet, false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.OLD_TV.getDefaultState().with(OldTvBlock.FACING, Direction.NORTH), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), ModBlocks.RADIO_RECEIVER.getDefaultState().with(RadioReceiverBlock.FACING, Direction.NORTH), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ), ModBlocks.CAST_IRON_RADIATOR.getDefaultState().with(CastIronRadiatorBlock.FACING, Direction.EAST), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.RED_WOOL.getDefaultState(), false);
            }
            case 1 -> {
                // 2. Soviet Communal Kitchen
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        BlockState tile = ((lx + lz) % 2 == 0) ? Blocks.WHITE_TERRACOTTA.getDefaultState() : Blocks.LIGHT_GRAY_TERRACOTTA.getDefaultState();
                        chunk.setBlockState(mutable.set(lx, baseY, lz), tile, false);
                    }
                }
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), ModBlocks.GAS_STOVE.getDefaultState().with(GasStoveBlock.FACING, Direction.SOUTH), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), Blocks.IRON_BARS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, minZ), ModBlocks.RUSTY_SINK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.BARREL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, midZ), Blocks.CRAFTING_TABLE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), ModBlocks.CAST_IRON_RADIATOR.getDefaultState().with(CastIronRadiatorBlock.FACING, Direction.NORTH), false);
            }
            case 2 -> {
                // 3. Survivor Fortified Bunker
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.PACKED_MUD.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.COBWEB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ), Blocks.HAY_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ + 1), Blocks.HAY_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.BARREL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), ModBlocks.FOOD_DISPENSER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ), Blocks.IRON_BARS.getDefaultState(), false);
            }
            case 3 -> {
                // 4. Liquidator Welded Quarantine - Doorway is WELDED SHUT!
                chunk.setBlockState(mutable.set(doorX, baseY + 1, doorZ), Blocks.IRON_BARS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(doorX, baseY + 2, doorZ), Blocks.IRON_BARS.getDefaultState(), false);

                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.BLACKSTONE.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY, midZ), Blocks.MAGMA_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BASALT.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.POLISHED_BLACKSTONE.getDefaultState(), false);
            }
            case 4 -> {
                // 5. Illegal Moonshine Still & Fungi Farm
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.MUD.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.CAULDRON.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.BREWING_STAND.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), ModBlocks.TOXIC_MUSHROOM.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY, minZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.BARREL.getDefaultState(), false);
            }
            case 5 -> {
                // 6. Mad Radio Ham Numbers Station
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_DEEPSLATE.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.RADIO_RECEIVER.getDefaultState().with(RadioReceiverBlock.FACING, Direction.EAST), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.LIGHTNING_ROD.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.CHISELED_BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.HOPPER.getDefaultState(), false);
            }
            case 6 -> {
                // 7. Walled-off Phantom Room - Doorway is BRICKED SHUT!
                chunk.setBlockState(mutable.set(doorX, baseY + 1, doorZ), Blocks.STONE_BRICKS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(doorX, baseY + 2, doorZ), Blocks.STONE_BRICKS.getDefaultState(), false);

                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.STONE_BRICKS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.CHEST.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.COBWEB.getDefaultState(), false);
            }
            case 7 -> {
                // 8. Creepy Abandoned Creche / Kindergarten
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.STRIPPED_BIRCH_WOOD.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.IRON_BARS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, minZ), Blocks.WHITE_BED.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.CYAN_WOOL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.FLOWER_POT.getDefaultState(), false);
            }
            case 8 -> {
                // 9. Biomass Cult Shrine
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.CRIMSON_NYLIUM.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.TOXIC_MUSHROOM.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.RED_CARPET.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.RED_CARPET.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), ModBlocks.EMERGENCY_LAMP.getDefaultState(), false);
            }
            case 9 -> {
                // 10. Electrical Maintenance Plenum
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.OBSERVER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.ANVIL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.COPPER_BULB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.BARREL.getDefaultState(), false);
            }
            case 10 -> {
                // 11. Party Committee Archive
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_BLACKSTONE.getDefaultState(), false);
                    }
                }
                for (int lx = minX; lx <= maxX; lx++) {
                    chunk.setBlockState(mutable.set(lx, baseY + 1, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.POLISHED_ANDESITE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.HEAVY_CORE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.RED_CARPET.getDefaultState(), false);
            }
            case 11 -> {
                // 12. Looted Abandoned Flat
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.COBWEB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.OAK_PLANKS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.BARREL.getDefaultState(), false);
            }
        }
    }

    private void generateIndustrialHall(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
            chunk.setBlockState(mutable.set(midX, y, midZ), ModBlocks.P46_PANEL.getDefaultState(), false);
        }
        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BLAST_FURNACE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ + 1), Blocks.ANVIL.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.HOPPER.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), ModBlocks.FOOD_DISPENSER.getDefaultState(), false);
        chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.BARREL.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 4, minZ), Blocks.CHAIN.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 4, maxZ), Blocks.CHAIN.getDefaultState(), false);
    }

    private void generateLaboratory(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.TOXIC_MUSHROOM.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.TINTED_GLASS.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), Blocks.TINTED_GLASS.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 1, midZ + 1), Blocks.TINTED_GLASS.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 1, midZ - 1), Blocks.TINTED_GLASS.getDefaultState(), false);

        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BREWING_STAND.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.CAULDRON.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.CHEST.getDefaultState(), false);
    }

    private void generateAdministration(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        for (int lx = minX; lx <= maxX; lx++) {
            chunk.setBlockState(mutable.set(lx, baseY + 1, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
            chunk.setBlockState(mutable.set(lx, baseY + 2, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
        }

        chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.POLISHED_ANDESITE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.HEAVY_CORE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 1, maxZ), Blocks.CHISELED_BOOKSHELF.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.BARREL.getDefaultState(), false);
    }

    private void generateFloodedTechnical(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        for (int lx = minX; lx <= maxX; lx++) {
            for (int lz = minZ; lz <= maxZ; lz++) {
                if ((lx + lz) % 2 == 0) {
                    chunk.setBlockState(mutable.set(lx, baseY + 1, lz), Blocks.WATER.getDefaultState(), false);
                }
            }
        }

        chunk.setBlockState(mutable.set(midX, baseY + 4, midZ), Blocks.COPPER_GRATE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 4, minZ), Blocks.COPPER_BLOCK.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 4, maxZ), Blocks.COPPER_BLOCK.getDefaultState(), false);

        chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), Blocks.COBWEB.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.CAULDRON.getDefaultState(), false);
    }

    private void generatePurgedZone(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

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
