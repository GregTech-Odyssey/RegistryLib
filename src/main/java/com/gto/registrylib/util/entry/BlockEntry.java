package com.gto.registrylib.util.entry;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlockEntry<T extends Block> extends ItemProviderEntry<Block, T>
                       implements Holder<Block> {

    public BlockEntry(ResourceKey<Block> key) {
        super(key);
    }

    public static <T extends Block> BlockEntry<T> cast(RegistryEntry<Block, T> entry) {
        return RegistryEntry.cast(BlockEntry.class, entry);
    }

    public BlockState getDefaultState() {
        return value.defaultBlockState();
    }

    public boolean is(BlockState state) {
        return value == state.getBlock();
    }

    @Override
    public final Block value() {
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
    @SuppressWarnings("deprecation")
    public final boolean is(Holder<Block> holder) {
        return holder.is(this.key);
    }

    @Override
    public final boolean is(Identifier key) {
        return this.key.identifier().equals(key);
    }

    @Override
    public final boolean is(ResourceKey<Block> key) {
        return this.key == key;
    }

    @Override
    public final boolean is(Predicate<ResourceKey<Block>> predicate) {
        return predicate.test(key);
    }

    @Override
    public final boolean is(TagKey<Block> tag) {
        return value != null && value.builtInRegistryHolder.is(tag);
    }

    @Override
    public final Stream<TagKey<Block>> tags() {
        return value != null ? value.builtInRegistryHolder.tags() : Stream.empty();
    }

    @Override
    public final DataComponentMap components() {
        return value != null ? value.builtInRegistryHolder.components() : DataComponentMap.EMPTY;
    }

    @Override
    public final Either<ResourceKey<Block>, Block> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public final Optional<ResourceKey<Block>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public final Holder.Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    @Override
    public final boolean canSerializeIn(HolderOwner<Block> registry) {
        return value != null && value.builtInRegistryHolder.canSerializeIn(registry);
    }

    @Override
    public final Holder<Block> getDelegate() {
        return value != null ? value.builtInRegistryHolder : this;
    }

    @Override
    public final ResourceKey<Block> getKey() {
        return this.key;
    }

    @Override
    public final <Z> @Nullable Z getData(DataMapType<Block, Z> type) {
        return value == null ? null : value.builtInRegistryHolder.getData(type);
    }
}
