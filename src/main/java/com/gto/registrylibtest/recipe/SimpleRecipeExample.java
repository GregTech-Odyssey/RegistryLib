package com.gto.registrylibtest.recipe;

import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibRecipeProvider;
import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

/**
 * 最简单的自定义配方注册示例：祭坛（Altar）。
 *
 * <p>Simple custom recipe registration example: the Altar. Demonstrates registering a custom
 * RecipeType, RecipeSerializer, processing Block and BlockEntity via RegistryLib.
 *
 * <p>丢物品到祭坛上方即可转化。配方 JSON 位于 {@code data/registrylibtest/recipe/altar_*.json}。
 *
 * <ul>
 *   <li>RecipeType — {@link #ALTAR_TYPE}
 *   <li>RecipeSerializer — {@link #ALTAR_SERIALIZER}
 *   <li>处理方块 — {@link #ALTAR_BLOCK}
 *   <li>处理方块实体 — {@link #ALTAR_BE}
 * </ul>
 */
public class SimpleRecipeExample {

    // ── RecipeType 注册 ────────────────────────────────────────────────────
    // Register a RecipeType via the convenience method on RegistryCore.

    public static final RegistryEntry<RecipeType<?>, RecipeType<AltarRecipe>> ALTAR_TYPE =
            RegistryLibTest.REGISTRYLIB.recipeType("altar");

    // ── RecipeSerializer 注册 ──────────────────────────────────────────────
    // Register a RecipeSerializer via the convenience method on RegistryCore.

    public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<AltarRecipe>> ALTAR_SERIALIZER =
            RegistryLibTest.REGISTRYLIB.recipeSerializer(
                    "altar", () -> new RecipeSerializer<>(AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC));

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

    // ── 配方数据生成 ────────────────────────────────────────────────────────

    static {
        RegistryLibTest.REGISTRYLIB.addDataGenerator(
                ProviderType.RECIPE, (RegistryLibRecipeProvider prov) -> {
                    prov.accept(
                            ResourceKey.create(
                                    Registries.RECIPE,
                                    Identifier.fromNamespaceAndPath(
                                            RegistryLibTest.MOD_ID,
                                            "altar_cobblestone_to_stone")),
                            new AltarRecipe(
                                    Ingredient.of(Items.COBBLESTONE),
                                    new ItemStackTemplate(Items.STONE),
                                    40),
                            null);

                    prov.accept(
                            ResourceKey.create(
                                    Registries.RECIPE,
                                    Identifier.fromNamespaceAndPath(
                                            RegistryLibTest.MOD_ID, "altar_raw_iron_to_ingot")),
                            new AltarRecipe(
                                    Ingredient.of(Items.RAW_IRON),
                                    new ItemStackTemplate(Items.IRON_INGOT),
                                    80),
                            null);
                });
    }
}
