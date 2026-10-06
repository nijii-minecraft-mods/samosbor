package com.samosbor.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class ElevatorDoorBlock extends DoorBlock implements PolymerBlock {

    public ElevatorDoorBlock() {
        super(BlockSetType.COPPER, AbstractBlock.Settings.copy(Blocks.COPPER_DOOR).strength(5.0f, 800.0f));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.WEATHERED_COPPER_DOOR.getDefaultState()
                .with(FACING, state.get(FACING))
                .with(HALF, state.get(HALF))
                .with(HINGE, state.get(HINGE))
                .with(OPEN, state.get(OPEN))
                .with(POWERED, state.get(POWERED));
    }
}
