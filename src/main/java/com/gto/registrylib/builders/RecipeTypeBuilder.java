package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.util.entry.RecipeTypeEntry;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * 配方类型 Builder，一次性注册 {@link RecipeType} + {@link RecipeSerializer}。
 *
 * <p>
 * Fluent builder that registers both a {@link RecipeType} and {@link RecipeSerializer} under the
 * same name. After registration, use the returned {@link RecipeTypeEntry} to add individual recipes
 * via {@link RecipeTypeEntry#addRecipe}.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * 
 * // Step 1: Register the RecipeType + RecipeSerializer
 * public static final RecipeTypeEntry<AltarRecipe> ALTAR = REGISTRYLIB
 *         .<AltarRecipe>recipeType("altar")
 *         .serializer(AltarRecipe.CODEC, AltarRecipe.STREAM_CODEC)
 *         .register();
 *
 * // Step 2: Add individual recipes (can be done after registration)
 * static {
 *     ALTAR.addRecipe("cobblestone_to_stone",
 *             new AltarRecipe(Ingredient.of(Items.COBBLESTONE), new ItemStackTemplate(Items.STONE), 40));
 * }
 * }</pre>
 *
 * <h3>Custom Factories</h3>
 *
 * <pre>{@code
 * // Custom RecipeType factory
 * .<MyRecipe>recipeType("my_recipe")
 *         .typeFactory(id -> new MyCustomRecipeType<>(id))
 *         .serializer(MyRecipe.CODEC, MyRecipe.STREAM_CODEC)
 *         .register();
 *
 * // Custom RecipeSerializer factory (alternative to serializer())
 * .<MyRecipe>recipeType("my_recipe")
 *         .serializerFactory(() -> MyRecipe.SERIALIZER)
 *         .register();
 * }</pre>
 *
 * @param <T> the concrete recipe type
 * @param <P> the parent type (for builder chaining)
 */
public class RecipeTypeBuilder<T extends Recipe<?>, P> {

    private final RegistryCore core;
    private final P parent;
    private final String name;

    private MapCodec<T> codec;
    private StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;
    private Function<Identifier, RecipeType<T>> typeFactory;

    protected RecipeTypeBuilder(RegistryCore core, P parent, String name) {
        this.core = core;
        this.parent = parent;
        this.name = name;
    }

    public static <T extends Recipe<?>, P> RecipeTypeBuilder<T, P> create(
                                                                          RegistryCore core, P parent, String name) {
        return new RecipeTypeBuilder<>(core, parent, name);
    }

    // === Configuration ===

    /**
     * 设置配方的序列化器参数（MapCodec + StreamCodec）。
     *
     * <p>
     * Sets the codec and stream codec used to construct the {@link RecipeSerializer}.
     */
    @StandardAPI
    public RecipeTypeBuilder<T, P> serializer(
                                              @NotNull MapCodec<T> codec, @NotNull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        this.codec = codec;
        this.streamCodec = streamCodec;
        return this;
    }

    /**
     * 自定义 {@link RecipeType} 的创建工厂。默认使用 {@code RecipeType.simple(id)}。
     *
     * <p>
     * Overrides how the {@link RecipeType} is created. By default, {@code RecipeType.simple(id)}
     * is used. The function receives the registry {@link Identifier} (e.g. {@code modid:name}).
     *
     * <pre>{@code
     * .<MyRecipe>recipeType("my_recipe")
     *         .typeFactory(id -> new MyCustomRecipeType<>(id))
     *         .serializer(MyRecipe.CODEC, MyRecipe.STREAM_CODEC)
     *         .register();
     * }</pre>
     */
    @StandardAPI
    public RecipeTypeBuilder<T, P> typeFactory(
                                               @NotNull Function<Identifier, RecipeType<T>> typeFactory) {
        this.typeFactory = typeFactory;
        return this;
    }

    // === Registration ===

    /**
     * 注册 RecipeType 和 RecipeSerializer，返回 {@link RecipeTypeEntry}。
     *
     * <p>
     * Registers the {@link RecipeType} and {@link RecipeSerializer}, and returns a {@link
     * RecipeTypeEntry} wrapping both. Use {@link RecipeTypeEntry#addRecipe} to add individual recipes
     * for datagen.
     */
    @StandardAPI
    public RecipeTypeEntry<T> register() {
        if (codec == null || streamCodec == null) {
            throw new IllegalStateException(
                    "RecipeTypeBuilder for '" + name + "' requires serializer(codec, streamCodec) or serializerFactory() before register()");
        }

        // Register RecipeType (custom factory or default)
        Function<Identifier, RecipeType<T>> tf = typeFactory != null ? typeFactory : RecipeType::simple;

        var serializer = new RecipeSerializer<>(codec, streamCodec);

        core.registry(name, serializer, Registries.RECIPE_SERIALIZER);

        return core.registry(
                name,
                Registries.RECIPE_TYPE,
                key -> tf.apply(key.identifier()),
                key -> new RecipeTypeEntry<>(key, core, serializer));
    }

    /**
     * 注册并返回父对象（用于链式调用）。
     *
     * <p>
     * Registers and returns the parent (for builder chaining).
     */
    @SyntaxSugar("register(); return parent")
    public P build() {
        register();
        return parent;
    }
}
