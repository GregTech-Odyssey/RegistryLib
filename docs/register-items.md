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

- You want to register a normal `Item` or a `CompositeItem`.
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
public static final ItemEntry<CompositeItem> WRENCH = RegistryLibTest.REGISTRYLIB
        .item("wrench", CompositeItem::create)
        .lang("Wrench")
        .defaultModel()
        .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .tooltip(item -> TooltipNode.root()
                .text("Used to configure machines"))
        .attach(new DurabilityBarAttachment())
        .register();
```

## Step-by-Step Explanation

1. `item("wrench", CompositeItem::create)` starts an `ItemBuilder` chain and determines the base type that will be registered.
2. `.lang("Wrench")` provides the display name for datagen.
3. `.defaultModel()` requests the most common default item model generation.
4. `.tab(...)` controls which creative inventory tab the Item belongs to.
5. `.tooltip(...)` supplies a root node or tooltip node construction logic to the Tooltip System.
6. `.attach(...)` only applies to Item types that support attachments, such as `CompositeItem`.
7. `.register()` submits the registration and returns `ItemEntry<CompositeItem>`.

{: .important }
> If you call `.attach(...)` on a normal `Item`, the problem is usually the selected type rather than the attachment itself. Attachments are designed for extensible Item types such as `CompositeItem`, not for every Item uniformly.

## Common Patterns

### I Only Want the Shortest Possible Item Registration

Keep only `item(...)`, `lang(...)`, and `register()`. Add `defaultModel()` only if your project wants the default generated model.

### I Want Multiple Items to Share Defaults

When multiple Items share a creative tab, lang prefix, or property modifiers, do not repeat the same setup on every Item. Move the shared defaults into [Group System]({{ '/group-system/' | relative_url }}).

### I Need More Complex Tooltips

When tooltips start to include multiple sections, conditional visibility, or separate rendering areas, do not keep forcing all of the logic into one lambda. Move to [Tooltip System]({{ '/tooltip-system/' | relative_url }}) and structure it explicitly.

## Common API Lookup

| Method | Purpose |
| --- | --- |
| `item(name, factory)` | Create an `ItemBuilder` |
| `lang(text)` | Set the display name |
| `defaultModel()` | Generate the default item model |
| `tab(tab)` | Set the creative tab |
| `tooltip(...)` | Add tooltip nodes |
| `attach(...)` | Add an attachment |
| `register()` | Complete registration |

## Related Links

- [5-Minute Quickstart]({{ '/quickstart/' | relative_url }})
- [Tooltip System]({{ '/tooltip-system/' | relative_url }})
- [Group System]({{ '/group-system/' | relative_url }})---
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

- You want to register a normal `Item` or a `CompositeItem`.
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
public static final ItemEntry<CompositeItem> WRENCH = RegistryLibTest.REGISTRYLIB
        .item("wrench", CompositeItem::create)
        .lang("Wrench")
        .defaultModel()
        .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .tooltip(item -> TooltipNode.root()
                .text("Used to configure machines"))
        .attach(new DurabilityBarAttachment())
        .register();
```

## Step-by-Step Explanation

1. `item("wrench", CompositeItem::create)` starts an `ItemBuilder` chain and determines the base type that will be registered.
2. `.lang("Wrench")` provides the display name for datagen.
3. `.defaultModel()` requests the most common default item model generation.
4. `.tab(...)` controls which creative inventory tab the Item belongs to.
5. `.tooltip(...)` supplies a root node or tooltip node construction logic to the Tooltip System.
6. `.attach(...)` only applies to Item types that support attachments, such as `CompositeItem`.
7. `.register()` submits the registration and returns `ItemEntry<CompositeItem>`.

{: .important }
> If you call `.attach(...)` on a normal `Item`, the problem is usually the selected type rather than the attachment itself. Attachments are designed for extensible Item types such as `CompositeItem`, not for every Item uniformly.

## Common Patterns

### I Only Want the Shortest Possible Item Registration

Keep only `item(...)`, `lang(...)`, and `register()`. Add `defaultModel()` only if your project wants the default generated model.

### I Want Multiple Items to Share Defaults

When multiple Items share a creative tab, lang prefix, or property modifiers, do not repeat the same setup on every Item. Move the shared defaults into [Group System]({{ '/group-system/' | relative_url }}).

### I Need More Complex Tooltips

When tooltips start to include multiple sections, conditional visibility, or separate rendering areas, do not keep forcing all of the logic into one lambda. Move to [Tooltip System]({{ '/tooltip-system/' | relative_url }}) and structure it explicitly.

## Common API Lookup

| Method | Purpose |
| --- | --- |
| `item(name, factory)` | Create an `ItemBuilder` |
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