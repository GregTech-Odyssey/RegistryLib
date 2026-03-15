---
title: Group System
parent: 核心系统
nav_order: 1
permalink: /group-system/
---

# Group System

`Group` 是包裹在 `RegistryCore` 外层的一组共享默认值。它解决的问题不是“能不能注册”，而是“同一内容家族里要不要重复写相同配置”。

## 何时使用

- 你有一批条目共享 lang 前缀。
- 你想让多个方块或物品默认进入同一个 creative tab。
- 你希望 block/item 的属性修饰器能被一整组条目继承。

{: .note }
> 如果你只注册一个孤立条目，通常直接使用 `RegistryCore` 更简单。Group 的价值在于复用，不在于替代所有入口。

## 快速例子

```java
public static final Group TIMER_GROUP = REGISTRYLIB.group("timers")
        .langPrefix("Timer")
        .blockProperties(p -> p.strength(5.0F, 6.0F))
        .build();

public static final BlockEntry<TimerBlock> TIMER_TIER_1 = TIMER_GROUP
        .block("tier_1", p -> new TimerBlock(p, 1))
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .simpleItem()
        .register();
```

这个例子里，`tier_1` 会自动继承 lang 前缀和 block 属性修饰器。

## 核心概念

### Group 会自动应用什么

| 默认值 | 作用范围 |
| --- | --- |
| `langPrefix` | 所有通过该 Group 注册的条目 |
| `tab` | BlockItem、普通 Item、流体桶 |
| `blockProperties` | 所有通过该 Group 注册的 Block |
| `itemProperties` | 所有通过该 Group 注册的普通 Item |

### 覆写顺序

Group 的默认值会在 Builder 创建时先应用。你在 `.block(...)`、`.item(...)`、`.fluid(...)` 之后继续链式调用的内容，优先级更高。

## 常见组合

- 内容分层：先用 Group 统一 lang 前缀和 tab，再在单个条目上覆写少数特殊值。
- 机器分级：用 Group 统一硬度和掉落要求，再在高阶条目上补特殊 tooltip 或更强属性。
- 大批量矿石：用 Group 统一挖掘要求和 creative tab，再在每个矿石上单独配置掉落逻辑。

## 边界与坑

- Group 不是命名空间替代品，它只负责共享默认值，不负责改变注册提交时机。
- Group 的 tab 不会影响没有对应物品形态的裸 BlockEntity 条目。
- 如果某个条目本身就是明显的例外项，不要为了“整齐”硬塞进同一个 Group。

## 相关链接

- [核心系统]({{ '/systems-overview/' | relative_url }})
- [注册 Blocks]({{ '/register-blocks/' | relative_url }})
- [注册 Fluids 和 Buckets]({{ '/register-fluids-and-buckets/' | relative_url }})
