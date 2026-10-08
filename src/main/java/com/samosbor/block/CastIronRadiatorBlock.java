package com.samosbor.block;

import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CastIronRadiatorBlock extends Block implements PolymerHeadBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    // Soviet Cast-Iron Radiator MS-140-500
    public static final String RADIATOR_SKIN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDVhOWQ4MjEzYjJjNmE0NjFiMjQ0NzllMGE4MTFjMGJlMjIwZjc4Yzg5NDJiMDgzODE4NmVhNGI1ZWE1YjZkMSJ9fX0=";

    public CastIronRadiatorBlock() {
        super(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).strength(3.0f, 6.0f));
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 0.5f, 1.8f);
            world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.4f, 1.5f);
            player.sendMessage(Text.literal("§7[Чугунный радиатор МС-140]: §8*Гул пара и глухой стук теплоносителя в секциях...*"), true);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public String getPolymerSkinValue(BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return RADIATOR_SKIN;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        Direction facing = state.contains(FACING) ? state.get(FACING) : Direction.NORTH;
        int rot = switch (facing) {
            case NORTH -> 8;
            case SOUTH -> 0;
            case WEST -> 4;
            case EAST -> 12;
            default -> 8;
        };
        return Blocks.PLAYER_HEAD.getDefaultState().with(SkullBlock.ROTATION, rot);
    }
}
