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

import java.util.List;

public class NoticeBoardBlock extends Block implements PolymerHeadBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    // Bulletin board / notice board head texture
    public static final String NOTICE_BOARD_SKIN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg5NTFkY2Q0OWJhNjlmM2Y5NGRhMGVhYjBiOTcxNGViYjg3MGQ2ZjVmMTEyZWM0ZGYxMWEwZmZlZDM0MTE1MSJ9fX0=";

    private static final List<String> NOTICES = List.of(
            "§e[ДОСКА ОБЪЯВЛЕНИЙ]: §f«ВНИМАНИЕ ЖИЛЬЦАМ СЕКТОРА! В связи с прорывом биомассы на 42-м этаже, норма выдачи пищконцентрата снижена до 150 грамм. Слава Партии!»",
            "§e[ДОСКА ОБЪЯВЛЕНИЙ]: §f«ОБЪЯВЛЕНИЕ: Пропал сантехник Потапов. В последний раз направлялся к техническому колодцу №7. Если услышите стук гаечным ключом из вентиляции — НЕ ОТВЕЧАЙТЕ.»",
            "§e[ДОСКА ОБЪЯВЛЕНИЙ]: §f«ИНСТРУКЦИЯ ПО САМОСБОРУ №14: При звуке сирены немедленно зайти в жилую ячейку, задраить гермодверь и залить швы эпоксидной пеной. Ожидать ликвидаторов.»",
            "§e[ДОСКА ОБЪЯВЛЕНИЙ]: §f«ПАМЯТКА: Замеченные за нелегальным сбором грибов в вентиляционных шахтах будут переведены в состав штрафных бригад дезактивации.»",
            "§e[ДОСКА ОБЪЯВЛЕНИЙ]: §f«ОБЪЯВЛЕНИЕ: Меняю 3 банки свиной тушёнки ГОСТ и рулон синей изоленты на исправный фильтр ГП-5К. Ячейка 318.»",
            "§e[ДОСКА ОБЪЯВЛЕНИЙ]: §f«ПРЕДУПРЕЖДЕНИЕ: Запрещено спускаться ниже 40-го этажа без защитного комплекта Л-1М и допуска парткома. Фиксируются очаги агрессивных мимиков.»",
            "§e[ДОСКА ОБЪЯВЛЕНИЙ]: §f«ПРИКАЗ: Всем жильцам проверить резиновые уплотнители гермозатворов. За негерметичность ячейки — ответственность по законам военного времени.»",
            "§e[ДОСКА ОБЪЯВЛЕНИЙ]: §f«ЗАПИСКА СВЕРХУ: Кто-то заварил лестничный марш на 112 этаже снаружи. Валера, если ты это читаешь, открой, воздух заканчивается...»"
    );

    public NoticeBoardBlock() {
        super(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).strength(1.2f));
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
            world.playSound(null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1.0f, 1.0f);
            String notice = NOTICES.get(world.random.nextInt(NOTICES.size()));
            player.sendMessage(Text.literal(notice), false);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public String getPolymerSkinValue(BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return NOTICE_BOARD_SKIN;
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
