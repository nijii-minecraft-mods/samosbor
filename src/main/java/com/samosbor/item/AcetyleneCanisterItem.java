package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AcetyleneCanisterItem extends Item implements PolymerItem {

    public AcetyleneCanisterItem() {
        super(new Item.Settings().maxCount(16));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.FIREWORK_STAR;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cБаллон с ацетиленом АЦ-5"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Сжатый горючий газ для газовых резаков"),
                Text.literal("§7и тяжёлых огнемётов РОКС-3."),
                Text.literal("§4Взрывоопасно! Хранить вдали от огня.")
        )));
        return out;
    }
}
