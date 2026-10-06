package com.samosbor.event;

import com.samosbor.block.HermeticDoorBlock;
import com.samosbor.dimension.ModDimensions;
import com.samosbor.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SamosborManager {

    private static SamosborPhase currentPhase = SamosborPhase.CALM;
    private static int phaseTimer = 24000; // 20 minutes default calm

    // Phase durations in ticks
    public static final int DURATION_CALM = 24000; // 20 min
    public static final int DURATION_WARNING = 600; // 30 sec
    public static final int DURATION_ESCALATION = 600; // 30 sec
    public static final int DURATION_PEAK = 2400; // 2 min
    public static final int DURATION_RECESSION = 400; // 20 sec

    private static final String[] MIMIC_WHISPERS = new String[]{
            "§d[Голос из коридора]: §7Открой дверь... это Валера... здесь холодно...",
            "§d[Шёпот за дверью]: §7Мама, открой, пожалуйста, я боюсь...",
            "§d[Хрип за гермозатвором]: §7Ликвидаторы... помогите... оно пробило этаж...",
            "§d[Голос парторга]: §7Гражданин, немедленно откройте дверь по приказу Администрации!",
            "§d[Стон за стеной]: §7Впустите... туман уже здесь... я живой...",
            "§d[Знакомый голос]: §7Это Валера, я детали ЖП-11-У принёс, открой скорее..."
    };

    public static SamosborPhase getCurrentPhase() {
        return currentPhase;
    }

    public static void setPhase(SamosborPhase phase, int duration) {
        currentPhase = phase;
        phaseTimer = duration;
    }

    public static void tickWorld(ServerWorld world) {
        if (!world.getRegistryKey().getValue().equals(ModDimensions.GIGAKHRUSHCHYOVKA_KEY.getValue())) {
            return;
        }

        phaseTimer--;

        if (phaseTimer <= 0) {
            transitionToNextPhase(world);
        }

        // Phase specific behaviors
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) return;

        switch (currentPhase) {
            case WARNING -> tickWarning(world, players);
            case ESCALATION -> tickEscalation(world, players);
            case PEAK -> tickPeak(world, players);
            case RECESSION -> tickRecession(world, players);
            case CALM -> {
                // Occasional atmospheric creaks
                if (world.random.nextInt(1200) == 0) {
                    for (ServerPlayerEntity player : players) {
                        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_CAVE.value(), SoundCategory.AMBIENT, 0.5f, 0.5f);
                    }
                }
            }
        }
    }

    private static void transitionToNextPhase(ServerWorld world) {
        switch (currentPhase) {
            case CALM -> {
                currentPhase = SamosborPhase.WARNING;
                phaseTimer = DURATION_WARNING;
                broadcastTitle(world, Text.literal("§eПРЕДВЕСТИЕ"), Text.literal("§7Воздух уплотняется... Запах озона и сырого мяса."), 10, 70, 20);
                broadcastMessage(world, Text.translatable("samosbor.event.warning"));
                for (ServerPlayerEntity player : world.getPlayers()) {
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.AMBIENT, 1.0f, 0.6f);
                }
            }
            case WARNING -> {
                currentPhase = SamosborPhase.ESCALATION;
                phaseTimer = DURATION_ESCALATION;
                broadcastTitle(world, Text.literal("§c§lТРЕВОГА! САМОСБОР!"), Text.literal("§eНЕМЕДЛЕННО ЗАКРОЙТЕ ГЕРМОДВЕРИ!"), 5, 80, 20);
                broadcastMessage(world, Text.translatable("samosbor.event.escalation"));
            }
            case ESCALATION -> {
                currentPhase = SamosborPhase.PEAK;
                phaseTimer = DURATION_PEAK;
                broadcastTitle(world, Text.literal("§4§lПИК САМОСБОРА"), Text.literal("§cНЕ ОТКРЫВАЙТЕ ДВЕРЬ НИКОМУ!"), 5, 100, 20);
                broadcastMessage(world, Text.translatable("samosbor.event.peak"));
            }
            case PEAK -> {
                currentPhase = SamosborPhase.RECESSION;
                phaseTimer = DURATION_RECESSION;
                broadcastTitle(world, Text.literal("§aОТБОЙ ТРЕВОГИ"), Text.literal("§7Самосбор отступил. Очистное звено ликвидаторов на этаже."), 10, 70, 20);
                broadcastMessage(world, Text.translatable("samosbor.event.recession"));

                // Spawn Liquidator purge team in the corridors
                for (ServerPlayerEntity player : world.getPlayers()) {
                    BlockPos corridorSpawn = player.getBlockPos().add(world.random.nextInt(8) - 4, 0, world.random.nextInt(8) - 4);
                    if (world.getBlockState(corridorSpawn).isAir()) {
                        ZombieEntity liquidator = EntityType.ZOMBIE.create(world);
                        if (liquidator != null) {
                            liquidator.refreshPositionAndAngles(corridorSpawn.getX() + 0.5, corridorSpawn.getY(), corridorSpawn.getZ() + 0.5, 0, 0);
                            liquidator.setCustomName(Text.literal("§6Ликвидатор Очистного Звена"));
                            liquidator.setCustomNameVisible(true);
                            liquidator.equipStack(EquipmentSlot.HEAD, new ItemStack(ModItems.GAS_MASK));
                            liquidator.equipStack(EquipmentSlot.CHEST, new ItemStack(net.minecraft.item.Items.IRON_CHESTPLATE));
                            liquidator.equipStack(EquipmentSlot.LEGS, new ItemStack(net.minecraft.item.Items.IRON_LEGGINGS));
                            liquidator.equipStack(EquipmentSlot.FEET, new ItemStack(net.minecraft.item.Items.IRON_BOOTS));
                            liquidator.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.FLAMETHROWER));
                            liquidator.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.45f);
                            liquidator.setEquipmentDropChance(EquipmentSlot.HEAD, 0.35f);
                            world.spawnEntity(liquidator);
                        }
                    }
                }
            }
            case RECESSION -> {
                currentPhase = SamosborPhase.CALM;
                phaseTimer = DURATION_CALM;
            }
        }
    }

    private static void tickWarning(ServerWorld world, List<ServerPlayerEntity> players) {
        // Warning: flickering lights and hum
        if (world.random.nextInt(60) == 0) {
            for (ServerPlayerEntity player : players) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.8f, 0.5f);
            }
        }
    }

    private static void tickEscalation(ServerWorld world, List<ServerPlayerEntity> players) {
        // Escalation: howling alarm sirens!
        if (phaseTimer % 40 == 0) {
            for (ServerPlayerEntity player : players) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVENT_RAID_HORN.value(), SoundCategory.HOSTILE, 2.0f, 0.8f);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 1.5f, 0.6f);
            }
        }

        // Spawn warning purple particles leaking through vents
        for (ServerPlayerEntity player : players) {
            world.spawnParticles(ParticleTypes.WITCH, player.getX() + (world.random.nextDouble() - 0.5) * 10, player.getY() + 3, player.getZ() + (world.random.nextDouble() - 0.5) * 10, 5, 0.2, 0.2, 0.2, 0.05);
        }
    }

    private static void tickPeak(ServerWorld world, List<ServerPlayerEntity> players) {
        // Peak: heavy purple fog and sirens
        if (phaseTimer % 60 == 0) {
            for (ServerPlayerEntity player : players) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 1.0f, 0.5f);
            }
        }

        // Mimic whispers in chat
        if (world.random.nextInt(200) == 0) {
            String whisper = MIMIC_WHISPERS[world.random.nextInt(MIMIC_WHISPERS.length)];
            broadcastMessage(world, Text.literal(whisper));
            for (ServerPlayerEntity player : players) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.HOSTILE, 0.7f, 0.8f);
            }
        }

        // Throttled safety check: run every 20 ticks (1 sec) for performance
        boolean checkSafety = (phaseTimer % 20 == 0);

        // Dense purple fog everywhere around players
        for (ServerPlayerEntity player : players) {
            double px = player.getX();
            double py = player.getY();
            double pz = player.getZ();

            world.spawnParticles(ParticleTypes.DRAGON_BREATH, px, py + 1, pz, 12, 4.0, 1.5, 4.0, 0.02);
            world.spawnParticles(ParticleTypes.PORTAL, px, py + 1, pz, 15, 5.0, 2.0, 5.0, 0.1);

            if (checkSafety) {
                boolean safe = isPlayerSafe(world, player);
                if (!safe) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 1, false, true));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 0, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1, false, false));
                    player.damage(world.getDamageSources().magic(), 2.0f);
                }
            }
        }

        // Spawn hostile Mimic entities in corridors
        if (world.random.nextInt(300) == 0 && players.size() > 0) {
            ServerPlayerEntity targetPlayer = players.get(world.random.nextInt(players.size()));
            BlockPos spawnPos = targetPlayer.getBlockPos().add(world.random.nextInt(10) - 5, 0, world.random.nextInt(10) - 5);
            if (world.getBlockState(spawnPos).isAir()) {
                ZombieEntity mimic = EntityType.ZOMBIE.create(world);
                if (mimic != null) {
                    mimic.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                    mimic.setCustomName(Text.literal("§dМимик (Порождение Самосбора)"));
                    mimic.setCustomNameVisible(true);
                    mimic.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CROWBAR));
                    mimic.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.35f);
                    mimic.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1));
                    mimic.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 1));
                    world.spawnEntity(mimic);
                }
            }
        }
    }

    private static void tickRecession(ServerWorld world, List<ServerPlayerEntity> players) {
        // Recession: clearing particles
        for (ServerPlayerEntity player : players) {
            world.spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1, player.getZ(), 2, 2.0, 1.0, 2.0, 0.01);
        }
    }

    public static boolean isPlayerSafe(ServerWorld world, ServerPlayerEntity player) {
        // 1. Gas mask protects against toxic fog
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (helmet.isOf(ModItems.GAS_MASK)) {
            return true;
        }

        // 2. Sealed room check via BFS flood-fill
        BlockPos startPos = player.getBlockPos();
        int startLx = Math.floorMod(startPos.getX(), 16);
        int startLz = Math.floorMod(startPos.getZ(), 16);

        // Open corridor coordinates: NOT safe
        if (startLx == 7 || startLx == 8 || startLz == 7 || startLz == 8) {
            return false;
        }

        java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        queue.add(startPos);
        visited.add(startPos);

        int maxAirBlocks = 120;

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            int lx = Math.floorMod(current.getX(), 16);
            int lz = Math.floorMod(current.getZ(), 16);
            if (lx == 7 || lx == 8 || lz == 7 || lz == 8) {
                return false; // Air path leads into open corridor
            }

            if (visited.size() > maxAirBlocks) {
                return false; // Room is breached or unbounded
            }

            for (net.minecraft.util.math.Direction dir : net.minecraft.util.math.Direction.values()) {
                BlockPos neighbor = current.offset(dir);
                if (visited.contains(neighbor)) {
                    continue;
                }

                if (Math.abs(neighbor.getY() - startPos.getY()) > 5) {
                    return false;
                }

                net.minecraft.block.BlockState bState = world.getBlockState(neighbor);

                if (bState.getBlock() instanceof HermeticDoorBlock) {
                    if (HermeticDoorBlock.isSealedAt(world, neighbor)) {
                        continue; // Sealed door blocks the fog
                    } else {
                        return false; // Unsealed door leaks fog
                    }
                }

                // If block has solid collision (walls, panes, bars, anvils, cauldrons, sinks, furniture, etc.)
                if (!bState.isAir() && !bState.isOf(net.minecraft.block.Blocks.FIRE) && bState.getFluidState().isEmpty() && !bState.getCollisionShape(world, neighbor).isEmpty()) {
                    continue;
                }

                visited.add(neighbor);
                queue.add(neighbor);
            }
        }

        return true;
    }

    private static void broadcastTitle(ServerWorld world, Text title, Text subtitle, int fadeIn, int stay, int fadeOut) {
        TitleFadeS2CPacket fadePacket = new TitleFadeS2CPacket(fadeIn, stay, fadeOut);
        TitleS2CPacket titlePacket = new TitleS2CPacket(title);
        SubtitleS2CPacket subtitlePacket = new SubtitleS2CPacket(subtitle);

        for (ServerPlayerEntity player : world.getPlayers()) {
            player.networkHandler.sendPacket(fadePacket);
            player.networkHandler.sendPacket(titlePacket);
            player.networkHandler.sendPacket(subtitlePacket);
        }
    }

    private static void broadcastMessage(ServerWorld world, Text message) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(message, false);
        }
    }
}
