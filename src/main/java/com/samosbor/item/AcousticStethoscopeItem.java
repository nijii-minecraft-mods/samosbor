package com.samosbor.item;

import com.samosbor.block.ModBlocks;
import com.samosbor.event.SamosborManager;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AcousticStethoscopeItem extends Item implements PolymerItem {

    public AcousticStethoscopeItem() {
        super(new Item.Settings().maxDamage(100));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos clickedPos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;

        // Scan 6-block radius around the target block
        boolean biomassNearby = false;
        boolean hostilesNearby = false;
        boolean machineryNearby = false;

        for (int dx = -6; dx <= 6; dx += 2) {
            for (int dy = -4; dy <= 4; dy += 2) {
                for (int dz = -6; dz <= 6; dz += 2) {
                    BlockPos p = clickedPos.add(dx, dy, dz);
                    BlockState bs = world.getBlockState(p);
                    if (bs.isOf(ModBlocks.BIOMASS) || bs.isOf(ModBlocks.TOXIC_MUSHROOM)) {
                        biomassNearby = true;
                    }
                    if (bs.isOf(ModBlocks.ELEVATOR_STATION) || bs.isOf(ModBlocks.ELEVATOR_PANEL) || bs.isOf(net.minecraft.block.Blocks.PISTON)) {
                        machineryNearby = true;
                    }
                }
            }
        }

        Box scanBox = new Box(clickedPos).expand(6.0);
        List<MobEntity> mobs = serverWorld.getEntitiesByClass(MobEntity.class, scanBox, LivingEntity::isAlive);
        if (!mobs.isEmpty()) {
            hostilesNearby = true;
        }

        // Evaluate acoustic feedback
        if (biomassNearby || hostilesNearby) {
            world.playSound(null, clickedPos, SoundEvents.BLOCK_SLIME_BLOCK_STEP, SoundCategory.PLAYERS, 1.0f, 0.7f);
            world.playSound(null, clickedPos, SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 0.8f, 0.8f);
            if (player != null) {
                player.sendMessage(Text.literal("§4[СТ-4]: Слышно влажное хлюпанье и копошение биомассы за преградой..."), true);
                stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        } else if (SamosborManager.isSamosborActive()) {
            world.playSound(null, clickedPos, SoundEvents.AMBIENT_CAVE.value(), SoundCategory.PLAYERS, 1.0f, 0.5f);
            world.playSound(null, clickedPos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 0.7f, 0.4f);
            if (player != null) {
                player.sendMessage(Text.literal("§c[СТ-4]: Слышен нарастающий инфразвуковой гул мегаструктуры!"), true);
                stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        } else if (machineryNearby) {
            world.playSound(null, clickedPos, SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.PLAYERS, 0.9f, 0.9f);
            world.playSound(null, clickedPos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.PLAYERS, 0.7f, 0.6f);
            if (player != null) {
                player.sendMessage(Text.literal("§7[СТ-4]: Слышен натяг стальных тросов и ритмичная вибрация шахты."), true);
                stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        } else {
            world.playSound(null, clickedPos, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.PLAYERS, 0.6f, 1.8f);
            if (player != null) {
                player.sendMessage(Text.literal("§a[СТ-4]: За преградой глухая тишина."), true);
                stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }
        if (player instanceof ServerPlayerEntity serverPlayer) {
            var adv = serverPlayer.server.getAdvancementLoader().get(net.minecraft.util.Identifier.of("samosbor", "listen_walls"));
            if (adv != null) {
                serverPlayer.getAdvancementTracker().grantCriterion(adv, "used_stethoscope");
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.SPYGLASS;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§bСлуховая трубка СТ-4"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Латунный акустический стетоскоп Ликвидаторов."),
                Text.literal("§eПКМ по стене/двери: §fпрослушать звуки за преградой."),
                Text.literal("§7Позволяет обнаружить гнездо биомассы или приближение тумана.")
        )));
        return out;
    }
}
