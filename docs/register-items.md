---
title: Registering Items
parent: Content Guides
nav_order: 1
permalink: /register-items/
---

# Registering Items

## What This Page Solves

When you need to register a normal Item, or you want to keep language, model, tooltip, recipe, tag, attachment, and related configuration in a single chain, this page is the most common and complete starting point in RegistryLib.

## When This Applies

- You want to register a normal `Item` or a `ComponentItem`.
- You want display name, model, creative tab, tooltip, recipe, and related behavior to live in one chain.
- You want later code to reference the result through `ItemEntry<T>`.

## Quick Start

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .lang("Copper Coin")
        .defaultModel()
        .register();
```

This chain declares an Item named `copper_coin`, provides its English display name, and generates the default item model resource.

## Full Example

```java
public static final ItemEntry<ComponentItem> MAGIC_WAND = RegistryLibTest.REGISTRYLIB
        .componentItem("magic_wand")
        .lang("Magic Wand")
        .defaultModel()
        .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .tooltip(Component.literal("A configurable tool"))
        .attach(new InspectAttachment())
        .register();
```

## Step-by-Step Explanation

1. `componentItem("magic_wand")` starts an `ItemBuilder` chain that is already prepared for attachments.
2. `.lang("Magic Wand")` provides the display name for datagen.
3. `.defaultModel()` requests the most common default item model generation.
4. `.tab(...)` controls which creative inventory tab the Item belongs to.
5. `.tooltip(...)` supplies tooltip content or node construction logic to the Tooltip System.
6. `.attach(...)` only applies to Item types that implement `IComponentItem`, including the built-in `ComponentItem` path created by `componentItem(...)`.
7. `.register()` submits the registration and returns `ItemEntry<ComponentItem>`.

{: .important }
> If you call `.attach(...)` on a normal `Item`, the problem is usually the selected type rather than the attachment itself. Attachments are designed for `ComponentItem` or another `IComponentItem` implementation, not for every Item uniformly.

## Choosing Between `item(...)` and `componentItem(...)`

- Use `item(...)` for ordinary Items that only need properties, language, model, tab, tags, recipes, or tooltip callbacks.
- Use `componentItem(...)` when the Item should own reusable `ItemAttachment` behaviors such as custom use logic, extra tooltip collection, or inventory tick behavior.

{: .note }
> `componentItem(...)` is the high-level entry point added for the attachment workflow. You can still provide your own factory with `componentItem("id", factory)` when your Item class already implements `IComponentItem`.

## Common Patterns

### I Only Want the Shortest Possible Item Registration

Keep only `item(...)`, `lang(...)`, and `register()`. Add `defaultModel()` only if your project wants the default generated model.

### I Need a Ready-to-Use Stack After Registration

`ItemEntry<T>` can be used directly to create `ItemStack` or `ItemResource` values later through helpers such as `asStack()` and `asResource()`. That is usually cleaner than reconstructing stacks from the raw registered item every time.

### I Want Multiple Items to Share Defaults

When multiple Items share a creative tab, lang prefix, or property modifiers, do not repeat the same setup on every Item. Move the shared defaults into [Group System]({{ '/group-system/' | relative_url }}).

### I Need More Complex Tooltips

When tooltips start to include multiple sections, conditional visibility, or separate rendering areas, do not keep forcing all of the logic into one lambda. Move to [Tooltip System]({{ '/tooltip-system/' | relative_url }}) and structure it explicitly.

## Common API Lookup

| Method | Purpose |
| --- | --- |
| `item(name, factory)` | Create an `ItemBuilder` |
| `componentItem(name)` | Create a `ComponentItem`-backed `ItemBuilder` |
| `lang(text)` | Set the display name |
| `defaultModel()` | Generate the default item model |
| `tab(tab)` | Set the creative tab |
| `tooltip(...)` | Add tooltip nodes |
| `attach(...)` | Add an attachment |
| `register()` | Complete registration |

## Related Links

- [5-Minute Quickstart]({{ '/quickstart/' | relative_url }})
- [Tooltip System]({{ '/tooltip-system/' | relative_url }})
- [Group System]({{ '/group-system/' | relative_url }})