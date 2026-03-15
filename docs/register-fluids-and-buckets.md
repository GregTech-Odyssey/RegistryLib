---
title: Register Fluids and Buckets
nav_order: 6
permalink: /register-fluids-and-buckets/
---

# Register Fluids and Buckets

RegistryLib's fluid registration consolidates the flowing fluid, visual settings, fluid block, and bucket item into a single Builder flow. For mods with large numbers of chemicals, molten materials, or custom liquids, this makes fluid content much easier to scale.

---

## Simple Example

The simplest fluid registration: built-in greyscale textures and a display name.

```java
private static final Identifier FLUID_STILL =
        Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_still");
private static final Identifier FLUID_FLOW =
        Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_flow");

public static final FluidEntry<BaseFlowingFluid.Flowing> ACID =
        RegistryLibTest.REGISTRYLIB.fluid(
                "acid",
                FLUID_STILL,
                FLUID_FLOW,
                fluid -> {
                    fluid.lang("Acid")
                            .clientExtension(FLUID_STILL, FLUID_FLOW);
                });
```

---

## Full Example

A full FluidBuilder example covering tinting, physical parameters, block/bucket configuration, and tags.

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON =
        RegistryLibTest.REGISTRYLIB.fluid(
                "molten_iron",
                FLUID_STILL,
                FLUID_FLOW,
                fluid -> {
                    fluid.properties(p -> p.density(3000).viscosity(6000).temperature(1800));
                    fluid.lang("Molten Iron");
                    fluid.clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400);
                    fluid.tag(FluidTags.LAVA);
                    fluid.block(block -> block.properties(p -> p.lightLevel(s -> 12)));
                    fluid.bucket(bucket -> bucket.lang("Molten Iron Bucket"));
                });

public static final FluidEntry<BaseFlowingFluid.Flowing> LIQUID_MAGIC =
        RegistryLibTest.REGISTRYLIB.fluid(
                "liquid_magic",
                Identifier.withDefaultNamespace("block/water_still"),
                Identifier.withDefaultNamespace("block/water_flow"),
                fluid -> {
                    fluid.properties(p -> p.lightLevel(15).density(500).viscosity(200));
                    fluid.lang("Liquid Magic");
                    fluid.block(block -> block.properties(p -> p.lightLevel(s -> 15)));
                    fluid.bucket(bucket -> bucket.lang("Liquid Magic Bucket"));
                });
```

---

## API Reference

### `clientExtension(Identifier, Identifier)`

Sets the still and flowing textures for the fluid without tinting.

```java
fluid.clientExtension(FLUID_STILL, FLUID_FLOW);
```

When using registrylib's built-in greyscale textures, omitting the colour preserves the original grey tone.

---

### `clientExtension(Identifier, Identifier, int)`

Sets the textures and tints them with an ARGB colour. Designed for use with greyscale textures and runtime tinting.

```java
fluid.clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400);
```

The colour format is ARGB (e.g. `0xFFFF4400` = opaque orange-red). Tinting is also applied to the bucket item model.

---

### `clientExtension(Supplier<Supplier<IClientFluidTypeExtensions>>)`

Fully custom client fluid rendering extension, for advanced usage.

```java
fluid.clientExtension(() -> () -> new IClientFluidTypeExtensions() {
    // custom implementation
});
```

---

### `properties(Consumer<FluidType.Properties>)`

Configures FluidType's physical parameters: density, viscosity, temperature, light level, and more.

```java
fluid.properties(p -> p.density(3000).viscosity(6000).temperature(1800));
```

These parameters affect the fluid's buoyancy calculations, flow-speed display, and tooltip information.

---

### `fluidProperties(Consumer<BaseFlowingFluid.Properties>)`

Configures properties at the `BaseFlowingFluid` level (e.g. manually linking source/block/bucket).

```java
fluid.fluidProperties(p -> p.slopeFindDistance(4).levelDecreasePerBlock(1));
```

In most cases this does not need to be called manually; `block()` and `bucket()` handle the linking automatically.

---

### `lang(String)`

Sets the fluid's display name.

```java
fluid.lang("Molten Iron");
```

---

### `defaultLang()`

Derives the display name automatically from the registry name (e.g. `molten_iron` → `Molten Iron`).

```java
fluid.defaultLang();
```

---

### `source(Function<BaseFlowingFluid.Properties, ? extends BaseFlowingFluid>)`

Sets a custom source fluid factory.

```java
fluid.source(BaseFlowingFluid.Source::new);
```

By default `defaultSource()` is called automatically; use this only when a custom source fluid class is required.

---

### `defaultSource()`

Uses the default `BaseFlowingFluid.Source` as the source fluid.

```java
fluid.defaultSource();
```

---

### `block(Consumer<BlockBuilder<LiquidBlock, FluidBuilder>>)`

Configures the fluid block sub-entry. Block properties can be customised inside the Consumer.

```java
fluid.block(block -> block.properties(p -> p.lightLevel(s -> 15)));
```

Use this to set block-specific properties such as light emission and explosion resistance.

---

### `block(BiFunction, Consumer)`

Uses a custom `LiquidBlock` subclass factory together with a Consumer for further configuration.

```java
fluid.block(MyLiquidBlock::new, block -> { /* configure */ });
```

---

### `noBlock()`

Disables fluid block generation. The fluid will not be placeable as a block in the world.

```java
fluid.noBlock();
```

---

### `defaultBlock()`

Uses the default fluid block settings.

```java
fluid.defaultBlock();
```

---

### `bucket(Consumer<ItemBuilder<BucketItem, FluidBuilder>>)`

Configures the bucket item sub-entry. The display name, creative tab, and more can be set inside the Consumer.

```java
fluid.bucket(bucket -> bucket.lang("Molten Iron Bucket"));
```

---

### `bucket(BiFunction, Consumer)`

Uses a custom `BucketItem` subclass factory together with a Consumer for further configuration.

```java
fluid.bucket(MyBucket::new, bucket -> { /* configure */ });
```

---

### `noBucket()`

Disables bucket item generation.

```java
fluid.noBucket();
```

---

### `defaultBucketTab(ResourceKey<CreativeModeTab>)`

Sets the default creative tab for the bucket item.

```java
fluid.defaultBucketTab(CreativeModeTabs.TOOLS_AND_UTILITIES);
```

---

### `tag(TagKey<Fluid>...)`

Adds tags to the fluid.

```java
fluid.tag(FluidTags.LAVA);
```

Tags are applied to both the source fluid and the flowing fluid.

---

### `removeTag(TagKey<Fluid>...)`

Removes previously added fluid tags.

```java
fluid.removeTag(FluidTags.LAVA);
```

---

## Texture Conventions

The default textures provided by RegistryLib are located at:
- `registrylib:block/fluid/liquid_still` — still texture (greyscale)
- `registrylib:block/fluid/liquid_flow` — flowing texture (greyscale)

These are single-channel greyscale images designed for runtime tinting via `clientExtension(still, flow, color)`. When using a custom coloured texture (e.g. vanilla water), use the two-argument overload instead.