package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class L1mHazmatItem extends Item implements PolymerItem, Equipment {

    public L1mHazmatItem() {
        super(new Item.Settings().maxDamage(600));
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.NETHERITE_CHESTPLATE;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§aЭлитный химкостюм Ликвидатора Л-1М"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Тяжёлый прорезиненный костюм высшей категории защиты ГО."),
                Text.literal("§a100% иммунитет к ядовитому газу Самосбора"),
                Text.literal("§eЗащита от термических ожогов и кислотных брызг")
        )));
        return out;
    }
}
