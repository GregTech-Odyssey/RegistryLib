package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryLib;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RegistryEntry<R, S extends R> extends DeferredHolder<R, S> implements Supplier<S> {
  private final RegistryLib owner;

  public RegistryEntry(RegistryLib owner, DeferredHolder<R, S> key) {
    super(key.getKey());

    if (owner == null) throw new NullPointerException("Owner must not be null");
    this.owner = owner;
  }

  public <X, Y extends X> RegistryEntry<X, Y> getSibling(
      ResourceKey<? extends Registry<X>> registryType) {
    return owner.get(getId().getPath(), registryType);
  }

  public <X, Y extends X> RegistryEntry<X, Y> getSibling(Registry<X> registry) {
    return getSibling(registry.key());
  }

  public Optional<RegistryEntry<R, S>> filter(Predicate<R> predicate) {
    Objects.requireNonNull(predicate);
    if (predicate.test(get())) {
      return Optional.of(this);
    }
    return Optional.empty();
  }

  public <X> boolean is(X entry) {
    return get() == entry;
  }

  @SuppressWarnings("unchecked")
  protected static <E extends RegistryEntry<?, ?>> E cast(
      Class<? super E> clazz, RegistryEntry<?, ?> entry) {
    if (clazz.isInstance(entry)) {
      return (E) entry;
    }
    throw new IllegalArgumentException(
        "Could not convert RegistryEntry: expecting " + clazz + ", found " + entry.getClass());
  }
}
