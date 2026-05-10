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
        return stack.get(value);
    }

    public T getOrDefault(ItemStack stack, T defaultValue) {
        return stack.getOrDefault(value, defaultValue);
    }

    public boolean has(ItemStack stack) {
        return stack.has(value);
    }

    @Nullable
    public T set(ItemStack stack, @Nullable T value) {
        return stack.set(this.value, value);
    }

    @Nullable
    public T remove(ItemStack stack) {
        return stack.remove(value);
    }

    public Item.Properties component(Item.Properties properties, T value) {
        return properties.component(this.value, value);
    }
}
