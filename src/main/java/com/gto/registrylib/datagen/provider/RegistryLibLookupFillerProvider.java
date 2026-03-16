package com.gto.registrylib.datagen.provider;

import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public interface RegistryLibLookupFillerProvider extends RegistryLibProvider {

    CompletableFuture<HolderLookup.Provider> getFilledProvider();
}
