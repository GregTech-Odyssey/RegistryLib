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

:::note
`ChunkStateEntry`, `WorldStateEntry`, and `WorldgenFeatureEntry` are boundary handles, not `RegistryEntry` subclasses. They expose the ids and runtime access methods needed for their domain without pretending to be normal registry objects.
:::

## ItemEntry\<T\>

**Extends:** `RegistryEntry<Item, T>`

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `T` | Get the registered Item instance |
| `asStack()` | `ItemStack` | Create a default ItemStack (count 1) |
| `asStack(int count)` | `ItemStack` | Create an ItemStack with the specified count |
| `readOnlyStack()` | `ItemStack` | Defensive copy of a cached ItemStack (count 1); safe against external mutation |
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

## BlockEntityTypeEntry\<T\>

**Extends:** `RegistryEntry<BlockEntityType<?>, BlockEntityType<T>>`

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `BlockEntityType<T>` | Get the BlockEntityType |

Host block binding is configured during registration via `validBlock()` or `validBlocks()` on the builder:

```java
BlockEntityTypeEntry<MyBlockEntity> MY_BE = REGISTRYLIB
        .blockEntity("my_be", MyBlockEntity::new)
        .validBlock(MY_BLOCK)
        .renderer(() -> MyBlockEntityRenderer::new)
        .register();
```

:::tip
Bind multiple blocks with `validBlocks(block1, block2, ...)` when the same BlockEntity type is shared across several blocks.
:::

## DataComponentTypeEntry\<T\>

**Extends:** `RegistryEntry<DataComponentType<?>, DataComponentType<T>>`

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `DataComponentType<T>` | Get the registered component type |
| `get(stack)` | `T` | Read a component value from an `ItemStack` |
| `getOrDefault(stack, defaultValue)` | `T` | Read a component value with a fallback |
| `has(stack)` | `boolean` | Check whether the stack has the component |
| `set(stack, value)` | `T` | Set the component value and return the previous value |
| `remove(stack)` | `T` | Remove the component and return the previous value |
| `component(properties, value)` | `Item.Properties` | Add the component to item properties |

```java
DataComponentTypeEntry<String> API_NOTE = REGISTRYLIB.dataComponentTypeEntry(
        "api_note",
        builder -> builder.persistent(Codec.STRING));

String note = API_NOTE.getOrDefault(stack, "");
API_NOTE.set(stack, "Stored on the stack");
```

## AttachmentTypeEntry\<T\>

**Extends:** `RegistryEntry<AttachmentType<?>, AttachmentType<T>>`

| Method | Return type | Description |
| --- | --- | --- |
| `get()` | `AttachmentType<T>` | Get the registered NeoForge attachment type |
| `getOrCreate(holder)` | `T` | Read or create data on an `IAttachmentHolder` |
| `getIfPresent(holder)` | `Optional<T>` | Read existing data without creating it |
| `set(holder, value)` | `T` | Replace attachment data and return the previous value |
| `remove(holder)` | `T` | Remove attachment data and return the previous value |
| `sync(holder)` | `void` | Sync attachment data through NeoForge |

Prefer `chunkState(...)` or `worldState(...)` when the attachment represents gameplay state.

## ChunkStateEntry\<T\>

**Does not extend:** `RegistryEntry`

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

## WorldStateEntry\<T\>

**Does not extend:** `RegistryEntry`

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

## WorldgenFeatureEntry

**Record fields:** `configuredKey`, `placedKey`

| Method | Return type | Description |
| --- | --- | --- |
| `configuredKey()` | `ResourceKey<ConfiguredFeature<?, ?>>` | Key for the generated configured feature |
| `placedKey()` | `ResourceKey<PlacedFeature>` | Key for the generated placed feature |

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

## See Also

- [API Overview](/reference/api-overview) —entry point selection and common chains
- [Builder Methods](/reference/builder-methods) —complete method reference for all Builder types
