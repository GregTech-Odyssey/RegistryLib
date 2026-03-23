---
sidebar_position: 102
title: Glossary
description: Key terms used throughout RegistryLib documentation.
---

# Glossary

| Term | Definition |
|------|-----------|
| **Attachment** | A modular behavior unit (`ItemAttachment`) that can be composed onto a `ComponentItem` via `.attach(...)`. |
| **BlockEntry** | A typed wrapper around `DeferredHolder<Block, T>` returned by `.register()` on a block chain. |
| **BlockItem** | The `Item` form of a `Block`. Created explicitly via `.simpleItem()` or `.item(...)` — not automatic. |
| **Builder** | A fluent configuration object (e.g., `ItemBuilder`, `BlockBuilder`) that collects settings and submits them at `.register()`. |
| **BuilderCallback** | The internal mechanism that receives a completed builder and submits its content to the NeoForge registry and datagen providers. |
| **Chain** | A sequence of fluent method calls starting from an entry point (e.g., `.item(...)`) and ending with `.register()`. |
| **ComponentItem** | An `Item` subclass that implements `IComponentItem`, supporting the attachment composition system. |
| **Datagen** | Data generation — the `runData` Gradle task that produces JSON assets (models, lang, loot, recipes, tags, advancements) from code. |
| **Entry** | A typed wrapper (e.g., `ItemEntry`, `FluidEntry`) around a `DeferredHolder` that provides convenience accessors. |
| **FluidEntry** | A typed wrapper for fluids that exposes `.getSource()`, `.getType()`, `.getBlock()`, `.getBucket()`, etc. |
| **Group** | A shared-defaults layer around `RegistryCore` that applies common properties, tags, and tabs to all entries registered through it. |
| **ItemEntry** | A typed wrapper around `DeferredHolder<Item, T>` with helpers like `.asStack()` and `.readOnlyStack()`. |
| **Lang** | Language / localization. RegistryLib generates `en_us.json` (and optional extra locales) during datagen. |
| **NeoForge** | The Minecraft modding framework that RegistryLib targets (successor to MinecraftForge for 1.21+). |
| **ProviderType** | A key identifying a datagen provider (e.g., `ProviderType.LANG`, `ProviderType.RECIPE`, `ProviderType.LOOT`). |
| **RegistryCore** | The main entry point for RegistryLib. Created via `RegistryCore.create(MOD_ID)`. Provides all builder factory methods. |
| **RootNode / RootNodeRef** | Tooltip layout containers. A `RootNode` groups `SubNode` entries; `RootNodeRef` is a reference handle for a named root. |
| **Sub-entry** | An entry created within another builder's chain (e.g., `.item(...)` inside a `BlockBuilder` creates a sub-entry `ItemBuilder`). |
| **SubNode** | The smallest tooltip rendering unit — a line of text, icon, progress bar, or custom element. |
| **TooltipNodeCollector** | The collector that merges tooltip nodes from multiple sources (builder, attachments, BlockItem) during rendering. |
