package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.datagen.GeneratorType;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibRecipeProvider;
import com.gto.registrylib.util.entry.RecipeEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * 配方 Builder，一次性注册 {@link RecipeType} + {@link RecipeSerializer}，并提供便捷的配方数据生成。
 *
 * <p>Fluent builder that registers both a {@link RecipeType} and {@link RecipeSerializer} under the
 * same name, and provides convenient recipe datagen via {@link #addRecipe}.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * public static final RecipeEntry<AltarRecipe> ALTAR = REGISTRYLIB
 *     .recipe("altar")
 *     .serializer(AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC)
 *     .addRecipe("cobblestone_to_stone",
 *         new AltarRecipe(Ingredient.of(Items.COBBLESTONE), new ItemStackTemplate(Items.STONE), 40))
 *     .register();
 * }</pre>
 *
 * @param <T> the concrete recipe type
 * @param <P> the parent type (for builder chaining)
 */
public class RecipeBuilder<T extends Recipe<?>, P> {

    private final RegistryCore core;
    private final P parent;
    private final String name;

    private MapCodec<T> codec;
    private StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    private final List<RecipeData<T>> recipes = new ArrayList<>();
    private final List<Consumer<RegistryLibRecipeProvider>> extraDatagens = new ArrayList<>();

    private record RecipeData<T>(String recipeName, Supplier<T> recipeSupplier) {}

    protected RecipeBuilder(RegistryCore core, P parent, String name) {
        this.core = core;
        this.parent = parent;
        this.name = name;
    }

    public static <T extends Recipe<?>, P> RecipeBuilder<T, P> create(
                                                                      RegistryCore core, P parent, String name) {
        return new RecipeBuilder<>(core, parent, name);
    }

    // === Configuration ===

    /**
     * 设置配方的序列化器参数（MapCodec + StreamCodec）。
     *
     * <p>Sets the codec and stream codec used to construct the {@link RecipeSerializer}.
     */
    @StandardAPI
    public RecipeBuilder<T, P> serializer(
                                          @Nonnull MapCodec<T> codec,
                                          @Nonnull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        this.codec = codec;
        this.streamCodec = streamCodec;
        return this;
    }

    /**
     * 添加一条配方用于数据生成。配方 JSON 将自动生成到 {@code data/<modid>/recipe/<recipeName>.json}。
     *
     * <p>Adds a recipe instance for datagen. The JSON will be emitted at
     * {@code data/<modid>/recipe/<recipeName>.json}.
     *
     * @param recipeName the recipe file name (without extension or namespace)
     * @param recipe the recipe instance
     */
    @StandardAPI
    public RecipeBuilder<T, P> addRecipe(@NotNull String recipeName, @NotNull T recipe) {
        recipes.add(new RecipeData<>(recipeName, () -> recipe));
        return this;
    }

    /**
     * 添加一条延迟创建的配方用于数据生成。
     *
     * <p>Adds a lazily-created recipe instance for datagen.
     *
     * @param recipeName the recipe file name
     * @param recipeSupplier a supplier that provides the recipe instance
     */
    @StandardAPI
    public RecipeBuilder<T, P> addRecipe(@NotNull String recipeName, @NotNull Supplier<T> recipeSupplier) {
        recipes.add(new RecipeData<>(recipeName, recipeSupplier));
        return this;
    }

    /**
     * 添加自定义的配方数据生成回调，可直接操作 {@link RegistryLibRecipeProvider}。
     *
     * <p>Adds a custom datagen callback for full control over recipe output.
     */
    @StandardAPI
    public RecipeBuilder<T, P> customRecipeData(@NotNull Consumer<RegistryLibRecipeProvider> datagen) {
        extraDatagens.add(datagen);
        return this;
    }

    // === Registration ===

    /**
     * 注册 RecipeType、RecipeSerializer 和配方数据生成，然后返回 {@link RecipeEntry}。
     *
     * <p>Registers the {@link RecipeType} and {@link RecipeSerializer}, sets up recipe datagen, and
     * returns a {@link RecipeEntry} wrapping both.
     */
    @StandardAPI
    @SuppressWarnings("unchecked")
    public RecipeEntry<T> register() {
        if (codec == null || streamCodec == null) {
            throw new IllegalStateException(
                    "RecipeBuilder for '" + name + "' requires serializer(codec, streamCodec) before register()");
        }

        // Register RecipeType
        var typeEntry = (RegistryEntry<RecipeType<?>, RecipeType<T>>) (RegistryEntry) core.simple(
                name,
                Registries.RECIPE_TYPE,
                key -> RecipeType.simple(key.identifier()));

        // Register RecipeSerializer
        final MapCodec<T> c = codec;
        final StreamCodec<RegistryFriendlyByteBuf, T> sc = streamCodec;
        var serializerEntry = (RegistryEntry<RecipeSerializer<?>, RecipeSerializer<T>>) (RegistryEntry) core.simple(
                name,
                Registries.RECIPE_SERIALIZER,
                key -> new RecipeSerializer<>(c, sc));

        // Register recipe datagen
        if (core.doDatagen() && (!recipes.isEmpty() || !extraDatagens.isEmpty())) {
            final String modid = core.getModid();
            core.addDataGenerator(
                    ProviderType.RECIPE,
                    (RegistryLibRecipeProvider prov) -> {
                        for (var data : recipes) {
                            prov.accept(
                                    ResourceKey.create(
                                            Registries.RECIPE,
                                            Identifier.fromNamespaceAndPath(modid, data.recipeName())),
                                    data.recipeSupplier().get(),
                                    null);
                        }
                        for (var extra : extraDatagens) {
                            extra.accept(prov);
                        }
                    });
        }

        return new RecipeEntry<>(typeEntry, serializerEntry);
    }

    /**
     * 注册并返回父对象（用于链式调用）。
     *
     * <p>Registers and returns the parent (for builder chaining).
     */
    @SyntaxSugar("register(); return parent")
    public P build() {
        register();
        return parent;
    }
}
