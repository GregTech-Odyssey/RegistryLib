package com.gto.registrylibtest.recipe;

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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * 祭坛配方：将物品丢在祭坛上方即可转化。
 *
 * <p>
 * Altar recipe: throw an item on top of the altar block to transform it.
 */
public class AltarRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient inputItem;
    private final ItemStackTemplate result;
    private final int processingTime;

    public AltarRecipe(Ingredient inputItem, ItemStackTemplate result, int processingTime) {
        this.inputItem = inputItem;
        this.result = result;
        this.processingTime = processingTime;
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

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return inputItem.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
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
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return SimpleRecipeExample.ALTAR.getSerializer();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return SimpleRecipeExample.ALTAR.get();
    }

    // === Codec & StreamCodec ===

    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(AltarRecipe::getInputItem),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(AltarRecipe::getResult),
                    com.mojang.serialization.Codec.INT
                            .optionalFieldOf("processing_time", 60)
                            .forGetter(AltarRecipe::getProcessingTime))
                    .apply(inst, AltarRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            AltarRecipe::getInputItem,
            ItemStackTemplate.STREAM_CODEC,
            AltarRecipe::getResult,
            ByteBufCodecs.INT,
            AltarRecipe::getProcessingTime,
            AltarRecipe::new);
}
