---
title: Registering Blocks
parent: Content Guides
nav_order: 2
permalink: /register-blocks/
---

# Registering Blocks

## What This Page Solves

Block registration usually involves more than the Block itself. It commonly includes the block item, drops, recipes, tags, and property initialization. This page shows how to keep those concerns inside a single `BlockBuilder` chain.

## When This Applies

- You want to register a regular Block.
- You want the Block to generate a matching BlockItem.
- You want to configure initial properties, drops, tags, or base resources in the same place.

## Quick Start

```java
public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
        .block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .simpleItem()
        .register();
```

This chain registers a Block that starts from stone-like properties and also generates the simplest matching BlockItem.

## Full Example

```java
public static final BlockEntry<Block> MACHINE_CASING = RegistryLibTest.REGISTRYLIB
        .block("machine_casing", Block::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .lang("Machine Casing")
        .simpleItem()
        .defaultLoot()
        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .tag(ItemTags.STONE_TOOL_MATERIALS)
        .register();
```

## Step-by-Step Explanation

1. `block("machine_casing", Block::new)` creates the `BlockBuilder`.
2. `.initialProperties(() -> Blocks.IRON_BLOCK)` copies an existing Block as the starting property source.
3. `.lang(...)` provides the display name for the Block and related generated resources.
4. `.simpleItem()` generates the standard default BlockItem.
5. `.defaultLoot()` generates the basic drop behavior.
6. `.tag(...)` can target either Block tags or Item tags depending on the tag type you pass in.
7. `.register()` returns `BlockEntry<Block>`.

{: .important }
> Registering a Block does not automatically mean a BlockItem exists. Whether it is generated, and how it is generated, depends on an explicit `.simpleItem()` or `.item(...)` call.

## Common Patterns

### I Only Need a Simple Block Item

If the default BlockItem is enough, use `.simpleItem()`. Switch to `.item(...)` only when you need a custom Item type or custom Item properties.

### Multiple Blocks Share the Same Property Defaults

If a whole set of machine casings or similar Blocks inherit the same defaults, centralize those defaults with [Group System]({{ '/group-system/' | relative_url }}) through `blockProperties(...)` or `itemProperties(...)` rather than repeating them.

### When Does BlockEntity Registration Enter the Picture?

If the Block needs tile or BlockEntity behavior, keep the Block registration independent first. Then continue with [Registering Block Entities and Renderers]({{ '/register-block-entities-and-renderers/' | relative_url }}) for host binding and renderer registration.

## Common API Lookup

| Method | Purpose |
| --- | --- |
| `block(name, factory)` | Create a `BlockBuilder` |
| `initialProperties(supplier)` | Set the source of initial properties |
| `simpleItem()` | Create the default BlockItem |
| `item(...)` | Customize the BlockItem |
| `defaultLoot()` | Generate basic drops |
| `tag(...)` | Add a tag |
| `register()` | Complete registration |

## Related Links

- [Registering Block Entities and Renderers]({{ '/register-block-entities-and-renderers/' | relative_url }})
- [Group System]({{ '/group-system/' | relative_url }})
- [API Reference]({{ '/api-reference/' | relative_url }})