package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import lombok.Getter;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RegistryEntry<T, S extends T> implements Supplier<S> {

    @Getter
    protected final ResourceKey<T> key;

    protected S value;

    public RegistryEntry(ResourceKey<T> key) {
        this.key = key;
    }

    public <X, Y extends X> RegistryEntry<X, Y> getSibling(
                                                           RegistryCore owner, ResourceKey<? extends Registry<X>> registryType) {
        return owner.get(key.identifier().getPath(), registryType);
    }

    public <X, Y extends X> RegistryEntry<X, Y> getSibling(RegistryCore owner, Registry<X> registry) {
        return getSibling(owner, registry.key());
    }

    public Optional<RegistryEntry<T, S>> filter(Predicate<T> predicate) {
        if (predicate.test(value)) {
            return Optional.of(this);
        }
        return Optional.empty();
    }

    public <X> boolean is(X entry) {
        return value == entry;
    }

    @SuppressWarnings("unchecked")
    protected static <E extends RegistryEntry<?, ?>> E cast(
                                                            Class<? super E> clazz, RegistryEntry<?, ?> entry) {
        try {
            return (E) entry;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Could not convert RegistryEntry: expecting " + clazz + ", found " + entry.getClass());
        }
    }

    @Override
    public S get() {
        return value;
    }

    public void set(S value) {
        if (this.value != null) throw new IllegalStateException("key: " + key + " value already bound");
        this.value = value;
    }
}
