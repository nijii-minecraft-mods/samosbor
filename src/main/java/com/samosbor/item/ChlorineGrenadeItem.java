package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChlorineGrenadeItem extends Item implements PolymerItem {

    public ChlorineGrenadeItem() {
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

            // Spawn trajectory trail
            double dist = start.distanceTo(end);
            int steps = (int) (dist * 3);
            for (int i = 0; i < steps; i++) {
                double p = (double) i / steps;
                Vec3d point = start.lerp(end, p);
                serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.01);
            }

            // Explode into thick toxic chlorine smoke cloud
            for (int i = 0; i < 40; i++) {
                double ox = (world.random.nextDouble() - 0.5) * 5.0;
                double oy = world.random.nextDouble() * 2.5;
                double oz = (world.random.nextDouble() - 0.5) * 5.0;
                serverWorld.spawnParticles(ParticleTypes.SNEEZE, end.x + ox, end.y + oy, end.z + oz, 3, 0.1, 0.1, 0.1, 0.02);
                serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, end.x + ox, end.y + oy, end.z + oz, 2, 0.2, 0.2, 0.2, 0.04);
            }

            // Toxic gas damage to living entities
            Box cloudBox = new Box(end.subtract(4, 2, 4), end.add(4, 4, 4));
            List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, cloudBox, LivingEntity::isAlive);
            for (LivingEntity target : targets) {
                // Players wearing Gas Mask or L-1M suit are immune
                if (target instanceof PlayerEntity player) {
                    boolean protectedByMask = player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.GAS_MASK);
                    boolean protectedBySuit = player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.L1M_SUIT);
                    if (protectedByMask || protectedBySuit) {
                        continue;
                    }
                }

                target.damage(serverWorld.getDamageSources().magic(), 10.0f);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 160, 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 2));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1));
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 0.8f, 0.8f);
            world.playSound(null, end.x, end.y, end.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.2f, 0.6f);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            user.getItemCooldownManager().set(this, 20);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.SPLASH_POTION;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§2Хлорная шашка УДШ-4"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Унифицированная дымовая шашка с хлорпикрином."),
                Text.literal("§2Облако едкого газа радиусом 4 блока"),
                Text.literal("§cУрон: 10 ед. + Иссушение II, Замедление III"),
                Text.literal("§aБезопасно при наличии противогаза или химкостюма")
        )));
        return out;
    }
}
