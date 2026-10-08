package com.samosbor.block;

import com.samosbor.item.ModItems;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GarbageChuteBlock extends Block implements PolymerBlock {
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    public GarbageChuteBlock() {
        super(AbstractBlock.Settings.copy(Blocks.HOPPER).strength(3.0f, 6.0f));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            long now = world.getTime();
            Long last = COOLDOWNS.get(player.getUuid());
            if (last != null && (now - last) < 40) { // 2 second cooldown
                player.sendMessage(Text.literal("§7[Мусоропровод]: Ковш заблокирован, ожидайте завершения сброса..."), true);
                return ActionResult.SUCCESS;
            }

            ItemStack held = player.getMainHandStack();
            if (!held.isEmpty()) {
                COOLDOWNS.put(player.getUuid(), now);
                if (!player.getAbilities().creativeMode) {
                    held.decrement(1);
                }
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.7f);
                world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_FALL, SoundCategory.BLOCKS, 1.2f, 0.6f);
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.4f, 0.5f);

                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                    var adv = serverPlayer.server.getAdvancementLoader().get(net.minecraft.util.Identifier.of("samosbor", "garbage_chute_find"));
                    if (adv != null) {
                        serverPlayer.getAdvancementTracker().grantCriterion(adv, "used_chute");
                    }
                }

                // Small chance to scavenge a discarded ration ticket or component in the chute
                if (world.random.nextInt(10) == 0) {
                    ItemStack loot = world.random.nextBoolean() ? new ItemStack(ModItems.RATION_TICKET) : new ItemStack(Items.IRON_NUGGET);
                    if (!player.getInventory().insertStack(loot)) {
                        player.dropItem(loot, false);
                    }
                    player.sendMessage(Text.literal("§e[Мусоропровод]: В мусорном ковше что-то зазвенело... Вы нашли полезную вещь!"), true);
                } else {
                    player.sendMessage(Text.literal("§7[Мусоропровод]: §8*Гулкий лязг падающего мусора эхом унёсся в бездну шахты...*"), true);
                }
                return ActionResult.SUCCESS;
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.8f, 0.8f);
                player.sendMessage(Text.literal("§7[Мусоропровод]: Чугунный загрузочный ковш пуст. Бросьте предмет (ПКМ с предметом)."), true);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.HOPPER.getDefaultState();
    }
}
