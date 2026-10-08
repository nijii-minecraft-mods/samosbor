package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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

public class BiomassHeartItem extends Item implements PolymerItem {

    public BiomassHeartItem() {
        super(new Item.Settings().maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 400, 2));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 600, 1));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 1));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 160, 0));

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.2f, 0.8f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 1.0f, 0.6f);

            user.sendMessage(Text.literal("§4[Сердце Биомассы]: Вы поглотили пульсирующий орган. Мускулы наливаются нечеловеческой силой!"), true);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            user.getItemCooldownManager().set(this, 100);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.HEART_OF_THE_SEA;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§4Пульсирующее Сердце Биомассы"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Трофей, вырванный из груди Валеры / Древнего Мимика."),
                Text.literal("§eПКМ: поглотить плоть органа"),
                Text.literal("§aДарует Регенерацию III, Силу II и Сопротивление II"),
                Text.literal("§8«Оно всё ещё бьётся в ладони»")
        )));
        return out;
    }
}
