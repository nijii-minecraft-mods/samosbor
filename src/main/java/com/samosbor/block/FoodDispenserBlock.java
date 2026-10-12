package com.samosbor.block;

import com.samosbor.item.ModItems;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FoodDispenserBlock extends Block implements PolymerBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public FoodDispenserBlock() {
        super(AbstractBlock.Settings.copy(Blocks.DISPENSER).strength(3.5f, 6.0f));
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
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(ModItems.RATION_TICKET)) {
            if (!world.isClient) {
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.8f, 1.5f);

                // Give random survival provision: food concentrate, canned stew, or hardtack
                int roll = world.random.nextInt(10);
                ItemStack food;
                if (roll < 5) {
                    food = new ItemStack(ModItems.FOOD_CONCENTRATE, 2);
                } else if (roll < 8) {
                    food = new ItemStack(ModItems.HARDTACK, 4);
                } else {
                    food = new ItemStack(ModItems.CANNED_STEW, 1);
                }

                if (!player.getInventory().insertStack(food)) {
                    ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, food);
                    world.spawnEntity(entity);
                }
                player.sendMessage(Text.literal("§7[Автомат спецпайка АТ-114]: §aТалон принят. Выдан суточный рацион!"), true);
            }
            return ItemActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        Direction facing = state.contains(FACING) ? state.get(FACING) : Direction.NORTH;
        return Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, facing);
    }
}
