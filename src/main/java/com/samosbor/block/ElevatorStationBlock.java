package com.samosbor.block;

import com.samosbor.dimension.ModDimensions;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElevatorStationBlock extends Block implements PolymerBlock {

    public ElevatorStationBlock() {
        super(AbstractBlock.Settings.copy(Blocks.LODESTONE).strength(5.0f, 1200.0f));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            teleportPlayer(world, pos, serverPlayer);
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient && entity instanceof ServerPlayerEntity serverPlayer) {
            if (!serverPlayer.hasStatusEffect(StatusEffects.DARKNESS)) {
                teleportPlayer(world, pos, serverPlayer);
            }
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    public static void teleportPlayer(World world, BlockPos pos, ServerPlayerEntity serverPlayer) {
        ServerWorld currentWorld = (ServerWorld) world;
        boolean isGigakhrushch = currentWorld.getRegistryKey().getValue().equals(ModDimensions.GIGAKHRUSHCHYOVKA_KEY.getValue());

        serverPlayer.sendMessage(Text.translatable("samosbor.elevator.teleporting"), true);
        currentWorld.playSound(null, pos, SoundEvents.BLOCK_CHAIN_FALL, SoundCategory.BLOCKS, 1.0f, 0.5f);
        currentWorld.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.6f);

        serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 80, 1, false, false, false));

        if (isGigakhrushch) {
            ServerWorld targetWorld = currentWorld.getServer().getWorld(World.OVERWORLD);
            if (targetWorld != null) {
                BlockPos targetPos = targetWorld.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos);
                serverPlayer.teleport(targetWorld, targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, serverPlayer.getYaw(), serverPlayer.getPitch());
            }
        } else {
            ServerWorld targetWorld = currentWorld.getServer().getWorld(ModDimensions.GIGAKHRUSHCHYOVKA_KEY);
            if (targetWorld != null) {
                int targetY = 66;
                serverPlayer.teleport(targetWorld, pos.getX() + 0.5, targetY + 1.0, pos.getZ() + 0.5, serverPlayer.getYaw(), serverPlayer.getPitch());
            }
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.LODESTONE.getDefaultState();
    }
}
