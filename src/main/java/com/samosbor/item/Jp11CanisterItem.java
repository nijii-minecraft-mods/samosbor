package com.samosbor.item;

import com.samosbor.block.ModBlocks;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.BlockState;
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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Jp11CanisterItem extends Item implements PolymerItem {

    public Jp11CanisterItem() {
        super(new Item.Settings().maxDamage(60));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            Vec3d eyePos = user.getEyePos();
            Vec3d lookDir = user.getRotationVector();
            Vec3d targetCenter = eyePos.add(lookDir.multiply(4.0));

            // Spray particles
            for (int i = 0; i < 25; i++) {
                double speed = 0.5 + world.random.nextDouble() * 0.5;
                Vec3d pDir = lookDir.multiply(speed).add((world.random.nextDouble() - 0.5) * 0.3, (world.random.nextDouble() - 0.5) * 0.3, (world.random.nextDouble() - 0.5) * 0.3);
                serverWorld.spawnParticles(ParticleTypes.WITCH, eyePos.x, eyePos.y - 0.2, eyePos.z, 1, pDir.x, pDir.y, pDir.z, 0.2);
                serverWorld.spawnParticles(ParticleTypes.BUBBLE_POP, eyePos.x, eyePos.y - 0.2, eyePos.z, 1, pDir.x, pDir.y, pDir.z, 0.1);
            }

            // Chemical dissolving of Biomass in cone
            BlockPos centerPos = BlockPos.ofFloored(targetCenter);
            int dissolved = 0;
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        BlockPos checkPos = centerPos.add(dx, dy, dz);
                        BlockState state = world.getBlockState(checkPos);
                        if (state.isOf(ModBlocks.BIOMASS) || state.isOf(ModBlocks.TOXIC_MUSHROOM)) {
                            world.setBlockState(checkPos, Blocks.AIR.getDefaultState());
                            serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.02);
                            dissolved++;
                        }
                    }
                }
            }

            // Damage and dissolve monsters / Mimics
            Box sprayBox = new Box(targetCenter.subtract(3, 2, 3), targetCenter.add(3, 2, 3));
            List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, sprayBox, e -> e != user && e.isAlive());
            for (LivingEntity enemy : enemies) {
                enemy.damage(serverWorld.getDamageSources().magic(), 14.0f);
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 140, 2));
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 140, 1));
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0f, 1.4f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.PLAYERS, 0.8f, 1.2f);

            if (!user.getAbilities().creativeMode) {
                stack.damage(1, user, LivingEntity.getSlotForHand(hand));
            }

            user.getItemCooldownManager().set(this, 15);
            if (dissolved > 0) {
                user.sendMessage(Text.literal("§d[ЖП-11]: Растворено " + dissolved + " ед. биомассы!"), true);
            }
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.DRAGON_BREATH;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§dАэрозольный баллон спецраствора ЖП-11"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Едкий дезактивирующий состав Ликвидаторов."),
                Text.literal("§dПКМ: химическая струя дезинфекции"),
                Text.literal("§aМгновенно растворяет блоки биомассы и грибов"),
                Text.literal("§cУрон по монстрам: 14 ед. магического урона")
        )));
        return out;
    }
}
