package com.gto.registrylib.providers.loot;

import net.minecraft.core.WritableRegistry;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

public interface RegistryLibLootTables extends LootTableSubProvider {

    default void validate(
                          WritableRegistry<LootTable> writableRegistry, ValidationContextSource validationContext) {}
}
