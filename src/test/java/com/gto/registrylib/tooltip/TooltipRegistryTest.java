package com.gto.registrylib.tooltip;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the thread-safety of {@link TooltipRegistry}'s internal collector.
 *
 * <p>
 * {@link TooltipRegistry} has a static initializer that depends on Minecraft runtime classes
 * ({@code Item}, {@code ItemStack}, etc.). On a plain unit-test classpath these classes are absent,
 * so loading {@code TooltipRegistry} throws {@link NoClassDefFoundError}. All tests in this class
 * therefore use {@link Assumptions#assumeTrue} to gracefully skip when the MC environment is not
 * available.
 *
 * <p>
 * Additionally, a set of "standalone" tests exercises {@link TooltipNodeCollector} directly (no
 * MC dependency) to verify that per-thread isolation prevents data corruption. These tests
 * demonstrate the correctness of the ThreadLocal pattern used in {@code TooltipRegistry.resolve()}.
 */
class TooltipRegistryTest {

    /**
     * Reflectively obtained ThreadLocal field, or {@code null} if {@code TooltipRegistry} cannot be
     * loaded (no MC runtime).
     */
    private static ThreadLocal<TooltipNodeCollector> threadLocalCollector;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void tryLoadRegistry() {
        try {
            Class<?> clazz = Class.forName("com.gto.registrylib.tooltip.TooltipRegistry");
            Field tlField = clazz.getDeclaredField("threadLocalCollector");
            tlField.setAccessible(true);
            threadLocalCollector = (ThreadLocal<TooltipNodeCollector>) tlField.get(null);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // MC runtime not on classpath — the reflection-based tests will be skipped.
            threadLocalCollector = null;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unexpected reflection error", e);
        }
    }

    // -----------------------------------------------------------------------
    // Tests that require TooltipRegistry to load (MC runtime on classpath)
    // -----------------------------------------------------------------------

    @Test
    void threadLocalCollectorReturnsSeparateInstancesPerThread() throws Exception {
        Assumptions.assumeTrue(
                threadLocalCollector != null,
                "Skipped: TooltipRegistry cannot load without Minecraft runtime classes");

        int threadCount = 10;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        List<TooltipNodeCollector> collectors = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            collectors.add(null);
        }

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            new Thread(
                    () -> {
                        try {
                            ready.countDown();
                            go.await();
                            collectors.set(idx, threadLocalCollector.get());
                        } catch (InterruptedException ignored) {} finally {
                            done.countDown();
                        }
                    },
                    "tooltip-test-" + i)
                    .start();
        }

        ready.await();
        go.countDown();
        done.await();

        for (int i = 0; i < threadCount; i++) {
            for (int j = i + 1; j < threadCount; j++) {
                assertNotSame(
                        collectors.get(i),
                        collectors.get(j),
                        "Threads " + i + " and " + j + " must not share a collector instance");
            }
        }
    }

    @Test
    void threadLocalCollectorReusesSameInstanceWithinThread() {
        Assumptions.assumeTrue(
                threadLocalCollector != null,
                "Skipped: TooltipRegistry cannot load without Minecraft runtime classes");

        TooltipNodeCollector first = threadLocalCollector.get();
        TooltipNodeCollector second = threadLocalCollector.get();
        assertTrue(first == second, "ThreadLocal must return the same instance within one thread");
    }

    // -----------------------------------------------------------------------
    // Standalone tests — exercise TooltipNodeCollector directly, no MC needed
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the ThreadLocal pattern used in production: each thread gets its own collector,
     * clears it before use, populates it, and reads back exactly the data it wrote — with no
     * cross-thread contamination.
     */
    @Test
    void concurrentCollectorUsageDoesNotCorrupt() throws Exception {
        ThreadLocal<TooltipNodeCollector> tl = ThreadLocal.withInitial(TooltipNodeCollector::new);
        RootNodeRef dummyRef = new RootNodeRef("test:dummy");

        int threadCount = 10;
        int iterations = 200;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(
                    () -> {
                        try {
                            ready.countDown();
                            go.await();
                            for (int i = 0; i < iterations; i++) {
                                TooltipNodeCollector c = tl.get();
                                c.nodesByRoot.clear();

                                int expected = threadId + 1;
                                for (int n = 0; n < expected; n++) {
                                    c.nodesByRoot
                                            .computeIfAbsent(dummyRef, k -> new ArrayList<>())
                                            .add(new TooltipNodeCollector.NodeEntry(null, false, false));
                                }

                                int actual = c.nodesByRoot.getOrDefault(dummyRef, List.of()).size();
                                if (actual != expected) {
                                    errorCount.incrementAndGet();
                                }
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        } finally {
                            done.countDown();
                        }
                    },
                    "tooltip-stress-" + t)
                    .start();
        }

        ready.await();
        go.countDown();
        done.await();

        assertEquals(
                0,
                errorCount.get(),
                "No data corruption should occur across " + threadCount + " concurrent threads");
    }

    /**
     * Verify that a ThreadLocal<TooltipNodeCollector> returns distinct instances across threads but
     * the same instance on repeated access within a single thread.
     */
    @Test
    void standaloneThreadLocalIsolation() throws Exception {
        ThreadLocal<TooltipNodeCollector> tl = ThreadLocal.withInitial(TooltipNodeCollector::new);

        // Same-thread reuse
        TooltipNodeCollector a = tl.get();
        TooltipNodeCollector b = tl.get();
        assertTrue(a == b, "Same thread must receive the same collector instance");

        // Cross-thread isolation
        TooltipNodeCollector[] other = new TooltipNodeCollector[1];
        Thread t = new Thread(() -> other[0] = tl.get(), "tooltip-isolation");
        t.start();
        t.join();

        assertNotSame(a, other[0], "Different threads must receive different collector instances");
    }
}
