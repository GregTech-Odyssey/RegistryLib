package com.gto.registrylib.builders;

import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;

@FunctionalInterface
public interface BuilderCallback {

    <R, T extends R> RegistryEntry<R, T> accept(
                                                String name,
                                                ResourceKey<? extends Registry<R>> type,
                                                Builder<R, T, ?, ?> builder,
                                                Function<ResourceKey<R>, ? extends T> factory,
                                                Function<ResourceKey<R>, ? extends RegistryEntry<R, T>> entryFactory);

    default <R, T extends R> RegistryEntry<R, T> accept(
                                                        String name,
                                                        ResourceKey<? extends Registry<R>> type,
                                                        Builder<R, T, ?, ?> builder,
                                                        Function<ResourceKey<R>, ? extends T> factory) {
        return accept(name, type, builder, factory, RegistryEntry::new);
    }
}
