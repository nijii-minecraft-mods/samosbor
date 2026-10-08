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
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class RadioReceiverBlock extends Block implements PolymerHeadBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty MODE = IntProperty.of("mode", 0, 2);

    // Vintage Soviet VEF-202 Radio head skin
    public static final String RADIO_SKIN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTU4NzlhN2FhN2VjZmY3MjA3NGUyZDNkZjljYTNlMGZiNDY2MmQwNDA3YTExZDhjMWM0ZjRlZjFlOTdkODRmMCJ9fX0=";

    private static final String[] BROADCASTS = new String[]{
            "§8[Радиоприёмник]: Выключен.",
            "§e[Радио «Маяк»]: §7«...План сборки ЖП-11-У перевыполнен на 104%! Гражданам не выходить в технические колодцы...»",
            "§c[Частота ГО]: §4«...ВНИМАНИЕ. На этажах 40-55 объявлен карантин. Любые нелюди подлежат бетонированию...»"
    };

    public RadioReceiverBlock() {
        super(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).strength(1.2f));
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(MODE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            int currentMode = state.get(MODE);
            int nextMode = (currentMode + 1) % 3;
            world.setBlockState(pos, state.with(MODE, nextMode), Block.NOTIFY_LISTENERS);

            if (nextMode == 1) {
                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 0.8f, 1.2f);
            } else if (nextMode == 2) {
                world.playSound(null, pos, SoundEvents.BLOCK_CONDUIT_AMBIENT_SHORT, SoundCategory.BLOCKS, 0.8f, 0.7f);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.8f, 0.5f);
            }

            player.sendMessage(Text.literal(BROADCASTS[nextMode]), true);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public String getPolymerSkinValue(BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return RADIO_SKIN;
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
