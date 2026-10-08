package com.samosbor.item;

import com.samosbor.block.ModBlocks;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
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
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Roks3FlamethrowerItem extends Item implements PolymerItem {

    public Roks3FlamethrowerItem() {
        super(new Item.Settings().maxDamage(500));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            Vec3d look = user.getRotationVector();
            Vec3d eyePos = user.getEyePos();

            // Stream of intense thermite flame up to 14 blocks
            for (int i = 1; i <= 14; i++) {
                Vec3d partPos = eyePos.add(look.multiply(i));
                serverWorld.spawnParticles(ParticleTypes.FLAME, partPos.x, partPos.y, partPos.z, 8, 0.4, 0.4, 0.4, 0.08);
                serverWorld.spawnParticles(ParticleTypes.LAVA, partPos.x, partPos.y, partPos.z, 2, 0.2, 0.2, 0.2, 0.05);
                serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, partPos.x, partPos.y, partPos.z, 3, 0.3, 0.3, 0.3, 0.04);

                // Incinerate biomass blocks in flame ray
                BlockPos bPos = BlockPos.ofFloored(partPos);
                if (serverWorld.getBlockState(bPos).isOf(ModBlocks.BIOMASS)) {
                    serverWorld.setBlockState(bPos, Blocks.AIR.getDefaultState());
                    serverWorld.spawnParticles(ParticleTypes.ASH, bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.05);
                }
            }

            // High damage AOE cone
            Vec3d endPos = eyePos.add(look.multiply(14));
            Box coneBox = new Box(eyePos, endPos).expand(2.5);
            List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, coneBox, e -> e != user && e.isAlive());

            for (LivingEntity target : targets) {
                target.setOnFireFor(8.0f);
                target.damage(serverWorld.getDamageSources().onFire(), 14.0f);
                // Knockback
                Vec3d push = look.multiply(0.6);
                target.addVelocity(push.x, 0.2, push.z);
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.2f, 0.7f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, 0.6f);
            stack.damage(1, user, LivingEntity.getSlotForHand(hand));
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.NETHERITE_HOE;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§4Тяжёлый огнемёт РОКС-3 «Шмель ГО»"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Армейский штурмовой огнемёт Ликвидаторов."),
                Text.literal("§cУрон: 14 ед. термитного огня"),
                Text.literal("§6Дальность: 14 блоков"),
                Text.literal("§eОсобенность: мгновенно выжигает биомассу Самосбора.")
        )));
        return out;
    }
}
