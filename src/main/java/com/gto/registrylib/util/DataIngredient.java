package com.gto.registrylib.util;

import com.gto.registrylib.datagen.provider.RegistryLibRecipeProvider;

import com.google.common.collect.ObjectArrays;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import lombok.Getter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DataIngredient {

    private final Ingredient parent;
    @Getter
    private final ResourceLocation id;
    private final Function<RegistryLibRecipeProvider, Criterion<InventoryChangeTrigger.TriggerInstance>> criteriaFactory;

    private DataIngredient(Ingredient parent, ItemLike item) {
        this.parent = parent;
        this.id = BuiltInRegistries.ITEM.getKey(item.asItem());
        this.criteriaFactory = prov -> prov.has(item);
    }

    private DataIngredient(Ingredient parent, TagKey<Item> tag) {
        this.parent = parent;
        this.id = tag.location();
        this.criteriaFactory = prov -> prov.has(tag);
    }

    private DataIngredient(Ingredient parent, ResourceLocation id, ItemPredicate... predicates) {
        this.parent = parent;
        this.id = id;
        this.criteriaFactory = prov -> RegistryLibRecipeProvider.inventoryTrigger(predicates);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> getCriterion(
                                                                          RegistryLibRecipeProvider prov) {
        return criteriaFactory.apply(prov);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(
                                                            Supplier<? extends T> first, Supplier<? extends T>... others) {
        return items(
                first.get(), (T[]) Arrays.stream(others).map(Supplier::get).toArray(ItemLike[]::new));
    }

    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(T first, T... others) {
        return ingredient(Ingredient.of(ObjectArrays.concat(first, others)), first);
    }

    public static DataIngredient tag(HolderSet.Named<Item> tag) {
        return ingredient(Ingredient.of(tag.key()), tag.key());
    }

    public static DataIngredient ingredient(Ingredient parent, ItemLike required) {
        return new DataIngredient(parent, required);
    }

    public static DataIngredient ingredient(Ingredient parent, TagKey<Item> required) {
        return new DataIngredient(parent, required);
    }

    public static DataIngredient ingredient(
                                            Ingredient parent, ResourceLocation id, ItemPredicate... criteria) {
        return new DataIngredient(parent, id, criteria);
    }

    public Ingredient toVanilla() {
        return parent;
    }
}
