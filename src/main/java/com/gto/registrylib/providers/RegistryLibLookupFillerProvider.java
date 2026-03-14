package com.gto.registrylib.providers;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;

public interface RegistryLibLookupFillerProvider extends RegistryLibProvider {

  CompletableFuture<HolderLookup.Provider> getFilledProvider();
}
