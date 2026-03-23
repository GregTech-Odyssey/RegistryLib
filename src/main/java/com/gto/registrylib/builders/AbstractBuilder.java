package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.datagen.*;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.datagen.provider.RegistryLibTagsProvider;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.Lazy;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

import javax.annotation.Nonnull;

@SuppressWarnings("unchecked")
public abstract class AbstractBuilder<R, T extends R, P, S extends AbstractBuilder<R, T, P, S>> {

    protected final RegistryCore core;
    protected final P parent;
    @Getter
    protected final String name;
    protected final ResourceKey<? extends Registry<R>> registryKey;

    protected final List<Consumer<? super T>> callbacks = new ArrayList<>();
    protected final Supplier<T> valueSupplier;
    protected final Reference2ReferenceOpenHashMap<ProviderType<? extends RegistryLibTagsProvider<?>>, Reference2BooleanOpenHashMap<TagKey<?>>> tagsByType;

    protected AbstractBuilder(
                              RegistryCore core, P parent, String name, ResourceKey<? extends Registry<R>> registryKey) {
        this.core = core;
        this.parent = parent;
        this.name = name;
        this.registryKey = registryKey;
        this.tagsByType = core.doDatagen() ? new Reference2ReferenceOpenHashMap<>() : null;
        this.valueSupplier = Lazy.of(() -> (T) core.get(name, registryKey).get());
    }

    protected abstract T createEntry(ResourceKey<R> key);

    protected RegistryEntry<R, T> createEntryWrapper(ResourceKey<R> key) {
        return new RegistryEntry<>(key);
    }

    public T getValue() {
        return valueSupplier.get();
    }

    /**
     * Registers this entry and returns the parent object, allowing the caller to continue configuring
     * the parent builder. Typically used to close a sub-entry chain: {@code
     * .item().tooltip(...).build() // returns the parent BlockBuilder}.
     */
    @StandardAPI
    public P build() {
        register();
        return parent;
    }

    @StandardAPI
    @MustBeInvokedByOverriders
    public RegistryEntry<R, T> register() {
        if (tagsByType != null) {
            tagsByType.forEach(
                    (type, tags) -> setData(
                            type,
                            prov -> tags.forEach(
                                    (tag, isOptional) -> prov.rawBuilder((TagKey) tag).add(asTag(isOptional)))));
        }
        return core.registry(name, registryKey, callbacks, this::createEntry, this::createEntryWrapper);
    }

    public static <R, T extends R> RegistryEntry<R, T> registry(
                                                                RegistryCore core,
                                                                String name,
                                                                ResourceKey<? extends Registry<R>> type,
                                                                AbstractBuilder<R, T, ?, ?> builder,
                                                                Function<ResourceKey<R>, ? extends T> factory) {
        return core.registry(name, type, builder.callbacks, factory, RegistryEntry::new);
    }

    // === Configuration ===

    @StandardAPI
    public <D> S setData(
                         @NotNull GeneratorType<? extends D> type, @NotNull Consumer<? extends D> cons) {
        core.setDataGenerator(name, registryKey, type, cons);
        return (S) this;
    }

    @StandardAPI
    public <D> S addData(
                         @NotNull GeneratorType<? extends D> type, @NotNull Consumer<? extends D> cons) {
        core.addDataGenerator(type, cons);
        return (S) this;
    }

    @StandardAPI
    public S onRegister(@NotNull Consumer<? super T> callback) {
        callbacks.add(callback);
        return (S) this;
    }

    @StandardAPI
    public <OR> S onRegisterAfter(
                                  @NotNull ResourceKey<? extends Registry<OR>> dependencyType,
                                  @NotNull Consumer<? super T> callback) {
        return onRegister(
                e -> {
                    if (core.isRegistered(dependencyType)) {
                        callback.accept(e);
                    } else {
                        core.addRegisterCallback(dependencyType, () -> callback.accept(e));
                    }
                });
    }

    @SafeVarargs
    @StandardAPI
    public final <E, TP extends TagsProvider<E> & RegistryLibTagsProvider<E>> S addTag(
                                                                                       @NotNull ProviderType<? extends TP> type, @NotNull TagKey<E>... tags) {
        return addTag(type, false, tags);
    }

    @SafeVarargs
    @StandardAPI
    public final <E, TP extends TagsProvider<E> & RegistryLibTagsProvider<E>> S addTag(
                                                                                       @NotNull ProviderType<? extends TP> type, boolean isOptional, @NotNull TagKey<E>... tags) {
        if (tagsByType != null) {
            var map = tagsByType.computeIfAbsent(type, _ -> new Reference2BooleanOpenHashMap<>());
            for (var tag : tags) {
                map.put(tag, isOptional);
            }
        }
        return (S) this;
    }

    protected TagEntry asTag(boolean isOptional) {
        Identifier id = Identifier.fromNamespaceAndPath(core.getModid(), name);
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

    @SyntaxSugar("lang(langKeyProvider, (p, t) -> p.getAutomaticName(t, registryKey))")
    public S lang(@NotNull Function<T, String> langKeyProvider) {
        if (core.doDatagen()) {
            return lang(langKeyProvider, (p, t) -> p.getAutomaticName(t, registryKey));
        }
        return (S) this;
    }

    @SyntaxSugar("lang(ProviderType.LANG, langKeyProvider, name)")
    public S lang(@NotNull Function<T, String> langKeyProvider, @NotNull String name) {
        if (core.doDatagen()) {
            return lang(langKeyProvider, FunctionUtil.constantBiFn(name));
        }
        return (S) this;
    }

    @StandardAPI
    public S lang(
                  @Nonnull ProviderType<? extends RegistryLibLangProvider> type,
                  @Nonnull Function<T, String> langKeyProvider,
                  @Nonnull String name) {
        if (core.doDatagen()) {
            return setData(type, prov -> prov.add(langKeyProvider.apply(getValue()), name));
        }
        return (S) this;
    }

    private S lang(
                   @NotNull Function<T, String> langKeyProvider,
                   @NotNull BiFunction<RegistryLibLangProvider, Supplier<? extends T>, String> localizedNameProvider) {
        return setData(
                ProviderType.LANG,
                prov -> prov.add(
                        langKeyProvider.apply(getValue()),
                        localizedNameProvider.apply(prov, this::getValue)));
    }
}
