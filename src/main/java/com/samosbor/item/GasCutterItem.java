package com.samosbor.item;

import com.samosbor.block.HermeticDoorBlock;
import com.samosbor.block.ModBlocks;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GasCutterItem extends Item implements PolymerItem {

    public GasCutterItem() {
        super(new Item.Settings().maxDamage(150));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        BlockState state = world.getBlockState(pos);

        // 1. Welding or cutting HermeticDoorBlock
        if (state.getBlock() instanceof HermeticDoorBlock) {
            boolean isWelded = state.get(HermeticDoorBlock.WELDED);
            BlockPos otherHalfPos = state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
            BlockState otherState = world.getBlockState(otherHalfPos);

            if (!isWelded) {
                // WELD SHUT
                if (player != null && !consumeAcetylene(player)) {
                    player.sendMessage(Text.literal("§c[РГ-3]: Требуется баллон ацетилена АЦ-5 для заваривания двери!"), true);
                    return ActionResult.FAIL;
                }

                BlockState newState = state.with(DoorBlock.OPEN, false).with(HermeticDoorBlock.SEALED, true).with(HermeticDoorBlock.WELDED, true);
                world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
                if (otherState.isOf(state.getBlock())) {
                    world.setBlockState(otherHalfPos, otherState.with(DoorBlock.OPEN, false).with(HermeticDoorBlock.SEALED, true).with(HermeticDoorBlock.WELDED, true), Block.NOTIFY_LISTENERS);
                }

                // Sparks and sounds
                serverWorld.spawnParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 20, 0.3, 0.5, 0.3, 0.1);
                serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.2, 0.4, 0.2, 0.05);

                world.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 1.0f, 0.6f);
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.9f, 0.8f);

                if (player != null) {
                    player.sendMessage(Text.literal("§6[Газовый резак РГ-3]: Швы заварены! Израсходован 1 баллон АЦ-5."), true);
                    stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
                    if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                        var adv = serverPlayer.server.getAdvancementLoader().get(net.minecraft.util.Identifier.of("samosbor", "weld_hermetic_door"));
                        if (adv != null) {
                            serverPlayer.getAdvancementTracker().grantCriterion(adv, "weld_door");
                        }
                    }
                }
                return ActionResult.SUCCESS;
            } else {
                // CUT OPEN
                if (player != null && !consumeAcetylene(player)) {
                    player.sendMessage(Text.literal("§c[РГ-3]: Требуется баллон ацетилена АЦ-5 для резки сварного шва!"), true);
                    return ActionResult.FAIL;
                }

                BlockState newState = state.with(HermeticDoorBlock.WELDED, false);
                world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
                if (otherState.isOf(state.getBlock())) {
                    world.setBlockState(otherHalfPos, otherState.with(HermeticDoorBlock.WELDED, false), Block.NOTIFY_LISTENERS);
                }

                serverWorld.spawnParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 25, 0.3, 0.5, 0.3, 0.15);
                serverWorld.spawnParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.2);

                world.playSound(null, pos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.BLOCKS, 1.0f, 0.8f);
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.8f, 0.9f);

                if (player != null) {
                    player.sendMessage(Text.literal("§e[Газовый резак РГ-3]: Сварной шов срезан! Израсходован 1 баллон АЦ-5."), true);
                    stack.damage(2, player, LivingEntity.getSlotForHand(context.getHand()));
                }
                return ActionResult.SUCCESS;
            }
        }

        // 2. Cutting Iron Bars (rebar / quarantine barriers)
        if (state.isOf(Blocks.IRON_BARS)) {
            world.breakBlock(pos, true, player);
            serverWorld.spawnParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.1);
            world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.7f, 1.4f);
            if (player != null) {
                stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
            return ActionResult.SUCCESS;
        }

        // 3. Incinerating Biomass
        if (state.isOf(ModBlocks.BIOMASS)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            serverWorld.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.08);
            serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.05);
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.BLOCKS, 0.8f, 1.0f);
            if (player != null) {
                stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private boolean consumeAcetylene(PlayerEntity player) {
        if (player.getAbilities().creativeMode) return true;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack invStack = player.getInventory().getStack(i);
            if (invStack.isOf(ModItems.ACETYLENE_CANISTER)) {
                invStack.decrement(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.GOLDEN_HOE;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6Газовый резак Ликвидатора РГ-3"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Инструмент экстренной герметизации."),
                Text.literal("§eПКМ по гермодвери: §fнаглухо заварить"),
                Text.literal("§eПКМ по заваренной двери: §fсрезать сварной шов"),
                Text.literal("§7Срезает арматуру и выжигает биомассу.")
        )));
        return out;
    }
}
