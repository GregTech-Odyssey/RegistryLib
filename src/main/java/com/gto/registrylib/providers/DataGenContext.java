package com.gto.registrylib.providers;

import com.gto.registrylib.builders.Builder;

import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class DataGenContext<R, E extends R> implements Supplier<E> {

    private final Supplier<E> entry;
    private final String name;
    private final Identifier id;

    public DataGenContext(Supplier<E> entry, String name, Identifier id) {
        this.entry = entry;
        this.name = name;
        this.id = id;
    }

    @Override
    public E get() {
        return entry.get();
    }

    public E getEntry() {
        return entry.get();
    }

    public String getName() {
        return name;
    }

    public Identifier getId() {
        return id;
    }

    public static <R, E extends R> DataGenContext<R, E> from(Builder<R, E, ?, ?> builder) {
        return new DataGenContext<>(
                () -> builder.getOwner().<R, E>get(builder.getName(), builder.getRegistryKey()).get(),
                builder.getName(),
                Identifier.fromNamespaceAndPath(builder.getOwner().getModid(), builder.getName()));
    }
}
