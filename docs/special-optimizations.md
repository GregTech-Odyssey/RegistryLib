---
title: Special Optimizations
nav_order: 12
permalink: /special-optimizations/
---

# Special Optimizations

This page documents performance and correctness optimizations contributed by ** (233)** to the 26.1 branch. These optimizations fall into two categories: thread-safety improvements and code/data-structure improvements.

---

## Thread Safety

### `Lazy<T>` — Double-Checked Locking (DCL)

**Before:** The entire `get()` method was `synchronized`, meaning every read — even after initialization — acquired a lock.

**After:** DCL pattern with a sentinel object `UNINITIALIZED` and a `volatile` field:

```java
public T get() {
    var value = this.value;
    if (value == UNINITIALIZED) {        // fast path: no lock on hot reads
        synchronized (this) {
            if (this.delegate != null) {
                this.value = this.delegate.get();
                this.delegate = null;    // release delegate for GC
            }
            value = this.value;
        }
    }
    return (T) value;
}
```

Hot reads (after initialization) are now lock-free. The `delegate` reference is also cleared after first use, allowing it to be garbage-collected.

---

### `Client` — Safe Post-Registration Null-Out

**Before:** `BER` and `FLUID_TYPE_EXTENSIONS` were plain mutable fields. After the setup event fired, they were set to `null` directly. A concurrent `registerBER()` call between the `forEach` and the `= null` assignment could silently drop a registration or throw `NullPointerException`.

**After:** Both maps are wrapped in `AtomicReference`. The flush-and-clear is done atomically via `getAndSet(null)`, and the registration side guards with a null check:

```java
// Registration side — safe against late calls
public void registerBER(Supplier<BlockEntityType<?>> type, BlockEntityRendererProvider provider) {
    var map = BER.get();
    if (map != null) map.put(type, provider);
}

// Flush side — atomic swap ensures no entry is lost or double-flushed
private void onClientSetup(FMLClientSetupEvent event) {
    var map = BER.getAndSet(null);
    if (map != null) map.forEach((type, provider) -> BlockEntityRenderers.register(type.get(), provider));
}
```

This closes the race window that风屿 identified: a thread can no longer observe a non-null map, get preempted, have the flush thread nullify the field, and then attempt to write into a null reference.

---

## Code & Data-Structure Optimizations

### Collection Identity Semantics — `ReferenceOpenHashSet` / `Reference2ReferenceOpenHashMap`

Several internal sets and maps now use fastutil identity-hash collections instead of standard `HashSet` / `HashMap`:

| Location | Before | After |
|---|---|---|
| `RegistryLib.completedRegistrations` | `HashSet` | `ReferenceOpenHashSet` |
| `AbstractBuilder.tagsByType` value map | `HashMultimap` | `Reference2BooleanOpenHashMap` |
| `BlockEntityBuilder.validBlocks` | `HashSet` | `ReferenceOpenHashSet` |
| `DataProviderInitializer` added-set | `HashSet` | `ReferenceOpenHashSet` |
| `RegistryLibLootTableProvider.currentLootCreators` | `HashSet` | `ReferenceOpenHashSet` |
| `RegistryCore` internal maps | `HashBasedTable` / `HashMultimap` | `NestedMap` / `NestedMultiMap` + `ReferenceOpenHashSet` |

`ResourceKey`, `ProviderType`, and similar objects are effectively singletons whose equality is reference equality. Using identity-hash collections avoids `hashCode()`/`equals()` calls entirely, reducing overhead on every map lookup.

### `AbstractBuilder.tagsByType` — Per-Tag `isOptional` + Deferred Registration

**Before:** `isOptional` was a single boolean field on the builder; all tags shared the same optional-state. Tag data generators were registered eagerly on every `tag()` call.

**After:** Each `TagKey` now stores its own `boolean isOptional` inside a `Reference2BooleanOpenHashMap`. All tag registrations are deferred and bulk-applied inside `register()`:

```java
@Override
@MustBeInvokedByOverriders
public RegistryEntry<R, T> register() {
    tagsByType.forEach(
        (type, tags) -> setData(type, (_, prov) ->
            tags.forEach((tag, isOptional) -> prov.rawBuilder((TagKey) tag).add(asTag(isOptional)))));
    return callback.accept(...);
}
```

This eliminates duplicate `setData` registrations and allows individual tags to be marked optional independently.

### Annotation Retention — `CLASS` instead of `RUNTIME`

`@StandardAPI` and `@SyntaxSugar` were annotated with `@Retention(RetentionPolicy.RUNTIME)`. Since these annotations are only used as developer documentation marks and are never read via reflection at runtime, they have been changed to `@Retention(RetentionPolicy.CLASS)`. This causes them to be stripped from loaded class files, reducing the annotation metadata overhead in the JVM's method area.

### `ProviderType.NULL` Sentinel

A shared `NULL` sentinel constant was added to `ProviderType` to replace repeated creation of anonymous `context -> null` lambdas when data-gen is not running:

```java
ProviderType NULL = _ -> null;

// Used in registerClientProvider:
if (!DatagenModLoader.isRunningDataGen()) return NULL;
```

### `RegistryLib` → `RegistryCore` Extraction

The internal registration logic (registration table, callbacks, datagens, creative-tab modifiers) was extracted from `RegistryLib` into a new `RegistryCore` class. `RegistryLib` now delegates to `RegistryCore`, making it a thin public API facade. The new `RegistryCore` uses the custom `NestedMap` / `NestedMultiMap` / `MultiMap` utility classes (backed by fastutil) in place of Guava `Table` and `Multimap`.

### `FastCollection` Library

The project now bundles ['s FastCollection library](https://github.com/233/FastCollection) (`libs/fastcollection-1.0.jar`), which provides the `NestedMap`, `NestedMultiMap`, and `MultiMap` interfaces used by `RegistryCore`.
