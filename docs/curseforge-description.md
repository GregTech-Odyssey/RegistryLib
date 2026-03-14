---
title: CurseForge Description
nav_order: 2
---

# RegistryLib

RegistryLib is a NeoForge library mod built to reduce registry boilerplate and make common content
setup feel much more consistent. Instead of scattering item registration, block setup, fluid
definitions, tooltip logic, and data-related glue code across multiple classes, you can keep that
work inside a clean fluent builder workflow.

The RegistryLibTest module shows the everyday use cases that matter most for real mod development.
It covers fast item and block registration, automatic item bindings, custom loot behavior, grouped
configuration for content families, and tooltip composition that stays readable even when your
content starts getting more complex.

RegistryLib also gives fluid content a much cleaner setup path. The examples include tinted fluids,
vanilla-style texture reuse, linked fluid blocks, and bucket item customization, which makes it a
strong fit for tech mods, material-heavy mods, and projects that need to add a lot of structured
content without drowning in setup code.

For larger projects, the library goes beyond basic registration. RegistryLibTest demonstrates
CompositeItem attachments, BlockEntity binding, client renderer integration, and structured tooltip
nodes, so the API remains useful even after your mod grows past simple items and decorative blocks.

If you want a registry workflow that scales from quick prototypes to full content mods without
turning into maintenance-heavy boilerplate, RegistryLib is designed for that job. It is a practical
builder-based toolkit for NeoForge developers who want cleaner registration code and more time to
focus on gameplay.

## Example Snippets

### Basic Item Registration

```java
public static final ItemEntry<Item> TEST_ITEM = REGISTRYLIB.item(
        "test_item",
        Item::new,
        item -> {
            item.tooltip((collector, stack) -> {
                collector.node(new SubNode.Basic(Component.literal("A simple test item")));
            });
        });
```

### Block With Custom Loot And Tooltip

```java
public static final BlockEntry<Block> MAGIC_ORE = REGISTRYLIB.block(
        "magic_ore",
        Block::new,
        block -> {
            block.initialProperties(() -> Blocks.IRON_ORE)
                    .loot((tables, b) ->
                            tables.add(b, tables.createOreDrop(b, ItemTest.MAGIC_DUST.get())))
                    .item(itemBuilder -> itemBuilder.tooltip((collector, stack) -> {
                        collector.node(
                                new SubNode.Basic(Component.literal("Drops Magic Dust when mined")),
                                true,
                                false);
                    }));
        });
```

### Shared Configuration With Groups

```java
public static final Group TIMER_GROUP = REGISTRYLIB
        .group("timers")
        .langPrefix("Timer")
        .blockProperties(p -> p.strength(5.0F, 6.0F))
        .build();

public static final BlockEntry<TimerBlock> TIMER_TIER_1 = TIMER_GROUP.block(
        "tier_1",
        p -> new TimerBlock(p, 1),
        block -> {
            block.initialProperties(() -> Blocks.IRON_BLOCK)
                    .item(itemBuilder -> itemBuilder.tooltip((collector, stack) -> {
                        collector.node(
                                new SubNode.Basic(Component.literal("Tier 1 Timer"), 0), true, false);
                        collector.node(
                                new SubNode.Basic(Component.literal("Tick interval: 20"), 10));
                    }));
        });
```

### Fluid, Block, And Bucket Setup In One Flow

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> LIQUID_MAGIC =
        REGISTRYLIB.fluid(
                "liquid_magic",
                Identifier.withDefaultNamespace("block/water_still"),
                Identifier.withDefaultNamespace("block/water_flow"),
                fluid -> {
                    fluid.properties(p -> p.lightLevel(15).density(500).viscosity(200))
                            .lang("Liquid Magic");

                    fluid.block(block -> block.properties(p -> p.lightLevel(state -> 15)));
                    fluid.bucket(bucket -> bucket.lang("Liquid Magic Bucket"));
                });
```
