package com.gto.registrylib.util.map;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiMapTest {

    @Test
    void putAndGetRoundtripReturnsValue() {
        MultiMap<String, Integer> map = MultiMap.create(ArrayList::new);

        map.put("key", 42);

        Collection<Integer> values = map.get("key");
        assertTrue(values.contains(42));
        assertEquals(1, values.size());
    }

    @Test
    void multiplePutsWithSameKeyAccumulateValues() {
        MultiMap<String, String> map = MultiMap.create(ArrayList::new);

        map.put("fruits", "apple");
        map.put("fruits", "banana");
        map.put("fruits", "cherry");

        Collection<String> values = map.get("fruits");
        assertEquals(3, values.size());
        assertTrue(values.contains("apple"));
        assertTrue(values.contains("banana"));
        assertTrue(values.contains("cherry"));
    }

    @Test
    void getOnMissingKeyReturnsEmptyCollection() {
        MultiMap<String, String> map = MultiMap.create(ArrayList::new);

        Collection<String> values = map.get("nonexistent");

        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    void removeKeyValueReturnsTrueOnMatchFalseOnMiss() {
        MultiMap<String, Integer> map = MultiMap.create(ArrayList::new);
        map.put("key", 1);
        map.put("key", 2);

        assertTrue(map.remove("key", 1));
        assertFalse(map.remove("key", 99));
        assertFalse(map.remove("missing", 1));
    }

    @Test
    void removeKeyValueCleansUpEmptyInnerCollection() {
        MultiMap<String, Integer> map = MultiMap.create(ArrayList::new);
        map.put("key", 1);

        map.remove("key", 1);

        assertTrue(map.isEmpty(), "Map should be empty after removing the last value for a key");
        assertTrue(map.get("key").isEmpty());
    }

    @Test
    void removeKeyReturnsAllValuesForThatKey() {
        MultiMap<String, Integer> map = MultiMap.create(ArrayList::new);
        map.put("key", 10);
        map.put("key", 20);
        map.put("other", 30);

        Collection<Integer> removed = map.remove("key");

        assertEquals(2, removed.size());
        assertTrue(removed.contains(10));
        assertTrue(removed.contains(20));
        assertTrue(map.get("key").isEmpty());
    }

    @Test
    void removeKeyOnMissingKeyReturnsEmptyCollection() {
        MultiMap<String, String> map = MultiMap.create(ArrayList::new);

        Collection<String> removed = map.remove("nonexistent");

        assertNotNull(removed);
        assertTrue(removed.isEmpty());
    }

    @Test
    void isEmptyTransitionsCorrectly() {
        MultiMap<String, String> map = MultiMap.create(ArrayList::new);
        assertTrue(map.isEmpty(), "Newly created map should be empty");

        map.put("key", "value");
        assertFalse(map.isEmpty(), "Map with entries should not be empty");

        map.remove("key");
        assertTrue(map.isEmpty(), "Map should be empty after removing all entries");
    }

    @Test
    void containsKeyViaGetMapWorks() {
        MultiMap<String, Integer> map = MultiMap.create(ArrayList::new);

        assertFalse(map.getMap().containsKey("key"));

        map.put("key", 1);
        assertTrue(map.getMap().containsKey("key"));

        map.remove("key");
        assertFalse(map.getMap().containsKey("key"));
    }

    @Test
    void sizeReflectsNumberOfKeys() {
        MultiMap<String, Integer> map = MultiMap.create(ArrayList::new);
        assertEquals(0, map.getMap().size());

        map.put("a", 1);
        map.put("b", 2);
        map.put("a", 3);
        assertEquals(2, map.getMap().size(), "Two distinct keys should yield size 2");
    }

    @Test
    void clearRemovesAllEntries() {
        MultiMap<String, Integer> map = MultiMap.create(ArrayList::new);
        map.put("a", 1);
        map.put("b", 2);

        map.clear();

        assertTrue(map.isEmpty());
        assertTrue(map.get("a").isEmpty());
    }

    @Test
    void identityMapUsesReferenceEquality() {
        MultiMap<String, Integer> map = MultiMap.createIdentity(ArrayList::new);
        String key1 = new String("key");
        String key2 = new String("key");

        map.put(key1, 1);
        map.put(key2, 2);

        // Reference-identity map treats key1 and key2 as different keys
        assertEquals(
                2,
                map.getMap().size(),
                "Identity map should treat distinct String instances as separate keys");
        assertEquals(1, map.get(key1).size());
        assertEquals(1, map.get(key2).size());
    }
}
