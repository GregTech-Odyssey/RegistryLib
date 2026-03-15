---
title: Special Optimizations
nav_order: 12
permalink: /special-optimizations/
---

# Optimization Methods & Checklist

A catalogue of general optimization techniques applied in this project. Each entry describes the applicable scenario and the approach taken.

---

## Concurrency & Thread Safety

### Double-Checked Locking for Lazy Initialization

**When to use:** A lazily-initialized value is read frequently from multiple threads, but written only once. The full `synchronized` block on every read is unnecessary after the first initialization.

Perform an unsynchronized volatile read first. Only if the sentinel value is observed, enter a `synchronized` block and check again before computing. This confines the lock contention to the very first call while all subsequent reads take a lock-free fast path.

### Atomic Swap for One-Shot Collection Draining

**When to use:** A collection accumulates entries during a setup phase, is drained (iterated) exactly once by an event, and must be discarded afterwards — while producers may still be racing to add entries.

Wrap the collection in an `AtomicReference` and drain via `getAndSet(null)`. The swap is atomic: producers that call `get()` after the swap observe `null` and gracefully skip, so no entry is silently lost and no `NullPointerException` is possible. This is safer than assigning `null` to a plain field after iterating.

---

## Memory Reclamation

### Null-Out One-Time References After Consumption

**When to use:** A field (factory, callback list, supplier delegate, etc.) is consumed exactly once during initialization or registration and serves no purpose afterwards, yet it holds a closure or object graph that prevents garbage collection.

Set the field to `null` immediately after its single use. This severs the reference chain, allowing the GC to reclaim the closure and everything it captures. Typical targets: lazy-init delegates, builder factories, registration callback lists.

### Replace Repeated Lambda Allocations with Shared Constants

**When to use:** The same stateless lambda — no-op consumer, identity function, always-true predicate, null-returning supplier — is instantiated at multiple call sites. Each allocation is tiny but adds up in aggregate.

Declare a single `static final` constant for each common behavior and share it across all call sites via a typed accessor method. This reduces class count, allocation pressure, and metaspace usage for lambda classes that are functionally identical.

---

## Collection Selection

### Use Identity-Hash Collections for Singleton Keys

**When to use:** Keys are singletons or interned objects (registry keys, enum-like constants, provider-type instances) where `==` is equivalent to `.equals()`, but the collection still pays for `hashCode()` and `equals()` on every operation.

Replace `HashSet` / `HashMap` with identity-hash variants (e.g., fastutil `ReferenceOpenHashSet`, `Reference2ReferenceOpenHashMap`). These use `System.identityHashCode()` and `==` internally, eliminating virtual dispatch on `hashCode()` and `equals()` for every insertion and lookup.

### Replace General-Purpose Multimaps/Tables with Purpose-Built Nested Maps

**When to use:** A Guava `Table` or `Multimap` is used for a two-level keyed structure, but its generality (hashing strategy, iteration ordering, copy-on-structural-modification safety) is not needed, and identity semantics at the outer or inner level would be beneficial.

Implement lightweight `NestedMap` / `NestedMultiMap` wrappers backed by identity-hash maps at each level. These auto-clean empty inner maps on removal (preventing unbounded key accumulation) and select the correct `computeIfAbsent` overload based on whether the inner map supports identity semantics.

---

## Deferred & Batched Operations

### Defer Side Effects to a Single Terminal Point

**When to use:** A builder or configuration API accumulates state over multiple calls, and each call currently triggers an immediate side effect (e.g., registering a data generator). Repeated calls to the same state slot overwrite or duplicate the effect.

Collect all state during configuration calls without triggering side effects. Apply the accumulated state in one batch at the terminal method (e.g., `register()` or `build()`). This guarantees each effect fires exactly once, simplifies removal/override of individual entries before finalization, and makes the builder order-independent.

### Store Per-Entry Flags Instead of Global State

**When to use:** A boolean flag (e.g., "optional") applies conceptually to individual entries within a collection, but is implemented as a single field on the enclosing builder, forcing all entries to share the same flag value.

Replace the global flag with a per-entry map (e.g., `Reference2BooleanOpenHashMap<TagKey<?>>`) that stores each entry's flag independently. This allows mixed states (some entries optional, some not) without separate builder invocations or workarounds.

---

## Structural Decoupling

### Extract Internal State into a Dedicated Core Class

**When to use:** A main API class contains both the public-facing fluent API and a large body of internal registration state, event handling, and bookkeeping. Modifications to internals risk breaking the public API surface, and the class is difficult to test in isolation.

Move all internal tables, callbacks, and event wiring into a separate core class. The public class becomes a thin facade that delegates to the core. This makes the core independently testable, reduces the risk of accidental API breakage during internal refactoring, and clearly separates what belongs to consumers versus what belongs to the implementation.

### Centralize Event Listeners as Static Handlers

**When to use:** Multiple instances of a class each register their own event listener, causing the event bus to accumulate N listeners for the same event type. Each listener does the same dispatch logic, differing only in which instance it belongs to.

Register a single static handler on the event bus and maintain a lookup map (e.g., `ConcurrentHashMap<String, Instance>`) keyed by discriminator (mod ID, name, etc.). The one handler fans out to the appropriate instance on each event. This reduces event bus overhead from O(N) listeners to O(1) and eliminates the need to unregister per-instance listeners.

---

## Minor Idioms

### Prefer `.toList()` over `Collectors.toList()`

**When to use:** A stream pipeline produces a list that is only read after collection. `Collectors.toList()` allocates an intermediate `Collector` object on each call.

Use `.toList()` (Java 16+). It produces an unmodifiable list directly through the stream infrastructure without an intermediate collector, and signals to the reader that the result is not intended to be mutated.

### Remove Unnecessary Lazy Wrapping of Static Flags

**When to use:** A value is wrapped in `Lazy.of(...)` but the underlying computation is trivially cheap or the value is already fixed at process startup (e.g., a boolean flag set by the launcher). The lazy wrapper adds an indirection and a volatile read on every access for no benefit.

Replace the `Lazy` wrapper with a direct method call or a plain `static final` field. Reserve `Lazy` for computations that are genuinely expensive or whose dependencies are not yet available at class-load time.
