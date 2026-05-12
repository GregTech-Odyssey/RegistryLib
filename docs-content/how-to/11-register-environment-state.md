---
sidebar_position: 11
title: Register Environment State
description: Register attachment-backed chunk and world state, debug commands, and worldgen features.
---

# Register Environment State

Use environment state APIs when a mod needs local values such as ambient essence, depletion, fertility, pollution, or dimension-level counters.

This API is state-facing. It does not register crops; crops and machines can read these handles from their own callbacks.

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

Use `modify(...)` for mutable values so RegistryLib can mark the chunk dirty and run the configured sync path.

```java
AMBIENT_ESSENCE.modify(serverLevel, chunkPos, value -> {
    // mutate a mutable state object here
});
```

For immutable values such as `Integer`, compute a new value and call `set(...)`:

```java
AMBIENT_ESSENCE.set(serverLevel, chunkPos, AMBIENT_ESSENCE.getOrCreate(serverLevel, chunkPos) + 1);
```

## World State

Use `worldState(...)` for dimension-level values attached to `ServerLevel`.

```java
public static final WorldStateEntry<Integer> ESSENCE_EPOCH = REGISTRYLIB
        .worldState("essence_epoch", Codec.INT, () -> 0)
        .debug(config -> config.writable(true).writePermission(4))
        .register();
```

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

- [Environment API](/reference/environment-api)
- [Register Crops](/how-to/register-crops)
- [API Overview](/reference/api-overview)
