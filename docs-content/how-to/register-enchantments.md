---
sidebar_position: 7
title: Register Enchantments
description: Quick reference for enchantment registration patterns.
---

# Register Enchantments

Enchantments in NeoForge 1.21+ are **data-driven**. RegistryLib provides an `.enchantment()` builder that generates the enchantment JSON, lang entries, and tag entries — all from code, no hand-written JSON needed.

## Simple Enchantment (Vanilla Effects)

Use vanilla effect components (e.g. `minecraft:block_experience`) — no custom `DataComponentType` registration needed.

```java
public static final EnchantmentEntry ORE_FORTUNE = REGISTRYLIB
        .enchantment("ore_fortune")
        .lang("Ore Fortune")
        .lang(LANG_ZH_CN, "矿石财运")
        .supportedItems("#minecraft:enchantable/mining")
        .weight(5)
        .maxLevel(3)
        .minCost(15, 9)
        .maxCost(65, 9)
        .anvilCost(4)
        .slots("mainhand")
        .addTag(TagKey.create(Registries.ENCHANTMENT,
                Identifier.fromNamespaceAndPath("minecraft", "in_enchanting_table")))
        .vanillaEffect("minecraft:block_experience", effect -> effect
                .add("type", "minecraft:add")
                .add("value", value -> value
                        .add("type", "minecraft:linear")
                        .add("base", 1.0)
                        .add("per_level_above_first", 1.0)))
        .register();
```

This single declaration:
1. Generates `data/<modid>/enchantment/ore_fortune.json` during datagen
2. Registers English and Chinese lang entries
3. Adds the enchantment to `minecraft:in_enchanting_table` tag
4. Returns an `EnchantmentEntry` with `.getKey()` for code references

## Full Enchantment (Custom Effect Component)

For custom behavior (e.g. auto-smelting), first register a `DataComponentType`, then use `.customEffect()` in the builder.

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

### 3. Register with the Enchantment Builder

```java
public static final EnchantmentEntry AUTO_SMELT = REGISTRYLIB
        .enchantment("auto_smelt")
        .lang("Auto Smelt")
        .lang(LANG_ZH_CN, "自动熔炼")
        .supportedItems("#minecraft:enchantable/mining")
        .weight(2)
        .maxLevel(1)
        .minCost(25, 25)
        .maxCost(75, 25)
        .anvilCost(8)
        .slots("mainhand")
        .addTag(TagKey.create(Registries.ENCHANTMENT,
                Identifier.fromNamespaceAndPath("minecraft", "in_enchanting_table")))
        .customEffect("registrylibtest:auto_smelt", effect -> effect
                .add("chance_per_level", 1.0f))
        .register();
```

## EnchantmentBuilder API

| Method | Purpose |
|---|---|
| `enchantment(name)` | Start enchantment builder (returns `EnchantmentBuilder`) |
| `.lang(name)` | Set English display name |
| `.lang(providerType, name)` | Set localized name for a specific lang provider |
| `.supportedItems(tagRef)` | Set supported item tag (e.g. `"#minecraft:enchantable/mining"`) |
| `.supportedItems(tagKey)` | Set supported item tag via `TagKey<Item>` |
| `.primaryItems(tagRef)` | Set primary items (enchanting table preference) |
| `.weight(n)` | Set enchantment weight (rarity) |
| `.maxLevel(n)` | Set maximum enchantment level |
| `.minCost(base, perLevel)` | Set minimum enchanting cost |
| `.maxCost(base, perLevel)` | Set maximum enchanting cost |
| `.anvilCost(n)` | Set anvil cost |
| `.slots(groups...)` | Set equipment slots (`"mainhand"`, `"armor"`, `"any"`) |
| `.exclusiveWith(keys...)` | Set mutually exclusive enchantments |
| `.vanillaEffect(id, builder)` | Add a vanilla effect via JSON builder |
| `.customEffect(id, builder)` | Add a custom mod effect via JSON builder |
| `.customEffect(id, jsonObj)` | Add a custom mod effect via raw `JsonObject` |
| `.addTag(tags...)` | Add to enchantment tags |
| `.register()` | Register and return `EnchantmentEntry` |
| `.build()` | Register and return parent (for chaining) |

## Enchantment JSON Fields (Generated)

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
Enchantments are data-driven in NeoForge 1.21+. The `.enchantment()` builder generates the JSON automatically during datagen — you do **not** need to write JSON files manually.
:::

## See Also

- [How-To: Register Items](/how-to/register-items)
- [Reference: API Overview](/reference/api-overview)
