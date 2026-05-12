package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.datagen.GeneratorType;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.datagen.provider.RegistryLibRecipeProvider;
import com.gto.registrylib.datagen.provider.RegistryLibTagsProvider;
import com.gto.registrylib.util.FunctionUtil;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class AbstractBuilder<R, T extends R, P, S extends AbstractBuilder<R, T, P, S>> {

    protected final RegistryCore core;
    protected final P parent;
    @Getter
    protected final String name;
    protected final ResourceKey<? extends Registry<R>> registryKey;
    protected final Reference2ReferenceOpenHashMap<ProviderType<? extends RegistryLibTagsProvider<?>>, Reference2BooleanOpenHashMap<TagKey<?>>> tagsByType;

    private @Nullable List<Consumer<? super T>> callbacks;
    private @Nullable Supplier<T> valueSupplier;

    private boolean registered;

    protected AbstractBuilder(
                              RegistryCore core, P parent, String name, ResourceKey<? extends Registry<R>> registryKey) {
        this.core = core;
        this.parent = parent;
        this.name = name;
        this.registryKey = registryKey;
        this.tagsByType = core.doDatagen() ? new Reference2ReferenceOpenHashMap<>() : null;
    }

    protected abstract T createEntry(ResourceKey<R> key);

    protected abstract RegistryEntry<R, T> createEntryWrapper(ResourceKey<R> key);

    public final T getValue() {
        return getValueSupplier().get();
    }

    public final Supplier<T> getValueSupplier() {
        var supplier = valueSupplier;
        if (supplier == null) {
            valueSupplier = supplier = new ValueSupplier<>(core, name, registryKey);
        }
        return supplier;
    }

    public final String getName() {
        return name;
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
        if (registered) throw new IllegalStateException("Builder already registered: " + name);
        registered = true;
        if (tagsByType != null) {
            tagsByType.forEach(
                    (type, tags) -> setData(
                            type,
                            prov -> tags.forEach(
                                    (tag, isOptional) -> prov.rawBuilder((TagKey) tag).add(asTag(isOptional)))));
        }
        var cbs = callbacks != null ? callbacks : Collections.<Consumer<? super T>>emptyList();
        return core.registry(name, registryKey, cbs, this::createEntry, this::createEntryWrapper);
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
    public S addRecipeData(@NotNull Consumer<RegistryLibRecipeProvider> cons) {
        core.addRecipeData(cons);
        return (S) this;
    }

    @StandardAPI
    public S onRegister(@NotNull Consumer<? super T> callback) {
        if (callbacks == null) callbacks = new ArrayList<>();
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
                  @NotNull ProviderType<? extends RegistryLibLangProvider> type,
                  @NotNull Function<T, String> langKeyProvider,
                  @NotNull String name) {
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

    private static final class ValueSupplier<R, T extends R> implements Supplier<T> {

        private final RegistryCore core;
        private final String name;
        private final ResourceKey<? extends Registry<R>> registryKey;
        private T value;

        private ValueSupplier(
                              RegistryCore core, String name, ResourceKey<? extends Registry<R>> registryKey) {
            this.core = core;
            this.name = name;
            this.registryKey = registryKey;
        }

        @Override
        public T get() {
            var value = this.value;
            if (value == null) {
                RegistryEntry<R, T> entry = core.get(name, registryKey);
                value = this.value = entry.get();
            }
            return value;
        }
    }
}
