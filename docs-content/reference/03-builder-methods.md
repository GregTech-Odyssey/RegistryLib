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
| `tagExisting(tag, blocks...)` | `TagKey<Block>, Block...` | Add one block tag to existing blocks |
| `itemTags().add(tag, items...)` | `TagKey<Item>, ItemLike...` | Batch-add existing items to an item tag |
| `blockTags().add(tag, blocks...)` | `TagKey<Block>, Block...` | Batch-add existing blocks to a block tag |

## Data Component Type Entries

Use `dataComponentTypeEntry(...)` when you need a lazy wrapper around a registered `DataComponentType`. The entry can be passed through supplier-based APIs without resolving the component at static initialization time.

```java
public static final DataComponentTypeEntry<MyEffect> MY_EFFECT =
        REGISTRYLIB.dataComponentTypeEntry("my_effect",
                builder -> builder.persistent(MyEffect.CODEC));
```

Use the direct `dataComponentType(...)` form when you need the concrete value immediately, and the entry form when another builder accepts a supplier or lazy registry entry.

## See Also

- [API Overview](/reference/api-overview) - entry point selection and common chains
- [Entry Types](/reference/entry-types) - reference for all Entry wrapper types
