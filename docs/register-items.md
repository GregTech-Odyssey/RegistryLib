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

{: .note }
> The code snippets on this page are excerpted from `SimpleItemExample` and `FullItemExample` in `RegistryLibTest`. They match the current test sources that pass `runData`.

## Quick Start

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .langCn("铜币")
        .lang("Copper Coin")
        .register();
```

This is the exact simple registration flow used in `SimpleItemExample`: one item, one English name, and one extra Chinese locale entry through the custom test Builder.

## Full Example

```java
public static final ItemEntry<ComponentItem> MAGIC_WAND = RegistryLibTest.REGISTRYLIB
        .componentItem("magic_wand")
        .initialProperties(() -> new Item.Properties().stacksTo(1))
        .properties(Item.Properties::fireResistant)
        .lang("Magic Wand")
        .lang(ModRegistryCore.LANG_ZH_CN, "魔法杆")
        .defaultModel()
        .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .removeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .tag(ItemTags.DURABILITY_ENCHANTABLE)
        .tooltip(Component.literal("§5A powerful magical artifact"))
        .tooltip((collector, stack) -> {
            collector.node(new SubNode.Basic(Component.literal("§dMagic Wand"), 0), true, false);
            collector.node(new SubNode.Basic(
                    Component.literal("§7Durability: §f" + (stack.getMaxDamage() - stack.getDamageValue())),
                    10));
            collector.node(DETAIL_BOX, new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
            collector.node(DETAIL_BOX, new SubNode.Basic(Component.literal("§7Fire resistant"), 10));
        })
        .attach(new InspectAttachment())
        .register();
```

## Step-by-Step Explanation

1. `componentItem("magic_wand")` starts an attachment-ready `ItemBuilder` exactly as used in `FullItemExample`.
2. `.initialProperties(...)` and `.properties(...)` show the two-layer property pattern used by the test mod.
3. `.lang(...)` and `.lang(ModRegistryCore.LANG_ZH_CN, ...)` show the bilingual naming path the test project actually generates.
4. `.defaultModel()`, `.tab(...)`, `.removeTab(...)`, and `.tag(...)` demonstrate the content-organization APIs used in the runnable example.
5. The two `.tooltip(...)` calls show both the simple overload and the collector-based overload with a separate root node.
6. `.attach(...)` binds a real `ItemAttachment` implementation from the test project.
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