package com.samosbor.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HermeticDoorBlock extends DoorBlock implements PolymerBlock {
    public static final BooleanProperty SEALED = BooleanProperty.of("sealed");

    public HermeticDoorBlock() {
        super(BlockSetType.IRON, AbstractBlock.Settings.copy(Blocks.IRON_DOOR).strength(6.0f, 1200.0f));
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, net.minecraft.util.math.Direction.NORTH)
                .with(OPEN, false)
                .with(HINGE, net.minecraft.block.enums.DoorHinge.LEFT)
                .with(POWERED, false)
                .with(HALF, DoubleBlockHalf.LOWER)
                .with(SEALED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(SEALED);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            boolean isOpen = state.get(OPEN);
            boolean isSealed = state.get(SEALED);

            if (isOpen) {
                // If open, close it first
                BlockState newState = state.with(OPEN, false).with(SEALED, false);
                world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.7f);
                return ActionResult.SUCCESS;
            }

            // Door is closed: toggle sealed state
            boolean newSealed = !isSealed;
            BlockState newState = state.with(SEALED, newSealed);
            world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);

            // Also update other half of double block
            BlockPos otherHalfPos = state.get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
            BlockState otherState = world.getBlockState(otherHalfPos);
            if (otherState.isOf(this)) {
                world.setBlockState(otherHalfPos, otherState.with(SEALED, newSealed), Block.NOTIFY_LISTENERS);
            }

            if (newSealed) {
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.8f, 0.6f);
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.5f);
                player.sendMessage(Text.translatable("samosbor.door.sealed"), true);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 0.8f);
                player.sendMessage(Text.translatable("samosbor.door.unsealed"), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.IRON_DOOR.getDefaultState()
                .with(FACING, state.get(FACING))
                .with(HALF, state.get(HALF))
                .with(HINGE, state.get(HINGE))
                .with(OPEN, state.get(OPEN))
                .with(POWERED, state.get(POWERED));
    }

    public static boolean isSealedAt(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof HermeticDoorBlock) {
            return !state.get(OPEN) && state.get(SEALED);
        }
        return false;
    }
}
