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
        .constantTint("block/templates/storage_cube", 0xC8D8E8)
        .simpleItem()
        .register();
```

`constantTint(texturePath, color)` writes a simple cube block model with `tintindex: 0` on each face and a tinted BlockItem model pointing at the block model. The template PNG must already exist under `assets/<modid>/textures/...`, or be generated once by a general resource provider. Use `tintedCube(texturePath, tintIndex)` when you need to control the face tint index separately.

For batch registration, move the visual rule into a preset:

```java
TextureRef storageTemplate = REGISTRYLIB.textureRef("block/templates/storage_cube");

REGISTRYLIB.block("lead_block")
        .visual(BlockVisualPreset.constantTintedCube(storageTemplate, 0x6E7380))
        .simpleItem()
        .register();
```

:::important
`.texture(path, imageSupplier)` generates a block texture PNG. Use `modelTexture(...)`, `existingTexture(...)`, `tintedCube(texturePath, tintIndex)`, or `constantTint(texturePath, color)` when the model should reference an existing shared texture.
:::

Use `TextureRef.mc("block/iron_block")` or `TextureRef.of(id)` for vanilla or third-party cube textures; string overloads resolve inside the current mod namespace.

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
| `tintedCube(tintIndex)` | Generate a cube block model with face tint indices using the block's own texture path |
| `tintedCube(texturePath, tintIndex)` | Generate a tinted cube block model referencing a shared texture |
| `constantTint(color)` | Generate a tinted BlockItem model using the block's own model |
| `constantTint(texturePath, color)` | Generate a tinted cube block model and tinted BlockItem model from a shared texture |
| `constantTint(texture, color)` | TextureRef variant for shared textures |
| `tintSource(sources...)` | Generate a BlockItem model with custom item tint sources |
| `modelTexture(texturePath)` / `existingTexture(texturePath)` | Reference an existing texture without writing a PNG |
| `visual(BlockVisualPreset.constantTintedCube(...))` | Reuse a shared block visual rule in batch registration |
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
