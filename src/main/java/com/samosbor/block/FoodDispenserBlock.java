package com.samosbor.block;

import com.samosbor.item.ModItems;
import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
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

public class FoodDispenserBlock extends Block implements PolymerHeadBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    // Soviet Food/Soda Vending Machine AT-114
    public static final String DISPENSER_SKIN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmE2MGQ2OTRkYzUwNjA0YjkwMTVjMzJhZTRjYmZlNmE5MmIzNDdiNzRiYTQxMDMxYzI2MTg4MzU4MjQ5NmU1In19fQ==";

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
    public String getPolymerSkinValue(BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return DISPENSER_SKIN;
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
