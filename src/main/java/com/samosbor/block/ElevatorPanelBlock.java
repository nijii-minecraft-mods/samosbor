package com.samosbor.block;

import com.samosbor.event.SamosborManager;
import com.samosbor.event.SamosborPhase;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class ElevatorPanelBlock extends Block implements PolymerBlock {
    public static final DirectionProperty FACING = net.minecraft.state.property.Properties.HORIZONTAL_FACING;

    public ElevatorPanelBlock() {
        super(AbstractBlock.Settings.copy(Blocks.OBSERVER).strength(4.0f, 600.0f));
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;

        // Check Samosbor emergency interlock
        if (SamosborManager.getCurrentPhase() != SamosborPhase.CALM) {
            serverWorld.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 1.0f, 0.5f);
            serverWorld.playSound(null, pos, SoundEvents.BLOCK_COPPER_BULB_TURN_OFF, SoundCategory.BLOCKS, 1.2f, 0.6f);
            serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.05);
            player.sendMessage(Text.literal("§c[ЛИФТ]: СИСТЕМА ЗАБЛОКИРОВАНА АВАРИЙНЫМ ПРОТОКОЛОМ САМОСБОРА!"), true);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 40, 0, false, false));
            return ActionResult.SUCCESS;
        }

        boolean goingDown = player.isSneaking();
        boolean expressTravel = !player.getMainHandStack().isEmpty();

        // Step: 1 floor (6 blocks) or Express: 10 floors (60 blocks)
        int floorsToMove = expressTravel ? 10 : 1;
        int deltaY = goingDown ? (-6 * floorsToMove) : (6 * floorsToMove);
        int targetY = pos.getY() + deltaY;

        // Clamp to elevator shaft limits
        if (targetY < 6) targetY = 6;
        if (targetY > 1014) targetY = 1014;
        deltaY = targetY - pos.getY();

        if (deltaY == 0) {
            serverWorld.playSound(null, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 0.8f, 1.2f);
            player.sendMessage(Text.literal("§e[ЛИФТ]: Конечная отметка шахты (" + (goingDown ? "Нижний сектор" : "Верхний сектор") + ")"), true);
            return ActionResult.SUCCESS;
        }

        // Multi-passenger cabin transport: all entities inside cabin
        Box cabinBox = new Box(pos.getX() - 3.0, pos.getY() - 2.0, pos.getZ() - 3.0, pos.getX() + 4.0, pos.getY() + 4.0, pos.getZ() + 4.0);
        List<ServerPlayerEntity> passengers = serverWorld.getEntitiesByClass(ServerPlayerEntity.class, cabinBox, p -> true);

        // Heavy mechanical winch sound effects
        serverWorld.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.6f);
        serverWorld.playSound(null, pos, SoundEvents.BLOCK_CHAIN_FALL, SoundCategory.BLOCKS, 1.2f, 0.7f);
        serverWorld.playSound(null, pos, SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.BLOCKS, 1.0f, 0.4f);

        int targetFloorNumber = targetY / 6;

        for (ServerPlayerEntity passenger : passengers) {
            passenger.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 25, 0, false, false, false));
            passenger.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, false, false, false));
            passenger.teleport(serverWorld, passenger.getX(), passenger.getY() + deltaY, passenger.getZ(), passenger.getYaw(), passenger.getPitch());
            passenger.sendMessage(Text.literal("§a[ЛИФТ]: " + (goingDown ? "Спуск" : "Подъём") +
                    (expressTravel ? " [ЭКСПРЕСС x10]" : "") +
                    " -> Этаж " + targetFloorNumber + " (Y=" + targetY + ")"), true);
        }

        // Destination arrival effects
        BlockPos destinationPos = pos.up(deltaY);
        serverWorld.playSound(null, destinationPos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 0.8f, 1.8f);
        serverWorld.playSound(null, destinationPos, SoundEvents.BLOCK_COPPER_DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 0.8f);

        // Open landing doors at destination if present
        for (Direction dir : Direction.Type.HORIZONTAL) {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos checkPos = destinationPos.offset(dir, 3).up(dy);
                BlockState checkState = serverWorld.getBlockState(checkPos);
                if (checkState.getBlock() instanceof ElevatorDoorBlock) {
                    serverWorld.setBlockState(checkPos, checkState.with(DoorBlock.OPEN, true), Block.NOTIFY_LISTENERS);
                    BlockPos upper = checkPos.up();
                    if (serverWorld.getBlockState(upper).getBlock() instanceof ElevatorDoorBlock) {
                        serverWorld.setBlockState(upper, serverWorld.getBlockState(upper).with(DoorBlock.OPEN, true), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.OBSERVER.getDefaultState().with(net.minecraft.state.property.Properties.FACING, state.get(FACING));
    }
}
