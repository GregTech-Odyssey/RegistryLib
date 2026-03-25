package com.gto.registrylibtest.recipe;

import com.gto.registrylib.util.entry.ExtendRecipeEntry;
import com.gto.registrylib.util.entry.RecipeEntry;
import com.gto.registrylib.util.entry.RecipeRef;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/**
 * 使用全部 extend / copy API 的复杂配方示例。
 *
 * <p>
 * Full extend / copy recipe example that exercises every API method. Demonstrates:
 *
 * <ul>
 * <li>{@link RecipeRef#of(RecipeType, RecipeSerializer)} — 引用原版类型（熔炉、高炉）
 * <li>{@link RecipeRef#of(RecipeEntry)} — 引用 mod 自己注册的类型（祭坛）
 * <li>{@link ExtendRecipeEntry#addRecipe(String, Object)} — 直接实例
 * <li>{@link ExtendRecipeEntry#addRecipe(String, java.util.function.Supplier)} — 延迟创建
 * <li>{@link ExtendRecipeEntry#addRecipe(String, java.util.function.Function)} — 需要 registries
 * <li>{@link ExtendRecipeEntry#customRecipeData} — 自定义 datagen 回调
 * <li>{@link com.gto.registrylib.RegistryCore#copyRecipe} — 复制原版 / mod 配方类型
 * <li>{@link RecipeEntry#getRegisteredRecipes()} — 获取已注册配方列表（用于跨 Entry 镜像）
 * </ul>
 *
 * <h3>测试的配方类型</h3>
 *
 * <ul>
 * <li>原版熔炉 {@code minecraft:smelting} — extend + copy
 * <li>原版高炉 {@code minecraft:blasting} — extend
 * <li>mod 祭坛 {@code registrylibtest:altar} — extend + copy（跨 Entry 引用）
 * </ul>
 */
public class FullExtendCopyExample {

    // ══════════════════════════════════════════════════════════════════════
    // 1. RecipeRef 创建方式
    // ══════════════════════════════════════════════════════════════════════

    // 1a) 引用原版类型 — RecipeRef.of(RecipeType, RecipeSerializer)
    // Reference a vanilla type.
    private static final RecipeRef<SmeltingRecipe> SMELTING_REF = RecipeRef.of(RecipeType.SMELTING, SmeltingRecipe.SERIALIZER);

    private static final RecipeRef<BlastingRecipe> BLASTING_REF = RecipeRef.of(RecipeType.BLASTING, BlastingRecipe.SERIALIZER);

    // 1b) 引用 mod 自己注册的类型 — RecipeRef.of(RecipeEntry)
    // Reference our own mod's type (registered in SimpleRecipeExample).
    private static final RecipeRef<AltarRecipe> ALTAR_REF = RecipeRef.of(SimpleRecipeExample.ALTAR);

    // ══════════════════════════════════════════════════════════════════════
    // 2. extend 原版熔炉 — 覆盖所有 API
    // ══════════════════════════════════════════════════════════════════════

    public static final ExtendRecipeEntry<SmeltingRecipe> EXTENDED_SMELTING = RegistryLibTest.REGISTRYLIB
            .<SmeltingRecipe>extendRecipe(SMELTING_REF)

            // 2a) customRecipeData — 使用 SimpleCookingRecipeBuilder 生成标准熔炉配方
            // customRecipeData with SimpleCookingRecipeBuilder for standard smelting recipe.
            .customRecipeData(
                    prov -> SimpleCookingRecipeBuilder.smelting(
                            Ingredient.of(Items.DIRT),
                            RecipeCategory.MISC,
                            CookingBookCategory.BLOCKS,
                            Items.CLAY_BALL,
                            0.1F,
                            200)
                            .unlockedBy("has_dirt", prov.has(Items.DIRT))
                            .save(prov, prov.safeKey(Items.CLAY_BALL)));

    // ══════════════════════════════════════════════════════════════════════
    // 3. extend 原版高炉 — customRecipeData
    // ══════════════════════════════════════════════════════════════════════

    public static final ExtendRecipeEntry<BlastingRecipe> EXTENDED_BLASTING = RegistryLibTest.REGISTRYLIB
            .<BlastingRecipe>extendRecipe(BLASTING_REF)
            .customRecipeData(
                    prov -> SimpleCookingRecipeBuilder.blasting(
                            Ingredient.of(Items.GRAVEL),
                            RecipeCategory.MISC,
                            CookingBookCategory.MISC,
                            Items.FLINT,
                            0.3F,
                            100)
                            .unlockedBy("has_gravel", prov.has(Items.GRAVEL))
                            .save(prov, prov.safeKey(Items.FLINT)));

    // ══════════════════════════════════════════════════════════════════════
    // 4. extend mod 祭坛 — 覆盖所有 addRecipe 重载
    // ══════════════════════════════════════════════════════════════════════

    public static final ExtendRecipeEntry<AltarRecipe> EXTENDED_ALTAR = RegistryLibTest.REGISTRYLIB
            .<AltarRecipe>extendRecipe(ALTAR_REF)

            // 4a) addRecipe(String, T) — 直接实例
            // Direct recipe instance.
            .addRecipe(
                    "extend_altar_sand_to_glass",
                    new AltarRecipe(Ingredient.of(Items.SAND), new ItemStackTemplate(Items.GLASS), 50))

            // 4b) addRecipe(String, Supplier<T>) — 延迟创建
            // Lazy supplier (useful when the recipe depends on late-bound values).
            .addRecipe(
                    "extend_altar_gravel_to_flint",
                    () -> new AltarRecipe(
                            Ingredient.of(Items.GRAVEL), new ItemStackTemplate(Items.FLINT), 30))

            // 4c) addRecipe(String, Function<HolderLookup.Provider, T>) — 需要 registries
            // Registry-aware factory (for tag-based ingredients).
            .addRecipe(
                    "extend_altar_planks_to_stick",
                    registries -> new AltarRecipe(
                            Ingredient.of(
                                    registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.PLANKS)),
                            new ItemStackTemplate(Items.STICK, 4),
                            40));

    // ══════════════════════════════════════════════════════════════════════
    // 5. copy 原版熔炉 → 新类型 "advanced_smelting"
    // ══════════════════════════════════════════════════════════════════════

    // 创建新的 RecipeType "registrylibtest:advanced_smelting"，复用 SmeltingRecipe 的 codec
    // Create new RecipeType reusing SmeltingRecipe's codec.
    public static final RecipeEntry<SmeltingRecipe> ADVANCED_SMELTING = RegistryLibTest.REGISTRYLIB.<SmeltingRecipe>copyRecipe("advanced_smelting", SMELTING_REF);

    static {
        // 5a) addRecipe — 直接实例
        // Add recipes via addRecipe (ensures correct JSON "type" field).
        ADVANCED_SMELTING.addRecipe(
                "nether_brick",
                new SmeltingRecipe(
                        new Recipe.CommonInfo(true),
                        new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.BLOCKS, ""),
                        Ingredient.of(Items.NETHERRACK),
                        new ItemStackTemplate(Items.NETHER_BRICK),
                        0.1F,
                        100));

        // 5b) customRecipeData — 使用 SimpleCookingRecipeBuilder 验证 "type" 字段也能正确生成
        // customRecipeData with SimpleCookingRecipeBuilder: "type" must be
        // "registrylibtest:advanced_smelting", NOT "minecraft:smelting".
        ADVANCED_SMELTING.customRecipeData(
                prov -> SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Items.COBBLESTONE),
                        RecipeCategory.BUILDING_BLOCKS,
                        CookingBookCategory.BLOCKS,
                        Items.STONE,
                        0.1F,
                        100)
                        .unlockedBy("has_cobblestone", prov.has(Items.COBBLESTONE))
                        .save(prov, prov.safeKey(Items.STONE)));
    }

    // ══════════════════════════════════════════════════════════════════════
    // 6. copy mod 祭坛 → 新类型 "dark_altar"
    // ══════════════════════════════════════════════════════════════════════

    // 从 mod 自己的 AltarRecipe 创建新的 RecipeType "registrylibtest:dark_altar"
    // Copy our own AltarRecipe type to create a new "dark_altar" type.
    public static final RecipeEntry<AltarRecipe> DARK_ALTAR = RegistryLibTest.REGISTRYLIB.<AltarRecipe>copyRecipe("dark_altar", ALTAR_REF);

    static {
        // 6a) addRecipe(String, T) — 直接实例
        // Direct recipe instance.
        DARK_ALTAR.addRecipe(
                "dark_altar_bone_to_wither_rose",
                new AltarRecipe(
                        Ingredient.of(Items.BONE_BLOCK), new ItemStackTemplate(Items.WITHER_ROSE), 100));

        // 6b) addRecipe(String, Supplier<T>) — 延迟创建
        // Lazy supplier.
        DARK_ALTAR.addRecipe(
                "dark_altar_soul_sand_to_soul_torch",
                () -> new AltarRecipe(
                        Ingredient.of(Items.SOUL_SAND), new ItemStackTemplate(Items.SOUL_TORCH, 4), 60));

        // 6c) addRecipe(String, Function<HolderLookup.Provider, T>) — 需要 registries
        // Registry-aware factory.
        DARK_ALTAR.addRecipe(
                "dark_altar_flowers_to_dye",
                registries -> new AltarRecipe(
                        Ingredient.of(
                                registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.FLOWERS)),
                        new ItemStackTemplate(Items.BLACK_DYE, 2),
                        45));
    }

    // ══════════════════════════════════════════════════════════════════════
    // 7. getRegisteredRecipes() — 跨 Entry 配方镜像演示
    // ══════════════════════════════════════════════════════════════════════

    // 获取 DARK_ALTAR 上已注册的所有配方工厂列表（仅 datagen 时有内容）。
    // 这可用于将一个 Entry 的配方镜像到另一个 Entry，实现 copy + extend 联动。
    //
    // Get all recipe factories registered on DARK_ALTAR.
    // This enables mirroring recipes from one Entry to another (copy+extend linkage).
    static {
        int registeredCount = DARK_ALTAR.getRegisteredRecipes().size();
        // 在 datagen 时，registeredCount == 3（上面注册的 3 条配方）
        // During datagen, registeredCount == 3 (the 3 recipes added above).
    }
}
