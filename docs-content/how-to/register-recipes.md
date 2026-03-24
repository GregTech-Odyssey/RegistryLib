---
sidebar_position: 6
title: Register Recipes
description: Quick reference for custom recipe registration patterns.
---

# Register Recipes

## Simple Recipe (Altar)

Use the `.recipeType()` builder to register a `RecipeType` + `RecipeSerializer`, then add recipe instances via the returned `RecipeEntry`.

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

### 2. Register and Add Recipes

Registration and recipe addition are now two separate steps:

```java
// Step 1: Register RecipeType + RecipeSerializer
public static final RecipeEntry<AltarRecipe> ALTAR = REGISTRYLIB
        .<AltarRecipe>recipeType("altar")
        .serializer(AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC)
        .register();

// Step 2: Add individual recipe instances for datagen
static {
    ALTAR.addRecipe("altar_cobblestone_to_stone",
            new AltarRecipe(Ingredient.of(Items.COBBLESTONE),
                    new ItemStackTemplate(Items.STONE), 40));
    ALTAR.addRecipe("altar_raw_iron_to_ingot",
            new AltarRecipe(Ingredient.of(Items.RAW_IRON),
                    new ItemStackTemplate(Items.IRON_INGOT), 80));
}
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
// Step 1: Register RecipeType + RecipeSerializer
public static final RecipeEntry<InfuserRecipe> INFUSER = REGISTRYLIB
        .<InfuserRecipe>recipeType("infuser")
        .serializer(InfuserRecipe.CODEC, InfuserRecipe.STREAM_CODEC)
        .register();

// Step 2: Add recipe instances
static {
    INFUSER.addRecipe("infuser_coal_to_diamond",
            new InfuserRecipe(Ingredient.of(Items.COAL),
                    new ItemStackTemplate(Items.DIAMOND), 200, 10.0F, 1));
    INFUSER.addRecipe("infuser_gold_to_netherite",
            new InfuserRecipe(Ingredient.of(Items.GOLD_INGOT),
                    new ItemStackTemplate(Items.NETHERITE_SCRAP), 400, 25.0F, 2));
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

## API Reference

### RecipeTypeBuilder

| Method | Purpose |
|---|---|
| `recipeType(name)` | Start recipe type builder (returns `RecipeTypeBuilder`) |
| `.serializer(codec, streamCodec)` | Set the codecs for the `RecipeSerializer` |
| `.register()` | Register and return `RecipeEntry<T>` |
| `.build()` | Register and return parent (for chaining) |

### RecipeEntry

| Method | Purpose |
|---|---|
| `.addRecipe(name, recipe)` | Add a recipe instance for datagen |
| `.addRecipe(name, supplier)` | Add a lazily-created recipe for datagen |
| `.addRecipe(name, registries -> recipe)` | Add recipe with registry access (for tags) |
| `.customRecipeData(consumer)` | Advanced: raw control over `RecipeProvider` |
| `.getType()` | Get the registered `RecipeType<T>` |
| `.getSerializer()` | Get the registered `RecipeSerializer<T>` |
| `.getTypeKey()` | Get the `ResourceKey` of the recipe type |
| `.getSerializerKey()` | Get the `ResourceKey` of the serializer |

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

## Using Different Ingredient Types

`Ingredient` is NeoForge's abstraction for matching input items. By using `Ingredient.CODEC` in your recipe's codec, all ingredient types (vanilla and NeoForge custom) are automatically supported — no extra code needed in your recipe class.

### Vanilla Ingredients

```java
// Single item
Ingredient.of(Items.COBBLESTONE)

// Multiple items (matches any)
Ingredient.of(Items.COAL, Items.CHARCOAL)

// Tag-based (matches all items in the tag) — requires registries lookup
registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.LOGS)
// Then: Ingredient.of(holderSet)
```

:::tip
Tag-based ingredients need a `HolderLookup.Provider` (tags aren't available in static registries during datagen). Use the **Function overload** of `addRecipe`:
```java
ENTRY.addRecipe("my_recipe", registries -> new MyRecipe(
        Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.LOGS)),
        new ItemStackTemplate(Items.CHARCOAL), 60));
```
:::

### NeoForge Custom Ingredients

NeoForge provides several `ICustomIngredient` implementations. All return a vanilla `Ingredient` from their factory methods:

```java
// CompoundIngredient (OR) — matches if ANY child matches
CompoundIngredient.of(
        Ingredient.of(items.getOrThrow(ItemTags.PLANKS)),
        Ingredient.of(items.getOrThrow(ItemTags.LOGS)))

// DifferenceIngredient (A minus B) — matches A but excludes B
DifferenceIngredient.of(
        Ingredient.of(items.getOrThrow(ItemTags.WOOL)),
        Ingredient.of(Items.WHITE_WOOL))

// IntersectionIngredient (AND) — matches only if ALL children match
IntersectionIngredient.of(ingredientA, ingredientB)

// DataComponentIngredient — matches items with specific data components
DataComponentIngredient.of(false, DataComponents.DAMAGE, 100, Items.IRON_SWORD)

// BlockTagIngredient — matches items from a block tag (use only when no item tag exists)
new BlockTagIngredient(BlockTags.CONVERTABLE_TO_MUD).toVanilla()
```

### Complete Example

```java
static {
    // Simple item ingredient
    ENTRY.addRecipe("basic",
            new MyRecipe(Ingredient.of(Items.COBBLESTONE), ...));

    // Tag ingredient (via registries Function overload)
    ENTRY.addRecipe("from_logs",
            registries -> new MyRecipe(
                    Ingredient.of(registries.lookupOrThrow(Registries.ITEM)
                            .getOrThrow(ItemTags.LOGS)),
                    ...));

    // CompoundIngredient: planks OR logs
    ENTRY.addRecipe("planks_or_logs",
            registries -> {
                var items = registries.lookupOrThrow(Registries.ITEM);
                return new MyRecipe(
                        CompoundIngredient.of(
                                Ingredient.of(items.getOrThrow(ItemTags.PLANKS)),
                                Ingredient.of(items.getOrThrow(ItemTags.LOGS))),
                        ...);
            });

    // DifferenceIngredient: all wool except white
    ENTRY.addRecipe("colored_wool",
            registries -> new MyRecipe(
                    DifferenceIngredient.of(
                            Ingredient.of(registries.lookupOrThrow(Registries.ITEM)
                                    .getOrThrow(ItemTags.WOOL)),
                            Ingredient.of(Items.WHITE_WOOL)),
                    ...));

    // DataComponentIngredient: iron sword with damage=100
    ENTRY.addRecipe("damaged_sword",
            new MyRecipe(
                    DataComponentIngredient.of(false, DataComponents.DAMAGE, 100,
                            Items.IRON_SWORD),
                    ...));
}
```

:::note
The `Ingredient.CODEC` used in recipe serialization automatically handles all ingredient types, including NeoForge custom ones. The codec dispatches based on the `"neoforge:ingredient_type"` field in JSON for custom ingredients, or uses the vanilla string/tag format for vanilla ingredients.
:::

## See Also

- [How-To: Register Block Entities](/how-to/register-block-entities)
- [How-To: Register Custom Ingredients](/how-to/register-custom-ingredients)
- [Tutorial: Recipes & Tags](/tutorials/recipes-tags)
- [Reference: API Overview](/reference/api-overview)
