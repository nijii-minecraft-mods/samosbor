package com.samosbor;

import com.samosbor.block.ModBlocks;
import com.samosbor.command.SamosborCommand;
import com.samosbor.dimension.ModDimensions;
import com.samosbor.event.SamosborManager;
import com.samosbor.item.ModItems;
import com.samosbor.worldgen.KhrushchyovkaFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamosborMod implements ModInitializer {
    public static final String MOD_ID = "samosbor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final RegistryKey<PlacedFeature> KHRUSHCHYOVKA_PLACED_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of(MOD_ID, "khrushchyovka")
    );

    @Override
    public void onInitialize() {
        LOGGER.info("Инициализация мода Самосбор (Samosbor) 1.21.1...");

        // Register content
        ModBlocks.initialize();
        ModItems.initialize();
        ModDimensions.initialize();
        KhrushchyovkaFeature.register();

        // Register Overworld Khrushchyovka structure generation (rare anomaly in deep forests & taiga)
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(
                        net.minecraft.world.biome.BiomeKeys.DARK_FOREST,
                        net.minecraft.world.biome.BiomeKeys.TAIGA,
                        net.minecraft.world.biome.BiomeKeys.OLD_GROWTH_PINE_TAIGA,
                        net.minecraft.world.biome.BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA,
                        net.minecraft.world.biome.BiomeKeys.FOREST,
                        net.minecraft.world.biome.BiomeKeys.BIRCH_FOREST,
                        net.minecraft.world.biome.BiomeKeys.OLD_GROWTH_BIRCH_FOREST,
                        net.minecraft.world.biome.BiomeKeys.PLAINS
                ),
                GenerationStep.Feature.SURFACE_STRUCTURES,
                KHRUSHCHYOVKA_PLACED_KEY
        );

        // Register commands
        CommandRegistrationCallback.EVENT.register(SamosborCommand::register);

        // Register world tick event for Samosbor cycle
        ServerTickEvents.END_WORLD_TICK.register(SamosborManager::tickWorld);

        // Register Occult Enchantment and Cross-Dimensional hooks
        com.samosbor.event.OccultEnchantmentHooks.register();

        LOGGER.info("Самосбор успешно загружен. Гигахрущёвка готова к заселению.");
    }
}
