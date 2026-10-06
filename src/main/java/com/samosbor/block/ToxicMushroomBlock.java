package com.samosbor.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class ToxicMushroomBlock extends MushroomPlantBlock implements PolymerBlock {

    public ToxicMushroomBlock(RegistryKey<ConfiguredFeature<?, ?>> featureKey) {
        super(featureKey, AbstractBlock.Settings.copy(Blocks.RED_MUSHROOM).noCollision().breakInstantly());
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, false, true));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0, false, true));
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.RED_MUSHROOM.getDefaultState();
    }
}
