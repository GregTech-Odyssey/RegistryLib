package com.gto.registrylibtest;

import com.gto.registrylib.providers.ProviderType;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/**
 * 示例：成就进度注册
 *
 * <p>集中管理本 Mod 的所有成就树，便于统一查阅和维护。通过调用
 * {@link #register()} 将全部成就接入 RegistryCore 的数据生成流程。
 *
 * <p><b>进度树结构：</b>
 * <pre>
 * [basics/root] ── [basics/get_test_item]
 *              └── [basics/get_magic_dust]
 *
 * [advanced/root] ── [advanced/mine_magic_ore]
 *                └── [advanced/build_timer]  ★ CHALLENGE
 * </pre>
 *
 * <p><b>Tab 1 — RegistryCore Basics</b>（依赖 {@link ItemTest} 中的物品）
 * <ul>
 *   <li>{@code basics/root} — 持有合成台触发
 *   <li>{@code basics/get_test_item} — 获得 {@link ItemTest#TEST_ITEM}
 *   <li>{@code basics/get_magic_dust} — 获得 {@link ItemTest#MAGIC_DUST}
 * </ul>
 *
 * <p><b>Tab 2 — Advanced Crafting</b>（依赖 {@link BlockTest} 中的方块）
 * <ul>
 *   <li>{@code advanced/root} — 持有铁锭触发
 *   <li>{@code advanced/mine_magic_ore} — 获得 {@link BlockTest#MAGIC_ORE} 方块物品
 *   <li>{@code advanced/build_timer} — 获得 {@link BlockTest#TIMER_TIER_3} 方块物品
 * </ul>
 */
public class AdvancementTest {

    /**
     * 将所有成就注册到 {@link RegistryLibTest#REGISTRYLIB} 的数据生成器中。
     *
     * <p>在 {@link RegistryLibTest} 的 {@code static} 块中调用一次即可。
     */
    static void register() {
        RegistryLibTest.REGISTRYLIB.addDataGenerator(
                ProviderType.ADVANCEMENT,
                adv -> {
                    String cat = RegistryLibTest.MOD_ID;

                    // ── Tab 1: RegistryCore Basics ──────────────────────────────────

                    AdvancementHolder root = Advancement.Builder.advancement()
                            .display(
                                    ItemTest.TEST_ITEM.get(),
                                    adv.title(cat, "root", "RegistryCore Basics"),
                                    adv.desc(cat, "root", "Getting started with RegistryCore"),
                                    Identifier.withDefaultNamespace(
                                            "textures/gui/advancements/backgrounds/stone.png"),
                                    AdvancementType.TASK,
                                    false,
                                    false,
                                    false)
                            .addCriterion(
                                    "has_crafting_table",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/root"));

                    Advancement.Builder.advancement()
                            .parent(root)
                            .display(
                                    ItemTest.TEST_ITEM.get(),
                                    adv.title(cat, "basics/get_test_item", "First Item"),
                                    adv.desc(cat, "basics/get_test_item", "Obtain a Test Item"),
                                    null,
                                    AdvancementType.TASK,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_test_item",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(ItemTest.TEST_ITEM.get()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/get_test_item"));

                    Advancement.Builder.advancement()
                            .parent(root)
                            .display(
                                    ItemTest.MAGIC_DUST.get(),
                                    adv.title(cat, "basics/get_magic_dust", "Sparkling Discovery"),
                                    adv.desc(cat, "basics/get_magic_dust", "Find some Magic Dust"),
                                    null,
                                    AdvancementType.TASK,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_magic_dust",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(ItemTest.MAGIC_DUST.get()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/get_magic_dust"));

                    // ── Tab 2: Advanced Crafting ────────────────────────────────────

                    AdvancementHolder advRoot = Advancement.Builder.advancement()
                            .display(
                                    BlockTest.MAGIC_ORE.get().asItem(),
                                    adv.title(cat, "advanced/root", "Advanced Crafting"),
                                    adv.desc(cat, "advanced/root", "Explore advanced features of RegistryCore"),
                                    Identifier.withDefaultNamespace(
                                            "textures/gui/advancements/backgrounds/nether.png"),
                                    AdvancementType.TASK,
                                    false,
                                    false,
                                    false)
                            .addCriterion(
                                    "has_iron",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/root"));

                    Advancement.Builder.advancement()
                            .parent(advRoot)
                            .display(
                                    BlockTest.MAGIC_ORE.get().asItem(),
                                    adv.title(cat, "advanced/mine_magic_ore", "Magical Mining"),
                                    adv.desc(cat, "advanced/mine_magic_ore", "Mine a block of Magic Ore"),
                                    null,
                                    AdvancementType.GOAL,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_magic_ore",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(
                                            BlockTest.MAGIC_ORE.get().asItem()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/mine_magic_ore"));

                    Advancement.Builder.advancement()
                            .parent(advRoot)
                            .display(
                                    BlockTest.TIMER_TIER_3.get().asItem(),
                                    adv.title(cat, "advanced/build_timer", "Time Lord"),
                                    adv.desc(cat, "advanced/build_timer", "Craft a Tier 3 Timer"),
                                    null,
                                    AdvancementType.CHALLENGE,
                                    true,
                                    true,
                                    true)
                            .addCriterion(
                                    "has_timer_3",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(
                                            BlockTest.TIMER_TIER_3.get().asItem()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/build_timer"));
                });
    }
}
