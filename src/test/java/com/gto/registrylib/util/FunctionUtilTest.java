package com.gto.registrylib.util;

import org.junit.jupiter.api.Test;

import java.util.function.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunctionUtilTest {

    @Test
    void noOpConsumerAcceptsValueWithoutError() {
        Consumer<String> consumer = FunctionUtil.noOpConsumer();

        consumer.accept("anything");
        consumer.accept(null);
    }

    @Test
    void noOpBiConsumerAcceptsValuesWithoutError() {
        BiConsumer<String, Integer> consumer = FunctionUtil.noOpBiConsumer();

        consumer.accept("key", 42);
        consumer.accept(null, null);
    }

    @Test
    void alwaysTrueReturnsTrueForAnyInput() {
        Predicate<Object> pred = FunctionUtil.alwaysTrue();

        assertTrue(pred.test("hello"));
        assertTrue(pred.test(42));
        assertTrue(pred.test(null));
    }

    @Test
    void alwaysFalseReturnsFalseForAnyInput() {
        Predicate<Object> pred = FunctionUtil.alwaysFalse();

        assertFalse(pred.test("hello"));
        assertFalse(pred.test(42));
        assertFalse(pred.test(null));
    }

    @Test
    void identityFnReturnsInputUnchanged() {
        Function<String, String> fn = FunctionUtil.identityFn();

        String input = "test";
        assertSame(input, fn.apply(input));
    }

    @Test
    void identityUnaryOpReturnsInputUnchanged() {
        UnaryOperator<String> op = FunctionUtil.identityUnaryOp();

        String input = "test";
        assertSame(input, op.apply(input));
    }

    @Test
    void constantFnReturnsConstantRegardlessOfInput() {
        Object constant = new Object();
        Function<String, Object> fn = FunctionUtil.constantFn(constant);

        assertSame(constant, fn.apply("a"));
        assertSame(constant, fn.apply("b"));
        assertSame(constant, fn.apply(null));
    }

    @Test
    void constantSupplierReturnsConstant() {
        Object constant = new Object();
        Supplier<Object> supplier = FunctionUtil.constantSupplier(constant);

        assertSame(constant, supplier.get());
        assertSame(constant, supplier.get());
    }

    @Test
    void nullSupplierReturnsNull() {
        Supplier<Object> supplier = FunctionUtil.nullSupplier();

        assertNull(supplier.get());
        assertNull(supplier.get());
    }

    @Test
    void constantBiFnReturnsConstantRegardlessOfInputs() {
        Object constant = new Object();
        BiFunction<String, Integer, Object> fn = FunctionUtil.constantBiFn(constant);

        assertSame(constant, fn.apply("a", 1));
        assertSame(constant, fn.apply(null, null));
    }

    @Test
    void identityFunctionsReturnSameInstanceOnRepeatedCalls() {
        assertSame(FunctionUtil.identityFn(), FunctionUtil.identityFn());
        assertSame(FunctionUtil.identityUnaryOp(), FunctionUtil.identityUnaryOp());
        assertSame(FunctionUtil.noOpConsumer(), FunctionUtil.noOpConsumer());
        assertSame(FunctionUtil.noOpBiConsumer(), FunctionUtil.noOpBiConsumer());
        assertSame(FunctionUtil.alwaysTrue(), FunctionUtil.alwaysTrue());
        assertSame(FunctionUtil.alwaysFalse(), FunctionUtil.alwaysFalse());
        assertSame(FunctionUtil.nullSupplier(), FunctionUtil.nullSupplier());
    }

    @Test
    void singletonAccessorsBackedByStaticFields() {
        assertSame(FunctionUtil.IDENTITY_FN, FunctionUtil.identityFn());
        assertSame(FunctionUtil.IDENTITY_UNARY_OP, FunctionUtil.identityUnaryOp());
        assertSame(FunctionUtil.NO_OP_CONSUMER, FunctionUtil.noOpConsumer());
        assertSame(FunctionUtil.NO_OP_BICONSUMER, FunctionUtil.noOpBiConsumer());
        assertSame(FunctionUtil.ALWAYS_TRUE, FunctionUtil.alwaysTrue());
        assertSame(FunctionUtil.ALWAYS_FALSE, FunctionUtil.alwaysFalse());
        assertSame(FunctionUtil.NULL_SUPPLIER, FunctionUtil.nullSupplier());
    }
}
