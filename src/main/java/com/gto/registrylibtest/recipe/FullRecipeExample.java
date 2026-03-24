package com.gto.registrylibtest.recipe;

import java.awt.Color;

import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibRecipeProvider;
import com.gto.registrylib.util.ColorUtil;
import com.gto.registrylib.util.ImageUtil;
import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

/**
 * 使用全部 Recipe API 的复杂配方注册示例：注入器（Infuser）。
 *
 * <p>Full custom recipe registration example: the Infuser. Demonstrates a tiered processing machine
 * where the RecipeType is complex enough to query machine tier during recipe matching.
 *
 * <p>将物品丢在注入器上方即可转化。配方 JSON 位于 {@code data/registrylibtest/recipe/infuser_*.json}。 不同等级的注入器只能处理对应等级的配方。
 *
 * <ul>
 *   <li>RecipeType — {@link #INFUSER_TYPE}
 *   <li>RecipeSerializer — {@link #INFUSER_SERIALIZER}
 *   <li>T1 注入器 — {@link #INFUSER_T1}
 *   <li>T2 注入器 — {@link #INFUSER_T2}
 *   <li>方块实体 — {@link #INFUSER_BE}
 * </ul>
 *
 * <h3>机器等级（Machine Tier）</h3>
 *
 * <p>{@link InfuserRecipe.InfuserInput} 携带 {@code machineTier} 字段。 {@link InfuserRecipe#matches}
 * 在匹配时检查 {@code machineTier >= requiredTier}。 不同方块（T1/T2）通过 {@link InfuserBlock#getTier()}
 * 提供不同等级。
 */
public class FullRecipeExample {

    // ── RecipeType 注册 ────────────────────────────────────────────────────

    public static final RegistryEntry<RecipeType<?>, RecipeType<InfuserRecipe>> INFUSER_TYPE =
            RegistryLibTest.REGISTRYLIB.recipeType("infuser");

    // ── RecipeSerializer 注册 ──────────────────────────────────────────────

    public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<InfuserRecipe>> INFUSER_SERIALIZER =
            RegistryLibTest.REGISTRYLIB.recipeSerializer(
                    "infuser",
                    () -> new RecipeSerializer<>(InfuserRecipe.CODEC, InfuserRecipe.STREAM_CODEC));

    // ── T1 注入器方块 ─────────────────────────────────────────────────────

    public static final BlockEntry<InfuserBlock> INFUSER_T1 = RegistryLibTest.REGISTRYLIB
            .block("infuser_t1", p -> new InfuserBlock(p, 1))
            .initialProperties(Blocks.IRON_BLOCK)
            .properties(p -> p.strength(3.0F, 6.0F))
            .lang("Infuser Tier 1")
            .lang(ModRegistryCore.LANG_ZH_CN, "注入器 T1")
            .simpleItem()
            .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
            .texture(
                    () -> ImageUtil.generateIcon(
                            ColorUtil.generateRandomMutedColor(), ImageUtil.SQUARE, Color.YELLOW))
            .register();

    // ── T2 注入器方块 ─────────────────────────────────────────────────────

    public static final BlockEntry<InfuserBlock> INFUSER_T2 = RegistryLibTest.REGISTRYLIB
            .block("infuser_t2", p -> new InfuserBlock(p, 2))
            .initialProperties(Blocks.DIAMOND_BLOCK)
            .properties(p -> p.strength(5.0F, 8.0F))
            .lang("Infuser Tier 2")
            .lang(ModRegistryCore.LANG_ZH_CN, "注入器 T2")
            .simpleItem()
            .addTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
            .texture(
                    () -> ImageUtil.generateIcon(
                            ColorUtil.generateRandomMutedColor(), ImageUtil.SQUARE, Color.MAGENTA))
            .register();

    // ── 方块实体（共享） ────────────────────────────────────────────────────

    public static final BlockEntityEntry<InfuserBlockEntity> INFUSER_BE = RegistryLibTest.REGISTRYLIB
            .blockEntity("infuser", InfuserBlockEntity::new)
            .validBlocks(INFUSER_T1, INFUSER_T2)
            .register();

    // ── 配方数据生成 ────────────────────────────────────────────────────────

    static {
        RegistryLibTest.REGISTRYLIB.addDataGenerator(
                ProviderType.RECIPE, (RegistryLibRecipeProvider prov) -> {
                    prov.accept(
                            ResourceKey.create(
                                    Registries.RECIPE,
                                    Identifier.fromNamespaceAndPath(
                                            RegistryLibTest.MOD_ID, "infuser_coal_to_diamond")),
                            new InfuserRecipe(
                                    Ingredient.of(Items.COAL),
                                    new ItemStack(Items.DIAMOND),
                                    200,
                                    10.0F,
                                    1),
                            null);

                    prov.accept(
                            ResourceKey.create(
                                    Registries.RECIPE,
                                    Identifier.fromNamespaceAndPath(
                                            RegistryLibTest.MOD_ID, "infuser_gold_to_netherite")),
                            new InfuserRecipe(
                                    Ingredient.of(Items.GOLD_INGOT),
                                    new ItemStack(Items.NETHERITE_SCRAP),
                                    400,
                                    25.0F,
                                    2),
                            null);
                });
    }
}
