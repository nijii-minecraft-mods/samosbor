package com.samosbor.item;

import com.samosbor.block.ModBlocks;
import com.samosbor.event.SamosborManager;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DosimeterItem extends Item implements PolymerItem {

    public DosimeterItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !(entity instanceof PlayerEntity player)) {
            return;
        }

        boolean isHeld = player.getMainHandStack() == stack || player.getOffHandStack() == stack;
        if (!isHeld) {
            return;
        }

        int floor = Math.max(1, player.getBlockY() / 6);
        int rad = 18 + (floor % 7);

        // Deep foundations & flooded reactor have high radiation
        if (floor <= 35) {
            rad += 320 + (35 - floor) * 18;
        } else if (floor >= 135) {
            // Upper stratum atmospheric anomalies
            rad += 140 + (floor - 135) * 12;
        }

        // Active Samosbor event creates massive radiation surge
        if (SamosborManager.isSamosborActive()) {
            rad += 4200;
        }

        // Scan nearby 5-block radius for Biomass or Toxic Mushrooms
        BlockPos playerPos = player.getBlockPos();
        int bioCount = 0;
        for (int dx = -5; dx <= 5; dx += 2) {
            for (int dy = -3; dy <= 3; dy += 2) {
                for (int dz = -5; dz <= 5; dz += 2) {
                    BlockState s = world.getBlockState(playerPos.add(dx, dy, dz));
                    if (s.isOf(ModBlocks.BIOMASS)) {
                        bioCount += 2;
                    } else if (s.isOf(ModBlocks.TOXIC_MUSHROOM)) {
                        bioCount += 1;
                    }
                }
            }
        }
        rad += bioCount * 75;

        // Geiger clicking sound
        float clickChance = 0.04f;
        if (rad >= 1500) {
            clickChance = 0.95f;
        } else if (rad >= 600) {
            clickChance = 0.65f;
        } else if (rad >= 250) {
            clickChance = 0.35f;
        } else if (rad >= 70) {
            clickChance = 0.15f;
        }

        if (world.random.nextFloat() < clickChance) {
            float pitch = 1.6f + world.random.nextFloat() * 0.5f;
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.4f, pitch);
        }

        // Display actionbar reading every 20 ticks (1 second)
        if (player.age % 20 == 0) {
            String color;
            String status;
            if (rad < 50) {
                color = "§a";
                status = "Норма";
            } else if (rad < 250) {
                color = "§e";
                status = "Повышенный фон";
            } else if (rad < 800) {
                color = "§6";
                status = "ОПАСНО: Наденьте ГП-5!";
            } else {
                color = "§4§l";
                status = "КРИТИЧЕСКИЙ ФОН: Примите Антирадин!";
            }

            player.sendMessage(Text.literal("§8[ДП-5В]: " + color + "☢ " + rad + " мкР/ч §7(" + status + "§7)"), true);
        }
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.CLOCK;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6Дозиметр полевой ДП-5В"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Войсковой прибор радиационной разведки ГО."),
                Text.literal("§eЗвуковой счётчик Гейгера при приближении к биомассе"),
                Text.literal("§aОтображает уровень излучения (мкР/ч) в панели действий"),
                Text.literal("§8«Стрелка зашкаливает... Беги!»")
        )));
        return out;
    }
}
