package com.gto.registrylib.datagen;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.provider.RegistryLibLookupFillerProvider;
import com.gto.registrylib.datagen.provider.RegistryLibProvider;
import com.gto.registrylib.datagen.provider.RegistryLibRecipeRunner;
import com.gto.registrylib.datagen.provider.RegistryLibTagsProvider;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class RegistryLibDataProvider implements DataProvider {

    private static final Logger LOGGER = LogUtils.getLogger();

    static final BiMap<String, ProviderType<?>> TYPES = HashBiMap.create();

    static final Map<ResourceKey<? extends Registry<?>>, ProviderType<?>> TAG_TYPES = new ConcurrentHashMap<>();

    public static @Nullable String getTypeName(GeneratorType<?> type) {
        if (type instanceof ProviderType<?> prov) return TYPES.inverse().get(prov);
        return type.toString();
    }

    private final String mod;
    private final Map<ProviderType<?>, RegistryLibProvider> subProviders = new LinkedHashMap<>();
    private final Map<GeneratorType<?>, Object> subGenerators = new LinkedHashMap<>();
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;

    public RegistryLibDataProvider(RegistryCore parent, String modid, GatherDataEvent event) {
        this.mod = modid;
        this.registriesLookup = event.getLookupProvider();

        LOGGER.debug("Gathering providers");
        Map<ProviderType<?>, RegistryLibProvider> known = new Reference2ReferenceOpenHashMap<>();
        for (DataProviderInitializer.Sorted sorted : parent.getDataGenInitializer().getSortedProviders()) {
            ProviderType<?> type = sorted.type();
            var lookup = registriesLookup;
            if (sorted.parent() != null)
                lookup = ((RegistryLibLookupFillerProvider) known.get(sorted.parent())).getFilledProvider();
            RegistryLibProvider prov = ProviderType.create(type, parent, event, known, lookup);
            if (prov instanceof RegistryLibTagsProvider<?> tagsProvider && TAG_TYPES.get(tagsProvider.registry()) != type) {
                throw new IllegalStateException(
                        "Tag providers must be registered through ProviderType::registerTag");
            }
            known.put(type, prov);
            LOGGER.debug("Adding provider for type: {}", sorted.id());
            subProviders.put(type, prov);
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registriesLookup.thenCompose(
                provider -> {
                    var list = Lists.<CompletableFuture<?>>newArrayList();

                    for (Map.Entry<ProviderType<?>, RegistryLibProvider> e : subProviders.entrySet()) {
                        LOGGER.debug("Generating data for type: {}", getTypeName(e.getKey()));
                        CompletableFuture<?> future = e.getValue().run(cache);
                        // Chain deferred recipe writes after the recipe runner completes
                        if (e.getValue() instanceof RegistryLibRecipeRunner recipeRunner) {
                            future = future.thenCompose(v -> recipeRunner.writeDeferredRecipes(cache));
                        }
                        list.add(future);
                    }

                    return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
                });
    }

    @Override
    public String getName() {
        return "RegistryCore Provider for " + mod + " [" + subProviders.values().stream()
                .map(DataProvider::getName)
                .collect(Collectors.joining(", ")) + "]";
    }

    @SuppressWarnings("unchecked")
    public <P> Optional<P> getSubProvider(GeneratorType<P> type) {
        if (type instanceof ProviderType<?> prov)
            return Optional.ofNullable((P) subProviders.get(prov));
        return Optional.ofNullable((P) subGenerators.get(type));
    }

    public <T> void putSubProvider(GeneratorType<? extends T> type, T gen) {
        subGenerators.put(type, gen);
    }
}
