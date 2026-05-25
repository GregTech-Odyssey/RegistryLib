package com.gto.registrylib.util.registry;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreezableRegistryTest {

    @Test
    void registerAndGetRoundtrip() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();

        reg.register("a", 1);
        reg.register("b", 2);

        assertEquals(1, reg.get("a"));
        assertEquals(2, reg.get("b"));
    }

    @Test
    void getReturnsNullForMissingKey() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();

        assertNull(reg.get("missing"));
    }

    @Test
    void getOrThrowReturnsValueForPresentKey() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("x", 42);

        assertEquals(42, reg.getOrThrow("x"));
    }

    @Test
    void getOrThrowThrowsForMissingKey() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> reg.getOrThrow("missing"));
        assertTrue(ex.getMessage().contains("missing"));
    }

    @Test
    void getOptionalReturnsPresentForExistingKey() {
        FreezableRegistry<String, String> reg = FreezableRegistry.create();
        reg.register("k", "v");

        Optional<String> result = reg.getOptional("k");
        assertTrue(result.isPresent());
        assertEquals("v", result.get());
    }

    @Test
    void getOptionalReturnsEmptyForMissingKey() {
        FreezableRegistry<String, String> reg = FreezableRegistry.create();

        Optional<String> result = reg.getOptional("absent");
        assertTrue(result.isEmpty());
    }

    @Test
    void duplicateKeyThrowsIllegalArgument() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("dup", 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> reg.register("dup", 2));
        assertTrue(ex.getMessage().contains("dup"));
    }

    @Test
    void registerAfterFreezeThrowsIllegalState() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("a", 1);
        reg.freeze();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> reg.register("b", 2));
        assertTrue(ex.getMessage().contains("frozen"));
    }

    @Test
    void freezeTwiceThrowsIllegalState() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.freeze();

        IllegalStateException ex = assertThrows(IllegalStateException.class, reg::freeze);
        assertTrue(ex.getMessage().contains("already frozen"));
    }

    @Test
    void isFrozenReflectsState() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();

        assertFalse(reg.isFrozen());
        reg.freeze();
        assertTrue(reg.isFrozen());
    }

    @Test
    void valuesReturnsUnmodifiableCollection() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("a", 1);

        Collection<Integer> vals = reg.values();
        assertThrows(UnsupportedOperationException.class, () -> vals.add(99));
    }

    @Test
    void entriesReturnsUnmodifiableSet() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("a", 1);

        Set<Map.Entry<String, Integer>> ent = reg.entries();
        assertThrows(UnsupportedOperationException.class, () -> ent.add(Map.entry("b", 2)));
    }

    @Test
    void keysReturnsUnmodifiableSet() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("a", 1);

        Set<String> ks = reg.keys();
        assertThrows(UnsupportedOperationException.class, () -> ks.add("b"));
    }

    @Test
    void forEachIteratesAllEntries() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("a", 1);
        reg.register("b", 2);
        reg.register("c", 3);

        List<String> keys = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        reg.forEach(
                (k, v) -> {
                    keys.add(k);
                    values.add(v);
                });

        assertEquals(3, keys.size());
        assertTrue(keys.containsAll(List.of("a", "b", "c")));
        assertTrue(values.containsAll(List.of(1, 2, 3)));
    }

    @Test
    void createOrderedPreservesInsertionOrder() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.createOrdered();
        reg.register("c", 3);
        reg.register("a", 1);
        reg.register("b", 2);

        List<String> keyOrder = new ArrayList<>(reg.keys());
        assertEquals(List.of("c", "a", "b"), keyOrder);
    }

    @Test
    void isEmptyAndSizeTransitions() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();

        assertTrue(reg.isEmpty());
        assertEquals(0, reg.size());

        reg.register("first", 1);

        assertFalse(reg.isEmpty());
        assertEquals(1, reg.size());

        reg.register("second", 2);
        assertEquals(2, reg.size());
    }

    @Test
    void containsTrueForPresentKeyFalseForAbsent() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("present", 1);

        assertTrue(reg.contains("present"));
        assertFalse(reg.contains("absent"));
    }

    @Test
    void getStillWorksAfterFreeze() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("a", 10);
        reg.freeze();

        assertEquals(10, reg.get("a"));
        assertEquals(10, reg.getOrThrow("a"));
        assertTrue(reg.getOptional("a").isPresent());
    }

    @Test
    void registerNullKeyThrowsNpe() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();

        assertThrows(NullPointerException.class, () -> reg.register(null, 1));
    }

    @Test
    void registerNullValueThrowsNpe() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();

        assertThrows(NullPointerException.class, () -> reg.register("k", null));
    }

    @Test
    void frozenRegistryValuesAreUnmodifiable() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        reg.register("a", 1);
        reg.freeze();

        assertThrows(UnsupportedOperationException.class, () -> reg.values().add(2));
        assertThrows(UnsupportedOperationException.class, () -> reg.keys().add("b"));
        assertThrows(UnsupportedOperationException.class, () -> reg.entries().add(Map.entry("b", 2)));
    }

    @Test
    void createOrderedPreservesOrderAfterFreeze() {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.createOrdered();
        reg.register("z", 26);
        reg.register("m", 13);
        reg.register("a", 1);
        reg.freeze();

        List<String> keyOrder = new ArrayList<>(reg.keys());
        assertEquals(List.of("z", "m", "a"), keyOrder);
    }

    // -----------------------------------------------------------------------
    // Concurrent access tests
    // -----------------------------------------------------------------------

    @Test
    void concurrentRegistrationOfDifferentKeys() throws Exception {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        int threadCount = 10;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(
                    () -> {
                        try {
                            ready.countDown();
                            go.await();
                            reg.register("key-" + threadId, threadId);
                        } catch (InterruptedException ignored) {
                        } catch (Throwable e) {
                            errors.add(e);
                        } finally {
                            done.countDown();
                        }
                    },
                    "reg-concurrent-" + t)
                    .start();
        }

        ready.await();
        go.countDown();
        done.await();

        assertTrue(errors.isEmpty(),
                "No errors expected during concurrent registration but got: " + errors);
        assertEquals(threadCount, reg.size(),
                "All " + threadCount + " entries must be present");
        for (int t = 0; t < threadCount; t++) {
            assertEquals(t, reg.get("key-" + t),
                    "key-" + t + " must map to " + t);
        }
    }

    @Test
    void concurrentReadsAfterFreeze() throws Exception {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        for (int i = 0; i < 100; i++) {
            reg.register("k" + i, i);
        }
        reg.freeze();

        int threadCount = 10;
        int readsPerThread = 500;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int t = 0; t < threadCount; t++) {
            new Thread(
                    () -> {
                        try {
                            ready.countDown();
                            go.await();
                            for (int r = 0; r < readsPerThread; r++) {
                                for (int i = 0; i < 100; i++) {
                                    Integer val = reg.get("k" + i);
                                    if (val == null || val != i) {
                                        throw new AssertionError(
                                                "Expected " + i + " for k" + i + " but got " + val);
                                    }
                                }
                            }
                        } catch (InterruptedException ignored) {
                        } catch (Throwable e) {
                            errors.add(e);
                        } finally {
                            done.countDown();
                        }
                    },
                    "reg-read-" + t)
                    .start();
        }

        ready.await();
        go.countDown();
        done.await();

        assertTrue(errors.isEmpty(),
                "No errors expected during concurrent reads but got: " + errors);
    }

    @Test
    void concurrentReadsDuringRegistration() throws Exception {
        FreezableRegistry<String, Integer> reg = FreezableRegistry.create();
        // Pre-populate some entries so readers have something to read
        for (int i = 0; i < 50; i++) {
            reg.register("pre-" + i, i);
        }

        int writerCount = 5;
        int readerCount = 5;
        int totalThreads = writerCount + readerCount;
        CountDownLatch ready = new CountDownLatch(totalThreads);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(totalThreads);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        // Writers register new keys
        for (int w = 0; w < writerCount; w++) {
            final int writerId = w;
            new Thread(
                    () -> {
                        try {
                            ready.countDown();
                            go.await();
                            for (int i = 0; i < 10; i++) {
                                reg.register("w" + writerId + "-" + i, writerId * 10 + i);
                            }
                        } catch (InterruptedException ignored) {
                        } catch (Throwable e) {
                            errors.add(e);
                        } finally {
                            done.countDown();
                        }
                    },
                    "reg-writer-" + w)
                    .start();
        }

        // Readers read pre-existing keys concurrently
        for (int r = 0; r < readerCount; r++) {
            new Thread(
                    () -> {
                        try {
                            ready.countDown();
                            go.await();
                            for (int iter = 0; iter < 200; iter++) {
                                for (int i = 0; i < 50; i++) {
                                    Integer val = reg.get("pre-" + i);
                                    if (val == null || val != i) {
                                        throw new AssertionError(
                                                "Expected " + i + " for pre-" + i + " but got " + val);
                                    }
                                }
                            }
                        } catch (InterruptedException ignored) {
                        } catch (Throwable e) {
                            errors.add(e);
                        } finally {
                            done.countDown();
                        }
                    },
                    "reg-reader-" + r)
                    .start();
        }

        ready.await();
        go.countDown();
        done.await();

        assertTrue(errors.isEmpty(),
                "No errors expected during concurrent reads/writes but got: " + errors);

        // Verify all writer entries are present
        for (int w = 0; w < writerCount; w++) {
            for (int i = 0; i < 10; i++) {
                assertEquals(w * 10 + i, reg.get("w" + w + "-" + i),
                        "Writer " + w + " entry " + i + " must be present");
            }
        }
    }
}
