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

        currentWorld.playSound(null, pos, SoundEvents.BLOCK_CHAIN_FALL, SoundCategory.BLOCKS, 1.2f, 0.6f);
        currentWorld.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.7f);
        currentWorld.playSound(null, pos, SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.BLOCKS, 0.9f, 0.5f);

        serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 0, false, false, false));
        serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1, false, false, false));

        if (isGigakhrushch) {
            // Returning to Overworld
            ServerWorld targetWorld = currentWorld.getServer().getWorld(World.OVERWORLD);
            if (targetWorld != null) {
                BlockPos targetPos = targetWorld.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos);
                serverPlayer.teleport(targetWorld, targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, serverPlayer.getYaw(), serverPlayer.getPitch());
                serverPlayer.sendMessage(Text.literal("§a[ЛИФТ]: Выход на поверхность. Наземный сектор."), true);
            }
        } else {
            // Entering Gigakhrushchyovka
            ServerWorld targetWorld = currentWorld.getServer().getWorld(ModDimensions.GIGAKHRUSHCHYOVKA_KEY);
            if (targetWorld != null) {
                // Snap safely to open central corridor (lx=7, lz=7)
                int chunkOriginX = Math.floorDiv(pos.getX(), 16) * 16;
                int chunkOriginZ = Math.floorDiv(pos.getZ(), 16) * 16;
                int targetX = chunkOriginX + 7;
                int targetZ = chunkOriginZ + 7;
                // Gaussian (normal) distribution: Mean = 85 (Floor 85, Y = 510), StdDev = 28.0 floors
                // ~68% lands between Floor 57 and Floor 113 (Central Habitation)
                // ~27% lands between Floor 29-56 or 114-141 (Industrial & Upper Research)
                // ~5% lands in extreme tails: Flooded Reactor Bilges (Floor 2-28) or Party Nomenklatura (Floor 142-165)
                double gaussian = currentWorld.random.nextGaussian();
                int targetFloor = (int) Math.round(85.0 + gaussian * 30.0);
                targetFloor = Math.max(2, Math.min(165, targetFloor));
                int targetY = targetFloor * com.samosbor.dimension.GigakhrushchLayout.FLOOR_HEIGHT;

                BlockPos landingPos = new BlockPos(targetX, targetY, targetZ);
                BlockPos returnStationPos = landingPos.south(); // targetZ + 1

                // Ensure safe 3x3 landing pad and clearance
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos p = landingPos.add(dx, 0, dz);
                        targetWorld.setBlockState(p, ModBlocks.P46_PANEL.getDefaultState(), Block.NOTIFY_LISTENERS);
                        targetWorld.setBlockState(p.up(1), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                        targetWorld.setBlockState(p.up(2), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }

                // Place return elevator station so player can always go back!
                targetWorld.setBlockState(returnStationPos, ModBlocks.ELEVATOR_STATION.getDefaultState(), Block.NOTIFY_LISTENERS);
                targetWorld.setBlockState(returnStationPos.up(1), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                targetWorld.setBlockState(returnStationPos.up(2), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                targetWorld.setBlockState(returnStationPos.up(3), ModBlocks.FLUORESCENT_LAMP.getDefaultState(), Block.NOTIFY_LISTENERS);

                serverPlayer.teleport(targetWorld, targetX + 0.5, targetY + 1.0, targetZ + 0.5, serverPlayer.getYaw(), serverPlayer.getPitch());

                // Atmospheric sector arrival notification
                int altitudeM = targetY;
                if (targetFloor < 25) {
                    targetWorld.playSound(null, landingPos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.BLOCKS, 1.2f, 0.6f);
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.5f, 0.4f);
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_HEAVY_CORE_FALL, SoundCategory.BLOCKS, 1.8f, 0.5f);
                    serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, false, false, false));
                    serverPlayer.sendMessage(Text.literal(String.format("§4[ЛИФТ]: АВАРИЙНЫЙ СРЫВ ТРОСОВ! Этаж %d (%dм)!", targetFloor, altitudeM)), true);
                    serverPlayer.sendMessage(Text.literal(String.format("§4[ДИСПЕТЧЕР]: Трос оборван! Падение клети на Этаж %d (%dм). Затопленные трюмы и реакторы. Критический уровень опасности!", targetFloor, altitudeM)), false);
                } else if (targetFloor < 55) {
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 1.0f, 0.6f);
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.BLOCKS, 1.2f, 0.8f);
                    serverPlayer.sendMessage(Text.literal(String.format("§c[ЛИФТ]: Лифт прибыл на Этаж %d: Промышленный сектор (%dм)", targetFloor, altitudeM)), true);
                    serverPlayer.sendMessage(Text.literal(String.format("§c[ДИСПЕТЧЕР]: Спуск завершён. Отметка %dм (Этаж %d) — Нижний промышленный сектор. Тяжёлые машинные цеха.", altitudeM, targetFloor)), false);
                } else if (targetFloor <= 115) {
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.BLOCKS, 1.0f, 1.2f);
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_COPPER_DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 0.9f);
                    serverPlayer.sendMessage(Text.literal(String.format("§e[ЛИФТ]: Лифт прибыл на Этаж %d: Жилой сектор (%dм)", targetFloor, altitudeM)), true);
                    serverPlayer.sendMessage(Text.literal(String.format("§e[ДИСПЕТЧЕР]: Штатный спуск. Отметка %dм (Этаж %d) — Центральный жилой сектор. Секция типовых панельных блоков.", altitudeM, targetFloor)), false);
                } else if (targetFloor <= 145) {
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.0f, 1.4f);
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.1f);
                    serverPlayer.sendMessage(Text.literal(String.format("§b[ЛИФТ]: Лифт прибыл на Этаж %d: Научный сектор НИИ (%dм)", targetFloor, altitudeM)), true);
                    serverPlayer.sendMessage(Text.literal(String.format("§b[ДИСПЕТЧЕР]: Подъём завершён. Отметка %dм (Этаж %d) — Научно-исследовательский сектор закрытого типа.", altitudeM, targetFloor)), false);
                } else {
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 1.2f, 1.5f);
                    targetWorld.playSound(null, landingPos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1.2f, 2.0f);
                    serverPlayer.sendMessage(Text.literal(String.format("§d[ЛИФТ]: СПЕЦРЕЙС: Этаж %d — Стратум Номенклатуры (%dм)", targetFloor, altitudeM)), true);
                    serverPlayer.sendMessage(Text.literal(String.format("§d[ДИСПЕТЧЕР]: Внештатная остановка. Отметка %dм (Этаж %d) — Стратум Номенклатуры и Парткома. Внимание: охраняемый периметр!", altitudeM, targetFloor)), false);
                }
            }
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.LODESTONE.getDefaultState();
    }
}
