---
title: How to Register Block Entities and Renderers
nav_order: 4
---

# How to Register Block Entities and Renderers

RegistryLib lets you bind a BlockEntity type to one or more valid blocks and connect a client
renderer in the same registration flow. That keeps the registration entry focused and makes the
server-side and client-side responsibilities easier to reason about.

In RegistryLibTest, the timer BlockEntity is shared by three block tiers and connects to its
renderer through a lazy supplier. This pattern matters because it avoids loading client-only classes
on the server while still keeping the renderer hookup close to the BlockEntity declaration.

## BlockEntity With Valid Blocks And Renderer

```java
public static final BlockEntityEntry<TimerBlockEntity> TIMER_BLOCK_ENTITY =
        RegistryLibTest.REGISTRYLIB.blockEntity(
                "timer",
                TimerBlockEntity::new,
                be -> {
                    be.validBlocks(
                                    BlockTest.TIMER_TIER_1,
                                    BlockTest.TIMER_TIER_2,
                                    BlockTest.TIMER_TIER_3)
                            .renderer(() -> TimerBlockEntityRenderer::new);
                });
```

## Why This Pattern Helps

Use this when multiple related blocks should share one BlockEntity implementation, or when your mod
needs a custom visual layer for machine blocks, counters, displays, or other interactive content.
It keeps the registration compact while preserving proper client isolation.