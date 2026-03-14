package com.gto.registrylib.providers;

import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public interface RegistryLibLookupFillerProvider extends RegistryLibProvider {

    CompletableFuture<HolderLookup.Provider> getFilledProvider();
}
