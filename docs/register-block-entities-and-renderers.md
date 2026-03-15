---
title: 注册 Block Entities 和 Renderers
nav_order: 5
parent: Content Guides
permalink: /register-block-entities-and-renderers/
---

# 注册 Block Entities 和 Renderers

本页说明如何把 `BlockEntityType`、可承载它的 Block，以及客户端 renderer 放进同一条注册链里。

核心目标只有两个：一是明确哪些 Block 能持有这个 `BlockEntity`，二是保证 renderer 只在客户端懒加载，避免服务端错误加载 client class。

## 适用场景 / 前置条件

- 你已经有至少一个已注册的 Block 可作为宿主
- 你需要把一个 `BlockEntityType` 绑定到一个或多个 Block
- 你需要在 client 侧为该 `BlockEntity` 配置 renderer

## 快速开始

最小例子只有一个 `BlockEntity` 和一个合法宿主方块。

```java
public static final BlockEntityEntry<TimerBlockEntity> SIMPLE_TIMER_BE =
        RegistryLibTest.REGISTRYLIB
                .blockEntity("simple_timer", TimerBlockEntity::new)
                .validBlock(FullBlockExample.STANDALONE_TIMER)
                .register();
```

只要 `validBlock(...)` 已经声明，RegistryLib 就知道这个 `BlockEntityType` 可以附着在哪些 Block 上。

## 完整示例

下面的例子展示了一个更接近实际 mod 的场景：多个 tier Block 共用同一个 `BlockEntity` 实现，并在 client 侧绑定 renderer。

```java
public static final BlockEntityEntry<TimerBlockEntity> TIMER_BLOCK_ENTITY =
        RegistryLibTest.REGISTRYLIB
                .blockEntity("timer", TimerBlockEntity::new)
                // bind one BlockEntity type to multiple blocks
                .validBlocks(
                        FullBlockExample.TIMER_TIER_1,
                        FullBlockExample.TIMER_TIER_2,
                        FullBlockExample.TIMER_TIER_3)
                // lazily bind the client renderer via a supplier-of-supplier
                .renderer(() -> TimerBlockEntityRenderer::new)
                .register();
```

## 分步骤解释

1. 用 `.blockEntity("id", factory)` 开始注册链，工厂负责创建具体的 `BlockEntity` 实例。
2. 用 `.validBlock(...)` 或 `.validBlocks(...)` 指定宿主 Block。没有这一步，`BlockEntityType` 就不知道自己能附着到哪些方块。
3. 如果只需要服务端逻辑，到这里就可以 `.register()`；如果还需要渲染，再加 `.renderer(...)`。
4. `.renderer(() -> TimerBlockEntityRenderer::new)` 里的外层 `Supplier` 是关键，它把 renderer 的 class 加载推迟到 client 环境，避免服务端触发 `ClassNotFoundException`。

{: .important }
> renderer 相关类型必须继续保持在 `Supplier` 后面，不要把 client class 提前实例化或直接引用到服务端初始化路径里。

## 常见模式 / 常见坑

- 一个 `BlockEntity` 只服务一个 Block 时，优先用 `.validBlock(...)`；多个 tier 或多个外观共用同一实现时，再切到 `.validBlocks(...)`。
- 如果 Block 还没注册好，就不要急着绑定 `validBlock(...)`；先把宿主 Block 的注册关系理顺。
- renderer 是可选的，但只要你写了 renderer，就必须保持它的 client-only 惰性加载写法。
- 需要同时理解 Block 与 BlockEntity 的分工时，先看 [注册 Blocks]({{ '/register-blocks/' | relative_url }})，再回到本页补绑定与渲染部分。

## Quick API

| 方法 | 何时使用 |
| --- | --- |
| `.blockEntity("id", factory)` | 开始一个 BlockEntity 注册链。 |
| `.validBlock(...)` | 绑定单个宿主 Block。 |
| `.validBlocks(...)` | 绑定多个共享同一实现的宿主 Block。 |
| `.renderer(...)` | 懒加载 client renderer。 |
| `.register()` | 完成注册。 |

## 相关链接

- [Content Guides]({{ '/content-guides/' | relative_url }})
- [注册 Blocks]({{ '/register-blocks/' | relative_url }})