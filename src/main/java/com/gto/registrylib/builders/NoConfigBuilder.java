package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;

public class NoConfigBuilder<R, T extends R, P>
                            extends AbstractBuilder<R, T, P, NoConfigBuilder<R, T, P>> {

    private final Function<ResourceKey<R>, T> factory;

    public NoConfigBuilder(
                           RegistryCore owner,
                           P parent,
                           String name,
                           ResourceKey<? extends Registry<R>> registryType,
                           Function<ResourceKey<R>, T> factory) {
        super(owner, parent, name, registryType);
        this.factory = factory;
    }

    @Override
    protected T createEntry(ResourceKey<R> key) {
        return factory.apply(key);
    }
}
