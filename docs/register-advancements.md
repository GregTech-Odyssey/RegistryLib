---
title: 注册 Advancements
nav_order: 7
parent: Content Guides
permalink: /register-advancements/
---

# 注册 Advancements

本页说明如何通过 `addDataGenerator(ProviderType.ADVANCEMENT, ...)` 把 Advancement tree 接入 RegistryLib 的 datagen 流程。

你会在这里同时处理 root、child、标题、描述、criteria 与 tab 结构，而不是把这些定义分散到多处。

## 适用场景 / 前置条件

- 你已经在使用 RegistryLib 的 datagen 管线
- 你想在一个 `Consumer` 里组织完整的 Advancement tree
- 你需要自动写入标题和描述的语言项，而不是手动维护对应 lang key

## 快速开始

最小例子通常就是一个 root 和一个 child：先保存 root，再把它作为 child 的 `.parent(...)`。

```java
static void register() {
    RegistryLibTest.REGISTRYLIB.addDataGenerator(
            ProviderType.ADVANCEMENT,
            adv -> {
                String cat = RegistryLibTest.MOD_ID;

                // root advancement
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

                // child advancement
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

下面的现有示例展示了更完整的组织方式：多个 tab、不同 `AdvancementType`，以及隐藏的 `CHALLENGE` 节点。

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

                // GOAL type
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

                // CHALLENGE type (hidden)
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

## 分步骤解释

1. 用 `addDataGenerator(ProviderType.ADVANCEMENT, adv -> { ... })` 把 Advancement 定义挂到 RegistryLib 的 datagen 流程里。
2. 每个 root 都从 `Advancement.Builder.advancement()` 开始，并在 `.display(...)` 里提供 icon、title、desc、background 与 `AdvancementType`。
3. 标题和描述优先通过 `adv.title(...)`、`adv.desc(...)` 生成，这样会同步写入 lang，而不是把文案散落成硬编码 key。
4. child 节点通过 `.parent(root)` 接到父节点下面；root 节点通常有 background，child 节点则传 `null`。
5. 最后用 `.save(adv, id)` 输出数据。`id` 的 path 不只是文件名，也会影响 tree/tab 的组织方式。

{: .note }
> root Advancement 才需要背景图；child 节点在 `.display(...)` 里把 `background` 传 `null` 即可。

## 常见模式 / 常见坑

- 一个 tab 通常对应一个 root；如果你想拆出另一条独立进度线，直接再保存一个新的 root，而不是把所有内容都塞进同一棵树。
- `adv.title(...)` 和 `adv.desc(...)` 会顺带写语言项，所以不要再为同一条 Advancement 额外手写重复 lang key。
- `TASK`、`GOAL`、`CHALLENGE` 只是展示强度和完成反馈不同，树形结构本身仍然由 `.parent(...)` 决定。
- 如果你在 child 上错误传了背景图，文档可读性会下降，也偏离当前示例的组织方式。

## Quick API

| 方法 | 何时使用 |
| --- | --- |
| `addDataGenerator(ProviderType.ADVANCEMENT, ...)` | 注册 Advancement datagen 入口。 |
| `adv.title(...)` | 生成标题并写入 lang。 |
| `adv.desc(...)` | 生成描述并写入 lang。 |
| `.parent(...)` | 把节点挂到父 Advancement 下。 |
| `.save(...)` | 输出 Advancement 并拿到 `AdvancementHolder`。 |

## 相关链接

- [Content Guides]({{ '/content-guides/' | relative_url }})
- [Lang System]({{ '/lang-system/' | relative_url }})
- [Development and Maintenance]({{ '/development-and-maintenance/' | relative_url }})
