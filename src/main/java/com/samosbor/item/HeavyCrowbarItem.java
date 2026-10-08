package com.samosbor.item;

import com.samosbor.block.HermeticDoorBlock;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeavyCrowbarItem extends Item implements PolymerItem {

    public HeavyCrowbarItem() {
        super(new Item.Settings().maxDamage(450));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {
            target.damage(attacker.getWorld().getDamageSources().playerAttack(player), 9.0f);
        } else {
            target.damage(attacker.getWorld().getDamageSources().mobAttack(attacker), 9.0f);
        }
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        if (state.getBlock() instanceof HermeticDoorBlock) {
            if (!world.isClient) {
                boolean isWelded = state.get(HermeticDoorBlock.WELDED);
                if (isWelded) {
                    if (player != null) {
                        player.sendMessage(Text.literal("§c[!] Сварной шов слишком прочен для лома! Нужен Газовый резак."), true);
                    }
                    world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.8f, 0.6f);
                    return ActionResult.SUCCESS;
                }

                BlockPos otherPos = state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
                BlockState otherState = world.getBlockState(otherPos);

                BlockState newState = state.with(HermeticDoorBlock.SEALED, false).with(DoorBlock.OPEN, true);
                world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
                if (otherState.isOf(state.getBlock())) {
                    world.setBlockState(otherPos, otherState.with(HermeticDoorBlock.SEALED, false).with(DoorBlock.OPEN, true), Block.NOTIFY_LISTENERS);
                }

                world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.2f, 0.5f);
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.7f, 1.5f);
                if (player != null) {
                    player.sendMessage(Text.literal("§6[Тяжёлый лом]: Гермозатвор с треском сорван с упоров!"), true);
                    context.getStack().damage(1, player, EquipmentSlot.MAINHAND);
                }
            }
            return ActionResult.SUCCESS;
        }

        return super.useOnBlock(context);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.NETHERITE_SWORD;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§8Тяжёлая монтировка-гвоздодёр ГОСТ-1405"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Усиленный кованый стальной лом Ликвидаторов."),
                Text.literal("§cУрон: 9.0 ед. дробящего урона"),
                Text.literal("§eПКМ по гермодвери: срывает задраенный вентиль"),
                Text.literal("§7Прочность: 450 ед.")
        )));
        return out;
    }
}
