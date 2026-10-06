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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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

    private static Block register(String name, Block block, Item vanillaItem) {
        Identifier id = Identifier.of(MOD_ID, name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new PolymerBlockItem(block, new Item.Settings(), vanillaItem));
        return block;
    }

    public static void initialize() {
        // Called during mod initialization to load class
    }
}
