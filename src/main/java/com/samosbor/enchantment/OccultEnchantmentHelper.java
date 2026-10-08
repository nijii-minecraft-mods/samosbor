package com.samosbor.enchantment;

import com.samosbor.item.ModItems;
import com.samosbor.util.RomanNumeralUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class OccultEnchantmentHelper {

    public static final String RIFT_BREACH = "rift_breach";
    public static final String BIOMASS_CONTAGION = "biomass_contagion";
    public static final String CONCRETE_TRANSMUTATION = "concrete_transmutation";
    public static final String BORSHCHEVIK_POISON = "borshchevik_poison";

    private static final String OCCULT_TAG = "SamosborOccult";

    public static int getLevel(ItemStack stack, String occultId) {
        if (stack == null || stack.isEmpty()) return 0;
        NbtComponent nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComp == null) return 0;
        NbtCompound nbt = nbtComp.copyNbt();
        if (!nbt.contains(OCCULT_TAG)) return 0;
        NbtCompound occultNbt = nbt.getCompound(OCCULT_TAG);
        return occultNbt.getInt(occultId);
    }

    public static boolean has(ItemStack stack, String occultId) {
        return getLevel(stack, occultId) > 0;
    }

    public static void applyOccultEnchantment(ItemStack stack, String occultId, int level) {
        if (stack == null || stack.isEmpty()) return;

        // 1. Update NBT
        NbtComponent nbtComp = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = nbtComp.copyNbt();
        NbtCompound occultNbt = nbt.contains(OCCULT_TAG) ? nbt.getCompound(OCCULT_TAG) : new NbtCompound();
        occultNbt.putInt(occultId, level);
        nbt.put(OCCULT_TAG, occultNbt);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        // 2. Set glint override so item shimmers
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        // 3. Append to lore
        LoreComponent existingLore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
        List<Text> loreList = new ArrayList<>(existingLore.lines());

        String display = getDisplayName(occultId, level);
        // Remove old level line for this enchantment if already present
        loreList.removeIf(text -> text.getString().contains(getRawName(occultId)));
        loreList.add(Text.literal("§5§l" + display));

        stack.set(DataComponentTypes.LORE, new LoreComponent(loreList));
    }

    public static boolean canApply(String occultId, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();

        return switch (occultId) {
            case RIFT_BREACH -> item instanceof HoeItem || item instanceof ShovelItem || item == ModItems.SAPPER_SHOVEL;
            case BIOMASS_CONTAGION -> item instanceof SwordItem || item instanceof AxeItem || item == ModItems.RITUAL_SICKLE || item == ModItems.CROWBAR || item == ModItems.HEAVY_CROWBAR;
            case CONCRETE_TRANSMUTATION -> item instanceof PickaxeItem || item == ModItems.HEAVY_CROWBAR;
            case BORSHCHEVIK_POISON -> item instanceof SwordItem || item instanceof AxeItem || item instanceof RangedWeaponItem || item == ModItems.RITUAL_SICKLE;
            default -> false;
        };
    }

    public static String getRawName(String occultId) {
        return switch (occultId) {
            case RIFT_BREACH -> "Разлом Реальности";
            case BIOMASS_CONTAGION -> "Заражение Биомассой";
            case CONCRETE_TRANSMUTATION -> "Бетоноворот";
            case BORSHCHEVIK_POISON -> "Яд Борщевика";
            default -> "Оккультная Аномалия";
        };
    }

    public static String getDisplayName(String occultId, int level) {
        return getRawName(occultId) + " " + RomanNumeralUtil.toRoman(level);
    }

    public static String getDescription(String occultId) {
        return switch (occultId) {
            case RIFT_BREACH -> "§dВскапывание земли пробивает пространственный разлом в Гигахрущ (или возвращает в Верхний мир).";
            case BIOMASS_CONTAGION -> "§5Вампиризм и заражение хтонической биомассой. Поверженные враги детонируют спорами.";
            case CONCRETE_TRANSMUTATION -> "§7Добыча камня даёт панели П-46. ПКМ+Shift возводит бетонный монолитный заслон 3x3.";
            case BORSHCHEVIK_POISON -> "§2Фототоксический яд: в дневном свете или ярких лампах цель вспыхивает пламенем душ.";
            default -> "";
        };
    }
}
