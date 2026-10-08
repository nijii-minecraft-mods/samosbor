package com.samosbor.block;

import com.samosbor.item.ModItems;
import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

public class CultAltarBlock extends Block implements PolymerHeadBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    // Dark bloody biomass ritual altar texture
    public static final String ALTAR_SKIN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA4YmU3NjljNzExMDQ4YWI4Y2I1MDkyNGFkZjgyOTJhZGJkOWVjMTJmZjFmOGY1YjFjOWJhNzZhN2IzMmI3MiJ9fX0=";

    public CultAltarBlock() {
        super(AbstractBlock.Settings.copy(Blocks.OBSIDIAN).strength(4.0f).luminance(s -> 7));
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
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            ItemStack held = player.getMainHandStack();

            // 1. Purging the altar with a flamethrower
            if (held.isOf(ModItems.FLAMETHROWER) || held.isOf(ModItems.ROKS3_FLAMETHROWER)) {
                serverWorld.breakBlock(pos, false);
                serverWorld.playSound(null, pos, SoundEvents.ENTITY_WARDEN_DEATH, SoundCategory.BLOCKS, 1.2f, 0.8f);
                serverWorld.spawnParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.1);
                player.sendMessage(Text.literal("§6[Ликвидация]: Оккультный алтарь Биомассы выжжен огнесмесью!"), true);
                return ActionResult.SUCCESS;
            }

            // 2. Sacrificial food offering
            if (held.isOf(ModItems.FOOD_CONCENTRATE) || held.isOf(ModItems.CANNED_STEW) || held.isOf(Items.ROTTEN_FLESH)) {
                held.decrement(1);
                serverWorld.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, 1.0f, 0.5f);
                serverWorld.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0f, 0.7f);
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.05);

                player.heal(6.0f);
                player.sendMessage(Text.literal("§5Алтарь с чавканьем поглотил подношение. Ваши раны затягиваются плотью..."), true);
                return ActionResult.SUCCESS;
            }

            // 3. Ambient whisper
            player.sendMessage(Text.literal("§7[Алтарь тихо пульсирует. Из стыков костей веет озоном и сырым мясом.]"), false);
            serverWorld.playSound(null, pos, SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.BLOCKS, 1.0f, 0.9f);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public String getPolymerSkinValue(BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return ALTAR_SKIN;
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
