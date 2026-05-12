# Source Essence Testing

This document describes the current test contract for the source-essence registration examples: state attachments, debug commands, the essence crop, and the example worldgen feature.

The intended split is simple:

- GameTest proves runtime correctness.
- Datagen and JUnit prove generated data shape and codec behavior.
- Manual testing is only for developer-facing feel, visual density, and command UI comfort.

## Automated Validation

Run the full acceptance chain before treating this feature area as ready:

```powershell
.\gradlew compileJava runData test runGameTestServer
```

The GameTest server should report all required tests passing. The current source-essence suite is registered directly through `RegisterGameTestsEvent`; it does not use `TestFunctionLoader`.

## GameTest Coverage

### `state_attachments`

This test covers the state attachment boundary:

- Chunk state default creation through `getOrCreate`.
- Chunk state `set` and mutation behavior.
- Chunk dirty marking after mutation.
- `getIfLoaded` returning empty for an unloaded far chunk without force-loading it.
- World state default creation, `set`, and mutation behavior.
- Same-name world and chunk states staying distinct by scope.
- Explicit chunk and world sync calls not crashing.

State values touched by this test are cleared at the beginning of the test so previous GameTests or previous runs do not leak into the default-value assertions.

### `state_debug_commands`

This test covers the administrator command contract:

- `/registrylib state list`.
- World and chunk `debug`.
- Writable world and chunk `set`.
- World and chunk `get`.
- Read permission failure.
- Read-only state write failure.
- Invalid JSON / codec type failure returning a Brigadier command error.
- Suggestions for readable and writable state ids.

The `set` command intentionally accepts both world and chunk shapes:

```text
registrylib state set <state_id> <json_value>
registrylib state set <state_id> <chunk_x> <chunk_z> <json_value>
```

When a world and chunk state share the same id, the command dispatches chunk writes only if the tail parses as `<chunk_x> <chunk_z> <json_value>`.

### `essence_crop`

This test covers the crop runtime behavior:

- The example crop survives on farmland.
- The example crop rejects the wrong soil.
- The crop max age remains carrot-like at `7`.
- Random ticks consult the chunk state by making high ambient essence grow in a bounded number of attempts.
- Bonemeal growth does not exceed max age.
- Mature loot contains the configured produce and seed.
- Right-click harvest consumes the action, replants age `0`, and fires the harvest callback exactly once.
- The harvest callback mutates the example world state.

The test validates loot through the block loot table instead of relying on mock-player pickup behavior, which keeps the assertion stable in GameTest.

### `essence_worldgen`

This test covers the runtime worldgen registration and placement boundary:

- Configured feature exists in `Registries.CONFIGURED_FEATURE`.
- Placed feature exists in `Registries.PLACED_FEATURE`.
- NeoForge biome modifier exists in `NeoForgeRegistries.Keys.BIOME_MODIFIERS`.
- The biome modifier uses `VEGETAL_DECORATION`.
- The placed feature contains the expected heightmap and biome filters.
- The placement predicate accepts air above grass and rejects air above stone.

The sampling assertion focuses on the configured feature plus the block predicate filter. Full `placeWithBiomeCheck` behavior depends on biome generation settings outside the local GameTest fixture, so biome integration is verified by checking the registered biome modifier and placed-feature filters instead.

## Datagen Checks

`runData` should generate the relevant source-essence data under `src/generated/resources/registrylibtest`, including:

- `data/registrylibtest/worldgen/configured_feature/essence_node_patch.json`
- `data/registrylibtest/worldgen/placed_feature/essence_node_patch.json`
- `data/registrylibtest/neoforge/biome_modifier/essence_node_patch_add_feature.json`
- `data/registrylibtest/loot_table/blocks/essence_carrot.json`
- `assets/registrylibtest/blockstates/essence_carrot.json`
- `assets/registrylibtest/models/block/essence_carrot_stage*.json`
- `assets/registrylibtest/items/essence_carrot_seeds.json`

The important semantic checks are:

- The configured feature places `registrylibtest:decorative_stone`.
- The placed feature uses count, square spread, heightmap, grass-base predicate, and biome filter.
- The biome modifier targets `#minecraft:is_overworld` at `vegetal_decoration`.
- The crop loot table drops carrot when mature and seed otherwise, with mature seed bonus behavior.

## Manual Testing

Manual testing should stay small. Use it only for command completion feel, chat readability, crop interaction feel, and natural-generation visual sanity.

See [source_essence_manual_checklist.md](source_essence_manual_checklist.md) for the exact checklist.
