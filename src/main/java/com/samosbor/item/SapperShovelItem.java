package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SapperShovelItem extends Item implements PolymerItem {

    public SapperShovelItem() {
        super(new Item.Settings().maxDamage(350));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {
            target.damage(attacker.getWorld().getDamageSources().playerAttack(player), 7.5f);
        } else {
            target.damage(attacker.getWorld().getDamageSources().mobAttack(attacker), 7.5f);
        }
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && state.getHardness(world, pos) != 0.0f) {
            stack.damage(1, miner, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        if (state.isIn(BlockTags.SHOVEL_MINEABLE) || state.isIn(BlockTags.DIRT)) {
            return 8.0f;
        }
        return 1.0f;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.IRON_SHOVEL;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§7Сапёрная лопатка МПЛ-50"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Малая пехотная лопатка с закалённым лезвием."),
                Text.literal("§cУрон в ближнем бою: 7.5 ед."),
                Text.literal("§aМгновенно раскапывает завалы, грунт и туф")
        )));
        return out;
    }
}
