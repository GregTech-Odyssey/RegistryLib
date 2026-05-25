package com.gto.registrylib.util.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * A generic key-value registry with freeze semantics.
 *
 * <p>
 * Once {@link #freeze()} is called the registry becomes permanently immutable: further {@link
 * #register} calls throw {@link IllegalStateException}. All read operations are safe to call at any
 * time, including concurrently from multiple threads.
 *
 * <p>
 * Use the static factory methods {@link #create()} and {@link #createOrdered()} to obtain
 * instances. The ordered variant preserves insertion order during iteration.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public final class FreezableRegistry<K, V> {

    private volatile Map<K, V> map;
    private volatile boolean frozen;

    private FreezableRegistry(Map<K, V> backingMap) {
        this.map = backingMap;
        this.frozen = false;
    }

    /**
     * Creates a new registry backed by a {@link ConcurrentHashMap}.
     *
     * <p>
     * Iteration order is <b>not</b> guaranteed.
     */
    public static <K, V> FreezableRegistry<K, V> create() {
        return new FreezableRegistry<>(new ConcurrentHashMap<>());
    }

    /**
     * Creates a new registry backed by a synchronized {@link LinkedHashMap}.
     *
     * <p>
     * Iteration order matches insertion order.
     */
    public static <K, V> FreezableRegistry<K, V> createOrdered() {
        return new FreezableRegistry<>(Collections.synchronizedMap(new LinkedHashMap<>()));
    }

    /**
     * Registers a new entry.
     *
     * @throws IllegalStateException    if the registry is frozen
     * @throws IllegalArgumentException if the key is already present
     * @throws NullPointerException     if {@code key} or {@code value} is null
     */
    public synchronized void register(K key, V value) {
        if (key == null) {
            throw new NullPointerException("Key must not be null");
        }
        if (value == null) {
            throw new NullPointerException("Value must not be null");
        }
        if (frozen) {
            throw new IllegalStateException("Cannot register to frozen registry");
        }
        if (map.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate key: " + key);
        }
        map.put(key, value);
    }

    /** Returns the value mapped to {@code key}, or {@code null} if absent. */
    public V get(K key) {
        return map.get(key);
    }

    /**
     * Returns the value mapped to {@code key}.
     *
     * @throws IllegalArgumentException if the key is not present
     */
    public V getOrThrow(K key) {
        V value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("No entry found for key: " + key);
        }
        return value;
    }

    /**
     * Returns an {@link Optional} containing the value mapped to {@code key}, or {@link
     * Optional#empty()} if absent.
     */
    public Optional<V> getOptional(K key) {
        return Optional.ofNullable(map.get(key));
    }

    /** Returns {@code true} if the registry contains the given key. */
    public boolean contains(K key) {
        return map.containsKey(key);
    }

    /**
     * Freezes the registry, making it permanently immutable.
     *
     * <p>
     * After this call any attempt to {@link #register} will throw {@link IllegalStateException}.
     * The internal map is replaced with an unmodifiable snapshot.
     *
     * @throws IllegalStateException if the registry is already frozen
     */
    public synchronized void freeze() {
        if (frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
        this.map = Collections.unmodifiableMap(new LinkedHashMap<>(map));
        this.frozen = true;
    }

    /** Returns {@code true} if the registry has been frozen. */
    public boolean isFrozen() {
        return frozen;
    }

    /** Returns an unmodifiable view of the values in this registry. */
    public Collection<V> values() {
        return Collections.unmodifiableCollection(map.values());
    }

    /** Returns an unmodifiable view of the entries in this registry. */
    public Set<Map.Entry<K, V>> entries() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    /** Returns an unmodifiable view of the keys in this registry. */
    public Set<K> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /** Returns the number of entries in this registry. */
    public int size() {
        return map.size();
    }

    /** Returns {@code true} if this registry contains no entries. */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /** Iterates over every entry in the registry. */
    public void forEach(BiConsumer<K, V> action) {
        map.forEach(action);
    }
}
