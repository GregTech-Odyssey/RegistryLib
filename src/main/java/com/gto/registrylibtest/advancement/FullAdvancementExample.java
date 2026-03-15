package com.gto.registrylibtest.advancement;

import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylibtest.RegistryLibTest;
import com.gto.registrylibtest.block.FullBlockExample;
import com.gto.registrylibtest.item.FullItemExample;
import com.gto.registrylibtest.item.SimpleItemExample;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/**
 * 使用 Advancement API 全部功能的复杂示例。
 *
 * <p>
 * 涵盖：多个 Tab（背景不同）/ 三种 AdvancementType（TASK / GOAL / CHALLENGE）/ adv.title() 与 adv.desc() 国际化 /
 * 多层级进度树 / 隐藏成就。
 *
 * <pre>
 * Tab 1 — RegistryCore Basics
 * [basics/root] ── [basics/get_coin] ── [basics/get_magic_wand]
 *
 * Tab 2 — Advanced Crafting
 * [advanced/root] ── [advanced/mine_magic_ore] (GOAL)
 *                └── [advanced/build_timer]    (CHALLENGE, hidden)
 * </pre>
 */
public class FullAdvancementExample {

    public static void register() {
        RegistryLibTest.REGISTRYLIB.addDataGenerator(
                ProviderType.ADVANCEMENT,
                adv -> {
                    String cat = RegistryLibTest.MOD_ID;

                    // ── Tab 1: RegistryCore Basics ──────────────────────────────

                    AdvancementHolder root = Advancement.Builder.advancement()
                            .display(
                                    SimpleItemExample.COPPER_COIN.get(),
                                    adv.title(cat, "basics/root", "RegistryCore Basics"),
                                    adv.desc(cat, "basics/root", "Getting started with RegistryCore"),
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

                    // TASK：获得铜币
                    AdvancementHolder getCoin = Advancement.Builder.advancement()
                            .parent(root)
                            .display(
                                    SimpleItemExample.COPPER_COIN.get(),
                                    adv.title(cat, "basics/get_coin", "First Coin"),
                                    adv.desc(cat, "basics/get_coin", "Obtain a Copper Coin"),
                                    null,
                                    AdvancementType.TASK,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_coin",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(
                                            SimpleItemExample.COPPER_COIN.get()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/get_coin"));

                    // GOAL：获得魔法棒
                    Advancement.Builder.advancement()
                            .parent(getCoin)
                            .display(
                                    FullItemExample.MAGIC_WAND.get(),
                                    adv.title(cat, "basics/get_magic_wand", "Arcane Discovery"),
                                    adv.desc(cat, "basics/get_magic_wand", "Craft a Magic Wand"),
                                    null,
                                    AdvancementType.GOAL,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_magic_wand",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(FullItemExample.MAGIC_WAND.get()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "basics/get_magic_wand"));

                    // ── Tab 2: Advanced Crafting ─────────────────────────────────

                    AdvancementHolder advRoot = Advancement.Builder.advancement()
                            .display(
                                    FullBlockExample.MAGIC_ORE.get().asItem(),
                                    adv.title(cat, "advanced/root", "Advanced Crafting"),
                                    adv.desc(cat, "advanced/root", "Explore advanced features"),
                                    Identifier.withDefaultNamespace(
                                            "textures/gui/advancements/backgrounds/nether.png"),
                                    AdvancementType.TASK,
                                    false,
                                    false,
                                    false)
                            .addCriterion(
                                    "has_iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/root"));

                    // GOAL：挖到魔法矿石
                    Advancement.Builder.advancement()
                            .parent(advRoot)
                            .display(
                                    FullBlockExample.MAGIC_ORE.get().asItem(),
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
                                            FullBlockExample.MAGIC_ORE.get().asItem()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/mine_magic_ore"));

                    // CHALLENGE（隐藏）：获得三级计时器
                    Advancement.Builder.advancement()
                            .parent(advRoot)
                            .display(
                                    FullBlockExample.TIMER_TIER_3.get().asItem(),
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
                                            FullBlockExample.TIMER_TIER_3.get().asItem()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/build_timer"));
                });
    }
}
