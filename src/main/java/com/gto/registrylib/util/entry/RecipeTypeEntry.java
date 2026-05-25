package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

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
public class RecipeTypeEntry<T extends Recipe<?>>
                            extends RegistryEntry<RecipeType<?>, RecipeType<T>> {

    private final RegistryCore core;

    /** -- GETTER -- Returns the RecipeSerializer RegistryEntry. */
    @Getter
    private final RecipeSerializer<T> serializer;

    public RecipeTypeEntry(
                           ResourceKey<RecipeType<?>> key, RegistryCore core, RecipeSerializer<T> serializer) {
        super(key);
        this.core = core;
        this.serializer = serializer;
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
    public RecipeTypeEntry<T> addRecipe(@NotNull String recipeName, @NotNull T recipe) {
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
    public RecipeTypeEntry<T> addRecipe(
                                        @NotNull String recipeName, @NotNull Supplier<T> recipeSupplier) {
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
    public RecipeTypeEntry<T> addRecipe(
                                        @NotNull String recipeName, @NotNull Function<HolderLookup.Provider, T> recipeFactory) {
        String typeName = key.identifier().getPath();
        core.addRecipe(typeName + "/" + recipeName, recipeFactory);
        return this;
    }

    public static <T extends Recipe<?>> RecipeTypeEntry<T> cast(RegistryEntry<RecipeType<?>, RecipeType<T>> entry) {
        return RegistryEntry.cast(RecipeTypeEntry.class, entry);
    }
}
