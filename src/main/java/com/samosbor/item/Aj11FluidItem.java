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
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Aj11FluidItem extends Item implements PolymerItem {

    public Aj11FluidItem() {
        super(new Item.Settings().maxCount(16));
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 20;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 6000, 0)); // 5 min
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6000, 0)); // 5 min
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 3600, 1)); // 3 min Haste II

            serverWorld.spawnParticles(ParticleTypes.FALLING_HONEY, user.getX(), user.getY() + 1.0, user.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.PLAYERS, 1.0f, 0.8f);

            if (user instanceof ServerPlayerEntity sp) {
                sp.sendMessage(Text.literal("§6[АЖ-11]: Легкие заполнены гидростатической суспензией. Дыхание в жидких средах активно."), true);
            }
        }

        if (user instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
            stack.decrement(1);
        }
        return stack;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.POTION;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6§lАмниотический Раствор АЖ-11"));
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§e[Инкубационная Суспензия «АЖ-11»]"),
                Text.literal("§6Жидкость из стазис-аквариумов НИИ био-структур."),
                Text.literal("§7Позволяет дышать в любых агрессивных и затопленных средах."),
                Text.literal("§bДарует: подводное дыхание (5 мин), огнестойкость (5 мин) и спешку II.")
        )));
        return out;
    }
}
