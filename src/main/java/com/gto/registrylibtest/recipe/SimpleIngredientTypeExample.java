package com.gto.registrylibtest.recipe;

import com.gto.registrylib.util.entry.IngredientTypeEntry;
import com.gto.registrylibtest.RegistryLibTest;

/**
 * 最简单的自定义 Ingredient 类型注册示例。
 *
 * <p>
 * Simple custom ingredient type registration example. Registers a {@link MinDurabilityIngredient}
 * that matches items within a tag whose remaining durability exceeds a given threshold.
 *
 * <h3>How it works</h3>
 *
 * <ol>
 * <li>Implement {@link net.neoforged.neoforge.common.crafting.ICustomIngredient} in your ingredient class
 *     ({@link MinDurabilityIngredient})</li>
 * <li>Define {@code CODEC} (MapCodec) and optionally {@code STREAM_CODEC} in that class</li>
 * <li>Register via {@code REGISTRYLIB.ingredientType("name", MyIngredient.CODEC)}</li>
 * <li>Override {@code getType()} in your ingredient to return the registered type</li>
 * <li>Use {@code new MyIngredient(...).toVanilla()} in recipes</li>
 * </ol>
 *
 * <h3>Usage in Recipes</h3>
 *
 * <pre>{@code
 * // In recipe registration:
 * INFUSER.addRecipe("recycle_durable_swords", new InfuserRecipe(
 *         MinDurabilityIngredient.of(ItemTags.SWORDS, 100),
 *         new ItemStackTemplate(Items.IRON_INGOT, 3), 60, 10.0F, 1));
 * }</pre>
 *
 * <h3>Generated JSON ingredient</h3>
 *
 * <pre>{@code
 * {
 *   "neoforge:ingredient_type": "registrylibtest:min_durability",
 *   "tag": "minecraft:swords",
 *   "min_durability": 100
 * }
 * }</pre>
 */
public class SimpleIngredientTypeExample {

    // ── Ingredient Type 注册 ─────────────────────────────────────────────────
    // Register our custom IngredientType. One line is all it takes.

    public static final IngredientTypeEntry<MinDurabilityIngredient> MIN_DURABILITY =
            RegistryLibTest.REGISTRYLIB.ingredientType(
                    "min_durability",
                    MinDurabilityIngredient.CODEC,
                    MinDurabilityIngredient.STREAM_CODEC);
}
