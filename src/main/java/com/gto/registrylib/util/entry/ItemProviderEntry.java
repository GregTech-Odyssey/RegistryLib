package com.gto.registrylib.util.entry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemProviderEntry<T extends ItemLike, S extends T> extends RegistryEntry<T, S>
                              implements ItemLike {

    public ItemProviderEntry(ResourceKey<T> key) {
        super(key);
    }

    public ItemStack asStack() {
        return new ItemStack(this);
    }

    public ItemStack asStack(int count) {
        return new ItemStack(this, count);
    }

    public boolean is(ItemStack stack) {
        return value.asItem() == stack.getItem();
    }

    public boolean is(Holder<Item> holder) {
        return value.asItem() == holder.value();
    }

    public boolean is(Item item) {
        return value.asItem() == item;
    }

    @Override
    public Item asItem() {
        return value.asItem();
    }
}
