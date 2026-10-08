package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Cs4AerosolItem extends Item implements PolymerItem {

    public Cs4AerosolItem() {
        super(new Item.Settings().maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            Vec3d start = user.getEyePos();
            Vec3d dir = user.getRotationVector();
            double range = 20.0;
            Vec3d end = start.add(dir.multiply(range));

            BlockHitResult bHit = world.raycast(new RaycastContext(
                    start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
            if (bHit.getType() != HitResult.Type.MISS) {
                end = bHit.getPos();
            }

            // Trajectory
            double dist = start.distanceTo(end);
            int steps = (int) (dist * 3);
            for (int i = 0; i < steps; i++) {
                double p = (double) i / steps;
                Vec3d point = start.lerp(end, p);
                serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.01);
            }

            // Aerosol cloud
            for (int i = 0; i < 45; i++) {
                double ox = (world.random.nextDouble() - 0.5) * 6.0;
                double oy = world.random.nextDouble() * 3.0;
                double oz = (world.random.nextDouble() - 0.5) * 6.0;
                serverWorld.spawnParticles(ParticleTypes.SNEEZE, end.x + ox, end.y + oy, end.z + oz, 3, 0.1, 0.1, 0.1, 0.02);
                serverWorld.spawnParticles(ParticleTypes.EFFECT, end.x + ox, end.y + oy, end.z + oz, 2, 0.2, 0.4, 0.1, 0.05);
            }

            serverWorld.playSound(null, end.x, end.y, end.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.2f, 0.8f);
            serverWorld.playSound(null, end.x, end.y, end.z, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.PLAYERS, 1.0f, 1.5f);

            Box cloudBox = new Box(end.subtract(6, 3, 6), end.add(6, 4, 6));
            List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, cloudBox, LivingEntity::isAlive);

            // Turn mobs against each other
            for (LivingEntity entity : targets) {
                if (entity instanceof MobEntity mob) {
                    mob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0));
                    mob.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
                    mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1));

                    // Pick nearest other mob to attack
                    List<MobEntity> others = world.getEntitiesByClass(MobEntity.class, cloudBox.expand(6.0), o -> o != mob && o.isAlive());
                    if (!others.isEmpty()) {
                        MobEntity targetFoe = others.get(world.random.nextInt(others.size()));
                        mob.setTarget(targetFoe);
                    }
                } else if (entity instanceof PlayerEntity player && player != user) {
                    boolean protectedByMask = player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.GAS_MASK);
                    boolean protectedBySuit = player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.L1M_SUIT);
                    if (!protectedByMask && !protectedBySuit) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
                    }
                }
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 0.8f, 0.9f);
            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            user.getItemCooldownManager().set(this, 25);
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
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§e§lАэрозоль-дезориентатор АС-4"));
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§6[Спецсредство ГО «АС-4»]"),
                Text.literal("§eРаспыляет нейропаралитический аэрозоль дезориентации."),
                Text.literal("§cЗаставляет мутантов и мимиков атаковать друг друга в ярости."),
                Text.literal("§7Накладывает свечение и слепоту в радиусе 6 блоков.")
        )));
        return out;
    }
}
