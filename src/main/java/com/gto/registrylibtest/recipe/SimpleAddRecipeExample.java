package com.gto.registrylibtest.recipe;

import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/**
 * 使用 {@code core.addRecipe()} 向原版（或第三方）配方类型注入配方的示例。
 *
 * <p>
 * Demonstrates adding recipes to vanilla (or third-party) recipe types via {@link
 * com.gto.registrylib.RegistryCore#addRecipe}. No new RecipeType is registered — the JSON files
 * will use the original type's identifier.
 *
 * <p>
 * For registering a new custom RecipeType with {@code .recipeType()}, see {@link
 * SimpleRecipeExample}.
 */
public class SimpleAddRecipeExample {

    // Force static initialization
    public static final Object INIT = null;

    static {
        // Add a smelting recipe: cobblestone → amethyst shard
        // The recipe path will be "data/registrylibtest/recipe/smelting/amethyst_shard.json"
        RegistryLibTest.REGISTRYLIB.addRecipe(
                "smelting/amethyst_shard",
                new SmeltingRecipe(
                        new Recipe.CommonInfo(true),
                        new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.BLOCKS, ""),
                        Ingredient.of(Items.COBBLESTONE),
                        new ItemStackTemplate(Items.AMETHYST_SHARD),
                        0.5F,
                        200));

        // Add another smelting recipe using a lazy supplier
        RegistryLibTest.REGISTRYLIB.addRecipe(
                "smelting/copper_from_raw",
                () -> new SmeltingRecipe(
                        new Recipe.CommonInfo(true),
                        new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, ""),
                        Ingredient.of(Items.RAW_COPPER),
                        new ItemStackTemplate(Items.COPPER_INGOT),
                        0.7F,
                        100));
    }
}
