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

public class ValeraDogTagItem extends Item implements PolymerItem {

    public ValeraDogTagItem() {
        super(new Item.Settings().maxCount(64));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.NAME_TAG;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6Именной жетон Ликвидатора: ВАЛЕРА"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Оплавленный стальной жетон бойца Очистного Звена 04-Б."),
                Text.literal("§c«СТ. ЛЕЙТЕНАНТ В. В. КУЗНЕЦОВ (ВАЛЕРА)»"),
                Text.literal("§4«СТАТУС: МУТИРОВАЛ ПРИ ПРОРЫВЕ БИОМАССЫ»"),
                Text.literal("§8Легендарный артефакт павшего героя Гигахруща.")
        )));
        return out;
    }
}
