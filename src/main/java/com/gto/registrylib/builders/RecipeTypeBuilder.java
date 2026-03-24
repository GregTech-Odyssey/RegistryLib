package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.util.entry.RecipeEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;

/**
 * 配方类型 Builder，一次性注册 {@link RecipeType} + {@link RecipeSerializer}。
 *
 * <p>
 * Fluent builder that registers both a {@link RecipeType} and {@link RecipeSerializer} under the
 * same name. After registration, use the returned {@link RecipeEntry} to add individual recipes via
 * {@link RecipeEntry#addRecipe}.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * 
 * // Step 1: Register the RecipeType + RecipeSerializer
 * public static final RecipeEntry<AltarRecipe> ALTAR = REGISTRYLIB
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
 * @param <T> the concrete recipe type
 * @param <P> the parent type (for builder chaining)
 */
public class RecipeTypeBuilder<T extends Recipe<?>, P> {

    private final RegistryCore core;
    private final P parent;
    private final String name;

    private MapCodec<T> codec;
    private StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

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
                                              @Nonnull MapCodec<T> codec, @Nonnull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        this.codec = codec;
        this.streamCodec = streamCodec;
        return this;
    }

    // === Registration ===

    /**
     * 注册 RecipeType 和 RecipeSerializer，返回 {@link RecipeEntry}。
     *
     * <p>
     * Registers the {@link RecipeType} and {@link RecipeSerializer}, and returns a {@link
     * RecipeEntry} wrapping both. Use {@link RecipeEntry#addRecipe} to add individual recipes for
     * datagen.
     */
    @StandardAPI
    @SuppressWarnings("unchecked")
    public RecipeEntry<T> register() {
        if (codec == null || streamCodec == null) {
            throw new IllegalStateException(
                    "RecipeTypeBuilder for '" + name + "' requires serializer(codec, streamCodec) before register()");
        }

        // Register RecipeType
        var typeEntry = (RegistryEntry<RecipeType<?>, RecipeType<T>>) (RegistryEntry) core.simple(
                name, Registries.RECIPE_TYPE, key -> RecipeType.simple(key.identifier()));

        // Register RecipeSerializer
        final MapCodec<T> c = codec;
        final StreamCodec<RegistryFriendlyByteBuf, T> sc = streamCodec;
        var serializerEntry = (RegistryEntry<RecipeSerializer<?>, RecipeSerializer<T>>) (RegistryEntry) core.simple(
                name, Registries.RECIPE_SERIALIZER, key -> new RecipeSerializer<>(c, sc));

        return new RecipeEntry<>(core, typeEntry, serializerEntry);
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
