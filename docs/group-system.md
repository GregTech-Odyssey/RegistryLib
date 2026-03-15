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
| `blockProperties` | All Blocks registered through that Group |
| `itemProperties` | All regular Items registered through that Group |

### Override Order

Group defaults are applied when the Builder is created. Anything you continue to chain after `.block(...)`, `.item(...)`, or `.fluid(...)` has higher priority.

## Common Combinations

- Content families: use Group for a shared lang prefix and tab, then override only the few exceptional values on individual entries.
- Machine tiers: use Group for hardness and drop requirements, then add special tooltips or stronger properties on higher-tier entries.
- Large ore batches: use Group for mining requirements and creative tab, then configure each ore's drop logic separately.

## Boundaries and Pitfalls

- Group is not a namespace replacement. It only owns shared defaults and does not change when registration is submitted.
- A Group tab does not affect bare BlockEntity entries that do not have an Item form.
- If one entry is clearly an exception, do not force it into the same Group just for visual consistency.

## Related Links

- [Core Systems]({{ '/systems-overview/' | relative_url }})
- [Registering Blocks]({{ '/register-blocks/' | relative_url }})
- [Registering Fluids and Buckets]({{ '/register-fluids-and-buckets/' | relative_url }})
