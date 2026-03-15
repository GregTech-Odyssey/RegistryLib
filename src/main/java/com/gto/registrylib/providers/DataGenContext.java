package com.gto.registrylib.providers;

import com.gto.registrylib.builders.Builder;

import net.minecraft.resources.Identifier;

import lombok.Getter;

import java.util.function.Supplier;

public class DataGenContext<R, E extends R> implements Supplier<E> {

    private final Supplier<E> entry;
    @Getter
    private final String name;
    @Getter
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

    public static <R, E extends R> DataGenContext<R, E> from(Builder<R, E, ?, ?> builder) {
        return new DataGenContext<>(
                builder.asSupplier(),
                builder.getName(),
                Identifier.fromNamespaceAndPath(builder.getOwner().getModid(), builder.getName()));
    }
}
