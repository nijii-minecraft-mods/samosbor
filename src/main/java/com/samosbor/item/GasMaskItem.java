package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class GasMaskItem extends Item implements PolymerItem, Equipment {

    public static final int MAX_DURABILITY = 300;

    public GasMaskItem() {
        super(new Item.Settings().maxDamage(MAX_DURABILITY));
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.CHAINMAIL_HELMET;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, net.minecraft.text.Text.literal("§6Гражданский противогаз ГП-5"));
        out.set(net.minecraft.component.DataComponentTypes.LORE, new net.minecraft.component.type.LoreComponent(java.util.List.of(
                net.minecraft.text.Text.literal("§7Резиновая шлем-маска с фильтрующей коробкой ГП-5К."),
                net.minecraft.text.Text.literal("§aЗащищает органы дыхания при Самосборе."),
                net.minecraft.text.Text.literal("§cРасходует прочность фильтра в ядовитой среде."),
                net.minecraft.text.Text.literal("§8«ГОСТ 12.4.041-89»")
        )));
        return out;
    }
}
