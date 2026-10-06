package com.samosbor.item;

import com.samosbor.block.HermeticDoorBlock;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.EquipmentSlot;
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

public class CrowbarItem extends Item implements PolymerItem {

    public CrowbarItem() {
        super(new Item.Settings().maxDamage(150));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        if (state.getBlock() instanceof HermeticDoorBlock) {
            if (!world.isClient) {
                // Force unseal and open door
                BlockState newState = state.with(HermeticDoorBlock.SEALED, false).with(DoorBlock.OPEN, true);
                world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 0.5f);
                if (player != null) {
                    player.sendMessage(Text.literal("§7[Лом]: §cГермозатвор сорван монтировкой!"), true);
                    context.getStack().damage(2, player, EquipmentSlot.MAINHAND);
                }
            }
            return ActionResult.SUCCESS;
        }

        return super.useOnBlock(context);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.IRON_HOE;
    }
}
