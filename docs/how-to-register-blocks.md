---
title: How to Register Blocks
nav_order: 3
---

# How to Register Blocks

Block registration follows the same fluent style, which makes it easy to keep the block definition,
its properties, its loot behavior, and its generated BlockItem close together. You can start with a
simple block or move into custom subclasses and shared configuration groups without changing the
overall registration pattern.

RegistryLibTest demonstrates both ends of that range: a minimal block with a linked BlockItem, a
custom ore drop, and a timer block family built from a shared Group. This is the part of the API
that helps most when your mod has multiple tiers or many blocks that share the same defaults.

## Block With Linked Item

```java
public static final BlockEntry<Block> TEST_BLOCK = RegistryLibTest.REGISTRYLIB.block(
        "test_block",
        Block::new,
        block -> {
            block.initialProperties(() -> Blocks.STONE)
                    .item(itemBuilder ->
                            itemBuilder.tooltip(Component.literal("A solid test block")));
        });
```

## Block With Custom Loot

```java
public static final BlockEntry<Block> MAGIC_ORE = RegistryLibTest.REGISTRYLIB.block(
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

## Shared Group Configuration

```java
public static final Group TIMER_GROUP = RegistryLibTest.REGISTRYLIB
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
                        collector.node(new SubNode.Basic(Component.literal("Tier 1 Timer"), 0), true, false);
                        collector.node(new SubNode.Basic(Component.literal("Tick interval: 20"), 10));
                    }));
        });
```