package com.gto.registrylib.util.registry;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListRegistryTest {

    @Test
    void addAndIterateWorks() {
        ListRegistry<String> registry = new ListRegistry<>();

        registry.add("alpha");
        registry.add("beta");
        registry.add("gamma");

        List<String> collected = new ArrayList<>();
        registry.forEach(collected::add);

        assertEquals(3, collected.size());
        assertEquals("alpha", collected.get(0));
        assertEquals("beta", collected.get(1));
        assertEquals("gamma", collected.get(2));
    }

    @Test
    void streamReturnsAllElements() {
        ListRegistry<Integer> registry = new ListRegistry<>();
        registry.add(1);
        registry.add(2);
        registry.add(3);

        List<Integer> streamed = registry.stream().toList();

        assertEquals(List.of(1, 2, 3), streamed);
    }

    @Test
    void sizeReflectsNumberOfEntries() {
        ListRegistry<String> registry = new ListRegistry<>();
        assertEquals(0, registry.size());

        registry.add("one");
        assertEquals(1, registry.size());

        registry.add("two");
        assertEquals(2, registry.size());
    }

    @Test
    void frozenRegistryRejectsAdd() {
        ListRegistry<String> registry = new ListRegistry<>();
        registry.add("item");

        // freeze via consume() — the public API that triggers freezing
        registry.consume(e -> {});

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add("should fail"));
        assertTrue(ex.getMessage().contains("frozen"));
    }

    @Test
    void consumeIteratesAllElements() {
        ListRegistry<String> registry = new ListRegistry<>();
        registry.add("x");
        registry.add("y");

        List<String> consumed = new ArrayList<>();
        registry.consume(consumed::add);

        assertEquals(List.of("x", "y"), consumed);
    }

    @Test
    void consumeFreezesRegistry() {
        ListRegistry<String> registry = new ListRegistry<>();
        registry.add("item");

        registry.consume(e -> {});

        assertTrue(registry.isFrozen());
        assertThrows(IllegalStateException.class, () -> registry.add("should fail"));
    }

    @Test
    void clearMakesRegistryFrozen() {
        ListRegistry<String> registry = new ListRegistry<>();
        registry.add("item");

        registry.clear();

        assertTrue(registry.isFrozen());
        assertEquals(0, registry.size());
    }

    @Test
    void clearThenAddThrows() {
        ListRegistry<String> registry = new ListRegistry<>();
        registry.add("item");
        registry.clear();

        assertThrows(IllegalStateException.class, () -> registry.add("nope"));
    }

    @Test
    void alreadyFrozenClearThrows() {
        ListRegistry<String> registry = new ListRegistry<>();
        registry.add("item");

        // freeze via clear()
        registry.clear();

        // second clear should throw because registry is already frozen
        assertThrows(IllegalStateException.class, registry::clear);
    }

    @Test
    void iteratorWorks() {
        ListRegistry<String> registry = new ListRegistry<>();
        registry.add("a");
        registry.add("b");

        List<String> collected = new ArrayList<>();
        for (String s : registry) {
            collected.add(s);
        }

        assertEquals(List.of("a", "b"), collected);
    }
}
