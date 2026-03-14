package com.gto.registrylib.util.map;

import com.google.common.base.Supplier;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceFunction;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface NestedMap<K1, K2, V> {

    static <K1, K2, V> NestedMap<K1, K2, V> create(
                                                   Map<K1, Map<K2, V>> map, Supplier<Map<K2, V>> factory) {
        return new NestedMapWrapper<>(map, factory);
    }

    static <K1, K2, V> NestedMap<K1, K2, V> create(Supplier<Map<K2, V>> factory) {
        return new NestedMapWrapper<>(new HashMap<>(), factory);
    }

    static <K1, K2, V> NestedMap<K1, K2, V> createIdentity(Supplier<Map<K2, V>> factory) {
        return new NestedMapWrapper<>(new Reference2ReferenceOpenHashMap<>(), factory);
    }

    Map<K1, Map<K2, V>> getMap();

    V get(Object k1, Object k2);

    Map<K2, V> get(Object k1);

    V put(K1 k1, K2 k2, V value);

    V computeIfAbsent(
                      K1 k1, K2 k2, Reference2ReferenceFunction<? super K2, ? extends V> mappingFunction);

    V remove(Object k1, Object k2);

    Map<K2, V> remove(Object k1);

    boolean isEmpty();

    void clear();

    class NestedMapWrapper<K1, K2, V> implements NestedMap<K1, K2, V> {

        @Getter
        private final Map<K1, Map<K2, V>> map;
        private final Reference2ReferenceFunction<Object, Map<K2, V>> factory;
        private final boolean isRefMap;
        private boolean isInnerRefMap;

        private NestedMapWrapper(Map<K1, Map<K2, V>> map, Supplier<Map<K2, V>> factory) {
            this.map = map;
            this.factory = unusedKey -> {
                var innerRefMap = factory.get();
                if (innerRefMap instanceof Reference2ReferenceMap) {
                    isInnerRefMap = true;
                }
                return innerRefMap;
            };
            this.isRefMap = map instanceof Reference2ReferenceMap;
        }

        @Override
        public Map<K2, V> get(Object k1) {
            var innerMap = map.get(k1);
            if (innerMap != null) return innerMap;
            return Collections.emptyMap();
        }

        @Override
        public V get(Object k1, Object k2) {
            var innerMap = map.get(k1);
            return innerMap == null ? null : innerMap.get(k2);
        }

        @Override
        public V put(K1 k1, K2 k2, V value) {
            if (isRefMap) {
                return ((Reference2ReferenceMap<K1, Map<K2, V>>) map)
                        .computeIfAbsent(k1, factory)
                        .put(k2, value);
            } else {
                return map.computeIfAbsent(k1, factory).put(k2, value);
            }
        }

        @Override
        public V computeIfAbsent(
                                 K1 k1, K2 k2, Reference2ReferenceFunction<? super K2, ? extends V> mappingFunction) {
            Map<K2, V> innerMap;
            if (isRefMap) {
                innerMap = ((Reference2ReferenceMap<K1, Map<K2, V>>) map).computeIfAbsent(k1, factory);
            } else {
                innerMap = map.computeIfAbsent(k1, factory);
            }
            if (isInnerRefMap) {
                return ((Reference2ReferenceMap<K2, V>) innerMap).computeIfAbsent(k2, mappingFunction);
            } else {
                return innerMap.computeIfAbsent(k2, mappingFunction);
            }
        }

        @Override
        public V remove(Object k1, Object k2) {
            Map<K2, V> innerMap = map.get(k1);
            if (innerMap == null) return null;
            V removed = innerMap.remove(k2);
            if (removed != null && innerMap.isEmpty()) map.remove(k1);
            return removed;
        }

        @Override
        public Map<K2, V> remove(Object k1) {
            var innerMap = map.remove(k1);
            if (innerMap != null) return innerMap;
            return Collections.emptyMap();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public void clear() {
            map.clear();
        }
    }
}
