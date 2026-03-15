---
title: Special Optimizations
nav_order: 12
permalink: /special-optimizations/
---

# Special Optimizations

This page documents performance and correctness optimizations applied to the codebase, organized by technique category.

---

## Thread Safety

### Double-Checked Locking on `Lazy<T>`

**The entire `get()` method was `synchronized`, causing every read — including all hot reads after initialization — to acquire an intrinsic lock.**

The implementation now uses double-checked locking (DCL) with a sentinel object `UNINITIALIZED` and a `volatile` backing field. The outer check runs without any lock, so threads reading an already-initialized value take a pure load path with no synchronization. Only the first thread to observe `UNINITIALIZED` enters the `synchronized` block, computes the value, stores it, and clears the delegate reference. All subsequent callers return immediately from the unsynchronized fast path.

### Atomic Null-Out for Client-Side Registration Maps

**Client-side BER and fluid extension maps were plain mutable fields. After the setup events fired they were set to `null` directly, creating a race window between the in-progress `forEach` and the null assignment where a concurrent registration call could observe a non-null map reference that was about to disappear.**

Both maps are now wrapped in `AtomicReference`. The flush side uses `getAndSet(null)` to atomically exchange the map for `null` in a single operation — no other thread can observe a non-null reference after the swap begins. The registration side retrieves the reference once, checks for null, and writes into the local reference. A registration that arrives after the flush simply discards its entry rather than throwing `NullPointerException` or silently losing data.

---

## Memory Reclamation

### Releasing One-Time References After Use

**Factory functions, callback lists, and supplier delegates hold closures and object graphs that serve no purpose once they have been consumed exactly once.**

`Lazy<T>` nulls out `delegate` immediately after the value is computed on first call, freeing the supplier closure for GC. `Registration` nulls out both `creator` and `callbacks` after `register()` completes. The client-side maps are reclaimed via `AtomicReference.getAndSet(null)`. The pattern is consistent: any field used for deferred initialization is cleared as soon as that initialization fires.

### Shared Sentinel Constants for Common Functions

**Anonymous lambdas used as no-ops, identity functions, or constant suppliers were being freshly allocated at each call site, even though all instances are behaviorally identical.**

`FunctionUtil` provides a single set of package-level constant instances — `NO_OP_CONSUMER`, `NO_OP_BICONSUMER`, `IDENTITY_FN`, `ALWAYS_TRUE`, `ALWAYS_FALSE`, `NULL_SUPPLIER` — and typed accessor methods that return them via an unchecked cast. The same approach is applied to `ProviderType`: a single `NULL` sentinel constant replaces the repeated creation of `context -> null` lambdas in `registerClientProvider`. Call sites that need any of these behaviors now share one object rather than allocating a new closure.

---

## Collection & Data Structure

### Identity-Hash Collections for Singleton Keys

**`ResourceKey`, `ProviderType`, and related objects are effective singletons whose equality is reference equality. They were stored in `HashSet` and `HashMap`, which call `hashCode()` and `equals()` on every insertion and lookup even though those calls always reduce to a pointer comparison.**

These have been replaced with fastutil `ReferenceOpenHashSet` and `Reference2ReferenceOpenHashMap` across `completedRegistrations`, `AbstractBuilder.tagsByType`, `BlockEntityBuilder.validBlocks`, the datagen added-set in `DataProviderInitializer`, the loot table creator set, and `RegistryCore`'s internal maps. Identity collections skip the hash and equality computation entirely, eliminating that overhead from every registration and lookup in hot paths.

### Custom Nested Map Utilities Replacing Guava

**`RegistryCore`'s internal tables and multimaps used Guava `HashBasedTable` and `HashMultimap`, which do not support identity semantics and carry structural overhead from Guava's generalized API.**

The `NestedMap`, `NestedMultiMap`, and `MultiMap` interfaces (provided by the bundled FastCollection library) replace these. Each is backed by fastutil identity-hash maps at the outer and inner levels. The wrapper implementations auto-detect whether the inner map is a `Reference2ReferenceMap` to select the correct `computeIfAbsent` overload, and they clean up empty inner maps on removal to avoid unbounded key accumulation. This replaces Guava as a structural dependency for the core registration tables.

---

## Tag Registration

### Per-Tag `isOptional` with Deferred Bulk Application

**`isOptional` was a single boolean field on the builder shared by all tags on an entry. Data generators were registered eagerly on each `tag()` call, risking duplicate `setData` registrations when multiple tags targeted the same provider type.**

`tagsByType` now maps each `TagKey` to its own `boolean isOptional` via `Reference2BooleanOpenHashMap`. All tag data generators are collected during `tag()` calls and bulk-applied in a single `setData` call per provider type inside `register()`. Tags can be individually marked optional with `tag(type, true, tags)`, and the number of `setData` invocations is bounded to one per provider type regardless of how many `tag()` calls were made.

---

## Structural Decoupling

### `RegistryCore` Extracted from `RegistryLib`

**`RegistryLib` contained all registration state — tables, callbacks, datagens, creative-tab modifiers — making it a monolithic class where the public API and internal machinery were inseparable.**

The internal state and event handling have been moved to `RegistryCore`. `RegistryLib` becomes a thin entry-point that wires the event bus once and delegates entirely to `RegistryCore`. `RegistryCore` is independently instantiable and carries no dependency on the public `RegistryLib` API surface, making it straightforward to use in isolation or to test without the full mod lifecycle.

### Centralized Static Event Handlers

**Each `RegistryCore` instance registered its own event listeners on construction, causing the event bus to accumulate per-instance listeners and invoke them all on every event.**

Event handling is now static: `RegistryLib` registers `RegistryCore::onRegister`, `RegistryCore::onRegisterLate`, and related static methods once at mod init. A `ConcurrentHashMap<String, RegistryCore> REGISTRY_CORES` holds all live instances by mod ID; each static handler fans out to the appropriate core. The event bus holds one listener per event type instead of one per `RegistryCore` instance.

---

## API & Minor Improvements

### Annotation Retention Downgraded to `CLASS`

**`@StandardAPI` and `@SyntaxSugar` used `@Retention(RetentionPolicy.RUNTIME)`, keeping their descriptors accessible through reflection at runtime — a use case that does not exist for these developer-documentation markers.**

Both annotations now carry `@Retention(RetentionPolicy.CLASS)`. They remain visible to the compiler and to bytecode tooling, but are stripped from class data at load time, reducing the annotation metadata held in the JVM method area for every annotated method.

### Stream Terminal `.toList()` over `Collectors.toList()`

**Stream pipelines that produced read-only lists terminated with `Collectors.toList()`, which allocates an intermediate `Collector` object on each call.**

These have been replaced with `.toList()` (Java 16+), which produces an unmodifiable list directly through the stream infrastructure with no intermediate factory allocation. The semantics are equivalent for all read-only uses in the codebase.
