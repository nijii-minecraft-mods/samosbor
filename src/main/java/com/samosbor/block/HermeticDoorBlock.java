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
    public static final BooleanProperty WELDED = BooleanProperty.of("welded");

    public HermeticDoorBlock() {
        super(BlockSetType.IRON, AbstractBlock.Settings.copy(Blocks.IRON_DOOR).strength(6.0f, 1200.0f));
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, net.minecraft.util.math.Direction.NORTH)
                .with(OPEN, false)
                .with(HINGE, net.minecraft.block.enums.DoorHinge.LEFT)
                .with(POWERED, false)
                .with(HALF, DoubleBlockHalf.LOWER)
                .with(SEALED, false)
                .with(WELDED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(SEALED, WELDED);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            boolean isOpen = state.get(OPEN);
            boolean isSealed = state.get(SEALED);
            boolean isWelded = state.get(WELDED);
            BlockPos otherHalfPos = state.get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
            BlockState otherState = world.getBlockState(otherHalfPos);

            // Welded doors CANNOT be operated by hand
            if (isWelded) {
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.9f, 0.5f);
                world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_FALL, SoundCategory.BLOCKS, 0.6f, 1.6f);
                player.sendMessage(Text.literal("§c[!] Гермозатвор наглухо заварен автогеном! Срежьте шов Газовым резаком."), true);
                return ActionResult.SUCCESS;
            }

            // Shift+Right Click: Toggle sealing valve (Вентиль герметизации)
            if (player.isSneaking()) {
                if (isOpen) {
                    // Close and seal simultaneously
                    BlockState newState = state.with(OPEN, false).with(SEALED, true);
                    world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
                    if (otherState.isOf(this)) {
                        world.setBlockState(otherHalfPos, otherState.with(OPEN, false).with(SEALED, true), Block.NOTIFY_LISTENERS);
                    }
                    world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.7f);
                    world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.8f, 0.6f);
                    player.sendMessage(Text.literal("§eГермозатвор плотно задраен вентилем."), true);
                    awardSealAdvancement(player);
                    return ActionResult.SUCCESS;
                }

                boolean newSealed = !isSealed;
                BlockState newState = state.with(SEALED, newSealed);
                world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
                if (otherState.isOf(this)) {
                    world.setBlockState(otherHalfPos, otherState.with(SEALED, newSealed), Block.NOTIFY_LISTENERS);
                }

                if (newSealed) {
                    world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.8f, 0.6f);
                    world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.5f);
                    player.sendMessage(Text.literal("§eГермозатвор заблокирован вентилем (герметично)."), true);
                    awardSealAdvancement(player);
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 0.8f);
                    player.sendMessage(Text.literal("§7Вентиль разблокирован."), true);
                }
                return ActionResult.SUCCESS;
            }

            // Normal Right Click: Open or Close door
            if (isSealed) {
                // Cannot open when sealed with valve
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 0.7f, 1.6f);
                world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_FALL, SoundCategory.BLOCKS, 0.5f, 1.8f);
                player.sendMessage(Text.literal("§cГермозатвор задраен вентилем! (Shift+ПКМ чтобы отпереть вентиль)"), true);
                return ActionResult.SUCCESS;
            }

            // Toggle Open/Closed
            boolean newOpen = !isOpen;
            BlockState newState = state.with(OPEN, newOpen);
            world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
            if (otherState.isOf(this)) {
                world.setBlockState(otherHalfPos, otherState.with(OPEN, newOpen), Block.NOTIFY_LISTENERS);
            }

            if (newOpen) {
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 0.8f);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.7f);
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

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (state.get(WELDED) || state.get(SEALED)) {
            return;
        }
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }

    public static boolean isSealedAt(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof HermeticDoorBlock) {
            return !state.get(OPEN) && (state.get(SEALED) || state.get(WELDED));
        }
        return false;
    }

    private void awardSealAdvancement(PlayerEntity player) {
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            var adv = serverPlayer.server.getAdvancementLoader().get(net.minecraft.util.Identifier.of("samosbor", "seal_hermetic_door"));
            if (adv != null) {
                serverPlayer.getAdvancementTracker().grantCriterion(adv, "seal_door");
            }
        }
    }
}
