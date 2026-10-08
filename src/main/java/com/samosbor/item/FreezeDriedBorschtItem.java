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

public class FreezeDriedBorschtItem extends Item implements PolymerItem {
    public static final FoodComponent FOOD = new FoodComponent.Builder()
            .nutrition(6)
            .saturationModifier(0.8f)
            .alwaysEdible()
            .build();

    public FreezeDriedBorschtItem() {
        super(new Item.Settings().food(FOOD).maxCount(16));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.BEETROOT_SOUP;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cСублимированный борщ Партии"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Вакуумный пакет дегидрированного супа."),
                Text.literal("§cПитательный горячий паёк рабочих смен."),
                Text.literal("§7Снимает усталость и согревает в темноте.")
        )));
        return out;
    }
}
