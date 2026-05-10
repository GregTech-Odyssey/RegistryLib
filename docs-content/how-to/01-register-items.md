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

## Tinted Item Models

Use `constantTint(...)` when a grayscale item texture should be colored by the generated item model instead of generating one PNG per color:

```java
import com.gto.registrylib.util.color.RgbColor;

public static final ItemEntry<Item> TIN_DUST = REGISTRYLIB
        .item("tin_dust")
        .constantTint("item/templates/dust", RgbColor.of(0xC8D8E8))
        .lang("Tin Dust")
        .register();
```

`RgbColor` accepts only `0xRRGGBB`. RegistryLib converts it to opaque ARGB for item model tints.

The texture path is an existing model texture reference, not a generated PNG request. The template PNG must already exist under `assets/<modid>/textures/...`, or be generated once by a general resource provider. Multiple items can point at the same grayscale template and differ only by tint:

```java
REGISTRYLIB.item("lead_dust")
        .constantTint("item/templates/dust", RgbColor.of(0x6E7380))
        .register();
REGISTRYLIB.item("nickel_dust")
        .constantTint("item/templates/dust", RgbColor.of(0xD5C36A))
        .register();
```

For helper methods that pass texture paths around, prefer `TextureRef`:

```java
TextureRef dustTemplate = REGISTRYLIB.textureRef("item/templates/dust");

REGISTRYLIB.item("silver_dust")
        .visual(ItemVisualPreset.tintedTemplate(dustTemplate, RgbColor.of(0xD7D7E8)))
        .register();
```

For custom Minecraft item tint sources, use `tintSource(...)`.
Use `TextureRef.mc("item/iron_ingot")` or `TextureRef.of(id)` for vanilla or third-party textures; string overloads such as `modelTexture("item/template")` resolve in the current mod namespace.

:::important
`.texture(path, imageSupplier)` generates a PNG into `src/generated/resources`. Use `modelTexture(...)`, `existingTexture(...)`, `constantTint(texturePath, color)`, or `flatTintedModel(...)` when you only want the model to reference an existing shared template.
:::

## Common API Lookup

| Method | Purpose |
|---|---|
| `item(name, factory)` | Create an `ItemBuilder` |
| `componentItem(name)` | Create a `ComponentItem`-backed `ItemBuilder` |
| `lang(text)` | Set the display name |
| `defaultModel()` | Generate the default item model |
| `constantTint(RgbColor)` | Generate a flat item model using the item's own texture path |
| `constantTint(texturePath, RgbColor)` | Generate a flat tinted model referencing a shared texture |
| `constantTint(texture, RgbColor)` | TextureRef variant for shared textures |
| `tintSource(texturePath, sources...)` / `flatTintedModel(texturePath, sources...)` | Generate a flat item model with custom item tint sources |
| `modelTexture(texturePath)` / `existingTexture(texturePath)` | Reference an existing texture without writing a PNG |
| `visual(ItemVisualPreset.tintedTemplate(...))` | Reuse a shared item visual rule in batch registration |
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
