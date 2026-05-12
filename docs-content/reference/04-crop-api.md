---
sidebar_position: 4
title: Crop API
description: API reference for CropBuilder and RegistryLibCropBlock.
---

# Crop API

The crop API is for registering carrot-like age crops and their generated seed item. It is separate from environment state; callbacks may use state handles, but state registration belongs to the environment API.

## Entry Point

| What you want | Entry point | Returns |
| --- | --- | --- |
| Register a crop block and seed item | `crop("id")` | `CropBuilder<RegistryCore>` |
| Register a crop inside another chain | `crop(parent, "id")` | `CropBuilder<P>` |

## CropBuilder

| Method | Parameters | Description |
| --- | --- | --- |
| `properties(modifier)` | `UnaryOperator<BlockBehaviour.Properties>` | Configure crop block properties |
| `growthRoll(callback)` | `RegistryLibCropBlock.GrowthRoll` | Decide whether a random tick may advance growth |
| `onHarvest(callback)` | `RegistryLibCropBlock.HarvestCallback` | Run server harvest logic before right-click replanting |
| `rightClickHarvest(enabled)` | `boolean` | Enable or disable mature right-click harvest |
| `seedItem(configurator)` | `Consumer<ItemBuilder<Item, RegistryCore>>` | Configure the generated seed item |
| `produce(supplier)` | `Supplier<? extends Item>` | Configure mature crop loot |
| `stageTextures(textures...)` | `TextureRef...` | Generate crop stage blockstate/model data |
| `register()` | - | Register crop block and seed item, returning `BlockEntry<RegistryLibCropBlock>` |
| `build()` | - | Register and return the parent chain object |

## RegistryLibCropBlock

`RegistryLibCropBlock` extends vanilla `CropBlock`.

| Behavior | Notes |
| --- | --- |
| Max age | `7`, matching vanilla carrot-style crops |
| Random tick | Calls `growthRoll` before vanilla crop growth |
| Seed item | Supplied by the generated seed entry |
| Mature right-click | Optional; enabled by default |
| Harvest callback | Runs on the server before drops and replanting |

## Callback Types

```java
@FunctionalInterface
public interface GrowthRoll {
    boolean shouldGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);
}
```

```java
@FunctionalInterface
public interface HarvestCallback {
    void onHarvest(BlockState state, Level level, BlockPos pos, Player player);
}
```

## See Also

- [Register Crops](/how-to/register-crops)
- [Environment API](/reference/environment-api)
