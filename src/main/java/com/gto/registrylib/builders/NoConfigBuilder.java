package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public class NoConfigBuilder<R, T extends R, P>
                            extends AbstractBuilder<R, T, P, NoConfigBuilder<R, T, P>> {

    private final Supplier<T> factory;

    public NoConfigBuilder(
                           RegistryCore owner,
                           P parent,
                           String name,
                           BuilderCallback callback,
                           ResourceKey<? extends Registry<R>> registryType,
                           Supplier<T> factory) {
        super(owner, parent, name, callback, registryType);
        this.factory = factory;
    }

    @Override
    protected T createEntry() {
        return factory.get();
    }
}
