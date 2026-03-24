---
sidebar_position: 3
title: Your First Block
description: Register a block with matching BlockItem, loot, and tags.
---

# Your First Block

This tutorial registers two blocks —a simple decorative block and a fully configured ore —so you can see how RegistryLib handles blocks, block items, loot tables, and tags in a single chain.

## What You Will Learn

- How to register a basic block with `block()`
- How to attach a `BlockItem` with `.simpleItem()` or `.item(...)`
- How to define loot tables and block tags inline
- The relationship between Block registration and BlockItem generation

## Minimal Example —Decorative Stone

The shortest path to a working block with a matching item:

```java
public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
        .block(RegistryLibTest.REGISTRYLIB, "decorative_stone", Block::new)
        .langCn("装饰—)
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

That chain:
1. **`block(..., "decorative_stone", Block::new)`** —starts a `BlockBuilder` with the registry name and a vanilla `Block` factory.
2. **`.initialProperties(() -> Blocks.STONE)`** —copies properties from an existing block (hardness, sound, etc.).
3. **`.lang("Decorative Stone")`** —sets the English display name for datagen.
4. **`.simpleItem()`** —generates a default `BlockItem` so the block can appear in inventories and creative tabs.
5. **`.register()`** —submits the registration and returns a `BlockEntry<Block>`.

## Full Example —Magic Ore

When you need custom loot, mining tags, and a tooltip on the block item:

```java
public static final BlockEntry<Block> MAGIC_ORE = RegistryLibTest.REGISTRYLIB
        .block("magic_ore", Block::new)
        .initialProperties(() -> Blocks.IRON_ORE)
        .properties(p -> p.strength(4.0F, 5.0F).requiresCorrectToolForDrops())
        .lang("Magic Ore")
        .lang(ModRegistryCore.LANG_ZH_CN, "魔法矿石")
        .loot((tables, b) -> tables.add(b, tables.createOreDrop(b, SimpleItemExample.COPPER_COIN.get())))
        .addTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
        .item(item -> item.addTooltip((collector, stack) -> {
            collector.node(new SubNode.Basic(Component.literal("§5Drops coins when mined")), true, false);
        }))
        .register();
```

### Step-by-Step Breakdown

1. **`block("magic_ore", Block::new)`** —starts a `BlockBuilder` with a vanilla block factory.
2. **`.initialProperties(() -> Blocks.IRON_ORE)`** —copies the base properties from iron ore.
3. **`.properties(p -> p.strength(4.0F, 5.0F).requiresCorrectToolForDrops())`** —overrides specific properties on top of the base. This is the same two-layer pattern used by items.
4. **`.lang(...)`** and **`.lang(ModRegistryCore.LANG_ZH_CN, ...)`** —set English and Chinese display names.
5. **`.loot((tables, b) -> ...)`** —defines a custom loot table inline. Here it creates an ore drop that yields `COPPER_COIN`.
6. **`.addTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)`** —marks the block as pickaxe-mineable and requiring at least an iron tool. You can pass multiple tags in a single call.
7. **`.item(item -> ...)`** —creates a `BlockItem` with a customization callback. Inside the callback you have access to the full item builder API, including `.addTooltip(...)`.
8. **`.register()`** —submits the registration and returns a `BlockEntry<Block>`.

## Block vs. BlockItem

:::warning
Registering a Block does **not** automatically create a BlockItem. Whether a BlockItem is generated depends on an explicit `.simpleItem()` or `.item(...)` call in the chain.

- **`.simpleItem()`** —creates a default BlockItem with no customization.
- **`.item(item -> ...)`** —creates a BlockItem and lets you configure it (tooltips, tabs, tags, etc.).
- **No item call** —the block exists in the world but has no inventory representation.
:::

## Common Patterns

| Pattern | Method | Use Case |
| --- | --- | --- |
| Copy properties from vanilla | `.initialProperties(() -> Blocks.STONE)` | Match an existing block's behavior |
| Custom hardness / blast resistance | `.properties(p -> p.strength(3.0F, 6.0F))` | Fine-tune mining time |
| Self-drop loot | (default if no `.loot(...)` override) | Block drops itself |
| Ore-style drop | `.loot((t, b) -> t.add(b, t.createOreDrop(b, item)))` | Drop a different item |
| Multiple tags | `.addTag(tag1, tag2, tag3)` | Assign tool type and tier in one call |

## Verify in Game

1. Run `./gradlew runClient`.
2. Open a creative-mode world.
3. Search for "Decorative Stone" or "Magic Ore" in the creative inventory.
4. Place the block and try mining it (use the correct tool tier for Magic Ore).
5. Confirm the loot drops match your configuration.

## Related

- [Register Blocks (How-To)](/how-to/register-blocks) —full reference for all block builder options
- [Understanding the Registration Chain](/tutorials/understanding-chain) —how the fluent API works under the hood
