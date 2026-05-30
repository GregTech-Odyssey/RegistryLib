---
sidebar_position: 4
title: Register Block Entities
description: Quick reference for block entity and renderer registration.
---

# Register Block Entities

## Simple Block Entity

```java
public static final BlockEntityTypeEntry<TimerBlockEntity> SIMPLE_TIMER_BE = REGISTRYLIB
        .blockEntity("simple_timer", TimerBlockEntity::new)
        .validBlock(FullBlockExample.STANDALONE_TIMER)
        .register();
```

## Block Entity with Renderer

```java
public static final BlockEntityTypeEntry<TimerBlockEntity> TIMER_BLOCK_ENTITY = REGISTRYLIB
        .blockEntity("timer", TimerBlockEntity::new)
        .validBlocks(
                FullBlockExample.TIMER_TIER_1,
                FullBlockExample.TIMER_TIER_2,
                FullBlockExample.TIMER_TIER_3)
        .renderer(() -> () -> TimerBlockEntityRenderer::new)
        .register();
```

:::warning
Renderer classes are client-only. Use the **two-level** supplier form `() -> () -> TimerBlockEntityRenderer::new`. One level is not enough: the JVM resolves a lambda's instantiated return type when the lambda is *created*, so `() -> TimerBlockEntityRenderer::new` (return type `BlockEntityRendererProvider`) would load the client class on the dedicated server even though the body never runs. The extra outer `Supplier` returns a non-client `Supplier`, keeping the renderer type buried in the inner lambda that is only linked on the client.
:::

## Common API Lookup

| Method | Purpose |
|---|---|
| `blockEntity(name, factory)` | Create a `BlockEntityBuilder` |
| `validBlock(entry)` | Bind a single host block |
| `validBlocks(...)` | Bind multiple host blocks |
| `renderer(supplier)` | Register the renderer factory (lazy-loaded) |
| `register()` | Complete registration and return `BlockEntityTypeEntry<T>` |

## Common Patterns

**One BE, multiple blocks:** Use `.validBlocks(blockA, blockB, blockC)` to attach the same logic to several host blocks.

**No renderer needed:** If the block entity has no visual behavior, omit `.renderer(...)` entirely.

**Registration order:** Register the block first (with its item, loot, tags), then attach the block entity afterward. This is the most stable order both to read and implement.

## See Also

- [How-To: Register Blocks](/how-to/register-blocks)
- [Tutorial: Performance](/tutorials/performance)
