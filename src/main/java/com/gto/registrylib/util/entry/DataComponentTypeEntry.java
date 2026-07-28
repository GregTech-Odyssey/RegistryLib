package com.gto.registrylib.util.entry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class DataComponentTypeEntry<T>
                                   extends RegistryEntry<DataComponentType<?>, DataComponentType<T>> {

    public DataComponentTypeEntry(ResourceKey<DataComponentType<?>> key) {
        super(key);
    }

    @Nullable
    public T get(ItemStack stack) {
        if (value == null)
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        return stack.get(value);
    }

    public T getOrDefault(ItemStack stack, T defaultValue) {
        if (value == null)
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        return stack.getOrDefault(value, defaultValue);
    }

    public boolean has(ItemStack stack) {
        if (value == null)
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        return stack.has(value);
    }

    @Nullable
    public T set(ItemStack stack, @Nullable T value) {
        if (this.value == null)
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        return stack.set(this.value, value);
    }

    @Nullable
    public T remove(ItemStack stack) {
        if (value == null)
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        return stack.remove(value);
    }

    public Item.Properties component(Item.Properties properties, T value) {
        if (this.value == null)
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        return properties.component(this.value, value);
    }

    public static <T> DataComponentTypeEntry<T> cast(
                                                     RegistryEntry<DataComponentType<?>, DataComponentType<T>> entry) {
        return RegistryEntry.cast(DataComponentTypeEntry.class, entry);
    }
}
