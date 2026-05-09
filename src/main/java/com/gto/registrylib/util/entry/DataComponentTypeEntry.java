package com.gto.registrylib.util.entry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public class DataComponentTypeEntry<T>
                                   extends RegistryEntry<DataComponentType<?>, DataComponentType<T>> {

    public DataComponentTypeEntry(ResourceKey<DataComponentType<?>> key) {
        super(key);
    }
}
