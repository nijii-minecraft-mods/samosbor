package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GasMaskFilterItem extends Item implements PolymerItem {

    public GasMaskFilterItem() {
        super(new Item.Settings().maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ItemStack helmet = user.getEquippedStack(EquipmentSlot.HEAD);

        if (!world.isClient) {
            if (helmet.isOf(ModItems.GAS_MASK)) {
                if (helmet.isDamaged()) {
                    helmet.setDamage(Math.max(0, helmet.getDamage() - 150));
                    if (!user.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                    user.sendMessage(Text.literal("§a[Фильтр ГП-5К]: Фильтрующая коробка заменена (+150 прочности)."), true);
                    return TypedActionResult.success(stack);
                } else {
                    user.sendMessage(Text.literal("§7[Фильтр]: Противогаз полностью исправен."), true);
                    return TypedActionResult.pass(stack);
                }
            } else {
                user.sendMessage(Text.literal("§c[!] Наденьте противогаз для замены фильтра!"), true);
                return TypedActionResult.pass(stack);
            }
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.COAL;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§2Фильтр комбинированный ГП-5К"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Зелёная металлическая фильтрующая коробка с шихтой."),
                Text.literal("§eПКМ: ремонтирует надетый противогаз на 150 ед."),
                Text.literal("§8«Защищает от аэрозолей и паров озона»")
        )));
        return out;
    }
}
