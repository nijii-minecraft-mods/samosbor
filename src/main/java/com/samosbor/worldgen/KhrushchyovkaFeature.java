package com.samosbor.worldgen;

import com.mojang.serialization.Codec;
import com.samosbor.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
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

        // 1. Find ground height
        int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        if (groundY < 60 || groundY > 120) return false;

        BlockPos start = new BlockPos(origin.getX(), groundY, origin.getZ());
        if (world.getBlockState(start.down()).isAir()) return false;

        // Series 1-510 dimensions: width=13, length=17, height=24 (5 floors of 4 blocks + plinth & roof)
        int width = 13;
        int length = 17;
        int height = 24;

        BlockState p46 = ModBlocks.P46_PANEL.getDefaultState();
        BlockState painted = ModBlocks.PAINTED_PANEL.getDefaultState();
        BlockState plinth = Blocks.DEEPSLATE_BRICKS.getDefaultState();
        BlockState glass = Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.getDefaultState();
        BlockState ironBars = Blocks.IRON_BARS.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState smoothSlab = Blocks.SMOOTH_STONE_SLAB.getDefaultState();
        BlockState stairsStone = Blocks.STONE_BRICK_STAIRS.getDefaultState();
        BlockState fluorLamp = ModBlocks.FLUORESCENT_LAMP.getDefaultState();

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        // 2. Deep Foundation (Y = -8 to -1): Solid concrete bedrock pad to prevent floating
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = -8; y < 0; y++) {
                    mutable.set(start.getX() + x, start.getY() + y, start.getZ() + z);
                    world.setBlockState(mutable, p46, 3);
                }
            }
        }

        // 3. Ground Plinth & Basement Crawlspace (Y = 0)
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                mutable.set(start.getX() + x, start.getY(), start.getZ() + z);
                boolean isPerimeter = (x == 0 || x == width - 1 || z == 0 || z == length - 1);
                if (isPerimeter) {
                    // Periodic basement ventilation grates (продухи подвала)
                    boolean isVentGrate = ((x == 3 || x == 9) && (z == 0 || z == length - 1))
                            || ((z == 4 || z == 12) && (x == 0 || x == width - 1));
                    world.setBlockState(mutable, isVentGrate ? ironBars : plinth, 3);
                } else {
                    world.setBlockState(mutable, p46, 3); // Basement concrete floor
                }
            }
        }

        // 4. Main 5-Story Building Shell (Y = 1 to height)
        for (int y = 1; y <= height; y++) {
            int story = (y - 1) / 4; // 0 to 4 (5 stories)
            int storyY = (y - 1) % 4; // 0=floor slab, 1,2=walls/windows, 3=lintel
            boolean isFloorSlab = (storyY == 0 && y > 1);

            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    mutable.set(start.getX() + x, start.getY() + y, start.getZ() + z);
                    boolean isPerimeter = (x == 0 || x == width - 1 || z == 0 || z == length - 1);

                    if (y == height) {
                        // Roof slab with parapet
                        world.setBlockState(mutable, isPerimeter ? plinth : p46, 3);
                    } else if (isFloorSlab) {
                        // Inter-floor slab with central stairwell hole (x in 5..7, z in 7..9)
                        boolean isStairHole = (x >= 5 && x <= 7 && z >= 7 && z <= 9);
                        world.setBlockState(mutable, isStairHole ? air : p46, 3);
                    } else if (isPerimeter) {
                        // Entrance Doorway at Front Facade (z=0, x=6, y=1..2)
                        boolean isEntrance = (z == 0 && x == 6 && (y == 1 || y == 2));
                        if (isEntrance) {
                            world.setBlockState(mutable, air, 3);
                            continue;
                        }

                        // Windows configuration
                        boolean isWindow = (storyY == 1 || storyY == 2) &&
                                ((x == 2 || x == 3 || x == 9 || x == 10) && (z == 0 || z == length - 1)
                                        || (z == 3 || z == 4 || z == 12 || z == 13) && (x == 0 || x == width - 1));

                        if (isWindow) {
                            world.setBlockState(mutable, (random.nextInt(6) == 0) ? ironBars : glass, 3);
                        } else {
                            // Authentic two-tone Soviet facade: Story 0 is painted cyan/teal, upper stories weathered concrete
                            world.setBlockState(mutable, (story == 0) ? painted : p46, 3);
                        }
                    } else {
                        // Interior hollow space
                        world.setBlockState(mutable, air, 3);
                    }
                }
            }
        }

        // 5. Entrance Porch Group (Крыльцо и козырёк)
        // Porch concrete steps in front of entrance (z = -1, x = 5..7, y = 1)
        for (int x = 5; x <= 7; x++) {
            mutable.set(start.getX() + x, start.getY() + 1, start.getZ() - 1);
            world.setBlockState(mutable, stairsStone.with(StairsBlock.FACING, Direction.SOUTH), 3);
        }

        // Entrance heavy door (z = 0, x = 6, y = 1..2)
        BlockPos doorPos = start.add(6, 1, 0);
        world.setBlockState(doorPos, ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH), 3);
        world.setBlockState(doorPos.up(), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH), 3);

        // Intercom button beside door (z = -1, x = 7, y = 2)
        world.setBlockState(start.add(7, 2, -1), Blocks.STONE_BUTTON.getDefaultState(), 3);

        // Cantilever Canopy (Козырёк подъезда) over porch (y = 4, x = 5..7, z in -2..-1)
        for (int x = 5; x <= 7; x++) {
            for (int z = -2; z <= -1; z++) {
                mutable.set(start.getX() + x, start.getY() + 4, start.getZ() + z);
                world.setBlockState(mutable, smoothSlab.with(SlabBlock.TYPE, SlabType.BOTTOM), 3);
            }
        }
        // Metal angled strut supporting canopy
        world.setBlockState(start.add(5, 3, -1), Blocks.LIGHTNING_ROD.getDefaultState(), 3);
        world.setBlockState(start.add(7, 3, -1), Blocks.LIGHTNING_ROD.getDefaultState(), 3);

        // Lamp under canopy
        world.setBlockState(start.add(6, 3, -1), Blocks.OCHRE_FROGLIGHT.getDefaultState(), 3);

        // Porch Bench (Скамейка у подъезда)
        world.setBlockState(start.add(4, 1, -1), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST), 3);
        // Concrete trash urn (Урна)
        world.setBlockState(start.add(8, 1, -1), Blocks.CAULDRON.getDefaultState(), 3);

        // Notice Board by Entrance
        BlockPos signPos = start.add(7, 2, 0);
        world.setBlockState(signPos, Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.NORTH), 3);
        if (world.getBlockEntity(signPos) instanceof SignBlockEntity sign) {
            SignText text = new SignText().withColor(DyeColor.BLACK)
                    .withMessage(0, Text.literal("§0ОБЪЯВЛЕНИЯ"))
                    .withMessage(1, Text.literal("§01. Травля тараканов"))
                    .withMessage(2, Text.literal("§02. Сдача макулатуры"))
                    .withMessage(3, Text.literal("§43. В подвал не лазить!"));
            sign.setText(text, true);
        }

        // 6. Realistic Protruding Balconies on Upper Floors (Stories 1 to 4)
        for (int story = 1; story <= 4; story++) {
            int baseY = story * 4 + 1;

            // Front balconies at x=2..3 and x=9..10, protruding at z = -1
            placeBalcony(world, start.add(2, baseY, -1), start.add(3, baseY, -1), random);
            placeBalcony(world, start.add(9, baseY, -1), start.add(10, baseY, -1), random);

            // Back balconies at z = length
            placeBalcony(world, start.add(2, baseY, length), start.add(3, baseY, length), random);
            placeBalcony(world, start.add(9, baseY, length), start.add(10, baseY, length), random);
        }

        // 7. Corner Metal Drainpipes
        for (int y = 1; y < height; y++) {
            world.setBlockState(start.add(0, y, -1), Blocks.CHAIN.getDefaultState(), 3);
            world.setBlockState(start.add(width - 1, y, -1), Blocks.CHAIN.getDefaultState(), 3);
        }
        world.setBlockState(start.add(0, height, -1), Blocks.HOPPER.getDefaultState(), 3);
        world.setBlockState(start.add(width - 1, height, -1), Blocks.HOPPER.getDefaultState(), 3);

        // Roof TV Antenna Masts & Vent Flues
        world.setBlockState(start.add(3, height + 1, 3), Blocks.LIGHTNING_ROD.getDefaultState(), 3);
        world.setBlockState(start.add(3, height + 2, 3), Blocks.LIGHTNING_ROD.getDefaultState(), 3);
        world.setBlockState(start.add(10, height + 1, 13), Blocks.LIGHTNING_ROD.getDefaultState(), 3);
        world.setBlockState(start.add(6, height + 1, 8), p46, 3); // Central vent flue

        // 8. Interior Staircase connecting all 5 floors
        for (int story = 0; story < 5; story++) {
            int baseY = story * 4 + 1;
            world.setBlockState(start.add(6, baseY, 8), stairsStone.with(StairsBlock.FACING, Direction.NORTH), 3);
            world.setBlockState(start.add(6, baseY + 1, 9), stairsStone.with(StairsBlock.FACING, Direction.SOUTH), 3);
            world.setBlockState(start.add(6, baseY + 3, 8), fluorLamp, 3);
        }

        // 9. Ground Floor Lobby (Вестибюль 1 этажа)
        // Bank of Blue Metal Mailboxes (Почтовые ящики кв. 1-20)
        BlockPos mailboxPos = start.add(4, 2, 2);
        world.setBlockState(mailboxPos, Blocks.BLUE_TERRACOTTA.getDefaultState(), 3);
        BlockPos mailSignPos = start.add(4, 2, 1);
        world.setBlockState(mailSignPos, Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.NORTH), 3);
        if (world.getBlockEntity(mailSignPos) instanceof SignBlockEntity sign) {
            SignText text = new SignText().withColor(DyeColor.BLUE)
                    .withMessage(0, Text.literal("§1ПОЧТА"))
                    .withMessage(1, Text.literal("§0Кв. 1 - 20"))
                    .withMessage(2, Text.literal("§0Газета «Правда»"))
                    .withMessage(3, Text.literal("§0Квитанции ЖЭК"));
            sign.setText(text, true);
        }

        // Electrical Junction Box
        BlockPos fuseboxPos = start.add(8, 2, 2);
        world.setBlockState(fuseboxPos, Blocks.BLAST_FURNACE.getDefaultState(), 3);
        BlockPos fuseSignPos = start.add(8, 2, 1);
        world.setBlockState(fuseSignPos, Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.NORTH), 3);
        if (world.getBlockEntity(fuseSignPos) instanceof SignBlockEntity sign) {
            SignText text = new SignText().withColor(DyeColor.RED)
                    .withMessage(0, Text.literal("§4ВНИМАНИЕ!"))
                    .withMessage(1, Text.literal("§0380 ВОЛЬТ"))
                    .withMessage(2, Text.literal("§0НЕ ВЛЕЗАЙ —"))
                    .withMessage(3, Text.literal("§4УБЬЁТ!"));
            sign.setText(text, true);
        }

        // 10. GP-4 Freight Elevator Shaft to Gigakhrushchyovka Middle Stratum
        // Enclosed elevator bay at x=6, z=4..5
        BlockPos stationPos = start.add(6, 1, 5);
        world.setBlockState(stationPos, ModBlocks.ELEVATOR_STATION.getDefaultState(), 3);
        world.setBlockState(stationPos.up(3), fluorLamp, 3);

        // Hanging lift cables
        world.setBlockState(stationPos.up(1), Blocks.AIR.getDefaultState(), 3);
        world.setBlockState(stationPos.up(2), Blocks.CHAIN.getDefaultState(), 3);

        // Elevator mesh gate
        world.setBlockState(start.add(6, 1, 4), ModBlocks.ELEVATOR_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.NORTH), 3);
        world.setBlockState(start.add(6, 2, 4), ModBlocks.ELEVATOR_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.NORTH), 3);

        // Elevator station technical sign
        BlockPos liftSignPos = start.add(7, 2, 4);
        world.setBlockState(liftSignPos, Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.NORTH), 3);
        if (world.getBlockEntity(liftSignPos) instanceof SignBlockEntity sign) {
            SignText text = new SignText().withColor(DyeColor.RED)
                    .withMessage(0, Text.literal("§4ПОДЪЁМНИК ГП-4"))
                    .withMessage(1, Text.literal("§0Спуск в Гигахрущ"))
                    .withMessage(2, Text.literal("§0Отметка -510м (Эт.85)"))
                    .withMessage(3, Text.literal("§4ВХОД В СИЗ!"));
            sign.setText(text, true);
        }

        // Starter Survival Supply Crate
        BlockPos cratePos = start.add(5, 1, 5);
        world.setBlockState(cratePos, Blocks.BARREL.getDefaultState(), 3);
        if (world.getBlockEntity(cratePos) instanceof BarrelBlockEntity barrel) {
            barrel.setLootTable(LootTables.VILLAGE_PLAINS_CHEST);
            barrel.setLootTableSeed(random.nextLong());
        }

        // Old CRT TV and Radio in the lobby
        world.setBlockState(start.add(4, 1, 4), ModBlocks.OLD_TV.getDefaultState(), 3);
        world.setBlockState(start.add(8, 1, 4), ModBlocks.RADIO_RECEIVER.getDefaultState(), 3);

        return true;
    }

    private void placeBalcony(StructureWorldAccess world, BlockPos pos1, BlockPos pos2, Random random) {
        BlockState smoothSlab = Blocks.SMOOTH_STONE_SLAB.getDefaultState();
        BlockState ironBars = Blocks.IRON_BARS.getDefaultState();

        // Floor slab
        world.setBlockState(pos1, smoothSlab, 3);
        world.setBlockState(pos2, smoothSlab, 3);

        // Railings
        world.setBlockState(pos1.up(), ironBars, 3);
        world.setBlockState(pos2.up(), ironBars, 3);

        // Procedural Soviet balcony clutter
        int clutter = random.nextInt(4);
        if (clutter == 0) {
            world.setBlockState(pos1.up(1), Blocks.FLOWER_POT.getDefaultState(), 3);
        } else if (clutter == 1) {
            world.setBlockState(pos2.up(1), Blocks.BARREL.getDefaultState(), 3);
        } else if (clutter == 2) {
            // Drying laundry
            world.setBlockState(pos1.up(2), Blocks.CHAIN.getDefaultState(), 3);
            world.setBlockState(pos2.up(2), Blocks.WHITE_CARPET.getDefaultState(), 3);
        }
    }
}
