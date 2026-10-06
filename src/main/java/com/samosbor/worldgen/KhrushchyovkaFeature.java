package com.samosbor.worldgen;

import com.mojang.serialization.Codec;
import com.samosbor.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class KhrushchyovkaFeature extends Feature<DefaultFeatureConfig> {
    public static final Identifier ID = Identifier.of("samosbor", "khrushchyovka");
    public static final Feature<DefaultFeatureConfig> INSTANCE = new KhrushchyovkaFeature(DefaultFeatureConfig.CODEC);

    public KhrushchyovkaFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    public static void register() {
        Registry.register(Registries.FEATURE, ID, INSTANCE);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();

        // Find ground height
        int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        if (groundY < 60 || groundY > 120) return false;

        BlockPos start = new BlockPos(origin.getX(), groundY, origin.getZ());

        // Check flat area roughly
        if (world.getBlockState(start.down()).isAir()) return false;

        int width = 11;
        int length = 15;
        int height = 22; // 5 floors (approx 4 blocks per floor + roof)

        BlockState p46 = ModBlocks.P46_PANEL.getDefaultState();
        BlockState painted = ModBlocks.PAINTED_PANEL.getDefaultState();
        BlockState glass = Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.getDefaultState();
        BlockState ironBars = Blocks.IRON_BARS.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState doorLower = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH);
        BlockState doorUpper = ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH);
        BlockState elevatorStation = ModBlocks.ELEVATOR_STATION.getDefaultState();
        BlockState lamp = ModBlocks.FLUORESCENT_LAMP.getDefaultState();
        BlockState stairs = Blocks.STONE_BRICK_STAIRS.getDefaultState();

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        // 1. Deep Foundation: fill under the building down to 8 blocks to eliminate floating structures on slopes
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = -8; y <= 0; y++) {
                    mutable.set(start.getX() + x, start.getY() + y, start.getZ() + z);
                    world.setBlockState(mutable, p46, 3);
                }
            }
        }

        // 2. Main 5-story building shell
        for (int y = 1; y <= height; y++) {
            boolean isFloorSlab = (y % 4 == 0);

            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    mutable.set(start.getX() + x, start.getY() + y, start.getZ() + z);
                    boolean isPerimeter = (x == 0 || x == width - 1 || z == 0 || z == length - 1);

                    if (y == height) {
                        // Roof slab
                        world.setBlockState(mutable, p46, 3);
                    } else if (isFloorSlab) {
                        // Floor slab between stories with stairwell opening at center
                        boolean isStairHole = (x >= 4 && x <= 6 && z >= 6 && z <= 8);
                        if (!isStairHole) {
                            world.setBlockState(mutable, p46, 3);
                        } else {
                            world.setBlockState(mutable, air, 3);
                        }
                    } else if (isPerimeter) {
                        // Windows every few blocks
                        boolean isWindow = (y % 4 == 2 || y % 4 == 3) && ((x >= 2 && x <= 3) || (x >= 7 && x <= 8) || (z >= 2 && z <= 3) || (z >= 10 && z <= 11));
                        // Entrance door at (x=5, z=0, y=1..2)
                        boolean isEntrance = (x == 5 && z == 0 && (y == 1 || y == 2));

                        if (isEntrance) {
                            if (y == 1) world.setBlockState(mutable, doorLower, 3);
                            else world.setBlockState(mutable, doorUpper, 3);
                        } else if (isWindow) {
                            world.setBlockState(mutable, random.nextBoolean() ? glass : ironBars, 3);
                        } else {
                            // Two-tone facade: lower level painted, upper concrete
                            world.setBlockState(mutable, (y % 4 == 1) ? painted : p46, 3);
                        }
                    } else {
                        // Interior space
                        world.setBlockState(mutable, air, 3);
                    }
                }
            }
        }

        // 3. Interior stairs connecting all 5 floors
        for (int story = 0; story < 5; story++) {
            int baseY = story * 4 + 1;
            // Place wooden/stone stairs in the stair column
            world.setBlockState(start.add(5, baseY, 7), stairs, 3);
            world.setBlockState(start.add(5, baseY + 1, 8), stairs, 3);
            world.setBlockState(start.add(5, baseY + 2, 7), lamp, 3);
        }

        // 4. Entrance Canopy (Козырёк подъезда)
        for (int x = 4; x <= 6; x++) {
            mutable.set(start.getX() + x, start.getY() + 3, start.getZ() - 1);
            world.setBlockState(mutable, p46, 3);
        }

        // 5. Ground floor lobby with Elevator Station to Gigakhrushchyovka
        BlockPos elevatorPos = start.add(5, 1, 4);
        world.setBlockState(elevatorPos, elevatorStation, 3);
        world.setBlockState(elevatorPos.up(2), lamp, 3);

        // Entrance notice board / crate with basic supplies
        world.setBlockState(start.add(4, 1, 2), Blocks.BARREL.getDefaultState(), 3);
        world.setBlockState(start.add(6, 1, 2), ModBlocks.OLD_TV.getDefaultState(), 3);

        return true;
    }
}
