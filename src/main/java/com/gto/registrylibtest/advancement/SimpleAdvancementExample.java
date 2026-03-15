package com.gto.registrylibtest.advancement;

import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylibtest.RegistryLibTest;
import com.gto.registrylibtest.item.SimpleItemExample;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/** 最简单的成就进度注册：一个根成就 + 一个子成就。 */
public class SimpleAdvancementExample {

    public static void register() {
        RegistryLibTest.REGISTRYLIB.addDataGenerator(
                ProviderType.ADVANCEMENT,
                adv -> {
                    String cat = RegistryLibTest.MOD_ID;

                    // 根成就：持有合成台
                    AdvancementHolder root = Advancement.Builder.advancement()
                            .display(
                                    Items.CRAFTING_TABLE,
                                    adv.title(cat, "simple/root", "Getting Started"),
                                    adv.desc(cat, "simple/root", "Obtain a crafting table"),
                                    Identifier.withDefaultNamespace(
                                            "textures/gui/advancements/backgrounds/stone.png"),
                                    AdvancementType.TASK,
                                    false,
                                    false,
                                    false)
                            .addCriterion(
                                    "has_crafting_table",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "simple/root"));

                    // 子成就：获得铜币
                    Advancement.Builder.advancement()
                            .parent(root)
                            .display(
                                    SimpleItemExample.COPPER_COIN.get(),
                                    adv.title(cat, "simple/get_coin", "First Coin"),
                                    adv.desc(cat, "simple/get_coin", "Pick up a Copper Coin"),
                                    null,
                                    AdvancementType.TASK,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_coin",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(
                                            SimpleItemExample.COPPER_COIN.get()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "simple/get_coin"));
                });
    }
}
