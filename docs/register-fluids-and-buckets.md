---
title: 注册 Fluids 和 Buckets
nav_order: 6
parent: Content Guides
permalink: /register-fluids-and-buckets/
---

# 注册 Fluids 和 Buckets

本页说明如何在 RegistryLib 里用一条 FluidBuilder 链同时注册 flowing fluid、客户端渲染设置、fluid block 与 bucket item。

如果你的 mod 有化学液体、熔融金属或其他批量流体内容，这套写法比手动拆多个注册点更容易扩展。

## 适用场景 / 前置条件

- 你已经准备好了 still / flow 纹理 `Identifier`
- 你希望 fluid、bucket、block 在一个注册链里完成联动配置
- 你需要调整渲染 tint、物理参数或 bucket 显示名

## 快速开始

下面是最小可用版本：使用 RegistryLib 自带灰度流体纹理，并在客户端注册对应的 `clientExtension(...)`。

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

        这条链已经把显示名和客户端纹理表现绑定好了，适合先确认最基础的流体注册是否生效。

        ## 完整示例

        下面保留现有文档中的两个完整例子：一个展示灰度纹理 + 运行时 tint，另一个展示复用 vanilla 水纹理的写法。

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON =
        RegistryLibTest.REGISTRYLIB
                .fluid("molten_iron", FLUID_STILL, FLUID_FLOW)
                .properties(p -> p.density(3000).viscosity(6000).temperature(1800))
                .lang("Molten Iron")
                .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400)
## 分步骤解释

1. 用 `.fluid("id", still, flow)` 开始注册链，先确定 registry name 和基础纹理路径。
2. 用 `.lang(...)` 设置流体显示名；如果你使用的是灰度纹理，再用 `.clientExtension(still, flow, color)` 在客户端施加运行时 tint。
3. 用 `.properties(...)` 调整 `FluidType.Properties`，把密度、粘度、温度和光照等物理特征集中到同一处。
4. 用 `.block(...)` 配置 fluid block，用 `.bucket(...)` 配置 bucket item。大多数情况下，这两个子构建器已经足够把 source、block、bucket 的关联关系串起来。
5. 用 `.tag(...)` 给 source fluid 与 flowing fluid 同时打 tag；最后以 `.register()` 收尾。

{: .note }
> 三参数 `clientExtension(still, flow, color)` 的 `color` 使用 ARGB，例如 `0xFFFF4400`。这种写法最适合灰度流体纹理的运行时着色。

## 常见模式 / 常见坑

- 如果你用的是 RegistryLib 自带灰度纹理，优先选择带颜色参数的 `clientExtension(...)`；如果你复用的是原本就带颜色的信息纹理，例如 vanilla 水纹理，就用两参数版本。
- `block(...)` 和 `bucket(...)` 会处理大多数常见联动；除非你确实要干预 `BaseFlowingFluid.Properties` 细节，否则通常不需要额外写 `fluidProperties(...)`。
- `tag(...)` 会同时作用于 source fluid 与 flowing fluid，所以不需要分别配置两次。
- 想让流体不能放置或没有桶时，再使用 `noBlock()`、`noBucket()`；不要把它们和对应的子构建器混在一起保留。

## Quick API

| 方法 | 何时使用 |
| --- | --- |
| `.fluid("id", still, flow)` | 开始一个流体注册链。 |
| `.clientExtension(...)` | 指定客户端纹理与可选 tint。 |
| `.properties(...)` | 配置 `FluidType.Properties`。 |
| `.block(...)` | 定义 fluid block 的子配置。 |
| `.bucket(...)` | 定义 bucket item 的子配置。 |

## 相关链接

- [Content Guides]({{ '/content-guides/' | relative_url }})
- [注册 Blocks]({{ '/register-blocks/' | relative_url }})
- [Lang System]({{ '/lang-system/' | relative_url }})

```java
fluid.tag(FluidTags.LAVA);
```

Tags are applied to both the source fluid and the flowing fluid.

---

### `removeTag(TagKey<Fluid>...)`

Removes previously added fluid tags.

```java
fluid.removeTag(FluidTags.LAVA);
```

---

## Texture Conventions

The default textures provided by RegistryLib are located at:
- `registrylib:block/fluid/liquid_still` — still texture (greyscale)
- `registrylib:block/fluid/liquid_flow` — flowing texture (greyscale)

These are single-channel greyscale images designed for runtime tinting via `clientExtension(still, flow, color)`. When using a custom coloured texture (e.g. vanilla water), use the two-argument overload instead.