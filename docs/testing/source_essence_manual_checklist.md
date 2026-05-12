# Source Essence Manual Checklist

These checks are for developer-facing feel and visual sanity only. Runtime correctness is covered by `runGameTestServer`; do not use this checklist as a substitute for automated validation.

Before starting, run:

```powershell
.\gradlew compileJava runData test runGameTestServer
```

Only continue with manual testing after the automated suite passes.

## Command Experience

- In a test world, type `/registrylib state ` and confirm `list`, `get`, `set`, and `debug` appear as completions.
- Type a state id prefix, such as `/registrylib state get registrylibtest:e`, and confirm `registrylibtest:essence_epoch` is suggested.
- Type a chunk-state id prefix, such as `/registrylib state get registrylibtest:a`, and confirm `registrylibtest:ambient_essence` is suggested.
- Run one read command and confirm the chat output is readable:

```text
/registrylib state get registrylibtest:essence_epoch
```

- Run one chunk write command and confirm it succeeds without noisy output:

```text
/registrylib state set registrylibtest:ambient_essence <chunkX> <chunkZ> 7
```

- Run one debug command and confirm it clearly shows the state id, scope, presence, writable flag, and codec:

```text
/registrylib state debug registrylibtest:ambient_essence <chunkX> <chunkZ>
```

## Crop Experience

- Place a small farmland patch and plant `essence_carrot_seeds`.
- Set the current chunk to a high value with `/registrylib state set registrylibtest:ambient_essence <chunkX> <chunkZ> 7`.
- Confirm random ticks or bonemeal advance the crop normally.
- Right-click a mature crop and confirm it drops produce and replants as a young crop.
- Run `/registrylib state get registrylibtest:essence_epoch` before and after harvest if you want a quick visual confirmation that the harvest callback is visible through the command system.

## Natural Generation Feel

- Create or load an Overworld area with grass surface.
- Confirm the example source-node worldgen appears on grass.
- Confirm the feature is not visually too dense.
- Confirm generated blocks are not floating or underwater in ordinary terrain.
- Do not manually count placements; the automated test covers the basic positive and negative placement boundary.

## Do Not Hand-Test

- Datagen JSON paths and contents.
- Loot table and model path correctness.
- Registry presence for configured/placed features and biome modifiers.
- Wrong-soil crop rejection.
- Command permissions, codec parse failures, and command suggestions.
- `PlacedFeature#placeWithBiomeCheck` generation boundaries.
