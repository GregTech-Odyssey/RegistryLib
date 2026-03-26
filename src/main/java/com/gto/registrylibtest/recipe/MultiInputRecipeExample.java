package com.gto.registrylibtest.recipe;

import com.gto.registrylib.util.entry.RecipeTypeEntry;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

/**
 * 多输入配方注册示例：合成器（Synthesizer）。
 *
 * <p>
 * Multi-input recipe registration example: the Synthesizer. Demonstrates a crafting machine that
 * requires multiple item inputs (with count checking via {@link SizedIngredient}) and one fluid
 * input (with amount checking via {@link SizedFluidIngredient}).
 *
 * <p>
 * 合成器需要多个物品槽位 + 一个流体槽位，配方定义了每个槽位所需的物品类型/数量和流体类型/数量。 这是模组中常见的多输入机器配方的典型实现模式。
 *
 * <h3>核心要点 / Key Concepts</h3>
 *
 * <ul>
 * <li>{@link SizedIngredient} — 包装 {@link Ingredient} + 数量， {@link SizedIngredient#test}
 * 同时检查物品类型和 {@code stack.getCount() >= count}
 * <li>{@link SizedFluidIngredient} — 包装 {@link FluidIngredient} + 数量（mB）， {@link
 * SizedFluidIngredient#test} 同时检查流体类型和 {@code stack.getAmount() >= amount}
 * <li>自定义 {@link net.minecraft.world.item.crafting.RecipeInput} 携带多个物品 + 流体
 * </ul>
 */
public class MultiInputRecipeExample {

    // ── 配方类型注册（RecipeType + RecipeSerializer） ──────────────────────

    public static final RecipeTypeEntry<SynthesizerRecipe> SYNTHESIZER = RegistryLibTest.REGISTRYLIB
            .<SynthesizerRecipe>recipeType("synthesizer")
            .serializer(SynthesizerRecipe.CODEC, SynthesizerRecipe.STREAM_CODEC)
            .register();

    // ── 配方实例注册（数据生成） ────────────────────────────────────────────
    static {
        // 1) 基础多输入 + 流体 — 3 个铁锭 + 2 个金锭 + 1000mB 水 -> 钻石
        // Basic multi-input + fluid: 3 iron ingots + 2 gold ingots + 1000mB water -> diamond
        SYNTHESIZER.addRecipe(
                "synthesizer_iron_gold_water_to_diamond",
                new SynthesizerRecipe(
                        List.of(
                                SizedIngredient.of(Items.IRON_INGOT, 3), SizedIngredient.of(Items.GOLD_INGOT, 2)),
                        SizedFluidIngredient.of(Fluids.WATER, 1000),
                        new ItemStackTemplate(Items.DIAMOND),
                        200,
                        30.0F));

        // 2) 自定义 Ingredient + 多输入 + 流体
        // 1 把耐久 ≥ 200 的剑（MinDurabilityIngredient） + 4 个绿宝石 + 500mB 熔岩 -> 下界合金锭
        // Custom ingredient + multi-input + fluid:
        // 1 sword with durability >= 200 (MinDurabilityIngredient) + 4 emeralds + 500mB lava ->
        // netherite ingot
        SYNTHESIZER.addRecipe(
                "synthesizer_durable_sword_to_netherite",
                new SynthesizerRecipe(
                        List.of(
                                new SizedIngredient(MinDurabilityIngredient.of(ItemTags.SWORDS, 200), 1),
                                SizedIngredient.of(Items.EMERALD, 4)),
                        SizedFluidIngredient.of(Fluids.LAVA, 500),
                        new ItemStackTemplate(Items.NETHERITE_INGOT),
                        400,
                        50.0F));
    }
}
