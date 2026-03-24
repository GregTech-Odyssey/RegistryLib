---
sidebar_position: 7
title: Register Custom Ingredients
description: How to create and register custom Ingredient types with RegistryLib.
---

# Register Custom Ingredients

NeoForge's `ICustomIngredient` system lets you define custom matching logic for recipe inputs. RegistryLib provides a one-line API to register custom ingredient types.

## Overview

A custom ingredient consists of two parts:

1. **Ingredient class** — implements `ICustomIngredient` with matching logic and codecs
2. **Registration** — registers an `IngredientType<T>` to the NeoForge ingredient registry

## Step 1: Implement ICustomIngredient

Create a class that implements `ICustomIngredient`. You need:

- A `MapCodec<YourIngredient>` for JSON serialization
- Optionally a `StreamCodec` for network syncing (auto-derived from MapCodec if omitted)
- `test(ItemStack)` — the core matching logic
- `items()` — stream of items for display purposes
- `isSimple()` — `true` if matching only checks item type, `false` if it inspects components/NBT
- `getType()` — return your registered `IngredientType`
- `equals()` and `hashCode()` — **required by contract**

```java
public class MinDurabilityIngredient implements ICustomIngredient {

    private final TagKey<Item> tag;
    private final int minDurability;

    // ── Codecs ────────────────────────────────────────────────────────────
    public static final MapCodec<MinDurabilityIngredient> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(e -> e.tag),
                    Codec.INT.fieldOf("min_durability").forGetter(e -> e.minDurability))
                    .apply(inst, MinDurabilityIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MinDurabilityIngredient> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    // ── Constructor ───────────────────────────────────────────────────────
    public MinDurabilityIngredient(TagKey<Item> tag, int minDurability) {
        this.tag = tag;
        this.minDurability = minDurability;
    }

    // ── Matching Logic ────────────────────────────────────────────────────
    @Override
    public boolean test(ItemStack stack) {
        if (!stack.is(tag)) return false;
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) return false;
        int remaining = maxDamage - stack.getDamageValue();
        return remaining >= minDurability;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return BuiltInRegistries.ITEM.getOrThrow(tag).stream();
    }

    @Override
    public boolean isSimple() {
        return false; // We inspect stack components
    }

    @Override
    public IngredientType<?> getType() {
        return SimpleIngredientTypeExample.MIN_DURABILITY.get();
    }

    // ── Convenience Factory ───────────────────────────────────────────────
    public static Ingredient of(TagKey<Item> tag, int minDurability) {
        return new MinDurabilityIngredient(tag, minDurability).toVanilla();
    }

    // ── equals / hashCode (required!) ─────────────────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinDurabilityIngredient that)) return false;
        return minDurability == that.minDurability && tag.equals(that.tag);
    }

    @Override
    public int hashCode() {
        return 31 * tag.hashCode() + minDurability;
    }
}
```

## Step 2: Register the IngredientType

Use `REGISTRYLIB.ingredientType()` to register:

```java
// With auto-derived StreamCodec (simplest)
public static final IngredientTypeEntry<MinDurabilityIngredient> MIN_DURABILITY =
        REGISTRYLIB.ingredientType("min_durability", MinDurabilityIngredient.CODEC);

// With explicit StreamCodec (for non-simple ingredients)
public static final IngredientTypeEntry<MinDurabilityIngredient> MIN_DURABILITY =
        REGISTRYLIB.ingredientType(
                "min_durability",
                MinDurabilityIngredient.CODEC,
                MinDurabilityIngredient.STREAM_CODEC);
```

:::important
Make sure `getType()` in your ingredient class returns the registered type from the entry:
```java
@Override
public IngredientType<?> getType() {
    return MY_ENTRY.get();
}
```
:::

## Step 3: Use in Recipes

Custom ingredients are used exactly like any other ingredient. Call `toVanilla()` or use your convenience factory:

```java
// Direct usage
new MinDurabilityIngredient(ItemTags.SWORDS, 200).toVanilla()

// Via convenience factory
MinDurabilityIngredient.of(ItemTags.SWORDS, 200)
```

In recipe registration:

```java
INFUSER.addRecipe("infuser_durable_swords_to_diamond",
        new InfuserRecipe(
                MinDurabilityIngredient.of(ItemTags.SWORDS, 200),
                new ItemStackTemplate(Items.DIAMOND), 80, 20.0F, 2));
```

## Generated JSON

The custom ingredient serializes with a `neoforge:ingredient_type` discriminator:

```json
{
  "ingredient": {
    "neoforge:ingredient_type": "registrylibtest:min_durability",
    "tag": "minecraft:swords",
    "min_durability": 200
  },
  "result": { "id": "minecraft:diamond" },
  ...
}
```

:::note
Any recipe that uses `Ingredient.CODEC` in its serializer automatically supports custom ingredients — the codec dispatches based on `"neoforge:ingredient_type"` in JSON.
:::

## API Reference

### RegistryCore

| Method | Purpose |
|---|---|
| `.ingredientType(name, codec)` | Register with auto-derived StreamCodec |
| `.ingredientType(name, codec, streamCodec)` | Register with explicit StreamCodec |

### IngredientTypeEntry

| Method | Purpose |
|---|---|
| `.get()` | Get the registered `IngredientType<T>` |
| `.getEntry()` | Get the underlying `RegistryEntry` |

### ICustomIngredient Contract

| Method | Description |
|---|---|
| `test(ItemStack)` | Core matching logic |
| `items()` | Stream of accepted items (for display) |
| `isSimple()` | `true` = item-only matching, `false` = inspects components |
| `getType()` | Return your registered `IngredientType` |
| `display()` | (Optional) Custom `SlotDisplay` for rendering |
| `toVanilla()` | Convert to vanilla `Ingredient` (inherited, don't override) |
| `equals()` / `hashCode()` | **Must implement** |

## See Also

- [How-To: Register Recipes](/how-to/register-recipes) — recipe registration and built-in ingredient types
- [NeoForge Ingredients Documentation](https://docs.neoforged.net/docs/resources/server/recipes/ingredients)
