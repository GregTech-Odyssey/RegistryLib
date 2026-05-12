package com.gto.registrylib.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record WorldgenFeatureEntry(
                                   ResourceKey<ConfiguredFeature<?, ?>> configuredKey,
                                   ResourceKey<PlacedFeature> placedKey) {}
