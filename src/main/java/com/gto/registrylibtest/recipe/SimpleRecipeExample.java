package com.gto.registrylibtest.recipe;

import java.awt.Color;

import com.gto.registrylib.util.ImageUtil;
import com.gto.registrylib.util.entry.BlockEntityTypeEntry;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.RecipeTypeEntry;
import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

/**
 * 最简单的自定义配方注册示例：祭坛（Altar）。
 *
 * <p>
 * Simple custom recipe registration example: the Altar. Demonstrates registering a custom
 * RecipeType + RecipeSerializer via {@code .recipeType()}, then adding recipe instances via {@link
 * RecipeTypeEntry#addRecipe}.
 *
 * <p>
 * 丢物品到祭坛上方即可转化。配方 JSON 由 datagen 自动生成到 {@code data/registrylibtest/recipe/altar_*.json}。
 *
 * <ul>
 * <li>配方类型（RecipeType + RecipeSerializer） — {@link #ALTAR}
 * <li>处理方块 — {@link #ALTAR_BLOCK}
 * <li>处理方块实体 — {@link #ALTAR_BE}
 * </ul>
 */
public class SimpleRecipeExample {

    // ── 配方类型注册（RecipeType + RecipeSerializer） ──────────────────────
    // Register RecipeType and RecipeSerializer.

    public static final RecipeTypeEntry<AltarRecipe> ALTAR = RegistryLibTest.REGISTRYLIB
            .<AltarRecipe>recipeType("altar")
            .serializer(AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC)
            .register();

    // ── 配方实例注册（数据生成） ────────────────────────────────────────────
    // Add individual recipes via the entry. Recipe JSON is generated during datagen.
    static {
        // 1) 单物品 Ingredient — 最基础的用法
        // Single-item Ingredient — the most basic usage.
        ALTAR.addRecipe(
                "altar_cobblestone_to_stone",
                new AltarRecipe(Ingredient.of(Items.COBBLESTONE), new ItemStackTemplate(Items.STONE), 40));
        ALTAR.addRecipe(
                "altar_raw_iron_to_ingot",
                new AltarRecipe(
                        Ingredient.of(Items.RAW_IRON), new ItemStackTemplate(Items.IRON_INGOT), 80));

        // 2) 多物品 Ingredient — 匹配多个物品中的任意一个
        // Multi-item Ingredient — matches any of the listed items.
        ALTAR.addRecipe(
                "altar_fuel_to_torch",
                new AltarRecipe(
                        Ingredient.of(Items.COAL, Items.CHARCOAL), new ItemStackTemplate(Items.TORCH), 30));

        // 3) 标签 Ingredient — 匹配 Tag 中的所有物品（使用 registries 查找 Tag）
        // Tag-based Ingredient — matches all items in the tag (via registries lookup).
        ALTAR.addRecipe(
                "altar_logs_to_charcoal",
                registries -> new AltarRecipe(
                        Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.LOGS)),
                        new ItemStackTemplate(Items.CHARCOAL),
                        60));
    }

    // ── 方块 ───────────────────────────────────────────────────────────────
    // A simple altar block that processes items thrown on top.

    public static final BlockEntry<AltarBlock> ALTAR_BLOCK = RegistryLibTest.REGISTRYLIB
            .block("altar", AltarBlock::new)
            .initialProperties(Blocks.STONE)
            .properties(p -> p.strength(2.0F, 6.0F))
            .lang("Altar")
            .lang(ModRegistryCore.LANG_ZH_CN, "祭坛")
            .texture(() -> ImageUtil.generateIcon(new Color(178, 122, 210), ImageUtil.SQUARE, new Color(70, 52, 84)))
            .simpleItem()
            .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
            .register();

    // ── 方块实体 ────────────────────────────────────────────────────────────

    public static final BlockEntityTypeEntry<AltarBlockEntity> ALTAR_BE = RegistryLibTest.REGISTRYLIB
            .blockEntity("altar", AltarBlockEntity::new)
            .validBlock(ALTAR_BLOCK)
            .register();
}
