---
sidebar_position: 100
title: FAQ
description: Frequently asked questions about RegistryLib.
---

# Frequently Asked Questions

## General

### What Minecraft / NeoForge versions does RegistryLib support?

RegistryLib targets **NeoForge 26.1** on **Minecraft 1.21+**. Check the [GitHub releases](https://github.com/GregTech-Odyssey/RegistryLib/releases) page for the exact version matrix.

### Is RegistryLib a fork of Registrate?

RegistryLib is **inspired by** [Registrate](https://github.com/tterrag1098/Registrate) but is a clean-room rebuild for modern NeoForge. It shares the fluent-API philosophy but differs in implementation, type hierarchy, and feature set. See [What is RegistryLib?](/concepts/what-is-registrylib) for a detailed comparison.

### Can I use RegistryLib alongside vanilla registration?

Yes. RegistryLib does not replace the NeoForge registry system —it wraps it. You can mix RegistryLib chains with `DeferredRegister` calls in the same mod without conflict.

---

## Setup

### Gradle cannot resolve the RegistryLib dependency

1. Ensure your `settings.gradle` (or `build.gradle`) includes the Gtodyssey Maven repository: `https://maven.gtodyssey.com/releases`.
2. Check your network can reach `maven.gtodyssey.com`.
3. Run `./gradlew build --refresh-dependencies` to clear cached resolution failures.

See [Installation & Setup](/tutorials/installation) for the full walkthrough.

### `runData` or `runClient` fails at startup

- Confirm your NeoForge and Minecraft versions align with the RegistryLib release you are using.
- Make sure the class that initializes `RegistryCore.create(MOD_ID)` is loaded during mod construction (e.g., referenced from your `@Mod` class).

---

## Registration

### I called `.register()` but the item does not appear in-game

1. **Class not loaded** —the field holding your `ItemEntry` must be in a class that is referenced during mod construction. A static field in an unreferenced class is never initialized.
2. **Missing creative tab** —call `.addDefaultTab()` or `.addTab(...)` to place it in a creative tab.
3. **Missing model/texture** —call `.defaultModel()`, or provide a texture via `.texture(...)` or a resource pack.

### What is the difference between `item()` and `componentItem()`?

`item()` creates a plain `Item`. `componentItem()` creates a `ComponentItem` (or `IComponentItem` implementation) that supports the **attachment** system for modular behavior composition. Use `componentItem()` when you need `.attach(...)`.

### Can I register entries conditionally?

RegistryLib builders are submitted at `.register()` time. You can conditionally skip chains with normal Java control flow. However, once `.register()` is called, the entry is registered unconditionally —there is no built-in config-gated registration.

---

## Datagen

### My language file is missing entries

- Ensure `.lang("Display Name")` is present on each chain, or use `.defaultLang()`.
- Run `./gradlew runData` after any lang change —RegistryLib generates lang files during datagen, not at runtime.

### How do I add recipes?

Use `addDataGenerator(ProviderType.RECIPE, ...)` on your builder chain. See [Recipes and Tags](/tutorials/recipes-tags).

---

## Still stuck?

Check the [Troubleshooting](/troubleshooting) page for error-specific solutions, or open an issue on [GitHub](https://github.com/GregTech-Odyssey/RegistryLib/issues).
