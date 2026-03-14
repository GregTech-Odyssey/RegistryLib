---
title: How to Register Fluids and Buckets
nav_order: 5
---

# How to Register Fluids and Buckets

Fluid registration in RegistryLib is designed to keep the flowing fluid, visual setup, fluid block,
and bucket item inside one connected builder flow. That makes fluid content much easier to scale,
especially for mods with multiple chemicals, molten materials, or custom liquids.

RegistryLibTest shows two common styles: tinted fluids that reuse the library's grayscale textures,
and a fluid that reuses vanilla-style water textures while customizing both the fluid block and the
bucket item. The main benefit is that the whole fluid stack stays readable and localized.

## Tinted Fluid

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON =
        RegistryLibTest.REGISTRYLIB.fluid(
                "molten_iron",
                FLUID_STILL,
                FLUID_FLOW,
                fluid -> {
                    fluid.properties(p -> p.density(3000).viscosity(6000).temperature(1800))
                            .lang("Molten Iron")
                            .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400);
                });
```

## Fluid With Block And Bucket Customization

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> LIQUID_MAGIC =
        RegistryLibTest.REGISTRYLIB.fluid(
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

## Best Fit

This pattern is a strong fit for tech mods, magic systems, and material pipelines where fluids are
not a one-off feature but a repeatable content type that needs clean defaults and predictable setup.