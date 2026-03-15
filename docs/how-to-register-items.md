---
title: Register Items
nav_order: 2
permalink: /register-items/
---

# Register Items

RegistryLib 使用流畅的 Builder 风格注册物品。你只需提供一个 id、一个物品工厂和一个配置 Consumer，
即可在同一处完成属性设置、语言、模型、标签页、配方、Tooltip 及 CompositeItem 附件等所有配置。

---

## Simple Example

最基础的物品注册：一个物品 + 一行语言。

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB.item(
        "copper_coin",
        Item::new,
        item -> {
            item.lang("Copper Coin");
        });
```

---

## Full Example

使用 ItemBuilder 全部 API 的 CompositeItem 示例，包含属性、Tooltip 系统、附件、标签等。

```java
public static final ItemEntry<CompositeItem> MAGIC_WAND = RegistryLibTest.REGISTRYLIB.item(
        "magic_wand",
        CompositeItem::new,
        item -> {
            item.initialProperties(() -> new Item.Properties().stacksTo(1));
            item.properties(p -> p.fireResistant());
            item.lang("Magic Wand");
            item.defaultModel();
            item.tab(CreativeModeTabs.TOOLS_AND_UTILITIES);
            item.removeTab(CreativeModeTabs.TOOLS_AND_UTILITIES);
            item.tab(CreativeModeTabs.TOOLS_AND_UTILITIES);
            item.recipe((ctx, prov) -> { /* ShapedRecipeBuilder 等 */ });
            item.tag(ItemTags.DURABILITY_ENCHANTABLE);
            item.tooltip(Component.literal("§5A powerful magical artifact"));
            item.tooltip((collector, stack) -> {
                collector.node(
                        new SubNode.Basic(Component.literal("§dMagic Wand"), 0), true, false);
                collector.node(
                        new SubNode.Basic(
                                Component.literal("§7Durability: §f"
                                        + (stack.getMaxDamage() - stack.getDamageValue())), 10));
                collector.node(
                        DETAIL_BOX,
                        new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
            });
            item.attach(new InspectAttachment());
        });
```

---

## API Reference

### `initialProperties(Supplier<Item.Properties>)`

提供一个全新的 `Item.Properties` 作为基础，替换默认的空 Properties。

```java
item.initialProperties(() -> new Item.Properties().stacksTo(1));
```

用于需要在属性创建阶段就确定某些值的场景（如最大堆叠数）。

---

### `properties(UnaryOperator<Item.Properties>)`

在已有 Properties 基础上追加修改。可多次调用，效果叠加。

```java
item.properties(p -> p.fireResistant());
```

与 `initialProperties` 互补：先用 `initialProperties` 设基础，再用 `properties` 叠加调整。

---

### `lang(String)`

设置英文显示名称，自动写入语言文件。

```java
item.lang("Copper Coin");
```

---

### `defaultLang()`

从注册名自动推导英文名（如 `copper_coin` → `Copper Coin`）。

```java
item.defaultLang();
```

---

### `defaultModel()`

使用默认扁平物品模型（`FLAT_ITEM`）。

```java
item.defaultModel();
```

对普通材料类物品（如矿粉、宝石）足够使用，不需要自定义模型。

---

### `model(Supplier<BiConsumer<DataGenContext, RegistryLibItemModelGenerator>>)`

自定义物品模型生成逻辑。

```java
item.model(() -> (ctx, prov) -> {
    prov.generateFlatItem(ctx.get(), ModelTemplates.FLAT_ITEM);
});
```

当需要多层纹理、手持模型或非标准模型时使用。

---

### `tab(ResourceKey<CreativeModeTab>)`

将物品加入指定创造标签页，使用默认排序。

```java
item.tab(CreativeModeTabs.TOOLS_AND_UTILITIES);
```

---

### `tab(ResourceKey<CreativeModeTab>, BiConsumer)`

将物品加入标签页，并通过 modifier 控制排列位置。

```java
item.tab(CreativeModeTabs.TOOLS_AND_UTILITIES, (ctx, modifier) -> {
    modifier.accept(ctx, Items.DIAMOND_PICKAXE.getDefaultInstance());
});
```

第二个参数可指定插入位置（排在某物品之后）。

---

### `removeTab(ResourceKey<CreativeModeTab>)`

从指定标签页中移除物品。

```java
item.removeTab(CreativeModeTabs.TOOLS_AND_UTILITIES);
```

---

### `recipe(BiConsumer<DataGenContext, RegistryLibRecipeProvider>)`

通过 DataGen 生成配方。

```java
item.recipe((ctx, prov) -> {
    // ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())...
});
```

---

### `tag(TagKey<Item>...)`

给物品添加一个或多个标签。

```java
item.tag(ItemTags.DURABILITY_ENCHANTABLE);
```

---

### `tooltip(Component)`

添加单行静态 Tooltip。

```java
item.tooltip(Component.literal("§5A powerful magical artifact"));
```

最简单的 Tooltip 方式，适合一行描述。

---

### `tooltip(TooltipNodeCollector.TooltipConfig)`

注册动态多行 Tooltip，支持排序优先级和独立根节点。

```java
item.tooltip((collector, stack) -> {
    collector.node(new SubNode.Basic(Component.literal("§dTitle"), 0), true, false);
    collector.node(new SubNode.Basic(Component.literal("§7Info"), 10));
});
```

`collector.node(rootRef, subNode)` 可向独立浮窗写入信息，用于分层展示。

---

### `attach(CompositeItemAttachment<?>)`

绑定 CompositeItem 附件，为物品添加右键交互、额外 Tooltip、tick 行为等。

```java
item.attach(new InspectAttachment());
```

仅对 `CompositeItem` 类型有效。附件可覆写 `use()`、`useOn()`、`inventoryTick()`、`collectTooltipNodes()` 等方法。