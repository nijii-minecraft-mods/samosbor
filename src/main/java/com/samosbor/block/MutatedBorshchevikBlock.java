package com.samosbor.block;

import com.samosbor.item.ModItems;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MutatedBorshchevikBlock extends Block implements PolymerBlock {

    public MutatedBorshchevikBlock() {
        super(AbstractBlock.Settings.copy(Blocks.SWEET_BERRY_BUSH).noCollision().ticksRandomly().luminance(s -> 3));
    }


    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.SWEET_BERRY_BUSH.getDefaultState();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof LivingEntity living) {
            // Apply severe phototoxic contact burns
            living.damage(world.getDamageSources().cactus(), 2.0f);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 120, 1));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));

            if (world instanceof ServerWorld serverWorld && serverWorld.random.nextInt(10) == 0) {
                serverWorld.spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.02);
                serverWorld.playSound(null, pos, SoundEvents.BLOCK_SLIME_BLOCK_STEP, SoundCategory.BLOCKS, 0.8f, 0.6f);
            }
        }
    }
}
