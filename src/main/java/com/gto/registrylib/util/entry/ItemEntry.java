package com.gto.registrylib.util.entry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemEntry<T extends Item> extends ItemProviderEntry<Item, T> {

    public ItemEntry(ResourceKey<Item> key) {
        super(key);
    }

    public static <T extends Item> ItemEntry<T> cast(RegistryEntry<Item, T> entry) {
        return RegistryEntry.cast(ItemEntry.class, entry);
    }

    @Override
    protected Holder<Item> delegate() {
        return value.builtInRegistryHolder;
    }

    @Override
    public final boolean is(ItemStack stack) {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return value == stack.getItem();
    }

    @Override
    public final boolean is(Item item) {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return value == item;
    }
}
