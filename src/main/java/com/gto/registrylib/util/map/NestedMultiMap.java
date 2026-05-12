package com.gto.registrylib.util.map;

import com.google.common.base.Supplier;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceFunction;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface NestedMultiMap<K1, K2, V> {

    static <K1, K2, V> NestedMultiMap<K1, K2, V> create(
                                                        Map<K1, Map<K2, Collection<V>>> map,
                                                        Supplier<Map<K2, Collection<V>>> mapFactory,
                                                        Supplier<Collection<V>> collectionFactory) {
        return new NestedMultiMapWrapper<>(map, mapFactory, collectionFactory);
    }

    static <K1, K2, V> NestedMultiMap<K1, K2, V> create(
                                                        Supplier<Map<K2, Collection<V>>> mapFactory, Supplier<Collection<V>> collectionFactory) {
        return new NestedMultiMapWrapper<>(new HashMap<>(), mapFactory, collectionFactory);
    }

    static <K1, K2, V> NestedMultiMap<K1, K2, V> createIdentity(
                                                                Supplier<Map<K2, Collection<V>>> mapFactory, Supplier<Collection<V>> collectionFactory) {
        return new NestedMultiMapWrapper<>(
                new Reference2ReferenceOpenHashMap<>(), mapFactory, collectionFactory);
    }

    Map<K1, Map<K2, Collection<V>>> getMap();

    Collection<V> get(K1 k1, K2 k2);

    Map<K2, Collection<V>> get(K1 k1);

    void put(K1 k1, K2 k2, V value);

    boolean remove(K1 k1, K2 k2, V value);

    Collection<V> remove(K1 k1, K2 k2);

    Map<K2, Collection<V>> remove(K1 k1);

    boolean isEmpty();

    void clear();

    class NestedMultiMapWrapper<K1, K2, V> implements NestedMultiMap<K1, K2, V> {

        @Getter
        private final Map<K1, Map<K2, Collection<V>>> map;
        private final Reference2ReferenceFunction<Object, Map<K2, Collection<V>>> mapFactory;
        private final Reference2ReferenceFunction<Object, Collection<V>> collectionFactory;
        private final boolean isRefMap;
        private boolean isInnerRefMap;

        public NestedMultiMapWrapper(
                                     Map<K1, Map<K2, Collection<V>>> map,
                                     Supplier<Map<K2, Collection<V>>> mapFactory,
                                     Supplier<Collection<V>> collectionFactory) {
            this.map = map;
            this.collectionFactory = unusedKey -> collectionFactory.get();
            this.mapFactory = unusedKey -> {
                var innerRefMap = mapFactory.get();
                if (innerRefMap instanceof Reference2ReferenceMap) {
                    isInnerRefMap = true;
                }
                return innerRefMap;
            };
            this.isRefMap = map instanceof Reference2ReferenceMap;
        }

        @Override
        public Map<K1, Map<K2, Collection<V>>> getMap() {
            return map;
        }

        @Override
        public Collection<V> get(K1 k1, K2 k2) {
            var innerMap = map.get(k1);
            if (innerMap == null) return Collections.emptyList();
            var values = innerMap.get(k2);
            if (values != null) return values;
            return Collections.emptyList();
        }

        @Override
        public Map<K2, Collection<V>> get(K1 k1) {
            var innerMap = map.get(k1);
            if (innerMap != null) return innerMap;
            return Collections.emptyMap();
        }

        @Override
        public void put(K1 k1, K2 k2, V value) {
            Map<K2, Collection<V>> innerMap;
            Collection<V> values;
            if (isRefMap) {
                innerMap = ((Reference2ReferenceMap<K1, Map<K2, Collection<V>>>) map)
                        .computeIfAbsent(k1, mapFactory);
            } else {
                innerMap = map.computeIfAbsent(k1, mapFactory);
            }
            if (isInnerRefMap) {
                values = ((Reference2ReferenceMap<K2, Collection<V>>) innerMap)
                        .computeIfAbsent(k2, collectionFactory);
            } else {
                values = innerMap.computeIfAbsent(k2, collectionFactory);
            }
            values.add(value);
        }

        @Override
        public boolean remove(K1 k1, K2 k2, V value) {
            var innerMap = map.get(k1);
            if (innerMap == null) return false;
            var values = innerMap.get(k2);
            if (values == null) return false;
            if (values.remove(value)) {
                if (values.isEmpty()) {
                    innerMap.remove(k2);
                    if (innerMap.isEmpty()) {
                        map.remove(k1);
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public Collection<V> remove(K1 k1, K2 k2) {
            var innerMap = map.get(k1);
            if (innerMap == null) return Collections.emptyList();
            var values = innerMap.remove(k2);
            if (values == null) return Collections.emptyList();
            if (innerMap.isEmpty()) {
                map.remove(k1);
            }
            return values;
        }

        @Override
        public Map<K2, Collection<V>> remove(K1 k1) {
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
