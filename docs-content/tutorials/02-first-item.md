---
sidebar_position: 2
title: Your First Item
description: Register your first item with RegistryLib in 5 minutes.
---

# Your First Item

This tutorial registers two items: a minimal one-liner and a fully configured item, so you can see the range of the RegistryLib item API.

## What You Will Learn

- How to register a basic `Item` with `item()`
- How to register a feature-rich `ComponentItem` with `componentItem()`
- The difference between the two builder types
- How to verify your items in-game

## Minimal Example - Copper Coin

The shortest path to a working item:

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .langCn("Copper Coin (zh_cn)")
        .lang("Copper Coin")
        .register();
```

That single chain:

1. **`item("copper_coin", Item::new)`** starts an `ItemBuilder` with the registry name `copper_coin` and the vanilla `Item` factory.
2. **`.langCn(...)`** adds a Simplified Chinese localization through the test project's custom builder.
3. **`.lang("Copper Coin")`** sets the English display name for datagen.
4. **`.register()`** submits the registration and returns an `ItemEntry<Item>` you can reference later.

## Full Example - Magic Wand

When you need tooltips, creative tabs, tags, and attachments, use `componentItem()`:

```java
public static final ItemEntry<ComponentItem> MAGIC_WAND = RegistryLibTest.REGISTRYLIB
        .componentItem("magic_wand")
        .initialProperties(() -> new Item.Properties().stacksTo(1))
        .properties(Item.Properties::fireResistant)
        .lang("Magic Wand")
        .lang(ModRegistryCore.LANG_ZH_CN, "Magic Wand (zh_cn)")
        .defaultModel()
        .addDefaultTab()
        .addTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .removeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .addTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .addTag(ItemTags.DURABILITY_ENCHANTABLE)
        .addTooltip(Component.literal("A powerful magical artifact"))
        .addTooltip((collector, stack) -> {
            collector.node(new SubNode.Basic(Component.literal("Magic Wand"), 0), true, false);
            collector.node(new SubNode.Basic(
                    Component.literal("Durability: " + (stack.getMaxDamage() - stack.getDamageValue())),
                    10));
            collector.node(DETAIL_BOX, new SubNode.Basic(Component.literal("Detailed Information"), 0));
            collector.node(DETAIL_BOX, new SubNode.Basic(Component.literal("Fire resistant"), 10));
        })
        .attach(new InspectAttachment())
        .register();
```

### Step-by-Step Breakdown

1. **`componentItem("magic_wand")`** starts an attachment-ready `ItemBuilder` that produces a `ComponentItem`.
2. **`.initialProperties(...)`** and **`.properties(...)`** show the two-layer property pattern. `initialProperties` sets the base, and `properties` applies modifications on top.
3. **`.lang(...)`** and **`.lang(ModRegistryCore.LANG_ZH_CN, ...)`** show the bilingual naming path. The first call sets the English name; the second adds another locale.
4. **`.defaultModel()`** generates a standard item model pointing at `item/magic_wand`.
5. **`.addDefaultTab()`**, **`.addTab(...)`**, **`.removeTab(...)`** demonstrate the creative-tab APIs. You can add, remove, and re-add tabs freely; the final state is what counts.
6. **`.addTag(...)`** attaches a vanilla or modded item tag.
7. The two **`.addTooltip(...)`** calls show both overloads: one accepts a static `Component`, the other accepts a callback for dynamic tooltips built with the collector API.
8. **`.attach(...)`** binds an `ItemAttachment` implementation to the item.
9. **`.register()`** submits the registration and returns `ItemEntry<ComponentItem>`.

## Choosing Between `item()` and `componentItem()`

| | `item()` | `componentItem()` |
| --- | --- | --- |
| **Use when** | You need a plain item without attachment-based behavior | You need modular attachments or attachment-driven tooltips |
| **Produced type** | `Item` or your custom subclass | `ComponentItem` |
| **Supports `.attach(...)`** | No | Yes |
| **Supports `.addTooltip(...)`** | Yes | Yes |

:::important
`.addTooltip(...)` is available on both `item()` and `componentItem()` because it is defined on the base `ItemBuilder` and works for any item type.
Only `.attach(...)` requires `ComponentItem`. If you call `.attach(...)` on a plain `item()`, it will throw at runtime.
:::

## Verify in Game

1. Run `./gradlew runClient`.
2. Open a creative-mode world.
3. Search for your item name, such as "Copper Coin" or "Magic Wand", in the creative inventory.
4. Confirm the item appears with the correct name and tooltip.

If the item does not appear, check:

- Your registration class is loaded, either from your mod constructor or another loaded class.
- `.register()` is called at the end of the chain.

## Related

- [Your First Block](/tutorials/first-block) registers a block with a matching item
- [Register Items (How-To)](/how-to/register-items) gives the full reference for all item builder options
