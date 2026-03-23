---
sidebar_position: 1
title: What is RegistryLib?
description: Project positioning, goals, and comparison with vanilla registration.
---

# What is RegistryLib?

RegistryLib is a **fluent registration library** for NeoForge that lets you declare game objects — items, blocks, fluids, block entities, and more — using a single, readable builder chain instead of scattered boilerplate.

## The Problem

In vanilla NeoForge registration, adding a single block requires touching many disconnected places:

1. **DeferredRegister** — create the registry entry
2. **BlockStateProvider** — generate blockstate and model JSON
3. **LanguageProvider** — add the English name
4. **BlockLootSubProvider** — define loot tables
5. **TagsProvider** — add block/item tags
6. **Creative tab event** — add to creative inventory
7. Possibly a separate **ItemBlock** registration

These scattered calls are error-prone: forget one and you get an invisible block, a missing name, or an empty creative tab. As a mod grows, keeping all of these in sync becomes a maintenance burden.

## The Solution

RegistryLib consolidates everything into one builder chain:

```java
REGISTRYLIB.block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .defaultLoot()
        .simpleItem()
        .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
        .register();
```

One chain registers the block, generates its model, sets its display name, creates the loot table, adds the block item, and applies tags. All related concerns live together, so nothing gets forgotten.

## When to Use RegistryLib

| Scenario | Recommendation |
| --- | --- |
| Typical mod with many items/blocks | **Use RegistryLib** — saves significant boilerplate |
| Mod with only 1–2 simple items | Either works; RegistryLib still reduces file count |
| Library or API mod with no content | Vanilla registration is often sufficient |
| Heavily custom registry behavior | Start with RegistryLib, extend via [custom builders](/tutorials/expert/custom-builder) |
| Need fine-grained control over every datagen file | Vanilla providers give you full control; RegistryLib trades some flexibility for productivity |

## Lineage

RegistryLib is inspired by **Registrate** (tterrag's library for Forge), but rebuilt from the ground up for modern NeoForge. Key differences include:

- Native support for NeoForge's `DeferredHolder` and `RegisterEvent`
- Integrated datagen (models, lang, loot, tags) within the builder chain
- The [Group system](/how-to/register-items) for sharing defaults across entries
- Specialized entry types (`ItemEntry`, `FluidEntry`, etc.) with convenience helpers
- Support for the component item / attachment pattern

## See Also

- [Builder Pattern & Fluent API](/concepts/builder-pattern) — how the chain architecture works
- [Installation](/tutorials/beginner/installation) — get started in your project
- [API Overview](/reference/api-overview) — quick lookup of entry points and builders
