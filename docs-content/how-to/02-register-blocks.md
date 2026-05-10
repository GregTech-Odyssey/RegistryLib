---
sidebar_position: 2
title: Register Blocks
description: Quick reference for block registration patterns.
---

# Register Blocks

## Simple Block

```java
public static final BlockEntry<Block> DECORATIVE_STONE = REGISTRYLIB
        .block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

## Full Block with Custom Loot and Tags

```java
public static final BlockEntry<Block> MAGIC_ORE = REGISTRYLIB
        .block("magic_ore", Block::new)
        .initialProperties(() -> Blocks.IRON_ORE)
        .properties(p -> p.strength(4.0F, 5.0F).requiresCorrectToolForDrops())
        .lang("Magic Ore")
        .loot((tables, b) -> tables.add(b, tables.createOreDrop(b, SimpleItemExample.COPPER_COIN.get())))
        .addTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
        .item(item -> item.addTooltip((collector, stack) -> {
            collector.node(new SubNode.Basic(Component.literal("§5Drops coins when mined")), true, false);
        }))
        .register();
```

:::warning
Registering a Block does **NOT** automatically create a BlockItem. You must explicitly call `.simpleItem()` or `.item(...)` to generate one.
:::

## Tinted Block Models

Use `tintedCube(...)` when the block model needs face-level `tintindex` values, then use `constantTint(...)` or `tintSource(...)` for the generated BlockItem model:

```java
public static final BlockEntry<Block> TIN_BLOCK = REGISTRYLIB
        .block("tin_block")
        .tintedCube(0)
        .constantTint(0xC8D8E8)
        .simpleItem()
        .register();
```

`tintedCube(0)` writes a simple cube block model with `tintindex: 0` on each face. `constantTint(...)` affects the item model for the BlockItem.

## Common API Lookup

| Method | Purpose |
|---|---|
| `block(name, factory)` | Create a `BlockBuilder` |
| `initialProperties(supplier)` | Set the source of initial block properties |
| `properties(...)` | Refine properties after initial copy |
| `simpleItem()` | Create the default BlockItem |
| `item(...)` | Customize the BlockItem |
| `defaultLoot()` | Generate basic self-drop loot table |
| `loot(...)` | Supply a custom loot table callback |
| `tintedCube(tintIndex)` | Generate a cube block model with face tint indices |
| `constantTint(color)` | Generate a tinted BlockItem model using the block model |
| `tintSource(sources...)` | Generate a BlockItem model with custom item tint sources |
| `addTag(...)` | Add one or more block tags |
| `register()` | Complete registration and return `BlockEntry<T>` |

## Common Patterns

**Simple BlockItem:** Use `.simpleItem()` when the default BlockItem is enough. Switch to `.item(...)` only for custom Item types or properties.

**Shared properties via Group:** When multiple blocks share the same defaults (e.g., machine casings), centralize with the [Group System](/tutorials/group-system) through `blockProperties(...)` or `itemProperties(...)`.

**BlockEntity later:** Keep block registration independent first, then attach BlockEntity behavior separately. See [Register Block Entities](/how-to/register-block-entities).

## See Also

- [Tutorial: First Block](/tutorials/first-block)
- [Tutorial: Group System](/tutorials/group-system)
- [How-To: Register Block Entities](/how-to/register-block-entities)
