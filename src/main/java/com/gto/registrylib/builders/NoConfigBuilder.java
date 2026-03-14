package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryLib;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class NoConfigBuilder<R, T extends R, P>
    extends AbstractBuilder<R, T, P, NoConfigBuilder<R, T, P>> {

  private final Supplier<T> factory;

  public NoConfigBuilder(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      ResourceKey<? extends Registry<R>> registryType,
      Supplier<T> factory) {
    super(owner, parent, name, callback, registryType);
    this.factory = factory;
  }

  @Override
  protected T createEntry() {
    return factory.get();
  }
}
