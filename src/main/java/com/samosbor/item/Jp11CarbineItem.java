package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Jp11CarbineItem extends Item implements PolymerItem {

    public Jp11CarbineItem() {
        super(new Item.Settings().maxDamage(300));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            Vec3d start = user.getEyePos();
            Vec3d dir = user.getRotationVector();
            double maxRange = 36.0;
            Vec3d end = start.add(dir.multiply(maxRange));

            // Block raycast
            BlockHitResult blockHit = world.raycast(new RaycastContext(
                    start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
            if (blockHit.getType() != HitResult.Type.MISS) {
                end = blockHit.getPos();
            }

            // Entity raycast
            Box sweepBox = new Box(start, end).expand(1.2);
            List<LivingEntity> potentialTargets = world.getEntitiesByClass(LivingEntity.class, sweepBox, e -> e != user && e.isAlive());

            LivingEntity hitEntity = null;
            double closestDistSq = Double.MAX_VALUE;

            for (LivingEntity target : potentialTargets) {
                Box targetBox = target.getBoundingBox().expand(0.3);
                var hitOpt = targetBox.raycast(start, end);
                if (hitOpt.isPresent()) {
                    double distSq = start.squaredDistanceTo(hitOpt.get());
                    if (distSq < closestDistSq) {
                        closestDistSq = distSq;
                        hitEntity = target;
                    }
                }
            }

            // Spawn caustic particle beam trail
            double totalDist = start.distanceTo(end);
            int steps = (int) (totalDist * 3);
            for (int i = 0; i < steps; i++) {
                double progress = (double) i / steps;
                Vec3d p = start.lerp(end, progress);
                serverWorld.spawnParticles(ParticleTypes.GLOW, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.01);
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.01);
            }

            if (hitEntity != null) {
                // Caustic needle hit: bypasses armor with magic damage
                hitEntity.damage(serverWorld.getDamageSources().magic(), 18.0f);
                hitEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1));
                hitEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));

                serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, hitEntity.getX(), hitEntity.getY() + 1.0, hitEntity.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
                world.playSound(null, hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(), SoundEvents.ENTITY_WARDEN_ATTACK_IMPACT, SoundCategory.PLAYERS, 1.0f, 1.4f);
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.2f, 1.6f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.PLAYERS, 0.8f, 2.0f);

            user.getItemCooldownManager().set(this, 25);
            stack.damage(1, user, LivingEntity.getSlotForHand(hand));
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.CROSSBOW;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§bПневмокарабин спецраствора ЖП-11"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Экспериментальное химическое оружие НИИ."),
                Text.literal("§bУрон: 18 ед. едкой субстанции (сквозь броню)"),
                Text.literal("§5Эффект: Иссушение II, Замедление III"),
                Text.literal("§7Дальность: 36 блоков")
        )));
        return out;
    }
}
