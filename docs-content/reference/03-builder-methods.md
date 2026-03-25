---
sidebar_position: 3
title: Builder Methods
description: Complete method reference for all Builder types.
---

# Builder Methods

Complete method reference for all RegistryLib builder types. Each builder is created from an entry point on the `RegistryLib` instance and terminated with `.register()`.

:::note
All configuration methods return `this` for fluent chaining. Order generally doesn't matter, but `initialProperties` should come before `properties` when both are used.
:::

## ItemBuilder

Created via `item("id", factory)`, `item("id")`, or `componentItem("id")`.

| Method | Parameters | Description |
| --- | --- | --- |
| `initialProperties(supplier)` | `Supplier<Item.Properties>` | Set initial item properties from scratch |
| `properties(modifier)` | `UnaryOperator<Item.Properties>` | Modify the existing properties |
| `lang(text)` | `String` | Set English display name |
| `lang(providerType, text)` | `ProviderType, String` | Set locale-specific display name |
| `defaultLang()` | —| Infer display name from the registry path (e.g. `copper_coin` —"Copper Coin") |
| `defaultModel()` | —| Generate default item model during datagen |
| `addTab(tab)` | `ResourceKey<CreativeModeTab>` | Add to a creative tab |
| `addDefaultTab()` | —| Add to the RegistryCore default tab |
| `removeTab(tab)` | `ResourceKey<CreativeModeTab>` | Remove from a creative tab |
| `addTag(tags...)` | `TagKey<Item>...` | Add item tags |
| `addTooltip(component)` | `Component` | Add a simple static tooltip line |
| `addTooltip(collector)` | `BiConsumer<TooltipNodeCollector, ItemStack>` | Add a dynamic tooltip with context |
| `attach(attachment)` | `ItemAttachment` | Add an attachment (ComponentItem only) |
| `texture(supplier)` | `Supplier<BufferedImage>` | Generate a texture during datagen |
| `register()` | —| Submit and return `ItemEntry<T>` |

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
| `defaultLang()` | —| Infer display name from registry path |
| `simpleItem()` | —| Create a default BlockItem with no customization |
| `item(configurator)` | `Consumer<ItemBuilder>` | Create and customize the associated BlockItem |
| `defaultLoot()` | —| Generate basic self-drop loot table |
| `loot(configurator)` | `BiConsumer<BlockLootSubProvider, Block>` | Define a custom loot table |
| `addTag(tags...)` | `TagKey<Block>...` | Add block tags |
| `register()` | —| Submit and return `BlockEntry<T>` |

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
Use `simpleItem()` for blocks that just need a basic BlockItem. Use `item(b -> { ... })` when you need to customize the item (e.g., add tooltips, change model, set properties).
:::

## FluidBuilder

Created via `fluid("id", still, flow)`.

| Method | Parameters | Description |
| --- | --- | --- |
| `properties(modifier)` | `Consumer<FluidType.Properties>` | Modify fluid type properties |
| `lang(text)` | `String` | Set English display name |
| `lang(providerType, text)` | `ProviderType, String` | Set locale-specific display name |
| `clientExtension(still, flow)` | `ResourceLocation, ResourceLocation` | Client rendering with colored textures |
| `clientExtension(still, flow, tint)` | `ResourceLocation, ResourceLocation, int` | Client rendering with grayscale textures + tint color |
| `tag(tags...)` | `TagKey<Fluid>...` | Add fluid tags |
| `block(configurator)` | `Consumer<BlockBuilder>` | Generate a fluid block |
| `bucket(configurator)` | `Consumer<ItemBuilder>` | Generate a bucket item |
| `register()` | —| Submit and return `FluidEntry<T>` |

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
| `register()` | —| Submit and return `BlockEntityEntry<T>` |

**Example:**

```java
REGISTRYLIB.blockEntity("crusher_be", CrusherBlockEntity::new)
        .validBlocks(CRUSHER_BLOCK, ADVANCED_CRUSHER_BLOCK)
        .renderer(() -> CrusherRenderer::new)
        .register();
```

:::note
The renderer supplier is lazy —the factory is only invoked on the client side. This prevents server crashes from referencing client-only classes.
:::

## EntityBuilder

Created via `entity("id", factory, category)`.

| Method | Parameters | Description |
| --- | --- | --- |
| `properties(modifier)` | `UnaryOperator<EntityType.Builder<T>>` | Modify the EntityType.Builder directly (escape hatch) |
| `sized(width, height)` | `float, float` | Set collision box dimensions |
| `clientTrackingRange(range)` | `int` | Client rendering distance in chunks |
| `updateInterval(interval)` | `int` | Server→client position sync interval in ticks |
| `fireImmune()` | — | Make entity immune to fire/lava damage |
| `noSummon()` | — | Prevent `/summon` command from spawning this entity |
| `noSave()` | — | Exclude entity from world save data |
| `attributes(supplier)` | `Supplier<AttributeSupplier.Builder>` | Register entity attributes (required for LivingEntity) |
| `renderer(supplier)` | `Supplier<EntityRendererProvider>` | Register client-side renderer (lazy, client-only) |
| `spawnEgg()` | — | Create a default spawn egg item |
| `spawnEgg(consumer)` | `Consumer<ItemBuilder>` | Create and customise the spawn egg item |
| `lang(text)` | `String` | Set English display name |
| `lang(providerType, text)` | `ProviderType, String` | Set locale-specific display name |
| `defaultLang()` | — | Infer display name from registry path |
| `addTag(tags...)` | `TagKey<EntityType<?>>...` | Add entity type tags |
| `loot(configurator)` | `BiConsumer<RegistryLibEntityLootTables, EntityType<T>>` | Define entity loot table (drops on death) |
| `spawnPlacement(type, heightmap, predicate)` | `SpawnPlacementType, Heightmap.Types, SpawnPredicate<T>` | Set natural spawn placement rules |
| `spawnBiomes(biomeTag, weight, min, max)` | `TagKey<Biome>, int, int, int` | Add entity to biome natural spawn list |
| `register()` | — | Submit and return `EntityEntry<T>` |

**Example:**

```java
REGISTRYLIB.<MyMob>entity("my_mob", MyMob::new, MobCategory.MONSTER)
        .sized(0.6F, 1.95F)
        .clientTrackingRange(8)
        .updateInterval(3)
        .fireImmune()
        .attributes(MyMob::createAttributes)
        .renderer(() -> MyMobRenderer::new)
        .spawnEgg(egg -> egg.lang("My Mob Spawn Egg"))
        .addTag(EntityTypeTags.FALL_DAMAGE_IMMUNE)
        .loot((tables, entityType) -> tables.add(entityType, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.DIAMOND))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer()))))
        .spawnPlacement(SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules)
        .spawnBiomes(BiomeTags.IS_OVERWORLD, 80, 1, 3)
        .register();
```

:::warning
`attributes()` is **mandatory** for any entity extending `LivingEntity`. The game crashes at spawn time if attributes are not registered. EntityBuilder handles the `EntityAttributeCreationEvent` subscription automatically.
:::

:::tip
Use `spawnEgg()` for a plain spawn egg. Use `spawnEgg(egg -> { ... })` to customise the egg's name, tabs, or tooltips via the inner `ItemBuilder`.
:::

## RegistryCore Extend / Copy Entry Points

These are **static factory methods** on `RegistryCore` (your `RegistryLib` instance), not builders. They return entry objects directly.

| Method | Parameters | Returns | Description |
| --- | --- | --- | --- |
| `extendRecipe(ref)` | `RecipeRef<T>` | `ExtendRecipeEntry<T>` | Inject recipes into an existing type (no new registration) |
| `copyRecipe(name, ref)` | `String, RecipeRef<T>` | `RecipeEntry<T>` | Create a new RecipeType + RecipeSerializer reusing an existing codec |

**Example:**

```java
// Extend — no new type registered
ExtendRecipeEntry<SmeltingRecipe> EXTRA = REGISTRYLIB
        .<SmeltingRecipe>extendRecipe(SMELTING_REF)
        .addRecipe("name", recipe);

// Copy — registers "yourmod:name" RecipeType + RecipeSerializer
RecipeEntry<SmeltingRecipe> COPY = REGISTRYLIB
        .<SmeltingRecipe>copyRecipe("electric_smelting", SMELTING_REF);
```

:::warning
For copy types, use `addRecipe()` instead of `customRecipeData()` to ensure the correct `"type"` field in generated JSON.
:::

## See Also

- [API Overview](/reference/api-overview) —entry point selection and common chains
- [Entry Types](/reference/entry-types) —reference for all Entry wrapper types
