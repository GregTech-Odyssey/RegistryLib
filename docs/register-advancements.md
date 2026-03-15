---
title: 注册 Advancements
nav_order: 5
parent: 内容指南
permalink: /register-advancements/
---

# 注册 Advancements

本页解决“如何通过 RegistryLib 的 datagen 管线组织 Advancement tree，并自动生成标题与描述语言项”的问题。

## 适用场景 / 前置条件

- 你已经在使用 RegistryLib 的 datagen 流程。
- 你希望在一个 `Consumer` 里组织 root、child、criteria 与文案。
- 你不想手动维护 Advancement 标题和描述对应的 lang key。

## 快速开始

```java
static void register() {
    RegistryLibTest.REGISTRYLIB.addDataGenerator(
            ProviderType.ADVANCEMENT,
            adv -> {
                String cat = RegistryLibTest.MOD_ID;

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

这个结构已经足够表达“一个 tab 下的基础树形推进关系”。

## 完整示例

```java
static void register() {
    RegistryLibTest.REGISTRYLIB.addDataGenerator(
            ProviderType.ADVANCEMENT,
            adv -> {
                String cat = RegistryLibTest.MOD_ID;

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

                Advancement.Builder.advancement()
                        .parent(advRoot)
                        .display(
                                FullBlockExample.TIMER_TIER_3.get().asItem(),
                                adv.title(cat, "advanced/build_timer", "Time Lord"),
                                adv.desc(cat, "advanced/build_timer", "Craft a Tier 3 Timer"),
                                null,
                                AdvancementType.CHALLENGE,
                                true, true, true)
                        .addCriterion("has_timer_3",
                                InventoryChangeTrigger.TriggerInstance.hasItems(
                                        FullBlockExample.TIMER_TIER_3.get().asItem()))
                        .save(adv, Identifier.fromNamespaceAndPath(
                                cat, "advanced/build_timer"));
            });
}
```

## 分步骤解释

1. `addDataGenerator(ProviderType.ADVANCEMENT, adv -> { ... })` 把 Advancement 定义挂到 RegistryLib 的 datagen 流程里。
2. 每个 root 节点都从 `Advancement.Builder.advancement()` 开始，并在 `display(...)` 中设置 icon、title、desc、background 与 `AdvancementType`。
3. 标题和描述优先通过 `adv.title(...)`、`adv.desc(...)` 生成，这样会同步写入 lang。
4. child 节点通过 `parent(root)` 接到父节点下面；root 节点通常有背景图，child 通常传 `null`。
5. 最终用 `save(adv, id)` 输出数据。`id` 的 path 会影响树的组织方式。

{: .note }
> root Advancement 才需要背景图；child 节点通常把 `background` 传 `null` 即可。

## 常见模式 / 常见坑

- 一个 tab 通常对应一个 root。如果你想拆出另一条独立进度线，直接再保存一个新的 root。
- `adv.title(...)` 和 `adv.desc(...)` 会顺带写语言项，所以不要再为同一条 Advancement 额外维护重复 lang key。
- `TASK`、`GOAL`、`CHALLENGE` 影响展示强度和奖励反馈，但树形结构仍然由 `parent(...)` 决定。

## 常用 API 速览

| 方法 | 什么时候用 |
| --- | --- |
| `addDataGenerator(ProviderType.ADVANCEMENT, ...)` | 注册 Advancement datagen 入口。 |
| `adv.title(...)` | 生成标题并写入 lang。 |
| `adv.desc(...)` | 生成描述并写入 lang。 |
| `.parent(...)` | 把节点挂到父 Advancement 下。 |
| `.save(...)` | 输出 Advancement 并拿到 `AdvancementHolder`。 |

## 相关链接

- [内容指南]({{ '/content-guides/' | relative_url }})
- [Lang System]({{ '/lang-system/' | relative_url }})
- [工程与维护]({{ '/development-and-maintenance/' | relative_url }})
