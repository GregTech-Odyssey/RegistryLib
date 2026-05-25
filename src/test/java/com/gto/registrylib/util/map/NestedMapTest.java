package com.gto.registrylib.util.map;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NestedMapTest {

    @Test
    void twoLevelPutAndGetRoundtrip() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);

        map.put("outer", "inner", 42);

        assertEquals(42, map.get("outer", "inner"));
    }

    @Test
    void getReturnsNullForMissingInnerKey() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        map.put("outer", "exists", 1);

        assertNull(map.get("outer", "missing"));
    }

    @Test
    void getReturnsEmptyMapForMissingOuterKey() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);

        Map<String, Integer> result = map.get("nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getReturnsBothLevelsCorrectly() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        map.put("outer", "a", 1);
        map.put("outer", "b", 2);

        assertEquals(1, map.get("outer", "a"));
        assertEquals(2, map.get("outer", "b"));

        Map<String, Integer> innerMap = map.get("outer");
        assertEquals(2, innerMap.size());
    }

    @Test
    void removeInnerKeyRemovesEntry() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        map.put("outer", "inner", 42);

        Integer removed = map.remove("outer", "inner");

        assertEquals(42, removed);
        assertNull(map.get("outer", "inner"));
    }

    @Test
    void removeInnerKeyCleansUpEmptyInnerMap() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        map.put("outer", "only", 1);

        map.remove("outer", "only");

        assertTrue(map.isEmpty(), "Outer map should be cleaned up when inner map becomes empty");
    }

    @Test
    void removeInnerKeyDoesNotCleanUpNonEmptyInnerMap() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        map.put("outer", "a", 1);
        map.put("outer", "b", 2);

        map.remove("outer", "a");

        assertFalse(map.isEmpty());
        assertEquals(2, map.get("outer", "b"));
    }

    @Test
    void removeOnMissingKeysReturnsNull() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);

        assertNull(map.remove("missing", "inner"));
    }

    @Test
    void removeOuterKeyReturnsInnerMap() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        map.put("outer", "a", 1);
        map.put("outer", "b", 2);

        Map<String, Integer> removed = map.remove("outer");

        assertEquals(2, removed.size());
        assertTrue(removed.containsKey("a"));
        assertTrue(removed.containsKey("b"));
        assertTrue(map.isEmpty());
    }

    @Test
    void removeOuterKeyOnMissingReturnsEmptyMap() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);

        Map<String, Integer> removed = map.remove("nonexistent");

        assertNotNull(removed);
        assertTrue(removed.isEmpty());
    }

    @Test
    void computeIfAbsentCreatesValueLazily() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);

        Integer value = map.computeIfAbsent("outer", "inner", k -> 99);

        assertEquals(99, value);
        assertEquals(99, map.get("outer", "inner"));
    }

    @Test
    void computeIfAbsentReturnsExistingValue() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        map.put("outer", "inner", 42);

        Integer value = map.computeIfAbsent("outer", "inner", k -> 99);

        assertEquals(42, value, "Existing value should be returned, not recomputed");
    }

    @Test
    void putOverwritesPreviousValue() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        map.put("outer", "inner", 1);

        Integer old = map.put("outer", "inner", 2);

        assertEquals(1, old);
        assertEquals(2, map.get("outer", "inner"));
    }

    @Test
    void isEmptyTransitionsCorrectly() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        assertTrue(map.isEmpty());

        map.put("o", "i", 1);
        assertFalse(map.isEmpty());

        map.remove("o", "i");
        assertTrue(map.isEmpty());
    }

    @Test
    void clearRemovesAllEntries() {
        NestedMap<String, String, Integer> map = NestedMap.create(HashMap::new);
        map.put("a", "x", 1);
        map.put("b", "y", 2);

        map.clear();

        assertTrue(map.isEmpty());
        assertNull(map.get("a", "x"));
    }
}
