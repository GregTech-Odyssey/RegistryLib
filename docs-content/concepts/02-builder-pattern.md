---
sidebar_position: 2
title: Builder Pattern & Fluent API
description: How RegistryLib's generic builder architecture works.
---

# Builder Pattern & Fluent API

RegistryLib's core is a **generic builder architecture** that provides type-safe fluent chaining across related registrations. This page explains how it works under the hood.

## The Generic Base: AbstractBuilder\<R, T, P, S\>

Every builder in RegistryLib extends `AbstractBuilder<R, T, P, S>` with four type parameters:

| Parameter | Meaning | Example |
| --- | --- | --- |
| **R** | Registry supertype | `Block`, `Item`, `Fluid` |
| **T** | Actual object type | `MyBlock extends Block` |
| **P** | Parent type | `Registrate`, or a parent builder |
| **S** | Self type | The concrete builder class (for chaining) |

This design lets every configuration method return `this` (typed as `S`) so calls chain naturally:

```java
// All methods return the builder itself
blockBuilder
    .initialProperties(() -> Blocks.STONE)   // returns BlockBuilder
    .properties(p -> p.strength(2.0f))       // returns BlockBuilder
    .lang("My Block")                        // returns BlockBuilder
    .register();                             // returns BlockEntry
```

## Fluent Chaining

Every configuration method follows the same pattern internally:

```java
public S properties(UnaryOperator<Properties> func) {
    this.propertiesCallback = this.propertiesCallback.andThen(func);
    return self();  // Returns this, typed as S
}
```

Callbacks are composed with `.andThen()`, so calling a method multiple times appends behavior rather than replacing it. Order between independent methods (like `lang` and `addTag`) doesn't matter.

## The Parent Type System

The **P** parameter is what makes sub-entry chaining work. When you call `block(...).item(...)`, the ItemBuilder's parent type becomes the BlockBuilder:

```
BlockBuilder<Block, Registrate>
  .item()
    —
  ItemBuilder<BlockItem, BlockBuilder<Block, Registrate>>
    .register() —ItemEntry<BlockItem>
```

This means:
- The ItemBuilder knows its parent is a BlockBuilder
- Calling `.register()` on the ItemBuilder returns an `ItemEntry` and optionally allows continuing the parent chain
- **No type casting** —parent awareness is encoded in the generic types

### Sub-Entry Pattern

When `BlockBuilder.item()` is called, it creates a new ItemBuilder whose parent is the current BlockBuilder:

```java
// Simplified —inside BlockBuilder
public <I extends Item> ItemBuilder<I, BlockBuilder<T, P>> item(
    BiFunction<T, Item.Properties, I> factory)
{
    return owner.<I, BlockBuilder<T, P>>item(
        this,       // parent = this BlockBuilder
        getName(),
        factory
    );
}
```

The same pattern applies to `FluidBuilder.block()`, `FluidBuilder.bucket()`, and other sub-entry methods. Each creates a child builder with the appropriate parent type.

## Entry Type Hierarchy

When `.register()` produces an entry, the concrete type depends on the builder. All entry types share a common inheritance chain:

```
RegistryEntry<R, T>
  └─ AbstractHolderEntry<R, T>   (implements Holder<R>)
       ├─ ItemProviderEntry<T, S>  (implements ItemLike)
       │    ├─ ItemEntry<T>
       │    └─ BlockEntry<T>
       └─ FluidEntry<T>
```

`AbstractHolderEntry` is the abstract base for any entry that is backed by a NeoForge `Holder`. It provides `isBound()`, `get()` with a descriptive error message, and default `Holder` delegation. Concrete entries like `ItemEntry` and `BlockEntry` extend it through `ItemProviderEntry`, which adds `ItemLike` support (`asStack()`, `readOnlyStack()`).

## TagBatch Generification

Tag helpers `ItemTagBatch` and `BlockTagBatch` both extend a generic base class `TagBatch<T, B>`:

```java
public abstract class TagBatch<T, B extends TagBatch<T, B>> { ... }

public final class ItemTagBatch  extends TagBatch<Item, ItemTagBatch>  { ... }
public final class BlockTagBatch extends TagBatch<Block, BlockTagBatch> { ... }
```

Shared logic (adding tags, applying to entries) lives in `TagBatch<T, B>`. The concrete subclasses only provide their `ProviderType`. If you create a custom tag batch for a different registry, extend `TagBatch<T, B>` the same way.

## Lazy Evaluation

Entry objects are **not** created when you call builder methods. The builder only accumulates configuration callbacks. The actual game object is created later, when NeoForge fires the `RegisterEvent`:

1. **Builder phase** —you call `.properties()`, `.lang()`, etc. These store callbacks.
2. **Registration phase** —NeoForge fires `RegisterEvent`. RegistryLib calls `builder.createEntry()`, which invokes the stored callbacks and produces the real object.
3. **Post-registration** —data generators run (models, lang, loot, tags). Creative tabs are populated.

The `RegistryEntry` returned by `.register()` is a lazy reference —it resolves to the actual object only after registration completes.

:::warning
Calling `.get()` on an entry before registration has fired will throw `IllegalStateException` with the message *"Registry entry '...' has not been bound yet."* Use entries in setup code, event handlers, or gameplay logic —never during static initialization. See [Troubleshooting](/troubleshooting) for details.
:::

## The CRTP Pattern Beyond AbstractBuilder

The self-type (CRTP) pattern is not limited to `AbstractBuilder`. The state system uses the same technique in `AbstractStateBuilder<T, P, S>`:

```java
public abstract class AbstractStateBuilder<T, P, S extends AbstractStateBuilder<T, P, S>> {
    // ...
    protected abstract S self();
}

public final class ChunkStateBuilder<T, P>
        extends AbstractStateBuilder<T, P, ChunkStateBuilder<T, P>> { ... }

public final class WorldStateBuilder<T, P>
        extends AbstractStateBuilder<T, P, WorldStateBuilder<T, P>> { ... }
```

`AbstractStateBuilder` extracts shared fields (core, parent, name, codec, sync codec, debug config) and shared methods (sync, debug, the `register()`/`build()` guard) so that `ChunkStateBuilder` and `WorldStateBuilder` only add scope-specific logic. This is the same CRTP self-type trick as `AbstractBuilder<R, T, P, S>`, applied to a different part of the library.

## The BuilderCallback Mechanism

When `.register()` is called on a builder, it delegates to a `BuilderCallback`:

```java
public RegistryEntry<R, T> register() {
    if (registered) throw new IllegalStateException("Builder already registered: " + name);
    registered = true;
    // ... tag processing, callback collection ...
    return core.registry(name, registryKey, cbs, this::createEntry, this::createEntryWrapper);
}
```

:::warning
Calling `.register()` (or `.build()`) more than once on the same builder throws `IllegalStateException`. Each builder instance is single-use. This guard exists on all builder families: `AbstractBuilder`, `AbstractStateBuilder`, `EnchantmentBuilder`, `RecipeTypeBuilder`, `CropBuilder`, and `WorldgenFeatureBuilder`.
:::

The registration:
1. Creates a `Registration` record storing the builder, creator function, and entry factory
2. Stores it in an internal table indexed by registry type and name
3. Returns a `RegistryEntry<R, T>` that lazily resolves when NeoForge registers objects

This indirection is what allows RegistryLib to batch all registrations and execute them at the right time.

## Putting It Together

Here's how a full chain flows through the type system:

```java
// Start: BlockBuilder<Block, Registrate>
REGISTRYLIB.block("machine", MachineBlock::new)
        // Configure block —still BlockBuilder<MachineBlock, Registrate>
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .lang("Machine")
        .defaultLoot()

        // Sub-entry: ItemBuilder<BlockItem, BlockBuilder<MachineBlock, Registrate>>
        .item(b -> b
            .addTab(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .addTooltip(Component.literal("A useful machine"))
        )

        // Back at block level —register returns BlockEntry<MachineBlock>
        .register();
```

Each transition is type-safe. The compiler knows exactly what methods are available at every point in the chain.

## See Also

- [What is RegistryLib?](/concepts/what-is-registrylib) —project positioning and goals
- [Understanding the Chain](/tutorials/understanding-chain) —hands-on tutorial walking through a chain
- [Builder Methods](/reference/builder-methods) —complete method reference for all builder types
