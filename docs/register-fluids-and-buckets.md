---
title: Register Fluids and Buckets
nav_order: 6
permalink: /register-fluids-and-buckets/
---

# Register Fluids and Buckets

RegistryLib 的流体注册将 flowing fluid、视觉设置、流体方块和桶物品集中在一个 Builder 流程中。
对于有大量化学物质、熔融材料或自定义液体的 Mod，这使得流体内容更容易扩展。

---

## Simple Example

最基础的流体注册：使用内置灰度纹理 + 语言。

```java
private static final Identifier FLUID_STILL =
        Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_still");
private static final Identifier FLUID_FLOW =
        Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_flow");

public static final FluidEntry<BaseFlowingFluid.Flowing> ACID =
        RegistryLibTest.REGISTRYLIB.fluid(
                "acid",
                FLUID_STILL,
                FLUID_FLOW,
                fluid -> {
                    fluid.lang("Acid")
                            .clientExtension(FLUID_STILL, FLUID_FLOW);
                });
```

---

## Full Example

使用 FluidBuilder 全部 API 的流体示例，包含着色、物理参数、方块/桶配置、标签。

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON =
        RegistryLibTest.REGISTRYLIB.fluid(
                "molten_iron",
                FLUID_STILL,
                FLUID_FLOW,
                fluid -> {
                    fluid.properties(p -> p.density(3000).viscosity(6000).temperature(1800));
                    fluid.lang("Molten Iron");
                    fluid.clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400);
                    fluid.tag(FluidTags.LAVA);
                    fluid.block(block -> block.properties(p -> p.lightLevel(s -> 12)));
                    fluid.bucket(bucket -> bucket.lang("Molten Iron Bucket"));
                });

public static final FluidEntry<BaseFlowingFluid.Flowing> LIQUID_MAGIC =
        RegistryLibTest.REGISTRYLIB.fluid(
                "liquid_magic",
                Identifier.withDefaultNamespace("block/water_still"),
                Identifier.withDefaultNamespace("block/water_flow"),
                fluid -> {
                    fluid.properties(p -> p.lightLevel(15).density(500).viscosity(200));
                    fluid.lang("Liquid Magic");
                    fluid.block(block -> block.properties(p -> p.lightLevel(s -> 15)));
                    fluid.bucket(bucket -> bucket.lang("Liquid Magic Bucket"));
                });
```

---

## API Reference

### `clientExtension(Identifier, Identifier)`

设置流体的静止和流动纹理，不着色。

```java
fluid.clientExtension(FLUID_STILL, FLUID_FLOW);
```

使用 registrylib 内置的灰度纹理时，不传颜色则保持原色。

---

### `clientExtension(Identifier, Identifier, int)`

设置纹理并以 ARGB 颜色着色。适合使用灰度纹理配合运行时着色。

```java
fluid.clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400);
```

颜色格式为 ARGB（如 `0xFFFF4400` = 不透明橙红色）。着色同时会应用到桶物品模型。

---

### `clientExtension(Supplier<Supplier<IClientFluidTypeExtensions>>)`

完全自定义客户端流体渲染扩展，供高级用法使用。

```java
fluid.clientExtension(() -> () -> new IClientFluidTypeExtensions() {
    // 自定义实现
});
```

---

### `properties(Consumer<FluidType.Properties>)`

配置 FluidType 的物理参数：密度、粘度、温度、光照等级等。

```java
fluid.properties(p -> p.density(3000).viscosity(6000).temperature(1800));
```

这些参数影响流体的浮力计算、流动速度显示和工具提示信息。

---

### `fluidProperties(Consumer<BaseFlowingFluid.Properties>)`

配置 BaseFlowingFluid 层级的属性（如手动关联 source/block/bucket）。

```java
fluid.fluidProperties(p -> p.slopeFindDistance(4).levelDecreasePerBlock(1));
```

大多数情况下不需要手动调用，`block()` 和 `bucket()` 会自动关联。

---

### `lang(String)`

设置流体英文显示名称。

```java
fluid.lang("Molten Iron");
```

---

### `defaultLang()`

从注册名自动推导英文名（如 `molten_iron` → `Molten Iron`）。

```java
fluid.defaultLang();
```

---

### `source(Function<BaseFlowingFluid.Properties, ? extends BaseFlowingFluid>)`

设置自定义源流体工厂。

```java
fluid.source(BaseFlowingFluid.Source::new);
```

默认已自动调用 `defaultSource()`，仅在需要自定义源流体类时使用。

---

### `defaultSource()`

使用默认的 `BaseFlowingFluid.Source` 作为源流体。

```java
fluid.defaultSource();
```

---

### `block(Consumer<BlockBuilder<LiquidBlock, FluidBuilder>>)`

配置流体方块子条目。在 Consumer 内可自定义方块属性。

```java
fluid.block(block -> block.properties(p -> p.lightLevel(s -> 15)));
```

可用于设置自发光、爆炸抗性等方块特有属性。

---

### `block(BiFunction, Consumer)`

使用自定义 LiquidBlock 子类工厂 + Consumer 配置。

```java
fluid.block(MyLiquidBlock::new, block -> { /* 配置 */ });
```

---

### `noBlock()`

禁用流体方块生成。流体将无法在世界中放置为方块。

```java
fluid.noBlock();
```

---

### `defaultBlock()`

使用默认流体方块设置。

```java
fluid.defaultBlock();
```

---

### `bucket(Consumer<ItemBuilder<BucketItem, FluidBuilder>>)`

配置桶物品子条目。可在 Consumer 内设置显示名、标签页等。

```java
fluid.bucket(bucket -> bucket.lang("Molten Iron Bucket"));
```

---

### `bucket(BiFunction, Consumer)`

使用自定义 BucketItem 子类工厂 + Consumer 配置。

```java
fluid.bucket(MyBucket::new, bucket -> { /* 配置 */ });
```

---

### `noBucket()`

禁用桶物品生成。

```java
fluid.noBucket();
```

---

### `defaultBucketTab(ResourceKey<CreativeModeTab>)`

设置桶物品的默认创造标签页。

```java
fluid.defaultBucketTab(CreativeModeTabs.TOOLS_AND_UTILITIES);
```

---

### `tag(TagKey<Fluid>...)`

给流体添加标签。

```java
fluid.tag(FluidTags.LAVA);
```

标签会同时应用到源流体和流动流体。

---

### `removeTag(TagKey<Fluid>...)`

移除已添加的流体标签。

```java
fluid.removeTag(FluidTags.LAVA);
```

---

## 纹理约定

RegistryLib 提供的默认纹理位于：
- `registrylib:block/fluid/liquid_still` — 静止纹理（灰度图）
- `registrylib:block/fluid/liquid_flow` — 流动纹理（灰度图）

这些是单通道灰度图，适合通过 `clientExtension(still, flow, color)` 进行运行时着色。
如果使用自定义彩色纹理（如原版水纹理），传 2 参数版本即可。