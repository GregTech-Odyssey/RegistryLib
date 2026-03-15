package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.providers.DataGenContext;
import com.gto.registrylib.providers.GeneratorType;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.*;

public interface Builder<R, T extends R, P, S extends Builder<R, T, P, S>> {

    // === Core ===

    @StandardAPI
    RegistryEntry<R, T> register();

    @NotNull
    RegistryCore getOwner();

    @NotNull
    P getParent();

    @NotNull
    String getName();

    @NotNull
    ResourceKey<? extends Registry<R>> getRegistryKey();

    List<Consumer<? super T>> getCallbacks();

    @NotNull
    Supplier<T> asSupplier();

    default T getValue() {
        return asSupplier().get();
    }

    // === Configuration ===

    @SuppressWarnings("unchecked")
    @StandardAPI
    default <D> S setData(
                          @NotNull GeneratorType<? extends D> type, @NotNull BiConsumer<DataGenContext<R, T>, D> cons) {
        if (getOwner().doDatagen()) {
            getOwner().setDataGenerator(this, type, prov -> cons.accept(DataGenContext.from(this), prov));
        }
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @StandardAPI
    default <D> S addMiscData(
                              @NotNull GeneratorType<? extends D> type, @NotNull Consumer<? extends D> cons) {
        getOwner().addDataGenerator(type, cons);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @StandardAPI
    default <D> S dataMap(@NotNull DataMapType<R, D> type, @NotNull D val) {
        getOwner()
                .addDataGenerator(
                        ProviderType.DATA_MAP,
                        e -> e.builder(type).add(DataGenContext.from(this).getId(), val, false));
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @StandardAPI
    default <D> S dataMap(
                          @NotNull DataMapType<R, D> type, @NotNull Function<DataGenContext<R, T>, D> factory) {
        getOwner()
                .addDataGenerator(
                        ProviderType.DATA_MAP,
                        e -> {
                            var ctx = DataGenContext.from(this);
                            e.builder(type).add(ctx.getId(), factory.apply(ctx), false);
                        });
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @StandardAPI
    default <D> S dataMap(
                          @NotNull DataMapType<R, D> type,
                          @NotNull BiFunction<DataGenContext<R, T>, HolderLookup.Provider, D> factory) {
        getOwner()
                .addDataGenerator(
                        ProviderType.DATA_MAP,
                        e -> {
                            var ctx = DataGenContext.from(this);
                            e.builder(type).add(ctx.getId(), factory.apply(ctx, e.getProvider()), false);
                        });
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @StandardAPI
    default S onRegister(@NotNull Consumer<? super T> callback) {
        getCallbacks().add(callback);
        return (S) this;
    }

    @StandardAPI
    default <OR> S onRegisterAfter(
                                   @NotNull ResourceKey<? extends Registry<OR>> dependencyType,
                                   @NotNull Consumer<? super T> callback) {
        return onRegister(
                e -> {
                    if (getOwner().isRegistered(dependencyType)) {
                        callback.accept(e);
                    } else {
                        getOwner().addRegisterCallback(dependencyType, () -> callback.accept(e));
                    }
                });
    }

    @SuppressWarnings("unchecked")
    @StandardAPI
    default S transform(@NotNull UnaryOperator<S> func) {
        return func.apply((S) this);
    }

    /**
     * Registers this entry and returns the parent object, allowing the caller to continue configuring
     * the parent builder. Typically used to close a sub-entry chain: {@code
     * .item().tooltip(...).build() // returns the parent BlockBuilder}.
     */
    @StandardAPI
    default P build() {
        register();
        return getParent();
    }
}
