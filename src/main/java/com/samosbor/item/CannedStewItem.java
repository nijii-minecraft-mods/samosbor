package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CannedStewItem extends Item implements PolymerItem {
    public static final FoodComponent FOOD = new FoodComponent.Builder()
            .nutrition(8)
            .saturationModifier(1.0f)
            .alwaysEdible()
            .build();

    public CannedStewItem() {
        super(new Item.Settings().food(FOOD).maxCount(16));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.COOKED_BEEF;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§eСвиная тушёнка ГОСТ-5284"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Армейские консервы высшего сорта из спецхрана."),
                Text.literal("§a+8 ед. сытости и высокая сытность"),
                Text.literal("§8«Слава советским животноводам!»")
        )));
        return out;
    }
}
