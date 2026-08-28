package com.gto.registrylib.datagen.loot;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibProvider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.fml.LogicalSide;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.apache.commons.lang3.function.TriFunction;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RegistryLibLootTableProvider extends LootTableProvider implements RegistryLibProvider {

    public interface LootType<T extends RegistryLibLootTables> {

        LootType<RegistryLibBlockLootTables> BLOCK = register("block", LootContextParamSets.BLOCK, RegistryLibBlockLootTables::new);
        LootType<RegistryLibEntityLootTables> ENTITY = register("entity", LootContextParamSets.ENTITY, RegistryLibEntityLootTables::new);

        T getLootCreator(HolderLookup.Provider provider, RegistryCore parent, Consumer<T> callback);

        LootContextParamSet getLootSet();

        static <T extends RegistryLibLootTables> LootType<T> register(
                                                                      String name,
                                                                      LootContextParamSet set,
                                                                      TriFunction<HolderLookup.Provider, RegistryCore, Consumer<T>, T> factory) {
            LootType<T> type = new LootType<T>() {

                @Override
                public T getLootCreator(
                                        HolderLookup.Provider provider, RegistryCore parent, Consumer<T> callback) {
                    return factory.apply(provider, parent, callback);
                }

                @Override
                public LootContextParamSet getLootSet() {
                    return set;
                }
            };
            LOOT_TYPES.put(name, type);
            return type;
        }
    }

    private static final Map<String, LootType<?>> LOOT_TYPES = new HashMap<>();

    private final RegistryCore parent;

    @SuppressWarnings("rawtypes")
    private final Multimap<LootType<?>, Consumer<? super RegistryLibLootTables>> specialLootActions = HashMultimap.create();

    private final Multimap<LootContextParamSet, Consumer<BiConsumer<ResourceKey<LootTable>, LootTable.Builder>>> lootActions = HashMultimap.create();
    private final Set<RegistryLibLootTables> currentLootCreators = new ReferenceOpenHashSet<>();

    private final CompletableFuture<HolderLookup.Provider> providerFuture;

    public RegistryLibLootTableProvider(
                                        RegistryCore parent,
                                        PackOutput packOutput,
                                        CompletableFuture<HolderLookup.Provider> provider) {
        super(
                packOutput,
                Set.of(),
                VanillaLootTableProvider.create(packOutput, provider).getTables(),
                provider);
        this.parent = parent;
        this.providerFuture = provider;
    }

    public HolderLookup.Provider getProvider() {
        return providerFuture.getNow(null);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    @Override
    protected void validate(
                            WritableRegistry<LootTable> writableregistry,
                            ValidationContext validationcontext,
                            ProblemReporter.Collector collector) {
        currentLootCreators.forEach(c -> c.validate(writableregistry, validationcontext));
    }

    @SuppressWarnings("unchecked")
    public <T extends RegistryLibLootTables> void addLootAction(
                                                                LootType<T> type, Consumer<T> action) {
        this.specialLootActions.put(type, (Consumer<RegistryLibLootTables>) action);
    }

    public void addLootAction(
                              LootContextParamSet set,
                              Consumer<BiConsumer<ResourceKey<LootTable>, LootTable.Builder>> action) {
        this.lootActions.put(set, action);
    }

    private LootTableSubProvider getLootCreator(
                                                HolderLookup.Provider provider, RegistryCore parent, LootType<?> type) {
        RegistryLibLootTables creator = type.getLootCreator(
                provider, parent, cons -> specialLootActions.get(type).forEach(c -> c.accept(cons)));
        currentLootCreators.add(creator);
        return creator;
    }

    @Override
    public List<LootTableProvider.SubProviderEntry> getTables() {
        parent.genData(ProviderType.LOOT, this);
        currentLootCreators.clear();
        ImmutableList.Builder<LootTableProvider.SubProviderEntry> builder = ImmutableList.builder();
        for (LootType<?> type : LOOT_TYPES.values()) {
            builder.add(
                    new SubProviderEntry(
                            provider -> getLootCreator(provider, parent, type), type.getLootSet()));
        }
        return builder.build();
    }
}
