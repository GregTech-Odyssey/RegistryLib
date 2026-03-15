---
title: Registering Fluids and Buckets
parent: Content Guides
nav_order: 4
permalink: /register-fluids-and-buckets/
---

# Registering Fluids and Buckets

## What This Page Solves

In RegistryLib, a Fluid is usually not a single object. It is a connected family that can include the source fluid, flowing fluid, fluid type, optional block, optional bucket, and client-side rendering extensions. This page shows how to keep that family inside one maintainable chain.

## When This Applies

- You want to register a full fluid family.
- You want to generate a bucket or fluid block at the same time.
- You need to configure still and flow textures together with client rendering behavior.

## Quick Start

```java
public static final FluidEntry<BaseFlowingFluid> OIL = RegistryLibTest.REGISTRYLIB
        .fluid("oil", rl("block/oil_still"), rl("block/oil_flow"))
        .lang("Oil")
        .bucket()
        .register();
```

## Full Example

```java
public static final FluidEntry<BaseFlowingFluid> STEAM = RegistryLibTest.REGISTRYLIB
        .fluid("steam", rl("block/steam_still"), rl("block/steam_flow"))
        .lang("Steam")
        .properties(props -> props.density(-500).viscosity(100))
        .clientExtension(0xCCFFFFFF, rl("block/steam_still"), rl("block/steam_flow"))
        .block()
        .bucket()
        .register();
```

## Step-by-Step Explanation

1. `fluid("steam", still, flow)` creates the `FluidBuilder` and fixes the still and flow texture resources.
2. `.lang(...)` sets the display name.
3. `.properties(...)` customizes the fluid type or fluid properties.
4. `.clientExtension(...)` defines client rendering parameters, commonly for tinting or texture strategy.
5. `.block()` generates the fluid block.
6. `.bucket()` generates the bucket item.
7. `.register()` returns `FluidEntry<T>` so the related fluid objects can be accessed together.

{: .important }
> When choosing a `clientExtension(...)` overload, decide first whether your texture already contains color information. Grayscale textures usually need an explicit tint, while colored textures usually do not.

## Common Patterns

### I Only Want the Fluid and the Bucket

Keep `.bucket()` and omit `.block()`.

### I Want the Fluid to Exist as a World Block

Add `.block()` and then extend it with tags or interaction logic if needed.

### What Should I Check First When Fluid Rendering Looks Wrong?

Check the still and flow texture paths first. Then check whether the `clientExtension(...)` parameters match the texture strategy. Only after that should you investigate whether the resource generation path ran correctly.

## Common API Lookup

| Method | Purpose |
| --- | --- |
| `fluid(name, still, flow)` | Create a `FluidBuilder` |
| `lang(text)` | Set the display name |
| `properties(...)` | Modify fluid properties |
| `clientExtension(...)` | Configure client rendering |
| `block()` | Generate the fluid block |
| `bucket()` | Generate the bucket |
| `register()` | Complete registration |

## Related Links

- [Troubleshooting]({{ '/troubleshooting/' | relative_url }})
- [API Reference]({{ '/api-reference/' | relative_url }})
- [Advanced Topics]({{ '/advanced-topics/' | relative_url }})