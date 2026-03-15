---
title: Register Items
nav_order: 3
permalink: /register-items/
---

# Register Items

RegistryLib registers items using a fluent Builder pattern. Provide an id and an item factory, then chain configuration calls — properties, language, model, creative tab, recipe, tooltip, and CompositeItem attachments — and finalise with `.register()`.

---

## Simple Example

The simplest item registration: one item and a display name.

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

---

## Full Example

A CompositeItem example exercising every ItemBuilder API, including properties, the tooltip system, attachments, and tags.

```java
public static final ItemEntry<CompositeItem> MAGIC_WAND = RegistryLibTest.REGISTRYLIB
        .item("magic_wand", CompositeItem::new)
        .initialProperties(() -> new Item.Properties().stacksTo(1))
        .properties(p -> p.fireResistant())
        .lang("Magic Wand")
        .defaultModel()
        .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .removeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .recipe((ctx, prov) -> { /* ShapedRecipeBuilder, etc. */ })
        .tag(ItemTags.DURABILITY_ENCHANTABLE)
        .tooltip(Component.literal("§5A powerful magical artifact"))
        .tooltip((collector, stack) -> {
            collector.node(
                    new SubNode.Basic(Component.literal("§dMagic Wand"), 0), true, false);
            collector.node(
                    new SubNode.Basic(
                            Component.literal("§7Durability: §f"
                                    + (stack.getMaxDamage() - stack.getDamageValue())), 10));
            collector.node(
                    DETAIL_BOX,
                    new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
        })
        .attach(new InspectAttachment())
        .register();
```

---

## API Reference

### `initialProperties(Supplier<Item.Properties>)`

Provides a fresh `Item.Properties` as the base, replacing the default empty Properties.

```java
item.initialProperties(() -> new Item.Properties().stacksTo(1));
```

Use this when certain values must be fixed at property-creation time (e.g. max stack size).

---

### `properties(UnaryOperator<Item.Properties>)`

Appends modifications on top of the existing Properties. Can be called multiple times; effects accumulate.

```java
item.properties(p -> p.fireResistant());
```

Complements `initialProperties`: use `initialProperties` to set the foundation, then `properties` to layer adjustments on top.

---

### `lang(String)`

Sets the display name and writes it to the language file automatically.

```java
item.lang("Copper Coin");
```

---

### `defaultLang()`

Derives the display name automatically from the registry name (e.g. `copper_coin` → `Copper Coin`).

```java
item.defaultLang();
```

---

### `defaultModel()`

Uses the default flat item model (`FLAT_ITEM`).

```java
item.defaultModel();
```

Sufficient for common material items (e.g. dust, gems) that don't need a custom model.

---

### `model(Supplier<BiConsumer<DataGenContext, RegistryLibItemModelGenerator>>)`

Customises the item model generation logic.

```java
item.model(() -> (ctx, prov) -> {
    prov.generateFlatItem(ctx.get(), ModelTemplates.FLAT_ITEM);
});
```

Use this when you need a multi-layer texture, a held-item override, or a non-standard model.

---

### `tab(ResourceKey<CreativeModeTab>)`

Adds the item to the specified creative tab using default ordering.

```java
item.tab(CreativeModeTabs.TOOLS_AND_UTILITIES);
```

---

### `tab(ResourceKey<CreativeModeTab>, BiConsumer)`

Adds the item to a creative tab and controls its position via a modifier.

```java
item.tab(CreativeModeTabs.TOOLS_AND_UTILITIES, (ctx, modifier) -> {
    modifier.accept(ctx, Items.DIAMOND_PICKAXE.getDefaultInstance());
});
```

The second argument specifies the insertion position (placed after a given item).

---

### `removeTab(ResourceKey<CreativeModeTab>)`

Removes the item from the specified creative tab.

```java
item.removeTab(CreativeModeTabs.TOOLS_AND_UTILITIES);
```

---

### `recipe(BiConsumer<DataGenContext, RegistryLibRecipeProvider>)`

Generates a recipe through DataGen.

```java
item.recipe((ctx, prov) -> {
    // ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())...
});
```

---

### `tag(TagKey<Item>...)`

Adds one or more tags to the item.

```java
item.tag(ItemTags.DURABILITY_ENCHANTABLE);
```

---

### `tooltip(Component)`

Adds a single-line static tooltip.

```java
item.tooltip(Component.literal("§5A powerful magical artifact"));
```

The simplest tooltip option, suitable for a one-line description.

---

### `tooltip(TooltipNodeCollector.TooltipConfig)`

Registers a dynamic multi-line tooltip with support for sort priority and independent root nodes.

```java
item.tooltip((collector, stack) -> {
    collector.node(new SubNode.Basic(Component.literal("§dTitle"), 0), true, false);
    collector.node(new SubNode.Basic(Component.literal("§7Info"), 10));
});
```

`collector.node(rootRef, subNode)` writes information into a separate tooltip pane, enabling layered display.

---

### `attach(CompositeItemAttachment<?>)`

Binds a CompositeItem attachment, adding right-click interactions, extra tooltips, tick behaviour, and more.

```java
item.attach(new InspectAttachment());
```

{: .important }
> Only valid for `CompositeItem`. Attachments can override `use()`, `useOn()`, `inventoryTick()`, `collectTooltipNodes()`, etc.