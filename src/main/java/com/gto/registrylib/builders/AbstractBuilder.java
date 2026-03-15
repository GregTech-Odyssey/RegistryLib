package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylib.providers.RegistryLibTagsProvider;
import com.gto.registrylib.util.entry.LazyRegistryEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredHolder;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.NonNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public abstract class AbstractBuilder<R, T extends R, P, S extends AbstractBuilder<R, T, P, S>>
                                     implements Builder<R, T, P, S> {

    private final RegistryCore owner;
    private final P parent;
    private final String name;
    private final BuilderCallback callback;
    private final ResourceKey<? extends Registry<R>> registryKey;

    private final Reference2ReferenceOpenHashMap<ProviderType<? extends RegistryLibTagsProvider<?>>, Object2BooleanOpenHashMap<TagKey<?>>> tagsByType = new Reference2ReferenceOpenHashMap<>();
    private final LazyRegistryEntry<R, T> safeSupplier = new LazyRegistryEntry<>(this);

    protected AbstractBuilder(
                              RegistryCore owner,
                              P parent,
                              String name,
                              BuilderCallback callback,
                              ResourceKey<? extends Registry<R>> registryKey) {
        this.owner = owner;
        this.parent = parent;
        this.name = name;
        this.callback = callback;
        this.registryKey = registryKey;
    }

    @Override
    public @NonNull RegistryCore getOwner() {
        return owner;
    }

    @Override
    public @NonNull P getParent() {
        return parent;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    protected BuilderCallback getCallback() {
        return callback;
    }

    @Override
    public @NonNull ResourceKey<? extends Registry<R>> getRegistryKey() {
        return registryKey;
    }

    protected abstract T createEntry();

    @Override
    @StandardAPI
    @MustBeInvokedByOverriders
    @SuppressWarnings("all")
    public RegistryEntry<R, T> register() {
        tagsByType.forEach(
                (type, tags) -> setData(
                        type,
                (unusedContext, prov) -> tags.forEach(
                                (tag, isOptional) -> prov.rawBuilder((TagKey) tag).add(asTag(isOptional)))));
        return callback.accept(name, registryKey, this, this::createEntry, this::createEntryWrapper);
    }

    protected RegistryEntry<R, T> createEntryWrapper(DeferredHolder<R, T> delegate) {
        return new RegistryEntry<>(getOwner(), delegate);
    }

    @Override
    public @NonNull Supplier<T> asSupplier() {
        return safeSupplier;
    }

    // === Configuration ===

    @SafeVarargs
    @SyntaxSugar("tag(type, false, tags)")
    public final <TP extends TagsProvider<R> & RegistryLibTagsProvider<R>> S tag(
                                                                                 @Nonnull ProviderType<? extends TP> type, @Nonnull TagKey<R>... tags) {
        return tag(type, false, tags);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    @StandardAPI
    public final <TP extends TagsProvider<R> & RegistryLibTagsProvider<R>> S tag(
                                                                                 @Nonnull ProviderType<? extends TP> type, boolean isOptional, @Nonnull TagKey<R>... tags) {
        var map = tagsByType.computeIfAbsent(type, unusedType -> new Object2BooleanOpenHashMap<>());
        for (TagKey<R> tag : tags) {
            map.put(tag, isOptional);
        }
        return (S) this;
    }

    protected TagEntry asTag(boolean isOptional) {
        Identifier id = Identifier.fromNamespaceAndPath(getOwner().getModid(), getName());
        if (isOptional) return TagEntry.optionalElement(id);
        return TagEntry.element(id);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    @StandardAPI
    public final <TP extends TagsProvider<R> & RegistryLibTagsProvider<R>> S removeTag(
                                                                                       @Nonnull ProviderType<TP> type, @Nonnull TagKey<R>... tags) {
        var set = tagsByType.get(type);
        if (set != null) {
            for (TagKey<R> tag : tags) {
                set.removeBoolean(tag);
            }
        }
        return (S) this;
    }

    @StandardAPI
    public S lang(@Nonnull Function<T, String> langKeyProvider) {
        return lang(langKeyProvider, (p, t) -> p.<R>getAutomaticName(t, getRegistryKey()));
    }

    @StandardAPI
    public S lang(@Nonnull Function<T, String> langKeyProvider, @Nonnull String name) {
        return lang(langKeyProvider, (p, s) -> name);
    }

    @StandardAPI
    public S lang(
                  @Nonnull ProviderType<? extends RegistryLibLangProvider> type,
                  @Nonnull Function<T, String> langKeyProvider,
                  @Nonnull String name) {
        return setData(type, (ctx, prov) -> prov.add(langKeyProvider.apply(ctx.getEntry()), name));
    }

    @StandardAPI
    private S lang(
                   @Nonnull Function<T, String> langKeyProvider,
                   @Nonnull BiFunction<RegistryLibLangProvider, Supplier<? extends T>, String> localizedNameProvider) {
        return setData(
                ProviderType.LANG,
                (ctx, prov) -> prov.add(
                        langKeyProvider.apply(ctx.getEntry()),
                        localizedNameProvider.apply(prov, ctx::getEntry)));
    }

    public ResourceKey<R> getResourceKey() {
        return ResourceKey.create(
                getRegistryKey(), Identifier.fromNamespaceAndPath(getOwner().getModid(), getName()));
    }
}
