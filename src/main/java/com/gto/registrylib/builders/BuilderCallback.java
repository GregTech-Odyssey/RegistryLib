package com.gto.registrylib.builders;

import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface BuilderCallback {

    <R, T extends R> RegistryEntry<R, T> accept(
                                                String name,
                                                ResourceKey<? extends Registry<R>> type,
                                                Builder<R, T, ?, ?> builder,
                                                Supplier<? extends T> factory,
                                                Function<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory);

    default <R, T extends R> RegistryEntry<R, T> accept(
                                                        String name,
                                                        ResourceKey<? extends Registry<R>> type,
                                                        Builder<R, T, ?, ?> builder,
                                                        Supplier<? extends T> factory) {
        return accept(
                name,
                type,
                builder,
                factory,
                delegate -> new RegistryEntry<>(builder.getOwner(), delegate));
    }
}
