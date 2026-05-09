---
sidebar_position: 102
title: Glossary
description: Key terms used throughout RegistryLib documentation.
---

# Glossary

| Term | Definition |
|------|-----------|
| **Attachment** | A modular behavior unit (`ItemAttachment`) that can be composed onto a `ComponentItem` via `.attach(...)`. |
| **BlockEntry** | A typed wrapper around a registered `Block` returned by `.register()` on a block chain. |
| **BlockItem** | The `Item` form of a `Block`. Created explicitly via `.simpleItem()` or `.item(...)`; not automatic. |
| **Builder** | A fluent configuration object, such as `ItemBuilder` or `BlockBuilder`, that collects settings and submits them at `.register()`. |
| **Chain** | A sequence of fluent method calls starting from an entry point, such as `.item(...)`, and ending with `.register()`. |
| **ComponentItem** | An `Item` subclass that implements `IComponentItem`, supporting the attachment composition system. |
| **Datagen** | Data generation: the `runData` Gradle task that produces JSON assets from code. |
| **Entry** | A typed wrapper, such as `ItemEntry` or `FluidEntry`, that provides convenience accessors around a registered object. |
| **Existing entry** | A wrapper created by `existingItem(...)` or `existingBlock(...)` for vanilla or third-party objects that RegistryLib did not register. |
| **FluidEntry** | A typed wrapper for fluids that exposes `.getSource()`, `.getType()`, `.getBlock()`, `.getBucket()`, and related accessors. |
| **Group** | A shared-defaults layer around `RegistryCore` that applies common properties, tags, and tabs to entries registered through it. |
| **ItemEntry** | A typed wrapper around an `Item` with helpers like `.asStack()` and `.readOnlyStack()`. |
| **Lang** | Language / localization. RegistryLib generates `en_us.json` and optional extra locale files during datagen. |
| **NeoForge** | The Minecraft modding framework that RegistryLib targets. |
| **ProviderType** | A key identifying a datagen provider or generator family, such as `ProviderType.LANG`, `ProviderType.RECIPE`, or `ProviderType.LOOT`. |
| **RegistryCore** | The main entry point for RegistryLib. Created via `RegistryCore.create(MOD_ID)`. Provides builder factory methods and core datagen helpers. |
| **RootNode / RootNodeRef** | Tooltip layout containers. A `RootNode` groups `SubNode` entries; `RootNodeRef` is a reference handle for a named root. |
| **Sub-entry** | An entry created within another builder chain, such as `.item(...)` inside a `BlockBuilder`. |
| **SubNode** | The smallest tooltip rendering unit: a line of text, icon, progress bar, or custom element. |
| **TooltipNodeCollector** | The collector that merges tooltip nodes from multiple sources during rendering. |
