package com.samosbor.item;

import com.samosbor.block.ModBlocks;
import com.samosbor.dimension.GigakhrushchLayout;
import com.samosbor.dimension.ModDimensions;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RiftSplashPotionItem extends Item implements PolymerItem {

    public RiftSplashPotionItem() {
        super(new Item.Settings().maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            Vec3d start = user.getEyePos();
            Vec3d dir = user.getRotationVector();
            double range = 24.0;
            Vec3d end = start.add(dir.multiply(range));

            BlockHitResult bHit = world.raycast(new RaycastContext(
                    start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
            if (bHit.getType() != HitResult.Type.MISS) {
                end = bHit.getPos();
            }

            // Trajectory particles
            double dist = start.distanceTo(end);
            int steps = (int) (dist * 3);
            for (int i = 0; i < steps; i++) {
                double p = (double) i / steps;
                Vec3d point = start.lerp(end, p);
                serverWorld.spawnParticles(ParticleTypes.PORTAL, point.x, point.y, point.z, 2, 0.05, 0.05, 0.05, 0.02);
            }

            // Dimensional vortex detonation
            serverWorld.spawnParticles(ParticleTypes.PORTAL, end.x, end.y + 0.5, end.z, 80, 0.8, 0.8, 0.8, 0.3);
            serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, end.x, end.y + 0.5, end.z, 30, 0.5, 0.5, 0.5, 0.08);
            serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, end.x, end.y + 0.5, end.z, 40, 0.6, 0.6, 0.6, 0.1);
            serverWorld.playSound(null, end.x, end.y, end.z, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.3f, 0.7f);
            serverWorld.playSound(null, end.x, end.y, end.z, SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.PLAYERS, 1.1f, 0.5f);

            boolean isGigakhrushch = serverWorld.getRegistryKey().getValue().equals(ModDimensions.GIGAKHRUSHCHYOVKA_KEY.getValue());
            Box aoe = new Box(end.subtract(4.0, 3.0, 4.0), end.add(4.0, 3.0, 4.0));
            List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, aoe, LivingEntity::isAlive);

            if (isGigakhrushch) {
                // Banishes entities to Overworld
                ServerWorld overworld = serverWorld.getServer().getWorld(World.OVERWORLD);
                if (overworld != null) {
                    for (LivingEntity target : targets) {
                        BlockPos targetPos = overworld.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, target.getBlockPos());
                        target.teleport(overworld, targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, java.util.Set.of(), target.getYaw(), target.getPitch());
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0));
                        if (target instanceof ServerPlayerEntity sp) {
                            sp.sendMessage(Text.literal("§b[Колба Прорыва РП-7]: Пространство разорвано! Вас вышвырнуло в Верхний мир!"), true);
                        }
                    }
                }
            } else {
                // Sucks entities into Gigakhrushch
                ServerWorld gigaWorld = serverWorld.getServer().getWorld(ModDimensions.GIGAKHRUSHCHYOVKA_KEY);
                if (gigaWorld != null) {
                    for (LivingEntity target : targets) {
                        int chunkOriginX = Math.floorDiv(target.getBlockX(), 16) * 16;
                        int chunkOriginZ = Math.floorDiv(target.getBlockZ(), 16) * 16;
                        int targetX = chunkOriginX + 7;
                        int targetZ = chunkOriginZ + 7;

                        int targetFloor = 85 + (serverWorld.random.nextInt(21) - 10);
                        int targetY = targetFloor * GigakhrushchLayout.FLOOR_HEIGHT;
                        BlockPos landingPos = new BlockPos(targetX, targetY, targetZ);

                        // Platform
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                BlockPos p = landingPos.add(dx, 0, dz);
                                gigaWorld.setBlockState(p, ModBlocks.P46_PANEL.getDefaultState(), Block.NOTIFY_LISTENERS);
                                gigaWorld.setBlockState(p.up(1), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                                gigaWorld.setBlockState(p.up(2), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                            }
                        }

                        target.teleport(gigaWorld, targetX + 0.5, targetY + 1.0, targetZ + 0.5, java.util.Set.of(), target.getYaw(), target.getPitch());
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0));
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
                        if (target instanceof ServerPlayerEntity sp) {
                            sp.sendMessage(Text.literal(String.format("§4[Колба Прорыва РП-7]: Пространственный вихрь затянул вас в Гигахрущ на Этаж %d!", targetFloor)), true);
                        }
                    }
                }
            }

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            user.getItemCooldownManager().set(this, 30);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.SPLASH_POTION;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§d§lКолба Прорыва РП-7"));
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§5[Спецразработка НИИ Аномалий «РП-7»]"),
                Text.literal("§dПри взрыве генерирует сингулярность разлома пространства."),
                Text.literal("§eЗатягивает всех существ в радиусе 4 блоков в Гигахрущ"),
                Text.literal("§b(или мгновенно изгоняет обратно в Верхний мир, если вы внутри).")
        )));
        return out;
    }
}
