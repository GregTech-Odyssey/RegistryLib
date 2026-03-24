---
sidebar_position: 4
title: Understanding the Registration Chain
description: How RegistryLib's fluent API chain works under the hood.
---

# Understanding the Registration Chain

Now that you have registered items and blocks, this tutorial explains _how_ the fluent chain works so you can use it confidently in more complex scenarios.

## What Is a Registration Chain?

A registration chain is a sequence of method calls on a builder object that configures and then submits a game object to the registry. It follows the **fluent API** (or builder) pattern where each method returns the builder itself, allowing calls to be chained:

```java
REGISTRYLIB
        .item("example", Item::new)      // 1. Create the builder
        .lang("Example Item")             // 2. Configure
        .addDefaultTab()                  // 3. Configure more
        .register();                      // 4. Submit
```

Every chain follows the same lifecycle:

1. **Builder creation** —a factory method on `RegistryCore` (like `.item()`, `.block()`, `.fluid()`) creates a typed builder.
2. **Configuration** —zero or more chained calls set properties, lang entries, tags, loot, models, and other attributes.
3. **`.register()`** —the terminal operation that submits the builder's configuration to RegistryLib's internal registry and returns an `Entry`.

## The Lifecycle in Detail

```
RegistryCore            Builder                  Entry
    —                    —                       —
    ├─ .item(...)  ──────►│                        —
    —                    ├─ .lang(...)             —
    —                    ├─ .properties(...)       —
    —                    ├─ .addTag(...)           —
    —                    ├─ .register()  ─────────►│
    —                    —                       —
    —                    —(builder is consumed)   —(entry is returned)
```

After `.register()` is called:
- The builder's accumulated configuration is frozen and scheduled for registration during the appropriate NeoForge lifecycle events.
- A typed `Entry` object is returned immediately. The `Entry` acts as a lazy supplier —calling `.get()` on it returns the actual registered object once the registry event has fired.

## The Parent Type System

One of RegistryLib's most powerful features is **nested builders**. When you call `.item(...)` on a `BlockBuilder`, you enter an `ItemBuilder` whose _parent_ is the block builder:

```java
REGISTRYLIB
        .block("example_block", Block::new)    // BlockBuilder<Block, RegistryCore>
        .initialProperties(() -> Blocks.STONE)
        .item(item -> item                      // ItemBuilder<..., BlockBuilder>
                .addDefaultTab()
                .addTooltip(Component.literal("A tooltip"))
        )                                       // returns to BlockBuilder
        .register();                            // registers BOTH the block and its item
```

The generic parameter `P` (parent) tracks this relationship at the type level. When the inner builder finishes, control returns to the parent builder. A single `.register()` at the end submits the entire tree.

:::tip
You do not need to call `.register()` inside the `.item(...)` callback. The outer `.register()` handles everything.
:::

## Entry Types

Every `.register()` call returns a typed Entry:

| Builder | Returns | `.get()` yields |
| --- | --- | --- |
| `item(...)` | `ItemEntry<T>` | `T extends Item` |
| `block(...)` | `BlockEntry<T>` | `T extends Block` |
| `fluid(...)` | `FluidEntry<T>` | `T extends FlowingFluid` |

Entry objects are safe to store as `static final` fields. They are resolved lazily —`.get()` returns the registered instance after the registry event fires.

```java
// Store as a constant
public static final ItemEntry<Item> MY_ITEM = REGISTRYLIB
        .item("my_item", Item::new)
        .lang("My Item")
        .register();

// Use later (after registration)
Item item = MY_ITEM.get();
ResourceKey<Item> key = MY_ITEM.getKey();
```

## Common Pitfalls

### Forgetting `.register()`

The most common mistake. Without `.register()`, the chain builds a configuration object that is never submitted:

```java
// —Bug: no .register() —nothing is registered
REGISTRYLIB
        .item("forgotten_item", Item::new)
        .lang("Forgotten");

// —Correct
public static final ItemEntry<Item> MY_ITEM = REGISTRYLIB
        .item("my_item", Item::new)
        .lang("My Item")
        .register();
```

Since there is no returned `Entry`, the compiler does not force you to capture the result. Always end with `.register()`.

### Registration Class Not Loaded

Java only initializes a class when it is first referenced. If your registration class is never referenced, its `static final` fields are never evaluated and nothing gets registered.

```java
// In your mod constructor or initializer, force the class to load:
SimpleItemExample.init();  // a static method, or just reference any field
```

:::note
A common pattern is to add a no-op `public static void init() {}` method to each registration class and call it from your mod's constructor.
:::

### Calling `.get()` Too Early

Entry objects are lazy. Calling `.get()` before the registry event fires (e.g., during static initialization or in a constructor) will throw an exception or return `null`.

```java
// —Too early —registry event has not fired yet
public static final Item RAW = MY_ITEM.get();

// —Use the Entry itself and call .get() when needed at runtime
public void someMethod() {
    Item item = MY_ITEM.get();  // safe after registration
}
```

## Summary

| Concept | Key Point |
| --- | --- |
| Chain structure | Create —Configure —`.register()` |
| Terminal operation | `.register()` submits and returns an Entry |
| Nested builders | `.item(...)` on a block builder creates a child; one `.register()` submits both |
| Entry objects | Lazy suppliers; store as `static final`, call `.get()` at runtime |
| Class loading | Reference your registration class so its static fields are initialized |

## What's Next

- [Builder Pattern (Concepts)](/concepts/builder-pattern) —deeper dive into the builder architecture and generics
- [Group System](/tutorials/group-system) —organize registrations into logical groups
