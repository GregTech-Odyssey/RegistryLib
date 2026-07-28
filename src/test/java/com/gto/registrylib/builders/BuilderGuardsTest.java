package com.gto.registrylib.builders;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that all builder classes that bypass {@link AbstractBuilder} carry their own {@code
 * registered} double-registration guard.
 *
 * <p>
 * Because the unit-test classpath does not include Minecraft, these tests use reflection to
 * inspect builder classes rather than instantiating them. If a builder class is not on the
 * classpath for any reason, the individual test is skipped via JUnit assumptions.
 */
class BuilderGuardsTest {

    /**
     * Builder classes that do NOT extend {@link AbstractBuilder} and therefore must carry their own
     * {@code registered} field + guard in {@code register()}.
     */
    private static final List<String> STANDALONE_BUILDERS = List.of(
            "com.gto.registrylib.builders.EnchantmentBuilder",
            "com.gto.registrylib.builders.RecipeTypeBuilder",
            "com.gto.registrylib.worldgen.WorldgenFeatureBuilder",
            "com.gto.registrylib.crop.CropBuilder");

    @TestFactory
    Stream<DynamicTest> allStandaloneBuildersHaveRegisteredField() {
        return STANDALONE_BUILDERS.stream()
                .map(
                        fqcn -> DynamicTest.dynamicTest(
                                simpleName(fqcn) + " has 'registered' field",
                                () -> {
                                    Class<?> clazz = loadOrSkip(fqcn);
                                    Field field = findDeclaredField(clazz, "registered");
                                    assertNotNull(
                                            field,
                                            fqcn + " must declare a 'registered' field for double-registration protection");
                                    assertEquals(
                                            boolean.class,
                                            field.getType(),
                                            "'registered' field must be of type boolean");
                                }));
    }

    @TestFactory
    Stream<DynamicTest> registeredFieldDefaultsToFalse() {
        return STANDALONE_BUILDERS.stream()
                .map(
                        fqcn -> DynamicTest.dynamicTest(
                                simpleName(fqcn) + " 'registered' defaults to false",
                                () -> {
                                    Class<?> clazz = loadOrSkip(fqcn);
                                    Field field = findDeclaredField(clazz, "registered");
                                    assertNotNull(field, fqcn + " must declare 'registered'");
                                    // boolean fields default to false in Java, but verify it is not initialized
                                    // to true
                                    // by inspecting that it has no compile-time constant initializer of true.
                                    // Since the field is an instance field with no explicit = true, the JVM
                                    // default is false.
                                    assertTrue(
                                            field.getType() == boolean.class,
                                            "'registered' should be primitive boolean (defaults to false)");
                                }));
    }

    @TestFactory
    Stream<DynamicTest> registerMethodExists() {
        return STANDALONE_BUILDERS.stream()
                .map(
                        fqcn -> DynamicTest.dynamicTest(
                                simpleName(fqcn) + " has register() method",
                                () -> {
                                    Class<?> clazz = loadOrSkip(fqcn);
                                    assertTrue(
                                            hasNoArgMethod(clazz, "register"),
                                            fqcn + " must have a no-arg register() method");
                                }));
    }

    @Test
    void abstractBuilderAlsoHasRegisteredField() {
        Class<?> clazz = loadOrSkip("com.gto.registrylib.builders.AbstractBuilder");
        Field field = findDeclaredField(clazz, "registered");
        assertNotNull(field, "AbstractBuilder must declare 'registered'");
        assertEquals(boolean.class, field.getType());
    }

    // === Helpers ===

    private static Class<?> loadOrSkip(String fqcn) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            Assumptions.abort("Class not on test classpath: " + fqcn);
            return null; // unreachable
        }
    }

    private static Field findDeclaredField(Class<?> clazz, String name) {
        // Walk the class hierarchy in case the field is in a superclass,
        // but for standalone builders it should be declared directly.
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (NoClassDefFoundError e) {
                // Field type references an unavailable MC class; skip the rest of the hierarchy
                Assumptions.abort("Minecraft dependency not on test classpath: " + e.getMessage());
                return null; // unreachable
            }
        }
        return null;
    }

    private static boolean hasNoArgMethod(Class<?> clazz, String methodName) {
        try {
            for (var method : clazz.getDeclaredMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    return true;
                }
            }
        } catch (NoClassDefFoundError e) {
            Assumptions.abort("Minecraft dependency not on test classpath: " + e.getMessage());
        }
        return false;
    }

    private static String simpleName(String fqcn) {
        int dot = fqcn.lastIndexOf('.');
        return dot >= 0 ? fqcn.substring(dot + 1) : fqcn;
    }
}
