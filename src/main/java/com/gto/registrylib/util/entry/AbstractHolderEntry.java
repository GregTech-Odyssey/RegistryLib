package com.gto.registrylib.util.entry;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * Abstract base for registry entries that act as {@link Holder} delegates.
 *
 * <p>
 * Subclasses must implement {@link #delegate()} to return the underlying {@code
 * builtInRegistryHolder} obtained from the registered object. All {@link Holder} methods are
 * implemented here by delegating to that holder, with safe null handling for the pre-binding state.
 *
 * @param <R> the registry element type (e.g. {@code Item}, {@code Block}, {@code Fluid})
 * @param <T> the concrete registered type ({@code T extends R})
 */
public abstract class AbstractHolderEntry<R, T extends R> extends RegistryEntry<R, T>
                                         implements Holder<R> {

    protected AbstractHolderEntry(ResourceKey<R> key) {
        super(key);
    }

    /**
     * Returns the {@link Holder} to which all {@code Holder} methods delegate. Typically {@code
     * value.builtInRegistryHolder} (or equivalent).
     *
     * @return the delegate holder, never {@code null} when called
     * @throws IllegalStateException if the entry has not been bound yet
     */
    protected abstract Holder<R> delegate();

    // ---- Holder<R> implementation ----

    @Override
    public final R value() {
        if (value == null) {
            throw new IllegalStateException(
                    "Registry entry '" + key + "' has not been bound yet. " + "This usually means you're accessing it before registration is complete.");
        }
        return value;
    }

    @Override
    public final boolean isBound() {
        return value != null && delegate().isBound();
    }

    @Override
    @SuppressWarnings("deprecation")
    public final boolean is(Holder<R> holder) {
        return holder.is(this.key);
    }

    @Override
    public final boolean is(ResourceLocation key) {
        return this.key.location().equals(key);
    }

    @Override
    public final boolean is(ResourceKey<R> key) {
        return this.key == key;
    }

    @Override
    public final boolean is(Predicate<ResourceKey<R>> predicate) {
        return predicate.test(key);
    }

    @Override
    public final boolean is(TagKey<R> tag) {
        return value != null && delegate().is(tag);
    }

    @Override
    public final Stream<TagKey<R>> tags() {
        return value != null ? delegate().tags() : Stream.empty();
    }

    @Override
    public final Either<ResourceKey<R>, R> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public final Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public final Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    @Override
    public final boolean canSerializeIn(HolderOwner<R> registry) {
        return value != null && delegate().canSerializeIn(registry);
    }

    @Override
    public final Holder<R> getDelegate() {
        return value != null ? delegate() : this;
    }

    @Override
    public final ResourceKey<R> getKey() {
        return this.key;
    }

    @Override
    public final <Z> @Nullable Z getData(DataMapType<R, Z> type) {
        return value == null ? null : delegate().getData(type);
    }
}
