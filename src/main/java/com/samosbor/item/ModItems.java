package com.samosbor.item;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final String MOD_ID = "samosbor";

    public static final Item FOOD_CONCENTRATE = register("food_concentrate", new FoodConcentrateItem());
    public static final Item RATION_TICKET = register("ration_ticket", new SimplePolymerItem(new Item.Settings(), Items.PAPER));
    public static final Item GAS_MASK = register("gas_mask", new GasMaskItem());
    public static final Item FLAMETHROWER = register("flamethrower", new FlamethrowerItem());
    public static final Item JP11U_COMPONENT = register("jp11u_component", new SimplePolymerItem(new Item.Settings(), Items.HEAVY_CORE));
    public static final Item CROWBAR = register("crowbar", new CrowbarItem());

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    public static void initialize() {
        // Called during mod initialization to load class
    }
}
