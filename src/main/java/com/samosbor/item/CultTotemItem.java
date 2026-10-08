package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CultTotemItem extends Item implements PolymerItem {

    public CultTotemItem() {
        super(new Item.Settings().maxDamage(120).maxCount(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity player && world instanceof ServerWorld serverWorld) {
            boolean inOffhand = player.getEquippedStack(EquipmentSlot.OFFHAND) == stack;
            boolean inMainhand = player.getEquippedStack(EquipmentSlot.MAINHAND) == stack;

            if ((inOffhand || inMainhand) && serverWorld.random.nextInt(160) == 0) {
                serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 0.8f, 1.4f);
                serverWorld.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 0.8, player.getZ(),
                        5, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    public static boolean tryProtectFromSamosbor(PlayerEntity player, ItemStack stack, ServerWorld world) {
        if (stack.getDamage() < stack.getMaxDamage()) {
            stack.damage(1, player, EquipmentSlot.OFFHAND);

            // Remove lethal damage and give resistance
            player.removeStatusEffect(StatusEffects.WITHER);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, false, false));

            // Sanity cost: nausea and whispering hallucination
            if (world.random.nextInt(15) == 0) {
                player.sendMessage(Text.literal("§5[Сухожилия тотема]: §d«Дыши глубже... туман сохранит твою плоть...»"), true);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, false, false));
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.PLAYERS, 0.6f, 1.6f);
            }
            return true;
        }
        return false;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.TOTEM_OF_UNDYING;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§dОккультный тотем плоти (Амулет)"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Сплетённые сухожилия с клапаном противогаза."),
                Text.literal("§eВо второй руке: поглощает урон ядовитого тумана Самосбора"),
                Text.literal("§cРасплата: туманит рассудок и нашёптывает безумие"),
                Text.literal("§8«Оно дышит вместо тебя»")
        )));
        return out;
    }
}
