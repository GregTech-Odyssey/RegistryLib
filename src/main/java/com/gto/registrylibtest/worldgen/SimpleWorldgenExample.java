package com.gto.registrylibtest.worldgen;

import com.gto.registrylib.worldgen.WorldgenFeatureEntry;
import com.gto.registrylibtest.RegistryLibTest;
import com.gto.registrylibtest.block.SimpleBlockExample;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;

public final class SimpleWorldgenExample {

    public static final WorldgenFeatureEntry ESSENCE_NODE_PATCH = RegistryLibTest.REGISTRYLIB
            .worldgenFeature(
                    "essence_node_patch",
                    () -> new ConfiguredFeature<>(
                            Feature.SIMPLE_BLOCK,
                            new SimpleBlockConfiguration(
                                    BlockStateProvider.simple(SimpleBlockExample.DECORATIVE_STONE.get()))))
            .placement(CountPlacement.of(1))
            .placement(InSquarePlacement.spread())
            .placement(HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG))
            .placement(
                    BlockPredicateFilter.forPredicate(
                            BlockPredicate.allOf(
                                    BlockPredicate.replaceable(),
                                    BlockPredicate.noFluid(),
                                    BlockPredicate.matchesBlocks(BlockPos.ZERO.below(), Blocks.GRASS_BLOCK))))
            .placement(BiomeFilter.biome())
            .addToBiomes(
                    BiomeTags.IS_OVERWORLD,
                    net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION)
            .register();

    private SimpleWorldgenExample() {}
}
