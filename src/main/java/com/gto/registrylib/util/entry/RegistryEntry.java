package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import lombok.Getter;

import java.util.Locale;
import java.util.Optional;
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

    public boolean is(T entry) {
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

    public Identifier identifier() {
        return key.identifier();
    }

    @Override
    public S get() {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet. "
                            + "This usually means you're accessing it before registration is complete.");
        }
        return value;
    }

    public Optional<S> getOptional() {
        return Optional.ofNullable(value);
    }

    public boolean isBound() {
        return value != null;
    }

    public void bound(S value) {
        if (this.value != null) throw new IllegalStateException("key: " + key + " value already bound");
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "RegistryEntry{%s}", this.key);
    }
}
