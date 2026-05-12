---
sidebar_position: 5
title: Environment API
description: API reference for attachments, chunk state, world state, debug commands, and worldgen features.
---

# Environment API

The environment API is for attachment-backed state and datapack-backed worldgen. It is separate from crop registration; crops, machines, and blocks can consume these handles from their own logic.

## Entry Points

| What you want | Entry point | Returns |
| --- | --- | --- |
| Raw NeoForge attachment | `attachmentType("id", holder -> defaultValue)` | `AttachmentTypeBuilder<T, RegistryCore>` |
| Chunk-scoped state | `chunkState("id", codec, defaultValue)` | `ChunkStateBuilder<T, RegistryCore>` |
| World-scoped state | `worldState("id", codec, defaultValue)` | `WorldStateBuilder<T, RegistryCore>` |
| Configured and placed worldgen | `worldgenFeature("id", configuredFeature)` | `WorldgenFeatureBuilder<C, RegistryCore>` |

## AttachmentTypeBuilder

| Method | Parameters | Description |
| --- | --- | --- |
| `configure(callback)` | `UnaryOperator<AttachmentType.Builder<T>>` | Apply low-level NeoForge attachment configuration |
| `serialize(codec)` | `MapCodec<T>` | Configure persistent attachment serialization |
| `sync(streamCodec)` | `StreamCodec<? super RegistryFriendlyByteBuf, T>` | Configure attachment sync |
| `register()` | - | Register and return `AttachmentTypeEntry<T>` |

## AttachmentTypeEntry

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `AttachmentType<T>` | Get the registered NeoForge attachment type |
| `getOrCreate(holder)` | `T` | Read or create data on an `IAttachmentHolder` |
| `getIfPresent(holder)` | `Optional<T>` | Read existing data without creating it |
| `set(holder, value)` | `T` | Replace attachment data and return the previous value |
| `remove(holder)` | `T` | Remove attachment data and return the previous value |
| `sync(holder)` | `void` | Sync attachment data through NeoForge |

## ChunkStateBuilder

| Method | Parameters | Description |
| --- | --- | --- |
| `syncOnModify(enabled)` | `boolean` | Request sync after `set` or `modify` when a sync codec exists |
| `sync(streamCodec)` | `StreamCodec<? super RegistryFriendlyByteBuf, T>` | Configure sync and enable sync-on-modify |
| `debug()` | - | Enable read-only debug commands with default permissions |
| `debug(configurator)` | `UnaryOperator<StateDebugConfig>` | Enable and configure debug command permissions and writability |
| `register()` | - | Register and return `ChunkStateEntry<T>` |
| `build()` | - | Register and return the parent chain object |

## ChunkStateEntry

| Method | Return type | Description |
| --- | --- | --- |
| `identifier()` | `Identifier` | Logical state id |
| `scope()` | `StateScope` | Always `CHUNK` |
| `codec()` | `Codec<T>` | Persistent value codec |
| `attachmentType()` | `AttachmentType<T>` | Underlying attachment type |
| `debugConfig()` | `StateDebugConfig` | Debug command configuration |
| `getOrCreate(level, chunkPos)` | `T` | Read or create chunk state |
| `getIfLoaded(level, chunkPos)` | `Optional<T>` | Read without force-loading the chunk |
| `set(level, chunkPos, value)` | `void` | Replace the value and mark the chunk unsaved |
| `modify(level, chunkPos, action)` | `void` | Mutate the value and mark the chunk unsaved |
| `sync(level, chunkPos)` | `void` | Manually sync the state |

## WorldStateBuilder

| Method | Parameters | Description |
| --- | --- | --- |
| `syncOnModify(enabled)` | `boolean` | Request sync after `set` or `modify` when a sync codec exists |
| `sync(streamCodec)` | `StreamCodec<? super RegistryFriendlyByteBuf, T>` | Configure sync and enable sync-on-modify |
| `debug()` | - | Enable read-only debug commands with default permissions |
| `debug(configurator)` | `UnaryOperator<StateDebugConfig>` | Enable and configure debug command permissions and writability |
| `register()` | - | Register and return `WorldStateEntry<T>` |
| `build()` | - | Register and return the parent chain object |

## WorldStateEntry

| Method | Return type | Description |
| --- | --- | --- |
| `identifier()` | `Identifier` | Logical state id |
| `scope()` | `StateScope` | Always `WORLD` |
| `codec()` | `Codec<T>` | Persistent value codec |
| `attachmentType()` | `AttachmentType<T>` | Underlying attachment type |
| `debugConfig()` | `StateDebugConfig` | Debug command configuration |
| `getOrCreate(level)` | `T` | Read or create world state |
| `getIfPresent(level)` | `Optional<T>` | Read without creating state |
| `set(level, value)` | `void` | Replace the value |
| `modify(level, action)` | `void` | Mutate the value |
| `sync(level)` | `void` | Manually sync the state |

## StateDebugConfig

| Method | Description |
| --- | --- |
| `enabled(boolean)` | Enable or disable debug command visibility |
| `writable(boolean)` | Allow `set` commands |
| `readPermission(int)` | Permission level for `list`, `get`, and `debug` |
| `writePermission(int)` | Permission level for `set` |

## WorldgenFeatureBuilder

| Method | Parameters | Description |
| --- | --- | --- |
| `placement(modifier)` | `PlacementModifier` | Add one placed feature modifier |
| `placements(modifiers)` | `List<PlacementModifier>` | Add multiple placed feature modifiers |
| `addToBiomes(tag, step)` | `TagKey<Biome>, GenerationStep.Decoration` | Generate an `AddFeaturesBiomeModifier` for matching biomes |
| `register()` | - | Generate configured/placed feature data and return `WorldgenFeatureEntry` |
| `build()` | - | Register and return the parent chain object |

## WorldgenFeatureEntry

| Method | Return type | Description |
| --- | --- | --- |
| `configuredKey()` | `ResourceKey<ConfiguredFeature<?, ?>>` | Key for the generated configured feature |
| `placedKey()` | `ResourceKey<PlacedFeature>` | Key for the generated placed feature |

## See Also

- [Register Environment State](/how-to/register-environment-state)
- [Crop API](/reference/crop-api)
