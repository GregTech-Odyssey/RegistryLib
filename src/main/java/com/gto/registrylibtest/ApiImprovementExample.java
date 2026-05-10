package com.gto.registrylibtest;

import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.util.ImageUtil;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.DataComponentTypeEntry;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylib.util.visual.BlockVisualPreset;
import com.gto.registrylib.util.visual.ItemVisualPreset;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.awt.Color;

public final class ApiImprovementExample {

    public static final ItemEntry<Item> VANILLA_IRON_INGOT = RegistryLibTest.REGISTRYLIB.existingItem("minecraft:iron_ingot");
    public static final BlockEntry<Block> VANILLA_IRON_BLOCK = RegistryLibTest.REGISTRYLIB.existingBlock("minecraft:iron_block");

    public static final Component API_TOOLTIP = RegistryLibTest.REGISTRYLIB.lang(
            "tooltip.registrylibtest.api_improvement", "RegistryLib API improvement example");

    public static final DataComponentTypeEntry<String> API_NOTE = RegistryLibTest.REGISTRYLIB.dataComponentTypeEntry(
            "api_note",
            (DataComponentType.Builder<String> builder) -> builder.persistent(Codec.STRING));

    public static final ItemEntry<Item> API_TINTED_GEM = RegistryLibTest.REGISTRYLIB
            .item("api_tinted_gem")
            .visual(ItemVisualPreset.tintedTemplate("item/api_template_gem", 0x66CCFF))
            .register();

    public static final BlockEntry<Block> API_TINTED_BLOCK = RegistryLibTest.REGISTRYLIB
            .block("api_tinted_block")
            .visual(BlockVisualPreset.constantTintedCube("block/api_template_block", 0xCC8844))
            .simpleItem()
            .register();

    static {
        RegistryLibTest.REGISTRYLIB.addDataGenerator(
                ProviderType.GENERAL_RESOURCE,
                provider -> {
                    provider.addItemTexture(
                            provider.simpleTexture(
                                    "api_template_gem",
                                    () -> ImageUtil.generateIcon(new Color(190, 190, 190), ImageUtil.STAR)));
                    provider.addBlockTexture(
                            provider.simpleTexture(
                                    "api_template_block",
                                    () -> ImageUtil.generateIcon(
                                            new Color(170, 170, 170), ImageUtil.SQUARE, new Color(80, 80, 80))));
                });

        RegistryLibTest.REGISTRYLIB.lang(
                RegistryLibTest.REGISTRYLIB.locale("zh_cn"),
                "tooltip.registrylibtest.api_improvement",
                "RegistryLib API improvement example (zh_cn)");

        RegistryLibTest.REGISTRYLIB.tooltipExisting(VANILLA_IRON_INGOT, API_TOOLTIP);
        RegistryLibTest.REGISTRYLIB.addExistingToDefaultTab(VANILLA_IRON_INGOT);

        RegistryLibTest.REGISTRYLIB
                .itemTags()
                .add(ItemTags.BEACON_PAYMENT_ITEMS, Items.DIAMOND)
                .addSuppliers(ItemTags.BEACON_PAYMENT_ITEMS, VANILLA_IRON_INGOT);
        RegistryLibTest.REGISTRYLIB
                .blockTags()
                .add(BlockTags.MINEABLE_WITH_PICKAXE, Blocks.DIAMOND_BLOCK)
                .addSuppliers(BlockTags.MINEABLE_WITH_PICKAXE, VANILLA_IRON_BLOCK);

        RegistryLibTest.REGISTRYLIB.addRecipeData(
                prov -> prov.shapeless(RecipeCategory.MISC, Items.IRON_NUGGET, 9)
                        .requires(VANILLA_IRON_INGOT)
                        .unlockedBy("has_iron_ingot", prov.has(VANILLA_IRON_INGOT))
                        .save(prov, RegistryLibTest.MOD_ID + ":api/iron_nuggets_from_existing_iron"));
    }

    public static String readApiNote(ItemStack stack) {
        return API_NOTE.getOrDefault(stack, "");
    }

    private ApiImprovementExample() {}
}
