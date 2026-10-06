package com.samosbor.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.samosbor.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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

    private void generateGigakhrushch(Chunk chunk) {
        ChunkPos cpos = chunk.getPos();
        int chunkX = cpos.x;
        int chunkZ = cpos.z;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        BlockState bedrock = Blocks.BEDROCK.getDefaultState();
        BlockState p46 = ModBlocks.P46_PANEL.getDefaultState();
        BlockState painted = ModBlocks.PAINTED_PANEL.getDefaultState();
        BlockState hermeticDoorLower = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState hermeticDoorUpper = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        BlockState fluorLamp = ModBlocks.FLUORESCENT_LAMP.getDefaultState();
        BlockState emergLamp = ModBlocks.EMERGENCY_LAMP.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState biomass = ModBlocks.BIOMASS.getDefaultState();
        BlockState toxicMushroom = ModBlocks.TOXIC_MUSHROOM.getDefaultState();
        BlockState elevatorStation = ModBlocks.ELEVATOR_STATION.getDefaultState();
        BlockState dispenser = ModBlocks.FOOD_DISPENSER.getDefaultState();
        BlockState stairs = Blocks.STONE_BRICK_STAIRS.getDefaultState();
        BlockState chains = Blocks.CHAIN.getDefaultState();
        BlockState ironBars = Blocks.IRON_BARS.getDefaultState();

        boolean isStairChunk = Math.floorMod(chunkX + chunkZ, 4) == 0;
        boolean isElevatorChunk = Math.floorMod(chunkX + chunkZ, 8) == 2;

        // Foundation: Bedrock at Y=0..2
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                for (int y = 0; y <= 2; y++) {
                    chunk.setBlockState(mutable.set(lx, y, lz), bedrock, false);
                }
                // Reinforced ceiling at the very top Y=1020..1023
                for (int y = 1020; y < MAX_HEIGHT; y++) {
                    chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                }
            }
        }

        // Generate floors
        for (int floor = 0; floor < TOTAL_FLOORS && (floor * FLOOR_HEIGHT + FLOOR_HEIGHT) <= 1020; floor++) {
            int baseY = floor * FLOOR_HEIGHT;
            long seed = ((long) chunkX * 3129871L) ^ ((long) chunkZ * 6187913L) ^ ((long) floor * 1013904223L);
            int sectorType = Math.abs((int) (seed % 20));

            // Factory hall floor check
            boolean isFactoryHall = (floor % 30 == 15);

            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    // 1. Floor slab at y = baseY
                    boolean isStairHole = isStairChunk && (lx >= 3 && lx <= 6) && (lz >= 3 && lz <= 6);
                    boolean isElevatorHole = isElevatorChunk && (lx >= 11 && lx <= 13) && (lz >= 11 && lz <= 13);

                    if (isElevatorHole) {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), air, false);
                        if (lx == 12 && lz == 12) {
                            for (int y = baseY; y < baseY + FLOOR_HEIGHT; y++) {
                                chunk.setBlockState(mutable.set(lx, y, lz), chains, false);
                            }
                        }
                    } else if (isStairHole) {
                        // Stairwell opening
                        chunk.setBlockState(mutable.set(lx, baseY, lz), air, false);
                    } else {
                        chunk.setBlockState(mutable.set(lx, baseY, lz), p46, false);
                    }

                    // 2. Interior space: y = baseY + 1 .. baseY + 5
                    if (sectorType == 0) {
                        // Concreted dead zone
                        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                        continue;
                    }

                    if (sectorType == 1) {
                        // Biomass infested dead zone
                        chunk.setBlockState(mutable.set(lx, baseY, lz), biomass, false);
                        if ((lx + lz) % 7 == 0) {
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), toxicMushroom, false);
                        }
                    }

                    // Normal floor layout: Corridors and Rooms
                    boolean isCorridorX = (lx == 7 || lx == 8);
                    boolean isCorridorZ = (lz == 7 || lz == 8);
                    boolean isCorridor = isCorridorX || isCorridorZ;

                    boolean isCorridorWallX = (lx == 6 || lx == 9);
                    boolean isCorridorWallZ = (lz == 6 || lz == 9);
                    boolean isRoomPartition = (lz == 0 || lz == 15 || lx == 0 || lx == 15);

                    // Door openings into rooms
                    boolean isDoorway = (isCorridorWallX && (lz == 3 || lz == 12)) || (isCorridorWallZ && (lx == 3 || lx == 12));

                    if (isStairHole) {
                        // Stair steps connecting floors
                        if (lx == 4 && lz == 4) {
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), stairs, false);
                            chunk.setBlockState(mutable.set(lx, baseY + 2, lz), stairs, false);
                        }
                        continue;
                    }

                    if (isElevatorHole) {
                        continue;
                    }

                    // Place elevator station next to shaft
                    if (isElevatorChunk && lx == 10 && lz == 12 && (floor % 8 == 0)) {
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), elevatorStation, false);
                    }

                    if (isCorridor) {
                        // Clear corridor air
                        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                        }
                        // Ceiling lamps
                        if ((lx + lz + floor) % 8 == 0) {
                            BlockState lamp = (floor % 5 == 0) ? emergLamp : fluorLamp;
                            chunk.setBlockState(mutable.set(lx, baseY + FLOOR_HEIGHT - 1, lz), lamp, false);
                        }
                    } else if (isDoorway) {
                        // Doorway
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), hermeticDoorLower, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), hermeticDoorUpper, false);
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    } else if (isCorridorWallX || isCorridorWallZ || isRoomPartition) {
                        // Two-tone walls: lower painted, upper concrete
                        chunk.setBlockState(mutable.set(lx, baseY + 1, lz), painted, false);
                        chunk.setBlockState(mutable.set(lx, baseY + 2, lz), painted, false);
                        for (int y = baseY + 3; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), p46, false);
                        }
                    } else {
                        // Inside room
                        for (int y = baseY + 1; y < baseY + FLOOR_HEIGHT; y++) {
                            chunk.setBlockState(mutable.set(lx, y, lz), air, false);
                        }
                        // Room furniture / features
                        if (isFactoryHall && lx == 4 && lz == 4) {
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), dispenser, false);
                        } else if ((lx == 2 && lz == 2) || (lx == 13 && lz == 13)) {
                            // Crate / Iron bars
                            chunk.setBlockState(mutable.set(lx, baseY + 1, lz), ironBars, false);
                        }
                    }
                }
            }
        }
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
        text.add("Gigakhrushchyovka 1024m Floor: " + (pos.getY() / FLOOR_HEIGHT));
    }
}
