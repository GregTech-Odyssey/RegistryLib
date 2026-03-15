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

{: .note }
> The code snippets on this page are excerpted from `SimpleFluidExample` and `FullFluidExample` in `RegistryLibTest`. They match the current test sources that pass `runData`.

## Quick Start

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> ACID = RegistryLibTest.REGISTRYLIB
        .fluid(
                RegistryLibTest.REGISTRYLIB,
                "acid",
                FLUID_STILL,
                FLUID_FLOW,
                BaseFlowingFluid.Flowing::new)
        .langCn("酸液")
        .lang("Acid")
        .register();
```

## Full Example

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON = RegistryLibTest.REGISTRYLIB
        .fluid("molten_iron", FLUID_STILL, FLUID_FLOW)
        .properties(p -> p.density(3000).viscosity(6000).temperature(1800))
        .lang("Molten Iron")
        .lang(ModRegistryCore.LANG_ZH_CN, "熔融铁")
        .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400)
        .tag(FluidTags.LAVA)
        .block(block -> block.properties(p -> p.lightLevel(s -> 12)))
        .bucket(bucket -> bucket.lang("Molten Iron Bucket").lang(ModRegistryCore.LANG_ZH_CN, "熔融铁桶"))
        .register();
```

## Step-by-Step Explanation

1. `fluid(parent, "acid", still, flow, factory)` in `SimpleFluidExample` is the explicit-parent path used by the test project to expose `langCn(...)`.
2. `fluid("molten_iron", still, flow)` in `FullFluidExample` uses the standard fluent path for a larger fluid family.
3. `.properties(...)`, `.lang(...)`, and `.clientExtension(...)` configure the physical behavior, localized name, and rendering strategy.
4. `.tag(...)` applies the fluid tag used by the runnable example.
5. `.block(...)` and `.bucket(...)` show the sub-entry configuration pattern actually used in the test code.
6. `.register()` returns `FluidEntry<T>` so the related fluid objects can be accessed together.

{: .important }
> When choosing a `clientExtension(...)` overload, decide first whether your texture already contains color information. Grayscale textures usually need an explicit tint, while colored textures usually do not.

## Working with the Returned `FluidEntry`

The returned `FluidEntry<T>` now does more than hold the flowing fluid reference.

- `getSource()` gives you the source fluid.
- `getType()` gives you the shared `FluidType`.
- `getBlock()` and `getBucket()` let you reach the related sub-entries when they exist.
- `asStack(...)` and `asResource(...)` build transfer-friendly fluid values without reassembling them manually.

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