---
sidebar_position: 10
title: Register Environment Features
description: Register source-essence style state, crops, and worldgen through RegistryLib.
---

# Register Environment Features

RegistryLib can express source-essence style gameplay with three connected APIs:

- scoped world or chunk state backed by NeoForge attachments
- carrot-like crops that can read environment state while growing
- datapack-backed worldgen features and biome modifiers

Use these APIs when a mod needs local environment values such as ambient essence, depletion, fertility, pollution, or dimension-level counters.

:::note
State storage is built on NeoForge `AttachmentType` and `IAttachmentHolder`. `SavedData` is still a good fit for large indexes or custom files, but small chunk and world values should usually start with RegistryLib state entries.
:::

## Chunk State

Use `chunkState(...)` for values owned by a chunk.

```java
public static final ChunkStateEntry<Integer> AMBIENT_ESSENCE = REGISTRYLIB
        .chunkState("ambient_essence", Codec.INT, () -> 0)
        .debug(config -> config.writable(true).writePermission(4))
        .register();
```

The returned `ChunkStateEntry<T>` is a boundary handle, not a normal registry entry. It exposes:

| Method | Purpose |
| --- | --- |
| `identifier()` | Logical state id, such as `modid:ambient_essence` |
| `scope()` | Returns `StateScope.CHUNK` |
| `codec()` | Persistent JSON/NBT codec |
| `attachmentType()` | Underlying NeoForge attachment type |
| `debugConfig()` | Debug command metadata |
| `getOrCreate(level, chunkPos)` | Read or create the value, loading the chunk if needed |
| `getIfLoaded(level, chunkPos)` | Read only when the chunk is already loaded |
| `set(level, chunkPos, value)` | Replace the value and mark the chunk unsaved |
| `modify(level, chunkPos, action)` | Mutate the value, mark the chunk unsaved, and optionally sync |
| `sync(level, chunkPos)` | Manually sync the attachment |

Use `modify(...)` for mutable values so RegistryLib can mark the chunk dirty and run the configured sync path.

## World State

Use `worldState(...)` for dimension-level values attached to `ServerLevel`.

```java
public static final WorldStateEntry<Integer> ESSENCE_EPOCH = REGISTRYLIB
        .worldState("essence_epoch", Codec.INT, () -> 0)
        .debug(config -> config.writable(true).writePermission(4))
        .register();
```

The returned `WorldStateEntry<T>` exposes:

| Method | Purpose |
| --- | --- |
| `identifier()` | Logical state id |
| `scope()` | Returns `StateScope.WORLD` |
| `codec()` | Persistent JSON/NBT codec |
| `attachmentType()` | Underlying NeoForge attachment type |
| `getOrCreate(level)` | Read or create the value |
| `getIfPresent(level)` | Read only when already present |
| `set(level, value)` | Replace the value |
| `modify(level, action)` | Mutate the value and optionally sync |
| `sync(level)` | Manually sync the attachment |

World and chunk states may share the same identifier path. RegistryLib keeps the scope separate internally.

## Sync

State builders can opt into attachment sync:

```java
REGISTRYLIB.chunkState("ambient_essence", Codec.INT, () -> 0)
        .sync(ByteBufCodecs.INT)
        .register();
```

`sync(codec)` enables sync-on-modify. `syncOnModify(true)` only has an effect when a sync codec is also configured.

## Debug Commands

Call `.debug()` to make a state visible under:

```text
/registrylib state ...
```

Available command shapes:

```text
/registrylib state list
/registrylib state get <state_id>
/registrylib state get <state_id> <chunk_x> <chunk_z>
/registrylib state set <state_id> <json_value>
/registrylib state set <state_id> <chunk_x> <chunk_z> <json_value>
/registrylib state debug <state_id>
/registrylib state debug <state_id> <chunk_x> <chunk_z>
```

Values are parsed with the state's `Codec` from JSON. For example, an integer state accepts `12`, while a record state should use its normal JSON object.

By default, write commands are disabled unless the state debug config is writable:

```java
.debug(config -> config
        .writable(true)
        .readPermission(2)
        .writePermission(4))
```

When a world state and a chunk state share the same path, the command shape decides the scope. `get <id>` reads world state, while `get <id> <chunk_x> <chunk_z>` reads chunk state. `set` uses the chunk form when the value tail starts with two integer chunk coordinates.

## Crop

Use `crop(...)` to register a crop block and its seed item together.

```java
public static final BlockEntry<RegistryLibCropBlock> ESSENCE_CARROT = REGISTRYLIB
        .crop("essence_carrot")
        .properties(properties -> BlockBehaviour.Properties.of()
                .noCollision()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP))
        .produce(() -> Items.CARROT)
        .growthRoll((state, level, pos, random) -> {
            int ambient = AMBIENT_ESSENCE.getOrCreate(level, level.getChunk(pos).getPos());
            return random.nextInt(8) < Math.max(1, Math.min(7, ambient));
        })
        .onHarvest((state, level, pos, player) -> {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                ESSENCE_EPOCH.set(serverLevel, ESSENCE_EPOCH.getOrCreate(serverLevel) + 1);
            }
        })
        .stageTextures(
                TextureRef.mc("block/carrots_stage0"),
                TextureRef.mc("block/carrots_stage1"),
                TextureRef.mc("block/carrots_stage2"),
                TextureRef.mc("block/carrots_stage3"))
        .register();
```

The crop currently behaves like a vanilla `CropBlock` with max age `7`. RegistryLib generates stage blockstate/model data when stage textures are provided and generates crop loot from the configured produce and seed item.

| Method | Purpose |
| --- | --- |
| `properties(...)` | Configure block properties |
| `growthRoll(...)` | Decide whether a random tick advances growth |
| `onHarvest(...)` | Run server-side harvest logic |
| `rightClickHarvest(boolean)` | Enable or disable mature right-click harvest |
| `seedItem(...)` | Configure the generated seed item |
| `produce(...)` | Configure mature crop loot |
| `stageTextures(...)` | Generate crop stage models |
| `register()` | Register crop block and seed item |

## Worldgen Feature

Use `worldgenFeature(...)` to generate configured feature, placed feature, and optional biome modifier data.

```java
public static final WorldgenFeatureEntry ESSENCE_NODE_PATCH = REGISTRYLIB
        .worldgenFeature(
                "essence_node_patch",
                () -> new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(
                                BlockStateProvider.simple(ESSENCE_NODE.get()))))
        .placement(CountPlacement.of(1))
        .placement(InSquarePlacement.spread())
        .placement(HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG))
        .placement(BiomeFilter.biome())
        .addToBiomes(BiomeTags.IS_OVERWORLD, GenerationStep.Decoration.VEGETAL_DECORATION)
        .register();
```

The returned `WorldgenFeatureEntry` contains the configured and placed feature keys:

```java
ResourceKey<ConfiguredFeature<?, ?>> configured = ESSENCE_NODE_PATCH.configuredKey();
ResourceKey<PlacedFeature> placed = ESSENCE_NODE_PATCH.placedKey();
```

`addToBiomes(...)` writes a NeoForge `AddFeaturesBiomeModifier` named `<feature_name>_add_feature`.

## Low-Level Attachments

Use `attachmentType(...)` only when you need direct NeoForge attachment access.

```java
AttachmentTypeEntry<Integer> COUNTER = REGISTRYLIB
        .attachmentType("example_counter", holder -> 0)
        .serialize(Codec.INT.fieldOf("value"))
        .register();
```

`AttachmentTypeEntry<T>` exposes `getOrCreate`, `getIfPresent`, `set`, `remove`, and `sync` for any `IAttachmentHolder`.

## Current Boundaries

- `DataComponentType` is still for `ItemStack` data.
- `DataMapType` is still for static registry-object configuration.
- Capabilities should expose interaction APIs, not act as the persistent storage container.
- Chunk and world state are small attachment-backed values.
- Large global indexes should use a separate storage design and may reference these state entries as summaries or handles.

## See Also

- [API Overview](/reference/api-overview)
- [Entry Types](/reference/entry-types)
- [Builder Methods](/reference/builder-methods)
