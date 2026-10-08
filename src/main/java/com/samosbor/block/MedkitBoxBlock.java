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
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class MedkitBoxBlock extends Block implements PolymerHeadBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty OPENED = BooleanProperty.of("opened");

    // Soviet First Aid Wall Box Skin (Orange AI / White Red Cross)
    public static final String MEDKIT_BOX_SKIN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZiMGMyNjg4NzVlYTJkOGY5YmM4ZTU2OTVkZTkzMTU4ZTBhMTc0YTc4MTVjZmIyYWEzYjAyODY3ZjA4ZTlhIn19fQ==";

    public MedkitBoxBlock() {
        super(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).strength(2.0f));
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
    public String getPolymerSkinValue(BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return MEDKIT_BOX_SKIN;
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
