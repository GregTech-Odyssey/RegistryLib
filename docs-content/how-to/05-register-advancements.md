---
sidebar_position: 5
title: Register Advancements
description: Quick reference for advancement registration via datagen.
---

# Register Advancements

## Simple Advancement (Root + Child)

```java
REGISTRYLIB.addDataGenerator(
    ProviderType.ADVANCEMENT,
    adv -> {
        String cat = MOD_ID;

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                    Items.CRAFTING_TABLE,
                    adv.title(cat, "simple/root", "Getting Started"),
                    adv.desc(cat, "simple/root", "Obtain a crafting table"),
                    ResourceLocation.withDefaultNamespace(
                        "textures/gui/advancements/backgrounds/stone.png"),
                    AdvancementType.TASK,
                    false, false, false)
                .addCriterion(
                    "has_crafting_table",
                    InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
                .save(adv, ResourceLocation.fromNamespaceAndPath(cat, "simple/root"));

        Advancement.Builder.advancement()
                .parent(root)
                .display(
                    COPPER_COIN.get(),
                    adv.title(cat, "simple/get_coin", "First Coin"),
                    adv.desc(cat, "simple/get_coin", "Pick up a Copper Coin"),
                    null,
                    AdvancementType.TASK,
                    true, true, false)
                .addCriterion(
                    "has_coin",
                    InventoryChangeTrigger.TriggerInstance.hasItems(COPPER_COIN.get()))
                .save(adv, ResourceLocation.fromNamespaceAndPath(cat, "simple/get_coin"));
    });
```

## Full Multi-Tab Example

```java
REGISTRYLIB.addDataGenerator(
    ProviderType.ADVANCEMENT,
    adv -> {
        String cat = MOD_ID;

        // --- Tab 1: Basics ---
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                    COPPER_COIN.get(),
                    adv.title(cat, "basics/root", "RegistryCore Basics"),
                    adv.desc(cat, "basics/root", "Getting started with RegistryCore"),
                    ResourceLocation.withDefaultNamespace(
                        "textures/gui/advancements/backgrounds/stone.png"),
                    AdvancementType.TASK,
                    false, false, false)
                .addCriterion(
                    "has_crafting_table",
                    InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
                .save(adv, ResourceLocation.fromNamespaceAndPath(cat, "basics/root"));

        AdvancementHolder getCoin = Advancement.Builder.advancement()
                .parent(root)
                .display(
                    COPPER_COIN.get(),
                    adv.title(cat, "basics/get_coin", "First Coin"),
                    adv.desc(cat, "basics/get_coin", "Obtain a Copper Coin"),
                    null,
                    AdvancementType.TASK,
                    true, true, false)
                .addCriterion(
                    "has_coin",
                    InventoryChangeTrigger.TriggerInstance.hasItems(COPPER_COIN.get()))
                .save(adv, ResourceLocation.fromNamespaceAndPath(cat, "basics/get_coin"));

        Advancement.Builder.advancement()
                .parent(getCoin)
                .display(
                    MAGIC_WAND.get(),
                    adv.title(cat, "basics/get_magic_wand", "Arcane Discovery"),
                    adv.desc(cat, "basics/get_magic_wand", "Craft a Magic Wand"),
                    null,
                    AdvancementType.GOAL,
                    true, true, false)
                .addCriterion(
                    "has_magic_wand",
                    InventoryChangeTrigger.TriggerInstance.hasItems(MAGIC_WAND.get()))
                .save(adv, ResourceLocation.fromNamespaceAndPath(cat, "basics/get_magic_wand"));

        // --- Tab 2: Advanced ---
        AdvancementHolder advRoot = Advancement.Builder.advancement()
                .display(
                    MAGIC_ORE.get().asItem(),
                    adv.title(cat, "advanced/root", "Advanced Crafting"),
                    adv.desc(cat, "advanced/root", "Explore advanced features"),
                    ResourceLocation.withDefaultNamespace(
                        "textures/gui/advancements/backgrounds/nether.png"),
                    AdvancementType.TASK,
                    false, false, false)
                .addCriterion(
                    "has_iron",
                    InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                .save(adv, ResourceLocation.fromNamespaceAndPath(cat, "advanced/root"));

        Advancement.Builder.advancement()
                .parent(advRoot)
                .display(
                    TIMER_TIER_3.get().asItem(),
                    adv.title(cat, "advanced/build_timer", "Time Lord"),
                    adv.desc(cat, "advanced/build_timer", "Craft a Tier 3 Timer"),
                    null,
                    AdvancementType.CHALLENGE,
                    true, true, true)
                .addCriterion(
                    "has_timer_3",
                    InventoryChangeTrigger.TriggerInstance.hasItems(TIMER_TIER_3.get().asItem()))
                .save(adv, ResourceLocation.fromNamespaceAndPath(cat, "advanced/build_timer"));
    });
```

## Core API

### Entry Point

```java
REGISTRYLIB.addDataGenerator(ProviderType.ADVANCEMENT, adv -> { ... });
```

Hooks your advancement tree into RegistryLib's datagen pipeline.

### Localization Helpers

| Method | Generated Key Format |
|---|---|
| `adv.title(cat, name, text)` | `advancements.<cat>.<name>.title` |
| `adv.desc(cat, name, text)` | `advancements.<cat>.<name>.description` |

### Display Parameters

```java
.display(icon, title, description, background, type, showToast, announceToChat, hidden)
```

- `background` —only relevant for root advancements (pass `null` for children)
- `hidden` —keeps the advancement invisible until unlocked

### Tree Structure

- `.parent(holder)` —set the parent advancement
- `.save(adv, id)` —write the JSON and return an `AdvancementHolder` for children to reference

## AdvancementType

| Type | Use Case |
|---|---|
| `TASK` | Standard progression step |
| `GOAL` | Significant milestone |
| `CHALLENGE` | Difficult or hidden achievement |

## Common Patterns

**One root + children:** Every tab needs exactly one root advancement (the one with a background texture). All others chain via `.parent(...)`.

**Multiple tabs:** Each root with a distinct `background` texture creates a separate tab in the advancement screen. Use consistent path prefixes (`basics/...`, `advanced/...`) for organization.

**Hidden challenges:** Set the last three `.display(...)` booleans to `true, true, true` to create a hidden challenge that shows a toast and chat announcement on unlock.

:::important
The root advancement in each tab should provide the background texture. Child advancements pass `null` for the background parameter.
:::

## See Also

- [Tutorial: Multi-Language](/tutorials/multi-language)
- [Reference: API Overview](/reference/api-overview)
