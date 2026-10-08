package com.samosbor.item;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final String MOD_ID = "samosbor";

    // Survival rations & nutrition
    public static final Item FOOD_CONCENTRATE = register("food_concentrate", new FoodConcentrateItem());
    public static final Item RATION_TICKET = register("ration_ticket", new SimplePolymerItem(new Item.Settings(), Items.PAPER));
    public static final Item CANNED_STEW = register("canned_stew", new CannedStewItem());
    public static final Item FREEZE_DRIED_BORSCHT = register("freeze_dried_borscht", new FreezeDriedBorschtItem());
    public static final Item COOKED_BORSCHT = register("cooked_borscht", new CookedBorschtItem());
    public static final Item HARDTACK = register("hardtack", new HardtackItem());
    public static final Item MUSHROOM_MOONSHINE = register("mushroom_moonshine", new MushroomMoonshineItem());

    // Civil defense medicine, radiation & NBC protection
    public static final Item AI2_MEDKIT = register("ai2_medkit", new Ai2MedkitItem());
    public static final Item ANTIRAD = register("antirad", new AntiradItem());
    public static final Item BANDAGE = register("bandage", new BandageItem());
    public static final Item GAS_MASK = register("gas_mask", new GasMaskItem());
    public static final Item GAS_MASK_FILTER = register("gas_mask_filter", new GasMaskFilterItem());
    public static final Item L1M_SUIT = register("l1m_suit", new L1mHazmatItem());
    public static final Item DOSIMETER = register("dosimeter", new DosimeterItem());

    // Engineering tools & utility
    public static final Item BLUE_TAPE = register("blue_tape", new BlueTapeItem());
    public static final Item CROWBAR = register("crowbar", new CrowbarItem());
    public static final Item HEAVY_CROWBAR = register("heavy_crowbar", new HeavyCrowbarItem());
    public static final Item SAPPER_SHOVEL = register("sapper_shovel", new SapperShovelItem());
    public static final Item GAS_CUTTER = register("gas_cutter", new GasCutterItem());
    public static final Item ACETYLENE_CANISTER = register("acetylene_canister", new AcetyleneCanisterItem());
    public static final Item JP11_CANISTER = register("jp11_canister", new Jp11CanisterItem());
    public static final Item ACOUSTIC_STETHOSCOPE = register("acoustic_stethoscope", new AcousticStethoscopeItem());

    // Weapons, lore archives & tactical gear
    public static final Item SIGNAL_FLARE = register("signal_flare", new SignalFlareItem());
    public static final Item FLARE_GUN = register("flare_gun", new FlareGunItem());
    public static final Item CHLORINE_GRENADE = register("chlorine_grenade", new ChlorineGrenadeItem());
    public static final Item FLAMETHROWER = register("flamethrower", new FlamethrowerItem());
    public static final Item ROKS3_FLAMETHROWER = register("roks3_flamethrower", new Roks3FlamethrowerItem());
    public static final Item JP11_CARBINE = register("jp11_carbine", new Jp11CarbineItem());
    public static final Item JP11U_COMPONENT = register("jp11u_component", new SimplePolymerItem(new Item.Settings(), Items.HEAVY_CORE));
    public static final Item PARTY_DOSSIER = register("party_dossier", new PartyDossierItem());
    public static final Item CASSETTE_TAPE = register("cassette_tape", new CassetteTapeItem());

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    public static void initialize() {
        // Called during mod initialization to load class
    }
}
