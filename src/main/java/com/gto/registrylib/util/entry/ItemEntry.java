package com.gto.registrylib.util.entry;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ItemEntry<T extends Item> extends ItemProviderEntry<Item, T> implements Holder<Item> {

    public ItemEntry(ResourceKey<Item> key) {
        super(key);
    }

    public static <T extends Item> ItemEntry<T> cast(RegistryEntry<Item, T> entry) {
        return RegistryEntry.cast(ItemEntry.class, entry);
    }

    @Override
    public Item value() {
        return value;
    }

    @Override
    public boolean isBound() {
        return value != null && value.builtInRegistryHolder().isBound();
    }

    @Override
    public boolean areComponentsBound() {
        return value != null && value.builtInRegistryHolder().areComponentsBound();
    }

    @Override
    public boolean is(Identifier key) {
        return value != null && value.builtInRegistryHolder().is(key);
    }

    @Override
    public boolean is(ResourceKey<Item> key) {
        return value != null && value.builtInRegistryHolder().is(key);
    }

    @Override
    public boolean is(Predicate<ResourceKey<Item>> predicate) {
        return value != null && value.builtInRegistryHolder().is(predicate);
    }

    @Override
    public boolean is(TagKey<Item> tag) {
        return value != null && value.builtInRegistryHolder().is(tag);
    }

    @Override
    public Stream<TagKey<Item>> tags() {
        return value != null ? value.builtInRegistryHolder().tags() : Stream.empty();
    }

    @Override
    public DataComponentMap components() {
        return value != null ? value.builtInRegistryHolder().components() : DataComponentMap.EMPTY;
    }

    @Override
    public Either<ResourceKey<Item>, Item> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public Optional<ResourceKey<Item>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<Item> registry) {
        return value != null && value.builtInRegistryHolder().canSerializeIn(registry);
    }

    @Override
    public Holder<Item> getDelegate() {
        return value != null ? value.builtInRegistryHolder() : this;
    }

    @Override
    public ResourceKey<Item> getKey() {
        return this.key;
    }

    @Override
    public <Z> @Nullable Z getData(DataMapType<Item, Z> type) {
        return value == null ? null : value.builtInRegistryHolder().getData(type);
    }
}
