package com.samosbor.item;

import com.samosbor.enchantment.OccultEnchantmentHelper;
import com.samosbor.util.RomanNumeralUtil;
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

public class OccultTomeItem extends Item implements PolymerItem {

    public enum TomeType {
        RIFT_BREACH(OccultEnchantmentHelper.RIFT_BREACH, "Гримуар Прорыва в Гигахрущ", 3, "§dВскапывание земли пробивает разлом между мирами"),
        BIOMASS_CONTAGION(OccultEnchantmentHelper.BIOMASS_CONTAGION, "Фолиант Чёрной Биомассы", 3, "§5Заражает врагов биомассой, вытягивая жизнь"),
        CONCRETE_TRANSMUTATION(OccultEnchantmentHelper.CONCRETE_TRANSMUTATION, "Свиток Бетоноворота", 3, "§7Трансмутирует камень в панели П-46 и возводит заслон 3x3"),
        BORSHCHEVIK_POISON(OccultEnchantmentHelper.BORSHCHEVIK_POISON, "Трактат Секты Борщевода", 3, "§2Фототоксичный яд борщевика: вспышка пламени душ на свету"),
        PARTY_SHARPNESS("sharpness", "Секретная Директива: Острота XIII", 13, "§cПартийный указ НИИ-7: колоссальный урон оружием"),
        PARTY_LOOTING("looting", "Ордер Спецраспределения: Добыча X", 10, "§eПривилегия высшей номенклатуры: избыток трофеев"),
        PARTY_EFFICIENCY("efficiency", "Стахановский План: Эффективность X", 10, "§bПеревыполнение норм выработки: мгновенная добыча блоков"),
        PARTY_UNBREAKING("unbreaking", "Сталинский Монолит: Прочность X", 10, "§fМонолитная закалка арматуры: вечный ресурс инструмента"),
        PARTY_PROTECTION("protection", "Бронепанель П-46: Защита X", 10, "§9Военно-инженерная бронезащита высшей категории");

        public final String id;
        public final String title;
        public final int level;
        public final String description;

        TomeType(String id, String title, int level, String description) {
            this.id = id;
            this.title = title;
            this.level = level;
            this.description = description;
        }

        public boolean isVanillaEnchantment() {
            return this.name().startsWith("PARTY_");
        }
    }

    private final TomeType tomeType;

    public OccultTomeItem(TomeType tomeType) {
        super(new Item.Settings().maxCount(16));
        this.tomeType = tomeType;
    }

    public TomeType getTomeType() {
        return tomeType;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    tomeType.isVanillaEnchantment() ? SoundEvents.ITEM_BOOK_PAGE_TURN : SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                    SoundCategory.PLAYERS, 0.9f, 1.0f);

            user.sendMessage(Text.literal("§6[" + tomeType.title + "]: " + tomeType.description), true);
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.ENCHANTED_BOOK;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6§l" + tomeType.title));
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        String category = tomeType.isVanillaEnchantment()
                ? "§4[Секретная Документация Парткома ГО]"
                : "§5[Древний Оккультный Гримуар Гигахруща]";

        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal(category),
                Text.literal(tomeType.description),
                Text.literal("§eУровень: §f" + RomanNumeralUtil.toRoman(tomeType.level)),
                Text.literal("§7Примените на Оккультном Алтаре Биомассы для ритуала зачарования."),
                Text.literal("§8(Инструмент в правой руке, фолиант в левой)")
        )));
        return out;
    }
}
