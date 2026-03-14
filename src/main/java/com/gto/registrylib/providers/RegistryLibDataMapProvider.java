package com.gto.registrylib.providers;

import com.gto.registrylib.RegistryLib;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.DataMapProvider;

public class RegistryLibDataMapProvider extends DataMapProvider implements RegistryLibProvider {

  private final RegistryLib parent;

  @Nullable private HolderLookup.Provider provider;

  protected RegistryLibDataMapProvider(
      RegistryLib parent, PackOutput output, CompletableFuture<HolderLookup.Provider> pvd) {
    super(output, pvd);
    this.parent = parent;
  }

  @Override
  public LogicalSide getSide() {
    return LogicalSide.SERVER;
  }

  @Override
  protected void gather(HolderLookup.Provider provider) {
    this.provider = provider;
    parent.genData(ProviderType.DATA_MAP, this);
    this.provider = null;
  }

  public HolderLookup.Provider getProvider() {
    if (provider == null)
      throw new IllegalStateException("Holder Lookup Provider is not available now");
    return provider;
  }
}
