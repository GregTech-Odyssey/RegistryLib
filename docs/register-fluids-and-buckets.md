---
title: 注册 Fluids 和 Buckets
nav_order: 4
parent: 内容指南
permalink: /register-fluids-and-buckets/
---

# 注册 Fluids 和 Buckets

本页解决“如何用一条 FluidBuilder 链同时管理流体类型、客户端渲染、fluid block 和 bucket item”的问题。

## 适用场景 / 前置条件

- 你已经准备好了 still / flow 纹理的 `Identifier`。
- 你希望 fluid、bucket、block 在一个注册链里完成联动配置。
- 你需要调整 tint、物理参数或 bucket 的显示名。

## 快速开始

```java
private static final Identifier FLUID_STILL =
        Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_still");
private static final Identifier FLUID_FLOW =
        Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_flow");

public static final FluidEntry<BaseFlowingFluid.Flowing> ACID =
        RegistryLibTest.REGISTRYLIB
                .fluid("acid", FLUID_STILL, FLUID_FLOW)
                .lang("Acid")
                .clientExtension(FLUID_STILL, FLUID_FLOW)
                .register();
```

这条链已经把显示名和客户端纹理表现接上，适合先确认最基础的流体注册是否生效。

## 完整示例

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON =
        RegistryLibTest.REGISTRYLIB
                .fluid("molten_iron", FLUID_STILL, FLUID_FLOW)
                .properties(p -> p.density(3000).viscosity(6000).temperature(1800))
                .lang("Molten Iron")
                .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400)
                .tag(FluidTags.LAVA)
                .block(block -> block
                    .properties(p -> p.lightLevel(s -> 12))
                )
                .bucket(bucket -> bucket
                    .lang("Molten Iron Bucket")
                )
                .register();

public static final FluidEntry<BaseFlowingFluid.Flowing> LIQUID_MAGIC =
        RegistryLibTest.REGISTRYLIB
                .fluid(
                        "liquid_magic",
                        Identifier.withDefaultNamespace("block/water_still"),
                        Identifier.withDefaultNamespace("block/water_flow"))
                .properties(p -> p.lightLevel(15).density(500).viscosity(200))
                .lang("Liquid Magic")
                .block(block -> block
                    .properties(p -> p.lightLevel(s -> 15))
                )
                .bucket(bucket -> bucket
                    .lang("Liquid Magic Bucket")
                )
                .register();
```

## 分步骤解释

1. `fluid("id", still, flow)` 决定 registry name 和基础纹理路径。
2. `lang(...)` 设置显示名；如果你使用灰度纹理，再用三参数 `clientExtension(...)` 在客户端施加运行时着色。
3. `properties(...)` 调整 `FluidType.Properties`，把密度、粘度、温度和光照集中在同一处维护。
4. `block(...)` 和 `bucket(...)` 用于配置流体方块与桶物品。多数常见联动都在这里完成。
5. `tag(...)` 会同时作用于 source fluid 与 flowing fluid，最后统一 `.register()`。

{: .note }
> 三参数 `clientExtension(still, flow, color)` 的 `color` 使用 ARGB，例如 `0xFFFF4400`。它最适合灰度流体纹理的运行时着色。

## 常见模式 / 常见坑

- 如果你用的是 RegistryLib 自带灰度纹理，优先选择带颜色参数的 `clientExtension(...)`。
- 如果你复用的是本来就带颜色信息的纹理，例如 vanilla 水纹理，通常用两参数版本即可。
- `block(...)` 和 `bucket(...)` 已经覆盖大多数联动场景；除非你确实要干预底层 `BaseFlowingFluid.Properties`，否则一般不需要额外写 `fluidProperties(...)`。

## 常用 API 速览

| 方法 | 什么时候用 |
| --- | --- |
| `.fluid("id", still, flow)` | 开始一个流体注册链。 |
| `.clientExtension(...)` | 指定客户端纹理与可选 tint。 |
| `.properties(...)` | 配置 `FluidType.Properties`。 |
| `.block(...)` | 定义 fluid block 的子配置。 |
| `.bucket(...)` | 定义 bucket item 的子配置。 |

## 相关链接

- [内容指南]({{ '/content-guides/' | relative_url }})
- [注册 Blocks]({{ '/register-blocks/' | relative_url }})
- [Lang System]({{ '/lang-system/' | relative_url }})