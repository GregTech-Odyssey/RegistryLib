package com.gto.registrylib.worldgen;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class WorldgenFeatureBuilder<C extends FeatureConfiguration, P> {

    private final RegistryCore core;
    private final P parent;
    private final String name;
    private final Supplier<ConfiguredFeature<C, ?>> configuredFeature;
    private final List<PlacementModifier> placements = new ArrayList<>();
    private TagKey<Biome> biomeTag;
    private GenerationStep.Decoration decoration = GenerationStep.Decoration.VEGETAL_DECORATION;
    private boolean registered;

    public WorldgenFeatureBuilder(
                                  RegistryCore core, P parent, String name, ConfiguredFeature<C, ?> configuredFeature) {
        this(core, parent, name, () -> configuredFeature);
    }

    public WorldgenFeatureBuilder(
                                  RegistryCore core,
                                  P parent,
                                  String name,
                                  Supplier<ConfiguredFeature<C, ?>> configuredFeature) {
        this.core = core;
        this.parent = parent;
        this.name = name;
        this.configuredFeature = configuredFeature;
    }

    public static <C extends FeatureConfiguration, P> WorldgenFeatureBuilder<C, P> create(
                                                                                          RegistryCore core, P parent, String name, ConfiguredFeature<C, ?> configuredFeature) {
        return new WorldgenFeatureBuilder<>(core, parent, name, configuredFeature);
    }

    public static <C extends FeatureConfiguration, P> WorldgenFeatureBuilder<C, P> create(
                                                                                          RegistryCore core,
                                                                                          P parent,
                                                                                          String name,
                                                                                          Supplier<ConfiguredFeature<C, ?>> configuredFeature) {
        return new WorldgenFeatureBuilder<>(core, parent, name, configuredFeature);
    }

    @StandardAPI
    public WorldgenFeatureBuilder<C, P> placement(PlacementModifier modifier) {
        placements.add(modifier);
        return this;
    }

    @StandardAPI
    public WorldgenFeatureBuilder<C, P> placements(List<PlacementModifier> modifiers) {
        placements.addAll(modifiers);
        return this;
    }

    @StandardAPI
    public WorldgenFeatureBuilder<C, P> addToBiomes(
                                                    TagKey<Biome> biomeTag, GenerationStep.Decoration decoration) {
        this.biomeTag = biomeTag;
        this.decoration = decoration;
        return this;
    }

    @StandardAPI
    public WorldgenFeatureEntry register() {
        if (registered) {
            throw new IllegalStateException("Builder already registered: " + name);
        }
        registered = true;
        ResourceKey<ConfiguredFeature<?, ?>> configuredKey = ResourceKey.create(
                Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(core.getModid(), name));
        ResourceKey<PlacedFeature> placedKey = ResourceKey.create(
                Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(core.getModid(), name));
        core.getDataGenInitializer()
                .add(
                        Registries.CONFIGURED_FEATURE,
                        ctx -> ctx.register(configuredKey, configuredFeature.get()));
        core.getDataGenInitializer()
                .add(
                        Registries.PLACED_FEATURE,
                        ctx -> ctx.register(
                                placedKey,
                                new PlacedFeature(
                                        ctx.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(configuredKey),
                                        List.copyOf(placements))));
        if (biomeTag != null) {
            ResourceKey<net.neoforged.neoforge.common.world.BiomeModifier> modifierKey = ResourceKey.create(
                    NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    Identifier.fromNamespaceAndPath(core.getModid(), name + "_add_feature"));
            core.getDataGenInitializer()
                    .add(
                            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                            ctx -> ctx.register(
                                    modifierKey,
                                    new BiomeModifiers.AddFeaturesBiomeModifier(
                                            ctx.lookup(Registries.BIOME).getOrThrow(biomeTag),
                                            HolderSet.direct(
                                                    ctx.lookup(Registries.PLACED_FEATURE).getOrThrow(placedKey)),
                                            decoration)));
        }
        return new WorldgenFeatureEntry(configuredKey, placedKey);
    }

    @StandardAPI
    public P build() {
        register();
        return parent;
    }
}
