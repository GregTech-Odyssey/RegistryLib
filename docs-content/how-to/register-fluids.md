---
sidebar_position: 3
title: Register Fluids
description: Quick reference for fluid and bucket registration.
---

# Register Fluids

## Simple Fluid

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> ACID = REGISTRYLIB
        .fluid("acid", FLUID_STILL, FLUID_FLOW, BaseFlowingFluid.Flowing::new)
        .lang("Acid")
        .register();
```

## Full Fluid with Block and Bucket

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON = REGISTRYLIB
        .fluid("molten_iron", FLUID_STILL, FLUID_FLOW)
        .properties(p -> p.density(3000).viscosity(6000).temperature(1800))
        .lang("Molten Iron")
        .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400)
        .tag(FluidTags.LAVA)
        .block(block -> block.properties(p -> p.lightLevel(s -> 12)))
        .bucket(bucket -> bucket.lang("Molten Iron Bucket"))
        .register();
```

:::tip
When choosing a `clientExtension(...)` overload, decide whether your texture already contains color. Grayscale textures need an explicit tint; colored textures usually do not.
:::

## FluidEntry Accessors

The returned `FluidEntry<T>` provides access to the entire fluid family:

| Method | Returns |
|---|---|
| `getSource()` | The source fluid |
| `getType()` | The shared `FluidType` |
| `getBlock()` | The fluid block (if registered) |
| `getBucket()` | The bucket item (if registered) |
| `asStack(...)` | Transfer-friendly fluid stack |
| `asResource(...)` | Fluid resource value |
| `readOnlyStack(...)` | Read-only fluid stack for display |

## Common API Lookup

| Method | Purpose |
|---|---|
| `fluid(name, still, flow)` | Create a `FluidBuilder` |
| `lang(text)` | Set the display name |
| `properties(...)` | Modify fluid physical properties (density, viscosity, temperature) |
| `clientExtension(...)` | Configure client-side rendering |
| `block(...)` | Generate the fluid block |
| `bucket(...)` | Generate the bucket item |
| `tag(...)` | Apply a fluid tag |
| `register()` | Complete registration and return `FluidEntry<T>` |

## Common Patterns

**Fluid + bucket only:** Keep `.bucket()` and omit `.block()`.

**Fluid as a world block:** Add `.block()` and extend it with tags or interaction logic as needed.

**Rendering issues:** Check still/flow texture paths first, then verify `clientExtension(...)` parameters match your texture strategy, then check whether datagen ran correctly.

## See Also

- [Reference: Entry Types](/reference/entry-types)
- [Reference: API Overview](/reference/api-overview)
