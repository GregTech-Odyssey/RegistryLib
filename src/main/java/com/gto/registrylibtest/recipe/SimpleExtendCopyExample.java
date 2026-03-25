package com.gto.registrylibtest.recipe;

import com.gto.registrylib.util.entry.ExtendRecipeEntry;
import com.gto.registrylib.util.entry.RecipeEntry;
import com.gto.registrylib.util.entry.RecipeRef;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/**
 * 最简单的 extend / copy 配方示例。
 *
 * <p>
 * Minimal extend / copy recipe example. Demonstrates:
 *
 * <ul>
 * <li>{@link RecipeRef#of(RecipeType, RecipeSerializer)} — 引用原版熔炉
 * <li>{@link com.gto.registrylib.RegistryCore#extendRecipe(RecipeRef)} — 向原版熔炉注入额外配方
 * <li>{@link com.gto.registrylib.RegistryCore#copyRecipe(String, RecipeRef)} — 新建配方类型，复用熔炉 codec
 * </ul>
 *
 * <p>
 * 配方 JSON 由 datagen 自动生成到 {@code data/registrylibtest/recipe/} 目录。
 */
public class SimpleExtendCopyExample {

    // ── RecipeRef：引用原版熔炉类型 ──────────────────────────────────────
    // Reference to vanilla smelting type. Works for any type (vanilla, NeoForge, third-party).

    private static final RecipeRef<SmeltingRecipe> SMELTING_REF = RecipeRef.of(RecipeType.SMELTING, SmeltingRecipe.SERIALIZER);

    // ── extend：向原版熔炉注入额外配方（不创建新 RecipeType） ─────────────
    // Inject additional recipes into vanilla smelting. No new RecipeType is registered.
    // Generated JSON will have "type": "minecraft:smelting" with namespace "registrylibtest".

    public static final ExtendRecipeEntry<SmeltingRecipe> EXTRA_SMELTING = RegistryLibTest.REGISTRYLIB
            .<SmeltingRecipe>extendRecipe(SMELTING_REF)
            .customRecipeData(
                    prov -> SimpleCookingRecipeBuilder.smelting(
                            Ingredient.of(Items.COBBLESTONE),
                            RecipeCategory.MISC,
                            CookingBookCategory.BLOCKS,
                            Items.AMETHYST_SHARD,
                            0.5F,
                            200)
                            .unlockedBy("has_cobblestone", prov.has(Items.COBBLESTONE))
                            .save(prov, prov.safeKey(Items.AMETHYST_SHARD)));

    // ── copy：创建新 RecipeType，复用熔炉 codec ──────────────────────────
    // Create a new RecipeType "registrylibtest:electric_smelting" that reuses the
    // vanilla SmeltingRecipe codec. Recipes can be added using any addRecipe variant.

    public static final RecipeEntry<SmeltingRecipe> ELECTRIC_SMELTING = RegistryLibTest.REGISTRYLIB.<SmeltingRecipe>copyRecipe("electric_smelting", SMELTING_REF);

    static {
        // 向 copy 出的新类型添加一条配方（使用 addRecipe 确保 JSON "type" 正确）
        // Add a recipe to the copied type via addRecipe (ensures correct JSON "type" field).
        ELECTRIC_SMELTING.addRecipe(
                "copper_ingot",
                new SmeltingRecipe(
                        new Recipe.CommonInfo(true),
                        new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, ""),
                        Ingredient.of(Items.RAW_COPPER),
                        new ItemStackTemplate(Items.COPPER_INGOT),
                        0.7F,
                        100));
    }
}
