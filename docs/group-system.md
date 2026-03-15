---
title: Group System
nav_order: 8
permalink: /group-system/
---

# Group System

A `Group` is a wrapper around `RegistryCore` that applies shared defaults — lang prefix, creative tab, block property modifier, and item property modifier — to every entry registered through it.

Without a group, blocks in the same content family each repeat the same `lang(...)`, `tab(...)`, and `blockProperties(...)` calls. A group sets these once and every member entry inherits them automatically.

If you are registering a single isolated block or item, use `RegistryCore.block(...)` directly. Groups are the right tool when your mod has content families: tiers, variants, or any set of entries that belong together.

For block-specific registration options, also see [Register Blocks]({{ '/register-blocks/' | relative_url }}).

---

## Quick Start

```java
// 1. Declare the group once, as static final
public static final Group TIMER_GROUP = REGISTRYLIB.group("timers")
        .langPrefix("Timer")
        .blockProperties(p -> p.strength(5.0F, 6.0F))
        .build();

// 2. Register members through the group
public static final BlockEntry<TimerBlock> TIMER_TIER_1 = TIMER_GROUP.block(
        "tier_1",
        p -> new TimerBlock(p, 1),
        block -> block.initialProperties(() -> Blocks.IRON_BLOCK).simpleItem());

public static final BlockEntry<TimerBlock> TIMER_TIER_2 = TIMER_GROUP.block(
        "tier_2",
        p -> new TimerBlock(p, 2),
        block -> block.initialProperties(() -> Blocks.IRON_BLOCK).simpleItem());
```

What this does:

- `TIMER_TIER_1` gets the display name `"Timer Tier 1"` automatically.
- `TIMER_TIER_2` gets `"Timer Tier 2"`.
- Both inherit `strength(5.0F, 6.0F)` without repeating it.

---

## Choose the Right Approach

| Situation | Use |
| --- | --- |
| One isolated block or item | `RegistryCore.block(...)` / `RegistryCore.item(...)` directly |
| Multiple entries sharing block properties | `Group` with `.blockProperties(...)` |
| Multiple entries sharing a creative tab | `Group` with `.tab(...)` |
| Tiered content with a consistent name pattern | `Group` with `.langPrefix(...)` |
| One entry in the family needs different values | `Group` entry with an in-consumer override |

---

## Core Concepts

### What a Group Applies Automatically

Group defaults are applied before the per-entry config consumer runs:

| Default | Applied to |
| --- | --- |
| `blockProperties` modifier | All blocks registered through the group |
| `itemProperties` modifier | All standalone items registered through the group |
| `tab` | Block items, standalone items, and fluid buckets |
| `langPrefix` | All entries (prefix prepended to the entry name) |

The per-entry consumer runs after the group defaults, so anything you set there overrides the group.

### Lang Prefix

The display name formula is: `"<langPrefix> <TitleCase(entryName)>"`.

- `langPrefix("Timer")` + entry name `tier_1` → `"Timer Tier 1"`
- `langPrefix("Timer")` + entry name `tier_2` → `"Timer Tier 2"`

The group name itself auto-derives the lang prefix: `group("machines")` gives `"Machines"` unless you override with `.langPrefix(...)`.

To fully override a single entry's name, call `.lang("My Name")` inside its per-entry consumer — it takes precedence over the group-derived name.

### Creative Tab

`.tab(tab)` on the group propagates to:

- the `BlockItem` for blocks registered with `.simpleItem()` or `.item(...)`
- standalone `Item` entries
- fluid bucket items

Block entities and raw block entries without a block item are not affected by the tab setting.

---

## Step by Step

### 1. Create a Group

```java
public static final Group MACHINES = REGISTRYLIB.group("machines")
        .tab(MY_MACHINE_TAB)
        .blockProperties(p -> p.strength(3.5F).requiresCorrectToolForDrops())
        .build();
```

The name `"machines"` auto-derives `langPrefix = "Machines"`. Override it with `.langPrefix(...)` when the auto-derived form is wrong.

### 2. Register Blocks

```java
public static final BlockEntry<Block> CRUSHER = MACHINES.block(
        "crusher", Block::new,
        block -> block.initialProperties(() -> Blocks.IRON_BLOCK).defaultLoot().simpleItem());

public static final BlockEntry<Block> GRINDER = MACHINES.block(
        "grinder", Block::new,
        block -> block.initialProperties(() -> Blocks.IRON_BLOCK).defaultLoot().simpleItem());
```

Both inherit `strength(3.5F)`, `requiresCorrectToolForDrops()`, the tab, and display names `"Machines Crusher"` / `"Machines Grinder"`.

### 3. Register Standalone Items

```java
public static final ItemEntry<Item> CIRCUIT_BOARD = MACHINES.item(
        "circuit_board", Item::new,
        item -> item.defaultModel());
```

The item inherits the group tab and gets display name `"Machines Circuit Board"`.

### 4. Override a Default Per Entry

The per-entry consumer always runs last, so it can override any group default:

```java
public static final BlockEntry<Block> PROTOTYPE = MACHINES.block(
        "prototype", Block::new,
        block -> {
            block.initialProperties(() -> Blocks.GOLD_BLOCK);
            block.properties(p -> p.strength(1.0F));       // overrides group strength
            block.lang("Prototype Machine");                // overrides group lang prefix
            block.item(itemBuilder -> itemBuilder
                    .removeTab(MY_MACHINE_TAB)              // removes group tab
                    .tab(CreativeModeTabs.TOOLS_AND_UTILITIES));
        });
```

### 5. Register Block Entities

```java
public static final BlockEntityEntry<MyBlockEntity> MY_BE = MACHINES.blockEntity(
        "my_machine",
        (pos, state) -> new MyBlockEntity(pos, state),
        be -> be.renderer(() -> MyRenderer::new));
```

Group tab, lang prefix, and property modifiers do not apply to block entity entries.

### 6. Register Fluids

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> ACID = MACHINES.fluid(
        "acid",
        new Identifier("mymod:block/acid_still"),
        new Identifier("mymod:block/acid_flowing"),
        fluid -> fluid.properties(p -> p.viscosity(500)));
```

The group tab is automatically applied to the bucket item, and the lang prefix is applied to the fluid display name.

---

## Copy-Paste Recipes

### Recipe: Ore Family

```java
public static final Group ORES = REGISTRYLIB.group("ores")
        .tab(CreativeModeTabs.BUILDING_BLOCKS)
        .blockProperties(p -> p.requiresCorrectToolForDrops())
        .build();

public static final BlockEntry<Block> COPPER_ORE = ORES.block(
        "copper_ore", Block::new,
        block -> block.initialProperties(() -> Blocks.COPPER_ORE).defaultLoot().simpleItem());

public static final BlockEntry<Block> SILVER_ORE = ORES.block(
        "silver_ore", Block::new,
        block -> block.initialProperties(() -> Blocks.IRON_ORE).defaultLoot().simpleItem());
```

### Recipe: Machine Tiers with Per-Entry Tooltips

```java
public static final Group MACHINES = REGISTRYLIB.group("machines")
        .tab(MY_TAB)
        .blockProperties(p -> p.strength(3.5F).requiresCorrectToolForDrops())
        .build();

public static final BlockEntry<Block> TIER_1 = MACHINES.block(
        "tier_1", Block::new,
        block -> block.initialProperties(() -> Blocks.IRON_BLOCK)
                .item(itemBuilder -> itemBuilder.tooltip((collector, stack) ->
                        collector.node(new SubNode.Basic(Component.literal("§aBasic"), 0), true, false)))
                .defaultLoot());

public static final BlockEntry<Block> TIER_2 = MACHINES.block(
        "tier_2", Block::new,
        block -> block.initialProperties(() -> Blocks.DIAMOND_BLOCK)
                .item(itemBuilder -> itemBuilder.tooltip((collector, stack) ->
                        collector.node(new SubNode.Basic(Component.literal("§bAdvanced"), 0), true, false)))
                .defaultLoot());
```

### Recipe: Separate Group per Content Type

```java
// Decorative group — no required-tool enforcement
public static final Group DECORATIVE = REGISTRYLIB.group("decorative")
        .tab(CreativeModeTabs.BUILDING_BLOCKS)
        .blockProperties(p -> p.strength(1.5F))
        .build();

// Machine group — enforces tool, higher strength
public static final Group MACHINES = REGISTRYLIB.group("machines")
        .tab(MY_MACHINE_TAB)
        .blockProperties(p -> p.strength(5.0F).requiresCorrectToolForDrops())
        .build();
```

---

## Full Example

```java
public class MachinesExample {

    public static final Group MACHINES = RegistryLibTest.REGISTRYLIB
            .group("machines")
            .tab(CreativeModeTabs.BUILDING_BLOCKS)
            .blockProperties(p -> p.strength(3.5F).requiresCorrectToolForDrops())
            .build();

    // Blocks — each inherits tab, strength, requiresCorrectToolForDrops, and lang prefix "Machines"
    public static final BlockEntry<Block> CRUSHER = MACHINES.block(
            "crusher", Block::new,
            block -> block.initialProperties(() -> Blocks.IRON_BLOCK).defaultLoot().simpleItem());

    public static final BlockEntry<Block> GRINDER = MACHINES.block(
            "grinder", Block::new,
            block -> block.initialProperties(() -> Blocks.IRON_BLOCK).defaultLoot().simpleItem());

    // Standalone item — inherits tab and lang prefix
    public static final ItemEntry<Item> CIRCUIT_BOARD = MACHINES.item(
            "circuit_board", Item::new,
            item -> item.defaultModel());

    // Block that deviates from the group on strength, name, and tab
    public static final BlockEntry<Block> PROTOTYPE = MACHINES.block(
            "prototype", Block::new,
            block -> {
                block.initialProperties(() -> Blocks.GOLD_BLOCK);
                block.properties(p -> p.strength(1.0F));
                block.lang("Prototype Machine");
                block.item(itemBuilder -> itemBuilder
                        .removeTab(CreativeModeTabs.BUILDING_BLOCKS)
                        .tab(CreativeModeTabs.TOOLS_AND_UTILITIES));
            });
}
```

Resulting entry names and display names:

| Entry | Display Name |
| --- | --- |
| `"crusher"` | `"Machines Crusher"` |
| `"grinder"` | `"Machines Grinder"` |
| `"circuit_board"` | `"Machines Circuit Board"` |
| `"prototype"` | `"Prototype Machine"` (manually overridden) |

---

## Best Practices

- Declare the group `static final` before the entries that depend on it — initialization order matters.
- Use `.langPrefix(...)` explicitly when the group name auto-derives an awkward or incorrect prefix.
- Use groups for three or more related entries; for one or two, the overhead is not worth it.
- Per-entry overrides are normal and safe. The group does not prevent individual customization.
- Use `.item(...)` in the block consumer rather than a separate `group.item(...)` call when the item tightly belongs to a specific block.

---

## Source Reference

- `src/main/java/com/gto/registrylib/Group.java` — full implementation
- `src/main/java/com/gto/registrylibtest/block/FullBlockExample.java` — practical usage in the test mod

---

## API Reference

### `RegistryCore.group(String name)` → `Group.Builder`

Entry point for creating a group. The `name` parameter auto-derives a `langPrefix` in title case unless overridden.

### `Group.Builder`

| Method | Description |
| --- | --- |
| `langPrefix(String)` | Override the auto-derived lang prefix. The prefix is prepended to every entry's display name. |
| `tab(ResourceKey<CreativeModeTab>)` | Apply a creative tab to all item-like entries (block items, standalone items, fluid buckets). |
| `blockProperties(UnaryOperator<BlockBehaviour.Properties>)` | Apply a block properties modifier to all block entries. |
| `itemProperties(UnaryOperator<Item.Properties>)` | Apply an item properties modifier to all standalone item entries. |
| `build()` | Construct and return the `Group`. |

### `Group` — Entry Registration

| Method | Description |
| --- | --- |
| `block(name, factory, config)` | Register a block with group defaults applied. |
| `item(name, factory, config)` | Register a standalone item with group defaults applied. |
| `blockEntity(name, factory, config)` | Register a block entity. Group tab, lang prefix, and property modifiers do not apply. |
| `fluid(name, stillTexture, flowingTexture, config)` | Register a fluid using the default `BaseFlowingFluid.Flowing` factory. Group tab applies to the bucket. |
| `fluid(name, stillTexture, flowingTexture, fluidFactory, config)` | Register a fluid with a custom factory. |

### Application Order

For each entry, defaults are applied in this order:

1. Group `blockProperties` / `itemProperties` modifier
2. Group `tab`
3. Group `langPrefix` (derives a display name)
4. Per-entry config consumer (may override any of the above)
