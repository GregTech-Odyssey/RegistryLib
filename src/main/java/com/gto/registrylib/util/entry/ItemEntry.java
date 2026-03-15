package com.gto.registrylib.util.entry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ItemEntry<T extends Item> extends ItemProviderEntry<Item, T> {

    public ItemEntry(ResourceKey<Item> key) {
        super(key);
    }

    public static <T extends Item> ItemEntry<T> cast(RegistryEntry<Item, T> entry) {
        return RegistryEntry.cast(ItemEntry.class, entry);
    }
}
