package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import lombok.Getter;

import java.util.Locale;
import java.util.function.Supplier;

public class RegistryEntry<T, S extends T> implements Supplier<S> {

    public static final RegistryEntry EMPTY = new RegistryEntry<>(null) {

        @Override
        public void bound(Object value) {}
    };

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
        return value;
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
