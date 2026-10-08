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

    // Occult relics, cultist weapons & breach boss drops
    public static final Item BIOMASS_GRIMOIRE = register("biomass_grimoire", new BiomassGrimoireItem());
    public static final Item RITUAL_SICKLE = register("ritual_sickle", new RitualSickleItem());
    public static final Item CULT_TOTEM = register("cult_totem", new CultTotemItem());
    public static final Item BIOMASS_HEART = register("biomass_heart", new BiomassHeartItem());
    public static final Item VALERA_DOGTAG = register("valera_dogtag", new ValeraDogTagItem());
    public static final Item BORSHCHEVIK_EXTRACT = register("borshchevik_extract", new BorshchevikExtractItem());
    public static final Item ANOMALY_CORE = register("anomaly_core", new AnomalyCoreItem());
    public static final Item PROJECT_SODEYSTVIE = register("project_sodeystvie", new ProjectSodeystvieItem());

    // Soviet Anomalous Vials & Special Medical Ampoules
    public static final Item RIFT_SPLASH_POTION = register("rift_splash_potion", new RiftSplashPotionItem());
    public static final Item CS4_AEROSOL = register("cs4_aerosol", new Cs4AerosolItem());
    public static final Item SR11_STABILIZER = register("sr11_stabilizer", new Sr11StabilizerItem());
    public static final Item IM9_INHIBITOR = register("im9_inhibitor", new Im9InhibitorItem());
    public static final Item AJ11_FLUID = register("aj11_fluid", new Aj11FluidItem());

    // Samosbor Occult Enchantment Tomes (Tiers up to XXV)
    public static final Item OCCULT_TOME_RIFT = register("occult_tome_rift", new OccultTomeItem(OccultTomeItem.TomeType.RIFT_BREACH));
    public static final Item OCCULT_TOME_BIOMASS = register("occult_tome_biomass", new OccultTomeItem(OccultTomeItem.TomeType.BIOMASS_CONTAGION));
    public static final Item OCCULT_TOME_CONCRETE = register("occult_tome_concrete", new OccultTomeItem(OccultTomeItem.TomeType.CONCRETE_TRANSMUTATION));
    public static final Item OCCULT_TOME_BORSHCHEVIK = register("occult_tome_borshchevik", new OccultTomeItem(OccultTomeItem.TomeType.BORSHCHEVIK_POISON));
    public static final Item OCCULT_TOME_SPEAR = register("occult_tome_spear", new OccultTomeItem(OccultTomeItem.TomeType.PIERCING_SPEAR));
    public static final Item OCCULT_TOME_BARRIER = register("occult_tome_barrier", new OccultTomeItem(OccultTomeItem.TomeType.ENERGY_BARRIER));
    public static final Item OCCULT_TOME_PLASMA = register("occult_tome_plasma", new OccultTomeItem(OccultTomeItem.TomeType.PLASMA_PURGE));

    // Party Nomenklatura High-Level / Over-leveled Directives (Tiers up to XXV)
    public static final Item PARTY_TOME_SHARPNESS = register("party_tome_sharpness", new OccultTomeItem(OccultTomeItem.TomeType.PARTY_SHARPNESS));
    public static final Item PARTY_TOME_LOOTING = register("party_tome_looting", new OccultTomeItem(OccultTomeItem.TomeType.PARTY_LOOTING));
    public static final Item PARTY_TOME_EFFICIENCY = register("party_tome_efficiency", new OccultTomeItem(OccultTomeItem.TomeType.PARTY_EFFICIENCY));
    public static final Item PARTY_TOME_UNBREAKING = register("party_tome_unbreaking", new OccultTomeItem(OccultTomeItem.TomeType.PARTY_UNBREAKING));
    public static final Item PARTY_TOME_PROTECTION = register("party_tome_protection", new OccultTomeItem(OccultTomeItem.TomeType.PARTY_PROTECTION));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    public static void initialize() {
        // Called during mod initialization to load class
    }
}
