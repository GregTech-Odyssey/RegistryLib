package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylib.providers.RegistryLibTagsProvider;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.entry.LazyRegistryEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

@SuppressWarnings("unchecked")
public abstract class AbstractBuilder<R, T extends R, P, S extends AbstractBuilder<R, T, P, S>>
                                     implements Builder<R, T, P, S> {

    @Getter
    private final RegistryCore owner;
    @Getter
    private final P parent;
    @Getter
    private final String name;
    @Getter
    private final BuilderCallback callback;
    @Getter
    private final ResourceKey<? extends Registry<R>> registryKey;

    private final Reference2ReferenceOpenHashMap<ProviderType<? extends RegistryLibTagsProvider<?>>, Reference2BooleanOpenHashMap<TagKey<?>>> tagsByType;
    private final LazyRegistryEntry<R, T> safeSupplier = new LazyRegistryEntry<>(this);
    private RegistryEntry<R, T> entry;

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
        this.tagsByType = owner.doDatagen() ? new Reference2ReferenceOpenHashMap<>() : null;
    }

    protected abstract T createEntry(ResourceKey<R> key);

    @Override
    @StandardAPI
    @MustBeInvokedByOverriders
    public RegistryEntry<R, T> register() {
        if (tagsByType != null) {
            tagsByType.forEach(
                    (type, tags) -> setData(
                            type,
                            (_, prov) -> tags.forEach(
                                    (tag, isOptional) -> prov.rawBuilder((TagKey) tag).add(asTag(isOptional)))));
        }
        return callback.accept(name, registryKey, this, this::createEntry, this::createEntryWrapper);
    }

    protected RegistryEntry<R, T> createEntryWrapper(ResourceKey<R> key) {
        return new RegistryEntry<>(key);
    }

    @NotNull
    public RegistryEntry<R, T> get() {
        if (entry != null) return entry;
        return entry = owner.get(name, registryKey);
    }

    @Override
    public @NotNull Supplier<T> asSupplier() {
        return safeSupplier;
    }

    // === Configuration ===

    @SafeVarargs
    @StandardAPI
    public final <TP extends TagsProvider<R> & RegistryLibTagsProvider<R>> S tag(
                                                                                 @NotNull ProviderType<? extends TP> type, @NotNull TagKey<R>... tags) {
        return tag(type, false, tags);
    }

    @SafeVarargs
    @StandardAPI
    public final <TP extends TagsProvider<R> & RegistryLibTagsProvider<R>> S tag(
                                                                                 @NotNull ProviderType<? extends TP> type, boolean isOptional, @NotNull TagKey<R>... tags) {
        if (tagsByType != null) {
            var map = tagsByType.computeIfAbsent(type, _ -> new Reference2BooleanOpenHashMap<>());
            for (TagKey<R> tag : tags) {
                map.put(tag, isOptional);
            }
        }
        return (S) this;
    }

    protected TagEntry asTag(boolean isOptional) {
        Identifier id = Identifier.fromNamespaceAndPath(getOwner().getModid(), getName());
        if (isOptional) return TagEntry.optionalElement(id);
        return TagEntry.element(id);
    }

    @SafeVarargs
    @StandardAPI
    public final <TP extends TagsProvider<R> & RegistryLibTagsProvider<R>> S removeTag(
                                                                                       @NotNull ProviderType<TP> type, @NotNull TagKey<R>... tags) {
        if (tagsByType != null) {
            var set = tagsByType.get(type);
            if (set != null) {
                for (TagKey<R> tag : tags) {
                    set.removeBoolean(tag);
                }
            }
        }
        return (S) this;
    }

    @StandardAPI
    public S lang(@NotNull Function<T, String> langKeyProvider) {
        if (owner.doDatagen()) {
            return lang(langKeyProvider, (p, t) -> p.getAutomaticName(t, getRegistryKey()));
        }
        return (S) this;
    }

    @StandardAPI
    public S lang(@NotNull Function<T, String> langKeyProvider, @NotNull String name) {
        if (owner.doDatagen()) {
            return lang(langKeyProvider, FunctionUtil.constantBiFn(name));
        }
        return (S) this;
    }

    @StandardAPI
    public S lang(
                  @Nonnull ProviderType<? extends RegistryLibLangProvider> type,
                  @Nonnull Function<T, String> langKeyProvider,
                  @Nonnull String name) {
        if (owner.doDatagen()) {
            return setData(type, (ctx, prov) -> prov.add(langKeyProvider.apply(ctx.getEntry()), name));
        }
        return (S) this;
    }

    private S lang(
                   @NotNull Function<T, String> langKeyProvider,
                   @NotNull BiFunction<RegistryLibLangProvider, Supplier<? extends T>, String> localizedNameProvider) {
        return setData(
                ProviderType.LANG,
                (ctx, prov) -> prov.add(
                        langKeyProvider.apply(ctx.getEntry()),
                        localizedNameProvider.apply(prov, ctx::getEntry)));
    }
}
