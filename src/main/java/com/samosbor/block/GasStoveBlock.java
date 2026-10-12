package com.samosbor.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SmokerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
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

public class GasStoveBlock extends Block implements PolymerBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public GasStoveBlock() {
        super(AbstractBlock.Settings.copy(Blocks.SMOKER).strength(2.5f).luminance(s -> s.get(LIT) ? 8 : 0));
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
            ItemStack held = player.getMainHandStack();

            // Cooking Freeze-Dried Borscht
            if (held.isOf(com.samosbor.item.ModItems.FREEZE_DRIED_BORSCHT)) {
                if (isLit) {
                    if (!player.getAbilities().creativeMode) {
                        held.decrement(1);
                    }
                    ItemStack cooked = new ItemStack(com.samosbor.item.ModItems.COOKED_BORSCHT);
                    if (!player.getInventory().insertStack(cooked)) {
                        player.dropItem(cooked, false);
                    }
                    world.playSound(null, pos, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 1.0f, 1.2f);
                    world.playSound(null, pos, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.8f, 1.2f);
                    if (world instanceof net.minecraft.server.world.ServerWorld sw) {
                        sw.spawnParticles(net.minecraft.particle.ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 8, 0.1, 0.2, 0.1, 0.02);
                    }
                    player.sendMessage(Text.literal("§c[Плита «Лысьва»]: Борщ закипел! Получен горячий наваристый борщ."), true);
                    return ActionResult.SUCCESS;
                } else {
                    player.sendMessage(Text.literal("§e[Плита «Лысьва»]: Сначала зажгите конфорку для готовки."), true);
                    return ActionResult.SUCCESS;
                }
            }

            // Toggle gas burner flame
            boolean newLit = !isLit;
            world.setBlockState(pos, state.with(LIT, newLit), Block.NOTIFY_LISTENERS);

            if (newLit) {
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                world.playSound(null, pos, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5f, 1.5f);
                player.sendMessage(Text.literal("§7[Газовая плита «Лысьва»]: §bСинее пламя зажглось на конфорке."), true);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.8f, 1.2f);
                player.sendMessage(Text.literal("§7[Газовая плита]: Подача газа перекрыта."), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        Direction facing = state.contains(FACING) ? state.get(FACING) : Direction.NORTH;
        boolean isLit = state.contains(LIT) && state.get(LIT);
        return Blocks.SMOKER.getDefaultState()
                .with(SmokerBlock.FACING, facing)
                .with(SmokerBlock.LIT, isLit);
    }
}
