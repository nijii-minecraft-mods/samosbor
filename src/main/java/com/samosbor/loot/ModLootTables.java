package com.samosbor.loot;

import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModLootTables {
    public static final String MOD_ID = "samosbor";

    public static final RegistryKey<LootTable> RESIDENTIAL_APARTMENT = register("chests/residential_apartment");
    public static final RegistryKey<LootTable> CIVIL_DEFENSE_DEPOT = register("chests/civil_defense_depot");
    public static final RegistryKey<LootTable> LIQUIDATOR_CACHE = register("chests/liquidator_cache");
    public static final RegistryKey<LootTable> INDUSTRIAL_WORKSHOP = register("chests/industrial_workshop");
    public static final RegistryKey<LootTable> PARTY_NOMENKLATURA = register("chests/party_nomenklatura");
    public static final RegistryKey<LootTable> MEDICAL_STATION = register("chests/medical_station");
    public static final RegistryKey<LootTable> FLOODED_REACTOR = register("chests/flooded_reactor");
    public static final RegistryKey<LootTable> MIMIC_NEST = register("chests/mimic_nest");

    private static RegistryKey<LootTable> register(String path) {
        return RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(MOD_ID, path));
    }
}
