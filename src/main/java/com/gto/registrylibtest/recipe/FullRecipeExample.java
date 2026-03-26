package com.gto.registrylibtest.recipe;

import com.gto.registrylib.util.ColorUtil;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;

import java.awt.Color;

/**
 * 使用全部 Recipe API 的复杂配方注册示例：注入器（Infuser）。
 *
 * <p>
 * Full custom recipe registration example: the Infuser. Demonstrates a tiered processing machine
 * where the RecipeType is complex enough to query machine tier during recipe matching.
 *
 * <p>
 * 将物品丢在注入器上方即可转化。配方 JSON 由 datagen 自动生成到 {@code data/registrylibtest/recipe/infuser_*.json}。
 * 不同等级的注入器只能处理对应等级的配方。
 *
 * <ul>
 * <li>配方类型（RecipeType + RecipeSerializer） — {@link #INFUSER}
 * <li>自定义工厂配方类型 — {@link #INFUSER_CUSTOM}
 * <li>T1 注入器 — {@link #INFUSER_T1}
 * <li>T2 注入器 — {@link #INFUSER_T2}
 * <li>方块实体 — {@link #INFUSER_BE}
 * </ul>
 *
 * <h3>机器等级（Machine Tier）</h3>
 *
 * <p>
 * {@link InfuserRecipe.InfuserInput} 携带 {@code machineTier} 字段。 {@link InfuserRecipe#matches}
 * 在匹配时检查 {@code machineTier >= requiredTier}。 不同方块（T1/T2）通过 {@link InfuserBlock#getTier()} 提供不同等级。
 */
public class FullRecipeExample {

    // ── 配方类型注册（RecipeType + RecipeSerializer） ──────────────────────

    public static final RecipeTypeEntry<InfuserRecipe> INFUSER = RegistryLibTest.REGISTRYLIB
            .<InfuserRecipe>recipeType("infuser")
            .serializer(InfuserRecipe.CODEC, InfuserRecipe.STREAM_CODEC)
            .register();

    // ── 自定义工厂配方类型注册 ────────────────────────────────────────────
    // Register with custom RecipeType and RecipeSerializer factories.
    // typeFactory: override the default RecipeType.simple(id) creation.
    // serializerFactory: provide a pre-built RecipeSerializer instead of codec+streamCodec.

    public static final RecipeTypeEntry<InfuserRecipe> INFUSER_CUSTOM = RegistryLibTest.REGISTRYLIB
            .<InfuserRecipe>recipeType("infuser_custom")
            .typeFactory(RecipeType::simple)
            .serializer(InfuserRecipe.CODEC, InfuserRecipe.STREAM_CODEC)
            .register();

    // ── 配方实例注册（数据生成） ────────────────────────────────────────────
    static {
        // === INFUSER recipes ===

        // 1) 单物品 Ingredient — 基础用法
        // Single-item Ingredient — basic usage.
        INFUSER.addRecipe(
                "infuser_coal_to_diamond",
                new InfuserRecipe(
                        Ingredient.of(Items.COAL), new ItemStackTemplate(Items.DIAMOND), 20, 10.0F, 1));
        INFUSER.addRecipe(
                "infuser_gold_to_netherite",
                new InfuserRecipe(
                        Ingredient.of(Items.GOLD_INGOT),
                        new ItemStackTemplate(Items.NETHERITE_SCRAP),
                        40,
                        25.0F,
                        2));

        // 2) CompoundIngredient（OR 逻辑）— 匹配任意一个子 Ingredient
        // CompoundIngredient (OR logic) — matches if ANY child ingredient matches.
        INFUSER.addRecipe(
                "infuser_planks_or_logs_to_stick",
                registries -> {
                    var items = registries.lookupOrThrow(Registries.ITEM);
                    return new InfuserRecipe(
                            CompoundIngredient.of(
                                    Ingredient.of(items.getOrThrow(ItemTags.PLANKS)),
                                    Ingredient.of(items.getOrThrow(ItemTags.LOGS))),
                            new ItemStackTemplate(Items.STICK, 4),
                            30,
                            5.0F,
                            1);
                });

        // 3) DifferenceIngredient（差集）— 匹配 A 但排除 B
        // DifferenceIngredient (set difference) — matches A but excludes B.
        INFUSER.addRecipe(
                "infuser_non_white_wool_to_string",
                registries -> new InfuserRecipe(
                        DifferenceIngredient.of(
                                Ingredient.of(
                                        registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.WOOL)),
                                Ingredient.of(Items.WHITE_WOOL)),
                        new ItemStackTemplate(Items.STRING, 2),
                        40,
                        8.0F,
                        1));

        // 4) DataComponentIngredient（数据组件匹配）— 匹配带特定组件的物品
        // DataComponentIngredient — matches items with specific data components.
        // partial=false: 只要物品包含指定组件且值匹配即可（不要求精确匹配所有组件）
        INFUSER.addRecipe(
                "infuser_damaged_sword_to_iron",
                new InfuserRecipe(
                        DataComponentIngredient.of(
                                false, net.minecraft.core.component.DataComponents.DAMAGE, 100, Items.IRON_SWORD),
                        new ItemStackTemplate(Items.IRON_INGOT, 2),
                        60,
                        15.0F,
                        2));

        // 5) 自定义 Ingredient（MinDurabilityIngredient）— 匹配标签内且剩余耐久 ≥ 阈值的物品
        // Custom Ingredient (MinDurabilityIngredient) — matches items in tag with enough durability.
        // This uses our custom IngredientType registered via REGISTRYLIB.ingredientType().
        INFUSER.addRecipe(
                "infuser_durable_swords_to_diamond",
                new InfuserRecipe(
                        MinDurabilityIngredient.of(ItemTags.SWORDS, 200),
                        new ItemStackTemplate(Items.DIAMOND),
                        80,
                        20.0F,
                        2));

        // === INFUSER_CUSTOM recipes (custom factory demo) ===
        // 使用自定义工厂注册的配方类型同样可以正常添加配方。
        // Recipe types registered with custom factories work the same way.
        INFUSER_CUSTOM.addRecipe(
                "custom_iron_to_gold",
                new InfuserRecipe(
                        Ingredient.of(Items.IRON_INGOT), new ItemStackTemplate(Items.GOLD_INGOT), 30, 5.0F, 1));
    }

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

    public static final BlockEntityTypeEntry<InfuserBlockEntity> INFUSER_BE = RegistryLibTest.REGISTRYLIB
            .blockEntity("infuser", InfuserBlockEntity::new)
            .validBlocks(INFUSER_T1, INFUSER_T2)
            .register();
}
