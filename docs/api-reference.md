---
title: API Reference
nav_order: 6
has_children: true
permalink: /api-reference/
---

# API Reference

This page is for quick lookup only, not full teaching. Full examples stay in the tutorial pages; this page answers “which entry point should I start from?” and “what do common chains usually look like?”

{: .note }
> Deduplication rule: this page only provides method families, responsibility boundaries, and common chain lookup. It does not repeat full tutorial examples.

## Which Entry Point Should I Start From?

| What you want to do | Entry point | What usually comes next |
| --- | --- | --- |
| Register a regular or composite Item | `item("id", factory)` | `lang`, `defaultModel`, `tab`, `tooltip`, `attach` |
| Register a Block | `block("id", factory)` | `initialProperties`, `simpleItem` or `item`, `loot`, `tag` |
| Register a Fluid | `fluid("id", still, flow)` | `lang`, `clientExtension`, `properties`, `block`, `bucket` |
| Register a BlockEntity | `blockEntity("id", factory)` | `validBlock` or `validBlocks`, `renderer` |
| Share defaults across many entries | `group("name")` | `langPrefix`, `tab`, `blockProperties`, `itemProperties` |

## Builder Family Quick Lookup

| Builder | What it is responsible for | Typical endpoint |
| --- | --- | --- |
| `ItemBuilder` | Item properties, model, tooltip, tab, recipe, tag | `ItemEntry` |
| `BlockBuilder` | Block properties, drops, block item, recipe, tag | `BlockEntry` |
| `FluidBuilder` | Fluid type, rendering, block, bucket, tag | `FluidEntry` |
| `BlockEntityBuilder` | Host block binding and renderer | `BlockEntityEntry` |

## Entry Type Quick Lookup

| Type | Typical use |
| --- | --- |
| `ItemEntry<T>` | Reference an Item from another registration chain, recipe, or gameplay logic |
| `BlockEntry<T>` | Reference a Block and, where relevant, its default state or block item |
| `FluidEntry<T>` | Access source, type, block, bucket, and other fluid-related objects together |
| `BlockEntityEntry<T>` | Reference a `BlockEntityType` and complete host binding |

## Common Chain Lookup

### Minimal Item

```java
REGISTRYLIB.item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

### Minimal Block

```java
REGISTRYLIB.block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
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

## Related Links

- [Content Guides]({{ '/content-guides/' | relative_url }})
- [Core Systems]({{ '/systems-overview/' | relative_url }})
- [Advanced Topics]({{ '/advanced-topics/' | relative_url }})
