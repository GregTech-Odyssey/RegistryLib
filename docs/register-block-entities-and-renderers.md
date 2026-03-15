---
title: 注册 Block Entities 和 Renderers
nav_order: 3
parent: 内容指南
permalink: /register-block-entities-and-renderers/
---

# 注册 Block Entities 和 Renderers

本页解决“如何把 `BlockEntityType`、可承载它的 Block，以及客户端 renderer 放进同一条注册链里”的问题。

## 适用场景 / 前置条件

- 你已经有至少一个已注册的 Block 可作为宿主。
- 你要把一个 `BlockEntityType` 绑定到一个或多个 Block。
- 你可能还需要在 client 侧绑定 renderer。

## 快速开始

```java
public static final BlockEntityEntry<TimerBlockEntity> SIMPLE_TIMER_BE =
        RegistryLibTest.REGISTRYLIB
                .blockEntity("simple_timer", TimerBlockEntity::new)
                .validBlock(FullBlockExample.STANDALONE_TIMER)
                .register();
```

只要 `validBlock(...)` 已声明，RegistryLib 就知道这个 `BlockEntityType` 可以附着在哪些 Block 上。

## 完整示例

```java
public static final BlockEntityEntry<TimerBlockEntity> TIMER_BLOCK_ENTITY =
        RegistryLibTest.REGISTRYLIB
                .blockEntity("timer", TimerBlockEntity::new)
                .validBlocks(
                        FullBlockExample.TIMER_TIER_1,
                        FullBlockExample.TIMER_TIER_2,
                        FullBlockExample.TIMER_TIER_3)
                .renderer(() -> TimerBlockEntityRenderer::new)
                .register();
```

## 分步骤解释

1. `blockEntity("id", factory)` 开始注册链，工厂创建具体的 `BlockEntity`。
2. `validBlock(...)` 或 `validBlocks(...)` 指定宿主方块；没有这一步，`BlockEntityType` 不知道自己能附着到哪里。
3. 只需要服务端逻辑时，可以直接 `.register()`；需要渲染时，再追加 `.renderer(...)`。
4. `renderer(() -> TimerBlockEntityRenderer::new)` 的外层 `Supplier` 用于延迟加载 client class。

{: .important }
> renderer 相关类型必须继续保持在 `Supplier` 后面。不要把 client class 提前实例化，也不要把它放进会在服务端执行的初始化路径里。

## 常见模式 / 常见坑

- 一个实现只服务一个 Block 时，优先用 `validBlock(...)`；多个 tier 或多个外观共用一套实现时，再用 `validBlocks(...)`。
- 如果宿主 Block 自己都还没理顺，不要先急着绑定 BlockEntity；先把 Block 的注册链稳定下来。
- renderer 是可选的，但只要你写了 renderer，就必须保持 client-only 的惰性加载方式。

## 常用 API 速览

| 方法 | 什么时候用 |
| --- | --- |
| `.blockEntity("id", factory)` | 开始一个 BlockEntity 注册链。 |
| `.validBlock(...)` | 绑定单个宿主 Block。 |
| `.validBlocks(...)` | 绑定多个共享宿主。 |
| `.renderer(...)` | 懒加载 client renderer。 |
| `.register()` | 完成注册。 |

## 相关链接

- [内容指南]({{ '/content-guides/' | relative_url }})
- [注册 Blocks]({{ '/register-blocks/' | relative_url }})
- [故障排查]({{ '/troubleshooting/' | relative_url }})