package com.gto.registrylibtest.recipe;

import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.RecipeEntry;
import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.tags.BlockTags;
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
 * RecipeEntry#addRecipe}.
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

    public static final RecipeEntry<AltarRecipe> ALTAR = RegistryLibTest.REGISTRYLIB
            .<AltarRecipe>recipeType("altar")
            .serializer(AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC)
            .register();

    // ── 配方实例注册（数据生成） ────────────────────────────────────────────
    // Add individual recipes via the entry. Recipe JSON is generated during datagen.
    static {
        ALTAR.addRecipe(
                "altar_cobblestone_to_stone",
                new AltarRecipe(Ingredient.of(Items.COBBLESTONE), new ItemStackTemplate(Items.STONE), 40));
        ALTAR.addRecipe(
                "altar_raw_iron_to_ingot",
                new AltarRecipe(
                        Ingredient.of(Items.RAW_IRON), new ItemStackTemplate(Items.IRON_INGOT), 80));
    }

    // ── 方块 ───────────────────────────────────────────────────────────────
    // A simple altar block that processes items thrown on top.

    public static final BlockEntry<AltarBlock> ALTAR_BLOCK = RegistryLibTest.REGISTRYLIB
            .block("altar", AltarBlock::new)
            .initialProperties(Blocks.STONE)
            .properties(p -> p.strength(2.0F, 6.0F))
            .lang("Altar")
            .lang(ModRegistryCore.LANG_ZH_CN, "祭坛")
            .simpleItem()
            .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
            .register();

    // ── 方块实体 ────────────────────────────────────────────────────────────

    public static final BlockEntityEntry<AltarBlockEntity> ALTAR_BE = RegistryLibTest.REGISTRYLIB
            .blockEntity("altar", AltarBlockEntity::new)
            .validBlock(ALTAR_BLOCK)
            .register();
}
