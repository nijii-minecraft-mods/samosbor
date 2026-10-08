package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CassetteTapeItem extends Item implements PolymerItem {

    private static final String[] CASSETTE_RECORDS = new String[]{
            "§6[МК-60: «УВБ-76 / Жужжалка»]\n§e«Ж-Ж-Ж... МДЖБ... 76 812 ВАЛЕРА 90 12 44 02... СЕКТОР 7 НЕ ОТВЕЧАЕТ... ПРИЁМ...»",
            "§6[МК-60: «Рапорт Ликвидатора №14»]\n§c«Сектор 42 пробит, это не люди! Заваривайте переборки наглухо, бросайте нас, мы уже чувствуем туман!..»",
            "§6[МК-60: «Памятка Гражданской Обороны»]\n§e«Внимание всем жильцам. При запахе озона и сырого мяса перекройте вентиляцию и не верьте крикам родных из коридора...»",
            "§6[МК-60: «Диспетчер Метростроя»]\n§7«Служебная дрезина №11 не вернулась из нижнего горизонта. На рельсах обнаружена слизь. Движение остановлено.»",
            "§6[МК-60: «Архив Парткома: Протокол №88»]\n§d«Считать любые слухи о бесконечности Гигахруща вражеской провокацией. Норма белкового концентрата снижена на 15%.»"
    };

    public CassetteTapeItem() {
        super(new Item.Settings().maxCount(8));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.9f, 0.8f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 1.0f, 0.5f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.PLAYERS, 0.5f, 1.8f);

            int index = world.random.nextInt(CASSETTE_RECORDS.length);
            String transcript = CASSETTE_RECORDS[index];

            user.sendMessage(Text.literal("§8----------------------------------------"), false);
            user.sendMessage(Text.literal("§7[Воспроизведение ленты МК-60-2... Шуршание магнитофона]"), false);
            user.sendMessage(Text.literal(transcript), false);
            user.sendMessage(Text.literal("§8----------------------------------------"), false);
            user.sendMessage(Text.literal("§6▶ Воспроизведение записи МК-60"), true);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.MUSIC_DISC_11;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6Магнитная кассета МК-60"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Советская аудиокассета МК-60 с магнитной лентой."),
                Text.literal("§eПКМ: §fвоспроизвести секретную аудиозапись."),
                Text.literal("§8«Свема / Тасма — Сделано в СССР»")
        )));
        return out;
    }
}
