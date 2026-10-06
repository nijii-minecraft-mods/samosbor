package com.samosbor.item;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class FoodConcentrateItem extends SimplePolymerItem {
    public static final FoodComponent FOOD = new FoodComponent.Builder()
            .nutrition(7)
            .saturationModifier(0.9f)
            .alwaysEdible()
            .build();

    public FoodConcentrateItem() {
        super(new Item.Settings().food(FOOD), Items.DRIED_KELP);
    }
}
