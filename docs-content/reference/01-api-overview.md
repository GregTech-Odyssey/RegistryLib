---
sidebar_position: 1
title: API Overview
description: Entry points, builder families, and common chain patterns.
---

# API Overview

This page is for quick lookup only, not full teaching. It answers "which entry point should I start from?" and "what do common chains usually look like?"

:::note
The code snippets on this page are short excerpts from the runnable `RegistryLibTest` examples, not invented pseudo-code.
:::

## Which Entry Point Should I Start From?

| What you want to do | Entry point | What usually comes next |
| --- | --- | --- |
| Register a regular or composite Item | `item("id", factory)` or `item("id")` | `lang`, `defaultModel`, `addTab`, `addTooltip`, `attach` |
| Register a component-driven Item with attachments | `componentItem("id")` or `componentItem("id", factory)` | `lang`, `defaultModel`, `addTooltip`, `attach` |
| Register a Block | `block("id", factory)` or `block("id")` | `initialProperties`, `simpleItem` or `item`, `loot`, `addTag` |
| Register a Fluid | `fluid("id", still, flow)` | `lang`, `clientExtension`, `properties`, `block`, `bucket` |
| Register a BlockEntity | `blockEntity("id", factory)` | `validBlock` or `validBlocks`, `renderer` |
| Register a generic object in another registry | `generic("id", registryKey, factory)` or `simple(...)` | `register` or immediate completion |
| Register a creative tab | `creativeTab("id")` | title, icon, content population |
| Share defaults across many entries | `group("name")` | `langPrefix`, `tab`, `initialBlockProperties`, `blockProperties`, `itemProperties`, `addBlockTag`, `addItemTag`, `addFluidTag` |

:::tip
If you're unsure, start with `item(...)` or `block(...)` —they cover the vast majority of registrations. See the [How-to guides](/how-to/register-items) for step-by-step walkthroughs.
:::

## Builder Family Quick Lookup

| Builder | Responsibility | Endpoint |
| --- | --- | --- |
| `ItemBuilder` | Item properties, model, tooltip, tab, recipe, tag | `ItemEntry` |
| `BlockBuilder` | Block properties, drops, block item, recipe, tag | `BlockEntry` |
| `FluidBuilder` | Fluid type, rendering, block, bucket, tag | `FluidEntry` |
| `BlockEntityBuilder` | Host block binding and renderer | `BlockEntityTypeEntry` |

## Entry Type Quick Lookup

| Type | Typical use |
| --- | --- |
| `ItemEntry<T>` | Reference an Item; create `ItemStack` and `ItemResource` values directly |
| `BlockEntry<T>` | Reference a Block, its default state, and holder-style APIs expecting `Holder<Block>` |
| `FluidEntry<T>` | Access source, type, block, bucket, `FluidStack`, and `FluidResource` together |
| `BlockEntityTypeEntry<T>` | Reference a `BlockEntityType` with host binding |

## Entry Helper Quick Lookup

| Entry helper | What it gives you |
| --- | --- |
| `ItemEntry.asStack()` | A default `ItemStack` without reconstructing the item manually |
| `ItemEntry.readOnlyStack()` | Defensive copy of a cached `ItemStack` (count 1); safe against external mutation |
| `ItemEntry.asResource()` | An `ItemResource` wrapper for transfer-related APIs |
| `BlockEntry.getDefaultState()` | The block's default state for world placement or configuration |
| `FluidEntry.getSource()` | The matching source fluid instance |
| `FluidEntry.getType()` | The `FluidType` associated with the family |
| `FluidEntry.getBlock()` / `getBucket()` | The related fluid block or bucket when they exist |
| `FluidEntry.asStack()` / `asResource()` | Transfer-friendly fluid values without rebuilding them by hand |
| `FluidEntry.readOnlyStack()` | A cached read-only `FluidStack` (1000 mB); avoids repeated allocations |

:::note
Several Entry wrappers also satisfy holder-style usage directly. When another API expects a `Holder<Item>`, `Holder<Block>`, or `Holder<Fluid>`, the RegistryLib entry wrapper is often already usable as that value.
:::

## Common Chain Lookup

### Minimal Item

```java
REGISTRYLIB.item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

### Component Item with Attachments

```java
REGISTRYLIB.componentItem("magic_wand")
        .initialProperties(() -> new Item.Properties().stacksTo(1))
        .properties(Item.Properties::fireResistant)
        .lang("Magic Wand")
        .defaultModel()
        .attach(new InspectAttachment())
        .register();
```

### Minimal Block

```java
REGISTRYLIB.block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

### Block Inside a Group

```java
MACHINES.block("crusher", Block::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .defaultLoot()
        .simpleItem()
        .register();
```

## See Also

- [Entry Types](/reference/entry-types) —detailed reference for each Entry wrapper
- [Builder Methods](/reference/builder-methods) —complete method reference for all Builder types
- [Register Items](/how-to/register-items) —step-by-step item registration
- [Register Blocks](/how-to/register-blocks) —step-by-step block registration
- [Builder Pattern & Fluent API](/concepts/builder-pattern) —how the chain architecture works
