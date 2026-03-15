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

{: .important }
> **Risks:**
>
> - **Missing `volatile` causes instruction reordering:** If the result field is not `volatile`, the JIT may reorder the constructor write and the field assignment. Another thread can observe a non-null reference in the outer check while the object's internal fields are not yet fully initialised — the classic "partially-constructed object escape". The store-load barrier provided by `volatile` is a correctness requirement, not an optimisation hint.
> - **Using `null` instead of a sentinel as the uninitialised marker:** If the value domain allows `null` as a legitimate computation result, the `== null` check cannot distinguish "not yet initialised" from "result is null", causing every call to enter the `synchronized` block and recompute. Use a sentinel object that can never equal a legitimate result as the initial value.
> - **Inconsistent state after an exception inside the `synchronized` block:** If the computation function throws inside the lock, the field remains at the sentinel / null value. The next thread will re-enter and execute again, potentially triggering duplicate side-effects or an infinite retry loop. Decide up-front whether to cache the exception (fail-fast) or allow retries, and ensure the computation is idempotent.

### Atomic Swap for One-Shot Collection Draining

**When to use:** A collection accumulates entries during a setup phase, is drained (iterated) exactly once by an event, and must be discarded afterwards — while producers may still be racing to add entries.

Wrap the collection in an `AtomicReference` and drain via `getAndSet(null)`. The swap is atomic: producers that call `get()` after the swap observe `null` and gracefully skip, so no entry is silently lost and no `NullPointerException` is possible. This is safer than assigning `null` to a plain field after iterating.

{: .important }
> **Risks if implemented incorrectly:**
>
> - **TOCTOU (check-then-act) race:** If the code reads the field for a `!= null` check and then reads it again to use it, another thread may null out the field between the two reads, so the second read returns `null` even though the check passed. Always capture the result of `get()` into a local variable first; operate on the local — other threads cannot modify a local variable.
> - **The consumer side must use `getAndSet(null)` as a single atomic step**, not `get()` to iterate followed by a separate `= null` assignment. Otherwise there is a race window between the end of iteration and the null assignment.
> - **Late-arriving producer entries are silently lost:** If a producer captures the map reference before `getAndSet(null)` but calls `put()` after `forEach` has already finished, the entry is written into a map that will never be iterated again — no NPE, no error, but the entry is permanently lost. This risk cannot be eliminated by `AtomicReference`; it must be addressed architecturally by guaranteeing the producer phase completes before the consumer event fires.
> - **`ConcurrentHashMap.forEach` is weakly consistent:** If a producer concurrently calls `put()` during the drain, `forEach` does **not** guarantee those writes will be seen (ConcurrentHashMap iterators are weakly consistent). Entries that arrive within the drain window may be silently skipped.
> - **Single-use semantics cannot be reused:** Once the `AtomicReference` is set to `null` it is permanently dead. If the same lifecycle fires the event more than once, or if re-registration needs to be supported, this pattern will silently discard all subsequent registrations (producers see `null` and skip) with no error whatsoever.

---

## Memory Reclamation

### Null-Out One-Time References After Consumption

**When to use:** A field (factory, callback list, supplier delegate, etc.) is consumed exactly once during initialization or registration and serves no purpose afterwards, yet it holds a closure or object graph that prevents garbage collection.

Set the field to `null` immediately after its single use. This severs the reference chain, allowing the GC to reclaim the closure and everything it captures. Typical targets: lazy-init delegates, builder factories, registration callback lists.

{: .important }
> **Risks:**
>
> - **Second call causes NPE:** If the caller is expected to invoke this path only once but an accidental second trigger exists (overload, event replay, test harness), the field is already `null` and a `NullPointerException` is thrown. Capture the field into a local variable before nulling it out, or throw an explicit `IllegalStateException` on subsequent access.
> - **Multi-thread visibility:** If the field is not `volatile` and the null-out and the read happen on different threads, the reading thread may never observe `null` (stale cached value), keeping the object alive longer than intended — or may observe `null` before the field has been consumed.
> - **Difficult to debug post-mortem:** Once the field is nulled, a heap dump no longer contains the original object's data, losing all diagnostic context. In development builds, guard with `assert field != null` or a log statement before nulling to preserve visibility.

### Replace Repeated Lambda Allocations with Shared Constants

**When to use:** The same stateless lambda — no-op consumer, identity function, always-true predicate, null-returning supplier — is instantiated at multiple call sites. Each allocation is tiny but adds up in aggregate.

Declare a single `static final` constant for each common behavior and share it across all call sites via a typed accessor method. This reduces class count, allocation pressure, and metaspace usage for lambda classes that are functionally identical.

{: .important }
> **Risks:**
>
> - **Shared instance accidentally holds mutable state:** If a "stateless" lambda actually captures an external mutable variable (closure escape), sharing the constant means all call sites share the same mutable state, producing data races or logic errors. Verify that the constant lambda is truly capture-free.
> - **Generic type erasure creates false type safety:** `static final` constants are typically declared at a raw or lower-bounded type and returned via unchecked cast through a generic accessor. If the accessor's generic signature does not match the use site, the compiler will not complain, but a `ClassCastException` may surface downstream at runtime.
> - **Over-extraction harms readability:** When constants are scattered across a utility class and referenced as `SomeConstants.noop()`, readers must navigate away to understand the actual behaviour. For lambdas that appear only once or twice, inlining is clearer; the constant only pays off when there are three or more call sites.

---

## Collection Selection

### Use Identity-Hash Collections for Singleton Keys

**When to use:** Keys are singletons or interned objects (registry keys, enum-like constants, provider-type instances) where `==` is equivalent to `.equals()`, but the collection still pays for `hashCode()` and `equals()` on every operation.

Replace `HashSet` / `HashMap` with identity-hash variants (e.g., fastutil `ReferenceOpenHashSet`, `Reference2ReferenceOpenHashMap`). These use `System.identityHashCode()` and `==` internally, eliminating virtual dispatch on `hashCode()` and `equals()` for every insertion and lookup.

{: .important }
> **Risks:**
>
> - **Non-singleton keys silently corrupt semantics:** If keys are not strict singletons (e.g., deserialized objects, objects loaded across different ClassLoaders, or manually `new`-ed equivalent instances), `==` and `.equals()` disagree. An identity-hash collection will treat logically equal keys as distinct entries, causing silent duplicate insertion or failed lookups with no exception.
> - **`System.identityHashCode()` distribution is weaker than custom `hashCode()`:** The identity hash is a pseudo-random value stored in the object header, unrelated to content. With a large number of keys, collision rates may exceed a well-designed `hashCode()`, lengthening open-addressing probe chains and degrading performance.
> - **Hard dependency on fastutil leaks into the API boundary:** If fastutil types appear in method signatures or field declarations visible to other modules, those modules must also depend on fastutil or face a `NoClassDefFoundError` at runtime. Restrict fastutil types to internal implementation; expose only standard `Map`/`Set` interfaces externally.

### Replace General-Purpose Multimaps/Tables with Purpose-Built Nested Maps

**When to use:** A Guava `Table` or `Multimap` is used for a two-level keyed structure, but its generality (hashing strategy, iteration ordering, copy-on-structural-modification safety) is not needed, and identity semantics at the outer or inner level would be beneficial.

Implement lightweight `NestedMap` / `NestedMultiMap` wrappers backed by identity-hash maps at each level. These auto-clean empty inner maps on removal (preventing unbounded key accumulation) and select the correct `computeIfAbsent` overload based on whether the inner map supports identity semantics.

{: .important }
> **Risks:**
>
> - **Auto-cleaning empty inner maps conflicts with concurrent iteration:** If a removal triggers inner-map cleanup (deleting the outer key) while the outer map is being iterated, a `ConcurrentModificationException` will be thrown or entries will be skipped. Even in single-threaded code, structural modifications during iteration must be avoided.
> - **Loss of Guava's built-in safety guarantees:** Guava `Table`/`Multimap` provides `unmodifiableXxx` views, consistent `cellSet()` snapshots, and `null` rejection. Custom implementations easily omit these, allowing callers to mutate internal structure directly or insert `null` keys/values.
> - **Poor compatibility with serialization and debugging tools:** Standard and Guava collections have well-established `toString()`, JSON serialization, and IDE debugger support. Custom nested maps require explicit implementation of these or produce opaque output in logs and breakpoints.

---

## Deferred & Batched Operations

### Defer Side Effects to a Single Terminal Point

**When to use:** A builder or configuration API accumulates state over multiple calls, and each call currently triggers an immediate side effect (e.g., registering a data generator). Repeated calls to the same state slot overwrite or duplicate the effect.

Collect all state during configuration calls without triggering side effects. Apply the accumulated state in one batch at the terminal method (e.g., `register()` or `build()`). This guarantees each effect fires exactly once, simplifies removal/override of individual entries before finalization, and makes the builder order-independent.

{: .important }
> **Risks:**
>
> - **Forgetting to call the terminal method silently discards all state:** Because all side-effects are deferred to `register()` / `build()`, a caller that finishes configuring but omits the terminal call will see nothing take effect — with no compile-time or runtime indication. Consider adding an "un-finalised" warning via a GC phantom reference, shutdown hook, or static analysis rule.
> - **Peak memory during accumulation:** Previously each call could release intermediate objects immediately; now all accumulated state is held until the terminal point. If the number of entries is large or each carries heavyweight objects (textures, model data), peak memory can be significantly higher than in an eager-execution model.
> - **Exception handling at the terminal point is complex:** When batch-applying side-effects, a failure midway requires deciding between full rollback, skip-and-continue, or abort — every strategy is harder to implement correctly than per-call error handling. Unhandled exceptions leave a partially registered, inconsistent state.

### Store Per-Entry Flags Instead of Global State

**When to use:** A boolean flag (e.g., "optional") applies conceptually to individual entries within a collection, but is implemented as a single field on the enclosing builder, forcing all entries to share the same flag value.

Replace the global flag with a per-entry map (e.g., `Reference2BooleanOpenHashMap<TagKey<?>>`) that stores each entry's flag independently. This allows mixed states (some entries optional, some not) without separate builder invocations or workarounds.

{: .important }
> **Risks:**
>
> - **Ambiguous default value semantics:** Should an absent entry return `true` or `false`? Without a clear, documented convention, different callers will make different assumptions, producing hard-to-trace divergences. Standardise the default in a single `getOrDefault` call, or require all entries to carry an explicit flag.
> - **Memory overhead and key explosion:** When the number of entries is large (hundreds of TagKeys), a per-entry map uses far more memory than a single boolean field. Identity-hash maps have a relatively low load factor (default 0.75), so actual occupied capacity can reach 1.3–2× the entry count. Evaluate whether the entry scale justifies the overhead.
> - **Increased API complexity:** A single `setOptional(boolean)` becomes `setOptional(TagKey<?>, boolean)` or a chained `.optional()` modifier, requiring callers to understand the implicit "flag applies to the most recent entry" semantic. Without sufficient documentation and IDE hints, misuse is likely.

---

## Structural Decoupling

### Extract Internal State into a Dedicated Core Class

**When to use:** A main API class contains both the public-facing fluent API and a large body of internal registration state, event handling, and bookkeeping. Modifications to internals risk breaking the public API surface, and the class is difficult to test in isolation.

Move all internal tables, callbacks, and event wiring into a separate core class. The public class becomes a thin facade that delegates to the core. This makes the core independently testable, reduces the risk of accidental API breakage during internal refactoring, and clearly separates what belongs to consumers versus what belongs to the implementation.

{: .important }
> **Risks:**
>
> - **Blurred responsibility boundary between Core and Facade:** If the split is incomplete — some logic remaining in the Facade and some in the Core — future maintainers will be uncertain where new functionality belongs, leading to double entry-points or duplicated logic. Enforce a strict rule: the Facade only forwards parameters and adapts types; it contains no business logic.
> - **Core accessed directly, bypassing the Facade:** If the Core class is `public` (or package-private but accessible within the same package), downstream modules can bypass the Facade and manipulate the Core directly, undermining encapsulation and invalidating any invariants enforced by the Facade (pre-condition checks, logging, event firing). Keep the Core `final` with only the Facade holding a reference to it.
> - **Indirection increases debugging cost:** Every call passes through a delegation layer, deepening the stack and requiring breakpoints in two places. When Facade and Core method names correspond one-to-one, the overhead is small; but when renaming or parameter transformation is involved, the correspondence becomes opaque.

### Centralize Event Listeners as Static Handlers

**When to use:** Multiple instances of a class each register their own event listener, causing the event bus to accumulate N listeners for the same event type. Each listener does the same dispatch logic, differing only in which instance it belongs to.

Register a single static handler on the event bus and maintain a lookup map (e.g., `ConcurrentHashMap<String, Instance>`) keyed by discriminator (mod ID, name, etc.). The one handler fans out to the appropriate instance on each event. This reduces event bus overhead from O(N) listeners to O(1) and eliminates the need to unregister per-instance listeners.

{: .important }
> **Risks:**
>
> - **Lookup map memory leak:** Once an instance is registered in the static map, it will not be garbage-collected unless explicitly removed (e.g., on mod unload or hot-reload). The map grows without bound. Provide an `unregister()` method or use `WeakReference` values so instances are cleaned up when their lifecycle ends.
> - **An exception in one instance's handler disrupts all others:** If the static handler does not catch exceptions thrown by individual instance handlers, a single failure will abort the entire fan-out, and **unrelated instances** will never receive the event for that firing. Wrap each per-instance dispatch in a try-catch to isolate failures.
> - **Event ordering and priority cannot be expressed per-instance:** With N independent listeners, each can declare its own `@SubscribeEvent(priority=...)`. Consolidating to one handler forces all instances to share a single priority, making the processing order uniform. If different instances have ordering dependencies on the same event, this pattern cannot express them.

---

## Minor Idioms

### Prefer `.toList()` over `Collectors.toList()`

**When to use:** A stream pipeline produces a list that is only read after collection. `Collectors.toList()` allocates an intermediate `Collector` object on each call.

Use `.toList()` (Java 16+). It produces an unmodifiable list directly through the stream infrastructure without an intermediate collector, and signals to the reader that the result is not intended to be mutated.

{: .important }
> **Risks:**
>
> - **Unmodifiable list causes downstream `UnsupportedOperationException`:** `Collectors.toList()` returns a mutable `ArrayList`; `.toList()` returns an unmodifiable list. If any downstream code calls `sort()`, `remove()`, or `add()` on the returned list, replacing the collector will throw at runtime. Audit all consumption paths before switching.
> - **`null` elements are not permitted:** `.toList()` rejects `null` elements in the stream (throwing `NullPointerException`), whereas `Collectors.toList()` accepts them. If the stream source can produce `null`, the replacement introduces a new NPE.
> - **Semantic signal is misread by the team:** Team members may treat `.toList()` as a casual shorthand rather than an intentional signal of immutability. Without a team coding convention, someone may revert it to `Collectors.toList()` to "fix" the immutability, causing repeated back-and-forth changes. Codify the convention explicitly.

### Remove Unnecessary Lazy Wrapping of Static Flags

**When to use:** A value is wrapped in `Lazy.of(...)` but the underlying computation is trivially cheap or the value is already fixed at process startup (e.g., a boolean flag set by the launcher). The lazy wrapper adds an indirection and a volatile read on every access for no benefit.

Replace the `Lazy` wrapper with a direct method call or a plain `static final` field. Reserve `Lazy` for computations that are genuinely expensive or whose dependencies are not yet available at class-load time.

{: .important }
> **Risks:**
>
> - **Class-loading order dependency:** The `Lazy` wrapper may exist precisely to defer evaluation past a class-loading boundary — the dependency class or static field may not yet be ready at `<clinit>` time. Removing `Lazy` and switching to a `static final` field that evaluates eagerly can trigger `ExceptionInInitializerError` or silently obtain an uninitialised default value (`null`/`0`/`false`), and this failure may only reproduce under specific loading orders that are hard to consistently recreate in development.
> - **Misjudging "cheap":** Computations that appear trivial (reading a system property, invoking `ServiceLoader`) may involve I/O, lock contention, or security manager checks in certain environments, making their actual cost far higher than expected. Removing `Lazy` causes these to execute on every call. Confirm through profiling rather than intuition.
> - **Loss of single-execution guarantee:** `Lazy` inherently ensures the supplier runs exactly once (via thread-safe DCL). Replacing it with a direct method call means the supplier logic executes once per invocation at each call site. For suppliers with side-effects (writing files, registering callbacks), multiple executions produce incorrect results.
