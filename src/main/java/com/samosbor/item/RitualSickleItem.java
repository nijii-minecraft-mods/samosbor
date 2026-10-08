package com.samosbor.item;

import com.samosbor.block.ModBlocks;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class RitualSickleItem extends Item implements PolymerItem {

    public RitualSickleItem() {
        super(new Item.Settings().maxDamage(350));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);

        World world = attacker.getWorld();
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            // Inflict deep arterial bleeding & slowness
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 160, 0));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 0));

            serverWorld.spawnParticles(new DustParticleEffect(new Vector3f(0.8f, 0.0f, 0.0f), 1.2f),
                    target.getX(), target.getY() + 1.0, target.getZ(), 16, 0.3, 0.4, 0.3, 0.05);

            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 0.6f);
        }

        return true;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && state.isOf(ModBlocks.BIOMASS)) {
            stack.damage(1, miner, EquipmentSlot.MAINHAND);
            world.playSound(null, pos, SoundEvents.BLOCK_HONEY_BLOCK_BREAK, SoundCategory.BLOCKS, 1.0f, 0.7f);

            if (miner instanceof PlayerEntity player) {
                player.getHungerManager().add(1, 0.5f);
            }
            return true;
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        if (state.isOf(ModBlocks.BIOMASS)) {
            return 15.0f; // Slices through biomass instantly
        }
        return super.getMiningSpeed(stack, state);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.NETHERITE_HOE;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cКостяной серп Культа"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Изогнутое хищное лезвие из костей Ликвидатора."),
                Text.literal("§4Удар: вызывает глубокое кровотечение и иссушение"),
                Text.literal("§6Жатва: срезает биомассу мгновенно и питает носителя"),
                Text.literal("§8«Плоть едина с бетоном»")
        )));
        return out;
    }
}
