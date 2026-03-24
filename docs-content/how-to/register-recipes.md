---
sidebar_position: 6
title: Register Recipes
description: Quick reference for custom recipe registration patterns.
---

# Register Recipes

## Simple Recipe (Altar)

Use the `.recipe()` builder to register a `RecipeType` + `RecipeSerializer` and add recipe datagen — all in one fluent chain.

### 1. Define the Recipe Class

```java
public class AltarRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient inputItem;
    private final ItemStackTemplate result;
    private final int processingTime;

    // Constructor, getters...

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return inputItem.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
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
        return SimpleRecipeExample.ALTAR.getSerializer();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return SimpleRecipeExample.ALTAR.getType();
    }

    // Codec & StreamCodec as static final fields
    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(...);
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = StreamCodec.composite(...);
}
```

### 2. Register with the Recipe Builder

One call registers `RecipeType`, `RecipeSerializer`, and adds recipe JSON datagen:

```java
public static final RecipeEntry<AltarRecipe> ALTAR = REGISTRYLIB
        .<AltarRecipe>recipe("altar")
        .serializer(AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC)
        .addRecipe("altar_cobblestone_to_stone",
                new AltarRecipe(Ingredient.of(Items.COBBLESTONE),
                        new ItemStackTemplate(Items.STONE), 40))
        .addRecipe("altar_raw_iron_to_ingot",
                new AltarRecipe(Ingredient.of(Items.RAW_IRON),
                        new ItemStackTemplate(Items.IRON_INGOT), 80))
        .register();
```

The resulting `RecipeEntry<T>` provides:

- `ALTAR.getType()` — the `RecipeType<AltarRecipe>`
- `ALTAR.getSerializer()` — the `RecipeSerializer<AltarRecipe>`
- `ALTAR.getTypeKey()` / `ALTAR.getSerializerKey()` — the `ResourceKey`s

:::important
`RecipeSerializer` in NeoForge 26.1 is a **record**, not an interface. The builder constructs it via `new RecipeSerializer<>(codec, streamCodec)` automatically.
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

### Tier-Gated Registration

```java
public static final RecipeEntry<InfuserRecipe> INFUSER = REGISTRYLIB
        .<InfuserRecipe>recipe("infuser")
        .serializer(InfuserRecipe.CODEC, InfuserRecipe.STREAM_CODEC)
        .addRecipe("infuser_coal_to_diamond",
                new InfuserRecipe(Ingredient.of(Items.COAL),
                        new ItemStackTemplate(Items.DIAMOND), 200, 10.0F, 1))
        .addRecipe("infuser_gold_to_netherite",
                new InfuserRecipe(Ingredient.of(Items.GOLD_INGOT),
                        new ItemStackTemplate(Items.NETHERITE_SCRAP), 400, 25.0F, 2))
        .register();
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

## RecipeBuilder API

| Method | Purpose |
|---|---|
| `recipe(name)` | Start recipe builder (returns `RecipeBuilder`) |
| `.serializer(codec, streamCodec)` | Set the codecs for the `RecipeSerializer` |
| `.addRecipe(name, recipe)` | Add a recipe instance for datagen |
| `.addRecipe(name, supplier)` | Add a lazily-created recipe for datagen |
| `.customRecipeData(consumer)` | Advanced: raw control over `RecipeProvider` |
| `.register()` | Register and return `RecipeEntry<T>` |
| `.build()` | Register and return parent (for chaining) |

## Required Recipe Interface Methods

| Method | Description |
|---|---|
| `matches(input, level)` | Test if the input matches this recipe |
| `assemble(input)` | Produce the result `ItemStack` |
| `group()` | Return group string (usually `""`) |
| `showNotification()` | Whether to show recipe unlock notification |
| `getSerializer()` | Return the registered serializer via `ENTRY.getSerializer()` |
| `getType()` | Return the registered type via `ENTRY.getType()` |
| `placementInfo()` | Return `PlacementInfo.NOT_PLACEABLE` for custom recipes |
| `recipeBookCategory()` | Return recipe book category |

## See Also

- [How-To: Register Block Entities](/how-to/register-block-entities)
- [Tutorial: Recipes & Tags](/tutorials/recipes-tags)
- [Reference: API Overview](/reference/api-overview)
