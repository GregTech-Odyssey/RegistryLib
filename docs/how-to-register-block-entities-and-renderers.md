---
title: Register Block Entities and Renderers
nav_order: 4
permalink: /register-block-entities-and-renderers/
---

# Register Block Entities and Renderers

RegistryLib 允许你在一个流畅的注册流程中将 BlockEntity 类型绑定到方块并连接客户端渲染器。
服务端和客户端的职责清晰分离，渲染器通过惰性 Supplier 加载以避免在服务端引入客户端类。

---

## Simple Example

最基础的 BlockEntity 注册：绑定一个方块。

```java
public static final BlockEntityEntry<TimerBlockEntity> SIMPLE_TIMER_BE =
        RegistryLibTest.REGISTRYLIB.blockEntity(
                "simple_timer",
                TimerBlockEntity::new,
                be -> {
                    be.validBlock(FullBlockExample.STANDALONE_TIMER);
                });
```

---

## Full Example

使用 BlockEntityBuilder 全部 API：多方块绑定 + 客户端渲染器。

```java
public static final BlockEntityEntry<TimerBlockEntity> TIMER_BLOCK_ENTITY =
        RegistryLibTest.REGISTRYLIB.blockEntity(
                "timer",
                TimerBlockEntity::new,
                be -> {
                    // 将一个 BlockEntity 类型绑定到多个方块
                    be.validBlocks(
                            FullBlockExample.TIMER_TIER_1,
                            FullBlockExample.TIMER_TIER_2,
                            FullBlockExample.TIMER_TIER_3);

                    // 通过 supplier-of-supplier 惰性绑定客户端渲染器
                    be.renderer(() -> TimerBlockEntityRenderer::new);
                });
```

---

## API Reference

### `validBlock(Supplier<? extends Block>)`

注册一个允许承载此 BlockEntity 的方块。

```java
be.validBlock(FullBlockExample.STANDALONE_TIMER);
```

对于只与单个方块关联的 BlockEntity，使用此方法即可。

---

### `validBlocks(Supplier<? extends Block>...)`

注册多个允许承载此 BlockEntity 的方块。

```java
be.validBlocks(
        FullBlockExample.TIMER_TIER_1,
        FullBlockExample.TIMER_TIER_2,
        FullBlockExample.TIMER_TIER_3);
```

当多个方块共享同一个 BlockEntity 实现时（如多层级机器），使用此方法批量绑定。

---

### `renderer(Supplier<BlockEntityRendererProvider>)`

惰性绑定客户端渲染器。外层 `Supplier` 确保渲染器代码只在客户端环境加载，
避免服务端 ClassNotFoundException。

```java
be.renderer(() -> TimerBlockEntityRenderer::new);
```

渲染器类（如 `TimerBlockEntityRenderer`）只会在客户端被实例化，服务端运行时完全不触碰该类。
这是 NeoForge 推荐的客户端隔离模式。