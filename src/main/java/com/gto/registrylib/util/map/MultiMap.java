package com.gto.registrylib.util.map;

import com.google.common.base.Supplier;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceFunction;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface MultiMap<K, V> {

    static <K, V> MultiMap<K, V> create(Map<K, Collection<V>> map, Supplier<Collection<V>> factory) {
        return new MultiMapWrapper<>(map, factory);
    }

    static <K, V> MultiMap<K, V> create(Supplier<Collection<V>> factory) {
        return new MultiMapWrapper<>(new HashMap<>(), factory);
    }

    static <K, V> MultiMap<K, V> createIdentity(Supplier<Collection<V>> factory) {
        return new MultiMapWrapper<>(new Reference2ReferenceOpenHashMap<>(), factory);
    }

    Map<K, Collection<V>> getMap();

    Collection<V> get(K key);

    void put(K key, V value);

    boolean remove(K key, V value);

    Collection<V> remove(K key);

    boolean isEmpty();

    void clear();

    class MultiMapWrapper<K, V> implements MultiMap<K, V> {

        private final Map<K, Collection<V>> map;
        private final Reference2ReferenceFunction<Object, Collection<V>> factory;
        private final boolean isRefMap;

        private MultiMapWrapper(Map<K, Collection<V>> map, Supplier<Collection<V>> factory) {
            this.map = map;
            this.factory = unusedKey -> factory.get();
            this.isRefMap = map instanceof Reference2ReferenceMap;
        }

        @Override
        public Map<K, Collection<V>> getMap() {
            return map;
        }

        @Override
        public Collection<V> get(K key) {
            var values = map.get(key);
            if (values != null) return values;
            return Collections.emptyList();
        }

        @Override
        public void put(K key, V value) {
            if (isRefMap) {
                ((Reference2ReferenceMap<K, Collection<V>>) map).computeIfAbsent(key, factory).add(value);
            } else {
                map.computeIfAbsent(key, factory).add(value);
            }
        }

        @Override
        public boolean remove(K key, V value) {
            var values = map.get(key);
            if (values != null && values.remove(value)) {
                if (values.isEmpty()) map.remove(key);
                return true;
            }
            return false;
        }

        @Override
        public Collection<V> remove(K key) {
            var values = map.remove(key);
            if (values != null) return values;
            return Collections.emptyList();
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
