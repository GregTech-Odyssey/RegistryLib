---
title: Register Blocks
nav_order: 4
permalink: /register-blocks/
---

# Register Blocks

RegistryLib 的方块注册使用与物品相同的流畅 Builder 风格。方块定义、属性、战利品表、
BlockItem 绑定和 Group 共享配置可以集中在一处完成。

---

## Simple Example

最基础的方块注册：一个方块 + 语言 + 自动 BlockItem。

```java
public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB.block(
        "decorative_stone",
        Block::new,
        block -> {
            block.initialProperties(() -> Blocks.STONE)
                    .lang("Decorative Stone")
                    .simpleItem();
        });
```

---

## Full Example

使用 BlockBuilder 全部 API 的方块示例，包含自定义子类、Group 系统等。

```java
// ── 使用全部 API 的单方块 ──
public static final BlockEntry<Block> MAGIC_ORE = RegistryLibTest.REGISTRYLIB.block(
        "magic_ore",
        Block::new,
        block -> {
            block.initialProperties(() -> Blocks.IRON_ORE);
            block.properties(p -> p.strength(4.0F, 5.0F).requiresCorrectToolForDrops());
            block.lang("Magic Ore");
            block.loot((tables, b) ->
                    tables.add(b, tables.createOreDrop(b, SimpleItemExample.COPPER_COIN.get())));
            block.tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL);
            block.recipe((ctx, prov) -> { /* 配方生成 */ });
            block.item(itemBuilder -> {
                itemBuilder.tooltip((collector, stack) -> {
                    collector.node(
                            new SubNode.Basic(Component.literal("§5Drops coins when mined")),
                            true, false);
                });
            });
        });

// ── 自定义方块子类 ──
public static final BlockEntry<TimerBlock> STANDALONE_TIMER = RegistryLibTest.REGISTRYLIB.block(
        "standalone_timer",
        p -> new TimerBlock(p, 4),
        block -> {
            block.initialProperties(() -> Blocks.IRON_BLOCK)
                    .lang("Standalone Timer")
                    .defaultLoot()
                    .defaultBlockstate()
                    .simpleItem();
        });

// ── Group 系统：批量共享配置 ──
public static final Group TIMER_GROUP = RegistryLibTest.REGISTRYLIB
        .group("timers")
        .langPrefix("Timer")
        .blockProperties(p -> p.strength(5.0F, 6.0F))
        .build();

public static final BlockEntry<TimerBlock> TIMER_TIER_1 = TIMER_GROUP.block(
        "tier_1",
        p -> new TimerBlock(p, 1),
        block -> {
            block.initialProperties(() -> Blocks.IRON_BLOCK)
                    .item(itemBuilder -> itemBuilder.tooltip((collector, stack) -> {
                        collector.node(new SubNode.Basic(Component.literal("§aTier 1"), 0), true, false);
                        collector.node(new SubNode.Basic(Component.literal("§7Tick interval: 20"), 10));
                    }));
        });
```

---

## API Reference

### `initialProperties(Supplier<? extends Block>)`

从另一个已有方块复制属性作为基础。

```java
block.initialProperties(() -> Blocks.IRON_ORE);
```

等价于 `BlockBehaviour.Properties.ofFullCopy(block)`，常用于让新方块继承原版方块的硬度、声音、工具等级等。

---

### `properties(UnaryOperator<BlockBehaviour.Properties>)`

在已有属性基础上追加修改。可多次调用，效果叠加。

```java
block.properties(p -> p.strength(4.0F, 5.0F).requiresCorrectToolForDrops());
```

---

### `lang(String)`

设置方块英文显示名称，自动写入语言文件。

```java
block.lang("Magic Ore");
```

---

### `defaultLang()`

从注册名自动推导英文名（如 `magic_ore` → `Magic Ore`）。

```java
block.defaultLang();
```

---

### `simpleItem()`

使用默认设置自动生成对应的 BlockItem，无需额外配置。

```java
block.simpleItem();
```

适合装饰方块、简单材料方块等不需要自定义 BlockItem 的场景。

---

### `item(Consumer<ItemBuilder<BlockItem, BlockBuilder>>)`

创建 BlockItem 子条目并通过 Consumer 配置（Tooltip、标签页等）。

```java
block.item(itemBuilder -> {
    itemBuilder.tooltip(Component.literal("§5Drops coins when mined"));
});
```

---

### `item(BiFunction, Consumer)`

使用自定义 BlockItem 工厂 + Consumer 配置。

```java
block.item(MyBlockItem::new, itemBuilder -> {
    itemBuilder.lang("Custom Block Item");
});
```

---

### `blockEntity(BlockEntityFactory, Consumer)`

内联创建 BlockEntity 子条目，无需单独调用 `RegistryCore.blockEntity()`。

```java
block.blockEntity(MyBlockEntity::new, be -> {
    be.renderer(() -> MyRenderer::new);
});
```

---

### `defaultBlockstate()`

使用默认的六面立方体方块状态模型。

```java
block.defaultBlockstate();
```

---

### `blockstate(Supplier<BiConsumer<DataGenContext, RegistryLibBlockModelGenerator>>)`

自定义方块状态和模型生成逻辑。

```java
block.blockstate(() -> (ctx, prov) -> {
    prov.createTrivialCube(ctx.getEntry());
});
```

---

### `defaultLoot()`

使用默认战利品表（方块自身掉落）。

```java
block.defaultLoot();
```

---

### `loot(BiConsumer<RegistryLibBlockLootTables, T>)`

自定义战利品表生成逻辑。

```java
block.loot((tables, b) ->
        tables.add(b, tables.createOreDrop(b, Items.DIAMOND)));
```

适合矿石掉落、丝绸之触判断等自定义掉落逻辑。

---

### `recipe(BiConsumer<DataGenContext, RegistryLibRecipeProvider>)`

通过 DataGen 生成配方。

```java
block.recipe((ctx, prov) -> {
    // ShapedRecipeBuilder 等配方生成逻辑
});
```

---

### `tag(TagKey<Block>...)`

给方块添加一个或多个标签。

```java
block.tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL);
```

---

## Group 系统

Group 允许多个方块共享一组默认配置（属性、语言前缀等），减少重复代码。

### 创建 Group

```java
public static final Group TIMER_GROUP = RegistryLibTest.REGISTRYLIB
        .group("timers")
        .langPrefix("Timer")
        .blockProperties(p -> p.strength(5.0F, 6.0F))
        .build();
```

### 使用 Group 注册方块

```java
public static final BlockEntry<TimerBlock> TIMER_TIER_1 = TIMER_GROUP.block(
        "tier_1",
        p -> new TimerBlock(p, 1),
        block -> { /* 个性化配置 */ });
```

Group 还提供 `item()`、`blockEntity()`、`fluid()` 入口，当你的 Mod 有大量同系列内容时非常适用。