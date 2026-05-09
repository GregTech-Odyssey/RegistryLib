package com.gto.registrylibtest;

import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.DataComponentTypeEntry;
import com.gto.registrylib.util.entry.ItemEntry;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class ApiImprovementExample {

    public static final ItemEntry<Item> VANILLA_IRON_INGOT = RegistryLibTest.REGISTRYLIB.existingItem("minecraft:iron_ingot");
    public static final BlockEntry<Block> VANILLA_IRON_BLOCK = RegistryLibTest.REGISTRYLIB.existingBlock("minecraft:iron_block");

    public static final Component API_TOOLTIP = RegistryLibTest.REGISTRYLIB.lang(
            "tooltip.registrylibtest.api_improvement", "RegistryLib API improvement example");

    public static final DataComponentTypeEntry<String> API_NOTE = RegistryLibTest.REGISTRYLIB.dataComponentTypeEntry(
            "api_note",
            (DataComponentType.Builder<String> builder) -> builder.persistent(Codec.STRING));

    static {
        RegistryLibTest.REGISTRYLIB.lang(
                RegistryLibTest.REGISTRYLIB.locale("zh_cn"),
                "tooltip.registrylibtest.api_improvement",
                "RegistryLib API improvement example (zh_cn)");

        RegistryLibTest.REGISTRYLIB
                .itemTags()
                .add(ItemTags.BEACON_PAYMENT_ITEMS, Items.IRON_INGOT, VANILLA_IRON_INGOT.get());
        RegistryLibTest.REGISTRYLIB
                .blockTags()
                .add(BlockTags.MINEABLE_WITH_PICKAXE, Blocks.IRON_BLOCK, VANILLA_IRON_BLOCK.get());

        RegistryLibTest.REGISTRYLIB.addRecipeData(
                prov -> prov.shapeless(RecipeCategory.MISC, Items.IRON_NUGGET, 9)
                        .requires(VANILLA_IRON_INGOT)
                        .unlockedBy("has_iron_ingot", prov.has(VANILLA_IRON_INGOT))
                        .save(prov, RegistryLibTest.MOD_ID + ":api/iron_nuggets_from_existing_iron"));
    }

    private ApiImprovementExample() {}
}
