package com.samosbor.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class EmergencySirenBlock extends Block implements PolymerBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public EmergencySirenBlock() {
        super(AbstractBlock.Settings.copy(Blocks.REDSTONE_LAMP).strength(2.5f).luminance(s -> s.get(LIT) ? 15 : 0));
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            boolean isLit = state.get(LIT);
            boolean newLit = !isLit;
            world.setBlockState(pos, state.with(LIT, newLit), Block.NOTIFY_LISTENERS);

            if (newLit) {
                world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.BLOCKS, 1.2f, 0.5f);
                world.playSound(null, pos, SoundEvents.EVENT_RAID_HORN.value(), SoundCategory.BLOCKS, 1.0f, 1.0f);
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.05);
                }
                player.sendMessage(Text.literal("§c[СИРЕНА С-40]: §4ВНИМАНИЕ! РЕЖИМ ТРЕВОГИ АКТИВИРОВАН!"), true);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.8f, 0.5f);
                player.sendMessage(Text.literal("§7[Сирена С-40]: Сигнал тревоги отключен."), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        boolean isLit = state.contains(LIT) && state.get(LIT);
        return Blocks.REDSTONE_LAMP.getDefaultState().with(RedstoneLampBlock.LIT, isLit);
    }
}
