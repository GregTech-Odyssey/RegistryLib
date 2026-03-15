package com.gto.registrylib.util.entry;

import com.gto.registrylib.builders.Builder;

import java.util.function.Supplier;

public class LazyRegistryEntry<R, T extends R> implements Supplier<T> {

    private Builder<R, T, ?, ?> supplier;
    private RegistryEntry<R, T> value;

    public LazyRegistryEntry(Builder<R, T, ?, ?> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        var supplier = this.supplier;
        if (supplier != null) {
            this.value = supplier.get();
            this.supplier = null;
        }
        return this.value.value;
    }
}
