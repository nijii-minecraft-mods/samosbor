package com.samosbor.block;

import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BiomassBlock extends SimplePolymerBlock {

    public BiomassBlock() {
        super(AbstractBlock.Settings.copy(Blocks.NETHER_WART_BLOCK).strength(1.2f, 2.0f), Blocks.NETHER_WART_BLOCK);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        // Slow down movement when walking over biomass
        entity.slowMovement(state, new Vec3d(0.8, 0.8, 0.8));
        if (world.random.nextInt(30) == 0 && !world.isClient) {
            world.playSound(null, pos, SoundEvents.BLOCK_SLIME_BLOCK_STEP, SoundCategory.BLOCKS, 0.6f, 0.8f);
        }
        super.onSteppedOn(world, pos, state, entity);
    }
}
