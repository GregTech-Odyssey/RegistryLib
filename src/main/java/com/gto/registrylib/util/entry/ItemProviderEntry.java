package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemProviderEntry<R extends ItemLike, T extends R> extends RegistryEntry<R, T>
                              implements ItemLike {

    public ItemProviderEntry(RegistryCore owner, DeferredHolder<R, T> delegate) {
        super(owner, delegate);
    }

    public ItemStack asStack() {
        return new ItemStack(this);
    }

    public ItemStack asStack(int count) {
        return new ItemStack(this, count);
    }

    public boolean isIn(ItemStack stack) {
        return value().asItem() == stack.getItem();
    }

    public boolean is(Item item) {
        return value().asItem() == item;
    }

    @Override
    public Item asItem() {
        return value().asItem();
    }
}
