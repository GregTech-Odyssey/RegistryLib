---
sidebar_position: 6
title: Register Recipes
description: Quick reference for custom recipe registration patterns.
---

# Register Recipes

## Simple Recipe (Altar)

Register a custom `RecipeType` and `RecipeSerializer` via RegistryLib's convenience methods, then create a processing block and block entity.

### 1. Define the Recipe Class

```java
public class AltarRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient inputItem;
    private final ItemStack result;
    private final int processingTime;

    // Constructor, getters...

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return inputItem.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.copy();
    }

    @Override
    public boolean isSpecial() { return true; }

    @Override
    public boolean showNotification() { return false; }

    @Override
    public String group() { return ""; }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return SimpleRecipeExample.ALTAR_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return SimpleRecipeExample.ALTAR_TYPE.get();
    }

    // Codec & StreamCodec as static final fields
    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(...);
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = StreamCodec.composite(...);
}
```

### 2. Register RecipeType and RecipeSerializer

```java
public static final RegistryEntry<RecipeType<?>, RecipeType<AltarRecipe>> ALTAR_TYPE =
        REGISTRYLIB.recipeType("altar");

public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<AltarRecipe>> ALTAR_SERIALIZER =
        REGISTRYLIB.recipeSerializer(
                "altar", () -> new RecipeSerializer<>(AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC));
```

### 3. Add Recipe JSON

Place recipe definitions in `data/<modid>/recipe/`:

```json title="data/registrylibtest/recipe/altar_cobblestone_to_stone.json"
{
  "type": "registrylibtest:altar",
  "ingredient": "minecraft:cobblestone",
  "result": { "id": "minecraft:stone", "count": 1 },
  "processing_time": 40
}
```

:::important
`RecipeSerializer` in NeoForge 26.1 is a **record**, not an interface. Construct it via `new RecipeSerializer<>(mapCodec, streamCodec)`.
:::

## Full Recipe with Machine Tier (Infuser)

For complex machines that need extra context during recipe matching (e.g. machine tier), define a custom `RecipeInput`:

### Custom RecipeInput

```java
public record InfuserInput(ItemStack item, int machineTier) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        if (slot != 0) throw new IllegalArgumentException("No item for index " + slot);
        return item;
    }

    @Override
    public int size() { return 1; }
}
```

### Tier-Gated Matching

```java
@Override
public boolean matches(InfuserInput input, Level level) {
    return input.machineTier() >= requiredTier && inputItem.test(input.item());
}
```

### Multiple Tiers with Shared BlockEntity

```java
// T1 and T2 blocks with different tiers
public static final BlockEntry<InfuserBlock> INFUSER_T1 = REGISTRYLIB
        .block("infuser_t1", p -> new InfuserBlock(p, 1))
        .initialProperties(Blocks.IRON_BLOCK)
        .lang("Infuser Tier 1")
        .simpleItem()
        .register();

public static final BlockEntry<InfuserBlock> INFUSER_T2 = REGISTRYLIB
        .block("infuser_t2", p -> new InfuserBlock(p, 2))
        .initialProperties(Blocks.DIAMOND_BLOCK)
        .lang("Infuser Tier 2")
        .simpleItem()
        .register();

// Shared block entity for both tiers
public static final BlockEntityEntry<InfuserBlockEntity> INFUSER_BE = REGISTRYLIB
        .blockEntity("infuser", InfuserBlockEntity::new)
        .validBlocks(INFUSER_T1, INFUSER_T2)
        .register();
```

### Tier-Gated Recipe JSON

```json title="data/registrylibtest/recipe/infuser_gold_to_netherite.json"
{
  "type": "registrylibtest:infuser",
  "ingredient": "minecraft:gold_ingot",
  "result": { "id": "minecraft:netherite_scrap", "count": 1 },
  "processing_time": 400,
  "experience": 25.0,
  "required_tier": 2
}
```

## Common API Lookup

| Method | Purpose |
|---|---|
| `recipeType(name)` | Register a `RecipeType` via `simple()` |
| `recipeSerializer(name, factory)` | Register a `RecipeSerializer` record |
| `block(name, factory)` | Create the processing block |
| `blockEntity(name, factory)` | Create the processing block entity |
| `.validBlocks(...)` | Bind block entity to one or more blocks |

## Required Recipe Interface Methods

| Method | Description |
|---|---|
| `matches(input, level)` | Test if the input matches this recipe |
| `assemble(input)` | Produce the result `ItemStack` |
| `group()` | Return group string (usually `""`) |
| `showNotification()` | Whether to show recipe unlock notification |
| `getSerializer()` | Return the registered serializer |
| `getType()` | Return the registered type |
| `placementInfo()` | Return `PlacementInfo.NOT_PLACEABLE` for custom recipes |
| `recipeBookCategory()` | Return recipe book category |

## See Also

- [How-To: Register Block Entities](/how-to/register-block-entities)
- [Tutorial: Recipes & Tags](/tutorials/recipes-tags)
- [Reference: API Overview](/reference/api-overview)
