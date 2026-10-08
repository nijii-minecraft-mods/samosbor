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

public class SignalFlareItem extends Item implements PolymerItem {

    public SignalFlareItem() {
        super(new Item.Settings().maxCount(32));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.FIREWORK_ROCKET;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cСигнальная ракета СР-24 (Красная)"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Магниевый пиротехнический патрон 26-мм для СПШ-44."),
                Text.literal("§cЯркая вспышка освещает тёмные отсеки"),
                Text.literal("§6Ослепляет монстров и отпугивает мимиков.")
        )));
        return out;
    }
}
