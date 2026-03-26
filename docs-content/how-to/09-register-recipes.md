---
sidebar_position: 9
title: Register Recipes
description: Quick reference for custom recipe registration patterns.
---

# Register Recipes

## Simple Recipe (Altar)

Use the `.recipeType()` builder to register a `RecipeType` + `RecipeSerializer`, then add recipe instances via the returned `RecipeTypeEntry`.

### 1. Define the Recipe Class

```java
public class AltarRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient inputItem;
    private final ItemStackTemplate result;
    private final int processingTime;

    public AltarRecipe(Ingredient inputItem, ItemStackTemplate result, int processingTime) {
        this.inputItem = inputItem;
        this.result = result;
        this.processingTime = processingTime;
    }

    public Ingredient getInputItem() { return inputItem; }
    public ItemStackTemplate getResult() { return result; }
    public int getProcessingTime() { return processingTime; }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return inputItem.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }

    @Override public boolean isSpecial() { return true; }
    @Override public boolean showNotification() { return false; }
    @Override public String group() { return ""; }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return SimpleRecipeExample.ALTAR.getSerializer();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return SimpleRecipeExample.ALTAR.get();
    }

    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(AltarRecipe::getInputItem),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(AltarRecipe::getResult),
                    Codec.INT.optionalFieldOf("processing_time", 60)
                            .forGetter(AltarRecipe::getProcessingTime))
                    .apply(inst, AltarRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, AltarRecipe::getInputItem,
                    ItemStackTemplate.STREAM_CODEC, AltarRecipe::getResult,
                    ByteBufCodecs.INT, AltarRecipe::getProcessingTime,
                    AltarRecipe::new);
}
```

### 2. Register and Add Recipes

Registration and recipe addition are two separate steps:

```java
// Step 1: Register RecipeType + RecipeSerializer
public static final RecipeTypeEntry<AltarRecipe> ALTAR = REGISTRYLIB
        .<AltarRecipe>recipeType("altar")
        .serializer(AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC)
        .register();

// Step 2: Add individual recipe instances for datagen
static {
    // Single item ingredient
    ALTAR.addRecipe("altar_cobblestone_to_stone",
            new AltarRecipe(Ingredient.of(Items.COBBLESTONE),
                    new ItemStackTemplate(Items.STONE), 40));
    ALTAR.addRecipe("altar_raw_iron_to_ingot",
            new AltarRecipe(Ingredient.of(Items.RAW_IRON),
                    new ItemStackTemplate(Items.IRON_INGOT), 80));

    // Multiple items ingredient (matches any of them)
    ALTAR.addRecipe("altar_fuel_to_torch",
            new AltarRecipe(Ingredient.of(Items.COAL, Items.CHARCOAL),
                    new ItemStackTemplate(Items.TORCH), 30));

    // Tag ingredient — requires registries Function overload
    ALTAR.addRecipe("altar_logs_to_charcoal",
            registries -> new AltarRecipe(
                    Ingredient.of(registries.lookupOrThrow(Registries.ITEM)
                            .getOrThrow(ItemTags.LOGS)),
                    new ItemStackTemplate(Items.CHARCOAL), 60));
}
```

The resulting `RecipeTypeEntry<T>` provides:

- `ALTAR.get()` — the `RecipeType<AltarRecipe>` (inherited from `RegistryEntry`)
- `ALTAR.getSerializer()` — the `RecipeSerializer<AltarRecipe>`
- `ALTAR.getKey()` — the `ResourceKey<RecipeType<?>>`

:::tip
Tag-based ingredients need a `HolderLookup.Provider` (tags aren't available in static registries during datagen). Use the **Function overload** of `addRecipe`:
```java
ENTRY.addRecipe("name", registries -> new MyRecipe(
        Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.LOGS)),
        ...));
```
:::

:::important
`RecipeSerializer` in NeoForge 26.1 is a **record**, not an interface. The builder constructs it via `new RecipeSerializer<>(codec, streamCodec)` automatically.
:::

## Custom Factories

By default, `RecipeTypeBuilder` creates `RecipeType` via `RecipeType.simple(id)` and `RecipeSerializer` via `new RecipeSerializer<>(codec, streamCodec)`. You can override the `RecipeType` creation with a custom factory function.

### Custom RecipeType Factory

Use `.typeFactory()` to provide a custom `RecipeType` creation function. The function receives the registry `Identifier`.

```java
public static final RecipeTypeEntry<InfuserRecipe> INFUSER_CUSTOM = REGISTRYLIB
        .<InfuserRecipe>recipeType("infuser_custom")
        .typeFactory(RecipeType::simple)
        .serializer(InfuserRecipe.CODEC, InfuserRecipe.STREAM_CODEC)
        .register();
```

## Full Recipe with Machine Tier (Infuser)

For complex machines that need extra context during recipe matching (e.g. machine tier), define a custom `RecipeInput`. This example also demonstrates all supported ingredient types.

### Recipe Class with Custom RecipeInput

`InfuserInput` is defined as an **inner record** of the recipe class:

```java
public class InfuserRecipe implements Recipe<InfuserRecipe.InfuserInput> {

    private final Ingredient inputItem;
    private final ItemStackTemplate result;
    private final int processingTime;
    private final float experience;
    private final int requiredTier;

    public InfuserRecipe(Ingredient inputItem, ItemStackTemplate result,
                         int processingTime, float experience, int requiredTier) {
        this.inputItem = inputItem;
        this.result = result;
        this.processingTime = processingTime;
        this.experience = experience;
        this.requiredTier = requiredTier;
    }

    // getters...

    @Override
    public boolean matches(InfuserInput input, Level level) {
        return input.machineTier() >= requiredTier && inputItem.test(input.item());
    }

    @Override
    public ItemStack assemble(InfuserInput input) { return result.create(); }
    @Override public boolean isSpecial() { return true; }
    @Override public boolean showNotification() { return false; }
    @Override public String group() { return ""; }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }

    @Override
    public RecipeSerializer<? extends Recipe<InfuserInput>> getSerializer() {
        return FullRecipeExample.INFUSER.getSerializer();
    }

    @Override
    public RecipeType<? extends Recipe<InfuserInput>> getType() {
        return FullRecipeExample.INFUSER.get();
    }

    // Custom RecipeInput as inner record
    public record InfuserInput(ItemStack item, int machineTier) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) {
            if (slot != 0) throw new IllegalArgumentException("No item for index " + slot);
            return item;
        }
        @Override public int size() { return 1; }
    }

    public static final MapCodec<InfuserRecipe> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(InfuserRecipe::getInputItem),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(InfuserRecipe::getResult),
                    Codec.INT.optionalFieldOf("processing_time", 100)
                            .forGetter(InfuserRecipe::getProcessingTime),
                    Codec.FLOAT.optionalFieldOf("experience", 0.0F)
                            .forGetter(InfuserRecipe::getExperience),
                    Codec.INT.optionalFieldOf("required_tier", 1)
                            .forGetter(InfuserRecipe::getRequiredTier))
                    .apply(inst, InfuserRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfuserRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, InfuserRecipe::getInputItem,
                    ItemStackTemplate.STREAM_CODEC, InfuserRecipe::getResult,
                    ByteBufCodecs.INT, InfuserRecipe::getProcessingTime,
                    ByteBufCodecs.FLOAT, InfuserRecipe::getExperience,
                    ByteBufCodecs.INT, InfuserRecipe::getRequiredTier,
                    InfuserRecipe::new);
}
```

### Register and Add Recipes (All Ingredient Types)

```java
// Step 1: Register RecipeType + RecipeSerializer
public static final RecipeTypeEntry<InfuserRecipe> INFUSER = REGISTRYLIB
        .<InfuserRecipe>recipeType("infuser")
        .serializer(InfuserRecipe.CODEC, InfuserRecipe.STREAM_CODEC)
        .register();

// Step 2: Add recipe instances — demonstrating all ingredient types
static {
    // 1) Single item ingredient
    INFUSER.addRecipe("infuser_coal_to_diamond",
            new InfuserRecipe(Ingredient.of(Items.COAL),
                    new ItemStackTemplate(Items.DIAMOND), 20, 10.0F, 1));
    INFUSER.addRecipe("infuser_gold_to_netherite",
            new InfuserRecipe(Ingredient.of(Items.GOLD_INGOT),
                    new ItemStackTemplate(Items.NETHERITE_SCRAP), 40, 25.0F, 2));

    // 2) CompoundIngredient (OR logic) — matches planks OR logs
    INFUSER.addRecipe("infuser_planks_or_logs_to_stick",
            registries -> {
                var items = registries.lookupOrThrow(Registries.ITEM);
                return new InfuserRecipe(
                        CompoundIngredient.of(
                                Ingredient.of(items.getOrThrow(ItemTags.PLANKS)),
                                Ingredient.of(items.getOrThrow(ItemTags.LOGS))),
                        new ItemStackTemplate(Items.STICK, 4), 30, 5.0F, 1);
            });

    // 3) DifferenceIngredient (set subtraction) — all wool except white
    INFUSER.addRecipe("infuser_non_white_wool_to_string",
            registries -> new InfuserRecipe(
                    DifferenceIngredient.of(
                            Ingredient.of(registries.lookupOrThrow(Registries.ITEM)
                                    .getOrThrow(ItemTags.WOOL)),
                            Ingredient.of(Items.WHITE_WOOL)),
                    new ItemStackTemplate(Items.STRING, 2), 40, 8.0F, 1));

    // 4) DataComponentIngredient — match iron sword with damage=100
    INFUSER.addRecipe("infuser_damaged_sword_to_iron",
            new InfuserRecipe(
                    DataComponentIngredient.of(false, DataComponents.DAMAGE, 100,
                            Items.IRON_SWORD),
                    new ItemStackTemplate(Items.IRON_INGOT, 2), 60, 15.0F, 2));

    // 5) Custom Ingredient (MinDurabilityIngredient) — swords with ≥200 durability
    INFUSER.addRecipe("infuser_durable_swords_to_diamond",
            new InfuserRecipe(
                    MinDurabilityIngredient.of(ItemTags.SWORDS, 200),
                    new ItemStackTemplate(Items.DIAMOND), 80, 20.0F, 2));
}
```

### Multiple Tiers with Shared BlockEntity

```java
public static final BlockEntry<InfuserBlock> INFUSER_T1 = REGISTRYLIB
        .block("infuser_t1", p -> new InfuserBlock(p, 1))
        .initialProperties(Blocks.IRON_BLOCK)
        .properties(p -> p.strength(3.0F, 6.0F))
        .lang("Infuser Tier 1")
        .lang(LANG_ZH_CN, "注入器 T1")
        .simpleItem()
        .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
        .register();

public static final BlockEntry<InfuserBlock> INFUSER_T2 = REGISTRYLIB
        .block("infuser_t2", p -> new InfuserBlock(p, 2))
        .initialProperties(Blocks.DIAMOND_BLOCK)
        .properties(p -> p.strength(5.0F, 8.0F))
        .lang("Infuser Tier 2")
        .lang(LANG_ZH_CN, "注入器 T2")
        .simpleItem()
        .addTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
        .register();

public static final BlockEntityTypeEntry<InfuserBlockEntity> INFUSER_BE = REGISTRYLIB
        .blockEntity("infuser", InfuserBlockEntity::new)
        .validBlocks(INFUSER_T1, INFUSER_T2)
        .register();
```

## Multi-Input Recipe with Fluid (Synthesizer)

A machine recipe that requires **multiple item inputs** (with count checking) and a **fluid input** (with amount checking). This is the typical pattern for multi-input modded machines.

### Key Types

| Type | Purpose | Test Logic |
|---|---|---|
| `SizedIngredient` | Item ingredient + count | `ingredient.test(stack) && stack.getCount() >= count` |
| `SizedFluidIngredient` | Fluid ingredient + amount (mB) | `ingredient.test(stack) && stack.getAmount() >= amount` |

### Recipe Class with Multi-Input + Fluid

```java
public class SynthesizerRecipe implements Recipe<SynthesizerRecipe.SynthesizerInput> {

    private final List<SizedIngredient> ingredients;
    private final SizedFluidIngredient fluidIngredient;
    private final ItemStackTemplate result;
    private final int processingTime;
    private final float experience;

    // Constructor, getters omitted ...

    @Override
    public boolean matches(SynthesizerInput input, Level level) {
        if (input.items().size() != ingredients.size()) return false;
        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).test(input.items().get(i))) return false;
        }
        return fluidIngredient.test(input.fluid());
    }

    @Override
    public ItemStack assemble(SynthesizerInput input) { return result.create(); }

    @Override public RecipeSerializer<? extends Recipe<SynthesizerInput>> getSerializer() {
        return MultiInputRecipeExample.SYNTHESIZER.getSerializer();
    }
    @Override public RecipeType<? extends Recipe<SynthesizerInput>> getType() {
        return MultiInputRecipeExample.SYNTHESIZER.get();
    }
    // isSpecial(), showNotification(), etc. ...

    // === Custom RecipeInput: multiple items + fluid ===
    public record SynthesizerInput(List<ItemStack> items, FluidStack fluid) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) {
            if (slot < 0 || slot >= items.size())
                throw new IllegalArgumentException("No item for index " + slot);
            return items.get(slot);
        }
        @Override public int size() { return items.size(); }
    }

    // === Codec ===
    public static final MapCodec<SynthesizerRecipe> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    SizedIngredient.NESTED_CODEC.listOf().fieldOf("ingredients")
                            .forGetter(SynthesizerRecipe::getIngredients),
                    SizedFluidIngredient.CODEC.fieldOf("fluid")
                            .forGetter(SynthesizerRecipe::getFluidIngredient),
                    ItemStackTemplate.CODEC.fieldOf("result")
                            .forGetter(SynthesizerRecipe::getResult),
                    Codec.INT.optionalFieldOf("processing_time", 200)
                            .forGetter(SynthesizerRecipe::getProcessingTime),
                    Codec.FLOAT.optionalFieldOf("experience", 0.0F)
                            .forGetter(SynthesizerRecipe::getExperience))
                    .apply(inst, SynthesizerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SynthesizerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    SynthesizerRecipe::getIngredients,
                    SizedFluidIngredient.STREAM_CODEC,
                    SynthesizerRecipe::getFluidIngredient,
                    ItemStackTemplate.STREAM_CODEC,
                    SynthesizerRecipe::getResult,
                    ByteBufCodecs.INT,
                    SynthesizerRecipe::getProcessingTime,
                    ByteBufCodecs.FLOAT,
                    SynthesizerRecipe::getExperience,
                    SynthesizerRecipe::new);
}
```

:::tip Codec Cheat Sheet
- **Items**: `SizedIngredient.NESTED_CODEC` → `{"ingredient": "minecraft:iron_ingot", "count": 3}`
- **Items list**: `SizedIngredient.NESTED_CODEC.listOf()` → `[{"ingredient": ..., "count": ...}, ...]`
- **Fluid**: `SizedFluidIngredient.CODEC` → `{"ingredient": "minecraft:water", "amount": 1000}`
:::

### Register and Add Multi-Input Recipes

```java
public static final RecipeTypeEntry<SynthesizerRecipe> SYNTHESIZER = REGISTRYLIB
        .<SynthesizerRecipe>recipeType("synthesizer")
        .serializer(SynthesizerRecipe.CODEC, SynthesizerRecipe.STREAM_CODEC)
        .register();

static {
    // 1) Basic: 3 iron + 2 gold + 1000mB water -> diamond
    SYNTHESIZER.addRecipe("synthesizer_iron_gold_water_to_diamond",
            new SynthesizerRecipe(
                    List.of(
                            SizedIngredient.of(Items.IRON_INGOT, 3),
                            SizedIngredient.of(Items.GOLD_INGOT, 2)),
                    SizedFluidIngredient.of(Fluids.WATER, 1000),
                    new ItemStackTemplate(Items.DIAMOND), 200, 30.0F));

    // 2) Custom ingredient + fluid:
    //    1 sword (durability >= 200) + 4 emeralds + 500mB lava -> netherite ingot
    SYNTHESIZER.addRecipe("synthesizer_durable_sword_to_netherite",
            new SynthesizerRecipe(
                    List.of(
                            new SizedIngredient(
                                    MinDurabilityIngredient.of(ItemTags.SWORDS, 200), 1),
                            SizedIngredient.of(Items.EMERALD, 4)),
                    SizedFluidIngredient.of(Fluids.LAVA, 500),
                    new ItemStackTemplate(Items.NETHERITE_INGOT), 400, 50.0F));
}
```

### Generated JSON Example

```json title="synthesizer_iron_gold_water_to_diamond.json"
{
  "type": "registrylibtest:synthesizer",
  "experience": 30.0,
  "fluid": {
    "ingredient": "minecraft:water",
    "amount": 1000
  },
  "ingredients": [
    { "ingredient": "minecraft:iron_ingot", "count": 3 },
    { "ingredient": "minecraft:gold_ingot", "count": 2 }
  ],
  "result": { "id": "minecraft:diamond" }
}
```

:::info NeoForge 26.1 FluidStack Changes
**`FluidStack` has NOT been removed.** It is still the standard mutable fluid container (`net.neoforged.neoforge.fluids.FluidStack`) used in recipes, fluid handlers, and `SizedFluidIngredient`.

NeoForge 26.1 added `FluidResource` (`net.neoforged.neoforge.transfer.fluid.FluidResource`) — an **immutable** version without an amount, used by the new Transfer API. For recipe crafting, continue using `FluidStack`, `FluidIngredient`, and `SizedFluidIngredient`.
:::

## Using Different Ingredient Types

`Ingredient` is NeoForge's abstraction for matching input items. By using `Ingredient.CODEC` in your recipe's codec, all ingredient types are automatically supported — no extra code needed in your recipe class.

### Vanilla Ingredients

| Type | Example | Note |
|---|---|---|
| Single item | `Ingredient.of(Items.COBBLESTONE)` | |
| Multiple items | `Ingredient.of(Items.COAL, Items.CHARCOAL)` | Matches any of them |
| Tag-based | `Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.LOGS))` | Requires `registries` |

### NeoForge Built-in Custom Ingredients

| Type | Description | Example |
|---|---|---|
| `CompoundIngredient` | OR logic — matches if **any** child matches | `CompoundIngredient.of(ingredientA, ingredientB)` |
| `DifferenceIngredient` | Set subtraction — matches A but excludes B | `DifferenceIngredient.of(ingredientA, ingredientB)` |
| `IntersectionIngredient` | AND logic — matches only if **all** children match | `IntersectionIngredient.of(ingredientA, ingredientB)` |
| `DataComponentIngredient` | Matches items with specific data components | `DataComponentIngredient.of(false, DataComponents.DAMAGE, 100, Items.IRON_SWORD)` |
| `BlockTagIngredient` | Matches items from a block tag | `new BlockTagIngredient(BlockTags.CONVERTABLE_TO_MUD).toVanilla()` |

### Sized Wrappers (for count / amount checking)

| Type | Description | Example |
|---|---|---|
| `SizedIngredient` | Item ingredient + required count | `SizedIngredient.of(Items.IRON_INGOT, 3)` |
| `SizedFluidIngredient` | Fluid ingredient + required amount (mB) | `SizedFluidIngredient.of(Fluids.WATER, 1000)` |
| `FluidIngredient` | Matches a fluid (no amount check) | `FluidIngredient.of(Fluids.WATER)` |

### User-Defined Custom Ingredients

You can create your own `ICustomIngredient` implementations. See [How-To: Register Custom Ingredients](/how-to/register-custom-ingredients) for a complete walkthrough.

```java
// Use custom ingredient in recipes — works exactly like built-in ones
MinDurabilityIngredient.of(ItemTags.SWORDS, 200)
```

:::note
`Ingredient.CODEC` automatically handles all ingredient types, including custom ones. The codec dispatches based on the `"neoforge:ingredient_type"` field in JSON for custom ingredients, or uses the vanilla format for vanilla ingredients.
:::

## Add Recipes to Vanilla / Third-Party Types

Use `core.addRecipe()` to inject recipes into **existing** recipe types (vanilla, NeoForge, or third-party) **without registering a new type**. The generated JSON uses the original type's `"type"` field but lives under your mod's namespace.

```java
// Add a vanilla smelting recipe
REGISTRYLIB.addRecipe("smelting/amethyst_shard",
        new SmeltingRecipe(
                new Recipe.CommonInfo(true),
                new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.BLOCKS, ""),
                Ingredient.of(Items.COBBLESTONE),
                new ItemStackTemplate(Items.AMETHYST_SHARD),
                0.5F, 200));

// Lazy supplier variant
REGISTRYLIB.addRecipe("smelting/copper_from_raw",
        () -> new SmeltingRecipe(...));

// Registry-aware variant (for tag-based ingredients)
REGISTRYLIB.addRecipe("smelting/logs_to_charcoal",
        registries -> new SmeltingRecipe(
                new Recipe.CommonInfo(true),
                new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, ""),
                Ingredient.of(registries.lookupOrThrow(Registries.ITEM)
                        .getOrThrow(ItemTags.LOGS)),
                new ItemStackTemplate(Items.CHARCOAL),
                0.15F, 200));
```

### Generated JSON

```json title="data/registrylibtest/recipe/smelting/amethyst_shard.json"
{
  "type": "minecraft:smelting",
  "category": "blocks",
  "cookingtime": 200,
  "experience": 0.5,
  "ingredient": "minecraft:cobblestone",
  "result": { "id": "minecraft:amethyst_shard" }
}
```

Note the `"type"` is `"minecraft:smelting"` — the recipe is injected into the vanilla type. The file lives under `data/registrylibtest/recipe/` (your mod's namespace) so it doesn't conflict with vanilla recipes.

:::tip
`core.addRecipe()` works for any recipe type — vanilla, NeoForge, or third-party. For custom recipe types registered via `recipeType()`, prefer `RecipeTypeEntry.addRecipe()` which automatically prefixes the type name.
:::

## Adding Recipes: `addRecipe` on RecipeTypeEntry

`RecipeTypeEntry.addRecipe()` is a convenience wrapper that delegates to `core.addRecipe()` with the type name prefix. Three overloads:

| Overload | When to Use |
|---|---|
| `addRecipe(name, recipe)` | Recipe has no runtime dependencies |
| `addRecipe(name, supplier)` | Recipe construction requires deferred values |
| `addRecipe(name, registries -> recipe)` | Recipe needs tag lookups (`Ingredient.of(tag)`) |

```java
ALTAR.addRecipe("cobblestone_to_stone",
        new AltarRecipe(Ingredient.of(Items.COBBLESTONE),
                new ItemStackTemplate(Items.STONE), 40));
```

## API Reference

### RecipeTypeBuilder

| Method | Purpose |
|---|---|
| `recipeType(name)` | Start recipe type builder (returns `RecipeTypeBuilder`) |
| `.serializer(codec, streamCodec)` | Set the codecs for the `RecipeSerializer` |
| `.typeFactory(function)` | Custom `RecipeType` creation factory (receives `Identifier`) |
| `.register()` | Register and return `RecipeTypeEntry<T>` |
| `.build()` | Register and return parent (for chaining) |

### RegistryCore.addRecipe

Add recipes to any existing recipe type without registering a new `RecipeType`.

| Method | Purpose |
|---|---|
| `addRecipe(id, recipe)` | Add a recipe instance for datagen |
| `addRecipe(id, supplier)` | Add a lazily-created recipe |
| `addRecipe(id, registries -> recipe)` | Add recipe with registry access (for tags) |

### RecipeTypeEntry

| Method | Purpose |
|---|---|
| `.addRecipe(name, recipe)` | Add a recipe instance (convenience, delegates to `core.addRecipe`) |
| `.addRecipe(name, supplier)` | Add a lazily-created recipe |
| `.addRecipe(name, registries -> recipe)` | Add recipe with registry access |
| `.get()` | Get the registered `RecipeType<T>` (inherited from `RegistryEntry`) |
| `.getSerializer()` | Get the registered `RecipeSerializer<T>` |

### FluidIngredientType Registration

| Method | Purpose |
|---|---|
| `fluidIngredientType(name, codec)` | Register a custom `FluidIngredientType` (auto StreamCodec) |
| `fluidIngredientType(name, codec, streamCodec)` | Register a custom `FluidIngredientType` (explicit StreamCodec) |

## Required Recipe Interface Methods

| Method | Description |
|---|---|
| `matches(input, level)` | Test if the input matches this recipe |
| `assemble(input)` | Produce the result `ItemStack` |
| `group()` | Return group string (usually `""`) |
| `showNotification()` | Whether to show recipe unlock notification |
| `getSerializer()` | Return the registered serializer via `ENTRY.getSerializer()` |
| `getType()` | Return the registered type via `ENTRY.get()` |
| `placementInfo()` | Return `PlacementInfo.NOT_PLACEABLE` for custom recipes |
| `recipeBookCategory()` | Return recipe book category |

## See Also

- [How-To: Register Block Entities](/how-to/register-block-entities)
- [How-To: Register Custom Ingredients](/how-to/register-custom-ingredients)
- [Tutorial: Recipes & Tags](/tutorials/recipes-tags)
- [Reference: API Overview](/reference/api-overview)
