package com.gto.registrylib.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class LazyTest {

    @Test
    void getReturnsDelegateValue() {
        Lazy<String> lazy = Lazy.of(() -> "hello");

        assertEquals("hello", lazy.get());
    }

    @Test
    void getReturnsSameInstanceOnRepeatedCalls() {
        Object sentinel = new Object();
        Lazy<Object> lazy = Lazy.of(() -> sentinel);

        Object first = lazy.get();
        Object second = lazy.get();
        Object third = lazy.get();

        assertSame(first, second);
        assertSame(second, third);
    }

    @Test
    void delegateIsInvokedExactlyOnce() {
        AtomicInteger counter = new AtomicInteger(0);
        Lazy<String> lazy = Lazy.of(
                () -> {
                    counter.incrementAndGet();
                    return "computed";
                });

        lazy.get();
        lazy.get();
        lazy.get();

        assertEquals(1, counter.get());
    }

    @Test
    void getFromMultipleThreadsProducesConsistentResult() throws InterruptedException {
        int threadCount = 10;
        Object sentinel = new Object();
        AtomicInteger counter = new AtomicInteger(0);
        Lazy<Object> lazy = Lazy.of(
                () -> {
                    counter.incrementAndGet();
                    return sentinel;
                });

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Object> results = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            results.add(null);
        }

        for (int i = 0; i < threadCount; i++) {
            int index = i;
            Thread t = new Thread(
                    () -> {
                        try {
                            startLatch.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        results.set(index, lazy.get());
                        doneLatch.countDown();
                    });
            t.start();
        }

        startLatch.countDown();
        doneLatch.await();

        assertEquals(1, counter.get(), "Delegate should be invoked exactly once across all threads");
        for (int i = 0; i < threadCount; i++) {
            assertSame(sentinel, results.get(i), "Thread " + i + " should see the same instance");
        }
    }

    @Test
    void getHandlesNullReturnFromDelegate() {
        AtomicInteger counter = new AtomicInteger(0);
        Lazy<String> lazy = Lazy.of(
                () -> {
                    counter.incrementAndGet();
                    return null;
                });

        assertNull(lazy.get());
        assertNull(lazy.get());
        assertEquals(
                1, counter.get(), "Delegate should still be invoked exactly once even when returning null");
    }
}
