---
title: Group System
parent: Core Systems
nav_order: 1
permalink: /group-system/
---

# Group System

`Group` is a layer of shared defaults wrapped around `RegistryCore`. The question it solves is not “can this be registered?” but “should this content family keep repeating the same configuration?”

## When to Use It

- You have a set of entries that share a lang prefix.
- You want multiple Blocks or Items to default into the same creative tab.
- You want Block or Item property modifiers to be inherited by a whole series of entries.

{: .note }
> If you are only registering one isolated entry, using `RegistryCore` directly is usually simpler. Group adds value through reuse, not by replacing every entry point.

## Quick Example

```java
public static final Group TIMER_GROUP = REGISTRYLIB.group("timers")
        .langPrefix("Timer")
        .blockProperties(p -> p.strength(5.0F, 6.0F))
        .build();

public static final BlockEntry<TimerBlock> TIMER_TIER_1 = TIMER_GROUP
        .block("tier_1", p -> new TimerBlock(p, 1))
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .simpleItem()
        .register();
```

In this example, `tier_1` automatically inherits the lang prefix and the Block property modifier.

## Core Concepts

### What Group Applies Automatically

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

Group defaults are applied when the Builder is created. Anything you continue to chain after `.block(...)`, `.item(...)`, or `.fluid(...)` has higher priority.

## Common Combinations

- Content families: use Group for a shared lang prefix and tab, then override only the few exceptional values on individual entries.
- Machine tiers: use Group for hardness and drop requirements, then add special tooltips or stronger properties on higher-tier entries.
- Large ore batches: use Group for mining requirements and creative tab, then configure each ore's drop logic separately.

## Setting Initial Properties Across a Group

`Group.Builder` exposes `initialBlockProperties(...)` and `initialItemProperties(...)` for setting the base property template for every Block or Item registered through the group. This is equivalent to calling `.initialProperties(...)` on each individual builder entry.

```java
public static final Group MACHINE_GROUP = REGISTRYLIB.group("machines")
        .langPrefix("Machine")
        .initialBlockProperties(Blocks.IRON_BLOCK)   // all blocks copy from IRON_BLOCK
        .blockProperties(p -> p.strength(5.0F, 6.0F)) // then further modify strength
        .build();
```

`initialBlockProperties` accepts either a `Block` directly or a `Supplier<? extends Block>`. `initialItemProperties` accepts a `Supplier<Item.Properties>`. Both are applied before `blockProperties` / `itemProperties` modifiers, so the modifier has a consistent base to work from.

## Applying Tags Across a Group

`Group.Builder` exposes `addBlockTag(...)`, `addItemTag(...)`, and `addFluidTag(...)` for adding shared tags to every entry of the corresponding type that is registered through the group. Multiple tags can be passed in a single call, and multiple calls are allowed and accumulate:

```java
public static final Group ORE_GROUP = REGISTRYLIB.group("ores")
        .langPrefix("Magic")
        .tab(MY_TAB)
        .addBlockTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
        .addItemTag(ItemTags.DURABILITY_ENCHANTABLE)
        .build();
```

The tags are injected at the same time as other defaults — before any per-entry chain. A per-entry `.addTag(...)` call can always add more tags on top.

## Boundaries and Pitfalls

- Group is not a namespace replacement. It only owns shared defaults and does not change when registration is submitted.
- A Group tab does not affect bare BlockEntity entries that do not have an Item form.
- If one entry is clearly an exception, do not force it into the same Group just for visual consistency.

## Related Links

- [Core Systems]({{ '/systems-overview/' | relative_url }})
- [Registering Blocks]({{ '/register-blocks/' | relative_url }})
- [Registering Fluids and Buckets]({{ '/register-fluids-and-buckets/' | relative_url }})
