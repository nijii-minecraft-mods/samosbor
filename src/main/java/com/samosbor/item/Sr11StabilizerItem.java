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

public class Sr11StabilizerItem extends Item implements PolymerItem {

    public Sr11StabilizerItem() {
        super(new Item.Settings().maxCount(16));
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 20; // Fast drinking / injection (1 second)
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
            // Remove negative mental/sensory effects
            user.removeStatusEffect(StatusEffects.DARKNESS);
            user.removeStatusEffect(StatusEffects.BLINDNESS);
            user.removeStatusEffect(StatusEffects.NAUSEA);
            user.removeStatusEffect(StatusEffects.SLOWNESS);
            user.removeStatusEffect(StatusEffects.MINING_FATIGUE);

            // Grant stabilizer effects
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 3600, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1200, 0));

            serverWorld.spawnParticles(ParticleTypes.INSTANT_EFFECT, user.getX(), user.getY() + 1.0, user.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.9f, 1.5f);

            if (user instanceof ServerPlayerEntity sp) {
                sp.sendMessage(Text.literal("§b[СР-11]: Сознание очищено. Слуховые галлюцинации и паника подавлены."), true);
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
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§b§lСтабилизатор Рассудка СР-11"));
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§3[Ампула Психостабилизации «СР-11»]"),
                Text.literal("§bВоенно-медицинский стимулятор Ликвидаторов."),
                Text.literal("§7Снимает: темноту, слепоту, тошноту, замедление и панику."),
                Text.literal("§eДарует: ночное зрение (3 мин) и сопротивление урону (1 мин).")
        )));
        return out;
    }
}
