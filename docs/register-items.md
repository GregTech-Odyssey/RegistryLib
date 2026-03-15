---
title: 注册 Items
nav_order: 1
parent: 内容指南
permalink: /register-items/
---

# 注册 Items

本页解决“如何把一个 Item 的注册、显示名、模型、tooltip、tag、recipe 和可复用行为写在同一条链里”的问题。

## 适用场景 / 前置条件

- 你已经有可用的 `RegistryCore` 或 `Group`。
- 你要注册普通 `Item`，或带 attachment 的 `CompositeItem`。
- 你希望 datagen 相关配置和运行时配置留在同一个入口里。

## 快速开始

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

这是最小的 Item 注册链：注册名、工厂方法、英文显示名，然后提交注册。

## 完整示例

```java
public static final ItemEntry<CompositeItem> MAGIC_WAND = RegistryLibTest.REGISTRYLIB
        .item("magic_wand", CompositeItem::new)
        .initialProperties(() -> new Item.Properties().stacksTo(1))
        .properties(p -> p.fireResistant())
        .lang("Magic Wand")
        .defaultModel()
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

## 分步骤解释

1. `item("id", factory)` 决定注册名和对象类型。
2. `initialProperties(...)` 负责初始 `Item.Properties`，适合 `stacksTo(1)` 这类必须在一开始确定的设置。
3. `properties(...)` 在基础属性上叠加修改，适合追加防火等增量配置。
4. `lang(...)`、`defaultModel()`、`tab(...)` 把最常见的显示层设置集中写完。
5. `recipe(...)`、`tag(...)`、`tooltip(...)` 负责资源生成和玩家可见信息。
6. 如果对象类型是 `CompositeItem`，再用 `attach(...)` 追加可复用行为模块。

{: .important }
> `attach(...)` 只适用于 `CompositeItem`。如果工厂返回的是普通 `Item`，不要把 attachment 相关调用原样搬过去。

## 常见模式 / 常见坑

- 需要一句固定描述时，用 `tooltip(Component)`；需要读取 `ItemStack`、排序或额外面板时，再切换到回调形式。
- `defaultModel()` 适合平面 Item。只要你已经准备了自定义 model 生成逻辑，就不要同时保留默认模型调用。
- `removeTab(...)` 一般只在你先继承了 Group 默认 tab、后面又要显式改写时才有意义。

## 常用 API 速览

| 方法 | 什么时候用 |
| --- | --- |
| `.item("id", factory)` | 开始一个 Item 注册链。 |
| `.initialProperties(...)` | 先定义基础 `Item.Properties`。 |
| `.defaultModel()` | 生成默认平面模型。 |
| `.tooltip(...)` | 添加静态或动态 tooltip。 |
| `.attach(...)` | 给 `CompositeItem` 绑定 attachment。 |

## 相关链接

- [内容指南]({{ '/content-guides/' | relative_url }})
- [Tooltip System]({{ '/tooltip-system/' | relative_url }})
- [Lang System]({{ '/lang-system/' | relative_url }})
- [注册 Blocks]({{ '/register-blocks/' | relative_url }})