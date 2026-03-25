package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibRecipeProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 向已有的配方类型（原版 / 第三方）注入额外配方的条目。不注册新的 RecipeType，仅在 datagen 时生成 指向目标 type 的配方 JSON。
 *
 * <p>
 * An entry that injects additional recipes into an existing {@link RecipeType} without
 * registering a new one. During datagen, the generated JSON files use the <em>target</em> type's
 * identifier so that the vanilla {@code RecipeManager} picks them up under the correct type.
 *
 * <pre>{@code
 * public static final ExtendRecipeEntry<SmeltingRecipe> EXTRA_SMELTING =
 *     REGISTRYLIB.extendRecipe(RecipeRef.of(RecipeType.SMELTING, SmeltingRecipe.SERIALIZER))
 *         .addRecipe("smelt_obsidian", new SmeltingRecipe(...));
 * }</pre>
 *
 * @param <T> the concrete recipe type of the target
 */
public class ExtendRecipeEntry<T extends Recipe<?>> {

    private final RegistryCore core;

    /** -- GETTER -- The referenced target recipe type. */
    @Getter
    private final RecipeRef<T> ref;

    public ExtendRecipeEntry(RegistryCore core, RecipeRef<T> ref) {
        this.core = core;
        this.ref = ref;
    }

    // === Recipe Addition ===

    /**
     * 添加一条配方，datagen 时生成指向目标 type 的 JSON。
     *
     * <p>
     * Adds a recipe instance. The generated JSON will have {@code "type"} pointing to the target
     * recipe type (e.g. {@code minecraft:smelting}), and the key namespace will be this mod's modid.
     *
     * @param recipeName the recipe file name (without extension or namespace)
     * @param recipe     the recipe instance
     * @return this entry for chaining
     */
    @StandardAPI
    public ExtendRecipeEntry<T> addRecipe(@NotNull String recipeName, @NotNull T recipe) {
        return addRecipe(recipeName, () -> recipe);
    }

    /**
     * 添加一条延迟创建的配方。
     *
     * <p>
     * Adds a lazily-created recipe instance.
     *
     * @param recipeName     the recipe file name
     * @param recipeSupplier a supplier that provides the recipe instance
     * @return this entry for chaining
     */
    @StandardAPI
    public ExtendRecipeEntry<T> addRecipe(
                                          @NotNull String recipeName, @NotNull Supplier<T> recipeSupplier) {
        if (core.doDatagen()) {
            final String modid = core.getModid();
            core.addDataGenerator(
                    ProviderType.RECIPE,
                    (RegistryLibRecipeProvider prov) -> prov.accept(
                            ResourceKey.create(
                                    Registries.RECIPE, Identifier.fromNamespaceAndPath(modid, recipeName)),
                            recipeSupplier.get(),
                            null));
        }
        return this;
    }

    /**
     * 添加一条需要注册表查找的配方（例如基于 Tag 的 Ingredient）。
     *
     * <p>
     * Adds a recipe that requires registry lookups (e.g. tag-based {@code Ingredient}s).
     *
     * @param recipeName    the recipe file name
     * @param recipeFactory a function that receives the registries and produces a recipe
     * @return this entry for chaining
     */
    @StandardAPI
    public ExtendRecipeEntry<T> addRecipe(
                                          @NotNull String recipeName, @NotNull Function<HolderLookup.Provider, T> recipeFactory) {
        if (core.doDatagen()) {
            final String modid = core.getModid();
            core.addDataGenerator(
                    ProviderType.RECIPE,
                    (RegistryLibRecipeProvider prov) -> prov.accept(
                            ResourceKey.create(
                                    Registries.RECIPE, Identifier.fromNamespaceAndPath(modid, recipeName)),
                            recipeFactory.apply(prov.registries()),
                            null));
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
    public ExtendRecipeEntry<T> customRecipeData(
                                                 @NotNull Consumer<RegistryLibRecipeProvider> datagen) {
        if (core.doDatagen()) {
            core.addDataGenerator(ProviderType.RECIPE, datagen);
        }
        return this;
    }

    /** Returns the target RecipeType. */
    public RecipeType<T> getType() {
        return ref.type();
    }
}
