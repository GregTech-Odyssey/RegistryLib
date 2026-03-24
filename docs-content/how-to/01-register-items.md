---
sidebar_position: 1
title: Register Items
description: Quick reference for item registration patterns.
---

# Register Items

## Simple Item

```java
public static final ItemEntry<Item> COPPER_COIN = REGISTRYLIB
        .item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

## ComponentItem

Use `componentItem(...)` when the item needs reusable `ItemAttachment` behaviors (custom use logic, extra tooltip collection, inventory tick).

```java
public static final ItemEntry<ComponentItem> MAGIC_WAND = REGISTRYLIB
        .componentItem("magic_wand")
        .initialProperties(() -> new Item.Properties().stacksTo(1))
        .properties(Item.Properties::fireResistant)
        .lang("Magic Wand")
        .defaultModel()
        .addDefaultTab()
        .addTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .addTag(ItemTags.DURABILITY_ENCHANTABLE)
        .addTooltip(Component.literal("§5A powerful magical artifact"))
        .addTooltip((collector, stack) -> {
            collector.node(new SubNode.Basic(Component.literal("§dMagic Wand"), 0), true, false);
            collector.node(new SubNode.Basic(
                    Component.literal("§7Durability: §f" + (stack.getMaxDamage() - stack.getDamageValue())),
                    10));
        })
        .attach(new InspectAttachment())
        .register();
```

## Generating Textures Programmatically

When no hand-drawn texture exists, generate a placeholder icon during datagen:

```java
import com.gto.registrylib.util.ColorUtil;
import com.gto.registrylib.util.ImageUtil;

.texture(() -> ImageUtil.generateIcon(ColorUtil.generateRandomVibrantColor(), ImageUtil.CIRCLE))
```

**ImageUtil shapes:**

| Constant | Description |
|---|---|
| `ImageUtil.CIRCLE` | Filled circle with a soft highlight |
| `ImageUtil.SQUARE` | Filled rectangle |
| `ImageUtil.STAR` | Five-pointed star |

**ColorUtil methods:**

| Method | Description |
|---|---|
| `ColorUtil.generateRandomVibrantColor()` | High-saturation, medium-brightness random color |
| `ColorUtil.generateRandomMutedColor()` | Low-saturation, medium-brightness random color |
| `ColorUtil.generateRandomColor()` | Fully random RGB color |

:::note
These utilities are datagen-only —invoked when `doDatagen()` returns true. They have no runtime effect on the registered item.
:::

## Common API Lookup

| Method | Purpose |
|---|---|
| `item(name, factory)` | Create an `ItemBuilder` |
| `componentItem(name)` | Create a `ComponentItem`-backed `ItemBuilder` |
| `lang(text)` | Set the display name |
| `defaultModel()` | Generate the default item model |
| `addTab(tab)` | Add the item to a creative tab |
| `addDefaultTab()` | Add the item to the `RegistryCore`-level default tab |
| `removeTab(tab)` | Remove a previously added creative tab |
| `texture(imageSupplier)` | Supply a `BufferedImage` to generate the item texture during datagen |
| `addTooltip(...)` | Add tooltip nodes |
| `attach(...)` | Add an `ItemAttachment` |
| `register()` | Complete registration and return `ItemEntry<T>` |

## Common Patterns

**Shortest registration:**

```java
REGISTRYLIB.item("my_item", Item::new).lang("My Item").register();
```

**Ready-to-use stack:** `ItemEntry<T>` provides `asStack()` and `asResource()` helpers —no need to reconstruct stacks from the raw item.

**Shared defaults via Group:** When multiple items share a creative tab, lang prefix, or property modifiers, move shared setup into the [Group System](/tutorials/group-system) instead of repeating it.

**Complex tooltips:** When tooltips include multiple sections or conditional visibility, move to the [Tooltip System](/tutorials/tooltip-system) for structured rendering.

:::important
If you call `.attach(...)` on a normal `Item`, it will not work as expected. Attachments require `ComponentItem` or another `IComponentItem` implementation.
:::

## See Also

- [Tutorial: First Item](/tutorials/first-item)
- [Tutorial: Tooltip System](/tutorials/tooltip-system)
- [Reference: API Overview](/reference/api-overview)
