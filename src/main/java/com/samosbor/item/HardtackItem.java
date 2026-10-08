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

public class HardtackItem extends Item implements PolymerItem {
    public static final FoodComponent FOOD = new FoodComponent.Builder()
            .nutrition(3)
            .saturationModifier(0.6f)
            .snack()
            .alwaysEdible()
            .build();

    public HardtackItem() {
        super(new Item.Settings().food(FOOD).maxCount(64));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.COOKIE;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6Галеты армейские «Арктика»"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Сухие пресные армейские галеты повышенной стойкости."),
                Text.literal("§eБыстрый перекус на ходу.")
        )));
        return out;
    }
}
