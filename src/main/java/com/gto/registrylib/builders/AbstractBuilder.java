package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.providers.*;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.Lazy;
import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

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

    public DataGenContext<R, T> getDataGenContext() {
        return new DataGenContext<>(
                valueSupplier, name, Identifier.fromNamespaceAndPath(core.getModid(), name));
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
                            (_, prov) -> tags.forEach(
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
                         @NotNull GeneratorType<? extends D> type, @NotNull BiConsumer<DataGenContext<R, T>, D> cons) {
        if (core.doDatagen()) {
            core.setDataGenerator(
                    name, registryKey, type, prov -> cons.accept(getDataGenContext(), prov));
        }
        return (S) this;
    }

    @StandardAPI
    public <D> S addMiscData(
                             @NotNull GeneratorType<? extends D> type, @NotNull Consumer<? extends D> cons) {
        core.addDataGenerator(type, cons);
        return (S) this;
    }

    @StandardAPI
    public <D> S dataMap(@NotNull DataMapType<R, D> type, @NotNull D val) {
        if (core.doDatagen()) {
            core.addDataGenerator(
                    ProviderType.DATA_MAP, e -> e.builder(type).add(getDataGenContext().getId(), val, false));
        }
        return (S) this;
    }

    @StandardAPI
    public <D> S dataMap(
                         @NotNull DataMapType<R, D> type, @NotNull Function<DataGenContext<R, T>, D> factory) {
        if (core.doDatagen()) {
            core.addDataGenerator(
                    ProviderType.DATA_MAP,
                    e -> {
                        var ctx = getDataGenContext();
                        e.builder(type).add(ctx.getId(), factory.apply(ctx), false);
                    });
        }
        return (S) this;
    }

    @StandardAPI
    public <D> S dataMap(
                         @NotNull DataMapType<R, D> type,
                         @NotNull BiFunction<DataGenContext<R, T>, HolderLookup.Provider, D> factory) {
        if (core.doDatagen()) {
            core.addDataGenerator(
                    ProviderType.DATA_MAP,
                    e -> {
                        var ctx = getDataGenContext();
                        e.builder(type).add(ctx.getId(), factory.apply(ctx, e.getProvider()), false);
                    });
        }
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

    @StandardAPI
    public S lang(@NotNull Function<T, String> langKeyProvider) {
        if (core.doDatagen()) {
            return lang(langKeyProvider, (p, t) -> p.getAutomaticName(t, registryKey));
        }
        return (S) this;
    }

    @StandardAPI
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
