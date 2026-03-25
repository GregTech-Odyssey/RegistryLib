package com.gto.registrylib.datagen.loot;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegistryLibEntityLootTables extends EntityLootSubProvider
                                         implements RegistryLibLootTables {

    private final RegistryCore parent;
    private final Consumer<RegistryLibEntityLootTables> callback;

    public RegistryLibEntityLootTables(
                                       HolderLookup.Provider provider,
                                       RegistryCore parent,
                                       Consumer<RegistryLibEntityLootTables> callback) {
        // allowed=allFlags (output loot for any entity), required=empty (don't crash for entities
        // without .loot())
        super(FeatureFlags.REGISTRY.allFlags(), FeatureFlagSet.of(), provider);
        this.parent = parent;
        this.callback = callback;
    }

    @Override
    public void generate() {
        callback.accept(this);
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return parent.getAll(Registries.ENTITY_TYPE).stream().map(Supplier::get);
    }

    @Override
    public void add(EntityType<?> type, LootTable.Builder builder) {
        super.add(type, builder);
    }
}
