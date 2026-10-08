package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlamethrowerItem extends Item implements PolymerItem {

    public FlamethrowerItem() {
        super(new Item.Settings().maxDamage(250));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            Vec3d look = user.getRotationVector();
            Vec3d eyePos = user.getEyePos();

            int plasmaLvl = com.samosbor.enchantment.OccultEnchantmentHelper.getLevel(stack, com.samosbor.enchantment.OccultEnchantmentHelper.PLASMA_PURGE);
            int range = (plasmaLvl > 0) ? (6 + plasmaLvl * 2) : 6;
            float damage = 6.0f + (plasmaLvl * 3.0f);
            float fireSeconds = 5.0f + (plasmaLvl * 3.0f);

            // Spawn flame cone particles
            for (int i = 1; i <= range; i++) {
                Vec3d partPos = eyePos.add(look.multiply(i));
                serverWorld.spawnParticles(ParticleTypes.FLAME, partPos.x, partPos.y, partPos.z, 5, 0.3, 0.3, 0.3, 0.05);
                serverWorld.spawnParticles(ParticleTypes.SMOKE, partPos.x, partPos.y, partPos.z, 2, 0.2, 0.2, 0.2, 0.02);
                if (plasmaLvl > 0) {
                    serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, partPos.x, partPos.y, partPos.z, 2, 0.2, 0.2, 0.2, 0.04);
                    serverWorld.spawnParticles(ParticleTypes.LAVA, partPos.x, partPos.y, partPos.z, 1, 0.1, 0.1, 0.1, 0.02);
                }
            }

            // Ignite and damage living entities in flame cone
            Vec3d endPos = eyePos.add(look.multiply(range + 1));
            Box coneBox = new Box(eyePos, endPos).expand(1.5 + (plasmaLvl * 0.3));
            List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, coneBox, e -> e != user && e.isAlive());

            for (LivingEntity target : targets) {
                target.setOnFireFor(fireSeconds);
                target.damage(serverWorld.getDamageSources().onFire(), damage);

                // Plasma armor melting
                if (plasmaLvl > 0) {
                    for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
                        if (slot.isArmorSlot()) {
                            ItemStack armor = target.getEquippedStack(slot);
                            if (!armor.isEmpty()) {
                                armor.damage(2 * plasmaLvl, target, slot);
                            }
                        }
                    }
                }
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    (plasmaLvl > 0) ? SoundEvents.ENTITY_BLAZE_SHOOT : SoundEvents.ITEM_FIRECHARGE_USE,
                    SoundCategory.PLAYERS, 1.0f, (plasmaLvl > 0) ? 0.8f : 1.0f);
            stack.damage(1, user, LivingEntity.getSlotForHand(hand));
            user.getItemCooldownManager().set(this, 10);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.BLAZE_ROD;
    }
}
