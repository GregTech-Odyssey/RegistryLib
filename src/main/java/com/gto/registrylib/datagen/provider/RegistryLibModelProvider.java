package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.generator.RegistryLibBlockModelGenerator;
import com.gto.registrylib.datagen.generator.RegistryLibItemModelGenerator;

import com.google.gson.JsonElement;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 1.21.1 的 {@link net.minecraft.data.models.ModelProvider} 没有可覆写的 registerModels 钩子，并且会对所有已注册
 * 方块做「必须有 blockstate」的严格校验——这不符合库式模组（只给自己的方块生成数据）的需求。因此这里实现为独立的 {@link DataProvider}，仅驱动 {@link
 * RegistryLibBlockModelGenerator} 与 {@link RegistryLibItemModelGenerator}。
 */
public class RegistryLibModelProvider implements DataProvider, RegistryLibProvider {

    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider modelPathProvider;
    private final RegistryCore parent;

    public RegistryLibModelProvider(RegistryCore parent, PackOutput packOutput) {
        this.blockStatePathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.modelPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        this.parent = parent;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<Block, BlockStateGenerator> blockStates = new HashMap<>();
        Map<ResourceLocation, Supplier<JsonElement>> models = new HashMap<>();

        Consumer<BlockStateGenerator> blockStateOutput = generator -> {
            Block block = generator.getBlock();
            BlockStateGenerator previous = blockStates.put(block, generator);
            if (previous != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + block);
            }
        };
        BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput = (loc, json) -> {
            Supplier<JsonElement> previous = models.put(loc, json);
            if (previous != null) {
                throw new IllegalStateException("Duplicate model definition for " + loc);
            }
        };

        new RegistryLibBlockModelGenerator(parent, blockStateOutput, modelOutput).run();
        new RegistryLibItemModelGenerator(parent, modelOutput).run();

        return CompletableFuture.allOf(
                saveCollection(
                        cache,
                        blockStates,
                        block -> blockStatePathProvider.json(block.builtInRegistryHolder().key().location())),
                saveCollection(cache, models, modelPathProvider::json));
    }

    private <T> CompletableFuture<?> saveCollection(
                                                    CachedOutput cache,
                                                    Map<T, ? extends Supplier<JsonElement>> values,
                                                    Function<T, Path> pathFunction) {
        return CompletableFuture.allOf(
                values.entrySet().stream()
                        .map(
                                entry -> DataProvider.saveStable(
                                        cache, entry.getValue().get(), pathFunction.apply(entry.getKey())))
                        .toArray(CompletableFuture[]::new));
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public String getName() {
        return "RegistryLib Model Provider for " + parent.getModid();
    }
}
