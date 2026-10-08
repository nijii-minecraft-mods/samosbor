package com.samosbor.event;

import com.samosbor.block.HermeticDoorBlock;
import com.samosbor.dimension.ModDimensions;
import com.samosbor.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.SilverfishEntity;
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
import net.minecraft.util.Identifier;
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
            "§d[Голос соседа из коридора]: §7Открой дверь... это сосед из 42-й... здесь туман...",
            "§d[Шёпот за дверью]: §7Мама, открой, пожалуйста, я боюсь... оно ползёт...",
            "§d[Хрип за гермозатвором]: §7Ликвидаторы... помогите... прорыв на ярусе...",
            "§d[Голос дежурного]: §7Гражданин, немедленно откройте дверь для зачистки сектора!",
            "§d[Стон за стеной]: §7Впустите... туман уже здесь... я ещё человек...",
            "§d[Искажённый голос]: §7Дежурный, открой... я принёс канистру спецраствора..."
    };

    public static SamosborPhase getCurrentPhase() {
        return currentPhase;
    }

    public static boolean isSamosborActive() {
        return currentPhase != SamosborPhase.CALM;
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
                // Occasional atmospheric creaks and distant structural groans
                if (world.random.nextInt(400) == 0) {
                    float pitch = 0.2f + world.random.nextFloat() * 0.2f;
                    for (ServerPlayerEntity player : players) {
                        if (world.random.nextBoolean()) {
                            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.AMBIENT, 0.4f, pitch);
                        } else {
                            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_HEAVY_CORE_FALL, SoundCategory.AMBIENT, 0.5f, 0.4f);
                        }
                    }
                }

                // Geiger counter clicks near biomass blocks
                for (ServerPlayerEntity player : players) {
                    if (world.random.nextInt(8) == 0) {
                        BlockPos pPos = player.getBlockPos();
                        for (int dx = -5; dx <= 5; dx += 2) {
                            for (int dy = -2; dy <= 2; dy++) {
                                for (int dz = -5; dz <= 5; dz += 2) {
                                    if (world.getBlockState(pPos.add(dx, dy, dz)).isOf(com.samosbor.block.ModBlocks.BIOMASS)) {
                                        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.PLAYERS, 0.5f, 1.9f + world.random.nextFloat() * 0.1f);
                                        break;
                                    }
                                }
                            }
                        }
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

                // Award survive_siren advancement to alive players who survived in safe airtight rooms
                var surviveAdv = world.getServer().getAdvancementLoader().get(Identifier.of("samosbor", "survive_siren"));
                if (surviveAdv != null) {
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        if (player.isAlive() && isPlayerSafe(world, player)) {
                            player.getAdvancementTracker().grantCriterion(surviveAdv, "survive_siren_cycle");
                        }
                    }
                }

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
        // Escalation: howling alarm sirens and synchronized siren beacons!
        if (phaseTimer % 40 == 0) {
            for (ServerPlayerEntity player : players) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVENT_RAID_HORN.value(), SoundCategory.HOSTILE, 2.0f, 0.8f);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 1.5f, 0.6f);

                BlockPos pPos = player.getBlockPos();
                for (int dx = -8; dx <= 8; dx += 2) {
                    for (int dy = -2; dy <= 4; dy++) {
                        for (int dz = -8; dz <= 8; dz += 2) {
                            BlockPos sPos = pPos.add(dx, dy, dz);
                            net.minecraft.block.BlockState bState = world.getBlockState(sPos);
                            if (bState.getBlock() instanceof com.samosbor.block.EmergencySirenBlock && !bState.get(com.samosbor.block.EmergencySirenBlock.LIT)) {
                                world.setBlockState(sPos, bState.with(com.samosbor.block.EmergencySirenBlock.LIT, true), net.minecraft.block.Block.NOTIFY_LISTENERS);
                                world.spawnParticles(ParticleTypes.LAVA, sPos.getX() + 0.5, sPos.getY() + 0.8, sPos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.05);
                            }
                        }
                    }
                }
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

        // Spawn hostile entities in corridors with depth scaling & variety
        if (world.random.nextInt(180) == 0 && !players.isEmpty()) {
            ServerPlayerEntity targetPlayer = players.get(world.random.nextInt(players.size()));
            BlockPos spawnPos = targetPlayer.getBlockPos().add(world.random.nextInt(12) - 6, 0, world.random.nextInt(12) - 6);
            if (world.getBlockState(spawnPos).isAir()) {
                int playerFloor = (int) (targetPlayer.getY() / com.samosbor.dimension.GigakhrushchLayout.FLOOR_HEIGHT);
                int distFromCenter = Math.abs(playerFloor - 85);
                int mobRoll = world.random.nextInt(100);

                if (distFromCenter > 40 && mobRoll < 12) {
                    // 1. АБЕРРАЦИЯ А-14 (Апостол Вентиляции) - User #36
                    ZombieEntity a14 = EntityType.ZOMBIE.create(world);
                    if (a14 != null) {
                        a14.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                        a14.setCustomName(Text.literal("§4§lАберрация А-14 (Апостол Вентиляции)"));
                        a14.setCustomNameVisible(true);
                        a14.equipStack(EquipmentSlot.HEAD, new ItemStack(net.minecraft.item.Items.NETHERITE_HELMET));
                        a14.equipStack(EquipmentSlot.CHEST, new ItemStack(net.minecraft.item.Items.NETHERITE_CHESTPLATE));
                        a14.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.ROKS3_FLAMETHROWER));
                        a14.equipStack(EquipmentSlot.OFFHAND, new ItemStack(ModItems.ANOMALY_CORE));
                        a14.setEquipmentDropChance(EquipmentSlot.OFFHAND, 1.0f);
                        a14.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.40f);
                        a14.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 4800, 2));
                        a14.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 4800, 2));
                        a14.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 4800, 2));
                        a14.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 4800, 0));

                        world.spawnEntity(a14);
                        world.playSound(null, spawnPos, SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 1.6f, 0.5f);
                        broadcastMessage(world, Text.literal("§4§l[!] ИЗ ВЕНТШАХТЫ ВЫРВАЛАСЬ АБЕРРАЦИЯ А-14! АПОСТОЛ ВЕНТИЛЯЦИИ СОКРУШАЕТ БЕТОН!"));
                    }
                } else if (mobRoll < 22) {
                    // 2. Ликвидатор Спецпрототипа ЭК-5 - User #37
                    ZombieEntity ek5 = EntityType.ZOMBIE.create(world);
                    if (ek5 != null) {
                        ek5.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                        ek5.setCustomName(Text.literal("§c§lЛиквидатор Спецпрототипа ЭК-5"));
                        ek5.setCustomNameVisible(true);
                        ek5.equipStack(EquipmentSlot.HEAD, new ItemStack(ModItems.GAS_MASK));
                        ek5.equipStack(EquipmentSlot.CHEST, new ItemStack(ModItems.L1M_SUIT));
                        ek5.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.JP11_CARBINE));
                        ek5.equipStack(EquipmentSlot.OFFHAND, new ItemStack(ModItems.SR11_STABILIZER));
                        ek5.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.35f);
                        ek5.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.80f);
                        ek5.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 2400, 1));
                        ek5.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2400, 1));

                        world.spawnEntity(ek5);
                        broadcastMessage(world, Text.literal("§c[Ликвидатор ЭК-5]: «ПРОТОКОЛ ЗАКРЫТИЯ СЕКТОРА АКТИВИРОВАН. ЦЕЛЬ БУДЕТ ВЫЖЖЕНА.»"));
                    }
                } else if (mobRoll < 34) {
                    // 3. Аберрация А-19 (Многорукий Парторг) - User #42
                    ZombieEntity a19 = EntityType.ZOMBIE.create(world);
                    if (a19 != null) {
                        a19.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                        a19.setCustomName(Text.literal("§4Аберрация А-19 (Многорукий Парторг)"));
                        a19.setCustomNameVisible(true);
                        a19.equipStack(EquipmentSlot.HEAD, new ItemStack(net.minecraft.item.Items.WITHER_SKELETON_SKULL));
                        a19.equipStack(EquipmentSlot.CHEST, new ItemStack(net.minecraft.item.Items.IRON_CHESTPLATE));
                        a19.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.HEAVY_CROWBAR));
                        a19.equipStack(EquipmentSlot.OFFHAND, new ItemStack(ModItems.PROJECT_SODEYSTVIE));
                        a19.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.50f);
                        a19.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.30f);
                        a19.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 2400, 1));
                        a19.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 2400, 1));

                        world.spawnEntity(a19);
                        broadcastMessage(world, Text.literal("§4[Аберрация А-19]: «ВСЕ НА СУББОТНИК... СДАТЬ НОРМУ ПЛОТИ... СЛАВА ПАРТИИ!»"));
                    }
                } else if (mobRoll < 44) {
                    // 4. Псевдо-эмбрион ПЭ-2 - User #43
                    DrownedEntity pe2 = EntityType.DROWNED.create(world);
                    if (pe2 != null) {
                        pe2.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                        pe2.setCustomName(Text.literal("§3Псевдо-эмбрион ПЭ-2"));
                        pe2.setCustomNameVisible(true);
                        pe2.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.AJ11_FLUID));
                        pe2.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.40f);
                        pe2.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 2400, 1));
                        pe2.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 2400, 1));
                        world.spawnEntity(pe2);
                    }
                } else if (mobRoll < 54) {
                    // 5. Нейропаразит НП-1 - User #40
                    SilverfishEntity np1 = EntityType.SILVERFISH.create(world);
                    if (np1 != null) {
                        np1.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                        np1.setCustomName(Text.literal("§cНейропаразит НП-1"));
                        np1.setCustomNameVisible(true);
                        np1.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 2400, 2));
                        world.spawnEntity(np1);
                    }
                } else if (mobRoll < 66) {
                    // 6. Сектант Великой Биомассы
                    ZombieEntity cultist = EntityType.ZOMBIE.create(world);
                    if (cultist != null) {
                        cultist.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                        cultist.setCustomName(Text.literal("§5Сектант Великой Биомассы"));
                        cultist.setCustomNameVisible(true);
                        cultist.equipStack(EquipmentSlot.HEAD, new ItemStack(net.minecraft.item.Items.WITHER_SKELETON_SKULL));
                        cultist.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RITUAL_SICKLE));
                        cultist.equipStack(EquipmentSlot.OFFHAND, new ItemStack(ModItems.BIOMASS_GRIMOIRE));
                        cultist.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.25f);
                        cultist.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1800, 1));

                        world.spawnEntity(cultist);
                        if (world.random.nextBoolean()) {
                            broadcastMessage(world, Text.literal("§5[Сектант Биомассы]: §d«Святая Мякоть, раствори наш страх! Впусти Туман!»"));
                        }
                    }
                } else if (mobRoll < 76) {
                    // 7. Мясная Пиявка Вентиляции
                    EndermiteEntity leech = EntityType.ENDERMITE.create(world);
                    if (leech != null) {
                        leech.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                        leech.setCustomName(Text.literal("§4Мясная Пиявка Вентиляции"));
                        leech.setCustomNameVisible(true);
                        leech.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 2400, 2));
                        world.spawnEntity(leech);
                    }
                } else {
                    // 8. Standard or Mutated Mimic
                    ZombieEntity mimic = EntityType.ZOMBIE.create(world);
                    if (mimic != null) {
                        mimic.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                        if (distFromCenter > 20) {
                            mimic.setCustomName(Text.literal("§cМутировавший Мимик Сектора"));
                            mimic.equipStack(EquipmentSlot.HEAD, new ItemStack(net.minecraft.item.Items.IRON_HELMET));
                            mimic.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.HEAVY_CROWBAR));
                            mimic.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1800, 1));
                            mimic.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1800, 1));
                        } else {
                            mimic.setCustomName(Text.literal("§dМимик (Порождение Самосбора)"));
                            mimic.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CROWBAR));
                            mimic.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 0));
                        }
                        mimic.setCustomNameVisible(true);
                        mimic.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.35f);
                        world.spawnEntity(mimic);
                    }
                }
            }
        }
    }

    private static void tickRecession(ServerWorld world, List<ServerPlayerEntity> players) {
        // Recession: clearing particles and resetting sirens
        for (ServerPlayerEntity player : players) {
            world.spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1, player.getZ(), 2, 2.0, 1.0, 2.0, 0.01);

            BlockPos pPos = player.getBlockPos();
            for (int dx = -10; dx <= 10; dx += 2) {
                for (int dy = -3; dy <= 4; dy++) {
                    for (int dz = -10; dz <= 10; dz += 2) {
                        BlockPos sPos = pPos.add(dx, dy, dz);
                        net.minecraft.block.BlockState bState = world.getBlockState(sPos);
                        if (bState.getBlock() instanceof com.samosbor.block.EmergencySirenBlock && bState.get(com.samosbor.block.EmergencySirenBlock.LIT)) {
                            world.setBlockState(sPos, bState.with(com.samosbor.block.EmergencySirenBlock.LIT, false), net.minecraft.block.Block.NOTIFY_LISTENERS);
                        }
                    }
                }
            }
        }
    }

    public static boolean isPlayerSafe(ServerWorld world, ServerPlayerEntity player) {
        // 1. L-1M Hazmat suit grants full NBC immunity
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.isOf(ModItems.L1M_SUIT)) {
            return true;
        }

        // 1.1 Cult Totem (Tendon Amulet) in offhand protection
        ItemStack offhand = player.getEquippedStack(EquipmentSlot.OFFHAND);
        if (offhand.isOf(ModItems.CULT_TOTEM)) {
            if (com.samosbor.item.CultTotemItem.tryProtectFromSamosbor(player, offhand, world)) {
                return true;
            }
        }

        // 2. Sealed room check via BFS flood-fill
        BlockPos startPos = player.getBlockPos();
        int startLx = Math.floorMod(startPos.getX(), 16);
        int startLz = Math.floorMod(startPos.getZ(), 16);

        boolean inOpenCorridor = (startLx == 7 || startLx == 8 || startLz == 7 || startLz == 8);
        boolean roomAirtight = false;

        if (!inOpenCorridor) {
            java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();
            java.util.Set<BlockPos> visited = new java.util.HashSet<>();
            queue.add(startPos);
            visited.add(startPos);

            int maxAirBlocks = 120;
            boolean breached = false;

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();

                int lx = Math.floorMod(current.getX(), 16);
                int lz = Math.floorMod(current.getZ(), 16);
                if (lx == 7 || lx == 8 || lz == 7 || lz == 8) {
                    breached = true;
                    break;
                }

                if (visited.size() > maxAirBlocks) {
                    breached = true;
                    break;
                }

                for (net.minecraft.util.math.Direction dir : net.minecraft.util.math.Direction.values()) {
                    BlockPos neighbor = current.offset(dir);
                    if (visited.contains(neighbor)) {
                        continue;
                    }

                    if (Math.abs(neighbor.getY() - startPos.getY()) > 5) {
                        breached = true;
                        break;
                    }

                    net.minecraft.block.BlockState bState = world.getBlockState(neighbor);

                    if (bState.getBlock() instanceof HermeticDoorBlock) {
                        if (HermeticDoorBlock.isSealedAt(world, neighbor)) {
                            continue; // Sealed hermetic door blocks the fog
                        } else {
                            breached = true;
                            break;
                        }
                    }

                    if (!bState.isAir() && !bState.isOf(net.minecraft.block.Blocks.FIRE) && bState.getFluidState().isEmpty() && !bState.getCollisionShape(world, neighbor).isEmpty()) {
                        continue;
                    }

                    visited.add(neighbor);
                    queue.add(neighbor);
                }

                if (breached) break;
            }

            if (!breached) {
                roomAirtight = true;
            }
        }

        // If inside airtight room, player is 100% safe without consuming mask durability
        if (roomAirtight) {
            return true;
        }

        // 3. Exposed to toxic fog: Check Gas Mask durability
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (helmet.isOf(ModItems.GAS_MASK)) {
            if (helmet.getDamage() < helmet.getMaxDamage()) {
                helmet.damage(1, player, EquipmentSlot.HEAD);
                if (helmet.getDamage() >= helmet.getMaxDamage() - 25) {
                    player.sendMessage(Text.literal("§c[ГП-5]: Внимание! Ресурс фильтра ГП-5К почти исчерпан!"), true);
                    player.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 0.8f, 0.7f);
                }
                return true;
            } else {
                player.sendMessage(Text.literal("§4[ГП-5]: Фильтр пробит! Едкий туман проникает внутрь!"), true);
                return false;
            }
        }

        return false;
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
