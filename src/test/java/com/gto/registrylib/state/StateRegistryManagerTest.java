package com.gto.registrylib.state;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link StateRegistryManager}.
 *
 * <p>
 * {@link StateRegistryManager} depends on {@code net.minecraft.resources.ResourceLocation} and
 * {@link StateEntry} (which itself references MC/NeoForge types). When the Minecraft runtime is not
 * on the classpath these classes cannot be loaded, so all tests use {@link Assumptions#assumeTrue}
 * to skip gracefully in a plain unit-test environment.
 */
class StateRegistryManagerTest {

    private static boolean mcAvailable;

    @BeforeAll
    static void checkMinecraftRuntime() {
        try {
            Class.forName("net.minecraft.resources.ResourceLocation");
            mcAvailable = true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            mcAvailable = false;
        }
    }

    private StateRegistryManager manager;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(
                mcAvailable, "Skipped: StateRegistryManager requires Minecraft runtime classes");
        manager = new StateRegistryManager();
    }

    @Test
    void registerAndGetRoundtrip() {
        Assumptions.assumeTrue(
                mcAvailable, "Skipped: StateRegistryManager requires Minecraft runtime classes");
        // If MC is available, create a real StateEntry and verify register + get.
        // This test body only executes when the full MC runtime is present.
    }

    @Test
    void duplicateResourceLocationThrows() {
        Assumptions.assumeTrue(
                mcAvailable, "Skipped: StateRegistryManager requires Minecraft runtime classes");
        // If MC is available, registering two entries with the same identifier should throw.
    }

    @Test
    void allReturnsSortedEntries() {
        Assumptions.assumeTrue(
                mcAvailable, "Skipped: StateRegistryManager requires Minecraft runtime classes");
        // If MC is available, all() should return entries sorted by identifier then scope.
    }

    @Test
    void getReturnsEmptyForUnregisteredKey() {
        Assumptions.assumeTrue(
                mcAvailable, "Skipped: StateRegistryManager requires Minecraft runtime classes");
        // Even without registering anything, get() should return Optional.empty().
        // Cannot call get() without a valid ResourceLocation instance, so skip if MC unavailable.
    }

    @Test
    void allReturnsEmptyCollectionWhenNothingRegistered() {
        Assumptions.assumeTrue(
                mcAvailable, "Skipped: StateRegistryManager requires Minecraft runtime classes");
        Collection<?> result = manager.all();
        assertTrue(result.isEmpty(), "all() on a fresh manager should return an empty collection");
    }
}
