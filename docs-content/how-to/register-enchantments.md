---
sidebar_position: 7
title: Register Enchantments
description: Quick reference for enchantment registration patterns.
---

# Register Enchantments

Enchantments in NeoForge 1.21+ are **data-driven**. The enchantment definition lives in a JSON file; code only needs to define `ResourceKey` references and optionally register custom effect component types.

## Simple Enchantment (Vanilla Effects)

Use vanilla effect components (e.g. `minecraft:block_experience`) — no code registration needed beyond a `ResourceKey` and lang entries.

### 1. Define the ResourceKey

```java
public static final ResourceKey<Enchantment> ORE_FORTUNE = ResourceKey.create(
        Registries.ENCHANTMENT,
        Identifier.fromNamespaceAndPath(MOD_ID, "ore_fortune"));
```

### 2. Register Lang Entries

```java
// English
REGISTRYLIB.addLang("enchantment",
        Identifier.fromNamespaceAndPath(MOD_ID, "ore_fortune"),
        "Ore Fortune");

// Chinese (via LANG_ZH_CN provider)
REGISTRYLIB.addDataGenerator(LANG_ZH_CN,
        prov -> prov.add("enchantment.registrylibtest.ore_fortune", "矿石财运"));
```

### 3. Add Enchantment JSON

```json title="data/registrylibtest/enchantment/ore_fortune.json"
{
  "description": { "translate": "enchantment.registrylibtest.ore_fortune" },
  "supported_items": "#minecraft:enchantable/mining",
  "weight": 5,
  "max_level": 3,
  "min_cost": { "base": 15, "per_level_above_first": 9 },
  "max_cost": { "base": 65, "per_level_above_first": 9 },
  "anvil_cost": 4,
  "slots": ["mainhand"],
  "effects": {
    "minecraft:block_experience": [{
      "effect": {
        "type": "minecraft:add",
        "value": { "type": "minecraft:linear", "base": 1.0, "per_level_above_first": 1.0 }
      }
    }]
  }
}
```

### 4. Add Enchantment Tag

To make the enchantment obtainable via enchanting table:

```json title="data/minecraft/tags/enchantment/in_enchanting_table.json"
{
  "replace": false,
  "values": ["registrylibtest:ore_fortune"]
}
```

## Full Enchantment (Custom Effect Component)

For custom behavior (e.g. auto-smelting), register a custom `DataComponentType` for the enchantment effect.

### 1. Define the Effect Record

```java
public record AutoSmeltEffect(float chancePerLevel) {
    public static final MapCodec<AutoSmeltEffect> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    Codec.FLOAT.optionalFieldOf("chance_per_level", 1.0F)
                            .forGetter(AutoSmeltEffect::chancePerLevel))
                    .apply(inst, AutoSmeltEffect::new));
}
```

### 2. Register the DataComponentType

```java
@SuppressWarnings("unchecked")
public static final RegistryEntry<DataComponentType<?>,
        DataComponentType<List<ConditionalEffect<AutoSmeltEffect>>>>
    AUTO_SMELT_EFFECT = (RegistryEntry) REGISTRYLIB.simple(
            "auto_smelt",
            Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
            key -> DataComponentType
                    .<List<ConditionalEffect<AutoSmeltEffect>>>builder()
                    .persistent(ConditionalEffect.codec(
                            AutoSmeltEffect.CODEC.codec()).listOf())
                    .build());
```

### 3. Reference in Enchantment JSON

```json title="data/registrylibtest/enchantment/auto_smelt.json"
{
  "description": { "translate": "enchantment.registrylibtest.auto_smelt" },
  "supported_items": "#minecraft:enchantable/mining",
  "weight": 2,
  "max_level": 1,
  "min_cost": { "base": 25, "per_level_above_first": 25 },
  "max_cost": { "base": 75, "per_level_above_first": 25 },
  "anvil_cost": 8,
  "slots": ["mainhand"],
  "effects": {
    "registrylibtest:auto_smelt": [{
      "effect": { "chance_per_level": 1.0 }
    }]
  }
}
```

## Common API Lookup

| Method | Purpose |
|---|---|
| `ResourceKey.create(Registries.ENCHANTMENT, id)` | Create a reference key for the enchantment |
| `REGISTRYLIB.simple(name, ENCHANTMENT_EFFECT_COMPONENT_TYPE, ...)` | Register a custom effect component type |
| `REGISTRYLIB.addLang("enchantment", id, name)` | Add English lang entry |
| `REGISTRYLIB.addDataGenerator(LANG_ZH_CN, ...)` | Add localized lang entry |

## Enchantment JSON Fields

| Field | Description |
|---|---|
| `description` | Translatable text component for the name |
| `supported_items` | Item tag that can carry this enchantment |
| `weight` | Rarity weight (higher = more common) |
| `max_level` | Maximum enchantment level |
| `min_cost` / `max_cost` | Enchanting table cost range |
| `anvil_cost` | Anvil cost in levels |
| `slots` | Equipment slots (`mainhand`, `offhand`, `head`, etc.) |
| `effects` | Map of effect component type → effect definition list |

:::important
Enchantments are data-driven in NeoForge 1.21+. You do **not** register an `Enchantment` object in code. The JSON in `data/<modid>/enchantment/` is loaded automatically by the data-driven registry system.
:::

## See Also

- [How-To: Register Items](/how-to/register-items)
- [Reference: API Overview](/reference/api-overview)
