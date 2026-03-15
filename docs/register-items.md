---
title: 注册 Items
nav_order: 3
parent: Content Guides
permalink: /register-items/
---

# 注册 Items

本页说明如何用 RegistryLib 的 ItemBuilder 注册普通 Item 或 CompositeItem，并把语言、模型、配方、creative tab、tooltip 与 attachment 收拢到同一条 fluent chain 里。

如果你要做的是“一个可注册、可生成资源、可逐步追加行为的 Item”，这里就是主入口。

## 适用场景 / 前置条件

- 你已经有可用的 RegistryCore，例如 `RegistryLibTest.REGISTRYLIB`
- 你需要注册普通 Item，或带 `CompositeItemAttachment` 的 `CompositeItem`
- 你希望把 `lang`、`model`、`recipe`、`tooltip` 与 tag 配置放在同一个注册链中

## 快速开始

最小可用版本只需要名称、工厂方法和 `.register()`；这里额外保留 `.lang(...)`，方便你直接看到生成的显示名

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

这条链已经完成了三件事：声明注册名、指定 Item 工厂、写入英文语言项。

## 完整示例

下面的示例来自现有文档，展示了 `CompositeItem` 注册链里最常一起出现的能力：properties、默认模型、creative tab、recipe、tag、tooltip，以及 attachment。

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

## 分步骤解释

1. 用 `.item("id", factory)` 开始注册链。`id` 决定注册名，`factory` 决定实际 Item 类型。
2. 用 `.initialProperties(...)` 设定基础 `Item.Properties`。像 `stacksTo(1)` 这类需要在初始构造时确定的值，适合放这里。
3. 用 `.properties(...)` 叠加后续修改。它可以多次调用，适合补充 `fireResistant()` 之类的附加属性。
4. 用 `.lang(...)`、`.defaultModel()`、`.tab(...)` 把显示名、模型和 creative tab 补齐，让 datagen 与游戏内展示同步完成。
5. 用 `.recipe(...)`、`.tag(...)`、`.tooltip(...)` 添加资源与交互信息。静态一行提示可直接传 `Component`，动态内容则使用收集器回调。
6. 只有当注册对象是 `CompositeItem` 时，才继续用 `.attach(...)` 绑定 attachment；最后统一以 `.register()` 收尾。

{: .important }
> `.attach(...)` 只适用于 `CompositeItem`。如果工厂返回的是普通 `Item`，不要把 attachment 链接搬过去。

## 常见模式 / 常见坑

- 先用 `.initialProperties(...)` 打底，再用 `.properties(...)` 叠加。把两者职责分开后，注册链更容易维护。
- 只需要一句描述时，用 `.tooltip(Component)` 即可；需要按优先级排序、读 `ItemStack` 状态或拆分额外 tooltip box 时，再切换到回调形式。
- `removeTab(...)` 通常只在你先继承了 Group 或默认 tab、后面又想显式改写时才有意义。单个独立 Item 大多不需要它。
- `defaultModel()` 适合常见平面 Item；如果你本来就有自定义 model 生成逻辑，不要再同时保留默认模型调用。

## Quick API

| 方法 | 何时使用 |
| --- | --- |
| `.item("id", Item::new)` | 开始一个 Item 注册链。 |
| `.lang("Name")` | 写入英文显示名。 |
| `.defaultModel()` | 生成默认平面 Item 模型。 |
| `.tooltip(...)` | 添加静态或动态 tooltip。 |
| `.attach(...)` | 给 `CompositeItem` 绑定可复用行为。 |

## 相关链接

- [Content Guides]({{ '/content-guides/' | relative_url }})
- [Tooltip System]({{ '/tooltip-system/' | relative_url }})
- [Lang System]({{ '/lang-system/' | relative_url }})
- [注册 Blocks]({{ '/register-blocks/' | relative_url }})