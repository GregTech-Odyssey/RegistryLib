---
title: Registering Advancements
parent: Content Guides
nav_order: 5
permalink: /register-advancements/
---

# Registering Advancements

## What This Page Solves

When you want Advancement definitions to follow the same organizational style as the rest of your RegistryLib-based content, RegistryLib lets you hook them into the same datagen pipeline. This page restores the practical parts that matter in real use: how to register them, how titles and descriptions are localized, how root and child Advancements relate, and how to organize multiple tabs cleanly.

## When This Applies

- You are already using RegistryLib for other content types.
- You want Advancement datagen and naming conventions to stay aligned with the rest of the project.
- You need a complete registration example, not just a reminder that Minecraft already has an Advancement API.

## Quick Start

```java
RegistryLibTest.REGISTRYLIB.addDataGenerator(
	ProviderType.ADVANCEMENT,
	adv -> {
	    String cat = RegistryLibTest.MOD_ID;

	    AdvancementHolder root = Advancement.Builder.advancement()
		    .display(
			    Items.CRAFTING_TABLE,
			    adv.title(cat, "simple/root", "Getting Started"),
			    adv.desc(cat, "simple/root", "Obtain a crafting table"),
			    Identifier.withDefaultNamespace(
				    "textures/gui/advancements/backgrounds/stone.png"),
			    AdvancementType.TASK,
			    false,
			    false,
			    false)
		    .addCriterion(
			    "has_crafting_table",
			    InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
		    .save(adv, Identifier.fromNamespaceAndPath(cat, "simple/root"));

	    Advancement.Builder.advancement()
		    .parent(root)
		    .display(
			    SimpleItemExample.COPPER_COIN.get(),
			    adv.title(cat, "simple/get_coin", "First Coin"),
			    adv.desc(cat, "simple/get_coin", "Pick up a Copper Coin"),
			    null,
			    AdvancementType.TASK,
			    true,
			    true,
			    false)
		    .addCriterion(
			    "has_coin",
			    InventoryChangeTrigger.TriggerInstance.hasItems(
				    SimpleItemExample.COPPER_COIN.get()))
		    .save(adv, Identifier.fromNamespaceAndPath(cat, "simple/get_coin"));
	});
```

This gives you one root Advancement and one child Advancement inside the datagen pipeline already managed by RegistryLib.

## Full Example

```java
RegistryLibTest.REGISTRYLIB.addDataGenerator(
	ProviderType.ADVANCEMENT,
	adv -> {
	    String cat = RegistryLibTest.MOD_ID;

	    AdvancementHolder root = Advancement.Builder.advancement()
		    .display(
			    SimpleItemExample.COPPER_COIN.get(),
			    adv.title(cat, "basics/root", "RegistryCore Basics"),
			    adv.desc(cat, "basics/root", "Getting started with RegistryCore"),
			    Identifier.withDefaultNamespace(
				    "textures/gui/advancements/backgrounds/stone.png"),
			    AdvancementType.TASK,
			    false,
			    false,
			    false)
		    .addCriterion(
			    "has_crafting_table",
			    InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
		    .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/root"));

	    AdvancementHolder getCoin = Advancement.Builder.advancement()
		    .parent(root)
		    .display(
			    SimpleItemExample.COPPER_COIN.get(),
			    adv.title(cat, "basics/get_coin", "First Coin"),
			    adv.desc(cat, "basics/get_coin", "Obtain a Copper Coin"),
			    null,
			    AdvancementType.TASK,
			    true,
			    true,
			    false)
		    .addCriterion(
			    "has_coin",
			    InventoryChangeTrigger.TriggerInstance.hasItems(
				    SimpleItemExample.COPPER_COIN.get()))
		    .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/get_coin"));

	    Advancement.Builder.advancement()
		    .parent(getCoin)
		    .display(
			    FullItemExample.MAGIC_WAND.get(),
			    adv.title(cat, "basics/get_magic_wand", "Arcane Discovery"),
			    adv.desc(cat, "basics/get_magic_wand", "Craft a Magic Wand"),
			    null,
			    AdvancementType.GOAL,
			    true,
			    true,
			    false)
		    .addCriterion(
			    "has_magic_wand",
			    InventoryChangeTrigger.TriggerInstance.hasItems(FullItemExample.MAGIC_WAND.get()))
		    .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/get_magic_wand"));

	    AdvancementHolder advRoot = Advancement.Builder.advancement()
		    .display(
			    FullBlockExample.MAGIC_ORE.get().asItem(),
			    adv.title(cat, "advanced/root", "Advanced Crafting"),
			    adv.desc(cat, "advanced/root", "Explore advanced features"),
			    Identifier.withDefaultNamespace(
				    "textures/gui/advancements/backgrounds/nether.png"),
			    AdvancementType.TASK,
			    false,
			    false,
			    false)
		    .addCriterion(
			    "has_iron",
			    InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
		    .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/root"));

	    Advancement.Builder.advancement()
		    .parent(advRoot)
		    .display(
			    FullBlockExample.MAGIC_ORE.get().asItem(),
			    adv.title(cat, "advanced/mine_magic_ore", "Magical Mining"),
			    adv.desc(cat, "advanced/mine_magic_ore", "Mine a block of Magic Ore"),
			    null,
			    AdvancementType.GOAL,
			    true,
			    true,
			    false)
		    .addCriterion(
			    "has_magic_ore",
			    InventoryChangeTrigger.TriggerInstance.hasItems(
				    FullBlockExample.MAGIC_ORE.get().asItem()))
		    .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/mine_magic_ore"));

	    Advancement.Builder.advancement()
		    .parent(advRoot)
		    .display(
			    FullBlockExample.TIMER_TIER_3.get().asItem(),
			    adv.title(cat, "advanced/build_timer", "Time Lord"),
			    adv.desc(cat, "advanced/build_timer", "Craft a Tier 3 Timer"),
			    null,
			    AdvancementType.CHALLENGE,
			    true,
			    true,
			    true)
		    .addCriterion(
			    "has_timer_3",
			    InventoryChangeTrigger.TriggerInstance.hasItems(
				    FullBlockExample.TIMER_TIER_3.get().asItem()))
		    .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/build_timer"));
	});
```

This shows the complete shape that had been missing from the page: two tabs, multiple depth levels, all three `AdvancementType` values, and a hidden challenge Advancement.

## Step-by-Step Explanation

1. `addDataGenerator(ProviderType.ADVANCEMENT, adv -> { ... })` hooks your Advancement tree into RegistryLib's datagen pipeline.
2. `Advancement.Builder.advancement()` is still the vanilla builder. RegistryLib does not replace it; it hosts it.
3. `adv.title(...)` and `adv.desc(...)` generate localized `Component` values and write the corresponding language keys during datagen.
4. `.display(...)` defines the icon, title, description, background, type, and visibility behavior.
5. `.addCriterion(...)` defines the actual unlock condition.
6. `.save(adv, id)` writes the Advancement JSON and returns an `AdvancementHolder` that child Advancements can reference through `.parent(...)`.

{: .important }
> The root Advancement in a tab is the one that should provide the background texture. Child Advancements usually pass `null` for the background.

## Core API Notes

### `addDataGenerator(ProviderType.ADVANCEMENT, Consumer)`

This is the RegistryLib entry point for Advancement generation.

```java
RegistryLibTest.REGISTRYLIB.addDataGenerator(
	ProviderType.ADVANCEMENT,
	adv -> { /* define the tree here */ });
```

Use this when the Advancement tree belongs to the project as generated data, not as hand-maintained external JSON.

### `adv.title(category, name, text)` and `adv.desc(category, name, text)`

These methods were part of the older page and still matter because they are the cleanest bridge between Advancement declarations and localization.

- Title key format: `advancements.<category>.<name>.title`
- Description key format: `advancements.<category>.<name>.description`

That lets you keep the language key structure aligned with the Advancement identifier path.

### `.display(...)`

```java
.display(
    icon,
    title,
    description,
    background,
    type,
    showToast,
    announceToChat,
    hidden)
```

- `background` is mainly for root Advancements.
- `type` controls visual style and completion emphasis.
- `hidden` keeps the Advancement invisible until unlocked.

### `.parent(holder)`

Use this to build the tree explicitly. Root Advancements do not need a parent; everything else should point to the previous structural step in the progression.

### `.save(adv, id)`

The `Identifier` decides both the JSON output path and, in practice, the grouping structure you are imposing on the tree. Keeping paths such as `basics/...` and `advanced/...` consistent is one of the easiest ways to keep multi-tab projects understandable.

## Design Suggestions

### Keep Hierarchy Explicit

If a group of Advancements belongs to the same progression, do not scatter them across unrelated classes. Organizing by functional area is easier to maintain.

The restored full example uses a clear split between `basics/...` and `advanced/...`, which is a good default pattern when your project has multiple progression themes.

### Do Not Force Complex Conditions into One Unreadable Chain

RegistryLib is good at organizing the entry point, naming, and output location. If the trigger condition itself is complex, extract it into named variables or helper methods.

### Choose `AdvancementType` Intentionally

| Type | Meaning | Typical use |
| --- | --- | --- |
| `TASK` | Normal progression step | Root steps and routine unlocks |
| `GOAL` | Higher-weight milestone | Important feature unlocks |
| `CHALLENGE` | Highest-emphasis milestone | Rare, hard, or prestige goals |

{: .note }
> This page is not intended to replace Minecraft or NeoForge documentation for the Advancement mechanism itself. It explains how that content should be hosted inside a RegistryLib-based project structure.

## Common Patterns

### One Root and a Few Direct Children

This is the right shape when you only need a small progression line and want the tab to stay visually simple.

### Multiple Tabs for Different Systems

Use separate root Advancements with different identifier prefixes and different backgrounds when gameplay themes genuinely diverge.

### Hidden Challenge Rewards

Use `AdvancementType.CHALLENGE` together with `hidden = true` when the Advancement should exist as a reveal or surprise rather than as visible checklist content.

## Common API Lookup

| Method or API | Purpose |
| --- | --- |
| `addDataGenerator(ProviderType.ADVANCEMENT, ...)` | Register Advancement datagen work |
| `adv.title(...)` | Generate localized title text |
| `adv.desc(...)` | Generate localized description text |
| `Advancement.Builder.advancement()` | Start a vanilla Advancement builder |
| `parent(holder)` | Attach a child Advancement to a parent |
| `display(...)` | Configure icon, type, background, and visibility |
| `addCriterion(...)` | Define unlock requirements |
| `save(adv, id)` | Write the Advancement and return its holder |

## When to Continue Elsewhere

- If you care about language generation and multi-locale output, continue with [Lang System]({{ '/lang-system/' | relative_url }}).
- If you care about repository conventions, release hygiene, and broader upkeep rules, continue with [Maintenance]({{ '/development-and-maintenance/' | relative_url }}).

## Related Links

- [Content Guides]({{ '/content-guides/' | relative_url }})
- [Lang System]({{ '/lang-system/' | relative_url }})
- [API Reference]({{ '/api-reference/' | relative_url }})
