package com.gto.registrylib.util.entry;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@code RegistryEntry} null-guard and convenience methods.
 *
 * <p>
 * Because the unit-test classpath does not include Minecraft, all access happens through
 * reflection. If Minecraft classes are unavailable the entire suite is skipped via JUnit
 * assumptions.
 */
class RegistryEntryTest {

    private static Class<?> entryClass;
    private static Constructor<?> entryCtor;

    @BeforeAll
    static void checkClasspathAndResolve() {
        try {
            entryClass = Class.forName("com.gto.registrylib.util.entry.RegistryEntry");
            Class<?> rkClass = Class.forName("net.minecraft.resources.ResourceKey");
            entryCtor = entryClass.getDeclaredConstructor(rkClass);
            entryCtor.setAccessible(true);
        } catch (ClassNotFoundException | NoClassDefFoundError | NoSuchMethodException e) {
            Assumptions.abort("Minecraft classes not on test classpath -- skipping RegistryEntry tests");
        }
    }

    private static Object newEntry() throws Exception {
        return entryCtor.newInstance((Object) null);
    }

    private static void bound(Object entry, Object value) throws Exception {
        Method m = entryClass.getMethod("bound", Object.class);
        m.invoke(entry, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T callGet(Object entry) {
        return (T) ((Supplier<?>) entry).get();
    }

    private static void callGetExpectIllegalState(Object entry) throws Exception {
        try {
            ((Supplier<?>) entry).get();
            throw new AssertionError("Expected IllegalStateException from get() but none was thrown");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("has not been bound yet"), expected.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static Optional<?> callGetOptional(Object entry) throws Exception {
        Method m = entryClass.getMethod("getOptional");
        return (Optional<?>) m.invoke(entry);
    }

    private static boolean callIsBound(Object entry) throws Exception {
        Method m = entryClass.getMethod("isBound");
        return (boolean) m.invoke(entry);
    }

    private static boolean callIs(Object entry, Object value) throws Exception {
        Method m = entryClass.getMethod("is", Object.class);
        return (boolean) m.invoke(entry, value);
    }

    @Test
    void getReturnsValueAfterBound() throws Exception {
        Object entry = newEntry();
        String val = "hello";
        bound(entry, val);

        assertSame(val, callGet(entry));
    }

    @Test
    void getThrowsBeforeBound() throws Exception {
        Object entry = newEntry();
        callGetExpectIllegalState(entry);
    }

    @Test
    void getOptionalReturnsValueAfterBound() throws Exception {
        Object entry = newEntry();
        bound(entry, "world");

        assertEquals(Optional.of("world"), callGetOptional(entry));
    }

    @Test
    void getOptionalReturnsEmptyBeforeBound() throws Exception {
        Object entry = newEntry();

        assertEquals(Optional.empty(), callGetOptional(entry));
    }

    @Test
    void isBoundReturnsFalseThenTrue() throws Exception {
        Object entry = newEntry();

        assertFalse(callIsBound(entry));

        bound(entry, "value");

        assertTrue(callIsBound(entry));
    }

    @Test
    void isReturnsTrueForSameReference() throws Exception {
        Object entry = newEntry();
        String ref = "same";
        bound(entry, ref);

        assertTrue(callIs(entry, ref));
    }

    @Test
    void isReturnsFalseForDifferentReference() throws Exception {
        Object entry = newEntry();
        bound(entry, "one");

        assertFalse(callIs(entry, new String("one")));
    }
}
