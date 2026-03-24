---
sidebar_position: 8
title: Register Enchantments
description: Quick reference for enchantment registration patterns.
---

# Register Enchantments

Enchantments in NeoForge 1.21+ are **data-driven**. RegistryLib provides an `.enchantment()` builder that generates the enchantment JSON, lang entries, and tag entries — all from code, no hand-written JSON needed. The builder uses the Minecraft `Enchantment.Builder` API with typed `DataComponentType` references.

## Simple Enchantment (Vanilla Effects)

Use vanilla effect components (e.g. `EnchantmentEffectComponents.BLOCK_EXPERIENCE`) — no custom `DataComponentType` registration needed.

```java
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;

public static final EnchantmentEntry ORE_FORTUNE = REGISTRYLIB
        .enchantment("ore_fortune")
        .lang("Ore Fortune")
        .lang(LANG_ZH_CN, "矿石财运")
        .supportedItems(ItemTags.MINING_ENCHANTABLE)
        .weight(5)
        .maxLevel(3)
        .minCost(15, 9)
        .maxCost(65, 9)
        .anvilCost(4)
        .slots(EquipmentSlotGroup.MAINHAND)
        .addTag(EnchantmentTags.IN_ENCHANTING_TABLE)
        .withEffect(EnchantmentEffectComponents.BLOCK_EXPERIENCE,
                new AddValue(LevelBasedValue.perLevel(1.0f, 1.0f)))
        .register();
```

This single declaration:
1. Generates `data/<modid>/enchantment/ore_fortune.json` during datagen
2. Registers English and Chinese lang entries
3. Adds the enchantment to `minecraft:in_enchanting_table` tag
4. Returns an `EnchantmentEntry` with `.getKey()` for code references

## Full Enchantment (Custom Effect Component)

For custom behavior (e.g. auto-smelting), first register a `DataComponentType`, then use `.withEffect()` in the builder.

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

Use the `Supplier` overload of `withEffect()` for mod-registered `DataComponentType` entries, since the `RegistryEntry` value may not yet be available at class loading time:

```java
public static final EnchantmentEntry AUTO_SMELT = REGISTRYLIB
        .enchantment("auto_smelt")
        .lang("Auto Smelt")
        .lang(LANG_ZH_CN, "自动熔炼")
        .supportedItems(ItemTags.MINING_ENCHANTABLE)
        .weight(2)
        .maxLevel(1)
        .minCost(25, 25)
        .maxCost(75, 25)
        .anvilCost(8)
        .slots(EquipmentSlotGroup.MAINHAND)
        .addTag(EnchantmentTags.IN_ENCHANTING_TABLE)
        .withEffect(AUTO_SMELT_EFFECT, new AutoSmeltEffect(1.0f))
        .register();
```

:::tip
`AUTO_SMELT_EFFECT` (a `RegistryEntry`) is passed directly — it implements `Supplier`, so the `DataComponentType` is resolved lazily at datagen time when the value is guaranteed to be available. For vanilla effect components like `EnchantmentEffectComponents.BLOCK_EXPERIENCE`, you can pass them directly since they are static constants.
:::

## EnchantmentBuilder API

| Method | Purpose |
|---|---|
| `enchantment(name)` | Start enchantment builder (returns `EnchantmentBuilder`) |
| `.lang(name)` | Set English display name |
| `.lang(providerType, name)` | Set localized name for a specific lang provider |
| `.supportedItems(TagKey<Item>)` | Set supported item tag (e.g. `ItemTags.MINING_ENCHANTABLE`) |
| `.primaryItems(TagKey<Item>)` | Set primary items (enchanting table preference) |
| `.weight(n)` | Set enchantment weight (rarity) |
| `.maxLevel(n)` | Set maximum enchantment level |
| `.minCost(base, perLevel)` | Set minimum enchanting cost |
| `.maxCost(base, perLevel)` | Set maximum enchanting cost |
| `.anvilCost(n)` | Set anvil cost |
| `.slots(EquipmentSlotGroup...)` | Set equipment slots (e.g. `EquipmentSlotGroup.MAINHAND`) |
| `.exclusiveWith(ResourceKey<Enchantment>...)` | Set mutually exclusive enchantments |
| `.withEffect(type, effect)` | Add a conditional effect (vanilla `DataComponentType`) |
| `.withEffect(supplier, effect)` | Add a conditional effect (lazy `Supplier<DataComponentType>`) |
| `.withEffect(type, effect, condition)` | Add a conditional effect with loot condition |
| `.withSpecialEffect(type, effect)` | Add a non-list effect component |
| `.configure(consumer)` | Direct access to `Enchantment.Builder` |
| `.addTag(TagKey<Enchantment>...)` | Add to enchantment tags (e.g. `EnchantmentTags.IN_ENCHANTING_TABLE`) |
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
