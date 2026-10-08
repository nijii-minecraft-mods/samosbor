package com.samosbor.block;

import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

public class ModBlocks {
    public static final String MOD_ID = "samosbor";

    public static final Block P46_PANEL = register("p46_panel", new P46PanelBlock(), Items.CHISELED_STONE_BRICKS);
    public static final Block PAINTED_PANEL = register("painted_panel", new PaintedPanelBlock(), Items.CYAN_TERRACOTTA);
    public static final Block HERMETIC_DOOR = register("hermetic_door", new HermeticDoorBlock(), Items.IRON_DOOR);
    public static final Block EMERGENCY_LAMP = register("emergency_lamp", new EmergencyLampBlock(), Items.REDSTONE_LAMP);
    public static final Block FLUORESCENT_LAMP = register("fluorescent_lamp", new SimplePolymerBlock(AbstractBlock.Settings.copy(Blocks.SEA_LANTERN), Blocks.SEA_LANTERN), Items.SEA_LANTERN);
    public static final Block ELEVATOR_STATION = register("elevator_station", new ElevatorStationBlock(), Items.LODESTONE);
    public static final Block FOOD_DISPENSER = register("food_dispenser", new FoodDispenserBlock(), Items.DISPENSER);
    public static final Block BIOMASS = register("biomass", new BiomassBlock(), Items.NETHER_WART_BLOCK);
    public static final Block TOXIC_MUSHROOM = register("toxic_mushroom", new ToxicMushroomBlock(TreeConfiguredFeatures.HUGE_RED_MUSHROOM), Items.RED_MUSHROOM);

    // Domestic & Soviet furniture blocks
    public static final Block WALL_CARPET = register("wall_carpet", new WallCarpetBlock(), Items.RED_GLAZED_TERRACOTTA);
    public static final Block OLD_TV = registerHead("old_tv", new OldTvBlock());
    public static final Block RADIO_RECEIVER = registerHead("radio_receiver", new RadioReceiverBlock());
    public static final Block CAST_IRON_RADIATOR = register("cast_iron_radiator", new CastIronRadiatorBlock(), Items.ANVIL);
    public static final Block GAS_STOVE = register("gas_stove", new GasStoveBlock(), Items.SMOKER);
    public static final Block RUSTY_SINK = register("rusty_sink", new RustySinkBlock(), Items.CAULDRON);
    public static final Block GARBAGE_CHUTE = register("garbage_chute", new GarbageChuteBlock(), Items.HOPPER);

    // Elevator system blocks
    public static final Block ELEVATOR_DOOR = register("elevator_door", new ElevatorDoorBlock(), Items.COPPER_DOOR);
    public static final Block ELEVATOR_PANEL = register("elevator_panel", new ElevatorPanelBlock(), Items.OBSERVER);

    private static Block register(String name, Block block, Item vanillaItem) {
        Identifier id = Identifier.of(MOD_ID, name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new PolymerBlockItem(block, new Item.Settings(), vanillaItem));
        return block;
    }

    private static <T extends Block & eu.pb4.polymer.core.api.block.PolymerHeadBlock> Block registerHead(String name, T block) {
        Identifier id = Identifier.of(MOD_ID, name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new eu.pb4.polymer.core.api.item.PolymerHeadBlockItem(block, new Item.Settings()));
        return block;
    }

    public static void initialize() {
        // Called during mod initialization to load class
    }
}
