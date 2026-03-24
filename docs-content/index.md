---
slug: /
sidebar_position: 0
title: RegistryLib Documentation
description: Fluent registration library for NeoForge —register items, blocks, fluids, and more in one builder chain.
---

# RegistryLib

**RegistryLib** is a fluent registration library for [NeoForge](https://neoforged.net/) that consolidates item, block, fluid, block entity, and datagen registration into a single builder chain.

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .defaultModel()
    .addDefaultTab()
    .register();
```

One chain. One place. Everything registered.

---

## Why RegistryLib?

| Without RegistryLib | With RegistryLib |
|---|---|
| Separate `DeferredRegister` calls for each registry | One `RegistryCore` entry point |
| Manual `DataProvider` registration for lang, models, loot, tags | Datagen integrated into the builder chain |
| Scattered boilerplate across multiple classes | Everything in one fluent chain |
| Easy to forget a step (missing model, lang, creative tab— | Builder guides you through all common configuration |

---

## Pick Your Path

### I'm new to RegistryLib

Start with the **beginner tutorials** —they'll get you from zero to a working item in 5 minutes:

1. [Installation & Setup](/tutorials/installation) —Add the dependency and create your `RegistryCore`
2. [Your First Item](/tutorials/first-item) —Register a simple item
3. [Your First Block](/tutorials/first-block) —Register a block with a BlockItem
4. [Understanding the Chain](/tutorials/understanding-chain) —How the fluent API works

### I know the basics, show me more

Jump into **intermediate tutorials** for real-world patterns:

- [Group System](/tutorials/group-system) —Share defaults across entries
- [Tooltip System](/tutorials/tooltip-system) —Multi-section tooltips
- [Multi-Language Support](/tutorials/multi-language) —Add extra locales
- [Recipes and Tags](/tutorials/recipes-tags) —Datagen integration

### I need to do something specific

**How-to guides** are task-focused references:

- [Register Items](/how-to/register-items) | [Register Blocks](/how-to/register-blocks) | [Register Fluids](/how-to/register-fluids)
- [Register Block Entities](/how-to/register-block-entities) | [Register Advancements](/how-to/register-advancements)

### I need API details

**Reference** pages provide lookup tables and method signatures:

- [API Overview](/reference/api-overview) —Entry points and builder families
- [Entry Types](/reference/entry-types) —What you get back from `.register()`
- [Builder Methods](/reference/builder-methods) —Complete method reference

---

## Project Info

| | |
|---|---|
| **Version** | 1.0.4 |
| **Minecraft** | 26.1+ (NeoForge 26.1) |
| **Java** | 25 |
| **Source** | [RegistryLib](https://github.com/GregTech-Odyssey/RegistryLib) |
| **Organization** | [GregTech-Odyssey](https://github.com/GregTech-Odyssey) |
