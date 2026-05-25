package com.gto.registrylib.util.entry;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.item.ItemResource;

public abstract class ItemProviderEntry<T extends ItemLike, S extends T> extends AbstractHolderEntry<T, S>
                              implements ItemLike {

    private ItemStack readOnlyStack;

    public ItemProviderEntry(ResourceKey<T> key) {
        super(key);
    }

    public ItemResource asResource() {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet.");
        }
        return ItemResource.of(value);
    }

    public ItemResource asResource(DataComponentPatch components) {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet.");
        }
        return ItemResource.of(value, components);
    }

    public ItemStack readOnlyStack() {
        var stack = readOnlyStack;
        if (stack == null || stack.count < 1) {
            readOnlyStack = stack = asStack();
        }
        return stack;
    }

    public ItemStack asStack() {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet.");
        }
        return new ItemStack(value.asItem().builtInRegistryHolder);
    }

    public ItemStack asStack(int count) {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet.");
        }
        return new ItemStack(value.asItem().builtInRegistryHolder, count);
    }

    public ItemStack asStack(int count, DataComponentPatch components) {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet.");
        }
        return new ItemStack(value.asItem().builtInRegistryHolder, count, components);
    }

    public boolean is(ItemStack stack) {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet.");
        }
        return value.asItem() == stack.getItem();
    }

    public boolean is(Item item) {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet.");
        }
        return value.asItem() == item;
    }

    @Override
    public Item asItem() {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet.");
        }
        return value.asItem();
    }
}
