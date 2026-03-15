---
title: Register Advancements
nav_order: 7
permalink: /register-advancements/
---

# Register Advancements

RegistryLib hooks advancement trees into the data-generation pipeline via `addDataGenerator(ProviderType.ADVANCEMENT, ...)`. You can define a complete advancement tree — including multiple tabs, multi-level parent/child relationships, and localised titles/descriptions — inside a single Consumer.

---

## Simple Example

The simplest advancement registration: one root advancement and one child.

```java
static void register() {
    RegistryLibTest.REGISTRYLIB.addDataGenerator(
            ProviderType.ADVANCEMENT,
            adv -> {
                String cat = RegistryLibTest.MOD_ID;

                // root advancement
                AdvancementHolder root = Advancement.Builder.advancement()
                        .display(
                                Items.CRAFTING_TABLE,
                                adv.title(cat, "simple/root", "Getting Started"),
                                adv.desc(cat, "simple/root", "Obtain a crafting table"),
                                Identifier.withDefaultNamespace(
                                        "textures/gui/advancements/backgrounds/stone.png"),
                                AdvancementType.TASK,
                                false, false, false)
                        .addCriterion(
                                "has_crafting_table",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        Items.CRAFTING_TABLE))
                        .save(adv, Identifier.fromNamespaceAndPath(cat, "simple/root"));

                // child advancement
                Advancement.Builder.advancement()
                        .parent(root)
                        .display(
                                SimpleItemExample.COPPER_COIN.get(),
                                adv.title(cat, "simple/get_coin", "First Coin"),
                                adv.desc(cat, "simple/get_coin", "Pick up a Copper Coin"),
                                null,
                                AdvancementType.TASK,
                                true, true, false)
                        .addCriterion(
                                "has_coin",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        SimpleItemExample.COPPER_COIN.get()))
                        .save(adv, Identifier.fromNamespaceAndPath(cat, "simple/get_coin"));
            });
}
```

---

## Full Example

A complete example with multiple tabs, multiple types (TASK / GOAL / CHALLENGE), and hidden advancements.

```java
static void register() {
    RegistryLibTest.REGISTRYLIB.addDataGenerator(
            ProviderType.ADVANCEMENT,
            adv -> {
                String cat = RegistryLibTest.MOD_ID;

                // ── Tab 1: Basics ──
                AdvancementHolder root = Advancement.Builder.advancement()
                        .display(
                                SimpleItemExample.COPPER_COIN.get(),
                                adv.title(cat, "basics/root", "RegistryCore Basics"),
                                adv.desc(cat, "basics/root", "Getting started"),
                                Identifier.withDefaultNamespace(
                                        "textures/gui/advancements/backgrounds/stone.png"),
                                AdvancementType.TASK,
                                false, false, false)
                        .addCriterion("has_crafting_table",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        Items.CRAFTING_TABLE))
                        .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/root"));

                // GOAL type
                Advancement.Builder.advancement()
                        .parent(root)
                        .display(
                                FullItemExample.MAGIC_WAND.get(),
                                adv.title(cat, "basics/get_magic_wand", "Arcane Discovery"),
                                adv.desc(cat, "basics/get_magic_wand", "Craft a Magic Wand"),
                                null,
                                AdvancementType.GOAL,
                                true, true, false)
                        .addCriterion("has_magic_wand",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        FullItemExample.MAGIC_WAND.get()))
                        .save(adv, Identifier.fromNamespaceAndPath(
                                cat, "basics/get_magic_wand"));

                // ── Tab 2: Advanced ──
                AdvancementHolder advRoot = Advancement.Builder.advancement()
                        .display(
                                FullBlockExample.MAGIC_ORE.get().asItem(),
                                adv.title(cat, "advanced/root", "Advanced Crafting"),
                                adv.desc(cat, "advanced/root", "Explore advanced features"),
                                Identifier.withDefaultNamespace(
                                        "textures/gui/advancements/backgrounds/nether.png"),
                                AdvancementType.TASK,
                                false, false, false)
                        .addCriterion("has_iron",
                                InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/root"));

                // CHALLENGE type (hidden)
                Advancement.Builder.advancement()
                        .parent(advRoot)
                        .display(
                                FullBlockExample.TIMER_TIER_3.get().asItem(),
                                adv.title(cat, "advanced/build_timer", "Time Lord"),
                                adv.desc(cat, "advanced/build_timer", "Craft a Tier 3 Timer"),
                                null,
                                AdvancementType.CHALLENGE,
                                true, true, true)    // hidden = true
                        .addCriterion("has_timer_3",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        FullBlockExample.TIMER_TIER_3.get().asItem()))
                        .save(adv, Identifier.fromNamespaceAndPath(
                                cat, "advanced/build_timer"));
            });
}
```

---

## API Reference

### `addDataGenerator(ProviderType.ADVANCEMENT, Consumer)`

Registers advancements into RegistryCore's data-generation pipeline. The Consumer receives a `RegistryLibAdvancementProvider`.

```java
RegistryLibTest.REGISTRYLIB.addDataGenerator(
        ProviderType.ADVANCEMENT,
        adv -> { /* define your advancement tree here */ });
```

Call this once in `RegistryLibTest`'s `static` initialiser block.

---

### `adv.title(String category, String name, String title)`

Generates a localised advancement title, writes it to the language file automatically, and returns a `MutableComponent`.

```java
adv.title(cat, "basics/root", "RegistryCore Basics")
```

Language key format: `advancements.<category>.<name>.title`

---

### `adv.desc(String category, String name, String desc)`

Generates a localised advancement description, writes it to the language file automatically, and returns a `MutableComponent`.

```java
adv.desc(cat, "basics/root", "Getting started with RegistryCore")
```

Language key format: `advancements.<category>.<name>.description`

---

### `Advancement.Builder.advancement()`

Creates an advancement builder using the vanilla Minecraft Advancement API.

```java
Advancement.Builder.advancement()
        .display(icon, title, desc, background, type, toast, announce, hidden)
        .addCriterion(name, trigger)
        .save(consumer, id);
```

---

### `AdvancementType`

The advancement type determines the border style and completion notification:

| Type | Description | Border |
| --- | --- | --- |
| `TASK` | Normal task | Square border |
| `GOAL` | Goal (harder) | Rounded border |
| `CHALLENGE` | Challenge (hardest) | Spiky border |

---

### `.display()` Parameters

```java
.display(
    icon,        // ItemStack or Item — icon
    title,       // Component — title
    desc,        // Component — description
    background,  // Identifier — background texture (root advancements only, pass null for children)
    type,        // AdvancementType — TASK / GOAL / CHALLENGE
    showToast,   // boolean — show toast on completion
    announceChat,// boolean — announce in chat on completion
    hidden       // boolean — hidden (not visible until completed)
)
```

---

### `.parent(AdvancementHolder)`

Sets the parent advancement, forming the progress tree hierarchy. Root advancements do not need a parent.

```java
Advancement.Builder.advancement()
        .parent(root)
        // ...
```

---

### `.save(Consumer<AdvancementHolder>, Identifier)`

Saves the advancement to the data generator and returns an `AdvancementHolder` for subsequent child advancements to reference.

```java
.save(adv, Identifier.fromNamespaceAndPath(cat, "basics/root"));
```

The `Identifier`'s path determines the location of the advancement JSON file and its tab grouping (advancements sharing a path prefix appear in the same tab).
