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
| Register a data component type lazily | `dataComponentTypeEntry("id", builder)` | pass the entry into supplier-friendly APIs |
| Register a creative tab | `creativeTab("id")` | title, icon, content population |
| Share defaults across many entries | `group("name")` | `langPrefix`, `tab`, `initialBlockProperties`, `blockProperties`, `itemProperties`, `addBlockTag`, `addItemTag`, `addFluidTag` |
| Reference existing vanilla or third-party objects | `existingItem("namespace:id")`, `existingBlock("namespace:id")` | pass the entry to recipes, tags, or holder-style APIs |
| Add tags to existing objects | `tagExisting(...)`, `itemTags().add(...)`, `blockTags().add(...)` | datagen-only tag entries |

:::tip
If you're unsure, start with `item(...)` or `block(...)`; they cover the vast majority of registrations. See the [How-to guides](/how-to/register-items) for step-by-step walkthroughs.
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
| `DataComponentTypeEntry<T>` | Lazy wrapper for a `DataComponentType<T>` |

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
| Lazy entry wrappers | Use `get()` only after registration, or pass the entry/supplier to APIs that resolve lazily |

:::note
Several Entry wrappers also satisfy holder-style usage directly. When another API expects a `Holder<Item>`, `Holder<Block>`, or `Holder<Fluid>`, the RegistryLib entry wrapper is often already usable as that value.
:::

## Core Helper Quick Lookup

| Helper | Purpose |
| --- | --- |
| `locale("zh_cn")` | Get or create a lang provider for a locale |
| `lang(key, enUs)` | Add an English lang entry and return a translatable component |
| `lang(locale, key, value)` | Add a lang entry for a locale string |
| `addRecipeData(provider -> { ... })` | Add a typed recipe datagen callback |
| `tagExisting(tag, items...)` | Add an item tag to existing items |
| `tagExisting(tag, blocks...)` | Add a block tag to existing blocks |
| `itemTags().add(...)` / `blockTags().add(...)` | Batch tag helpers for existing objects or ids |

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

### Existing Object in a Recipe

```java
ItemEntry<Item> VANILLA_IRON_INGOT = REGISTRYLIB.existingItem("minecraft:iron_ingot");

REGISTRYLIB.addRecipeData(prov -> prov.shapeless(RecipeCategory.MISC, Items.IRON_NUGGET, 9)
        .requires(VANILLA_IRON_INGOT)
        .unlockedBy("has_iron_ingot", prov.has(VANILLA_IRON_INGOT))
        .save(prov, MOD_ID + ":iron_nuggets_from_existing_iron"));
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

- [Entry Types](/reference/entry-types) - detailed reference for each Entry wrapper
- [Builder Methods](/reference/builder-methods) - complete method reference for all Builder types
- [Register Items](/how-to/register-items) - step-by-step item registration
- [Register Blocks](/how-to/register-blocks) - step-by-step block registration
- [Builder Pattern & Fluent API](/concepts/builder-pattern) - how the chain architecture works
