package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Datagen provider for data-driven registry entries (e.g. enchantments).
 *
 * <p>Extends NeoForge's {@link DatapackBuiltinEntriesProvider} to generate JSON for all entries
 * added via {@link com.gto.registrylib.datagen.DataProviderInitializer#add}.
 */
public class RegistryLibDatapackProvider extends DatapackBuiltinEntriesProvider
                                         implements RegistryLibLookupFillerProvider {

    public RegistryLibDatapackProvider(
                                       RegistryCore owner,
                                       PackOutput output,
                                       CompletableFuture<HolderLookup.Provider> provider) {
        super(
                output,
                RegistryPatchGenerator.createLookup(
                        provider, owner.getDataGenInitializer().getDatapackRegistryProviders()),
                Set.of(owner.getModid()));
    }

    @Override
    public CompletableFuture<HolderLookup.Provider> getFilledProvider() {
        return getRegistryProvider();
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public String getName() {
        return "Datapack Registries";
    }
}
