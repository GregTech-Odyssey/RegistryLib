package com.gto.registrylib.util.entry;

import java.util.function.Supplier;
import javax.annotation.Nullable;

public class LazyRegistryEntry<R, T extends R> implements Supplier<T> {

  @Nullable private Supplier<? extends RegistryEntry<R, T>> supplier;
  @Nullable private RegistryEntry<R, T> value;

  public LazyRegistryEntry(Supplier<? extends RegistryEntry<R, T>> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    Supplier<? extends RegistryEntry<R, T>> supplier = this.supplier;
    if (supplier != null) {
      this.value = supplier.get();
      this.supplier = null;
    }
    return this.value.get();
  }
}
