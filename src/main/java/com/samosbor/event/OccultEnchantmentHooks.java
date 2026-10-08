package com.samosbor.event;

import com.samosbor.block.ModBlocks;
import com.samosbor.dimension.GigakhrushchLayout;
import com.samosbor.dimension.ModDimensions;
import com.samosbor.enchantment.OccultEnchantmentHelper;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.List;

public class OccultEnchantmentHooks {

    public static void register() {
        registerUseBlockHook();
        registerAttackEntityHook();
        registerBlockBreakHook();
        registerEntityDeathHook();
    }

    private static void registerUseBlockHook() {
        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND || !(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld serverWorld)) {
                return ActionResult.PASS;
            }

            ItemStack held = player.getMainHandStack();
            if (held.isEmpty()) return ActionResult.PASS;

            // 1. RIFT BREACH: Tilling ground tears open dimensional portal rift
            int riftLevel = OccultEnchantmentHelper.getLevel(held, OccultEnchantmentHelper.RIFT_BREACH);
            if (riftLevel > 0) {
                if (player.getItemCooldownManager().isCoolingDown(held.getItem())) {
                    return ActionResult.PASS;
                }

                BlockPos hitPos = hitResult.getBlockPos();
                boolean isGigakhrushch = serverWorld.getRegistryKey().getValue().equals(ModDimensions.GIGAKHRUSHCHYOVKA_KEY.getValue());

                // Set cooldown & damage tool
                player.getItemCooldownManager().set(held.getItem(), Math.max(60, 160 - riftLevel * 30));
                held.damage(2, serverPlayer, EquipmentSlot.MAINHAND);

                // Tear effects at ground
                serverWorld.spawnParticles(ParticleTypes.PORTAL, hitPos.getX() + 0.5, hitPos.getY() + 1.2, hitPos.getZ() + 0.5, 60, 0.6, 0.6, 0.6, 0.2);
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, hitPos.getX() + 0.5, hitPos.getY() + 1.0, hitPos.getZ() + 0.5, 25, 0.4, 0.4, 0.4, 0.05);
                serverWorld.playSound(null, hitPos, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.2f, 0.7f);
                serverWorld.playSound(null, hitPos, SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.PLAYERS, 1.0f, 0.5f);

                if (isGigakhrushch) {
                    // Tear exit back to Overworld
                    ServerWorld overworld = serverWorld.getServer().getWorld(World.OVERWORLD);
                    if (overworld != null) {
                        BlockPos targetPos = overworld.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, serverPlayer.getBlockPos());
                        serverPlayer.teleport(overworld, targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, serverPlayer.getYaw(), serverPlayer.getPitch());
                        serverPlayer.sendMessage(Text.literal("§b[Разлом Реальности]: Пространство схлопнулось! Вы вырвались из Гигахруща в Верхний мир!"), true);
                    }
                } else {
                    // Tear entrance into Gigakhrushch
                    ServerWorld gigaWorld = serverWorld.getServer().getWorld(ModDimensions.GIGAKHRUSHCHYOVKA_KEY);
                    if (gigaWorld != null) {
                        int chunkOriginX = Math.floorDiv(serverPlayer.getBlockX(), 16) * 16;
                        int chunkOriginZ = Math.floorDiv(serverPlayer.getBlockZ(), 16) * 16;
                        int targetX = chunkOriginX + 7;
                        int targetZ = chunkOriginZ + 7;

                        // Gaussian offset based on level (higher level = safer landing around floor 85)
                        int targetFloor = 85 + (serverWorld.random.nextInt(21) - 10);
                        int targetY = targetFloor * GigakhrushchLayout.FLOOR_HEIGHT;
                        BlockPos landingPos = new BlockPos(targetX, targetY, targetZ);

                        // Build 3x3 safety platform
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                BlockPos p = landingPos.add(dx, 0, dz);
                                gigaWorld.setBlockState(p, ModBlocks.P46_PANEL.getDefaultState(), Block.NOTIFY_LISTENERS);
                                gigaWorld.setBlockState(p.up(1), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                                gigaWorld.setBlockState(p.up(2), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                            }
                        }

                        // Place return station
                        BlockPos returnStationPos = landingPos.south();
                        gigaWorld.setBlockState(returnStationPos, ModBlocks.ELEVATOR_STATION.getDefaultState(), Block.NOTIFY_LISTENERS);
                        gigaWorld.setBlockState(returnStationPos.up(3), ModBlocks.FLUORESCENT_LAMP.getDefaultState(), Block.NOTIFY_LISTENERS);

                        serverPlayer.teleport(gigaWorld, targetX + 0.5, targetY + 1.0, targetZ + 0.5, serverPlayer.getYaw(), serverPlayer.getPitch());
                        serverPlayer.sendMessage(Text.literal(String.format("§4[Разлом Реальности]: Пространство пробито! Вы провалились в Гигахрущ на Этаж %d!", targetFloor)), true);
                    }
                }
                return ActionResult.SUCCESS;
            }

            // 2. CONCRETE TRANSMUTATION: Sneak + Right Click erects 3x3 concrete barrier
            int concLevel = OccultEnchantmentHelper.getLevel(held, OccultEnchantmentHelper.CONCRETE_TRANSMUTATION);
            if (concLevel > 0 && player.isSneaking()) {
                if (player.getItemCooldownManager().isCoolingDown(held.getItem())) {
                    return ActionResult.PASS;
                }

                player.getItemCooldownManager().set(held.getItem(), Math.max(30, 80 - concLevel * 15));
                held.damage(2, serverPlayer, EquipmentSlot.MAINHAND);

                Direction facing = player.getHorizontalFacing();
                Direction sideDir = facing.rotateYClockwise();
                BlockPos centerPos = hitResult.getBlockPos().offset(hitResult.getSide());

                BlockState barrierState = switch (concLevel) {
                    case 1 -> Blocks.CRACKED_STONE_BRICKS.getDefaultState();
                    case 2 -> Blocks.STONE_BRICKS.getDefaultState();
                    default -> ModBlocks.P46_PANEL.getDefaultState();
                };

                int placedCount = 0;
                for (int h = 0; h < 3; h++) {
                    for (int w = -1; w <= 1; w++) {
                        BlockPos wallPos = centerPos.offset(sideDir, w).up(h);
                        if (serverWorld.getBlockState(wallPos).isAir()) {
                            serverWorld.setBlockState(wallPos, barrierState, Block.NOTIFY_LISTENERS);
                            serverWorld.spawnParticles(ParticleTypes.POOF, wallPos.getX() + 0.5, wallPos.getY() + 0.5, wallPos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.1);
                            placedCount++;
                        }
                    }
                }

                if (placedCount > 0) {
                    serverWorld.playSound(null, centerPos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.2f, 0.8f);
                    serverWorld.playSound(null, centerPos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 0.6f);
                    serverPlayer.sendMessage(Text.literal("§7[Бетоноворот]: Монолитный заслон возведён! («Святой бетон спасёт от скверны»)"), true);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerAttackEntityHook() {
        AttackEntityCallback.EVENT.register((PlayerEntity player, World world, Hand hand, net.minecraft.entity.Entity entity, EntityHitResult hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND || !(world instanceof ServerWorld serverWorld) || !(entity instanceof LivingEntity target)) {
                return ActionResult.PASS;
            }

            ItemStack held = player.getMainHandStack();
            if (held.isEmpty()) return ActionResult.PASS;

            // 1. BIOMASS CONTAGION: Life leech, chthonic withering, tendril chain
            int bioLevel = OccultEnchantmentHelper.getLevel(held, OccultEnchantmentHelper.BIOMASS_CONTAGION);
            if (bioLevel > 0) {
                // Leech health
                player.heal(1.0f + bioLevel * 0.75f);

                // Inflict wither and slowness
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60 * bioLevel, bioLevel - 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60 * bioLevel, 1));

                // Particles & audio
                serverWorld.spawnParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.0, target.getZ(), 25, 0.4, 0.5, 0.4, 0.1);
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
                serverWorld.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 0.8f, 0.7f);

                // Tendril chain: spread to up to (bioLevel * 2) nearby hostiles
                List<HostileEntity> nearby = serverWorld.getEntitiesByClass(
                        HostileEntity.class,
                        new Box(target.getBlockPos()).expand(5.0),
                        e -> e != target && e.isAlive()
                );

                int chained = 0;
                for (HostileEntity foe : nearby) {
                    if (chained >= bioLevel * 2) break;
                    foe.damage(serverWorld.getDamageSources().magic(), 2.0f * bioLevel);
                    foe.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40 * bioLevel, 0));
                    serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, foe.getX(), foe.getY() + 1.0, foe.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                    chained++;
                }
            }

            // 2. BORSHCHEVIK POISON: Phototoxic reaction detonating in soul fire under light
            int borschLevel = OccultEnchantmentHelper.getLevel(held, OccultEnchantmentHelper.BORSHCHEVIK_POISON);
            if (borschLevel > 0) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80 * borschLevel, borschLevel - 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60 * borschLevel, 0));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 80 * borschLevel, 0));

                int light = serverWorld.getLightLevel(target.getBlockPos());
                boolean inDaylight = serverWorld.isDay() && serverWorld.isSkyVisible(target.getBlockPos());

                if (inDaylight || light >= 11) {
                    target.setOnFireFor(4 * borschLevel);
                    target.damage(serverWorld.getDamageSources().magic(), 4.0f + 2.0f * borschLevel);

                    serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 35, 0.4, 0.6, 0.4, 0.08);
                    serverWorld.spawnParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                    serverWorld.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.2f, 0.7f);
                    serverWorld.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_BLAZE_HURT, SoundCategory.PLAYERS, 1.0f, 0.6f);

                    player.sendMessage(Text.literal("§2[Яд Борщевика]: §aФототоксическая реакция на свет! Плоть цели вскипает пламенем душ!"), true);
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerBlockBreakHook() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !(world instanceof ServerWorld serverWorld)) return;

            ItemStack held = player.getMainHandStack();
            int concLevel = OccultEnchantmentHelper.getLevel(held, OccultEnchantmentHelper.CONCRETE_TRANSMUTATION);
            if (concLevel <= 0) return;

            // Check if stone, deepslate, or concrete
            if (state.isOf(Blocks.STONE) || state.isOf(Blocks.DEEPSLATE) || state.isOf(Blocks.COBBLESTONE)
                    || state.isOf(Blocks.ANDESITE) || state.isOf(Blocks.DIORITE) || state.isOf(Blocks.GRANITE)
                    || state.isOf(ModBlocks.P46_PANEL)) {

                if (serverWorld.random.nextFloat() < concLevel * 0.35f) {
                    Block.dropStack(serverWorld, pos, new ItemStack(Items.IRON_NUGGET, 1 + serverWorld.random.nextInt(3)));
                    serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
                }

                if (serverWorld.random.nextFloat() < concLevel * 0.25f) {
                    Block.dropStack(serverWorld, pos, new ItemStack(ModBlocks.P46_PANEL.asItem(), 1));
                }

                if (concLevel >= 3 && serverWorld.random.nextFloat() < 0.20f) {
                    Block.dropStack(serverWorld, pos, new ItemStack(Items.IRON_INGOT, 1));
                }
            }
        });
    }

    private static void registerEntityDeathHook() {
        ServerLivingEntityEvents.AFTER_DEATH.register((LivingEntity entity, DamageSource damageSource) -> {
            if (entity.getWorld().isClient || !(entity.getWorld() instanceof ServerWorld serverWorld)) return;

            if (damageSource.getAttacker() instanceof PlayerEntity player) {
                ItemStack weapon = player.getMainHandStack();
                int bioLevel = OccultEnchantmentHelper.getLevel(weapon, OccultEnchantmentHelper.BIOMASS_CONTAGION);
                if (bioLevel > 0) {
                    serverWorld.spawnParticles(ParticleTypes.CRIMSON_SPORE, entity.getX(), entity.getY() + 0.8, entity.getZ(), 40, 0.5, 0.5, 0.5, 0.1);
                    serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, entity.getX(), entity.getY() + 0.8, entity.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
                    serverWorld.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.0f, 0.8f);

                    // Additional healing
                    player.heal(2.0f);
                }
            }
        });
    }
}
