package com.gto.registrylib.util.entry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 配方注册条目，封装了 RecipeType 和 RecipeSerializer 的引用。
 *
 * <p>Wraps both a {@link RecipeType} and {@link RecipeSerializer} registered under the same name.
 *
 * @param <T> the concrete recipe type
 */
public class RecipeEntry<T extends Recipe<?>> {

    private final RegistryEntry<RecipeType<?>, RecipeType<T>> typeEntry;
    private final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<T>> serializerEntry;

    public RecipeEntry(
                       RegistryEntry<RecipeType<?>, RecipeType<T>> typeEntry,
                       RegistryEntry<RecipeSerializer<?>, RecipeSerializer<T>> serializerEntry) {
        this.typeEntry = typeEntry;
        this.serializerEntry = serializerEntry;
    }

    /** Returns the registered RecipeType. */
    public RecipeType<T> getType() {
        return typeEntry.get();
    }

    /** Returns the registered RecipeSerializer. */
    public RecipeSerializer<T> getSerializer() {
        return serializerEntry.get();
    }

    /** Returns the RecipeType RegistryEntry. */
    public RegistryEntry<RecipeType<?>, RecipeType<T>> getTypeEntry() {
        return typeEntry;
    }

    /** Returns the RecipeSerializer RegistryEntry. */
    public RegistryEntry<RecipeSerializer<?>, RecipeSerializer<T>> getSerializerEntry() {
        return serializerEntry;
    }

    /** Returns the ResourceKey of the RecipeType. */
    public ResourceKey<RecipeType<?>> getTypeKey() {
        return typeEntry.getKey();
    }

    /** Returns the ResourceKey of the RecipeSerializer. */
    public ResourceKey<RecipeSerializer<?>> getSerializerKey() {
        return serializerEntry.getKey();
    }
}
