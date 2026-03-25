package com.gto.registrylib.util.entry;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibRecipeProvider;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 配方注册条目，封装了 RecipeType 和 RecipeSerializer 的引用，并提供添加配方实例的 API。
 *
 * <p>
 * Wraps both a {@link RecipeType} and {@link RecipeSerializer} registered under the same name.
 * Use {@link #addRecipe} to add individual recipe instances for datagen after registration.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * // After registration:
 * ALTAR.addRecipe("cobblestone_to_stone",
 *         new AltarRecipe(Ingredient.of(Items.COBBLESTONE), new ItemStackTemplate(Items.STONE), 40));
 * ALTAR.addRecipe("raw_iron_to_ingot",
 *         () -> new AltarRecipe(Ingredient.of(Items.RAW_IRON), new ItemStackTemplate(Items.IRON_INGOT), 80));
 * }</pre>
 *
 * @param <T> the concrete recipe type
 */
public class RecipeEntry<T extends Recipe<?>> {

    private final RegistryCore core;

    /** -- GETTER -- Returns the RecipeType RegistryEntry. */
    @Getter
    private final RegistryEntry<RecipeType<?>, RecipeType<T>> typeEntry;

    /** -- GETTER -- Returns the RecipeSerializer RegistryEntry. */
    @Getter
    private final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<T>> serializerEntry;

    // === copy 支持：记录本 Entry 已注册的所有 (key, factory) ===
    // 仅在 doDatagen() 为 true 时填充，运行时为空列表，零开销。
    private final List<Map.Entry<String, Function<HolderLookup.Provider, T>>> registeredRecipes = new ArrayList<>();

    public RecipeEntry(
                       RegistryCore core,
                       RegistryEntry<RecipeType<?>, RecipeType<T>> typeEntry,
                       RegistryEntry<RecipeSerializer<?>, RecipeSerializer<T>> serializerEntry) {
        this.core = core;
        this.typeEntry = typeEntry;
        this.serializerEntry = serializerEntry;
    }

    // === Recipe Addition ===

    /**
     * 添加一条配方用于数据生成。配方 JSON 将自动生成到 {@code data/<modid>/recipe/<typeName>/<recipeName>.json}。
     *
     * <p>
     * Adds a recipe instance for datagen. The JSON will be emitted at {@code
     * data/<modid>/recipe/<typeName>/<recipeName>.json}.
     *
     * @param recipeName the recipe file name (without extension or namespace)
     * @param recipe     the recipe instance
     * @return this entry for chaining
     */
    @StandardAPI
    public RecipeEntry<T> addRecipe(@NotNull String recipeName, @NotNull T recipe) {
        return addRecipe(recipeName, _reg -> recipe);
    }

    /**
     * 添加一条延迟创建的配方用于数据生成。
     *
     * <p>
     * Adds a lazily-created recipe instance for datagen.
     *
     * @param recipeName     the recipe file name
     * @param recipeSupplier a supplier that provides the recipe instance
     * @return this entry for chaining
     */
    @StandardAPI
    public RecipeEntry<T> addRecipe(@NotNull String recipeName, @NotNull Supplier<T> recipeSupplier) {
        return addRecipe(recipeName, _reg -> recipeSupplier.get());
    }

    /**
     * 添加一条需要注册表查找的配方用于数据生成（例如基于 Tag 的 Ingredient）。
     *
     * <p>
     * Adds a recipe for datagen that requires registry lookups (e.g. tag-based {@code
     * Ingredient}s). The {@link HolderLookup.Provider} gives access to item tags and other registry
     * data.
     *
     * <pre>{@code
     * ENTRY.addRecipe("from_logs", registries -> new MyRecipe(
     *         Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.LOGS)),
     *         new ItemStackTemplate(Items.CHARCOAL), 60));
     * }</pre>
     *
     * @param recipeName    the recipe file name
     * @param recipeFactory a function that receives the registries and produces a recipe
     * @return this entry for chaining
     */
    @StandardAPI
    public RecipeEntry<T> addRecipe(
                                    @NotNull String recipeName, @NotNull Function<HolderLookup.Provider, T> recipeFactory) {
        if (core.doDatagen()) {
            registeredRecipes.add(new AbstractMap.SimpleImmutableEntry<>(recipeName, recipeFactory));
            final String modid = core.getModid();
            final String typeName = typeEntry.getKey().identifier().getPath();
            core.addDataGenerator(
                    ProviderType.RECIPE,
                    (RegistryLibRecipeProvider prov) -> prov.acceptWithSerializer(
                            ResourceKey.create(
                                    Registries.RECIPE,
                                    Identifier.fromNamespaceAndPath(modid, typeName + "/" + recipeName)),
                            recipeFactory.apply(prov.registries()),
                            serializerEntry.get()));
        }
        return this;
    }

    /**
     * 添加自定义的配方数据生成回调，可直接操作 {@link RegistryLibRecipeProvider}。
     *
     * <p>
     * Adds a custom datagen callback for full control over recipe output.
     *
     * @param datagen the datagen callback
     * @return this entry for chaining
     */
    @StandardAPI
    public RecipeEntry<T> customRecipeData(@NotNull Consumer<RegistryLibRecipeProvider> datagen) {
        if (core.doDatagen()) {
            core.addDataGenerator(ProviderType.RECIPE, datagen);
        }
        return this;
    }

    // === Accessors ===

    /**
     * 返回本 Entry 已注册的所有配方工厂（仅 datagen 时有内容）。
     *
     * <p>
     * Returns all recipe factories registered on this entry. Only populated during datagen. Used
     * internally by {@code copyRecipe} to mirror recipes.
     */
    public List<Map.Entry<String, Function<HolderLookup.Provider, T>>> getRegisteredRecipes() {
        return Collections.unmodifiableList(registeredRecipes);
    }

    /** Returns the registered RecipeType. */
    public RecipeType<T> getType() {
        return typeEntry.get();
    }

    /** Returns the registered RecipeSerializer. */
    public RecipeSerializer<T> getSerializer() {
        return serializerEntry.get();
    }

    /** Returns the ResourceKey of the RecipeType. */
    public ResourceKey<RecipeType<?>> getTypeKey() {
        return typeEntry.getKey();
    }

    /** Returns the ResourceKey of the RecipeSerializer. */
    public ResourceKey<RecipeSerializer<?>> getSerializerKey() {
        return serializerEntry.getKey();
    }
}
