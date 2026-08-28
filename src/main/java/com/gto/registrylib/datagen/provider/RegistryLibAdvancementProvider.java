package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.ProviderType;

import com.google.common.collect.Lists;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public class RegistryLibAdvancementProvider
                                            implements RegistryLibProvider, Consumer<AdvancementHolder> {

    private final RegistryCore owner;
    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;
    private final List<CompletableFuture<?>> advancementsToSave = Lists.newArrayList();
    private HolderLookup.Provider provider;

    public RegistryLibAdvancementProvider(
                                          RegistryCore owner,
                                          PackOutput packOutputIn,
                                          CompletableFuture<HolderLookup.Provider> registriesLookupIn) {
        this.owner = owner;
        this.packOutput = packOutputIn;
        this.registriesLookup = registriesLookupIn;
    }

    public HolderLookup.Provider getProvider() {
        return provider;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    public MutableComponent title(String category, String name, String title) {
        return owner.addLang(
                "advancements", ResourceLocation.fromNamespaceAndPath(category, name), "title", title);
    }

    public MutableComponent desc(String category, String name, String desc) {
        return owner.addLang(
                "advancements", ResourceLocation.fromNamespaceAndPath(category, name), "description", desc);
    }

    private @Nullable CachedOutput cache;
    private Set<ResourceLocation> seenAdvancements = new HashSet<>();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registriesLookup.thenCompose(
                lookup -> {
                    this.provider = lookup;
                    advancementsToSave.clear();

                    try {
                        this.cache = cache;
                        this.seenAdvancements.clear();
                        owner.genData(ProviderType.ADVANCEMENT, this);
                    } finally {
                        this.cache = null;
                    }

                    return CompletableFuture.allOf(advancementsToSave.toArray(CompletableFuture[]::new));
                });
    }

    @Override
    public void accept(@Nullable AdvancementHolder holder) {
        withConditions(holder, List.of());
    }

    public void withConditions(@Nullable AdvancementHolder holder, List<ICondition> conditions) {
        this.registriesLookup.thenAccept(
                lookup -> {
                    CachedOutput cache = this.cache;
                    if (cache == null) {
                        throw new IllegalStateException("Cannot accept advancements outside of act");
                    }
                    Objects.requireNonNull(holder, "Cannot accept a null advancement");
                    Path path = getPath(this.packOutput.getOutputFolder(), holder);
                    if (!seenAdvancements.add(holder.id())) {
                        throw new IllegalStateException("Duplicate advancement " + holder.id());
                    } else if (conditions.isEmpty()) {
                        advancementsToSave.add(
                                DataProvider.saveStable(cache, lookup, Advancement.CODEC, holder.value(), path));
                    } else {
                        advancementsToSave.add(
                                DataProvider.saveStable(
                                        cache,
                                        lookup,
                                        Advancement.CONDITIONAL_CODEC,
                                        Optional.of(new WithConditions<>(conditions, holder.value())),
                                        path));
                    }
                });
    }

    private static Path getPath(Path pathIn, AdvancementHolder advancementIn) {
        return pathIn.resolve(
                "data/" + advancementIn.id().getNamespace() + "/advancement/" + advancementIn.id().getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Advancements";
    }
}
