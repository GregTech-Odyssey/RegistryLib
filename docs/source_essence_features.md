# Source Essence Feature APIs

RegistryLib now has the first set of APIs needed to express source-essence style gameplay: persistent environment state, crop-like natural carriers, worldgen entries, and developer-facing debug commands.

The goal is to model a single environmental resource that can be sampled by crops, machines, and later world-scale systems without inventing custom storage. The implementation uses NeoForge attachment APIs as the storage layer and keeps RegistryLib's public API focused on registration and safe handles.

## Feature Surface

The current feature surface has four main parts:

- `attachmentType(...)` registers a NeoForge `AttachmentType`.
- `chunkState(...)` and `worldState(...)` register scoped persistent state handles.
- `crop(...)` registers a carrot-like crop block plus seed, loot, stage models, growth callback, and harvest callback.
- `worldgenFeature(...)` registers configured feature data, placed feature data, and optional biome modifier data.

These APIs are intended to be enough for a first source-essence loop:

1. A chunk stores `ambient_essence`.
2. A crop reads that chunk value while growing.
3. Harvesting the crop can mutate world state such as `essence_epoch`.
4. A worldgen feature can place source-node blocks in chosen biomes.
5. Debug commands let developers inspect and tune state in-game.

## Attachments

Use `attachmentType(...)` when you need direct access to a NeoForge attachment and do not need RegistryLib's state wrapper.

```java
var attachment = REGISTRYLIB
        .attachmentType("example_counter", holder -> 0)
        .serialize(Codec.INT.fieldOf("value"))
        .register();
```

The returned `AttachmentTypeEntry<T>` wraps the registered attachment and exposes:

- `getOrCreate(holder)`
- `getIfPresent(holder)`
- `set(holder, value)`
- `remove(holder)`
- `sync(holder)`

This is a low-level API. Prefer `chunkState(...)` or `worldState(...)` when the value represents gameplay state.

## Chunk State

Use `chunkState(...)` for values attached to `ChunkAccess` / `LevelChunk`.

```java
public static final ChunkStateEntry<Integer> AMBIENT_ESSENCE = REGISTRYLIB
        .chunkState("ambient_essence", Codec.INT, () -> 0)
        .debug(config -> config.writable(true).writePermission(4))
        .register();
```

`ChunkStateEntry<T>` exposes a boundary-oriented handle:

- `identifier()`
- `scope()`
- `codec()`
- `attachmentType()`
- `debugConfig()`
- `getOrCreate(level, chunkPos)`
- `getIfLoaded(level, chunkPos)`
- `set(level, chunkPos, value)`
- `modify(level, chunkPos, action)`
- `sync(level, chunkPos)`

Chunk mutation goes through `set(...)` or `modify(...)`. These methods mark the chunk unsaved and call sync only when the builder was configured with a sync codec.

`getIfLoaded(...)` is the safe read path for debug and boundary checks because it does not force-load a missing chunk.

## World State

Use `worldState(...)` for values attached to a `ServerLevel`.

```java
public static final WorldStateEntry<Integer> ESSENCE_EPOCH = REGISTRYLIB
        .worldState("essence_epoch", Codec.INT, () -> 0)
        .debug(config -> config.writable(true).writePermission(4))
        .register();
```

`WorldStateEntry<T>` exposes:

- `identifier()`
- `scope()`
- `codec()`
- `attachmentType()`
- `debugConfig()`
- `getOrCreate(level)`
- `getIfPresent(level)`
- `set(level, value)`
- `modify(level, action)`
- `sync(level)`

World state is intended for dimension-level values and small coordination state. Large indexes or custom files should still be implemented separately, with the world state acting as a handle or summary when useful.

## Debug Commands

State entries can opt into debug commands with `.debug()` or `.debug(config -> ...)`.

Commands are registered under:

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

Read commands require the state's read permission. Write commands require the state to be debug-enabled, writable, and allowed by the state's write permission.

Values are parsed through the state's `Codec` using JSON. For example, an integer state accepts `7`; a structured state should accept its normal JSON object.

World and chunk states may share the same `Identifier`. Debug command lookup includes the requested scope, so `registrylibtest:ambient_essence` can exist as both a chunk state and a world state without colliding. For `set`, the command dispatches to the chunk form when the tail parses as `<chunk_x> <chunk_z> <json_value>`.

## Crops

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

The builder supports:

- `properties(...)`
- `growthRoll(...)`
- `onHarvest(...)`
- `rightClickHarvest(...)`
- `seedItem(...)`
- `produce(...)`
- `stageTextures(...)`

The registered crop currently behaves like a vanilla `CropBlock` with max age `7`. RegistryLib generates crop stage blockstate/model data when stage textures are provided and generates crop loot using the configured produce and seed.

The right-click harvest path runs only on the server side, calls the harvest callback, destroys the mature crop with drops, and replants age `0`.

## Worldgen Features

Use `worldgenFeature(...)` to register a datapack configured feature and placed feature from Java.

```java
public static final WorldgenFeatureEntry ESSENCE_NODE_PATCH = REGISTRYLIB
        .worldgenFeature(
                "essence_node_patch",
                () -> new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(
                                BlockStateProvider.simple(SimpleBlockExample.DECORATIVE_STONE.get()))))
        .placement(CountPlacement.of(1))
        .placement(InSquarePlacement.spread())
        .placement(HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG))
        .placement(BlockPredicateFilter.forPredicate(
                BlockPredicate.allOf(
                        BlockPredicate.replaceable(),
                        BlockPredicate.noFluid(),
                        BlockPredicate.matchesBlocks(BlockPos.ZERO.below(), Blocks.GRASS_BLOCK))))
        .placement(BiomeFilter.biome())
        .addToBiomes(BiomeTags.IS_OVERWORLD, GenerationStep.Decoration.VEGETAL_DECORATION)
        .register();
```

The builder supports:

- `placement(modifier)`
- `placements(modifiers)`
- `addToBiomes(biomeTag, decorationStep)`
- `register()`
- `build()`

`register()` returns a `WorldgenFeatureEntry` with the configured and placed feature keys. If `addToBiomes(...)` is used, RegistryLib also generates a NeoForge `AddFeaturesBiomeModifier` under `<name>_add_feature`.

## Generated Data

The source-essence example generates data such as:

- crop blockstate and stage models
- seed item model
- crop loot table
- configured feature JSON
- placed feature JSON
- NeoForge biome modifier JSON

This keeps content authoring in Java while still producing vanilla/NeoForge datapack resources.

## Example Entries

The current `registrylibtest` examples demonstrate the API:

- `SimpleStateExample.AMBIENT_ESSENCE`: chunk state, integer codec, writable debug command.
- `SimpleStateExample.ESSENCE_EPOCH`: world state, integer codec, writable debug command.
- `SimpleStateExample.AMBIENT_ESSENCE_WORLD`: same logical id as the chunk state, proving scope separation.
- `SimpleCropExample.ESSENCE_CARROT`: carrot-like crop that reads chunk essence and mutates world state on harvest.
- `SimpleWorldgenExample.ESSENCE_NODE_PATCH`: simple Overworld vegetation-step source-node feature.

## Current Boundaries

- State persistence is based on NeoForge attachments.
- `DataComponentType` remains for `ItemStack` data.
- `DataMapType` remains for static registry-object configuration.
- Capabilities should expose interaction APIs, not act as the persistent storage container for this state.
- `SavedData` is not part of the high-level state API yet; use it separately for large indexes or custom global files.
- The crop API currently targets carrot-like age-stage crops.
- The worldgen API currently focuses on configured/placed feature plus add-features biome modifier registration.

These boundaries keep the first version narrow while still covering the gameplay path needed for source-essence prototypes.
