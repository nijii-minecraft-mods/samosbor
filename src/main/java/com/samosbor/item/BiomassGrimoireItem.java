package com.samosbor.item;

import com.samosbor.block.ModBlocks;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BiomassGrimoireItem extends Item implements PolymerItem {

    private static final List<String> SCRIPTURES = List.of(
            "§d[Молитвенник Биомассы]: §5«Семя тумана падёт на бетон, и раскроются вены стен...»",
            "§d[Молитвенник Биомассы]: §5«Мы отвергли лица, мы отвергли имена. Осталась лишь тёплая плоть...»",
            "§d[Молитвенник Биомассы]: §5«Тот, кто скребётся за панелью — это брат твой. Впусти его...»",
            "§d[Молитвенник Биомассы]: §5«Огонь Ликвидатора бессилен перед вечностью деления клеток...»",
            "§d[Молитвенник Биомассы]: §5«Не крути вентиль, отвори затвор. Самосбор есть очищение...»"
    );

    public BiomassGrimoireItem() {
        super(new Item.Settings().maxCount(1).maxDamage(64));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            if (user.isSneaking()) {
                // Shift+Right Click: Blood sacrifice - summon biomass trap
                user.damage(serverWorld.getDamageSources().magic(), 4.0f);
                user.sendMessage(Text.literal("§4Вы окропили страницы кровью... Биомасса пробивает пол!"), true);

                BlockPos center = user.getBlockPos();
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        BlockPos p = center.add(dx, -1, dz);
                        if (serverWorld.getBlockState(p).isSolidBlock(serverWorld, p)
                                && !serverWorld.getBlockState(p).isOf(ModBlocks.BIOMASS)
                                && serverWorld.random.nextFloat() < 0.50f) {
                            serverWorld.setBlockState(p, ModBlocks.BIOMASS.getDefaultState());
                        }
                    }
                }

                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, user.getX(), user.getY() + 1, user.getZ(), 20, 1.0, 1.0, 1.0, 0.05);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.PLAYERS, 1.0f, 0.6f);
                stack.damage(2, user, LivingEntity.getSlotForHand(hand));
            } else {
                // Right Click: Pacify nearby hostiles at sanity cost
                String verse = SCRIPTURES.get(serverWorld.random.nextInt(SCRIPTURES.size()));
                user.sendMessage(Text.literal(verse), false);

                Box box = user.getBoundingBox().expand(14.0);
                List<HostileEntity> hostiles = serverWorld.getEntitiesByClass(HostileEntity.class, box, HostileEntity::isAlive);
                for (HostileEntity mob : hostiles) {
                    mob.setTarget(null);
                    mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1));
                }

                user.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 80, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));

                serverWorld.spawnParticles(ParticleTypes.WITCH, user.getX(), user.getY() + 1.2, user.getZ(), 15, 0.8, 0.5, 0.8, 0.02);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.2f, 0.7f);
                stack.damage(1, user, LivingEntity.getSlotForHand(hand));
                user.getItemCooldownManager().set(this, 120);
            }
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.WRITTEN_BOOK;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§5Молитвенник Великой Биомассы"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Переплетённый кожей фолиант Культа Биомассы."),
                Text.literal("§dПКМ: прошептать ересь (усмиряет монстров ценой рассудка)"),
                Text.literal("§cShift+ПКМ: пролить кровь (порождает биомассу под ногами)"),
                Text.literal("§8«Смерти нет. Есть лишь Великое Слияние.»")
        )));
        return out;
    }
}
