package com.samosbor.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProjectSodeystvieItem extends Item implements PolymerItem {

    private static final String[] DOSSIER_EXCERPTS = {
            "§4[ГРИФ: ОСОБАЯ ВАЖНОСТЬ. ПРОЕКТ «СОДЕЙСТВИЕ-8»]\n§7«Объект 14-Б не поддаётся стандартной нейтрализации. Арматурный каркас серии П-46 вживлён непосредственно в мышечную массу. В случае резонанса — заварить перекрытия.»",
            "§4[ОТЧЁТ НИИ-7 ПО СЕКТОРУ К-2]\n§7«Реактор на магматическом ядре стабилен. Однако в контурах охлаждения обнаружены скопления псевдо-эмбрионов ПЭ-2. В вентиляцию подан спецраствор ЖП-11.»",
            "§4[ПРОТОКОЛ ИНКУБАТОРА Б-11]\n§7«Аквариумные цистерны повреждены при толчке. Суспензия АЖ-11 вытекла на нижние уровни. Замечена аберрация А-14. Назначить внеочередную санобработку.»",
            "§4[ДИРЕКТИВА ЦК ПАРТКОМА ГО]\n§7«Слухи о внешнем мире пресекать распылением. Стен у Гигахруща нет. Есть лишь этажи выше и этажи ниже. Слава Партии!»"
    };

    public ProjectSodeystvieItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            String excerpt = DOSSIER_EXCERPTS[world.random.nextInt(DOSSIER_EXCERPTS.length)];
            user.sendMessage(Text.literal(excerpt), false);
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0f, 0.9f);
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.WRITTEN_BOOK;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§4§lСпецпапка «Проект Содействие-8»"));
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§c[Секретный Архив Парткома ГО]"),
                Text.literal("§7Документы высшей государственной тайны о природе Гигахруща."),
                Text.literal("§eНажмите ПКМ, чтобы прочесть засекреченные материалы."),
                Text.literal("§8Экземпляр №0014-Б. Распространение карается завариванием.")
        )));
        return out;
    }
}
