package com.samosbor.worldgen;

import com.mojang.serialization.Codec;
import com.samosbor.block.ModBlocks;
import com.samosbor.item.ModItems;
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
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

        // 1. Surface validation
        int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        if (groundY < 60 || groundY > 120) return false;

        BlockPos start = new BlockPos(origin.getX(), groundY, origin.getZ());
        if (world.getBlockState(start.down()).isAir()) return false;

        // Series 1-510 dimensions: width=13 (X:0..12), length=17 (Z:0..16), height=21 (5 floors x 4 + slabs)
        int width = 13;
        int length = 17;
        int height = 21;

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

        // 2. Solid Concrete Foundation (Y = -6 to -1)
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = -6; y < 0; y++) {
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
                    boolean isVentGrate = ((x == 3 || x == 9) && (z == 0 || z == length - 1))
                            || ((z == 4 || z == 12) && (x == 0 || x == width - 1));
                    world.setBlockState(mutable, isVentGrate ? ironBars : plinth, 3);
                } else {
                    world.setBlockState(mutable, p46, 3);
                }
            }
        }

        // 4. Exterior Building Shell & Inter-floor Slabs (Y = 1 to height)
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
                        // Floor slab: open hole only for the enclosed stairwell shaft (x in 5..7, z in 7..10)
                        boolean isStairHole = (x >= 5 && x <= 7 && z >= 7 && z <= 10);
                        world.setBlockState(mutable, isStairHole ? air : p46, 3);
                    } else if (isPerimeter) {
                        // Entrance Doorway at Front Facade (z=0, x=6, y=1..2)
                        if (z == 0 && x == 6 && (y == 1 || y == 2)) {
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
                            // Two-tone facade: Ground floor painted, upper floors weathered panel
                            world.setBlockState(mutable, (story == 0) ? painted : p46, 3);
                        }
                    } else {
                        // Hollow out interior initially
                        world.setBlockState(mutable, air, 3);
                    }
                }
            }
        }

        // 5. Entrance Porch & Canopy Group
        for (int x = 5; x <= 7; x++) {
            mutable.set(start.getX() + x, start.getY() + 1, start.getZ() - 1);
            world.setBlockState(mutable, stairsStone.with(StairsBlock.FACING, Direction.SOUTH), 3);
        }

        // Main entrance hermetic door (z = 0, x = 6, y = 1..2)
        BlockPos frontDoorPos = start.add(6, 1, 0);
        world.setBlockState(frontDoorPos, ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH), 3);
        world.setBlockState(frontDoorPos.up(), ModBlocks.HERMETIC_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH), 3);

        // Canopy over porch
        for (int x = 5; x <= 7; x++) {
            for (int z = -2; z <= -1; z++) {
                world.setBlockState(start.add(x, 4, z), smoothSlab.with(SlabBlock.TYPE, SlabType.BOTTOM), 3);
            }
        }
        world.setBlockState(start.add(5, 3, -1), Blocks.LIGHTNING_ROD.getDefaultState(), 3);
        world.setBlockState(start.add(7, 3, -1), Blocks.LIGHTNING_ROD.getDefaultState(), 3);
        world.setBlockState(start.add(6, 3, -1), Blocks.OCHRE_FROGLIGHT.getDefaultState(), 3);
        world.setBlockState(start.add(4, 1, -1), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST), 3);
        world.setBlockState(start.add(8, 1, -1), Blocks.CAULDRON.getDefaultState(), 3);

        // Entrance Notice Board
        BlockPos signPos = start.add(7, 2, 0);
        world.setBlockState(signPos, Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.NORTH), 3);
        setSignSafely(world, signPos, new SignText().withColor(DyeColor.BLACK)
                .withMessage(0, Text.literal("§0ДОМ 14/2"))
                .withMessage(1, Text.literal("§0Кв. 1 - 20"))
                .withMessage(2, Text.literal("§4ВНИМАНИЕ!"))
                .withMessage(3, Text.literal("§4ПОДВАЛ ЗАКРЫТ")));

        // 6. Protruding Balconies on Stories 1 to 4
        for (int story = 1; story <= 4; story++) {
            int baseY = story * 4 + 1;
            placeBalcony(world, start.add(2, baseY, -1), start.add(3, baseY, -1), random);
            placeBalcony(world, start.add(9, baseY, -1), start.add(10, baseY, -1), random);
            placeBalcony(world, start.add(2, baseY, length), start.add(3, baseY, length), random);
            placeBalcony(world, start.add(9, baseY, length), start.add(10, baseY, length), random);
        }

        // 7. Roof TV Antennas, Drainpipes & Elevator Penthouse
        for (int y = 1; y < height; y++) {
            world.setBlockState(start.add(0, y, -1), Blocks.CHAIN.getDefaultState(), 3);
            world.setBlockState(start.add(width - 1, y, -1), Blocks.CHAIN.getDefaultState(), 3);
        }
        world.setBlockState(start.add(0, height, -1), Blocks.HOPPER.getDefaultState(), 3);
        world.setBlockState(start.add(width - 1, height, -1), Blocks.HOPPER.getDefaultState(), 3);

        // Roof Elevator Machine Room Penthouse over x=5..7, z=3..5, y=height..height+3
        for (int x = 5; x <= 7; x++) {
            for (int z = 3; z <= 5; z++) {
                for (int y = height; y <= height + 3; y++) {
                    boolean isWall = (x == 5 || x == 7 || z == 3 || z == 5);
                    if (y == height + 3) {
                        world.setBlockState(start.add(x, y, z), p46, 3);
                    } else if (isWall) {
                        world.setBlockState(start.add(x, y, z), (y == height + 2 && (x == 6 || z == 4)) ? ironBars : p46, 3);
                    } else {
                        world.setBlockState(start.add(x, y, z), air, 3);
                    }
                }
            }
        }
        world.setBlockState(start.add(3, height + 1, 3), Blocks.LIGHTNING_ROD.getDefaultState(), 3);
        world.setBlockState(start.add(3, height + 2, 3), Blocks.LIGHTNING_ROD.getDefaultState(), 3);
        world.setBlockState(start.add(9, height + 1, 13), Blocks.LIGHTNING_ROD.getDefaultState(), 3);

        // 8. STRUCTURAL CORE: Enclosed Freight Elevator Shaft & Stairwell Shaft across all floors
        for (int story = 0; story < 5; story++) {
            int baseY = story * 4 + 1;

            // --- A. Enclosed Elevator Shaft (x=5..7, z=3..5) ---
            for (int y = baseY; y < baseY + 4; y++) {
                // Side enclosing walls
                world.setBlockState(start.add(4, y, 3), p46, 3);
                world.setBlockState(start.add(4, y, 4), p46, 3);
                world.setBlockState(start.add(4, y, 5), p46, 3);
                world.setBlockState(start.add(8, y, 3), p46, 3);
                world.setBlockState(start.add(8, y, 4), p46, 3);
                world.setBlockState(start.add(8, y, 5), p46, 3);
                // Back dividing wall between elevator and stairwell
                world.setBlockState(start.add(5, y, 6), p46, 3);
                world.setBlockState(start.add(6, y, 6), p46, 3);
                world.setBlockState(start.add(7, y, 6), p46, 3);
            }

            // --- B. Enclosed Stairwell Shaft (x=5..7, z=6..11) ---
            for (int y = baseY; y < baseY + 4; y++) {
                // Side enclosing walls dividing stairwell from left and right apartments
                for (int z = 6; z <= 11; z++) {
                    world.setBlockState(start.add(4, y, z), p46, 3);
                    world.setBlockState(start.add(8, y, z), p46, 3);
                }
                // Rear wall dividing stairwell from rear apartment space
                world.setBlockState(start.add(5, y, 11), p46, 3);
                world.setBlockState(start.add(6, y, 11), p46, 3);
                world.setBlockState(start.add(7, y, 11), p46, 3);
            }

            // --- C. Real Stair Flights & Landings ---
            // Landing platform at z=7 across x=5..7
            world.setBlockState(start.add(5, baseY, 7), p46, 3);
            world.setBlockState(start.add(6, baseY, 7), p46, 3);
            world.setBlockState(start.add(7, baseY, 7), p46, 3);

            // 4-step flight of stairs rising towards upper landing
            if (story < 4) {
                world.setBlockState(start.add(6, baseY, 8), stairsStone.with(StairsBlock.FACING, Direction.SOUTH), 3);
                world.setBlockState(start.add(6, baseY + 1, 9), stairsStone.with(StairsBlock.FACING, Direction.SOUTH), 3);
                world.setBlockState(start.add(6, baseY + 2, 10), stairsStone.with(StairsBlock.FACING, Direction.SOUTH), 3);
                // Intermediate top landing at z=10
                world.setBlockState(start.add(5, baseY + 3, 10), p46, 3);
                world.setBlockState(start.add(6, baseY + 3, 10), p46, 3);
                world.setBlockState(start.add(7, baseY + 3, 10), p46, 3);
                // Railing along staircase
                world.setBlockState(start.add(5, baseY + 1, 8), ironBars, 3);
                world.setBlockState(start.add(5, baseY + 2, 9), ironBars, 3);
            }

            // Garbage chute duct on the stair landing (x=7, z=7)
            world.setBlockState(start.add(7, baseY + 1, 7), ModBlocks.GARBAGE_CHUTE.getDefaultState(), 3);
            world.setBlockState(start.add(7, baseY + 2, 7), p46, 3);
            world.setBlockState(start.add(7, baseY + 3, 7), p46, 3);

            // Ceiling lamp on stair landing
            world.setBlockState(start.add(6, baseY + 3, 7), fluorLamp, 3);

            // Doors from landing into Left and Right apartments
            BlockPos leftAptDoor = start.add(4, baseY, 8);
            world.setBlockState(leftAptDoor, Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.WEST), 3);
            world.setBlockState(leftAptDoor.up(), Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.WEST), 3);

            BlockPos rightAptDoor = start.add(8, baseY, 8);
            world.setBlockState(rightAptDoor, Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.EAST), 3);
            world.setBlockState(rightAptDoor.up(), Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.EAST), 3);

            // --- D. Detailed Apartment Furnishings on Stories 0 to 4 ---
            furnishApartments(world, start, baseY, story, random);
        }

        // 9. GROUND FLOOR ENTRANCE VESTIBULE (Story 0, z=1..2, x=5..7)
        // Bank of mailboxes on left wall (x=4, z=1..2)
        world.setBlockState(start.add(4, 1, 1), Blocks.BLUE_TERRACOTTA.getDefaultState(), 3);
        world.setBlockState(start.add(4, 2, 1), Blocks.BLUE_TERRACOTTA.getDefaultState(), 3);
        BlockPos mailSign = start.add(4, 2, 2);
        world.setBlockState(mailSign, Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.EAST), 3);
        setSignSafely(world, mailSign, new SignText().withColor(DyeColor.BLUE)
                .withMessage(0, Text.literal("§1ПОЧТА"))
                .withMessage(1, Text.literal("§0Кв. 1 - 20"))
                .withMessage(2, Text.literal("§0Газета «Правда»"))
                .withMessage(3, Text.literal("§0Извещения ЖЭК")));

        // Electrical Junction Box on right wall (x=8, z=1..2)
        world.setBlockState(start.add(8, 1, 1), Blocks.BLAST_FURNACE.getDefaultState(), 3);
        world.setBlockState(start.add(8, 2, 1), Blocks.IRON_BARS.getDefaultState(), 3);
        BlockPos fuseSign = start.add(8, 2, 2);
        world.setBlockState(fuseSign, Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.WEST), 3);
        setSignSafely(world, fuseSign, new SignText().withColor(DyeColor.RED)
                .withMessage(0, Text.literal("§4ВНИМАНИЕ!"))
                .withMessage(1, Text.literal("§0380 ВОЛЬТ"))
                .withMessage(2, Text.literal("§0НЕ ВЛЕЗАЙ —"))
                .withMessage(3, Text.literal("§4УБЬЁТ!")));

        // 10. GP-4 FREIGHT ELEVATOR CABIN (Story 0, x=5..7, z=3..5)
        // Front mesh wall facing vestibule at z=2
        world.setBlockState(start.add(5, 1, 2), ironBars, 3);
        world.setBlockState(start.add(5, 2, 2), ironBars, 3);
        world.setBlockState(start.add(7, 1, 2), ironBars, 3);
        world.setBlockState(start.add(7, 2, 2), ironBars, 3);

        // Elevator entrance copper door at x=6, z=2
        BlockPos elevDoorPos = start.add(6, 1, 2);
        world.setBlockState(elevDoorPos, ModBlocks.ELEVATOR_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.NORTH), 3);
        world.setBlockState(elevDoorPos.up(), ModBlocks.ELEVATOR_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.NORTH), 3);

        // Inside Elevator Cabin:
        BlockPos stationPos = start.add(6, 1, 4);
        world.setBlockState(stationPos, ModBlocks.ELEVATOR_STATION.getDefaultState(), 3); // Lodestone teleport core
        world.setBlockState(stationPos.up(1), air, 3);
        world.setBlockState(stationPos.up(2), Blocks.CHAIN.getDefaultState(), 3); // Winch cable
        world.setBlockState(stationPos.up(3), fluorLamp, 3); // Cabin ceiling lamp

        // Elevator control panel & technical sign on the cabin inside wall
        world.setBlockState(start.add(5, 2, 4), ModBlocks.ELEVATOR_PANEL.getDefaultState(), 3);
        BlockPos liftSign = start.add(7, 2, 4);
        world.setBlockState(liftSign, Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING, Direction.WEST), 3);
        setSignSafely(world, liftSign, new SignText().withColor(DyeColor.RED)
                .withMessage(0, Text.literal("§4ПОДЪЁМНИК ГП-4"))
                .withMessage(1, Text.literal("§0Спуск в Гигахрущ"))
                .withMessage(2, Text.literal("§0Шахта: Эт. 2-165"))
                .withMessage(3, Text.literal("§4ВХОД В СИЗ!")));

        // Civil Defense Starter Provision Crate inside vestibule
        BlockPos cratePos = start.add(5, 1, 1);
        world.setBlockState(cratePos, Blocks.BARREL.getDefaultState(), 3);
        if (world.getBlockEntity(cratePos) instanceof BarrelBlockEntity barrel) {
            barrel.setStack(0, new ItemStack(ModItems.FOOD_CONCENTRATE, 4));
            barrel.setStack(1, new ItemStack(ModItems.GAS_MASK_FILTER, 2));
            barrel.setStack(2, new ItemStack(ModItems.BLUE_TAPE, 1));
            barrel.setStack(3, new ItemStack(Items.TORCH, 8));
        }

        return true;
    }

    private void furnishApartments(StructureWorldAccess world, BlockPos start, int baseY, int story, Random random) {
        BlockState p46 = ModBlocks.P46_PANEL.getDefaultState();
        BlockState carpet = ModBlocks.WALL_CARPET.getDefaultState();

        // Partition Wall dividing Living Room from Kitchen in Left Apartment at z=11, x=1..3
        for (int x = 1; x <= 3; x++) {
            for (int y = baseY; y < baseY + 3; y++) {
                world.setBlockState(start.add(x, y, 11), p46, 3);
            }
        }
        world.setBlockState(start.add(2, baseY, 11), Blocks.AIR.getDefaultState(), 3);
        world.setBlockState(start.add(2, baseY + 1, 11), Blocks.AIR.getDefaultState(), 3);

        // Partition Wall in Right Apartment at z=11, x=9..11
        for (int x = 9; x <= 11; x++) {
            for (int y = baseY; y < baseY + 3; y++) {
                world.setBlockState(start.add(x, y, 11), p46, 3);
            }
        }
        world.setBlockState(start.add(10, baseY, 11), Blocks.AIR.getDefaultState(), 3);
        world.setBlockState(start.add(10, baseY + 1, 11), Blocks.AIR.getDefaultState(), 3);

        // --- LEFT APARTMENT (Living Room z=6..10, Kitchen z=12..15) ---
        // 1. Kitchen (z=12..15, x=1..3):
        world.setBlockState(start.add(1, baseY, 14), ModBlocks.GAS_STOVE.getDefaultState(), 3);
        world.setBlockState(start.add(1, baseY, 13), ModBlocks.RUSTY_SINK.getDefaultState(), 3);
        world.setBlockState(start.add(1, baseY + 2, 14), Blocks.BARREL.getDefaultState(), 3);
        world.setBlockState(start.add(3, baseY, 13), Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM), 3);

        // 2. Living Room (z=6..10, x=1..3):
        world.setBlockState(start.add(2, baseY + 1, 6), carpet, 3);
        world.setBlockState(start.add(1, baseY, 8), Blocks.OAK_PLANKS.getDefaultState(), 3);
        if (story % 2 == 0) {
            world.setBlockState(start.add(1, baseY + 1, 8), ModBlocks.OLD_TV.getDefaultState(), 3);
        } else {
            world.setBlockState(start.add(1, baseY + 1, 8), ModBlocks.RADIO_RECEIVER.getDefaultState(), 3);
        }
        world.setBlockState(start.add(3, baseY, 8), Blocks.OAK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST), 3);
        world.setBlockState(start.add(1, baseY, 12), ModBlocks.CAST_IRON_RADIATOR.getDefaultState(), 3);

        // --- RIGHT APARTMENT (Living Room z=6..10, Bedroom/Store z=12..15) ---
        world.setBlockState(start.add(11, baseY, 14), Blocks.RED_BED.getDefaultState(), 3);
        world.setBlockState(start.add(11, baseY, 8), Blocks.BOOKSHELF.getDefaultState(), 3);
        world.setBlockState(start.add(11, baseY + 1, 8), Blocks.BOOKSHELF.getDefaultState(), 3);
        world.setBlockState(start.add(9, baseY, 13), Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM), 3);
        world.setBlockState(start.add(11, baseY, 12), ModBlocks.CAST_IRON_RADIATOR.getDefaultState(), 3);

        // Storage Chest with Soviet Domestic Loot
        BlockPos chestPos = start.add(11, baseY, 9);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 3);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModItems.HARDTACK, 3 + random.nextInt(4)));
            chest.setStack(1, new ItemStack(ModItems.RATION_TICKET, 1 + random.nextInt(3)));
            if (random.nextInt(3) == 0) chest.setStack(2, new ItemStack(ModItems.CANNED_STEW, 1));
            if (random.nextInt(4) == 0) chest.setStack(3, new ItemStack(ModItems.BANDAGE, 2));
            if (random.nextInt(5) == 0) chest.setStack(4, new ItemStack(ModItems.BLUE_TAPE, 1));
        }
    }

    private void placeBalcony(StructureWorldAccess world, BlockPos pos1, BlockPos pos2, Random random) {
        BlockState smoothSlab = Blocks.SMOOTH_STONE_SLAB.getDefaultState();
        BlockState ironBars = Blocks.IRON_BARS.getDefaultState();

        world.setBlockState(pos1, smoothSlab, 3);
        world.setBlockState(pos2, smoothSlab, 3);

        world.setBlockState(pos1.up(), ironBars, 3);
        world.setBlockState(pos2.up(), ironBars, 3);

        int clutter = random.nextInt(4);
        if (clutter == 0) {
            world.setBlockState(pos1.up(1), Blocks.FLOWER_POT.getDefaultState(), 3);
        } else if (clutter == 1) {
            world.setBlockState(pos2.up(1), Blocks.BARREL.getDefaultState(), 3);
        } else if (clutter == 2) {
            world.setBlockState(pos1.up(2), Blocks.CHAIN.getDefaultState(), 3);
            world.setBlockState(pos2.up(2), Blocks.WHITE_CARPET.getDefaultState(), 3);
        }
    }

    private void setSignSafely(StructureWorldAccess world, BlockPos pos, SignText text) {
        try {
            if (world.getBlockEntity(pos) instanceof SignBlockEntity sign) {
                sign.setText(text, true);
            }
        } catch (Throwable ignored) {
            // Silently ignore during worldgen decoration to prevent deadlocks
        }
    }
}