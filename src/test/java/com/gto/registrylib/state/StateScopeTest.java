package com.gto.registrylib.state;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the {@link StateScope} enum.
 */
class StateScopeTest {

    @Test
    void worldIdReturnsWorld() {
        assertEquals("world", StateScope.WORLD.id());
    }

    @Test
    void chunkIdReturnsChunk() {
        assertEquals("chunk", StateScope.CHUNK.id());
    }

    @Test
    void allValuesHaveNonNullNonEmptyIds() {
        for (StateScope scope : StateScope.values()) {
            assertNotNull(scope.id(), scope.name() + " has a null id");
            assertFalse(scope.id().isEmpty(), scope.name() + " has an empty id");
        }
    }

    @Test
    void allIdsAreUnique() {
        Set<String> seen = new HashSet<>();
        for (StateScope scope : StateScope.values()) {
            boolean added = seen.add(scope.id());
            assertFalse(!added,
                    "Duplicate id '" + scope.id() + "' found on " + scope.name());
        }
        assertEquals(StateScope.values().length, seen.size(),
                "Number of unique ids must equal number of enum values");
    }
}
