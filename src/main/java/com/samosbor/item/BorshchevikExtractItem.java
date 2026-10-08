package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BorshchevikExtractItem extends Item implements PolymerItem {

    public BorshchevikExtractItem() {
        super(new Item.Settings().maxCount(16));
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        super.finishUsing(stack, world, user);
        if (user instanceof ServerPlayerEntity serverPlayer) {
            Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));

            // Potent stim effects but agonizing side effects
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 1)); // Strength II (1 min)
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1200, 0)); // Resistance I (1 min)
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0)); // Nausea (15 sec)
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 160, 0)); // Poison I (8 sec)

            serverPlayer.sendMessage(Text.literal("§2[Вытяжка Борщевода]: §aЕдкий сок обжигает пищевод! Мышцы немеют от первобытной силы!"), true);
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (user instanceof PlayerEntity player && !player.isInCreativeMode()) {
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().insertStack(bottle)) {
                    player.dropItem(bottle, false);
                }
            }
            return stack;
        }
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.POTION;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§2§lВытяжка Мутировавшего Борщевика"));
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Кустарный концентрат сока староверов-борщеводов."),
                Text.literal("§aСила II и Сопротивление I на 60 сек"),
                Text.literal("§cПобочный эффект: тошнота и ожог пищевода"),
                Text.literal("§8«Канон: samosbor.shoutwiki.com/wiki/Борщевик»")
        )));
        return out;
    }
}
