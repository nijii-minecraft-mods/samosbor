package com.samosbor.block;

import com.samosbor.item.ModItems;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
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

public class MedkitBoxBlock extends Block implements PolymerBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty OPENED = BooleanProperty.of("opened");

    public MedkitBoxBlock() {
        super(AbstractBlock.Settings.copy(Blocks.RED_GLAZED_TERRACOTTA).strength(2.0f));
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(OPENED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPENED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            boolean opened = state.get(OPENED);
            if (!opened) {
                world.setBlockState(pos, state.with(OPENED, true), Block.NOTIFY_LISTENERS);
                world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.8f, 1.2f);
                world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(), SoundCategory.BLOCKS, 1.0f, 1.5f);

                // Dispense supplies
                int roll = world.random.nextInt(3);
                ItemStack medItem = switch (roll) {
                    case 0 -> new ItemStack(ModItems.AI2_MEDKIT, 1);
                    case 1 -> new ItemStack(ModItems.ANTIRAD, 1);
                    default -> new ItemStack(ModItems.BANDAGE, 2);
                };

                if (!player.getInventory().insertStack(medItem)) {
                    ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, medItem);
                    world.spawnEntity(entity);
                }
                player.sendMessage(Text.literal("§a[Аптечка ГО]: Извлечены медикаменты первой помощи!"), true);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 0.6f, 1.5f);
                player.sendMessage(Text.literal("§7[Аптечка ГО]: Пуста. Всё ценное уже забрали."), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.RED_GLAZED_TERRACOTTA.getDefaultState();
    }
}
