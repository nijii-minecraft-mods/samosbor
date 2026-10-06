package com.samosbor.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EmergencyLampBlock extends Block implements PolymerBlock {
    public static final BooleanProperty LIT = Properties.LIT;

    public EmergencyLampBlock() {
        super(AbstractBlock.Settings.copy(Blocks.REDSTONE_LAMP)
                .luminance(state -> state.get(LIT) ? 15 : 0));
        this.setDefaultState(this.stateManager.getDefaultState().with(LIT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(LIT, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) {
            boolean powered = world.isReceivingRedstonePower(pos);
            if (powered != state.get(LIT)) {
                world.setBlockState(pos, state.with(LIT, powered), Block.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.REDSTONE_LAMP.getDefaultState().with(Properties.LIT, state.get(LIT));
    }
}
