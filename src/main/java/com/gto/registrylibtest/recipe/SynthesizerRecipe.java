package com.gto.registrylibtest.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

/**
 * 合成器配方：需要多个物品输入（带数量检查）和一个流体输入。
 *
 * <p>
 * Synthesizer recipe: requires multiple item inputs (with count checking via {@link SizedIngredient})
 * and one fluid input (with amount checking via {@link SizedFluidIngredient}).
 *
 * <p>
 * 这是多输入配方的典型示例。每个物品输入通过 {@link SizedIngredient} 同时检查物品类型和数量，
 * 流体输入通过 {@link SizedFluidIngredient} 检查流体类型和数量。
 *
 * <h3>示例 JSON</h3>
 *
 * <pre>{@code
 * {
 *   "type": "registrylibtest:synthesizer",
 *   "ingredients": [
 *     { "ingredient": "minecraft:iron_ingot", "count": 3 },
 *     { "ingredient": "minecraft:gold_ingot", "count": 2 }
 *   ],
 *   "fluid": {
 *     "ingredient": "minecraft:water",
 *     "amount": 1000
 *   },
 *   "result": { "id": "minecraft:diamond" },
 *   "processing_time": 200,
 *   "experience": 30.0
 * }
 * }</pre>
 */
public class SynthesizerRecipe implements Recipe<SynthesizerRecipe.SynthesizerInput> {

    private final List<SizedIngredient> ingredients;
    private final SizedFluidIngredient fluidIngredient;
    private final ItemStackTemplate result;
    private final int processingTime;
    private final float experience;

    public SynthesizerRecipe(
                             List<SizedIngredient> ingredients,
                             SizedFluidIngredient fluidIngredient,
                             ItemStackTemplate result,
                             int processingTime,
                             float experience) {
        this.ingredients = List.copyOf(ingredients);
        this.fluidIngredient = fluidIngredient;
        this.result = result;
        this.processingTime = processingTime;
        this.experience = experience;
    }

    public List<SizedIngredient> getIngredients() {
        return ingredients;
    }

    public SizedFluidIngredient getFluidIngredient() {
        return fluidIngredient;
    }

    public ItemStackTemplate getResult() {
        return result;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public float getExperience() {
        return experience;
    }

    @Override
    public boolean matches(SynthesizerInput input, Level level) {
        if (input.items().size() != ingredients.size()) return false;
        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).test(input.items().get(i))) return false;
        }
        return fluidIngredient.test(input.fluid());
    }

    @Override
    public ItemStack assemble(SynthesizerInput input) {
        return result.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SynthesizerInput>> getSerializer() {
        return MultiInputRecipeExample.SYNTHESIZER.getSerializer();
    }

    @Override
    public RecipeType<? extends Recipe<SynthesizerInput>> getType() {
        return MultiInputRecipeExample.SYNTHESIZER.getType();
    }

    // === Custom RecipeInput ===

    /**
     * 合成器配方输入：携带多个物品槽位和一个流体槽位。
     *
     * <p>
     * Synthesizer recipe input: carries multiple item stacks (one per slot) and a fluid stack.
     * Each item slot is positionally matched against the recipe's ingredient list.
     */
    public record SynthesizerInput(List<ItemStack> items, FluidStack fluid) implements RecipeInput {

        @Override
        public ItemStack getItem(int slot) {
            if (slot < 0 || slot >= items.size())
                throw new IllegalArgumentException("No item for index " + slot);
            return items.get(slot);
        }

        @Override
        public int size() {
            return items.size();
        }
    }

    // === Codec & StreamCodec ===

    public static final MapCodec<SynthesizerRecipe> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    SizedIngredient.NESTED_CODEC.listOf().fieldOf("ingredients")
                            .forGetter(SynthesizerRecipe::getIngredients),
                    SizedFluidIngredient.CODEC.fieldOf("fluid")
                            .forGetter(SynthesizerRecipe::getFluidIngredient),
                    ItemStackTemplate.CODEC.fieldOf("result")
                            .forGetter(SynthesizerRecipe::getResult),
                    Codec.INT
                            .optionalFieldOf("processing_time", 200)
                            .forGetter(SynthesizerRecipe::getProcessingTime),
                    Codec.FLOAT
                            .optionalFieldOf("experience", 0.0F)
                            .forGetter(SynthesizerRecipe::getExperience))
                    .apply(inst, SynthesizerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SynthesizerRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SynthesizerRecipe::getIngredients,
            SizedFluidIngredient.STREAM_CODEC,
            SynthesizerRecipe::getFluidIngredient,
            ItemStackTemplate.STREAM_CODEC,
            SynthesizerRecipe::getResult,
            ByteBufCodecs.INT,
            SynthesizerRecipe::getProcessingTime,
            ByteBufCodecs.FLOAT,
            SynthesizerRecipe::getExperience,
            SynthesizerRecipe::new);
}
