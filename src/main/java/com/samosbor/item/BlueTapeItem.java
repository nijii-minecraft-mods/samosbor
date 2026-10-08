package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueTapeItem extends Item implements PolymerItem {

    public BlueTapeItem() {
        super(new Item.Settings().maxDamage(10));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack tapeStack = user.getStackInHand(hand);
        ItemStack offhand = user.getOffHandStack();

        if (!world.isClient) {
            if (hand == Hand.OFF_HAND) {
                return TypedActionResult.pass(tapeStack);
            }

            if (!offhand.isEmpty() && offhand.isDamaged()) {
                offhand.setDamage(Math.max(0, offhand.getDamage() - 100));
                tapeStack.damage(1, user, LivingEntity.getSlotForHand(hand));

                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, user.getX(), user.getY() + 1.0, user.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                }

                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.PLAYERS, 1.0f, 1.2f);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.PLAYERS, 0.9f, 0.8f);

                user.sendMessage(Text.literal("§9[Синяя изолента]: Предмет в левой руке обмотан (+100 прочности)!"), true);
                return TypedActionResult.success(tapeStack);
            } else {
                user.sendMessage(Text.literal("§7[Изолента]: Возьмите повреждённый предмет во вторую руку."), true);
                return TypedActionResult.pass(tapeStack);
            }
        }

        return TypedActionResult.success(tapeStack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.BLUE_DYE;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§9Синяя изолента ГОСТ-2162"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Главный инструмент советского инженера."),
                Text.literal("§eПКМ: чинит предмет во второй руке на 100 ед."),
                Text.literal("§7Чинит инструменты, оружие, броню и противогазы!"),
                Text.literal("§8«Держит всё: от трубы до вселенной»")
        )));
        return out;
    }
}
