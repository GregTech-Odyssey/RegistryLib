package com.gto.registrylib.util.entry;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemProviderEntry<T extends ItemLike, S extends T> extends RegistryEntry<T, S>
                              implements ItemLike {

    private ItemStack readOnlyStack;

    public ItemProviderEntry(ResourceKey<T> key) {
        super(key);
    }

    public ItemResource asResource() {
        return ItemResource.of(value);
    }

    public ItemResource asResource(DataComponentPatch components) {
        return ItemResource.of(value, components);
    }

    public ItemStack readOnlyStack() {
        var stack = readOnlyStack;
        if (stack == null) {
            readOnlyStack = stack = asStack();
        }
        return stack.copy();
    }

    public ItemStack asStack() {
        return new ItemStack(value.asItem().builtInRegistryHolder);
    }

    public ItemStack asStack(int count) {
        return new ItemStack(value.asItem().builtInRegistryHolder, count);
    }

    public ItemStack asStack(int count, DataComponentPatch components) {
        return new ItemStack(value.asItem().builtInRegistryHolder, count, components);
    }

    public boolean is(ItemStack stack) {
        return value.asItem() == stack.getItem();
    }

    public boolean is(Item item) {
        return value.asItem() == item;
    }

    @Override
    public Item asItem() {
        return value.asItem();
    }
}
