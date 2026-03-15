package com.gto.registrylib.util.entry;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    public final boolean is(ItemStack stack) {
        return value == stack.getItem();
    }

    @Override
    public final boolean is(Item item) {
        return value == item;
    }

    @Override
    public final Item value() {
        return value;
    }

    @Override
    public final boolean isBound() {
        return value != null && value.builtInRegistryHolder.isBound();
    }

    @Override
    public final boolean areComponentsBound() {
        return value != null && value.builtInRegistryHolder.areComponentsBound();
    }

    @Override
    public final boolean is(Holder<Item> holder) {
        return value == holder.value();
    }

    @Override
    public final boolean is(Identifier key) {
        return this.key.identifier().equals(key);
    }

    @Override
    public final boolean is(ResourceKey<Item> key) {
        return this.key == key;
    }

    @Override
    public final boolean is(Predicate<ResourceKey<Item>> predicate) {
        return predicate.test(key);
    }

    @Override
    public final boolean is(TagKey<Item> tag) {
        return value != null && value.builtInRegistryHolder.is(tag);
    }

    @Override
    public final Stream<TagKey<Item>> tags() {
        return value != null ? value.builtInRegistryHolder.tags() : Stream.empty();
    }

    @Override
    public final DataComponentMap components() {
        return value != null ? value.builtInRegistryHolder.components() : DataComponentMap.EMPTY;
    }

    @Override
    public final Either<ResourceKey<Item>, Item> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public final Optional<ResourceKey<Item>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public final Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    @Override
    public final boolean canSerializeIn(HolderOwner<Item> registry) {
        return value != null && value.builtInRegistryHolder.canSerializeIn(registry);
    }

    @Override
    public final Holder<Item> getDelegate() {
        return value != null ? value.builtInRegistryHolder : this;
    }

    @Override
    public final ResourceKey<Item> getKey() {
        return this.key;
    }

    @Override
    public final <Z> @Nullable Z getData(DataMapType<Item, Z> type) {
        return value == null ? null : value.builtInRegistryHolder.getData(type);
    }
}
