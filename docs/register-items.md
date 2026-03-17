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
        .addDefaultTab()
        .addTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .removeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .addTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .addTag(ItemTags.DURABILITY_ENCHANTABLE)
        .addTooltip(Component.literal("§5A powerful magical artifact"))
        .addTooltip((collector, stack) -> {
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
4. `.defaultModel()`, `.addDefaultTab()`, `.addTab(...)`, `.removeTab(...)`, and `.addTag(...)` demonstrate the content-organization APIs used in the runnable example.
   - `.addDefaultTab()` — adds the item to the `RegistryCore`-level default tab if one is set; useful when you later also add extra tabs and still want the default included.
   - `.addTab(tab)` — adds the item to the specified creative tab. Multiple calls are allowed, so the item can appear in several tabs at once.
5. The two `.addTooltip(...)` calls show both the simple overload and the collector-based overload with a separate root node.
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
| `item(name)` | Create a plain `Item` builder without specifying a factory |
| `componentItem(name)` | Create a `ComponentItem`-backed `ItemBuilder` |
| `lang(text)` | Set the display name |
| `defaultModel()` | Generate the default item model |
| `addTab(tab)` | Add the item to a creative tab |
| `addDefaultTab()` | Add the item to the `RegistryCore`-level default tab (even when other tabs are also added) |
| `removeTab(tab)` | Remove a previously added creative tab |
| `texture(imageSupplier)` | Supply a `BufferedImage` to generate the item texture during datagen |
| `addTooltip(...)` | Add tooltip nodes |
| `attach(...)` | Add an attachment |
| `register()` | Complete registration |}

## Generating Item Textures Programmatically

When the item has no hand-drawn texture and a simple procedurally-generated icon is acceptable (for example, during rapid prototyping or for automatically-colored placeholder icons), use `.texture(Supplier<BufferedImage>)` in the datagen chain:

```java
import com.gto.registrylib.util.ColorUtil;
import com.gto.registrylib.util.ImageUtil;

.texture(() -> ImageUtil.generateIcon(ColorUtil.generateRandomVibrantColor(), ImageUtil.CIRCLE))
```

The supplier is only evaluated during datagen. The generated PNG is written via `RegistryLibGeneralResourceProvider` to the `textures/item/` folder alongside normal resource providers.

`ImageUtil` offers the following built-in shapes:

| Constant | Description |
| --- | --- |
| `ImageUtil.CIRCLE` | Filled circle with a soft highlight |
| `ImageUtil.SQUARE` | Filled rectangle |
| `ImageUtil.STAR` | Five-pointed star |

`ColorUtil` provides random color generators for quick placeholder icons:

| Method | Description |
| --- | --- |
| `ColorUtil.generateRandomVibrantColor()` | High-saturation, medium-brightness random color |
| `ColorUtil.generateRandomMutedColor()` | Low-saturation, medium-brightness random color |
| `ColorUtil.generateRandomColor()` | Fully random RGB color |

{: .note }
> These utilities are datagen-only and are only invoked when `doDatagen()` returns true. They have no runtime effect on the registered item.

## Related Links

- [5-Minute Quickstart]({{ '/quickstart/' | relative_url }})
- [Tooltip System]({{ '/tooltip-system/' | relative_url }})
- [Group System]({{ '/group-system/' | relative_url }})