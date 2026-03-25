package com.gto.registrylib.datagen.provider;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.util.DataIngredient;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

public class RegistryLibRecipeProvider extends RecipeProvider implements RecipeOutput {

    private final RegistryLibRecipeRunner runner;
    private final RecipeOutput outputDelegated;

    public RegistryLibRecipeProvider(
                                     RegistryLibRecipeRunner runner, HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.runner = runner;
        this.outputDelegated = output;
    }

    @Override
    public void buildRecipes() {
        runner.provider = this;
        runner.owner.genData(ProviderType.RECIPE, this);
        runner.provider = null;
    }

    // Delegate RecipeOutput methods
    @Override
    public void accept(
                       ResourceKey<Recipe<?>> key,
                       Recipe<?> recipe,
                       @Nullable AdvancementHolder advancement,
                       ICondition... conditions) {
        outputDelegated.accept(key, recipe, advancement, conditions);
    }

    /**
     * Accepts a recipe using a specific {@link RecipeSerializer} for the {@code "type"} field,
     * instead of the recipe's own {@code getSerializer()}. Used by {@code copyRecipe} to emit JSON
     * with the correct copied type identifier.
     *
     * <p>
     * Bypasses the standard {@link net.minecraft.data.recipes.RecipeOutput} pipeline because
     * {@code Recipe.CODEC} dispatches on {@code recipe.getSerializer()} which returns the original
     * (vanilla/source) serializer. Instead, we manually encode the recipe body using the copy
     * serializer's codec and inject the correct {@code "type"} field.
     */
    @SuppressWarnings("unchecked")
    public <T extends Recipe<?>> void acceptWithSerializer(
                                                           ResourceKey<Recipe<?>> key, T recipe, RecipeSerializer<T> serializer) {
        var ops = registries.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        // Encode the recipe body using the serializer's MapCodec (same codec as the original)
        com.google.gson.JsonElement body = ((com.mojang.serialization.MapCodec<T>) serializer.codec())
                .codec()
                .encodeStart(ops, recipe)
                .getOrThrow(
                        msg -> new IllegalStateException(
                                "Failed to encode recipe " + key.identifier() + ": " + msg));
        // Add the "type" field with the copy serializer's registry name
        Identifier serializerId = net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
        com.google.gson.JsonObject json = body.getAsJsonObject();
        json.addProperty("type", serializerId.toString());
        // Store for deferred writing (CachedOutput not available here)
        runner.deferredWrites.add(new RegistryLibRecipeRunner.DeferredRecipeWrite(key, json));
    }

    @Override
    public Advancement.Builder advancement() {
        return outputDelegated.advancement();
    }

    @Override
    public void includeRootAdvancement() {
        outputDelegated.includeRootAdvancement();
    }

    public HolderLookup.Provider registries() {
        return registries;
    }

    public Identifier safeId(Identifier id) {
        return Identifier.fromNamespaceAndPath(runner.owner.getModid(), safeName(id));
    }

    public Identifier safeId(DataIngredient source) {
        return safeId(source.getId());
    }

    public Identifier safeId(ItemLike registryEntry) {
        return safeId(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public ResourceKey<Recipe<?>> safeKey(Identifier id) {
        return ResourceKey.create(
                Registries.RECIPE, Identifier.fromNamespaceAndPath(runner.owner.getModid(), safeName(id)));
    }

    public ResourceKey<Recipe<?>> safeKey(DataIngredient source) {
        return safeKey(source.getId());
    }

    public ResourceKey<Recipe<?>> safeKey(ItemLike registryEntry) {
        return safeKey(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public String safeName(Identifier id) {
        return id.getPath().replace('/', '_');
    }

    public String safeName(DataIngredient source) {
        return safeName(source.getId());
    }

    public String safeName(ItemLike registryEntry) {
        return safeName(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public static final int DEFAULT_SMELT_TIME = 200;
    public static final int DEFAULT_BLAST_TIME = DEFAULT_SMELT_TIME / 2;
    public static final int DEFAULT_SMOKE_TIME = DEFAULT_BLAST_TIME;
    public static final int DEFAULT_CAMPFIRE_TIME = DEFAULT_SMELT_TIME * 3;

    public <T extends ItemLike, S extends AbstractCookingRecipe> void cooking(
                                                                              DataIngredient source,
                                                                              RecipeCategory category,
                                                                              CookingBookCategory cookingCategory,
                                                                              Supplier<? extends T> result,
                                                                              float experience,
                                                                              int cookingTime,
                                                                              AbstractCookingRecipe.Factory<S> factory) {
        SimpleCookingRecipeBuilder.generic(
                source.toVanilla(),
                category,
                cookingCategory,
                result.get(),
                experience,
                cookingTime,
                factory)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeId(result.get()) + "_from_" + safeName(source));
    }

    public <T extends ItemLike> void smelting(
                                              DataIngredient source,
                                              RecipeCategory category,
                                              Supplier<? extends T> result,
                                              float experience) {
        smelting(source, category, result, experience, DEFAULT_SMELT_TIME);
    }

    public <T extends ItemLike> void smelting(
                                              DataIngredient source,
                                              RecipeCategory category,
                                              Supplier<? extends T> result,
                                              float experience,
                                              int cookingTime) {
        cooking(
                source,
                category,
                CookingBookCategory.MISC,
                result,
                experience,
                cookingTime,
                SmeltingRecipe::new);
    }

    public <T extends ItemLike> void blasting(
                                              DataIngredient source,
                                              RecipeCategory category,
                                              Supplier<? extends T> result,
                                              float experience) {
        blasting(source, category, result, experience, DEFAULT_BLAST_TIME);
    }

    public <T extends ItemLike> void blasting(
                                              DataIngredient source,
                                              RecipeCategory category,
                                              Supplier<? extends T> result,
                                              float experience,
                                              int cookingTime) {
        cooking(
                source,
                category,
                CookingBookCategory.MISC,
                result,
                experience,
                cookingTime,
                BlastingRecipe::new);
    }

    public <T extends ItemLike> void square(
                                            DataIngredient source, RecipeCategory category, Supplier<? extends T> output, boolean small) {
        ShapedRecipeBuilder builder = shaped(category, output.get()).define('X', source.toVanilla());
        if (small) {
            builder.pattern("XX").pattern("XX");
        } else {
            builder.pattern("XXX").pattern("XXX").pattern("XXX");
        }
        builder
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeKey(output.get()));
    }

    public <T extends ItemLike> void storage(
                                             Supplier<? extends T> source, RecipeCategory category, Supplier<? extends T> output) {
        storage(DataIngredient.items(source), category, source, DataIngredient.items(output), output);
    }

    public <T extends ItemLike> void storage(
                                             DataIngredient sourceIngredient,
                                             RecipeCategory category,
                                             Supplier<? extends T> source,
                                             DataIngredient outputIngredient,
                                             Supplier<? extends T> output) {
        square(sourceIngredient, category, output, false);
        singleItemUnfinished(outputIngredient, category, source, 1, 9)
                .save(this, safeId(sourceIngredient) + "_from_" + safeName(output.get()));
    }

    public <T extends ItemLike> ShapelessRecipeBuilder singleItemUnfinished(
                                                                            DataIngredient source,
                                                                            RecipeCategory category,
                                                                            Supplier<? extends T> result,
                                                                            int required,
                                                                            int amount) {
        return shapeless(category, result.get(), amount)
                .requires(source.toVanilla(), required)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this));
    }

    public <T extends ItemLike> void singleItem(
                                                DataIngredient source,
                                                RecipeCategory category,
                                                Supplier<? extends T> result,
                                                int required,
                                                int amount) {
        singleItemUnfinished(source, category, result, required, amount)
                .save(this, safeKey(result.get()));
    }

    // Expose protected methods from RecipeProvider

    @Override
    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return super.shaped(category, result);
    }

    @Override
    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return super.shaped(category, result, count);
    }

    @Override
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStackTemplate result) {
        return super.shapeless(category, result);
    }

    @Override
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return super.shapeless(category, result);
    }

    @Override
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return super.shapeless(category, result, count);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
        return super.has(itemLike);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return super.has(tag);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(
                                                                                     ItemPredicate... predicates) {
        return RecipeProvider.inventoryTrigger(predicates);
    }

    public static String getHasName(ItemLike itemLike) {
        return RecipeProvider.getHasName(itemLike);
    }

    public static String getItemName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike);
    }

    @Override
    public Ingredient tag(TagKey<Item> tag) {
        return super.tag(tag);
    }
}
