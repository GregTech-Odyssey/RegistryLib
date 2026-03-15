---
title: Register Advancements
nav_order: 6
permalink: /register-advancements/
---

# Register Advancements

RegistryLib 通过 `addDataGenerator(ProviderType.ADVANCEMENT, ...)` 将成就进度树接入数据生成流程。
你可以在一个 Consumer 中定义完整的进度树结构，包含多个 Tab、多层级父子关系和国际化标题/描述。

---

## Simple Example

最基础的成就注册：一个根成就 + 一个子成就。

```java
static void register() {
    RegistryLibTest.REGISTRYLIB.addDataGenerator(
            ProviderType.ADVANCEMENT,
            adv -> {
                String cat = RegistryLibTest.MOD_ID;

                // 根成就
                AdvancementHolder root = Advancement.Builder.advancement()
                        .display(
                                Items.CRAFTING_TABLE,
                                adv.title(cat, "simple/root", "Getting Started"),
                                adv.desc(cat, "simple/root", "Obtain a crafting table"),
                                Identifier.withDefaultNamespace(
                                        "textures/gui/advancements/backgrounds/stone.png"),
                                AdvancementType.TASK,
                                false, false, false)
                        .addCriterion(
                                "has_crafting_table",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        Items.CRAFTING_TABLE))
                        .save(adv, Identifier.fromNamespaceAndPath(cat, "simple/root"));

                // 子成就
                Advancement.Builder.advancement()
                        .parent(root)
                        .display(
                                SimpleItemExample.COPPER_COIN.get(),
                                adv.title(cat, "simple/get_coin", "First Coin"),
                                adv.desc(cat, "simple/get_coin", "Pick up a Copper Coin"),
                                null,
                                AdvancementType.TASK,
                                true, true, false)
                        .addCriterion(
                                "has_coin",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        SimpleItemExample.COPPER_COIN.get()))
                        .save(adv, Identifier.fromNamespaceAndPath(cat, "simple/get_coin"));
            });
}
```

---

## Full Example

多 Tab、多类型（TASK / GOAL / CHALLENGE）、隐藏成就的完整示例。

```java
static void register() {
    RegistryLibTest.REGISTRYLIB.addDataGenerator(
            ProviderType.ADVANCEMENT,
            adv -> {
                String cat = RegistryLibTest.MOD_ID;

                // ── Tab 1: Basics ──
                AdvancementHolder root = Advancement.Builder.advancement()
                        .display(
                                SimpleItemExample.COPPER_COIN.get(),
                                adv.title(cat, "basics/root", "RegistryCore Basics"),
                                adv.desc(cat, "basics/root", "Getting started"),
                                Identifier.withDefaultNamespace(
                                        "textures/gui/advancements/backgrounds/stone.png"),
                                AdvancementType.TASK,
                                false, false, false)
                        .addCriterion("has_crafting_table",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        Items.CRAFTING_TABLE))
                        .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/root"));

                // GOAL 类型
                Advancement.Builder.advancement()
                        .parent(root)
                        .display(
                                FullItemExample.MAGIC_WAND.get(),
                                adv.title(cat, "basics/get_magic_wand", "Arcane Discovery"),
                                adv.desc(cat, "basics/get_magic_wand", "Craft a Magic Wand"),
                                null,
                                AdvancementType.GOAL,
                                true, true, false)
                        .addCriterion("has_magic_wand",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        FullItemExample.MAGIC_WAND.get()))
                        .save(adv, Identifier.fromNamespaceAndPath(
                                cat, "basics/get_magic_wand"));

                // ── Tab 2: Advanced ──
                AdvancementHolder advRoot = Advancement.Builder.advancement()
                        .display(
                                FullBlockExample.MAGIC_ORE.get().asItem(),
                                adv.title(cat, "advanced/root", "Advanced Crafting"),
                                adv.desc(cat, "advanced/root", "Explore advanced features"),
                                Identifier.withDefaultNamespace(
                                        "textures/gui/advancements/backgrounds/nether.png"),
                                AdvancementType.TASK,
                                false, false, false)
                        .addCriterion("has_iron",
                                InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/root"));

                // CHALLENGE 类型（隐藏）
                Advancement.Builder.advancement()
                        .parent(advRoot)
                        .display(
                                FullBlockExample.TIMER_TIER_3.get().asItem(),
                                adv.title(cat, "advanced/build_timer", "Time Lord"),
                                adv.desc(cat, "advanced/build_timer", "Craft a Tier 3 Timer"),
                                null,
                                AdvancementType.CHALLENGE,
                                true, true, true)    // hidden = true
                        .addCriterion("has_timer_3",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        FullBlockExample.TIMER_TIER_3.get().asItem()))
                        .save(adv, Identifier.fromNamespaceAndPath(
                                cat, "advanced/build_timer"));
            });
}
```

---

## API Reference

### `addDataGenerator(ProviderType.ADVANCEMENT, Consumer)`

将成就进度注册到 RegistryCore 的数据生成流程。Consumer 接收 `RegistryLibAdvancementProvider`。

```java
RegistryLibTest.REGISTRYLIB.addDataGenerator(
        ProviderType.ADVANCEMENT,
        adv -> { /* 在此定义进度树 */ });
```

在 `RegistryLibTest` 的 `static` 块中调用一次即可。

---

### `adv.title(String category, String name, String title)`

为成就生成国际化标题，自动写入语言文件并返回 `MutableComponent`。

```java
adv.title(cat, "basics/root", "RegistryCore Basics")
```

语言键格式：`advancements.<category>.<name>.title`

---

### `adv.desc(String category, String name, String desc)`

为成就生成国际化描述，自动写入语言文件并返回 `MutableComponent`。

```java
adv.desc(cat, "basics/root", "Getting started with RegistryCore")
```

语言键格式：`advancements.<category>.<name>.description`

---

### `Advancement.Builder.advancement()`

创建一个成就构建器。使用原版 Minecraft 的 Advancement API。

```java
Advancement.Builder.advancement()
        .display(icon, title, desc, background, type, toast, announce, hidden)
        .addCriterion(name, trigger)
        .save(consumer, id);
```

---

### `AdvancementType`

成就类型决定了边框样式和完成提示：

| 类型 | 说明 | 边框 |
| --- | --- | --- |
| `TASK` | 普通任务 | 方形边框 |
| `GOAL` | 目标（较难） | 圆角边框 |
| `CHALLENGE` | 挑战（最难） | 尖角边框 |

---

### `.display()` 参数说明

```java
.display(
    icon,        // ItemStack 或 Item — 图标
    title,       // Component — 标题
    desc,        // Component — 描述
    background,  // Identifier — 背景纹理（仅根成就需要，子成就传 null）
    type,        // AdvancementType — TASK / GOAL / CHALLENGE
    showToast,   // boolean — 完成时是否弹出提示
    announceChat,// boolean — 完成时是否在聊天栏公告
    hidden       // boolean — 是否隐藏（完成前不可见）
)
```

---

### `.parent(AdvancementHolder)`

设置父成就，形成进度树层级关系。根成就不需要父节点。

```java
Advancement.Builder.advancement()
        .parent(root)
        // ...
```

---

### `.save(Consumer<AdvancementHolder>, Identifier)`

保存成就到数据生成器，返回 `AdvancementHolder` 供后续子成就引用。

```java
.save(adv, Identifier.fromNamespaceAndPath(cat, "basics/root"));
```

`Identifier` 的路径决定了成就 JSON 文件的位置和 Tab 归属（相同前缀为同一 Tab）。
