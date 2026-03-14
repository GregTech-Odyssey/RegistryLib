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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

public interface Builder<R, T extends R, P, S extends Builder<R, T, P, S>>
                        extends Supplier<RegistryEntry<R, T>> {

    // === Core ===

    @StandardAPI
    RegistryEntry<R, T> register();

    @Nonnull
    RegistryCore getOwner();

    @Nonnull
    P getParent();

    @Nonnull
    String getName();

    @Nonnull
    ResourceKey<? extends Registry<R>> getRegistryKey();

    @Override
    @Nonnull
    default RegistryEntry<R, T> get() {
        return getOwner().get(getName(), getRegistryKey());
    }

    @Nonnull
    default T getEntry() {
        return get().get();
    }

    @Nonnull
    Supplier<T> asSupplier();

    // === Configuration ===

    @SuppressWarnings("unchecked")
    @StandardAPI
    default <D> S setData(
                          @Nonnull GeneratorType<? extends D> type, @Nonnull BiConsumer<DataGenContext<R, T>, D> cons) {
        getOwner().setDataGenerator(this, type, prov -> cons.accept(DataGenContext.from(this), prov));
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @StandardAPI
    default <D> S addMiscData(
                              @Nonnull GeneratorType<? extends D> type, @Nonnull Consumer<? extends D> cons) {
        getOwner().addDataGenerator(type, cons);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @StandardAPI
    default <D> S dataMap(@Nonnull DataMapType<R, D> type, @Nonnull D val) {
        getOwner()
                .addDataGenerator(
                        ProviderType.DATA_MAP,
                        e -> e.builder(type).add(DataGenContext.from(this).getId(), val, false));
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @StandardAPI
    default <D> S dataMap(
                          @Nonnull DataMapType<R, D> type, @Nonnull Function<DataGenContext<R, T>, D> factory) {
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
                          @Nonnull DataMapType<R, D> type,
                          @Nonnull BiFunction<DataGenContext<R, T>, HolderLookup.Provider, D> factory) {
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
    default S onRegister(@Nonnull Consumer<? super T> callback) {
        getOwner().<R, T>addRegisterCallback(getName(), getRegistryKey(), callback);
        return (S) this;
    }

    @StandardAPI
    default <OR> S onRegisterAfter(
                                   @Nonnull ResourceKey<? extends Registry<OR>> dependencyType,
                                   @Nonnull Consumer<? super T> callback) {
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
    default S transform(@Nonnull UnaryOperator<S> func) {
        return func.apply((S) this);
    }
}
