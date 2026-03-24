---
sidebar_position: 5
title: Group System
description: Share defaults across multiple entries with the Group system.
---

# Group System

## What You Will Learn

In this tutorial you will learn how to use `Group` to share default settings �?lang prefixes, creative tabs, block/item properties, and tags �?across a family of related entries. By the end you will be able to:

- Create a `Group` with shared defaults.
- Register multiple blocks and items through that group.
- Override group defaults on individual entries when needed.
- Apply tags to every entry in a group with a single call.

:::note
If you are only registering one isolated entry, using `RegistryCore` directly is usually simpler. Group adds value through **reuse**, not by replacing every entry point.
:::

## Step 1 �?Decide Whether You Need a Group

`Group` is a layer of shared defaults wrapped around `RegistryCore`. The question it solves is not "can this be registered?" but "should this content family keep repeating the same configuration?"

Ask yourself:

- Do several entries share the same lang prefix (e.g. "Timer", "Machine")?
- Should they all appear in the same creative tab?
- Do they need identical base block or item properties?

If you answered **yes** to at least two, a Group will save you repetition. If not, register entries directly through `RegistryCore`.

## Step 2 �?Create the Group

Call `.group(name)` on your `RegistryCore` instance, configure the shared defaults, and finish with `.build()`:

```java
public static final Group TIMER_GROUP = REGISTRYLIB.group("timers")
        .langPrefix("Timer")
        .blockProperties(p -> p.strength(5.0F, 6.0F))
        .build();
```

Every block registered through `TIMER_GROUP` will now inherit the `"Timer"` lang prefix and `strength(5.0F, 6.0F)`.

## Step 3 �?Register Entries Through the Group

Use the group instead of `RegistryCore` as the starting point:

```java
public static final BlockEntry<TimerBlock> TIMER_TIER_1 = TIMER_GROUP
        .block("tier_1", p -> new TimerBlock(p, 1))
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .simpleItem()
        .register();
```

`tier_1` automatically inherits the lang prefix and the block property modifier from the group.

## Step 4 �?Understand What Gets Applied Automatically

Here is the full list of defaults that a Group can apply:

| Default | Applies to |
| --- | --- |
| `langPrefix` | All entries registered through that Group |
| `tab` | BlockItems, regular Items, and fluid buckets |
| `initialBlockProperties` | All Blocks registered through that Group |
| `blockProperties` | All Blocks registered through that Group |
| `initialItemProperties` | All regular Items registered through that Group |
| `itemProperties` | All regular Items registered through that Group |
| `addBlockTag(...)` | All Blocks registered through that Group |
| `addItemTag(...)` | All Items (and BlockItems) registered through that Group |
| `addFluidTag(...)` | All Fluids registered through that Group |

### Override Order

Group defaults are applied when the Builder is created. Anything you chain **after** `.block(...)`, `.item(...)`, or `.fluid(...)` has higher priority:

```java
public static final BlockEntry<TimerBlock> TIMER_TIER_3 = TIMER_GROUP
        .block("tier_3", p -> new TimerBlock(p, 3))
        .initialProperties(() -> Blocks.NETHERITE_BLOCK)  // overrides group's initial properties
        .properties(p -> p.strength(50.0F, 1200.0F))       // overrides group's strength
        .simpleItem()
        .register();
```

## Step 5 �?Set Initial Properties Across the Group

`Group.Builder` exposes `initialBlockProperties(...)` and `initialItemProperties(...)` for setting the base property template:

```java
public static final Group MACHINE_GROUP = REGISTRYLIB.group("machines")
        .langPrefix("Machine")
        .initialBlockProperties(Blocks.IRON_BLOCK)
        .blockProperties(p -> p.strength(5.0F, 6.0F))
        .build();
```

`initialBlockProperties` accepts either a `Block` directly or a `Supplier<? extends Block>`. It is applied **before** `blockProperties` modifiers, so the modifier always has a consistent base.

## Step 6 �?Apply Tags Across the Group

Use `addBlockTag(...)`, `addItemTag(...)`, and `addFluidTag(...)` to tag every entry of the corresponding type. Multiple tags can be passed in a single call, and calls are cumulative:

```java
public static final Group ORE_GROUP = REGISTRYLIB.group("ores")
        .langPrefix("Magic")
        .tab(MY_TAB)
        .addBlockTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
        .addItemTag(ItemTags.DURABILITY_ENCHANTABLE)
        .build();
```

Tags are injected at the same time as other defaults �?before any per-entry chain. A per-entry `.addTag(...)` call can always add more tags on top.

:::tip
For more detail on per-entry tags and recipe datagen, see the [Recipes and Tags](./recipes-tags) tutorial.
:::

## Common Patterns

- **Content families** �?Use Group for a shared lang prefix and tab, then override only the few exceptional values on individual entries.
- **Machine tiers** �?Use Group for hardness and drop requirements, then add special [tooltips](./tooltip-system) or stronger properties on higher-tier entries.
- **Large ore batches** �?Use Group for mining requirements and creative tab, then configure each ore's drop logic separately.

## Boundaries and Pitfalls

:::warning
- Group is **not** a namespace replacement. It only owns shared defaults and does not change when registration is submitted.
- A Group `tab` does not affect bare BlockEntity entries that do not have an Item form.
- If one entry is clearly an exception, do not force it into the same Group just for visual consistency.
:::

## Next Steps

- [Registering Blocks](/how-to/register-blocks) �?Detailed block registration guide.
- [Tooltip System](./tooltip-system) �?Add rich tooltip content to your grouped items.
- [API Reference](/reference/api-overview) �?Full API surface for `Group` and `Group.Builder`.
