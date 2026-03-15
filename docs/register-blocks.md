---
title: Register Blocks
nav_order: 4
permalink: /register-blocks/
---

# Register Blocks

RegistryLib registers blocks using the same fluent Builder pattern as items. Block definition, properties, loot tables, BlockItem binding, and Group-shared configuration can all be handled in one place.

---

## Simple Example

The simplest block registration: one block, a display name, and an automatic BlockItem.

```java
public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
        .block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

---

## Full Example

A block example exercising every BlockBuilder API, including custom subclasses and the Group system.

```java
// ── single block using every API ──
public static final BlockEntry<Block> MAGIC_ORE = RegistryLibTest.REGISTRYLIB.block(
        "magic_ore",
        Block::new)
        .initialProperties(() -> Blocks.IRON_ORE)
        .properties(p -> p.strength(4.0F, 5.0F).requiresCorrectToolForDrops())
        .lang("Magic Ore")
        .loot((tables, b) ->
                tables.add(b, tables.createOreDrop(b, SimpleItemExample.COPPER_COIN.get())))
        .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
        .recipe((ctx, prov) -> { /* recipe generation */ })
        .item()
            .tooltip((collector, stack) -> {
                collector.node(
                        new SubNode.Basic(Component.literal("§5Drops coins when mined")),
                        true, false);
            })
            .build()
        .register();

// ── custom block subclass ──
public static final BlockEntry<TimerBlock> STANDALONE_TIMER = RegistryLibTest.REGISTRYLIB
        .block("standalone_timer", p -> new TimerBlock(p, 4))
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .lang("Standalone Timer")
        .defaultLoot()
        .defaultBlockstate()
        .simpleItem()
        .register();
```

---

## API Reference

### `initialProperties(Supplier<? extends Block>)`

Copies properties from an existing block to use as the base.

```java
block.initialProperties(() -> Blocks.IRON_ORE);
```

Equivalent to `BlockBehaviour.Properties.ofFullCopy(block)`. Commonly used to inherit hardness, sounds, and required tool tier from a vanilla block.

---

### `properties(UnaryOperator<BlockBehaviour.Properties>)`

Appends modifications on top of the existing properties. Can be called multiple times; effects accumulate.

```java
block.properties(p -> p.strength(4.0F, 5.0F).requiresCorrectToolForDrops());
```

---

### `lang(String)`

Sets the block's display name and writes it to the language file automatically.

```java
block.lang("Magic Ore");
```

---

### `defaultLang()`

Derives the display name automatically from the registry name (e.g. `magic_ore` → `Magic Ore`).

```java
block.defaultLang();
```

---

### `simpleItem()`

Automatically generates a corresponding BlockItem with default settings, requiring no additional configuration.

```java
block.simpleItem();
```

Suitable for decorative or simple material blocks that don't need a customised BlockItem.

---

### `item()`

Creates a BlockItem sub-entry and returns its ItemBuilder for chain configuration. Call `.build()` to return to the parent BlockBuilder.

```java
block.item()
    .tooltip(Component.literal("§5Drops coins when mined"))
    .build();
```

---

### `item(BiFunction)`

Uses a custom BlockItem factory and returns its ItemBuilder for chain configuration.

```java
block.item(MyBlockItem::new)
    .lang("Custom Block Item")
    .build();
```

---

### `blockEntity(BlockEntityFactory)`

Creates a BlockEntity sub-entry inline and returns its builder. Call `.build()` to return to the parent BlockBuilder.

```java
block.blockEntity(MyBlockEntity::new)
    .renderer(() -> MyRenderer::new)
    .build();
```

---

### `defaultBlockstate()`

Uses the default full-cube blockstate model.

```java
block.defaultBlockstate();
```

---

### `blockstate(Supplier<BiConsumer<DataGenContext, RegistryLibBlockModelGenerator>>)`

Customises the blockstate and model generation logic.

```java
block.blockstate(() -> (ctx, prov) -> {
    prov.createTrivialCube(ctx.getEntry());
});
```

---

### `defaultLoot()`

Uses the default loot table (drops the block itself).

```java
block.defaultLoot();
```

---

### `loot(BiConsumer<RegistryLibBlockLootTables, T>)`

Customises the loot table generation logic.

```java
block.loot((tables, b) ->
        tables.add(b, tables.createOreDrop(b, Items.DIAMOND)));
```

Suitable for ore drops, Silk Touch checks, and other custom drop logic.

---

### `recipe(BiConsumer<DataGenContext, RegistryLibRecipeProvider>)`

Generates a recipe through DataGen.

```java
block.recipe((ctx, prov) -> {
    // ShapedRecipeBuilder and other recipe builders
});
```

---

### `tag(TagKey<Block>...)`

Adds one or more tags to the block.

```java
block.tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL);
```

---

For batch-shared configuration across a content family — shared creative tabs, property modifiers, and lang prefixes — see [Group System]({{ '/group-system/' | relative_url }}).