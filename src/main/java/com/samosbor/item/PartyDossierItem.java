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

public class PartyDossierItem extends Item implements PolymerItem {

    private static final List<String> DOSSIERS = List.of(
            "§4[СОВЕРШЕННО СЕКРЕТНО] §6Протокол №14-О:\n§7«О герметизации 12-40 этажей в связи с прорывом органики. Ликвидаторам приказано заварить марши снаружи. Выживших жильцов считать статистической погрешностью пятилетки.»",
            "§4[СОВЕРШЕННО СЕКРЕТНО] §6Служебная записка:\n§7«Лифты на отметке Y=510 перестали подчиняться пультам. Натяжные канаты обросли серыми жилами. Кто-то дёргает за тросы из шахты снизу. Запретить спуск ниже 70 этажа.»",
            "§4[СОВЕРШЕННО СЕКРЕТНО] §6Акт сантехбригады №9:\n§7«Существо «Валера» в ячейке 14 переросло чугунную ванну. Углеродная масса достигла 4 метров. Валера ушёл по вытяжке в сторону парткома. Не включать вентиляцию!»",
            "§4[СОВЕРШЕННО СЕКРЕТНО] §6Директива Главка ГО:\n§7«Звук сирены оповещения — это не сигнал тревоги. Исследования НИИ показали, что сирена резонирует с биомассой. Сирена ПРИЗЫВАЕТ Самосбор. Выключить её невозможно.»",
            "§4[СОВЕРШЕННО СЕКРЕТНО] §6Рапорт штурм-взвода №3:\n§7«Огнесмесь РОКС-3 эффективна лишь временно. Обожжённая биомасса через 2 часа мутирует в мимиков, принимающих форму ящиков с консервами. Не трогать бесхозные сундуки!»",
            "§4[СОВЕРШЕННО СЕКРЕТНО] §6Памятка партработнику:\n§7«Разговоры об „улице“, „небе“ или „выходе наружу“ классифицируются как особо опасный психоз. Нарушители подлежат распылу спецраствором ЖП-11.»",
            "§4[СОВЕРШЕННО СЕКРЕТНО] §6Лабораторный отчёт НИИ-4:\n§7«Синтезирован пищевой концентрат нового поколения. 80% объёма составляет переработанная биомасса после термической очистки. Питательность в норме. Вкус мясной.»",
            "§4[СОВЕРШЕННО СЕКРЕТНО] §6Последняя запись коменданта:\n§7«Они заварили дверь с той стороны. Но стук идёт не из коридора. Стук идёт изнутри бетонной стены за ковром. Кто-то царапается в бетоне прямо сейчас...»"
    );

    public PartyDossierItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0f, 0.9f);
            String dossier = DOSSIERS.get(world.random.nextInt(DOSSIERS.size()));
            user.sendMessage(Text.literal(dossier), false);
            user.getItemCooldownManager().set(this, 20);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.BOOK;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, net.minecraft.item.tooltip.TooltipType tooltipType, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§4Секретная папка Парткома «ОСОБАЯ ПАПКА»"));
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Папка с грифом «ХРАНИТЬ ВЕЧНО. ПЕРЕД ПРОЧТЕНИЕМ СЖЕЧЬ»."),
                Text.literal("§eПКМ: читать засекреченные протоколы и директивы"),
                Text.literal("§cРаскрывает тайны Гигахруща и природы Самосбора")
        )));
        return out;
    }
}
