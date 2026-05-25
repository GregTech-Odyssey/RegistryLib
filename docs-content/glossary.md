---
sidebar_position: 102
title: Glossary
description: Key terms used throughout RegistryLib documentation.
---

# Glossary

| Term | Definition |
|------|-----------|
| **AbstractHolderEntry** | The abstract base class for entry types that are backed by a NeoForge `Holder`. `ItemEntry`, `BlockEntry`, and `FluidEntry` all extend it (directly or via `ItemProviderEntry`). It provides `get()` with a descriptive error, `isBound()`, and `Holder` delegation. |
| **Attachment** | A modular behavior unit (`ItemAttachment`) that can be composed onto a `ComponentItem` via `.attach(...)`. |
| **BlockEntry** | A typed wrapper around a registered `Block` returned by `.register()` on a block chain. Extends `AbstractHolderEntry` via `ItemProviderEntry`. |
| **BlockItem** | The `Item` form of a `Block`. Created explicitly via `.simpleItem()` or `.item(...)`; not automatic. |
| **Builder** | A fluent configuration object, such as `ItemBuilder` or `BlockBuilder`, that collects settings and submits them at `.register()`. |
| **Chain** | A sequence of fluent method calls starting from an entry point, such as `.item(...)`, and ending with `.register()`. |
| **ComponentItem** | An `Item` subclass that implements `IComponentItem`, supporting the attachment composition system. |
| **Datagen** | Data generation: the `runData` Gradle task that produces JSON assets from code. |
| **Entry** | A typed wrapper, such as `ItemEntry` or `FluidEntry`, that provides convenience accessors around a registered object. All concrete entries extend `AbstractHolderEntry`. |
| **Existing entry** | A wrapper created by `existingItem(...)` or `existingBlock(...)` for vanilla or third-party objects that RegistryLib did not register. |
| **FluidEntry** | A typed wrapper for fluids that exposes `.getSource()`, `.getType()`, `.getBlock()`, `.getBucket()`, and related accessors. Extends `AbstractHolderEntry` directly. |
| **FreezableRegistry** | A generic key-value registry (`FreezableRegistry<K, V>`) that supports a one-way freeze transition. After `freeze()` is called, `register()` throws and the internal map becomes an unmodifiable snapshot. Used internally for tooltip registries, state registries, and other lookup tables. |
| **getOptional()** | A safe accessor on `RegistryEntry` that returns `Optional<T>` instead of throwing when the entry is not yet bound. Prefer this or `isBound()` when you are unsure whether registration has completed. |
| **Group** | A shared-defaults layer around `RegistryCore` that applies common properties, tags, and tabs to entries registered through it. |
| **isBound()** | A method on `RegistryEntry` (and overridden in `AbstractHolderEntry`) that returns `true` once the entry has been populated by the registry event. Use it to guard `.get()` calls in contexts where registration may not have completed. |
| **ItemEntry** | A typed wrapper around an `Item` with helpers like `.asStack()` and `.readOnlyStack()`. Extends `AbstractHolderEntry` via `ItemProviderEntry`. |
| **Lang** | Language / localization. RegistryLib generates `en_us.json` and optional extra locale files during datagen. |
| **NeoForge** | The Minecraft modding framework that RegistryLib targets. |
| **ProviderType** | A key identifying a datagen provider or generator family, such as `ProviderType.LANG`, `ProviderType.RECIPE`, or `ProviderType.LOOT`. |
| **RegistryCore** | The main entry point for RegistryLib. Created via `RegistryCore.create(MOD_ID)`. Provides builder factory methods and core datagen helpers. |
| **RootNode / RootNodeRef** | Tooltip layout containers. A `RootNode` groups `SubNode` entries; `RootNodeRef` is a reference handle for a named root. |
| **Sub-entry** | An entry created within another builder chain, such as `.item(...)` inside a `BlockBuilder`. |
| **SubNode** | The smallest tooltip rendering unit: a line of text, icon, progress bar, or custom element. |
| **TooltipNodeCollector** | The collector that merges tooltip nodes from multiple sources during rendering. |
