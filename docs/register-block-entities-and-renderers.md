---
title: Registering Block Entities and Renderers
parent: Content Guides
nav_order: 3
permalink: /register-block-entities-and-renderers/
---

# Registering Block Entities and Renderers

## What This Page Solves

When a Block needs persistent state, ticking logic, or a custom renderer, you usually need to manage the `BlockEntityType`, the allowed host Blocks, and the client-side renderer together. This page breaks that chain into the smallest reliable structure.

## When This Applies

- You already have a Block and need to attach a BlockEntity.
- You need to bind one or more host Blocks.
- You need to register a client renderer without forcing client classes to load too early on the server.

## Quick Start

```java
public static final BlockEntityEntry<MachineBlockEntity> MACHINE_BE = RegistryLibTest.REGISTRYLIB
        .blockEntity("machine", MachineBlockEntity::new)
        .validBlock(MACHINE_CASING)
        .register();
```

## Example with a Renderer

```java
public static final BlockEntityEntry<MachineBlockEntity> MACHINE_BE = RegistryLibTest.REGISTRYLIB
        .blockEntity("machine", MachineBlockEntity::new)
        .validBlock(MACHINE_CASING)
        .renderer(() -> MachineBlockEntityRenderer::new)
        .register();
```

## Step-by-Step Explanation

1. `blockEntity("machine", MachineBlockEntity::new)` creates the `BlockEntityBuilder`.
2. `.validBlock(...)` or `.validBlocks(...)` defines which Blocks this `BlockEntityType` can attach to.
3. `.renderer(...)` provides the renderer factory and keeps client loading lazy through a `Supplier`.
4. `.register()` submits the registration and returns `BlockEntityEntry<T>`.

{: .warning }
> Renderer-related types must stay on client-only paths. If you reference a renderer class directly without lazy wrapping, the server environment can fail during class loading.

## Common Patterns

### One BlockEntity Bound to Multiple Blocks

Use `.validBlocks(blockA, blockB, blockC)` or an equivalent collection-based form to attach the same logic to multiple host Blocks.

### BlockEntity Without a Renderer

If the object does not need dedicated visual behavior, keep only `validBlock(...)` and `.register()`.

### Register the Block First, Then the BlockEntity

This is the most stable order both to read and to implement. First make the Block and its item, loot, and related resources stand on their own. Then attach BlockEntity behavior afterward.

## Common API Lookup

| Method | Purpose |
| --- | --- |
| `blockEntity(name, factory)` | Create a `BlockEntityBuilder` |
| `validBlock(entry)` | Bind a single host Block |
| `validBlocks(...)` | Bind multiple host Blocks |
| `renderer(supplier)` | Register the renderer factory |
| `register()` | Complete registration |

## Related Links

- [Registering Blocks]({{ '/register-blocks/' | relative_url }})
- [Development and Maintenance]({{ '/development-and-maintenance/' | relative_url }})
- [Troubleshooting]({{ '/troubleshooting/' | relative_url }})