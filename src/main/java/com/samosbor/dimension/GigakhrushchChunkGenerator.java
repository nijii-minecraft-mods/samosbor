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
import com.samosbor.block.NoticeBoardBlock;
import com.samosbor.block.MedkitBoxBlock;
import com.samosbor.block.EmergencySirenBlock;
import com.samosbor.loot.ModLootTables;
import com.samosbor.item.ModItems;
import com.samosbor.dimension.GigakhrushchLayout.FloorArchetype;
import com.samosbor.dimension.GigakhrushchLayout.CourtyardType;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
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

    public static final int FLOOR_HEIGHT = GigakhrushchLayout.FLOOR_HEIGHT;
    public static final int MAX_HEIGHT = GigakhrushchLayout.MAX_HEIGHT;
    public static final int TOTAL_FLOORS = GigakhrushchLayout.TOTAL_FLOORS;
    public static final int[] TIER_BOUNDARIES = GigakhrushchLayout.TIER_BOUNDARIES;

    public static int getStairTierForFloor(int floor) {
        return GigakhrushchLayout.getStairTierForFloor(floor);
    }

    public static boolean isBoundaryFloor(int floor) {
        return GigakhrushchLayout.isBoundaryFloor(floor);
    }

    public static boolean isCourtyardChunk(int chunkX, int chunkZ) {
        return GigakhrushchLayout.isCourtyardChunk(chunkX, chunkZ);
    }

    public static boolean isMetroLineChunk(int chunkZ) {
        return GigakhrushchLayout.isMetroLineChunk(chunkZ);
    }

    public static boolean isMetroFloor(int floor) {
        return GigakhrushchLayout.isMetroFloor(floor);
    }

    public static boolean isMetroStationChunk(int chunkX, int chunkZ) {
        return GigakhrushchLayout.isMetroStationChunk(chunkX, chunkZ);
    }

    public static boolean isStairForTier(int chunkX, int chunkZ, int tier) {
        return GigakhrushchLayout.isStairForTier(chunkX, chunkZ, tier);
    }

    public static boolean isStairChunkForFloor(int chunkX, int chunkZ, int floor) {
        return GigakhrushchLayout.isStairChunkForFloor(chunkX, chunkZ, floor);
    }

    public static boolean isElevatorChunk(int chunkX, int chunkZ) {
        return GigakhrushchLayout.isElevatorChunk(chunkX, chunkZ);
    }

    public static GigakhrushchLayout.CourtyardType getCourtyardType(int chunkX, int chunkZ) {
        return GigakhrushchLayout.getCourtyardType(chunkX, chunkZ);
    }

    public static GigakhrushchLayout.FloorArchetype getFloorArchetype(int floor) {
        return GigakhrushchLayout.getFloorArchetype(floor);
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
        boolean isMetroLine = isMetroLineChunk(chunkZ);

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

            int tier = getStairTierForFloor(floor);
            boolean isCurrentTier = isStairForTier(chunkX, chunkZ, tier);
            boolean isPrevTierTop = isBoundaryFloor(floor) && isStairForTier(chunkX, chunkZ, tier - 1);
            boolean isStair = isCurrentTier || isPrevTierTop;
            boolean isMetroTransit = isMetroFloor(floor) && isMetroLine;

            if (isCourtyard) {
                // Vertical courtyard-well lightwell (variable geometry, window entropy, balconies, metro skybridge)
                generateCourtyardChunk(chunk, mutable, chunkX, chunkZ, baseY, floor, p46, air, ironBars, chains, emergLamp);
                continue;
            }

            // 1. Generate floor slab at baseY
            boolean isBottomLanding = (floor == 1) || (isCurrentTier && isBoundaryFloor(floor));
            generateFloorSlab(chunk, mutable, baseY, floor, archetype, isStair, isBottomLanding, isElevator, isMetroTransit, p46, air);

            if (isMetroTransit) {
                // Procedural Metro Station or Tunnel Artery
                generateMetroSegment(chunk, mutable, chunkX, chunkZ, baseY, floor, p46, air, ironBars, chains, emergLamp, fluorLamp, floorSeed);
                // Connect approach corridors at lx=7..8
                for (int lx = 7; lx <= 8; lx++) {
                    for (int lz = 0; lz <= 2; lz++) {
                        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                        }
                    }
                    for (int lz = 13; lz <= 15; lz++) {
                        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                        }
                    }
                }
            } else {
                // 2. Generate corridors and perimeter walls
                generateCorridorsAndWalls(chunk, mutable, baseY, floor, archetype, p46, painted, air, fluorLamp, emergLamp,
                        hermeticDoorLowerN, hermeticDoorUpperN, hermeticDoorLowerS, hermeticDoorUpperS,
                        hermeticDoorLowerE, hermeticDoorUpperE, hermeticDoorLowerW, hermeticDoorUpperW);

                // 3. Generate Segmented Stairwell in Quadrant 1 (lx in 1..6, lz in 1..6)
                if (isStair) {
                    generateSegmentedStairwell(chunk, mutable, baseY, floor, isCurrentTier, isPrevTierTop, p46, painted, air, ironBars, emergLamp);
                }

                // 4. Generate Elevator Shaft & Cabin in Quadrant 4 (lx in 10..14, lz in 10..14)
                if (isElevator) {
                    generateElevatorShaft(chunk, mutable, baseY, floor, p46, air, chains, ironBars, elevatorDoorLower, elevatorDoorUpper, emergLamp);
                }

                // 5. Generate Room Interiors in remaining quadrants with rich 24-variant catalog
                generateRoomQuadrants(chunk, mutable, baseY, floor, floorSeed, archetype, isStair, isElevator, p46, air);
            }
        }
    }

    private void generateCourtyardChunk(Chunk chunk, BlockPos.Mutable mutable, int chunkX, int chunkZ, int baseY, int floor,
                                        BlockState p46, BlockState air, BlockState ironBars, BlockState chains, BlockState emergLamp) {
        CourtyardType cType = getCourtyardType(chunkX, chunkZ);

        int minVoid, maxVoid;
        switch (cType) {
            case LIGHTWELL_4X4 -> { minVoid = 6; maxVoid = 9; }
            case CHASM_12X12   -> { minVoid = 2; maxVoid = 13; }
            case STEPPED_TERRACE -> {
                if (floor < 50) {
                    minVoid = 6; maxVoid = 9;   // Lower tiers: 4x4 narrow vent shaft
                } else if (floor < 110) {
                    minVoid = 4; maxVoid = 11;  // Mid tiers: 8x8 standard well
                } else {
                    minVoid = 2; maxVoid = 13;  // Upper tiers: 12x12 colossal abyss
                }
            }
            default -> { minVoid = 4; maxVoid = 11; }
        }

        int facadeMin = minVoid - 1;
        int facadeMax = maxVoid + 1;
        int centerCoord = (minVoid + maxVoid) / 2;

        long courtyardSeed = ((long) chunkX * 73856093L) ^ ((long) chunkZ * 19349663L);
        int netInterval = 8 + Math.floorMod(courtyardSeed >> 3, 9); // between 8 and 16 floors
        int catwalkInterval = 14 + Math.floorMod(courtyardSeed >> 7, 13); // between 14 and 26 floors
        int clothesInterval = 3 + Math.floorMod(courtyardSeed >> 11, 3); // between 3 and 5 floors

        boolean hasCatchNet = (floor % netInterval == 0);
        boolean hasCatwalk = (floor % catwalkInterval == 0) && (cType != CourtyardType.CHASM_12X12);
        boolean hasClothesLine = (floor % clothesInterval == 0);
        boolean isMetroBridge = isMetroFloor(floor) && isMetroLineChunk(chunkZ);

        // 1. Floor slab at baseY:
        // Any block outside minVoid..maxVoid is solid P46
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                boolean isVoid = (lx >= minVoid && lx <= maxVoid && lz >= minVoid && lz <= maxVoid);
                if (isVoid) {
                    if (isMetroBridge && (lz >= 6 && lz <= 9)) {
                        // Metro sky-bridge base
                        if (lz == 7 || lz == 8) {
                            chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.REDSTONE_BLOCK.getDefaultState(), false);
                        } else {
                            chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.STONE_BRICKS.getDefaultState(), false);
                        }
                    } else if (hasCatchNet) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.COBWEB.getDefaultState(), false);
                        if (lx == centerCoord && lz == centerCoord) {
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), ModBlocks.OLD_TV.getDefaultState(), false);
                        } else if (lx == minVoid + 1 && lz == maxVoid - 1) {
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

        // 2. Facade walls and courtyard interior
        for (int lx = facadeMin; lx <= facadeMax; lx++) {
            for (int lz = facadeMin; lz <= facadeMax; lz++) {
                boolean isFacade = (lx == facadeMin || lx == facadeMax || lz == facadeMin || lz == facadeMax);
                if (isFacade) {
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        // Catwalk access doorway
                        if (hasCatwalk && lz == centerCoord && (lx == facadeMin || lx == facadeMax) && (y == baseY + 1 || y == baseY + 2)) {
                            chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                            continue;
                        }

                        // Metro sky-bridge portal
                        if (isMetroBridge && (lz >= 6 && lz <= 9) && (lx == facadeMin || lx == facadeMax)) {
                            chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                            continue;
                        }

                        // Window placement with entropy
                        boolean isWindowPos = (y == baseY + 2 || y == baseY + 3) && ((lx % 3 == 0) || (lz % 3 == 0));
                        if (isWindowPos) {
                            int windowSeed = lx * 31 + lz * 17 + floor * 7;
                            BlockState windowBlock = switch (Math.floorMod(windowSeed, 6)) {
                                case 0 -> Blocks.OAK_PLANKS.getDefaultState();
                                case 1 -> ironBars;
                                case 2 -> Blocks.COBWEB.getDefaultState();
                                default -> Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.getDefaultState();
                            };
                            chunk.setBlockState(mutable.set(lx, y, lz), windowBlock, false);
                        } else {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    }

                    // External air conditioning units BK-1500 under windows
                    if ((lx + lz + floor) % 8 == 0) {
                        int acX = (lx == facadeMin) ? lx - 1 : ((lx == facadeMax) ? lx + 1 : lx);
                        int acZ = (lz == facadeMin) ? lz - 1 : ((lz == facadeMax) ? lz + 1 : lz);
                        if (acX >= 0 && acX < 16 && acZ >= 0 && acZ < 16) {
                            chunk.setBlockState(mutable.set(acX, baseY + 1, acZ), Blocks.DROPPER.getDefaultState(), false);
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

        // 3. Sky-Bridge for Metro line crossing courtyard
        if (isMetroBridge) {
            for (int lx = minVoid; lx <= maxVoid; lx++) {
                chunk.setBlockState(mutable.set(lx, baseY + 1, 7), Blocks.POWERED_RAIL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(lx, baseY + 1, 8), Blocks.POWERED_RAIL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(lx, baseY + 1, 6), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(lx, baseY + 1, 9), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(lx, baseY + 2, 6), ironBars, false);
                chunk.setBlockState(mutable.set(lx, baseY + 2, 9), ironBars, false);
                chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, 6), p46, false);
                chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, 7), p46, false);
                chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, 8), p46, false);
                chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, 9), p46, false);
            }
        }

        // 4. Catwalk bridge
        if (hasCatwalk && !isMetroBridge) {
            for (int lx = minVoid; lx <= maxVoid; lx++) {
                chunk.setBlockState(mutable.set(lx, baseY + 1, centerCoord), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(lx, baseY + 2, centerCoord - 1), ironBars, false);
                chunk.setBlockState(mutable.set(lx, baseY + 2, centerCoord + 1), ironBars, false);
            }
        }

        // 5. Cantilevered balconies protruding into the void
        if (floor % 7 == 0 && cType != CourtyardType.LIGHTWELL_4X4 && !isMetroBridge) {
            chunk.setBlockState(mutable.set(minVoid, baseY + 1, centerCoord), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
            chunk.setBlockState(mutable.set(minVoid, baseY + 2, centerCoord), Blocks.FLOWER_POT.getDefaultState(), false);
            chunk.setBlockState(mutable.set(minVoid, baseY + 2, centerCoord - 1), ironBars, false);
            chunk.setBlockState(mutable.set(minVoid, baseY + 2, centerCoord + 1), ironBars, false);
        }

        // 6. Clotheslines
        if (hasClothesLine && !isMetroBridge) {
            int lineZ = minVoid + 1 + Math.floorMod(floor, Math.max(1, maxVoid - minVoid - 1));
            for (int lx = minVoid; lx <= maxVoid; lx++) {
                chunk.setBlockState(mutable.set(lx, baseY + 3, lineZ), chains, false);
                if (lx % 3 == 0) {
                    chunk.setBlockState(mutable.set(lx, baseY + 2, lineZ), Blocks.WHITE_CARPET.getDefaultState(), false);
                }
            }
        }

        // 7. Surrounding perimeter corridors
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                if (lx < facadeMin || lx > facadeMax || lz < facadeMin || lz > facadeMax) {
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
                                  boolean isStair, boolean isBottomLanding, boolean isElevatorChunk, boolean isMetroTransit, BlockState p46, BlockState air) {
        BlockState floorState = switch (archetype) {
            case PURGED -> Blocks.BLACKSTONE.getDefaultState();
            case LABORATORY -> Blocks.WHITE_CONCRETE.getDefaultState();
            case ADMINISTRATION -> Blocks.POLISHED_ANDESITE.getDefaultState();
            case FLOODED_TECHNICAL -> Blocks.MOSSY_STONE_BRICKS.getDefaultState();
            default -> p46;
        };

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                if (isMetroTransit) {
                    if (lz == 7 || lz == 8) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.REDSTONE_BLOCK.getDefaultState(), false);
                    } else if (lz == 6 || lz == 9) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.GRAVEL.getDefaultState(), false);
                    } else {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_ANDESITE.getDefaultState(), false);
                    }
                    continue;
                }

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

    private void generateMetroSegment(Chunk chunk, BlockPos.Mutable mutable, int chunkX, int chunkZ, int baseY, int floor,
                                      BlockState p46, BlockState air, BlockState ironBars, BlockState chains, BlockState emergLamp,
                                      BlockState fluorLamp, long seed) {
        boolean isStation = isMetroStationChunk(chunkX, chunkZ);
        BlockState stoneBricks = Blocks.STONE_BRICKS.getDefaultState();
        BlockState chiseledStone = Blocks.CHISELED_STONE_BRICKS.getDefaultState();
        BlockState quartz = Blocks.SMOOTH_QUARTZ.getDefaultState();
        BlockState andesite = Blocks.POLISHED_ANDESITE.getDefaultState();
        BlockState redstoneBlock = Blocks.REDSTONE_BLOCK.getDefaultState();
        BlockState poweredRail = Blocks.POWERED_RAIL.getDefaultState();
        BlockState yellowTerracotta = Blocks.YELLOW_TERRACOTTA.getDefaultState();
        BlockState blackConcrete = Blocks.BLACK_CONCRETE.getDefaultState();
        BlockState ironBlock = Blocks.IRON_BLOCK.getDefaultState();

        if (isStation) {
            // === SOVIET METRO STATION («Сороконожка») ===
            // 1. Excavate station hall from lx=0..15, lz=3..12
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 3; lz <= 12; lz++) {
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }
                }
            }

            // 2. Track bed in center (lz=7..8)
            for (int lx = 0; lx < 16; lx++) {
                chunk.setBlockState(mutable.set(lx, baseY, 7), redstoneBlock, false);
                chunk.setBlockState(mutable.set(lx, baseY, 8), redstoneBlock, false);
                chunk.setBlockState(mutable.set(lx, baseY + 1, 7), poweredRail, false);
                chunk.setBlockState(mutable.set(lx, baseY + 1, 8), poweredRail, false);
            }

            // 3. Station Platforms (North lz=4..6, South lz=9..11) at baseY + 1
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 4; lz <= 6; lz++) {
                    BlockState plat = (lz == 6) ? yellowTerracotta : (((lx + lz) % 2 == 0) ? quartz : andesite);
                    chunk.setBlockState(mutable.set(lx, baseY + 1, lz), plat, false);
                }
                for (int lz = 9; lz <= 11; lz++) {
                    BlockState plat = (lz == 9) ? yellowTerracotta : (((lx + lz) % 2 == 0) ? quartz : andesite);
                    chunk.setBlockState(mutable.set(lx, baseY + 1, lz), plat, false);
                }
            }

            // 4. Station Pillars & Ceiling Vault
            for (int lx = 0; lx < 16; lx++) {
                // Vaulted ceiling across lz=3..12
                for (int lz = 3; lz <= 12; lz++) {
                    chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, lz), stoneBricks, false);
                }
                // Pillars
                if (lx == 2 || lx == 6 || lx == 10 || lx == 14) {
                    for (int y = baseY + 2; y < baseY + FLOOR_HEIGHT - 1; y++) {
                        chunk.setBlockState(mutable.set(lx, y, 4), Blocks.QUARTZ_PILLAR.getDefaultState(), false);
                        chunk.setBlockState(mutable.set(lx, y, 11), Blocks.QUARTZ_PILLAR.getDefaultState(), false);
                    }
                }
                // Platform lighting
                if (lx % 4 == 2) {
                    chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, 5), fluorLamp, false);
                    chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, 10), fluorLamp, false);
                }
            }

            // 5. Waiting Benches
            BlockState benchNorth = Blocks.POLISHED_DEEPSLATE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
            BlockState benchSouth = Blocks.POLISHED_DEEPSLATE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
            chunk.setBlockState(mutable.set(4, baseY + 2, 5), benchNorth, false);
            chunk.setBlockState(mutable.set(11, baseY + 2, 5), benchNorth, false);
            chunk.setBlockState(mutable.set(4, baseY + 2, 10), benchSouth, false);
            chunk.setBlockState(mutable.set(11, baseY + 2, 10), benchSouth, false);

            // 6. Station Signs with Lore names
            int stationId = Math.floorMod(chunkX / 4, 6);
            String stationName = switch (stationId) {
                case 0 -> "«ПИЩКОНЦЕНТРАТНАЯ»";
                case 1 -> "«БЛОК-404»";
                case 2 -> "«ОЧИСТНАЯ»";
                case 3 -> "«МЕТРОСТРОЙ-СЕВЕР»";
                case 4 -> "«НИИ-ХИММАШ»";
                default -> "«ЗАВОДСКАЯ»";
            };
            placeWallNotice(chunk, mutable.set(8, baseY + 3, 3), Direction.SOUTH, "§fСТАНЦИЯ", "§6" + stationName, "§0Линия ГО-Ликвидация", "§0Посадка на дрезины", DyeColor.ORANGE);
            placeWallNotice(chunk, mutable.set(8, baseY + 3, 12), Direction.NORTH, "§fСТАНЦИЯ", "§6" + stationName, "§0Линия ГО-Ликвидация", "§0Посадка на дрезины", DyeColor.ORANGE);

            // 7. Station Supply Chest (Minecarts, Rails, Torches, Coal, Bread)
            placeStationChest(chunk, mutable.set(8, baseY + 2, 5));

            // 8. Station Hermetic Blast Gates (Гермоворота В-1) at North and South entrances
            // North portal at lx=7..8, lz=3
            chunk.setBlockState(mutable.set(6, baseY + 2, 3), yellowTerracotta, false);
            chunk.setBlockState(mutable.set(9, baseY + 2, 3), blackConcrete, false);
            chunk.setBlockState(mutable.set(6, baseY + 3, 3), blackConcrete, false);
            chunk.setBlockState(mutable.set(9, baseY + 3, 3), yellowTerracotta, false);
            chunk.setBlockState(mutable.set(7, baseY + 4, 3), ironBlock, false);
            chunk.setBlockState(mutable.set(8, baseY + 4, 3), ironBlock, false);
            chunk.setBlockState(mutable.set(7, baseY + 5, 3), chains, false);
            chunk.setBlockState(mutable.set(8, baseY + 5, 3), chains, false);
            chunk.setBlockState(mutable.set(6, baseY + 4, 3), Blocks.GRINDSTONE.getDefaultState(), false);
            chunk.setBlockState(mutable.set(7, baseY + 2, 3), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH), false);
            chunk.setBlockState(mutable.set(7, baseY + 3, 3), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH), false);
            chunk.setBlockState(mutable.set(8, baseY + 2, 3), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH), false);
            chunk.setBlockState(mutable.set(8, baseY + 3, 3), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH), false);

            // South portal at lx=7..8, lz=12
            chunk.setBlockState(mutable.set(6, baseY + 2, 12), yellowTerracotta, false);
            chunk.setBlockState(mutable.set(9, baseY + 2, 12), blackConcrete, false);
            chunk.setBlockState(mutable.set(6, baseY + 3, 12), blackConcrete, false);
            chunk.setBlockState(mutable.set(9, baseY + 3, 12), yellowTerracotta, false);
            chunk.setBlockState(mutable.set(7, baseY + 4, 12), ironBlock, false);
            chunk.setBlockState(mutable.set(8, baseY + 4, 12), ironBlock, false);
            chunk.setBlockState(mutable.set(7, baseY + 5, 12), chains, false);
            chunk.setBlockState(mutable.set(8, baseY + 5, 12), chains, false);
            chunk.setBlockState(mutable.set(6, baseY + 4, 12), Blocks.GRINDSTONE.getDefaultState(), false);
            chunk.setBlockState(mutable.set(7, baseY + 2, 12), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.NORTH), false);
            chunk.setBlockState(mutable.set(7, baseY + 3, 12), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.NORTH), false);
            chunk.setBlockState(mutable.set(8, baseY + 2, 12), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.NORTH), false);
            chunk.setBlockState(mutable.set(8, baseY + 3, 12), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.NORTH), false);

            // Wall enclosures at lz=3 and lz=12
            for (int lx = 0; lx < 16; lx++) {
                if (lx < 6 || lx > 9) {
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                        chunk.setBlockState(mutable.set(lx, y, 3), p46, false);
                        chunk.setBlockState(mutable.set(lx, y, 12), p46, false);
                    }
                }
            }
        } else {
            // === PROCEDURAL RAILWAY TUNNEL SEGMENT ===
            // 1. Excavate tunnel lx=0..15, lz=5..10
            for (int lx = 0; lx < 16; lx++) {
                for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                    chunk.setBlockState(mutable.set(lx, y, 5), (lx % 4 == 0) ? chiseledStone : stoneBricks, false);
                    chunk.setBlockState(mutable.set(lx, y, 10), (lx % 4 == 0) ? chiseledStone : stoneBricks, false);
                }
                for (int lz = 6; lz <= 9; lz++) {
                    for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT - 1; y++) {
                        chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                    }
                }
                for (int lz = 5; lz <= 10; lz++) {
                    chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, lz), stoneBricks, false);
                }

                // Walkways & Powered Rails
                chunk.setBlockState(mutable.set(lx, baseY + 1, 6), andesite, false);
                chunk.setBlockState(mutable.set(lx, baseY + 1, 9), andesite, false);
                chunk.setBlockState(mutable.set(lx, baseY, 7), redstoneBlock, false);
                chunk.setBlockState(mutable.set(lx, baseY, 8), redstoneBlock, false);
                chunk.setBlockState(mutable.set(lx, baseY + 1, 7), poweredRail, false);
                chunk.setBlockState(mutable.set(lx, baseY + 1, 8), poweredRail, false);

                // Hanging cables & emergency lamps
                if (lx % 4 == 0) {
                    chunk.setBlockState(mutable.set(lx, baseY + 4, 7), chains, false);
                    chunk.setBlockState(mutable.set(lx, baseY + 3, 7), emergLamp, false);
                }
            }

            // Maintenance alcove at lx=8, lz=5
            chunk.setBlockState(mutable.set(8, baseY + 1, 5), air, false);
            chunk.setBlockState(mutable.set(8, baseY + 2, 5), air, false);
            chunk.setBlockState(mutable.set(8, baseY + 1, 4), air, false);
            chunk.setBlockState(mutable.set(8, baseY + 2, 4), air, false);
            placeBarrel(chunk, mutable.set(8, baseY + 1, 4), ModLootTables.CIVIL_DEFENSE_DEPOT, seed);
            chunk.setBlockState(mutable.set(8, baseY + 3, 4), emergLamp, false);

            // Tunnel Access Door to Sector Corridor at lx=7, lz=10
            chunk.setBlockState(mutable.set(7, baseY + 1, 10), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.NORTH), false);
            chunk.setBlockState(mutable.set(7, baseY + 2, 10), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.NORTH), false);
        }
    }

    private void placeStationChest(Chunk chunk, BlockPos pos) {
        chunk.setBlockState(pos, Blocks.CHEST.getDefaultState(), false);
        ChestBlockEntity chest = new ChestBlockEntity(pos.toImmutable(), Blocks.CHEST.getDefaultState());
        chest.setStack(0, new ItemStack(Items.MINECART, 2));
        chest.setStack(1, new ItemStack(Items.CHEST_MINECART, 1));
        chest.setStack(2, new ItemStack(Items.POWERED_RAIL, 16));
        chest.setStack(3, new ItemStack(Items.COAL, 16));
        chest.setStack(4, new ItemStack(Items.TORCH, 16));
        chest.setStack(5, new ItemStack(ModItems.HARDTACK, 16));
        chest.setStack(6, new ItemStack(ModItems.CANNED_STEW, 2));
        chest.setStack(7, new ItemStack(ModItems.BLUE_TAPE, 1));
        chunk.setBlockEntity(chest);
    }

    private void generateCorridorsAndWalls(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, FloorArchetype archetype,
                                           BlockState p46, BlockState painted, BlockState air, BlockState fluorLamp, BlockState emergLamp,
                                           BlockState doorLN, BlockState doorUN, BlockState doorLS, BlockState doorUS,
                                           BlockState doorLE, BlockState doorUE, BlockState doorLW, BlockState doorUW) {
        // Pass 1: Carve all corridor open space, floor dressings and ceiling lamps
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                boolean isCorridorX = (lx == 7 || lx == 8);
                boolean isCorridorZ = (lz == 7 || lz == 8);
                if (isCorridorX || isCorridorZ) {
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
            }
        }

        // Pass 2: Generate corridor boundary walls, doors, chutes, and notices
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                boolean isCorridorX = (lx == 7 || lx == 8);
                boolean isCorridorZ = (lz == 7 || lz == 8);
                boolean isCorridor = isCorridorX || isCorridorZ;

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
                        boolean isNoticeWall = (isWallX && lz == 4) || (isWallZ && lx == 11);
                        if (isFireCabinet && floor % 3 == 0) {
                            chunk.setBlockState(mutable.set(lx, baseY + 2, lz), Blocks.RED_TERRACOTTA.getDefaultState(), false);
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), painted, false);
                        } else {
                            BlockState lowerWall = (archetype == FloorArchetype.PURGED) ? Blocks.POLISHED_BLACKSTONE.getDefaultState() : painted;
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), lowerWall, false);
                            chunk.setBlockState(mutable.set(lx, baseY + 2, lz), lowerWall, false);

                            if (isNoticeWall) {
                                int signX = isWallX ? (lx == 6 ? 7 : 8) : lx;
                                int signZ = isWallZ ? (lz == 6 ? 7 : 8) : lz;
                                Direction facing = isWallX ? (lx == 6 ? Direction.EAST : Direction.WEST) : (lz == 6 ? Direction.SOUTH : Direction.NORTH);
                                if (floor % 4 == 0) {
                                    chunk.setBlockState(mutable.set(signX, baseY + 2, signZ), ModBlocks.NOTICE_BOARD.getDefaultState().with(NoticeBoardBlock.FACING, facing), false);
                                } else if (floor % 4 == 2) {
                                    chunk.setBlockState(mutable.set(signX, baseY + 2, signZ), ModBlocks.MEDKIT_BOX.getDefaultState().with(MedkitBoxBlock.FACING, facing), false);
                                } else {
                                    int noticeType = Math.floorMod(floor / 2 + (lx + lz), 6);
                                    switch (noticeType) {
                                        case 0 -> placeWallNotice(chunk, mutable.set(signX, baseY + 2, signZ), facing, "§4ВНИМАНИЕ!", "§0САМОСБОР БЛИЗКО", "§0Задраить двери", "§0по сигналу ГО!", DyeColor.RED);
                                        case 1 -> placeWallNotice(chunk, mutable.set(signX, baseY + 2, signZ), facing, "§1ОБЪЯВЛЕНИЕ", "§0Выдача концентрата", "§008:00 - 10:00", "§0Строго по талонам", DyeColor.BLUE);
                                        case 2 -> placeWallNotice(chunk, mutable.set(signX, baseY + 2, signZ), facing, "§4ОПАСНАЯ ЗОНА", "§0Пломба Очистки", "§0Вход воспрещён!", "§0Штраф: распыл", DyeColor.RED);
                                        case 3 -> placeWallNotice(chunk, mutable.set(signX, baseY + 2, signZ), facing, "§2ГРАФИК ДЕЖУРСТВ", "§0Мытьё пола: кв.1-4", "§0В мусоропровод", "§0не лезть!", DyeColor.GREEN);
                                        case 4 -> placeWallNotice(chunk, mutable.set(signX, baseY + 2, signZ), facing, "§4НЕ ВЕРЬ ШЁПОТУ", "§0Они копируют голос", "§0Дверь не открывать", "§4ДАЖЕ БЛИЗКИМ", DyeColor.PURPLE);
                                        default -> placeWallNotice(chunk, mutable.set(signX, baseY + 2, signZ), facing, "§8СЛАВА ПАРТИИ!", "§0План по бетону 102%", "§0Берегите ГП-5", "§0Сектор жилой", DyeColor.BLACK);
                                    }
                                }
                            }
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

            int endedTier = GigakhrushchLayout.getStairTierForFloor(floor) - 1;
            boolean isShort = GigakhrushchLayout.isShortFlight(endedTier);

            if (isShort) {
                // Collapsed flight rubble cone, broken masonry and exposed rebar
                chunk.setBlockState(mutable.set(2, baseY + 1, 2), Blocks.TUFF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(3, baseY + 1, 2), Blocks.COBBLESTONE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(2, baseY + 1, 3), Blocks.CRACKED_STONE_BRICKS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(3, baseY + 2, 2), Blocks.TUFF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(4, baseY + 1, 3), ironBars, false);
                chunk.setBlockState(mutable.set(4, baseY + 2, 3), ironBars, false);
                placeWallNotice(chunk, mutable.set(3, baseY + 2, 1), Direction.SOUTH, "§4[ВНИМАНИЕ]", "§0ПРОЛЁТ ОБРУШЕН", "§0ВЫХОД В КОРИДОР", "§4НА ЭТАЖ " + floor, DyeColor.RED);
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

    private void placeChest(Chunk chunk, BlockPos pos, RegistryKey<LootTable> lootTable, long seed) {
        chunk.setBlockState(pos, Blocks.CHEST.getDefaultState(), false);
        ChestBlockEntity chest = new ChestBlockEntity(pos.toImmutable(), Blocks.CHEST.getDefaultState());
        chest.setLootTable(lootTable);
        chest.setLootTableSeed(seed);
        chunk.setBlockEntity(chest);
    }

    private void placeBarrel(Chunk chunk, BlockPos pos, RegistryKey<LootTable> lootTable, long seed) {
        chunk.setBlockState(pos, Blocks.BARREL.getDefaultState(), false);
        BarrelBlockEntity barrel = new BarrelBlockEntity(pos.toImmutable(), Blocks.BARREL.getDefaultState());
        barrel.setLootTable(lootTable);
        barrel.setLootTableSeed(seed);
        chunk.setBlockEntity(barrel);
    }

    private void placeCustomLootChest(Chunk chunk, BlockPos pos, ItemStack... items) {
        chunk.setBlockState(pos, Blocks.CHEST.getDefaultState(), false);
        ChestBlockEntity chest = new ChestBlockEntity(pos.toImmutable(), Blocks.CHEST.getDefaultState());
        int slot = 0;
        for (ItemStack item : items) {
            if (slot < chest.size() && item != null && !item.isEmpty()) {
                chest.setStack(slot++, item);
            }
        }
        chunk.setBlockEntity(chest);
    }

    private void placeCustomLootBarrel(Chunk chunk, BlockPos pos, ItemStack... items) {
        chunk.setBlockState(pos, Blocks.BARREL.getDefaultState(), false);
        BarrelBlockEntity barrel = new BarrelBlockEntity(pos.toImmutable(), Blocks.BARREL.getDefaultState());
        int slot = 0;
        for (ItemStack item : items) {
            if (slot < barrel.size() && item != null && !item.isEmpty()) {
                barrel.setStack(slot++, item);
            }
        }
        chunk.setBlockEntity(barrel);
    }

    private void placeWallNotice(Chunk chunk, BlockPos pos, Direction facing, String l1, String l2, String l3, String l4, DyeColor color) {
        chunk.setBlockState(pos, Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, facing), false);
        SignBlockEntity sign = new SignBlockEntity(pos.toImmutable(), Blocks.OAK_WALL_SIGN.getDefaultState());
        SignText text = new SignText().withColor(color)
                .withMessage(0, Text.literal(l1))
                .withMessage(1, Text.literal(l2))
                .withMessage(2, Text.literal(l3))
                .withMessage(3, Text.literal(l4));
        sign.setText(text, true);
        chunk.setBlockEntity(sign);
    }

    private RegistryKey<LootTable> getDepthLootTable(int floor, RegistryKey<LootTable> defaultTable) {
        GigakhrushchLayout.FloorZone zone = GigakhrushchLayout.getFloorZone(floor);
        return switch (zone) {
            case DEEP_FOUNDATIONS -> ModLootTables.FLOODED_REACTOR;
            case LOWER_INDUSTRIAL -> ModLootTables.INDUSTRIAL_WORKSHOP;
            case CENTRAL_HABITATION -> defaultTable;
            case UPPER_RESEARCH -> ModLootTables.MEDICAL_STATION;
            case PARTY_STRATUM -> ModLootTables.PARTY_NOMENKLATURA;
        };
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

        int variant = Math.abs((int) (seed % 47));

        switch (archetype) {
            case RESIDENTIAL -> generateResidentialVariant(chunk, mutable, baseY, floor, variant, minX, maxX, minZ, maxZ, doorX, doorZ, seed);
            case INDUSTRIAL -> generateIndustrialHall(chunk, mutable, baseY, minX, maxX, minZ, maxZ, seed);
            case LABORATORY -> generateLaboratory(chunk, mutable, baseY, minX, maxX, minZ, maxZ, seed);
            case ADMINISTRATION -> generateAdministration(chunk, mutable, baseY, minX, maxX, minZ, maxZ, seed);
            case FLOODED_TECHNICAL -> generateFloodedTechnical(chunk, mutable, baseY, minX, maxX, minZ, maxZ, seed);
            case PURGED -> generatePurgedZone(chunk, mutable, baseY, minX, maxX, minZ, maxZ, seed);
        }
    }

    private void generateResidentialVariant(Chunk chunk, BlockPos.Mutable mutable, int baseY, int floor, int variant,
                                           int minX, int maxX, int minZ, int maxZ, int doorX, int doorZ, long seed) {
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
                chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), ModBlocks.NOTICE_BOARD.getDefaultState().with(NoticeBoardBlock.FACING, Direction.SOUTH), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.RED_WOOL.getDefaultState(), false);
                placeWallNotice(chunk, mutable.set(midX - 1, baseY + 2, minZ), Direction.SOUTH, "§8«МАЯК-РП»", "§0Частота ГО: 142.5", "§0Слушать тревогу", "§0при сирене!", DyeColor.BLACK);
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
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, minZ), ModLootTables.RESIDENTIAL_APARTMENT, seed);
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
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, minZ), ModLootTables.CIVIL_DEFENSE_DEPOT, seed);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), ModBlocks.FOOD_DISPENSER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ), Blocks.IRON_BARS.getDefaultState(), false);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4УБЕЖИЩЕ №9", "§0Пайка на 3 дня", "§0Фильтры на исходе", "§0Заварено изнутри", DyeColor.RED);
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
                placeChest(chunk, mutable.set(maxX, baseY + 1, minZ), ModLootTables.LIQUIDATOR_CACHE, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4ОПЕЧАТАНО", "§0Штурм-взвод №3", "§0Биомасса выжжена", "§0Вход воспрещён!", DyeColor.RED);
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
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, minZ), ModLootTables.MIMIC_NEST, seed);
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
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.INDUSTRIAL_WORKSHOP, seed);
                placeWallNotice(chunk, mutable.set(midX - 1, baseY + 2, minZ), Direction.SOUTH, "§5НОМЕРНАЯ СТАНЦИЯ", "§0УЗБ-76 «ЖУЖЖАЛКА»", "§04625 кГц", "§0МДЖБ 76 497", DyeColor.PURPLE);
            }
            case 6 -> {
                // 7. Walled-off Phantom Room - Doorway is BRICKED SHUT!
                chunk.setBlockState(mutable.set(doorX, baseY + 1, doorZ), Blocks.STONE_BRICKS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(doorX, baseY + 2, doorZ), Blocks.STONE_BRICKS.getDefaultState(), false);

                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.STONE_BRICKS.getDefaultState(), false);
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.RESIDENTIAL_APARTMENT, seed);
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
                chunk.setBlockState(mutable.set(minX + 2, baseY + 2, minZ), ModBlocks.MEDKIT_BOX.getDefaultState().with(MedkitBoxBlock.FACING, Direction.SOUTH), false);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§1ЯСЛИ №12", "§0«СОЛНЫШКО»", "§0Дети эвакуированы", "§0при Самосборе", DyeColor.BLUE);
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
                placeChest(chunk, mutable.set(minX, baseY + 1, maxZ), ModLootTables.MIMIC_NEST, seed);
                placeWallNotice(chunk, mutable.set(minX, baseY + 2, minZ), Direction.SOUTH, "§4ОНО ЗОВЁТ", "§4СВЯЩЕННАЯ ПЛОТЬ", "§4СЛИЯНИЕ НЕИЗБЕЖНО", "§4СЛАВА САМОСБОРУ", DyeColor.RED);
            }
            case 9 -> {
                // 10. Electrical Maintenance Plenum
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.OBSERVER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.ANVIL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.COPPER_BULB.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, minZ), ModLootTables.INDUSTRIAL_WORKSHOP, seed);
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
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.PARTY_NOMENKLATURA, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4ПАРТКОМ СЕКТОРА", "§0Приём граждан:", "§0Вт, Чт 14:00-16:00", "§0Талоны не выдаём!", DyeColor.RED);
            }
            case 11 -> {
                // 12. Looted Abandoned Flat
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.COBWEB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.OAK_PLANKS.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, minZ), ModLootTables.RESIDENTIAL_APARTMENT, seed);
            }
            case 12 -> {
                // 13. Sector Post Office (Почтовое отделение Сектора 14)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.SMOOTH_STONE.getDefaultState(), false);
                    }
                }
                for (int lz = minZ; lz <= maxZ; lz++) {
                    chunk.setBlockState(mutable.set(midX, baseY + 1, lz), Blocks.OAK_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, false), false);
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(minX, baseY + 1, minZ), ModLootTables.RESIDENTIAL_APARTMENT, seed);
                placeBarrel(chunk, mutable.set(minX, baseY + 2, minZ), ModLootTables.RESIDENTIAL_APARTMENT, seed + 1);
                placeChest(chunk, mutable.set(minX, baseY + 1, maxZ), ModLootTables.RESIDENTIAL_APARTMENT, seed);
                chunk.setBlockState(mutable.set(maxX, baseY + 2, midZ), ModBlocks.NOTICE_BOARD.getDefaultState().with(NoticeBoardBlock.FACING, Direction.WEST), false);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§1ПОЧТА СЕКТОРА 14", "§0Приём посылок: ЗАКРЫТ", "§0Письма не доходят", "§0с 1986 года", DyeColor.BLUE);
            }
            case 13 -> {
                // 14. Medical Isolation Ward (Медпункт / Изолятор)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.WHITE_CONCRETE.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.WHITE_BED.getDefaultState().with(BedBlock.FACING, Direction.SOUTH).with(BedBlock.PART, BedPart.FOOT), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ + 1), Blocks.WHITE_BED.getDefaultState().with(BedBlock.FACING, Direction.SOUTH).with(BedBlock.PART, BedPart.HEAD), false);
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, minZ), Blocks.IRON_BARS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX + 1, baseY + 2, minZ), Blocks.TRIPWIRE_HOOK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.CAULDRON.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, midZ), ModBlocks.MEDKIT_BOX.getDefaultState().with(MedkitBoxBlock.FACING, Direction.EAST), false);
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.MEDICAL_STATION, seed);
                placeWallNotice(chunk, mutable.set(maxX, baseY + 2, midZ), Direction.WEST, "§4ИЗОЛЯТОР №7-Б", "§0Карантин по спорам", "§0Вход в ОЗК", "§0Пациент №41 сбежал", DyeColor.RED);
            }
            case 14 -> {
                // 15. Sector Canteen (Столовая общепита №19)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_GRANITE.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ), ModBlocks.FOOD_DISPENSER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ + 1), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ + 2), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(minX, baseY + 1, minZ), ModLootTables.RESIDENTIAL_APARTMENT, seed);
                placeBarrel(chunk, mutable.set(minX, baseY + 1, minZ + 1), ModLootTables.RESIDENTIAL_APARTMENT, seed);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, midZ), Blocks.OAK_FENCE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 2, midZ), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState(), false);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§2СТОЛОВАЯ №19", "§0Суп концентратный", "§0Хлеб белковый", "§0Сырую биомассу не брать", DyeColor.GREEN);
            }
            case 15 -> {
                // 16. Boiler & Thermal Substation (Бойлерная / Теплоузел)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.MOSSY_COBBLESTONE.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.BLAST_FURNACE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.HOPPER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ - 1), Blocks.CAULDRON.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.LIGHTNING_ROD.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), Blocks.LEVER.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.INDUSTRIAL_WORKSHOP, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4ТЕПЛОУЗЕЛ ТУ-4", "§0Давление 24 атм", "§0Вентиль не крутить!", "§0Утечка пара смертельна", DyeColor.RED);
            }
            case 16 -> {
                // 17. Security Checkpoint (КПП Сектора 9)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_ANDESITE.getDefaultState(), false);
                    }
                }
                for (int lz = minZ; lz <= maxZ; lz++) {
                    if (lz == midZ) {
                        chunk.setBlockState(mutable.set(midX, baseY + 1, lz), Blocks.OAK_FENCE_GATE.getDefaultState(), false);
                    } else {
                        chunk.setBlockState(mutable.set(midX, baseY + 1, lz), Blocks.IRON_BARS.getDefaultState(), false);
                        chunk.setBlockState(mutable.set(midX, baseY + 2, lz), Blocks.IRON_BARS.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.LECTERN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), Blocks.BELL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 3, midZ), ModBlocks.EMERGENCY_SIREN.getDefaultState(), false);
                placeChest(chunk, mutable.set(minX, baseY + 1, maxZ), ModLootTables.CIVIL_DEFENSE_DEPOT, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4КПП СЕКТОРА 9", "§0Предъявите пропуск", "§0и талон на жизнь", "§0Без штампа — ликвидация", DyeColor.RED);
            }
            case 17 -> {
                // 18. Inverted Gravity Anomaly Flat (Аномальная комната)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY + 5, lz), Blocks.DARK_OAK_PLANKS.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 4, minZ), ModBlocks.WALL_CARPET.getDefaultState().with(WallCarpetBlock.FACING, Direction.SOUTH), false);
                chunk.setBlockState(mutable.set(midX, baseY + 4, midZ), Blocks.DARK_OAK_FENCE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX - 1, baseY + 4, midZ), Blocks.DARK_OAK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST).with(StairsBlock.HALF, BlockHalf.TOP), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 4, maxZ), Blocks.SKELETON_SKULL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.CHAIN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.LANTERN.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(minX, baseY + 1, minZ), ModLootTables.PARTY_NOMENKLATURA, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§5АНОМАЛИЯ Г-11", "§0Гравитационный сдвиг", "§0Не прыгать!", "§0Смерть об потолок", DyeColor.PURPLE);
            }
            case 18 -> {
                // 19. Civil Defense Filter Depot (Склад СИЗ и фильтров ГО)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.SMOOTH_STONE.getDefaultState(), false);
                    }
                }
                for (int lx = minX; lx <= maxX; lx += 2) {
                    placeBarrel(chunk, mutable.set(lx, baseY + 1, minZ), ModLootTables.CIVIL_DEFENSE_DEPOT, seed + lx);
                    placeBarrel(chunk, mutable.set(lx, baseY + 2, minZ), ModLootTables.CIVIL_DEFENSE_DEPOT, seed + lx * 2);
                }
                chunk.setBlockState(mutable.set(minX, baseY + 2, midZ), ModBlocks.MEDKIT_BOX.getDefaultState().with(MedkitBoxBlock.FACING, Direction.EAST), false);
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.CIVIL_DEFENSE_DEPOT, seed);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.ANVIL.getDefaultState(), false);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§1СКЛАД СИЗ №3", "§0Фильтры ГП-5", "§0Срок годности: 1979", "§0Без маски вход закрыт", DyeColor.BLUE);
            }
            case 19 -> {
                // 20. Cybernetics & Vacuum Tube Scrapyard (Мастерская ЭВМ)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.EXPOSED_COPPER.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.REPEATER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.COMPARATOR.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.COPPER_BULB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.LIGHTNING_ROD.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.CHISELED_COPPER.getDefaultState(), false);
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.INDUSTRIAL_WORKSHOP, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§6ВЦ «ИСКРА-12»", "§0Вычислительный блок", "§0Не закорачивать!", "§0ЭВМ считает Самосбор", DyeColor.ORANGE);
            }
            case 20 -> {
                // 21. Communal Laundry (Прачечная / Постирочная)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.CYAN_TERRACOTTA.getDefaultState(), false);
                    }
                }
                for (int lx = minX; lx <= maxX; lx += 2) {
                    chunk.setBlockState(mutable.set(lx, baseY + 1, minZ), Blocks.WATER_CAULDRON.getDefaultState(), false);
                }
                chunk.setBlockState(mutable.set(minX, baseY + 3, midZ), Blocks.CHAIN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), Blocks.CHAIN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 3, midZ), Blocks.CHAIN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.WHITE_CARPET.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.RESIDENTIAL_APARTMENT, seed);
            }
            case 21 -> {
                // 22. Lenin Propaganda Red Corner (Красный уголок)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.RED_CONCRETE.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ), Blocks.GOLD_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, minZ), Blocks.LECTERN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.RED_CARPET.getDefaultState(), false);
                placeWallNotice(chunk, mutable.set(midX, baseY + 3, minZ), Direction.SOUTH, "§4ЛЕНИН ЖИВ", "§4ПАРТИЯ ЕДИНА", "§0Пятилетку — в 3 дня!", "§0Догнать и перегнать", DyeColor.RED);
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.PARTY_NOMENKLATURA, seed);
            }
            case 22 -> {
                // 23. Secret Panic Room behind Bookshelf (Тайная комната)
                chunk.setBlockState(mutable.set(doorX, baseY + 1, doorZ), Blocks.BOOKSHELF.getDefaultState(), false);
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.RED_BED.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.LANTERN.getDefaultState(), false);
                placeChest(chunk, mutable.set(maxX, baseY + 1, minZ), ModLootTables.LIQUIDATOR_CACHE, seed);
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.RESIDENTIAL_APARTMENT, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§2ДНЕВНИК ИНЖЕНЕРА", "§0Они замуровали выход", "§0У меня есть консервы", "§0Самосбор снаружи", DyeColor.GREEN);
            }
            case 23 -> {
                // 24. Mimic Nest Trap Room (Логово мимика)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.SCULK.getDefaultState(), false);
                    }
                }
                placeChest(chunk, mutable.set(midX, baseY + 1, midZ), ModLootTables.MIMIC_NEST, seed);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.CHEST.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ + 1), Blocks.SCULK_SENSOR.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.COBWEB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.COBWEB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.COBWEB.getDefaultState(), false);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4НЕ ТРОГАЙ", "§4ОНИ ЖИВЫЕ", "§4ЭТО НЕ СУНДУК", "§4БЕГИ", DyeColor.RED);
            }
            case 24 -> {
                // 25. Floor Breach / Structural Collapse (Обрушенное межэтажное перекрытие)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.CRACKED_STONE_BRICKS.getDefaultState(), false);
                    }
                }
                // Vertical breach hole down to floor below
                for (int lx = midX - 1; lx <= midX + 1; lx++) {
                    for (int lz = midZ - 1; lz <= midZ + 1; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.AIR.getDefaultState(), false);
                    }
                }
                // Exposed twisted rebar and dangling chains
                chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), Blocks.IRON_BARS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.LIGHTNING_ROD.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 4, midZ), Blocks.CHAIN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), Blocks.CHAIN.getDefaultState(), false);
                // Rubble cone around breach
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.TUFF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.COBBLESTONE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.GRAVEL.getDefaultState(), false);
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.RESIDENTIAL_APARTMENT), seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4[ПРОВАЛ]", "§0Плита обрушена", "§0Осторожно: дыра", "§4на нижний ярус!", DyeColor.RED);
            }
            case 25 -> {
                // 26. Skewed / Deformed Cell (Сплющенная ячейка с просевшим потолком)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.MUD_BRICKS.getDefaultState(), false);
                        // Sagging ceiling: fills baseY+3 and baseY+4 with cracked stone forcing crawlspace
                        chunk.setBlockState(mutable.set(lx, baseY + 3, lz), Blocks.CRACKED_STONE_BRICKS.getDefaultState(), false);
                        chunk.setBlockState(mutable.set(lx, baseY + 4, lz), Blocks.TUFF.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.COBWEB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState(), false);
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.RESIDENTIAL_APARTMENT), seed);
                placeWallNotice(chunk, mutable.set(doorX, baseY + 2, doorZ), Direction.NORTH, "§6[СДВИГ ПЛИТ]", "§0Потолок просел", "§0Пробираться", "§0ползком!", DyeColor.ORANGE);
            }
            case 26 -> {
                // 27. Apex Mimic Breach Nest (Логово Апекс-Мимика в вентиляции)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.SCULK.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.CAULDRON.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), Blocks.BONE_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ + 1), Blocks.SKELETON_SKULL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), ModBlocks.TOXIC_MUSHROOM.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.MIMIC_NEST), seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4[РАПОРТ 14]", "§0Существо поглотило", "§0жильцов и ушло", "§4В ВЫТЯЖКУ", DyeColor.RED);
            }
            case 27 -> {
                // 28. Welded Quarantine Stash (Заваренная гермодверь Ударного Звена)
                chunk.setBlockState(mutable.set(doorX, baseY + 1, doorZ), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.NORTH).with(HermeticDoorBlock.WELDED, true).with(HermeticDoorBlock.SEALED, true), false);
                chunk.setBlockState(mutable.set(doorX, baseY + 2, doorZ), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.NORTH).with(HermeticDoorBlock.WELDED, true).with(HermeticDoorBlock.SEALED, true), false);

                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_BASALT.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY, midZ), Blocks.MAGMA_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.CRYING_OBSIDIAN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.RESPAWN_ANCHOR.getDefaultState(), false);
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.LIQUIDATOR_CACHE), seed);
                placeWallNotice(chunk, mutable.set(doorX, baseY + 3, doorZ), Direction.NORTH, "§4ОБВАРЕНО СНАРУЖИ", "§0ЗВЕНО 4108-Б", "§0Карантин Б-2", "§4НЕ ВСКРЫВАТЬ!", DyeColor.RED);
            }
            case 28 -> {
                // 29. Bricked-In Survivor Shelter (Замурованное крыло)
                chunk.setBlockState(mutable.set(doorX, baseY + 1, doorZ), Blocks.BRICKS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(doorX, baseY + 2, doorZ), Blocks.CRACKED_STONE_BRICKS.getDefaultState(), false);

                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.OAK_PLANKS.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.RED_BED.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.LECTERN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.CANDLE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.SKELETON_SKULL.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.CIVIL_DEFENSE_DEPOT), seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§2ДНЕВНИК №4", "§0Дверь заложили...", "§0Воздух кончается", "§0Партком, прощай.", DyeColor.GREEN);
            }
            case 29 -> {
                // 30. Thermite Purge Crater (Термитный кратер зачистки)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.BLACKSTONE.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY, midZ), Blocks.OBSIDIAN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.FIRE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.BASALT.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), Blocks.MAGMA_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.ANVIL.getDefaultState(), false);
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.LIQUIDATOR_CACHE), seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4СЕКТОР ВЫЖЖЕН", "§0Огнесмесь РОКС-3", "§0Очаг уничтожен", "§4Фон в норме", DyeColor.RED);
            }
            case 30 -> {
                // 31. Soviet School / Civil Defense Classroom (Советский класс ГО и НВП)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.OAK_PLANKS.getDefaultState(), false);
                    }
                }
                // Chalkboard on front wall with glowing chemistry formulas
                chunk.setBlockState(mutable.set(midX - 1, baseY + 2, minZ), Blocks.DARK_PRISMARINE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, minZ), Blocks.DARK_PRISMARINE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 2, minZ), Blocks.DARK_PRISMARINE.getDefaultState(), false);

                // Teacher's podium and lectern
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ + 1), Blocks.LECTERN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, minZ + 1), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);

                // Desks in 2 rows
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, midZ), Blocks.DARK_OAK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH), false);
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, midZ - 1), Blocks.DARK_OAK_SLAB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX - 1, baseY + 1, midZ), Blocks.DARK_OAK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH), false);
                chunk.setBlockState(mutable.set(maxX - 1, baseY + 1, midZ - 1), Blocks.DARK_OAK_SLAB.getDefaultState(), false);

                // Bookshelf and first aid cabinet
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, maxZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX, baseY + 2, minZ + 1), ModBlocks.MEDKIT_BOX.getDefaultState().with(MedkitBoxBlock.FACING, Direction.WEST), false);

                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.CIVIL_DEFENSE_DEPOT, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 3, minZ), Direction.SOUTH, "§fНОРМАТИВ ГО №1", "§aНадевание ГП-5: 7с", "§0При опоздании —", "§4РАСПЫЛ В СМЕСИ", DyeColor.YELLOW);
            }
            case 31 -> {
                // 32. Soviet Assembly Hall / Auditorium (Актовый зал)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.SMOOTH_STONE.getDefaultState(), false);
                    }
                }
                // Raised stage with velvet curtain and speaker's podium
                chunk.setBlockState(mutable.set(midX - 1, baseY + 1, minZ), Blocks.RED_WOOL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, minZ), Blocks.OAK_SLAB.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, minZ), Blocks.RED_WOOL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, minZ), Blocks.LECTERN.getDefaultState(), false);

                // Rows of audience cinema seats
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, midZ), Blocks.CRIMSON_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.CRIMSON_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH), false);
                chunk.setBlockState(mutable.set(maxX - 1, baseY + 1, midZ), Blocks.CRIMSON_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH), false);

                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, maxZ - 1), Blocks.CRIMSON_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, maxZ - 1), Blocks.CRIMSON_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH), false);
                chunk.setBlockState(mutable.set(maxX - 1, baseY + 1, maxZ - 1), Blocks.CRIMSON_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH), false);

                // Projection booth at rear wall
                chunk.setBlockState(mutable.set(midX, baseY + 2, maxZ), Blocks.DISPENSER.getDefaultState(), false);

                placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.PARTY_NOMENKLATURA, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 3, minZ), Direction.SOUTH, "§6СЛАВА ТРУДУ!", "§eПЛАН — ЗАКОН!", "§eВЫПОЛНЕНИЕ — ЧЕСТЬ!", "§4ПАРТКОМ ЖЭК", DyeColor.RED);
            }
            case 32 -> {
                // 33. Gymnasium / Sports Hall (Спортивный зал)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.STRIPPED_BIRCH_WOOD.getDefaultState(), false);
                    }
                }
                // Wall climbing bars (шведская стенка) along minX
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ - 1), Blocks.LADDER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, midZ - 1), Blocks.LADDER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ), Blocks.LADDER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, midZ), Blocks.LADDER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ + 1), Blocks.LADDER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, midZ + 1), Blocks.LADDER.getDefaultState(), false);

                // Gymnastic mats on the floor
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.BLUE_WOOL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.CYAN_WOOL.getDefaultState(), false);

                // Hanging chains
                chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), Blocks.CHAIN.getDefaultState(), false);

                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.CIVIL_DEFENSE_DEPOT, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§1СПОРТЗАЛ ГО", "§0Нормы ГТО Сектора", "§0Бег в ОЗК и ГП-5", "§2Отсек 12", DyeColor.BLUE);
            }
            case 33 -> {
                // 34. Biomass Cult Sanctum / Heretical Chapel (Оккультное святилище Культа Биомассы)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_BLACKSTONE.getDefaultState(), false);
                    }
                }
                // Central Sacrificial Altar
                chunk.setBlockState(mutable.set(midX, baseY, midZ), Blocks.CRYING_OBSIDIAN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.CULT_ALTAR.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.TOXIC_MUSHROOM.getDefaultState(), false);

                // Biomass tendrils and blood carpets
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ + 1), Blocks.RED_CARPET.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ - 1), Blocks.RED_CARPET.getDefaultState(), false);

                // Pews and dark skulls
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, maxZ - 1), Blocks.WITHER_SKELETON_SKULL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX - 1, baseY + 1, maxZ - 1), Blocks.CANDLE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, minZ + 1), Blocks.POLISHED_BLACKSTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH), false);
                chunk.setBlockState(mutable.set(maxX - 1, baseY + 1, minZ + 1), Blocks.POLISHED_BLACKSTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH), false);

                // Occult mimic nest loot cache
                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.MIMIC_NEST, seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4ПЛОТЬ ЕДИНА", "§4С БЕТОНОМ", "§5СВЯТАЯ МЯКОТЬ", "§0Слава Самосбору", DyeColor.RED);
            }
            case 34 -> {
                // 35. Borshchevod Greenhouse Apartment (Оранжерея Староверов-Борщеводов)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.FARMLAND.getDefaultState(), false);
                    }
                }
                // UV Heat lamps on ceiling
                chunk.setBlockState(mutable.set(midX, baseY + FLOOR_HEIGHT - 1, midZ), ModBlocks.FLUORESCENT_LAMP.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + FLOOR_HEIGHT - 1, minZ + 1), ModBlocks.FLUORESCENT_LAMP.getDefaultState(), false);

                // Thickets of Mutated Borshchevik
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.MUTATED_BORSHCHEVIK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), ModBlocks.MUTATED_BORSHCHEVIK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), ModBlocks.MUTATED_BORSHCHEVIK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, maxZ - 1), ModBlocks.MUTATED_BORSHCHEVIK.getDefaultState(), false);

                // Herbalist cauldron and sap barrel
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.CAULDRON.getDefaultState(), false);
                placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.MIMIC_NEST), seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§2СЕКТА БОРЩЕВОДА", "§0Зонтики созрели", "§0Сбор сока в сумерках", "§4НЕ ВЫХОДИТЬ НА СВЕТ", DyeColor.GREEN);
            }
            case 35 -> {
                // 36. Rusty God Chapel (Храм Ржавого Бога и Чёрного Краза)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        BlockState cu = ((lx + lz) % 2 == 0) ? Blocks.EXPOSED_COPPER.getDefaultState() : Blocks.CUT_COPPER.getDefaultState();
                        chunk.setBlockState(mutable.set(lx, baseY, lz), cu, false);
                    }
                }
                // Heavy machinery & anvil altar
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.CHIPPED_ANVIL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.CHAIN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), Blocks.CHAIN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, midZ), Blocks.HEAVY_CORE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX - 1, baseY + 1, midZ), Blocks.IRON_BLOCK.getDefaultState(), false);

                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.LIQUIDATOR_CACHE), seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§6РЖАВЫЙ БОГ", "§0Чёрный Краз грядёт", "§0Отвергни плоть —", "§4СТАЛЬ ВЕЧНА", DyeColor.ORANGE);
            }
            case 36 -> {
                // 37. Incubation Aquarium «ИНК-7» / «Спецрезервуар Б-11»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_DEEPSLATE.getDefaultState(), false);
                    }
                }
                int fluidType = Math.abs((int) ((seed / 3) % 3));
                BlockState fluidBlock = switch (fluidType) {
                    case 0 -> Blocks.HONEY_BLOCK.getDefaultState();
                    case 1 -> Blocks.SLIME_BLOCK.getDefaultState();
                    default -> Blocks.WATER.getDefaultState();
                };
                boolean isCracked = ((seed % 3) == 0);

                for (int y = baseY + 1; y <= baseY + 3; y++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos p = mutable.set(midX + dx, y, midZ + dz);
                            if (dx == 0 && dz == 0) {
                                if (y == baseY + 2) {
                                    chunk.setBlockState(p, (fluidType == 0) ? Blocks.ZOMBIE_HEAD.getDefaultState() : Blocks.SKELETON_SKULL.getDefaultState(), false);
                                } else {
                                    chunk.setBlockState(p, fluidBlock, false);
                                }
                            } else {
                                if (isCracked && y == baseY + 1 && (dx == 1 || dz == 1)) {
                                    chunk.setBlockState(p, Blocks.GLASS_PANE.getDefaultState(), false);
                                } else {
                                    chunk.setBlockState(p, Blocks.TINTED_GLASS.getDefaultState(), false);
                                }
                            }
                        }
                    }
                }
                if (isCracked) {
                    chunk.setBlockState(mutable.set(midX + 2, baseY, midZ), fluidBlock, false);
                    chunk.setBlockState(mutable.set(midX - 2, baseY, midZ + 1), fluidBlock, false);
                }
                chunk.setBlockState(mutable.set(midX, baseY + 4, midZ), ModBlocks.P46_PANEL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.CAULDRON.getDefaultState(), false);

                int scaledLvl = com.samosbor.enchantment.OccultEnchantmentHelper.getScaledFloorLevel(floor, seed);
                placeCustomLootBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ),
                        new ItemStack(ModItems.AJ11_FLUID, 1 + (int)(seed % 2)),
                        new ItemStack(ModItems.IM9_INHIBITOR, 1),
                        com.samosbor.item.OccultTomeItem.createStack(ModItems.OCCULT_TOME_BIOMASS, scaledLvl)
                );
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§3ИНКУБАТОРИЙ Б-11", isCracked ? "§4ТРЕЩИНА В ЦИСТЕРНЕ" : "§0Стазис-контур ИНК-7", "§0Суспензия: АЖ-11", "§cНЕ ПОВРЕЖДАТЬ СТЕКЛО", DyeColor.CYAN);
            }
            case 37 -> {
                // 38. Geofront CO2 Greenhouse «ОКБ-2»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), ((lx + lz) % 2 == 0) ? Blocks.MOSS_BLOCK.getDefaultState() : Blocks.COARSE_DIRT.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(minX + 1, baseY + 1, minZ + 1), ModBlocks.MUTATED_BORSHCHEVIK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX - 1, baseY + 1, minZ + 1), ModBlocks.MUTATED_BORSHCHEVIK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, maxZ - 1), ModBlocks.TOXIC_MUSHROOM.getDefaultState(), false);

                chunk.setBlockState(mutable.set(midX, baseY + 4, midZ), ModBlocks.FLUORESCENT_LAMP.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX + 1, baseY + 4, midZ), Blocks.CHAIN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(maxX - 1, baseY + 4, midZ), Blocks.CHAIN.getDefaultState(), false);

                int scaledLvl = com.samosbor.enchantment.OccultEnchantmentHelper.getScaledFloorLevel(floor, seed);
                placeCustomLootChest(chunk, mutable.set(maxX, baseY + 1, maxZ),
                        new ItemStack(ModItems.BORSHCHEVIK_EXTRACT, 2),
                        com.samosbor.item.OccultTomeItem.createStack(ModItems.OCCULT_TOME_BORSHCHEVIK, scaledLvl),
                        new ItemStack(ModItems.GAS_MASK_FILTER, 1)
                );
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§2ОРАНЖЕРЕЯ ОКБ-2", "§0Сектор фотосинтеза", "§0CO2: 14.8% — Норма", "§4БЕРЕЧЬСЯ ОЖОГОВ", DyeColor.GREEN);
            }
            case 38 -> {
                // 39. Sector Nerve Node «Узел-Н»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.CRIMSON_NYLIUM.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.HEAVY_CORE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), Blocks.REDSTONE_LAMP.getDefaultState().with(net.minecraft.state.property.Properties.LIT, true), false);

                chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), Blocks.REDSTONE_WIRE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.REDSTONE_WIRE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ - 1), Blocks.SCULK_VEIN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ + 1), Blocks.SCULK_VEIN.getDefaultState(), false);

                int scaledLvl = com.samosbor.enchantment.OccultEnchantmentHelper.getScaledFloorLevel(floor, seed);
                placeCustomLootChest(chunk, mutable.set(minX, baseY + 1, minZ),
                        new ItemStack(ModItems.BIOMASS_HEART, 1),
                        com.samosbor.item.OccultTomeItem.createStack(ModItems.OCCULT_TOME_BARRIER, scaledLvl)
                );
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§5СВЯЗЬ «УЗЕЛ-Н»", "§0Нервные жилы яруса", "§0Импульс: 180 Гц", "§cНЕ ОБРЫВАТЬ КАБЕЛИ", DyeColor.PURPLE);
            }
            case 39 -> {
                // 40. Test Shaft «Объект 14-Б»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.SMOOTH_STONE.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.IRON_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.P46_PANEL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), Blocks.HEAVY_CORE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 4, midZ), Blocks.CHAIN.getDefaultState(), false);

                chunk.setBlockState(mutable.set(midX - 1, baseY + 2, midZ), ModBlocks.P46_PANEL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 2, midZ), ModBlocks.P46_PANEL.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, midZ), Blocks.SCAFFOLDING.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, midZ), Blocks.SCAFFOLDING.getDefaultState(), false);

                int scaledLvl = com.samosbor.enchantment.OccultEnchantmentHelper.getScaledFloorLevel(floor, seed);
                placeCustomLootChest(chunk, mutable.set(maxX, baseY + 1, maxZ),
                        new ItemStack(ModItems.PROJECT_SODEYSTVIE, 1),
                        com.samosbor.item.OccultTomeItem.createStack(ModItems.OCCULT_TOME_SPEAR, scaledLvl),
                        new ItemStack(ModItems.HEAVY_CROWBAR, 1)
                );
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4ОБЪЕКТ 14-Б", "§0Шахта Спецкаркаса", "§0Броня: плиты П-46", "§4СОВЕРШЕННО СЕКРЕТНО", DyeColor.RED);
            }
            case 40 -> {
                // 41. Preservation Capsule Block «Спецрезерв Д-6»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_ANDESITE.getDefaultState(), false);
                    }
                }
                for (int dx = -1; dx <= 1; dx += 2) {
                    chunk.setBlockState(mutable.set(midX + dx, baseY + 1, midZ), Blocks.TINTED_GLASS.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX + dx, baseY + 2, midZ), Blocks.SKELETON_SKULL.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX + dx, baseY + 3, midZ), Blocks.IRON_TRAPDOOR.getDefaultState().with(net.minecraft.block.TrapdoorBlock.HALF, net.minecraft.block.enums.BlockHalf.TOP), false);
                }
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.OLD_TV.getDefaultState().with(OldTvBlock.FACING, Direction.SOUTH), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), ModBlocks.MEDKIT_BOX.getDefaultState().with(MedkitBoxBlock.FACING, Direction.SOUTH), false);

                int scaledLvl = com.samosbor.enchantment.OccultEnchantmentHelper.getScaledFloorLevel(floor, seed);
                placeCustomLootChest(chunk, mutable.set(maxX, baseY + 1, maxZ),
                        new ItemStack(ModItems.L1M_SUIT, 1),
                        new ItemStack(ModItems.SR11_STABILIZER, 2),
                        com.samosbor.item.OccultTomeItem.createStack(ModItems.PARTY_TOME_PROTECTION, scaledLvl)
                );
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§1СПЕЦРЕЗЕРВ Д-6", "§0Стазис Ликвидаторов", "§0Пломба не нарушена", "§0Приказ Коменданта", DyeColor.BLUE);
            }
            case 41 -> {
                // 42. Hermetic Blast Gate «Гермоворота ГВ-8»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.YELLOW_TERRACOTTA.getDefaultState(), false);
                    }
                }
                for (int z = minZ; z <= maxZ; z++) {
                    chunk.setBlockState(mutable.set(midX, baseY + 1, z), Blocks.IRON_BLOCK.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX, baseY + 2, z), (z == midZ) ? ModBlocks.HERMETIC_DOOR.getDefaultState() : Blocks.IRON_BARS.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX, baseY + 3, z), Blocks.PISTON.getDefaultState().with(net.minecraft.state.property.Properties.FACING, Direction.DOWN), false);
                }
                chunk.setBlockState(mutable.set(minX, baseY + 3, midZ), ModBlocks.EMERGENCY_SIREN.getDefaultState(), false);

                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.CIVIL_DEFENSE_DEPOT), seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§eШЛЮЗ ГВ-8", "§0Гидрозатвор яруса", "§0Закрывать при гуле!", "§0ГО СССР", DyeColor.YELLOW);
            }
            case 42 -> {
                // 43. Magma Core Power Block «Ядро К-2»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.POLISHED_BLACKSTONE.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.CAULDRON.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.MAGMA_BLOCK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), Blocks.IRON_BARS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 4, midZ), Blocks.CHAIN.getDefaultState(), false);

                chunk.setBlockState(mutable.set(midX - 1, baseY + 2, midZ), Blocks.LIGHTNING_ROD.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 2, midZ), Blocks.LIGHTNING_ROD.getDefaultState(), false);

                int delta = Math.abs(floor - 85);
                int plasmaTier = Math.min(3, 1 + delta / 30);
                placeCustomLootChest(chunk, mutable.set(maxX, baseY + 1, maxZ),
                        new ItemStack(ModItems.ROKS3_FLAMETHROWER, 1),
                        new ItemStack(ModItems.ACETYLENE_CANISTER, 2),
                        com.samosbor.item.OccultTomeItem.createStack(ModItems.OCCULT_TOME_PLASMA, plasmaTier)
                );
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§6РЕАКТОР «ЯДРО К-2»", "§0Тепловая станция", "§0Температура: 1400°C", "§4ВЫБРОС ПЛАЗМЫ", DyeColor.ORANGE);
            }
            case 43 -> {
                // 44. Psychological Fixation Chamber «Спецсектор 9»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.RED_TERRACOTTA.getDefaultState(), false);
                    }
                }
                boolean useCogwheel = (seed % 2 == 0);
                if (useCogwheel) {
                    chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.EXPOSED_COPPER.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.CUT_COPPER.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), Blocks.COPPER_GRATE.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX - 1, baseY + 2, midZ), Blocks.CHIPPED_ANVIL.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX + 1, baseY + 2, midZ), Blocks.HEAVY_CORE.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX, baseY + 2, midZ - 1), Blocks.CHAIN.getDefaultState(), false);
                } else {
                    chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.P46_PANEL.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), ModBlocks.P46_PANEL.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX - 1, baseY + 2, midZ), Blocks.IRON_BARS.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX + 1, baseY + 2, midZ), Blocks.IRON_BARS.getDefaultState(), false);
                    chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), Blocks.CHAIN.getDefaultState(), false);
                }
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), ModBlocks.CULT_ALTAR.getDefaultState(), false);

                int scaledLvl = com.samosbor.enchantment.OccultEnchantmentHelper.getScaledFloorLevel(floor, seed);
                placeCustomLootChest(chunk, mutable.set(maxX, baseY + 1, maxZ),
                        new ItemStack(ModItems.ANOMALY_CORE, 1),
                        new ItemStack(ModItems.RIFT_SPLASH_POTION, 2),
                        com.samosbor.item.OccultTomeItem.createStack(ModItems.OCCULT_TOME_RIFT, scaledLvl)
                );
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4СПЕЦСЕКТОР 9", useCogwheel ? "§0Шестерня Ржавого Бога" : "§0Монолитная фиксация", "§0Пункт очистки душ", "§4ОТВЕРГНИ ПЛОТЬ", DyeColor.RED);
            }
            case 44 -> {
                // 45. Autoclave Bioreactor «БР-11»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.STONE_BRICKS.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.BLAST_FURNACE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.HOPPER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX, baseY + 3, midZ), ModBlocks.BIOMASS.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), Blocks.CAULDRON.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.ANVIL.getDefaultState(), false);

                int scaledLvl = com.samosbor.enchantment.OccultEnchantmentHelper.getScaledFloorLevel(floor, seed);
                placeCustomLootBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ),
                        new ItemStack(ModBlocks.P46_PANEL.asItem(), 8),
                        new ItemStack(Items.IRON_INGOT, 4),
                        com.samosbor.item.OccultTomeItem.createStack(ModItems.OCCULT_TOME_CONCRETE, scaledLvl)
                );
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§7АВТОКЛАВ БР-11", "§0Биореактор синтеза", "§0Сырьё: биомасса", "§0Выход: П-46 и металл", DyeColor.GRAY);
            }
            case 45 -> {
                // 46. Food Synthesizer «ПС-4»
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.WHITE_TERRACOTTA.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), ModBlocks.FOOD_DISPENSER.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX - 1, baseY + 1, midZ), ModBlocks.RUSTY_SINK.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), ModBlocks.GAS_STOVE.getDefaultState().with(GasStoveBlock.FACING, Direction.SOUTH), false);

                placeCustomLootBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ),
                        new ItemStack(ModItems.FOOD_CONCENTRATE, 6),
                        new ItemStack(ModItems.CANNED_STEW, 3),
                        new ItemStack(ModItems.RATION_TICKET, 5),
                        new ItemStack(ModItems.HARDTACK, 4)
                );
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§6СИНТЕЗАТОР ПС-4", "§0Раздаточный блок", "§0Норма: 1 банка/смена", "§2СЛАВА ПАРТИИ!", DyeColor.YELLOW);
            }
            default -> {
                // 47. Outer Wall Cartographer Outpost (Аванпост Искателей Выхода)
                for (int lx = minX; lx <= maxX; lx++) {
                    for (int lz = minZ; lz <= maxZ; lz++) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), Blocks.STRIPPED_SPRUCE_WOOD.getDefaultState(), false);
                    }
                }
                chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.CARTOGRAPHY_TABLE.getDefaultState(), false);
                chunk.setBlockState(mutable.set(midX + 1, baseY + 1, midZ), Blocks.LECTERN.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 2, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
                chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.TARGET.getDefaultState(), false);

                placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), getDepthLootTable(floor, ModLootTables.CIVIL_DEFENSE_DEPOT), seed);
                placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§bИСКАТЕЛИ ВЫХОДА", "§0Сектор 104-Юг", "§0Стена существует", "§bМы найдём край!", DyeColor.CYAN);
            }
        }
    }

    private void generateIndustrialHall(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ, long seed) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
            chunk.setBlockState(mutable.set(midX, y, midZ), ModBlocks.P46_PANEL.getDefaultState(), false);
        }
        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BLAST_FURNACE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ + 1), Blocks.ANVIL.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.HOPPER.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), ModBlocks.FOOD_DISPENSER.getDefaultState(), false);
        placeBarrel(chunk, mutable.set(minX, baseY + 1, maxZ), ModLootTables.INDUSTRIAL_WORKSHOP, seed);
        chunk.setBlockState(mutable.set(midX, baseY + 4, minZ), Blocks.CHAIN.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 4, maxZ), Blocks.CHAIN.getDefaultState(), false);
    }

    private void generateLaboratory(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ, long seed) {
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
        chunk.setBlockState(mutable.set(minX, baseY + 2, midZ), ModBlocks.MEDKIT_BOX.getDefaultState().with(MedkitBoxBlock.FACING, Direction.EAST), false);
        placeChest(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.MEDICAL_STATION, seed);
        placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§3НИИ «ГИГАСТРОЙ»", "§0Лаб. 4-Б: Биомасса", "§0Вход только в ГП-5", "§0Класс опасности: IV", DyeColor.CYAN);
    }

    private void generateAdministration(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ, long seed) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        for (int lx = minX; lx <= maxX; lx++) {
            chunk.setBlockState(mutable.set(lx, baseY + 1, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
            chunk.setBlockState(mutable.set(lx, baseY + 2, minZ), Blocks.BOOKSHELF.getDefaultState(), false);
        }

        chunk.setBlockState(mutable.set(midX, baseY + 1, midZ), Blocks.POLISHED_ANDESITE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 2, midZ), Blocks.HEAVY_CORE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 1, maxZ), Blocks.CHISELED_BOOKSHELF.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 2, midZ), ModBlocks.NOTICE_BOARD.getDefaultState().with(NoticeBoardBlock.FACING, Direction.WEST), false);
        placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.PARTY_NOMENKLATURA, seed);
        placeWallNotice(chunk, mutable.set(midX, baseY + 2, minZ), Direction.SOUTH, "§4ПАРТКОМ СЕКТОРА", "§0Приём граждан:", "§0Вт, Чт 14:00-16:00", "§0Талоны не выдаём!", DyeColor.RED);
    }

    private void generateFloodedTechnical(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ, long seed) {
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
        chunk.setBlockState(mutable.set(maxX, baseY + 1, minZ), Blocks.CAULDRON.getDefaultState(), false);
        placeBarrel(chunk, mutable.set(maxX, baseY + 1, maxZ), ModLootTables.FLOODED_REACTOR, seed);
    }

    private void generatePurgedZone(Chunk chunk, BlockPos.Mutable mutable, int baseY, int minX, int maxX, int minZ, int maxZ, long seed) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;

        chunk.setBlockState(mutable.set(midX, baseY, midZ), Blocks.MAGMA_BLOCK.getDefaultState(), false);
        chunk.setBlockState(mutable.set(minX, baseY + 1, minZ), Blocks.BASALT.getDefaultState(), false);
        chunk.setBlockState(mutable.set(maxX, baseY + 1, maxZ), Blocks.POLISHED_BLACKSTONE.getDefaultState(), false);
        chunk.setBlockState(mutable.set(minX, baseY + 1, maxZ), Blocks.COAL_BLOCK.getDefaultState(), false);
        chunk.setBlockState(mutable.set(midX, baseY + 2, minZ), Blocks.IRON_BARS.getDefaultState(), false);
        placeChest(chunk, mutable.set(midX, baseY + 1, midZ), ModLootTables.LIQUIDATOR_CACHE, seed);
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
