---
sidebar_position: 10
title: Register Crops
description: Register crop-like plants, seeds, growth callbacks, harvest callbacks, and crop datagen.
---

# Register Crops

Use `crop(...)` when a plant behaves like a carrot-style age crop and should be registered together with its seed item, stage models, and crop loot.

This API is content-facing. It does not store world state by itself, but growth and harvest callbacks can read or mutate external state such as a chunk essence value.

## Basic Crop

```java
public static final BlockEntry<RegistryLibCropBlock> ESSENCE_CARROT = REGISTRYLIB
        .crop("essence_carrot")
        .properties(properties -> BlockBehaviour.Properties.of()
                .noCollision()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP))
        .produce(() -> Items.CARROT)
        .stageTextures(
                TextureRef.mc("block/carrots_stage0"),
                TextureRef.mc("block/carrots_stage1"),
                TextureRef.mc("block/carrots_stage2"),
                TextureRef.mc("block/carrots_stage3"))
        .register();
```

The builder registers:

- the crop block
- a generated `<name>_seeds` item
- crop blockstate and stage models when textures are supplied
- crop loot using the configured produce item and generated seed item

## Environment-Aware Growth

`growthRoll(...)` runs before vanilla crop growth. Return `true` to let the vanilla random tick proceed, or `false` to block that tick.

```java
.growthRoll((state, level, pos, random) -> {
    int ambient = AMBIENT_ESSENCE.getOrCreate(level, level.getChunk(pos).getPos());
    return random.nextInt(8) < Math.max(1, Math.min(7, ambient));
})
```

Keep environment state registration separate. See [Register Environment State](/how-to/register-environment-state) for `chunkState(...)` and `worldState(...)`.

## Harvest Callback

`onHarvest(...)` runs on mature right-click harvest before the block is destroyed and replanted at age `0`.

```java
.onHarvest((state, level, pos, player) -> {
    if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
        ESSENCE_EPOCH.set(serverLevel, ESSENCE_EPOCH.getOrCreate(serverLevel) + 1);
    }
})
```

The crop's right-click harvest path is server-authoritative:

- client side returns success for interaction feedback
- server side runs the callback
- server side drops mature crop loot
- server side replants the crop at age `0`

## Builder Lookup

| Method | Purpose |
| --- | --- |
| `properties(...)` | Configure crop block properties |
| `growthRoll(...)` | Decide whether a random tick advances growth |
| `onHarvest(...)` | Run harvest logic |
| `rightClickHarvest(boolean)` | Enable or disable mature right-click harvest |
| `seedItem(...)` | Configure the generated seed item |
| `produce(...)` | Configure mature crop loot |
| `stageTextures(...)` | Generate crop stage models |
| `register()` | Register crop block and seed item |

## Current Boundaries

- The current crop block is carrot-like and has max age `7`.
- Custom soil behavior comes from vanilla `CropBlock` rules unless you provide a custom block type later.
- Persistent environmental values should live in the environment state API, not inside the crop builder.

## See Also

- [Crop API](/reference/crop-api)
- [Register Environment State](/how-to/register-environment-state)
- [Register Blocks](/how-to/register-blocks)
