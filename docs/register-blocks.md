---
title: 注册 Blocks
nav_order: 4
parent: Content Guides
permalink: /register-blocks/
---

# 注册 Blocks

本页说明如何用 BlockBuilder 把 Block 本体、BlockItem、loot、recipe 与 tag 放进同一条注册链里。

如果你要做的是“一个能被放置、有掉落、通常还带一个 BlockItem 的方块”，优先从这里开始；BlockEntity 相关内容则放在单独页面展开。

## 适用场景 / 前置条件

- 你已经有可用的 RegistryCore 或 Group
- 你要注册普通 Block，或者带自定义 `BlockItem` 的 Block
- 你希望把方块属性、掉落表、物品提示与 datagen 配置集中管理

## 快速开始

下面这个例子适合最常见的“普通方块 + 自动 BlockItem”场景。

```java
public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
        .block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

    这个版本已经完成了基础属性复制、显示名写入，以及默认 `BlockItem` 生成。

    ## 完整示例

    下面的示例保留了现有文档中的两种常见路径：一个是功能更完整的普通 Block 链；另一个是自定义 Block 子类的注册方式。

```java
// ── single block using every API ──
public static final BlockEntry<Block> MAGIC_ORE = RegistryLibTest.REGISTRYLIB.block(
        "magic_ore",
        Block::new)
        .initialProperties(() -> Blocks.IRON_ORE)
        .properties(p -> p.strength(4.0F, 5.0F).requiresCorrectToolForDrops())
        .lang("Magic Ore")
        .loot((tables, b) ->
                tables.add(b, tables.createOreDrop(b, SimpleItemExample.COPPER_COIN.get())))
        .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
        .recipe((ctx, prov) -> { /* recipe generation */ })
        .item(item -> item
            .tooltip((collector, stack) -> {
                collector.node(
                        new SubNode.Basic(Component.literal("§5Drops coins when mined")),
                        true, false);
            })
        )
        .register();

// ── custom block subclass ──
public static final BlockEntry<TimerBlock> STANDALONE_TIMER = RegistryLibTest.REGISTRYLIB
        .block("standalone_timer", p -> new TimerBlock(p, 4))
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .lang("Standalone Timer")
        .defaultLoot()
        .defaultBlockstate()
        .simpleItem()
        .register();
```

## 分步骤解释

1. 用 `.block("id", factory)` 声明方块，并用 `.initialProperties(...)` 从现有方块复制一套基础属性。
2. 用 `.properties(...)` 追加真正和你内容有关的差异，例如硬度、爆炸抗性或 `requiresCorrectToolForDrops()`。
3. 用 `.lang(...)`、`.defaultBlockstate()`、`.defaultLoot()` 先把最常见的资源生成路径走通；这套组合适合大量“标准全方块”内容。
4. 如果需要方块物品，使用 `.simpleItem()` 或 `.item(...)`。前者适合默认 `BlockItem`，后者适合追加 tooltip、语言或自定义工厂。
5. 用 `.loot(...)`、`.recipe(...)`、`.tag(...)` 处理掉落、配方和挖掘分组；像矿石掉落这种偏行为的差异，通常都放在 `.loot(...)`。
6. 如果方块需要 `BlockEntity`，可以在这里内联创建，也可以跳到专门的 [注册 Block Entities 和 Renderers]({{ '/register-block-entities-and-renderers/' | relative_url }}) 页面单独处理。

{: .note }
> `.simpleItem()` 只会生成默认 BlockItem。只要你需要 tooltip、改名或自定义工厂，就改用 `.item(...)`。

## 常见模式 / 常见坑

- `initialProperties(() -> Blocks.X)` 的意义是“复制基线”，不是“引用原方块实例”。它适合快速继承 vanilla 手感。
- 没有调用 `.simpleItem()` 或 `.item(...)` 时，注册出来的只是 Block 本体，不会自动出现对应 BlockItem。
- `defaultLoot()` 适合“挖掉自己掉自己”；矿石、条件掉落或特殊产物请直接写 `.loot(...)`。
- 多个同类方块共享 creative tab、lang 前缀或属性修饰时，不要在每个条目里重复写，改用 [Group System]({{ '/group-system/' | relative_url }})。

## Quick API

| 方法 | 何时使用 |
| --- | --- |
| `.block("id", Block::new)` | 开始一个 Block 注册链。 |
| `.simpleItem()` | 生成默认 BlockItem。 |
| `.item(...)` | 自定义 BlockItem 行为或展示。 |
| `.defaultLoot()` / `.loot(...)` | 生成默认掉落，或覆写掉落逻辑。 |
| `.tag(...)` | 把方块加入挖掘与分类 tag。 |

## 相关链接

- [Content Guides]({{ '/content-guides/' | relative_url }})
- [Group System]({{ '/group-system/' | relative_url }})
- [注册 Block Entities 和 Renderers]({{ '/register-block-entities-and-renderers/' | relative_url }})
- [注册 Items]({{ '/register-items/' | relative_url }})