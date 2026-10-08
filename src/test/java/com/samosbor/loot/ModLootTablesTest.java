package com.samosbor.loot;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModLootTablesTest {

    @Test
    public void testLootTableIdentifiers() {
        assertNotNull(ModLootTables.RESIDENTIAL_APARTMENT);
        assertNotNull(ModLootTables.CIVIL_DEFENSE_DEPOT);
        assertNotNull(ModLootTables.LIQUIDATOR_CACHE);
        assertNotNull(ModLootTables.INDUSTRIAL_WORKSHOP);
        assertNotNull(ModLootTables.PARTY_NOMENKLATURA);
        assertNotNull(ModLootTables.MEDICAL_STATION);
        assertNotNull(ModLootTables.FLOODED_REACTOR);
        assertNotNull(ModLootTables.MIMIC_NEST);
    }

    @Test
    public void testLootTableJsonFilesExistAndAreValid() {
        String[] tables = {
                "residential_apartment.json",
                "civil_defense_depot.json",
                "liquidator_cache.json",
                "industrial_workshop.json",
                "party_nomenklatura.json",
                "medical_station.json",
                "flooded_reactor.json",
                "mimic_nest.json"
        };

        File dir = new File("src/main/resources/data/samosbor/loot_table/chests");
        assertTrue(dir.exists() && dir.isDirectory(), "Loot tables directory must exist");

        for (String file : tables) {
            File jsonFile = new File(dir, file);
            assertTrue(jsonFile.exists(), "Loot table JSON file must exist: " + file);
            assertTrue(jsonFile.length() > 20, "Loot table JSON file must not be empty: " + file);
        }
    }

    @Test
    public void testCraftingRecipeFilesExist() {
        String[] recipes = {
                "bandage.json",
                "blue_tape.json",
                "gas_mask_filter.json",
                "signal_flare.json",
                "heavy_crowbar.json",
                "sapper_shovel.json",
                "chlorine_grenade.json",
                "mushroom_moonshine.json",
                "dosimeter.json",
                "jp11_canister.json"
        };

        File dir = new File("src/main/resources/data/samosbor/recipe");
        assertTrue(dir.exists() && dir.isDirectory(), "Recipe directory must exist");

        for (String file : recipes) {
            File jsonFile = new File(dir, file);
            assertTrue(jsonFile.exists(), "Recipe JSON file must exist: " + file);
            assertTrue(jsonFile.length() > 20, "Recipe JSON file must not be empty: " + file);
        }
    }
}
