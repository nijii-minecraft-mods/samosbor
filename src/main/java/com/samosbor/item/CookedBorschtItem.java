package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CookedBorschtItem extends Item implements PolymerItem {

    public static final FoodComponent FOOD = new FoodComponent.Builder()
            .nutrition(10)
            .saturationModifier(1.2f)
            .alwaysEdible()
            .build();

    public CookedBorschtItem() {
        super(new Item.Settings().food(FOOD).maxCount(16));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 300, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 0));
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.8f, 1.0f);
        }

        ItemStack result = super.finishUsing(stack, world, user);

        if (user instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
            ItemStack bowl = new ItemStack(Items.BOWL);
            if (!player.getInventory().insertStack(bowl)) {
                player.dropItem(bowl, false);
            }
        }

        return result;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.BEETROOT_SOUP;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cГорячий наваристый борщ"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Свежеприготовленный на конфорке горячий суп."),
                Text.literal("§a+10 сытости, Регенерация I (15 сек), Сопротивление I (30 сек)"),
                Text.literal("§8«Согревает душу даже в бетонном аду Гигахруща»")
        )));
        return out;
    }
}
