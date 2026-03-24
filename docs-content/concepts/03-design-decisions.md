---
sidebar_position: 3
title: Design Decisions
description: Why RegistryLib is designed the way it is.
---

# Design Decisions

This page explains the reasoning behind RegistryLib's major architectural choices, what alternatives were considered, and what trade-offs were accepted.

## Why a Fluent API?

**Alternatives considered:** annotation processing, YAML/JSON config files, code-generation plugins.

RegistryLib uses a fluent builder API because:

1. **Discoverability** —IDE auto-complete shows you exactly what methods are available at each point in the chain. No need to memorize annotation names or config schemas.
2. **Type safety** —the Java compiler catches mistakes at build time. A typo in an annotation string would only fail at runtime; a typo in a method name fails immediately.
3. **Expressiveness** —lambdas and method references let you inline complex logic (custom loot tables, dynamic tooltips) directly in the chain without separate config files.
4. **No magic** —there's no annotation processor, no reflection, no bytecode manipulation. The code you see is the code that runs.

The trade-off is verbosity compared to annotation-based approaches —but in practice, the chains are concise and the IDE assistance compensates.

## Why Integrate Datagen Into the Builder Chain?

**Alternative:** separate data generator classes (the vanilla approach).

Vanilla NeoForge separates registration from data generation. You register a block in one class, then write its lang entry in a `LanguageProvider`, its loot table in a `BlockLootSubProvider`, its model in a `BlockStateProvider`, and so on.

RegistryLib integrates these into the builder chain:

```java
REGISTRYLIB.block("my_block", Block::new)
        .lang("My Block")              // lang entry
        .defaultLoot()                  // loot table
        .simpleItem()                   // block item + model
        .addTag(BlockTags.MINEABLE_WITH_PICKAXE)  // tag
        .register();
```

**Rationale:** related concerns belong together. When you add a new block, you almost always need a lang entry, loot table, item form, and tags. Scattering these across four files means four places to update, four places where something can be forgotten. Keeping them together reduces cognitive overhead and prevents "silent omission" bugs.

:::note
RegistryLib still generates standard data packs under the hood. The integrated API is sugar over the same `DataProvider` infrastructure that vanilla uses.
:::

## Why Does Group Exist Separately?

**Alternative:** builder inheritance or a "default properties" static field.

The [Group system](/tutorials/group-system) lets you share defaults (lang prefix, creative tab, block properties, item properties, tags) across many entries without repeating them:

```java
Group MACHINES = REGISTRYLIB.group("machines")
        .langPrefix("Machine: ")
        .tab(MY_TAB)
        .initialBlockProperties(() -> Blocks.IRON_BLOCK)
        .addBlockTag(MY_MACHINE_TAG);
```

Group is a separate concept rather than builder inheritance because:

1. **Orthogonal to builder type** —a group can contain blocks, items, and fluids. Builder inheritance would require a separate base class per builder type.
2. **Composable** —you can switch groups mid-registration without restructuring your class hierarchy.
3. **Runtime configurable** —group properties can be computed programmatically, not just hardcoded.
4. **No class explosion** —builder inheritance would lead to `MachineBlockBuilder`, `MachineItemBuilder`, etc. Groups avoid this.

## Why Do Entry Types Wrap DeferredHolder?

**Alternative:** return the raw registered object, or return the `DeferredHolder` directly.

Entry types (`ItemEntry`, `BlockEntry`, etc.) extend `DeferredHolder` and add convenience methods:

- `asStack()`, `readOnlyStack()`, `asResource()` for items and fluids
- `getDefaultState()` for blocks
- `getSource()`, `getType()`, `getBlock()`, `getBucket()` for fluids

**Rationale:**

1. **Lazy by default** —wrapping `DeferredHolder` means entries are safe to declare as `static final` fields. They resolve when NeoForge is ready, not at class-load time.
2. **Convenience without coupling** —helper methods like `asStack()` save repeated boilerplate in gameplay code without requiring you to import RegistryLib in your game logic (since they return vanilla types).
3. **Holder compatibility** —because entries extend `DeferredHolder`, they work directly in APIs that expect `Holder<Block>`, `Holder<Item>`, etc.

The trade-off is an extra layer of indirection, but in practice this has zero measurable performance impact.

## Why Method Overriding for Custom Builders?

**Alternative:** a plugin/extension system, decorator pattern, or composition.

When you need a custom builder (e.g., a `MachineBlockBuilder` that adds machine-specific methods), RegistryLib uses [method overriding](/tutorials/custom-builder):

```java
public class MachineBlockBuilder<T extends MachineBlock, P>
        extends BlockBuilder<T, P> {

    public MachineBlockBuilder<T, P> tier(int tier) {
        // custom configuration
        return this;
    }
}
```

**Rationale:**

1. **Java-native** —no special framework to learn. If you know inheritance, you know how to extend builders.
2. **Full access** —subclasses can override `createEntry()`, `registerModel()`, and other protected methods for deep customization.
3. **Type-safe chaining** —the self-type parameter `S` ensures your custom methods chain correctly with inherited methods.

A plugin system would add complexity for a use case that most mods don't need. Method overriding keeps the common case simple and the advanced case possible.

## Trade-offs and Limitations

Every design has trade-offs. Here are RegistryLib's known limitations:

| Limitation | Why it exists | Workaround |
| --- | --- | --- |
| Not all registries have dedicated builders | Supporting every NeoForge registry would bloat the API | Use `generic()` or `simple()` for unsupported registries |
| Datagen must run to generate assets | Integrated datagen means no manual JSON editing | This is intentional —generated assets are more maintainable |
| Learning curve for the generic type system | `AbstractBuilder<R, T, P, S>` has four type parameters | In practice, users only see the outer API; generics are internal |
| Custom builders require understanding of the builder hierarchy | Method overriding needs knowledge of the base classes | Most mods don't need custom builders; the built-in ones cover common cases |

:::tip
If you're unsure whether RegistryLib's approach fits your use case, start with the [beginner tutorials](/tutorials/installation). The fluent API is simpler to *use* than it is to *implement*, and most of the complexity lives in the internals you'll never touch.
:::

## See Also

- [Builder Pattern & Fluent API](/concepts/builder-pattern) —how the generic builder architecture works
- [Custom Builder](/tutorials/custom-builder) —tutorial for creating your own builder type
- [Performance](/tutorials/performance) —optimizations and performance considerations
