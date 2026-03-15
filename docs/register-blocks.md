---
title: 注册 Blocks
nav_order: 2
parent: 内容指南
permalink: /register-blocks/
---

# 注册 Blocks

本页解决“如何把 Block 本体、BlockItem、掉落、配方和 tag 放进同一条注册链里”的问题。

## 适用场景 / 前置条件

- 你已经有可用的 `RegistryCore` 或 `Group`。
- 你要注册普通方块，或带自定义 `BlockItem` 的方块。
- 你希望 Block 的 datagen 与运行时配置放在同一处维护。

## 快速开始

```java
public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
        .block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

这个例子已经完成属性复制、显示名写入和默认 `BlockItem` 生成。

## 完整示例

```java
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

1. `block("id", factory)` 开始注册链，`initialProperties(...)` 用于复制现有方块的基线属性。
2. `properties(...)` 追加真正和当前内容相关的差异，例如强度、爆炸抗性或掉落要求。
3. `lang(...)`、`defaultBlockstate()`、`defaultLoot()` 适合标准全方块内容的快速落地。
4. 需要方块物品时，选择 `simpleItem()` 或 `item(...)`。前者生成默认 `BlockItem`，后者允许追加 tooltip 或自定义工厂。
5. `loot(...)`、`recipe(...)`、`tag(...)` 负责掉落、配方和分类；矿石等特殊掉落通常直接写在 `loot(...)` 里。

{: .note }
> `.simpleItem()` 只生成默认 BlockItem。只要你需要 tooltip、改名或自定义工厂，就切换到 `.item(...)`。

## 常见模式 / 常见坑

- `initialProperties(() -> Blocks.X)` 表示复制属性基线，而不是复用原方块实例。
- 没有调用 `.simpleItem()` 或 `.item(...)` 时，注册出来的只有 Block 本体。
- `defaultLoot()` 适合“挖掉自己掉自己”；矿石、条件掉落或特殊产物请直接使用 `.loot(...)`。

## 常用 API 速览

| 方法 | 什么时候用 |
| --- | --- |
| `.block("id", factory)` | 开始一个 Block 注册链。 |
| `.initialProperties(...)` | 复制现有方块的属性基线。 |
| `.simpleItem()` | 生成默认 BlockItem。 |
| `.item(...)` | 自定义 BlockItem 行为或展示。 |
| `.defaultLoot()` / `.loot(...)` | 使用默认掉落，或覆写掉落逻辑。 |

## 相关链接

- [内容指南]({{ '/content-guides/' | relative_url }})
- [Group System]({{ '/group-system/' | relative_url }})
- [注册 Block Entities 和 Renderers]({{ '/register-block-entities-and-renderers/' | relative_url }})
- [注册 Items]({{ '/register-items/' | relative_url }})