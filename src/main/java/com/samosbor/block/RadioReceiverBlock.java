package com.samosbor.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
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

public class RadioReceiverBlock extends Block implements PolymerBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty MODE = IntProperty.of("mode", 0, 2);

    private static final String[] BROADCASTS = new String[]{
            "§8[Радиоприёмник «ВЭФ-202»]: Выключен.",
            "§e[Радио «Маяк»]: §7«...План сборки ЖП-11-У перевыполнен на 104%! Гражданам не выходить в технические колодцы...»",
            "§c[Частота ГО]: §4«...ВНИМАНИЕ. На этажах 40-55 объявлен карантин. Любые нелюди подлежат бетонированию...»"
    };

    public RadioReceiverBlock() {
        super(AbstractBlock.Settings.copy(Blocks.NOTE_BLOCK).strength(1.2f));
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
            ItemStack held = player.getMainHandStack();
            if (held.isOf(com.samosbor.item.ModItems.CASSETTE_TAPE)) {
                world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 1.0f, 0.7f);
                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1.0f, 0.5f);
                world.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.6f, 1.8f);
                player.sendMessage(Text.literal("§6[ВЭФ-202]: §7Воспроизведение магнитной кассеты через динамик..."), true);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    var adv = serverPlayer.server.getAdvancementLoader().get(net.minecraft.util.Identifier.of("samosbor", "number_station"));
                    if (adv != null) {
                        serverPlayer.getAdvancementTracker().grantCriterion(adv, "interact_radio");
                    }
                }
                held.use(world, player, net.minecraft.util.Hand.MAIN_HAND);
                return ActionResult.SUCCESS;
            }

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
            if (player instanceof ServerPlayerEntity serverPlayer && nextMode > 0) {
                var adv = serverPlayer.server.getAdvancementLoader().get(net.minecraft.util.Identifier.of("samosbor", "number_station"));
                if (adv != null) {
                    serverPlayer.getAdvancementTracker().grantCriterion(adv, "interact_radio");
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.NOTE_BLOCK.getDefaultState();
    }
}
