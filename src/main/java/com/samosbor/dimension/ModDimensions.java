package com.samosbor.dimension;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class ModDimensions {
    public static final String MOD_ID = "samosbor";

    public static final RegistryKey<DimensionType> GIGAKHRUSHCHYOVKA_TYPE_KEY = RegistryKey.of(
            RegistryKeys.DIMENSION_TYPE,
            Identifier.of(MOD_ID, "gigakhrushchyovka")
    );

    public static final RegistryKey<World> GIGAKHRUSHCHYOVKA_KEY = RegistryKey.of(
            RegistryKeys.WORLD,
            Identifier.of(MOD_ID, "gigakhrushchyovka")
    );

    public static void initialize() {
        GigakhrushchChunkGenerator.register();
    }
}
