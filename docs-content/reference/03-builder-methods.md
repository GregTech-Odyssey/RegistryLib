---
sidebar_position: 3
title: Builder Methods
description: Complete method reference for all Builder types.
---

# Builder Methods

Complete method reference for all RegistryLib builder types. Each builder is created from an entry point on the `RegistryLib` instance and terminated with `.register()`.

:::note
All configuration methods return `this` for fluent chaining. Order generally does not matter, but `initialProperties` should come before `properties` when both are used.
:::

## ItemBuilder

Created via `item("id", factory)`, `item("id")`, or `componentItem("id")`.

| Method | Parameters | Description |
| --- | --- | --- |
| `initialProperties(supplier)` | `Supplier<Item.Properties>` | Set initial item properties from scratch |
| `properties(modifier)` | `UnaryOperator<Item.Properties>` | Modify the existing properties |
| `lang(text)` | `String` | Set English display name |
| `lang(providerType, text)` | `ProviderType, String` | Set locale-specific display name |
| `defaultLang()` | - | Infer display name from the registry path |
| `defaultModel()` | - | Generate default item model during datagen |
| `constantTint(color)` | `RgbColor` | Generate a flat tinted item model using the item's own texture path |
| `constantTint(texturePath, color)` | `String, RgbColor` | Generate a flat tinted item model referencing a shared texture |
| `constantTint(texture, color)` | `TextureRef, RgbColor` | Type-safe variant of the shared texture overload |
| `tintSource(texturePath, sources...)` | `String, ItemTintSource...` | Generate a flat item model with custom item tint sources |
| `tintSource(texture, sources...)` | `TextureRef, ItemTintSource...` | Type-safe variant of the shared texture overload |
| `flatTintedModel(texturePath, sources...)` | `String, ItemTintSource...` | Explicit alias for a flat tinted model |
| `flatTintedModel(texture, sources...)` | `TextureRef, ItemTintSource...` | Type-safe variant of the flat tinted model overload |
| `modelTexture(texturePath)` / `existingTexture(texturePath)` | `String` | Reference an existing texture without generating a PNG |
| `modelTexture(texture)` / `existingTexture(texture)` | `TextureRef` | Type-safe variant for existing textures |
| `visual(preset)` | `ItemVisualPreset` | Apply a reusable item visual strategy |
| `addTab(tab)` | `ResourceKey<CreativeModeTab>` | Add to a creative tab |
| `addDefaultTab()` | - | Add to the RegistryCore default tab |
| `removeTab(tab)` | `ResourceKey<CreativeModeTab>` | Remove from a creative tab |
| `addTag(tags...)` | `TagKey<Item>...` | Add item tags |
| `addRecipeData(callback)` | `Consumer<RegistryLibRecipeProvider>` | Add a typed recipe datagen callback |
| `addTooltip(component)` | `Component` | Add a simple static tooltip line |
| `addTooltip(collector)` | `BiConsumer<TooltipNodeCollector, ItemStack>` | Add a dynamic tooltip with context |
| `attach(attachment)` | `ItemAttachment` | Add an attachment (ComponentItem only) |
| `texture(supplier)` | `Supplier<BufferedImage>` | Generate a texture during datagen |
| `register()` | - | Submit and return `ItemEntry<T>` |

**Example:**

```java
REGISTRYLIB.item("copper_coin", Item::new)
        .initialProperties(() -> new Item.Properties().stacksTo(64))
        .lang("Copper Coin")
        .defaultModel()
        .addTab(CreativeModeTabs.MISC)
        .addTooltip(Component.literal("A shiny coin"))
        .register();
```

## BlockBuilder

Created via `block("id", factory)` or `block("id")`.

| Method | Parameters | Description |
| --- | --- | --- |
| `initialProperties(supplier)` | `Supplier<? extends Block>` | Copy properties from an existing block |
| `properties(modifier)` | `UnaryOperator<BlockBehaviour.Properties>` | Modify block properties |
| `lang(text)` | `String` | Set English display name |
| `lang(providerType, text)` | `ProviderType, String` | Set locale-specific display name |
| `defaultLang()` | - | Infer display name from registry path |
| `simpleItem()` | - | Create a default BlockItem with no customization |
| `item(configurator)` | `Consumer<ItemBuilder>` | Create and customize the associated BlockItem |
| `defaultLoot()` | - | Generate basic self-drop loot table |
| `loot(configurator)` | `BiConsumer<BlockLootSubProvider, Block>` | Define a custom loot table |
| `tintedCube(tintIndex)` | `int` | Generate a simple cube block model using the block's own texture path |
| `tintedCube(texturePath, tintIndex)` | `String, int` | Generate a tinted cube block model referencing a shared texture |
| `tintedCube(texture, tintIndex)` | `TextureRef/Identifier, int` | Type-safe shared texture variants |
| `constantTint(color)` | `RgbColor` | Register opaque block tint and generate a tinted BlockItem model using the block model |
| `constantTint(texturePath, color)` | `String, RgbColor` | Generate a shared-texture tinted cube, register block tint, and generate a tinted BlockItem model |
| `constantTint(texture, color)` | `TextureRef, RgbColor` | Type-safe variant of the shared texture overload |
| `tintSource(sources...)` | `ItemTintSource...` | Generate a BlockItem model with custom item tint sources |
| `blockTintSource(sources...)` | `BlockTintSource...` | Register runtime block/world tint sources |
| `blockConstantTint(color)` | `RgbColor` or `ArgbColor` | Register a constant block tint source; RGB is converted to opaque ARGB |
| `layeredCube(particle, layers...)` | `TextureRef, BlockModelLayer...` | Generate a layered full-cube model with per-layer `tintindex` and translucency settings |
| `debugTint()` | - | Log model tint indices, tint source counts, colors, and warnings |
| `modelTexture(texturePath)` / `existingTexture(texturePath)` | `String` | Reference an existing cube texture without generating a PNG |
| `modelTexture(texture)` / `existingTexture(texture)` | `TextureRef` | Type-safe variant for existing cube textures |
| `visual(preset)` | `BlockVisualPreset` | Apply a reusable block visual strategy |
| `addTag(tags...)` | `TagKey<Block>...` | Add block tags |
| `addItemTag(tags...)` | `TagKey<Item>...` | Add tags to the generated BlockItem |
| `addRecipeData(callback)` | `Consumer<RegistryLibRecipeProvider>` | Add a typed recipe datagen callback |
| `register()` | - | Submit and return `BlockEntry<T>` |

**Example:**

```java
REGISTRYLIB.block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .properties(p -> p.strength(1.5f, 6.0f))
        .lang("Decorative Stone")
        .defaultLoot()
        .simpleItem()
        .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
        .register();
```

:::tip
Use `simpleItem()` for blocks that just need a basic BlockItem. Use `item(b -> { ... })` when you need to customize the item, such as adding tooltips, changing the model, or setting properties.
:::

## Color Types

Tint APIs use typed colors instead of raw integers:

| Type | Accepted value | Use |
| --- | --- | --- |
| `RgbColor.of(0xRRGGBB)` | 24-bit RGB only | Item tint and the common block tint builder path |
| `ArgbColor.of(0xAARRGGBB)` | 32-bit ARGB | Explicit block tint sources when alpha is intentional |

`RgbColor` rejects alpha bits and RegistryLib converts it to opaque ARGB. Raw Minecraft `BlockTintSources.constant(int)` does not add alpha; passing `0xRRGGBB` directly produces alpha `0`. Use `RegistryLibTintSources.blockConstant(RgbColor)` or `BlockBuilder.blockConstantTint(RgbColor)` for the opaque default.

## CropBuilder

Created via `crop("id")`.

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

## AttachmentTypeBuilder

Created via `attachmentType("id", holder -> defaultValue)` or `attachmentType("id", () -> defaultValue)`.

| Method | Parameters | Description |
| --- | --- | --- |
| `configure(callback)` | `UnaryOperator<AttachmentType.Builder<T>>` | Apply low-level NeoForge attachment configuration |
| `serialize(codec)` | `MapCodec<T>` | Configure persistent attachment serialization |
| `sync(streamCodec)` | `StreamCodec<? super RegistryFriendlyByteBuf, T>` | Configure attachment sync |
| `register()` | - | Register and return `AttachmentTypeEntry<T>` |

## ChunkStateBuilder

Created via `chunkState("id", codec, defaultValue)`.

| Method | Parameters | Description |
| --- | --- | --- |
| `syncOnModify(enabled)` | `boolean` | Request sync after `set` or `modify` when a sync codec exists |
| `sync(streamCodec)` | `StreamCodec<? super RegistryFriendlyByteBuf, T>` | Configure sync and enable sync-on-modify |
| `debug()` | - | Enable read-only debug commands with default permissions |
| `debug(configurator)` | `UnaryOperator<StateDebugConfig>` | Enable and configure debug command permissions and writability |
| `register()` | - | Register and return `ChunkStateEntry<T>` |
| `build()` | - | Register and return the parent chain object |

## WorldStateBuilder

Created via `worldState("id", codec, defaultValue)`.

| Method | Parameters | Description |
| --- | --- | --- |
| `syncOnModify(enabled)` | `boolean` | Request sync after `set` or `modify` when a sync codec exists |
| `sync(streamCodec)` | `StreamCodec<? super RegistryFriendlyByteBuf, T>` | Configure sync and enable sync-on-modify |
| `debug()` | - | Enable read-only debug commands with default permissions |
| `debug(configurator)` | `UnaryOperator<StateDebugConfig>` | Enable and configure debug command permissions and writability |
| `register()` | - | Register and return `WorldStateEntry<T>` |
| `build()` | - | Register and return the parent chain object |

## WorldgenFeatureBuilder

Created via `worldgenFeature("id", configuredFeature)` or `worldgenFeature("id", configuredFeatureSupplier)`.

| Method | Parameters | Description |
| --- | --- | --- |
| `placement(modifier)` | `PlacementModifier` | Add one placed feature modifier |
| `placements(modifiers)` | `List<PlacementModifier>` | Add multiple placed feature modifiers |
| `addToBiomes(tag, step)` | `TagKey<Biome>, GenerationStep.Decoration` | Generate an `AddFeaturesBiomeModifier` for matching biomes |
| `register()` | - | Generate configured/placed feature data and return `WorldgenFeatureEntry` |
| `build()` | - | Register and return the parent chain object |

## FluidBuilder

Created via `fluid("id", still, flow)`.

| Method | Parameters | Description |
| --- | --- | --- |
| `properties(modifier)` | `Consumer<FluidType.Properties>` | Modify fluid type properties |
| `lang(text)` | `String` | Set English display name |
| `lang(providerType, text)` | `ProviderType, String` | Set locale-specific display name |
| `clientExtension(still, flow)` | `ResourceLocation, ResourceLocation` | Client rendering with colored textures |
| `clientExtension(still, flow, tint)` | `ResourceLocation, ResourceLocation, int` | Client rendering with grayscale textures and tint color |
| `tag(tags...)` | `TagKey<Fluid>...` | Add fluid tags |
| `block(configurator)` | `Consumer<BlockBuilder>` | Generate a fluid block |
| `bucket(configurator)` | `Consumer<ItemBuilder>` | Generate a bucket item |
| `addRecipeData(callback)` | `Consumer<RegistryLibRecipeProvider>` | Add a typed recipe datagen callback |
| `register()` | - | Submit and return `FluidEntry<T>` |

**Example:**

```java
REGISTRYLIB.fluid("molten_gold", STILL_TEXTURE, FLOW_TEXTURE)
        .lang("Molten Gold")
        .clientExtension(STILL_TEXTURE, FLOW_TEXTURE, 0xFFD4AF37)
        .properties(p -> p.density(3000).viscosity(6000).temperature(1300))
        .tag(FluidTags.LAVA)
        .block(b -> {})
        .bucket(b -> b.lang("Molten Gold Bucket"))
        .register();
```

:::warning
Always call `block(...)` and `bucket(...)` if you want your fluid to be placeable in the world and pickable with a bucket. Without them, only the flowing/source fluids are registered.
:::

## BlockEntityBuilder

Created via `blockEntity("id", factory)`.

| Method | Parameters | Description |
| --- | --- | --- |
| `validBlock(entry)` | `BlockEntry` | Bind a single host block |
| `validBlocks(entries...)` | `BlockEntry...` | Bind multiple host blocks |
| `renderer(supplier)` | `Supplier<BlockEntityRendererFactory>` | Register a renderer (lazy, client-side only) |
| `register()` | - | Submit and return `BlockEntityTypeEntry<T>` |

**Example:**

```java
REGISTRYLIB.blockEntity("crusher_be", CrusherBlockEntity::new)
        .validBlocks(CRUSHER_BLOCK, ADVANCED_CRUSHER_BLOCK)
        .renderer(() -> CrusherRenderer::new)
        .register();
```

:::note
The renderer supplier is lazy: the factory is only invoked on the client side. This prevents server crashes from referencing client-only classes.
:::

## EntityBuilder

Created via `entity("id", factory, category)`.

| Method | Parameters | Description |
| --- | --- | --- |
| `properties(modifier)` | `UnaryOperator<EntityType.Builder<T>>` | Modify the EntityType.Builder directly (escape hatch) |
| `sized(width, height)` | `float, float` | Set collision box dimensions |
| `clientTrackingRange(range)` | `int` | Client rendering distance in chunks |
| `updateInterval(interval)` | `int` | Server-to-client position sync interval in ticks |
| `fireImmune()` | - | Make entity immune to fire/lava damage |
| `noSummon()` | - | Prevent `/summon` command from spawning this entity |
| `noSave()` | - | Exclude entity from world save data |
| `attributes(supplier)` | `Supplier<AttributeSupplier.Builder>` | Register entity attributes (required for LivingEntity) |
| `renderer(supplier)` | `Supplier<EntityRendererProvider>` | Register client-side renderer (lazy, client-only) |
| `spawnEgg()` | - | Create a default spawn egg item |
| `spawnEgg(consumer)` | `Consumer<ItemBuilder>` | Create and customize the spawn egg item |
| `lang(text)` | `String` | Set English display name |
| `lang(providerType, text)` | `ProviderType, String` | Set locale-specific display name |
| `defaultLang()` | - | Infer display name from registry path |
| `addTag(tags...)` | `TagKey<EntityType<?>>...` | Add entity type tags |
| `loot(configurator)` | `BiConsumer<RegistryLibEntityLootTables, EntityType<T>>` | Define entity loot table (drops on death) |
| `spawnPlacement(type, heightmap, predicate)` | `SpawnPlacementType, Heightmap.Types, SpawnPredicate<T>` | Set natural spawn placement rules |
| `spawnBiomes(biomeTag, weight, min, max)` | `TagKey<Biome>, int, int, int` | Add entity to biome natural spawn list |
| `register()` | - | Submit and return `EntityEntry<T>` |

:::warning
`attributes()` is **mandatory** for any entity extending `LivingEntity`. The game crashes at spawn time if attributes are not registered. EntityBuilder handles the `EntityAttributeCreationEvent` subscription automatically.
:::

## RegistryCore addRecipe Entry Points

These are convenience methods on `RegistryCore` (your `RegistryLib` instance). They add recipes to any recipe type (vanilla, NeoForge, or third-party) without registering a new `RecipeType`.

| Method | Parameters | Description |
| --- | --- | --- |
| `addRecipe(id, recipe)` | `String, Recipe<?>` | Add a direct recipe instance |
| `addRecipe(id, supplier)` | `String, Supplier<? extends Recipe<?>>` | Add a lazily-created recipe |
| `addRecipe(id, function)` | `String, Function<HolderLookup.Provider, ? extends Recipe<?>>` | Add a registry-aware recipe factory |

**Example:**

```java
REGISTRYLIB.addRecipe("smelting/amethyst_shard",
        new SmeltingRecipe(...));
```

## RegistryCore Existing-Object and Tag Helpers

These helpers add datagen entries for objects that were registered outside the current RegistryLib builder chain.

| Method | Parameters | Description |
| --- | --- | --- |
| `existingItem(id)` | `String`, `Identifier`, or `ResourceKey<Item>` | Return an `ItemEntry` bound to an already-registered item |
| `existingBlock(id)` | `String`, `Identifier`, or `ResourceKey<Block>` | Return a `BlockEntry` bound to an already-registered block |
| `tagExisting(tag, items...)` | `TagKey<Item>, ItemLike...` | Add one item tag to existing items |
| `tagExistingSuppliers(tag, items...)` | `TagKey<Item>, Supplier<? extends ItemLike>...` | Add one item tag to lazy item suppliers |
| `tagExisting(tag, blocks...)` | `TagKey<Block>, Block...` | Add one block tag to existing blocks |
| `tagExistingBlockSuppliers(tag, blocks...)` | `TagKey<Block>, Supplier<? extends Block>...` | Add one block tag to lazy block suppliers |
| `itemTags().add(tag, items...)` | `TagKey<Item>, ItemLike...` | Batch-add existing items to an item tag |
| `itemTags().addSuppliers(tag, items...)` | `TagKey<Item>, Supplier<? extends ItemLike>...` | Batch-add lazy item suppliers to an item tag |
| `blockTags().add(tag, blocks...)` | `TagKey<Block>, Block...` | Batch-add existing blocks to a block tag |
| `blockTags().addSuppliers(tag, blocks...)` | `TagKey<Block>, Supplier<? extends Block>...` | Batch-add lazy block suppliers to a block tag |
| `tooltipExisting(item, tooltip)` | `ItemLike` or `ItemEntry`, `Component`/tooltip config | Attach RegistryLib tooltip nodes to existing items |
| `tooltipExistingSupplier(item, tooltip)` | `Supplier<? extends ItemLike>`, `Component`/tooltip config | Attach tooltip nodes to a lazy item supplier |
| `addExistingToTab(tab, item)` | `ResourceKey<CreativeModeTab>`, `ItemLike` or `ItemEntry` | Add an existing item to a creative tab |
| `addExistingSupplierToTab(tab, item)` | `ResourceKey<CreativeModeTab>`, `Supplier<? extends ItemLike>` | Add a lazy item supplier to a creative tab |
| `addExistingToDefaultTab(item)` | `ItemLike` or `ItemEntry` | Add an existing item to the default creative tab |

## Data Component Type Entries

Use `dataComponentTypeEntry(...)` when you need a lazy wrapper around a registered `DataComponentType`. The entry can be passed through supplier-based APIs without resolving the component at static initialization time.

```java
public static final DataComponentTypeEntry<MyEffect> MY_EFFECT =
        REGISTRYLIB.dataComponentTypeEntry("my_effect",
                builder -> builder.persistent(MyEffect.CODEC));
```

The entry can also read and mutate stack data directly:

```java
MyEffect effect = MY_EFFECT.get(stack);
MyEffect fallback = MY_EFFECT.getOrDefault(stack, MyEffect.EMPTY);
MY_EFFECT.set(stack, newEffect);
```

Use the direct `dataComponentType(...)` form when you need the concrete value immediately, and the entry form when another builder accepts a supplier or lazy registry entry.

## See Also

- [API Overview](/reference/api-overview) - entry point selection and common chains
- [Entry Types](/reference/entry-types) - reference for all Entry wrapper types
