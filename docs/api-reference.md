---
title: API 参考
nav_order: 5
permalink: /api-reference/
---

# API 参考

这一页只做速查，不承担完整教学。详细示例留在教程页；这里负责回答“我该从哪个入口开始”和“常见链路通常长什么样”。

{: .note }
> 去重规则：本页只提供方法族、职责边界和常见链路速查，不重复整页教程中的完整示例。

## 从哪个入口开始

| 你要做什么 | 入口 | 后续通常会接什么 |
| --- | --- | --- |
| 注册普通或复合 Item | `item("id", factory)` | `lang`、`defaultModel`、`tab`、`tooltip`、`attach` |
| 注册 Block | `block("id", factory)` | `initialProperties`、`simpleItem` 或 `item`、`loot`、`tag` |
| 注册 Fluid | `fluid("id", still, flow)` | `lang`、`clientExtension`、`properties`、`block`、`bucket` |
| 注册 BlockEntity | `blockEntity("id", factory)` | `validBlock` 或 `validBlocks`、`renderer` |
| 批量共享默认值 | `group("name")` | `langPrefix`、`tab`、`blockProperties`、`itemProperties` |

## Builder 族速查

| Builder | 负责什么 | 典型终点 |
| --- | --- | --- |
| `ItemBuilder` | Item 属性、模型、tooltip、tab、recipe、tag | `ItemEntry` |
| `BlockBuilder` | Block 属性、掉落、方块物品、配方、tag | `BlockEntry` |
| `FluidBuilder` | 流体类型、渲染、block、bucket、tag | `FluidEntry` |
| `BlockEntityBuilder` | 宿主方块绑定与 renderer | `BlockEntityEntry` |

## Entry 类型速查

| 类型 | 典型用途 |
| --- | --- |
| `ItemEntry<T>` | 在其他注册链、配方或逻辑中引用 Item |
| `BlockEntry<T>` | 引用 Block，并可进一步关联默认状态或方块物品 |
| `FluidEntry<T>` | 同时访问 source、type、block、bucket 等流体相关对象 |
| `BlockEntityEntry<T>` | 引用 `BlockEntityType` 并完成宿主绑定 |

## 常见链路速查

### 最小 Item

```java
REGISTRYLIB.item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

### 最小 Block

```java
REGISTRYLIB.block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .simpleItem()
        .register();
```

### Group 里的 Block

```java
MACHINES.block("crusher", Block::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .defaultLoot()
        .simpleItem()
        .register();
```

## 相关链接

- [内容指南]({{ '/content-guides/' | relative_url }})
- [核心系统]({{ '/systems-overview/' | relative_url }})
- [高级主题]({{ '/advanced-topics/' | relative_url }})
