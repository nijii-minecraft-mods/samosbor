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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlareGunItem extends Item implements PolymerItem {

    public FlareGunItem() {
        super(new Item.Settings().maxDamage(100));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack gunStack = user.getStackInHand(hand);

        // Check for ammunition
        boolean hasAmmo = user.getAbilities().creativeMode;
        ItemStack ammoStack = ItemStack.EMPTY;
        for (int i = 0; i < user.getInventory().size(); i++) {
            ItemStack s = user.getInventory().getStack(i);
            if (s.isOf(ModItems.SIGNAL_FLARE)) {
                hasAmmo = true;
                ammoStack = s;
                break;
            }
        }

        if (!hasAmmo) {
            if (!world.isClient) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.8f, 1.4f);
                user.sendMessage(Text.literal("§c[!] Нет сигнальных ракет СР-24!"), true);
            }
            return TypedActionResult.fail(gunStack);
        }

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            Vec3d start = user.getEyePos();
            Vec3d dir = user.getRotationVector();
            double range = 48.0;
            Vec3d end = start.add(dir.multiply(range));

            BlockHitResult bHit = world.raycast(new RaycastContext(
                    start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
            if (bHit.getType() != HitResult.Type.MISS) {
                end = bHit.getPos();
            }

            // Spawn bright red flare tracer particles
            double dist = start.distanceTo(end);
            int steps = (int) (dist * 4);
            for (int i = 0; i < steps; i++) {
                double p = (double) i / steps;
                Vec3d point = start.lerp(end, p);
                serverWorld.spawnParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 2, 0.05, 0.05, 0.05, 0.01);
                serverWorld.spawnParticles(ParticleTypes.LAVA, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.01);
            }

            // Explosion flash of light and fire at impact point
            serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, end.x, end.y, end.z, 20, 0.5, 0.5, 0.5, 0.05);
            serverWorld.spawnParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 3, 0.2, 0.2, 0.2, 0.01);
            serverWorld.spawnParticles(ParticleTypes.LAVA, end.x, end.y, end.z, 15, 0.4, 0.4, 0.4, 0.1);

            // Blind and burn nearby monsters
            Box flareBox = new Box(end.subtract(6, 6, 6), end.add(6, 6, 6));
            List<LivingEntity> monsters = world.getEntitiesByClass(LivingEntity.class, flareBox, e -> e != user && e.isAlive());
            for (LivingEntity m : monsters) {
                m.setOnFireFor(8.0f);
                m.damage(serverWorld.getDamageSources().onFire(), 12.0f);
                m.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
                m.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 160, 1));
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1.5f, 0.8f);
            world.playSound(null, end.x, end.y, end.z, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 1.0f, 1.2f);

            if (!user.getAbilities().creativeMode && !ammoStack.isEmpty()) {
                ammoStack.decrement(1);
            }
            gunStack.damage(1, user, LivingEntity.getSlotForHand(hand));
            user.getItemCooldownManager().set(this, 30);
        }

        return TypedActionResult.success(gunStack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.CROSSBOW;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cСигнальный пистолет СПШ-44"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Ракетница Шпагина калибра 26 мм."),
                Text.literal("§cОслепляющая магниевая вспышка на 48 блоков"),
                Text.literal("§6Поджигает цели, наносит 12 ед. урона"),
                Text.literal("§eТребует ракеты СР-24 для стрельбы")
        )));
        return out;
    }
}
