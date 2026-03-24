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

/**
 * 注入器配方：将物品丢在注入器上方进行处理，要求机器等级达到最低要求。
 *
 * <p>Infuser recipe: throw items on top of the infuser block. Requires a minimum machine tier to
 * process. Provides experience on completion.
 */
public class InfuserRecipe implements Recipe<InfuserRecipe.InfuserInput> {

    private final Ingredient inputItem;
    private final ItemStackTemplate result;
    private final int processingTime;
    private final float experience;
    private final int requiredTier;

    public InfuserRecipe(
                         Ingredient inputItem,
                         ItemStackTemplate result,
                         int processingTime,
                         float experience,
                         int requiredTier) {
        this.inputItem = inputItem;
        this.result = result;
        this.processingTime = processingTime;
        this.experience = experience;
        this.requiredTier = requiredTier;
    }

    public Ingredient getInputItem() {
        return inputItem;
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

    public int getRequiredTier() {
        return requiredTier;
    }

    @Override
    public boolean matches(InfuserInput input, Level level) {
        return input.machineTier() >= requiredTier && inputItem.test(input.item());
    }

    @Override
    public ItemStack assemble(InfuserInput input) {
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
    public RecipeSerializer<? extends Recipe<InfuserInput>> getSerializer() {
        return FullRecipeExample.INFUSER_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<InfuserInput>> getType() {
        return FullRecipeExample.INFUSER_TYPE.get();
    }

    // === Custom RecipeInput ===

    /**
     * 注入器配方输入：包含物品和机器等级。
     *
     * <p>Infuser recipe input: carries the item stack and the machine tier. The machine tier is
     * checked against the recipe's required tier.
     */
    public record InfuserInput(ItemStack item, int machineTier) implements RecipeInput {

        @Override
        public ItemStack getItem(int slot) {
            if (slot != 0)
                throw new IllegalArgumentException("No item for index " + slot);
            return item;
        }

        @Override
        public int size() {
            return 1;
        }
    }

    // === Codec & StreamCodec ===

    public static final MapCodec<InfuserRecipe> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(InfuserRecipe::getInputItem),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(InfuserRecipe::getResult),
                    Codec.INT
                            .optionalFieldOf("processing_time", 100)
                            .forGetter(InfuserRecipe::getProcessingTime),
                    Codec.FLOAT
                            .optionalFieldOf("experience", 0.0F)
                            .forGetter(InfuserRecipe::getExperience),
                    Codec.INT
                            .optionalFieldOf("required_tier", 1)
                            .forGetter(InfuserRecipe::getRequiredTier))
                    .apply(inst, InfuserRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfuserRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            InfuserRecipe::getInputItem,
            ItemStackTemplate.STREAM_CODEC,
            InfuserRecipe::getResult,
            ByteBufCodecs.INT,
            InfuserRecipe::getProcessingTime,
            ByteBufCodecs.FLOAT,
            InfuserRecipe::getExperience,
            ByteBufCodecs.INT,
            InfuserRecipe::getRequiredTier,
            InfuserRecipe::new);
}
