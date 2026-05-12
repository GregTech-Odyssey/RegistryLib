---
sidebar_position: 1
title: API Overview
description: Entry points, builder families, and common chain patterns.
---

# API Overview

This page is for quick lookup only, not full teaching. It answers "which entry point should I start from?" and "what do common chains usually look like?"

:::note
The code snippets on this page are short excerpts from the runnable `RegistryLibTest` examples, not invented pseudo-code.
:::

## Which Entry Point Should I Start From?

| What you want to do | Entry point | What usually comes next |
| --- | --- | --- |
| Register a regular or composite Item | `item("id", factory)` or `item("id")` | `lang`, `defaultModel`, `addTab`, `addTooltip`, `attach` |
| Register a component-driven Item with attachments | `componentItem("id")` or `componentItem("id", factory)` | `lang`, `defaultModel`, `addTooltip`, `attach` |
| Register a Block | `block("id", factory)` or `block("id")` | `initialProperties`, `simpleItem` or `item`, `loot`, `addTag` |
| Register a Fluid | `fluid("id", still, flow)` | `lang`, `clientExtension`, `properties`, `block`, `bucket` |
| Register a BlockEntity | `blockEntity("id", factory)` | `validBlock` or `validBlocks`, `renderer` |
| Register a generic object in another registry | `generic("id", registryKey, factory)` or `simple(...)` | `register` or immediate completion |
| Register a data component type lazily | `dataComponentTypeEntry("id", builder)` | pass the entry into supplier-friendly APIs |
| Register a creative tab | `creativeTab("id")` or `creativeTab("id", enUs, locales)` | title, icon, content population |
| Register a NeoForge attachment type | `attachmentType("id", holder -> defaultValue)` | `serialize`, `sync`, `register` |
| Register chunk or world state | `chunkState("id", codec, default)` or `worldState("id", codec, default)` | `debug`, `sync`, `register` |
| Register a crop-like plant | `crop("id")` | `properties`, `produce`, `growthRoll`, `onHarvest`, `stageTextures` |
| Register configured and placed worldgen | `worldgenFeature("id", configuredFeature)` | `placement`, `addToBiomes`, `register` |
| Share defaults across many entries | `group("name")` | `langPrefix`, `tab`, `initialBlockProperties`, `blockProperties`, `itemProperties`, `addBlockTag`, `addItemTag`, `addFluidTag` |
| Reference existing vanilla or third-party objects | `existingItem("namespace:id")`, `existingBlock("namespace:id")` | pass the entry to recipes, tags, tabs, tooltips, or holder-style APIs |
| Add tags to existing objects | `tagExisting(...)`, `itemTags().add(...)`, `itemTags().addSuppliers(...)`, `blockTags().addSuppliers(...)` | datagen-only tag entries |
| Share grayscale template textures | `constantTint(texturePath, RgbColor.of(...))`, `visual(ItemVisualPreset.tintedTemplate(...))`, `visual(BlockVisualPreset.constantTintedCube(...))` | model tint without per-material PNGs |

:::tip
If you're unsure, start with `item(...)` or `block(...)`; they cover the vast majority of registrations. See the [How-to guides](/how-to/register-items) for step-by-step walkthroughs.
:::

## Builder Family Quick Lookup

| Builder | Responsibility | Endpoint |
| --- | --- | --- |
| `ItemBuilder` | Item properties, model, tooltip, tab, recipe, tag | `ItemEntry` |
| `BlockBuilder` | Block properties, drops, block item, recipe, tag | `BlockEntry` |
| `FluidBuilder` | Fluid type, rendering, block, bucket, tag | `FluidEntry` |
| `BlockEntityBuilder` | Host block binding and renderer | `BlockEntityTypeEntry` |
| `CropBuilder` | Crop block, seed item, growth, harvest, stage models | `BlockEntry<RegistryLibCropBlock>` |
| `AttachmentTypeBuilder` | NeoForge attachment registration | `AttachmentTypeEntry` |
| `ChunkStateBuilder` / `WorldStateBuilder` | Attachment-backed scoped state | `ChunkStateEntry` / `WorldStateEntry` |
| `WorldgenFeatureBuilder` | Configured feature, placed feature, biome modifier data | `WorldgenFeatureEntry` |

## Entry Type Quick Lookup

| Type | Typical use |
| --- | --- |
| `ItemEntry<T>` | Reference an Item; create `ItemStack` and `ItemResource` values directly |
| `BlockEntry<T>` | Reference a Block, its default state, and holder-style APIs expecting `Holder<Block>` |
| `FluidEntry<T>` | Access source, type, block, bucket, `FluidStack`, and `FluidResource` together |
| `BlockEntityTypeEntry<T>` | Reference a `BlockEntityType` with host binding |
| `DataComponentTypeEntry<T>` | Lazy wrapper for a `DataComponentType<T>` |
| `AttachmentTypeEntry<T>` | Lazy wrapper for a NeoForge `AttachmentType<T>` |
| `ChunkStateEntry<T>` | Scoped handle for chunk attachment state |
| `WorldStateEntry<T>` | Scoped handle for world attachment state |
| `WorldgenFeatureEntry` | Datapack keys for configured and placed features |

## Entry Helper Quick Lookup

| Entry helper | What it gives you |
| --- | --- |
| `ItemEntry.asStack()` | A default `ItemStack` without reconstructing the item manually |
| `ItemEntry.readOnlyStack()` | Defensive copy of a cached `ItemStack` (count 1); safe against external mutation |
| `ItemEntry.asResource()` | An `ItemResource` wrapper for transfer-related APIs |
| `BlockEntry.getDefaultState()` | The block's default state for world placement or configuration |
| `DataComponentTypeEntry.get(stack)` | Read a component value without scattering `.get()` at call sites |
| `DataComponentTypeEntry.set(stack, value)` | Set a component value on a stack through the lazy entry wrapper |
| `AttachmentTypeEntry.getOrCreate(holder)` | Read or create raw attachment data on an `IAttachmentHolder` |
| `ChunkStateEntry.modify(level, pos, action)` | Mutate chunk state while marking the chunk unsaved |
| `ChunkStateEntry.getIfLoaded(level, pos)` | Read chunk state without force-loading the chunk |
| `WorldStateEntry.set(level, value)` | Replace level-scoped state attached to a `ServerLevel` |
| `WorldgenFeatureEntry.placedKey()` | Reference the generated placed feature key |
| `FluidEntry.getSource()` | The matching source fluid instance |
| `FluidEntry.getType()` | The `FluidType` associated with the family |
| `FluidEntry.getBlock()` / `getBucket()` | The related fluid block or bucket when they exist |
| `FluidEntry.asStack()` / `asResource()` | Transfer-friendly fluid values without rebuilding them by hand |
| `FluidEntry.readOnlyStack()` | A cached read-only `FluidStack` (1000 mB); avoids repeated allocations |
| Lazy entry wrappers | Use `get()` only after registration, or pass the entry/supplier to APIs that resolve lazily |

:::note
Several Entry wrappers also satisfy holder-style usage directly. When another API expects a `Holder<Item>`, `Holder<Block>`, or `Holder<Fluid>`, the RegistryLib entry wrapper is often already usable as that value.
:::

## Core Helper Quick Lookup

| Helper | Purpose |
| --- | --- |
| `locale("zh_cn")` | Get or create a lang provider for a locale |
| `lang(key, enUs)` | Add an English lang entry and return a translatable component |
| `lang(locale, key, value)` | Add a lang entry for a locale string |
| Duplicate lang entry with same value | Ignored as idempotent; conflicting values still fail fast |
| `addRecipeData(provider -> { ... })` | Add a typed recipe datagen callback |
| `tagExisting(tag, items...)` | Add an item tag to existing items |
| `tagExisting(tag, blocks...)` | Add a block tag to existing blocks |
| `itemTags().add(...)` / `blockTags().add(...)` | Batch tag helpers for existing objects or ids |
| `tooltipExisting(item, tooltip)` | Attach RegistryLib tooltips to vanilla or third-party items |
| `addExistingToTab(tab, item)` | Add existing items to creative tabs through RegistryLib |
| `builder.texture("foo", imageSupplier)` | Generate a PNG resource during datagen |
| `builder.modelTexture("item/template")` / `builder.existingTexture("item/template")` | Reference an existing texture in the generated model |
| `REGISTRYLIB.textureRef("item/template")` | Create a `TextureRef` for type-safe texture APIs |
| `REGISTRYLIB.cleanGeneratedNamespace("textures/item/generated")` | Explicitly remove stale generated files under a specific `textures/...` subdirectory before writing resources |
| `RgbColor.of(0xRRGGBB)` | Typed opaque RGB input for item and common block tint APIs |
| `ArgbColor.of(0xAARRGGBB)` | Typed ARGB input for explicit block tint sources |
| `RegistryLibTintSources.blockConstant(RgbColor)` | Create an opaque block tint source without raw ARGB mistakes |

## Texture Path Decision Matrix

| Scenario | API | Writes a PNG? |
| --- | --- | --- |
| Generate a new placeholder or procedural texture | `builder.texture("path", imageSupplier)` | Yes |
| Reuse a shared grayscale item template | `constantTint("item/templates/dust", RgbColor.of(0x6E7380))` | No |
| Reuse a shared grayscale block template | `constantTint("block/templates/storage_cube", RgbColor.of(0x6E7380))` | No |
| Reuse an untinted existing texture | `modelTexture("item/template")` or `existingTexture("block/template")` | No |
| Pass a typed texture path through helper code | `TextureRef.mod(MOD_ID, "item/template")`, `TextureRef.mc("item/iron_ingot")`, or `REGISTRYLIB.textureRef("item/template")` | No |
| Remove stale generated textures after changing strategy | `cleanGeneratedNamespace("textures/item/generated")` | Deletes old files first |

Use `builder.texture(...)` only when RegistryLib should create the image file. Use `TextureRef`, `modelTexture`, `existingTexture`, and the shared-texture tint overloads when the file already exists or is generated once by another provider.

## Common Chain Lookup

### Minimal Item

```java
REGISTRYLIB.item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

### Component Item with Attachments

```java
REGISTRYLIB.componentItem("magic_wand")
        .initialProperties(() -> new Item.Properties().stacksTo(1))
        .properties(Item.Properties::fireResistant)
        .lang("Magic Wand")
        .defaultModel()
        .attach(new InspectAttachment())
        .register();
```

### Minimal Block

```java
REGISTRYLIB.block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

### Existing Object in a Recipe

```java
ItemEntry<Item> VANILLA_IRON_INGOT = REGISTRYLIB.existingItem("minecraft:iron_ingot");

REGISTRYLIB.tooltipExisting(VANILLA_IRON_INGOT,
        Component.translatable("tooltip.example.iron_ingot"));
REGISTRYLIB.addExistingToDefaultTab(VANILLA_IRON_INGOT);

REGISTRYLIB.addRecipeData(prov -> prov.shapeless(RecipeCategory.MISC, Items.IRON_NUGGET, 9)
        .requires(VANILLA_IRON_INGOT)
        .unlockedBy("has_iron_ingot", prov.has(VANILLA_IRON_INGOT))
        .save(prov, MOD_ID + ":iron_nuggets_from_existing_iron"));
```

### Shared Tinted Template

```java
ItemVisualPreset dustVisual(RgbColor color) {
    return ItemVisualPreset.tintedTemplate("item/templates/dust", color);
}

REGISTRYLIB.item("lead_dust").visual(dustVisual(RgbColor.of(0x6E7380))).register();
REGISTRYLIB.item("nickel_dust").visual(dustVisual(RgbColor.of(0xD5C36A))).register();

TextureRef storageTemplate = REGISTRYLIB.textureRef("block/templates/storage_cube");
REGISTRYLIB.block("lead_storage_block")
        .visual(BlockVisualPreset.constantTintedCube(storageTemplate, RgbColor.of(0x6E7380)))
        .simpleItem()
        .register();
```

For blocks, `constantTint(RgbColor)` also registers the runtime block tint source. Use `debugTint()` while developing layered models to log tint source counts and catch missing `tintindex` coverage.

### Cleaning Stale Generated Textures

If an older datagen strategy produced one PNG per material and the new strategy uses shared templates, explicitly clean the old generated directory:

```java
REGISTRYLIB.cleanGeneratedNamespace("textures/item/generated_materials");
```

The path is relative to `src/generated/resources/assets/<modid>/`. Keep it as narrow as possible; RegistryLib rejects paths that escape the namespace or target the whole namespace root.
Use a textures-only path such as `textures/item/generated_materials`, not `models/...` or `lang`.

### Block Inside a Group

```java
MACHINES.block("crusher", Block::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .defaultLoot()
        .simpleItem()
        .register();
```

## See Also

- [Entry Types](/reference/entry-types) - detailed reference for each Entry wrapper
- [Builder Methods](/reference/builder-methods) - complete method reference for all Builder types
- [Register Items](/how-to/register-items) - step-by-step item registration
- [Register Blocks](/how-to/register-blocks) - step-by-step block registration
- [Register Environment Features](/how-to/register-environment-features) - chunk/world state, crops, and worldgen
- [Builder Pattern & Fluent API](/concepts/builder-pattern) - how the chain architecture works
