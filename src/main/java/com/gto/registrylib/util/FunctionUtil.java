package com.gto.registrylib.util;

import lombok.experimental.UtilityClass;

import java.util.function.*;

@UtilityClass
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FunctionUtil {

    public final Supplier NULL_SUPPLIER = () -> null;

    public final Function IDENTITY_FN = Function.identity();

    public final Consumer NO_OP_CONSUMER = _ -> {};

    public final BiConsumer NO_OP_BICONSUMER = (_, _) -> {};

    public final Predicate ALWAYS_TRUE = t -> true;

    public final Predicate ALWAYS_FALSE = t -> false;

    public <T, R> Function<T, R> identityFn() {
        return IDENTITY_FN;
    }

    public <T> Consumer<T> noOpConsumer() {
        return NO_OP_CONSUMER;
    }

    public <T, U> BiConsumer<T, U> noOpBiConsumer() {
        return NO_OP_BICONSUMER;
    }

    public <T> Predicate<T> alwaysTrue() {
        return ALWAYS_TRUE;
    }

    public <T> Predicate<T> alwaysFalse() {
        return ALWAYS_FALSE;
    }

    public <T> Supplier<T> nullSupplier() {
        return NULL_SUPPLIER;
    }

    public <T> Supplier<T> constantSupplier(T value) {
        return () -> value;
    }

    public <T, R> Function<T, R> constantFn(R value) {
        return _ -> value;
    }

    public <T, U, R> BiFunction<T, U, R> constantBiFn(R value) {
        return (_, _) -> value;
    }
}
