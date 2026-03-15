---
title: Register Block Entities and Renderers
nav_order: 5
permalink: /register-block-entities-and-renderers/
---

# Register Block Entities and Renderers

RegistryLib lets you bind a BlockEntity type to blocks and wire up the client renderer in a single fluent registration. Server and client responsibilities are cleanly separated; the renderer is loaded lazily via a `Supplier` to prevent client classes from being loaded on the server.

---

## Simple Example

The simplest BlockEntity registration: binding to one block.

```java
public static final BlockEntityEntry<TimerBlockEntity> SIMPLE_TIMER_BE =
        RegistryLibTest.REGISTRYLIB
                .blockEntity("simple_timer", TimerBlockEntity::new)
                .validBlock(FullBlockExample.STANDALONE_TIMER)
                .register();
```

---

## Full Example

A full BlockEntityBuilder example: multiple block bindings and a client renderer.

```java
public static final BlockEntityEntry<TimerBlockEntity> TIMER_BLOCK_ENTITY =
        RegistryLibTest.REGISTRYLIB
                .blockEntity("timer", TimerBlockEntity::new)
                // bind one BlockEntity type to multiple blocks
                .validBlocks(
                        FullBlockExample.TIMER_TIER_1,
                        FullBlockExample.TIMER_TIER_2,
                        FullBlockExample.TIMER_TIER_3)
                // lazily bind the client renderer via a supplier-of-supplier
                .renderer(() -> TimerBlockEntityRenderer::new)
                .register();
```

---

## API Reference

### `validBlock(Supplier<? extends Block>)`

Registers a block that is allowed to host this BlockEntity.

```java
be.validBlock(FullBlockExample.STANDALONE_TIMER);
```

Use this method for BlockEntities associated with a single block.

---

### `validBlocks(Supplier<? extends Block>...)`

Registers multiple blocks that are allowed to host this BlockEntity.

```java
be.validBlocks(
        FullBlockExample.TIMER_TIER_1,
        FullBlockExample.TIMER_TIER_2,
        FullBlockExample.TIMER_TIER_3);
```

Use this when multiple blocks share the same BlockEntity implementation (e.g. tiered machines).

---

### `renderer(Supplier<BlockEntityRendererProvider>)`

Lazily binds the client renderer. The outer `Supplier` ensures renderer code is only loaded in client contexts, preventing `ClassNotFoundException` on the server.

```java
be.renderer(() -> TimerBlockEntityRenderer::new);
```

{: .important }
> The renderer class (e.g. `TimerBlockEntityRenderer`) is only instantiated on the client; the server runtime never touches it. This is the NeoForge-recommended client-isolation pattern.