---
sidebar_position: 2
title: Entry Types
description: Reference for all RegistryLib entry wrapper types.
---

# Entry Types

RegistryLib provides specialized Entry types that wrap `DeferredHolder` with convenience helpers. All entry types extend `RegistryEntry<R, T>` and are lazily resolved —the underlying object is created only when NeoForge's registration event fires.

:::note
All entry types also implement `Holder`-style interfaces. When an API expects a `Holder<Item>`, `Holder<Block>`, or `Holder<Fluid>`, you can pass the entry directly.
:::

## ItemEntry\<T\>

**Extends:** `RegistryEntry<Item, T>`

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `T` | Get the registered Item instance |
| `asStack()` | `ItemStack` | Create a default ItemStack (count 1) |
| `asStack(int count)` | `ItemStack` | Create an ItemStack with the specified count |
| `readOnlyStack()` | `ItemStack` | Cached read-only ItemStack (count 1); avoids repeated allocations in hot paths |
| `asResource()` | `ItemResource` | ItemResource wrapper for transfer APIs |

**Usage example:**

```java
ItemEntry<Item> COPPER_COIN = REGISTRYLIB.item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();

// Later in code
ItemStack stack = COPPER_COIN.asStack(16);
Item item = COPPER_COIN.get();
```

## BlockEntry\<T\>

**Extends:** `RegistryEntry<Block, T>`

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `T` | Get the registered Block instance |
| `getDefaultState()` | `BlockState` | Block's default BlockState for placement or configuration |

**Usage example:**

```java
BlockEntry<Block> DECORATIVE_STONE = REGISTRYLIB.block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .simpleItem()
        .register();

// Later in code
BlockState state = DECORATIVE_STONE.getDefaultState();
```

## FluidEntry\<T\>

**Extends:** `RegistryEntry<Fluid, T>`

The most feature-rich entry type. Provides access to the entire fluid family (source, flowing, block, bucket) from a single reference.

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `T` | Get the flowing fluid |
| `getSource()` | `Fluid` | Get the source fluid |
| `getType()` | `FluidType` | Get the FluidType |
| `getBlock()` | `LiquidBlock` | Get the fluid block (if registered) |
| `getBucket()` | `Item` | Get the bucket item (if registered) |
| `asStack()` | `FluidStack` | Create a FluidStack (1000 mB) |
| `asStack(long amount)` | `FluidStack` | Create a FluidStack with the specified amount |
| `readOnlyStack()` | `FluidStack` | Cached read-only FluidStack (1000 mB); avoids repeated allocations |
| `asResource()` | `FluidResource` | FluidResource wrapper for transfer APIs |

:::warning
`getBlock()` and `getBucket()` return `null` if the fluid was registered without `.block(...)` or `.bucket(...)` in its builder chain.
:::

**Usage example:**

```java
FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_GOLD = REGISTRYLIB
        .fluid("molten_gold", STILL_TEXTURE, FLOW_TEXTURE)
        .lang("Molten Gold")
        .block(b -> {})
        .bucket(b -> {})
        .register();

// Later in code
FluidStack stack = MOLTEN_GOLD.asStack(500);
Fluid source = MOLTEN_GOLD.getSource();
Item bucket = MOLTEN_GOLD.getBucket();
```

## BlockEntityEntry\<T\>

**Extends:** `RegistryEntry<BlockEntityType<?>, BlockEntityType<T>>`

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `BlockEntityType<T>` | Get the BlockEntityType |

Host block binding is configured during registration via `validBlock()` or `validBlocks()` on the builder:

```java
BlockEntityEntry<MyBlockEntity> MY_BE = REGISTRYLIB
        .blockEntity("my_be", MyBlockEntity::new)
        .validBlock(MY_BLOCK)
        .renderer(() -> MyBlockEntityRenderer::new)
        .register();
```

:::tip
Bind multiple blocks with `validBlocks(block1, block2, ...)` when the same BlockEntity type is shared across several blocks.
:::

## EntityEntry\<T\>

**Extends:** `RegistryEntry<EntityType<?>, EntityType<T>>`

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `EntityType<T>` | Get the EntityType |
| `is(entity)` | `boolean` | Check if an Entity instance is of this type |

**Usage example:**

```java
EntityEntry<CrystalGuardian> CRYSTAL_GUARDIAN = REGISTRYLIB
        .<CrystalGuardian>entity("crystal_guardian", CrystalGuardian::new, MobCategory.MONSTER)
        .attributes(CrystalGuardian::createAttributes)
        .renderer(() -> CrystalGuardianRenderer::new)
        .register();

// Later in code
EntityType<CrystalGuardian> type = CRYSTAL_GUARDIAN.get();
boolean match = CRYSTAL_GUARDIAN.is(someEntity);
```

:::tip
Entities with attributes (any `LivingEntity` subclass) **must** call `.attributes()` on the builder. Omitting it causes a crash at entity spawn time.
:::

## RecipeRef\<T\>

A lazy reference to an existing `RecipeType<T>` + `RecipeSerializer<T>` pair. Used as input for `extendRecipe()` and `copyRecipe()`.

| Factory Method | Parameters | Description |
| --- | --- | --- |
| `RecipeRef.of(type, serializer)` | `RecipeType<T>, RecipeSerializer<T>` | Reference a vanilla or third-party recipe type |
| `RecipeRef.of(entry)` | `RecipeEntry<T>` | Reference your own mod's registered type (lazily resolved) |

| Method | Return type | Description |
| --- | --- | --- |
| `type()` | `RecipeType<T>` | Get the referenced RecipeType |
| `serializer()` | `RecipeSerializer<T>` | Get the referenced RecipeSerializer |

**Usage example:**

```java
// Vanilla types — resolved eagerly
RecipeRef<SmeltingRecipe> SMELTING_REF =
        RecipeRef.of(RecipeType.SMELTING, SmeltingRecipe.SERIALIZER);

// Mod types — resolved lazily when accessed
RecipeRef<AltarRecipe> ALTAR_REF =
        RecipeRef.of(SimpleRecipeExample.ALTAR);
```

## ExtendRecipeEntry\<T\>

Returned by `extendRecipe()`. Injects recipes into an existing type **without registering a new `RecipeType` or `RecipeSerializer`**. All three `addRecipe` overloads and `customRecipeData` are available. Chainable.

| Method | Return type | Description |
| --- | --- | --- |
| `addRecipe(name, recipe)` | `ExtendRecipeEntry<T>` | Add a direct recipe instance |
| `addRecipe(name, supplier)` | `ExtendRecipeEntry<T>` | Add a lazily-created recipe |
| `addRecipe(name, function)` | `ExtendRecipeEntry<T>` | Add a registry-aware recipe factory |
| `customRecipeData(consumer)` | `ExtendRecipeEntry<T>` | Raw control over RecipeProvider |

**Usage example:**

```java
ExtendRecipeEntry<SmeltingRecipe> EXTRA_SMELTING = REGISTRYLIB
        .<SmeltingRecipe>extendRecipe(SMELTING_REF)
        .customRecipeData(prov -> SimpleCookingRecipeBuilder.smelting(...)
                .save(prov, prov.safeKey(Items.AMETHYST_SHARD)));
```

## See Also

- [API Overview](/reference/api-overview) —entry point selection and common chains
- [Builder Methods](/reference/builder-methods) —complete method reference for all Builder types
