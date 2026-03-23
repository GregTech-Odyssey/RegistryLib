---
sidebar_position: 2
title: Performance & Optimization
description: RegistryLib-specific optimization strategies and lifecycle awareness.
---

# Performance and Implementation Optimizations

This page keeps only optimization ideas that directly relate to RegistryLib: why they exist, when they apply, and what they cost when implemented poorly. It is not a general Java performance guide.

## Builder and Registration Lifecycle

### Delay Side Effects Until the Endpoint

Builders collect configuration across multiple chained calls, so many side effects are best delayed until `.register()` or `.build()`. The value of doing so is:

- more stable call ordering
- later chained calls can override earlier defaults
- fewer duplicate registrations or duplicate datagen injections during configuration

:::warning
Delayed submission only works if the endpoint method is definitely called. If `.register()` or `.build()` is omitted, all previous configuration may fail silently.
:::

### Release One-Time References After Consumption

For factories, callback lists, or deferred delegates that are only needed once, clearing references after use reduces unnecessary object lifetime. This kind of optimization fits best on internal fields whose lifecycle clearly ends when registration finishes.

## Structural Choices for Large Registration Batches

### Prefer Group for Shared Defaults

When multiple entries share a lang prefix, a tab, or property modifiers, prefer Group over repeating the same chained setup on every entry. This lowers both boilerplate and future change cost.

### Only Introduce Custom Builders When They Are Truly Needed

If you only want shared defaults, Group is usually enough. Custom Builders only become valuable when you need new project-level methods or stable custom compile-time return types.

## Datagen and Type Selection

### Keep Language, Model, and Recipe Logic Close to the Registration Chain

One of RegistryLib's strengths is that datagen callbacks and registered objects stay near each other. Keeping that logic near the Builder chain is usually easier to trace and override than maintaining scattered external tables.

### Choose Container Types That Match the Semantics

When an internal structure depends on object identity rather than value equality, identity-based collections often match RegistryLib semantics better. This only makes sense when the keys truly have stable singleton-like identity.

:::note
If an optimization cannot clearly explain why it affects RegistryLib's registration, generation, or runtime lifecycle, it does not belong on this page.
:::

## Lifecycle and Environment Boundaries

### Client-Only Types Must Stay Lazily Loaded

Once a renderer, tooltip rendering node, or another client class enters a shared initialization path, the environment boundary is broken. Keeping it wrapped in a `Supplier` is a necessary cost for both readability and safety.

### Events and Cache Structures Need Explicit Single-Use or Multi-Use Semantics

If an internal structure is meant to be consumed only once during one event phase, make that single-use behavior explicit in the implementation rather than relying on convention. That is how you avoid duplicate registration, late writes, or null access.

## When You Should Not Optimize

- You still have not identified whether the real bottleneck is boilerplate duplication, datagen structure, or an environment-boundary mistake.
- You are only adding abstraction because it looks more advanced.
- You want to copy generic Java micro-optimizations into RegistryLib documentation without explaining how they relate to the Builder lifecycle.

## Related Pages

- [Custom Builders](../expert/custom-builder.md)
- [Group System](../intermediate/group-system.md)
- [Design Decisions](../../concepts/design-decisions.md)
