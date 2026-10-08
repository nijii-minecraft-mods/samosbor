package com.samosbor.item;

import com.samosbor.enchantment.OccultEnchantmentHelper;
import com.samosbor.util.RomanNumeralUtil;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
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
        RIFT_BREACH(OccultEnchantmentHelper.RIFT_BREACH, "Разлом Реальности РП", 25, "§dВскапывание земли пробивает разлом между мирами"),
        BIOMASS_CONTAGION(OccultEnchantmentHelper.BIOMASS_CONTAGION, "Био-агент Б", 25, "§5Заражает врагов биомассой, вытягивая жизнь"),
        CONCRETE_TRANSMUTATION(OccultEnchantmentHelper.CONCRETE_TRANSMUTATION, "Монолит М", 25, "§7Трансмутирует камень в панели П-46 и возводит заслон 3x3"),
        BORSHCHEVIK_POISON(OccultEnchantmentHelper.BORSHCHEVIK_POISON, "Фототоксин ФТ", 25, "§2Фототоксичный яд: вспышка пламени душ на свету"),
        PIERCING_SPEAR(OccultEnchantmentHelper.PIERCING_SPEAR, "Пробойная Пика ПК", 25, "§cИгнорирует броню и тики неуязвимости цели"),
        ENERGY_BARRIER(OccultEnchantmentHelper.ENERGY_BARRIER, "Энергобарьер ЭБ", 25, "§9Отражает снаряды с тройной скоростью и создаёт ударную волну"),
        PLASMA_PURGE(OccultEnchantmentHelper.PLASMA_PURGE, "Плазменный Прожиг ПЖ", 3, "§6Дальность огнесмеси до 12 блоков, плавит броню"),
        PARTY_SHARPNESS("sharpness", "Специзделие О-25: Острота", 25, "§cПартийная директива НИИ-7: колоссальный урон оружием"),
        PARTY_LOOTING("looting", "Специзделие Д-25: Добыча", 25, "§eОрдер Спецраспределения: избыток трофеев номенклатуры"),
        PARTY_EFFICIENCY("efficiency", "Стахановский План: Эффективность", 25, "§bСпецуказ Госплана: мгновенная выработка породы"),
        PARTY_UNBREAKING("unbreaking", "Специзделие П-25: Прочность", 25, "§fМонолитная закалка арматуры: несокрушимый ресурс"),
        PARTY_PROTECTION("protection", "Бронепанель П-46: Защита", 25, "§9Военно-инженерная бронезащита высшей категории");

        public final String id;
        public final String title;
        public final int maxLevel;
        public final String description;

        TomeType(String id, String title, int maxLevel, String description) {
            this.id = id;
            this.title = title;
            this.maxLevel = maxLevel;
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

    public int getLevel(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            NbtComponent nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (nbtComp != null) {
                NbtCompound nbt = nbtComp.copyNbt();
                if (nbt.contains("tome_level")) {
                    return Math.max(1, Math.min(nbt.getInt("tome_level"), tomeType.maxLevel));
                }
            }
        }
        return tomeType.maxLevel;
    }

    public String getId(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            NbtComponent nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (nbtComp != null) {
                NbtCompound nbt = nbtComp.copyNbt();
                if (nbt.contains("tome_id")) {
                    return nbt.getString("tome_id");
                }
            }
        }
        return tomeType.id;
    }

    public static ItemStack createStack(Item item, int level) {
        ItemStack stack = new ItemStack(item);
        if (item instanceof OccultTomeItem tome) {
            int clamped = Math.max(1, Math.min(level, tome.tomeType.maxLevel));
            NbtCompound nbt = new NbtCompound();
            nbt.putString("tome_id", tome.tomeType.id);
            nbt.putInt("tome_level", clamped);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        int lvl = getLevel(stack);
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    tomeType.isVanillaEnchantment() ? SoundEvents.ITEM_BOOK_PAGE_TURN : SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                    SoundCategory.PLAYERS, 0.9f, 1.0f);

            user.sendMessage(Text.literal("§6[" + tomeType.title + " " + RomanNumeralUtil.toRoman(lvl) + "]: " + tomeType.description), true);
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
        int lvl = getLevel(itemStack);
        out.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6§l" + tomeType.title + " " + RomanNumeralUtil.toRoman(lvl)));
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        String category = tomeType.isVanillaEnchantment()
                ? "§4[Спецдокументация Парткома ГО]"
                : "§5[Классифицированный Фолиант НИИ]";

        out.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal(category),
                Text.literal(tomeType.description),
                Text.literal(String.format("§eУровень допуска: §f%s §8(%d/%d)", RomanNumeralUtil.toRoman(lvl), lvl, tomeType.maxLevel)),
                Text.literal("§7Примените на Оккультном Алтаре Биомассы для ритуала зачарования."),
                Text.literal("§8(Инструмент в правой руке, фолиант в левой)")
        )));
        return out;
    }
}
