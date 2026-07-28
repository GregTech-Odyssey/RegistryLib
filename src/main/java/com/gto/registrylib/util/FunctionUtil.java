package com.gto.registrylib.util;

import lombok.experimental.UtilityClass;

import java.util.function.*;

@UtilityClass
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FunctionUtil {

    public static final Supplier NULL_SUPPLIER = () -> null;

    public static final Function IDENTITY_FN = Function.identity();

    public static final UnaryOperator IDENTITY_UNARY_OP = UnaryOperator.identity();

    public static final Consumer NO_OP_CONSUMER = _ -> {};

    public static final BiConsumer NO_OP_BICONSUMER = (_, _) -> {};

    public static final Predicate ALWAYS_TRUE = t -> true;

    public static final Predicate ALWAYS_FALSE = t -> false;

    public <T, R> Function<T, R> identityFn() {
        return IDENTITY_FN;
    }

    public <T> UnaryOperator<T> identityUnaryOp() {
        return IDENTITY_UNARY_OP;
    }

    public <T> Consumer<T> noOpConsumer() {
        return NO_OP_CONSUMER;
    }

    public static <T> Consumer<T> noOpConsumerStatic() {
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
