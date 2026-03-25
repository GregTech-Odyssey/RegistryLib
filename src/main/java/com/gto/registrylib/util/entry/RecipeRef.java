package com.gto.registrylib.util.entry;

import com.gto.registrylib.annotations.StandardAPI;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * 对某个已存在的 {@link RecipeType} + {@link RecipeSerializer} 的引用。
 *
 * <p>
 * A lightweight reference to an existing {@link RecipeType} and its associated {@link
 * RecipeSerializer}. Used by {@code extendRecipe} / {@code copyRecipe} to specify the target recipe
 * type. Works identically for vanilla, NeoForge, and third-party recipe types.
 *
 * <p>
 * Values are resolved lazily — safe to create during static initialization even when the
 * underlying registry entries are not yet bound.
 *
 * <pre>{@code
 * // Vanilla smelting
 * RecipeRef.of(RecipeType.SMELTING, SmeltingRecipe.SERIALIZER)
 *
 * // Third-party mod
 * RecipeRef.of(SomeMod.RECIPE_TYPE.get(), SomeMod.SERIALIZER.get())
 *
 * // From an existing RecipeEntry
 * RecipeRef.of(myRecipeEntry)
 * }</pre>
 *
 * @param <T> the concrete recipe type
 */
public final class RecipeRef<T extends Recipe<?>> {

    private final Supplier<RecipeType<T>> typeSupplier;
    private final Supplier<RecipeSerializer<T>> serializerSupplier;

    private RecipeRef(
                      Supplier<RecipeType<T>> typeSupplier, Supplier<RecipeSerializer<T>> serializerSupplier) {
        this.typeSupplier = typeSupplier;
        this.serializerSupplier = serializerSupplier;
    }

    /** Returns the referenced RecipeType. */
    public RecipeType<T> type() {
        return typeSupplier.get();
    }

    /** Returns the referenced RecipeSerializer. */
    public RecipeSerializer<T> serializer() {
        return serializerSupplier.get();
    }

    /**
     * 创建一个引用，指向指定的 RecipeType 和 RecipeSerializer。
     *
     * <p>
     * Creates a reference to the given {@link RecipeType} and {@link RecipeSerializer}. This works
     * for vanilla types (e.g. {@code RecipeType.SMELTING}), NeoForge types, and any third-party mod
     * types — they are all registered in the same NeoForge registries.
     *
     * @param type       the recipe type
     * @param serializer the recipe serializer
     * @param <T>        the concrete recipe type
     * @return a new RecipeRef
     */
    @StandardAPI
    public static <T extends Recipe<?>> RecipeRef<T> of(
                                                        @Nonnull RecipeType<T> type, @Nonnull RecipeSerializer<T> serializer) {
        return new RecipeRef<>(() -> type, () -> serializer);
    }

    /**
     * 从已有的 {@link RecipeEntry} 创建引用（用于 mod 内跨 Entry 引用）。
     *
     * <p>
     * Creates a reference from an existing {@link RecipeEntry} registered by the same mod. Values
     * are resolved lazily — safe to call during static initialization.
     *
     * @param entry the RecipeEntry to reference
     * @param <T>   the concrete recipe type
     * @return a new RecipeRef
     */
    @StandardAPI
    public static <T extends Recipe<?>> RecipeRef<T> of(@Nonnull RecipeEntry<T> entry) {
        return new RecipeRef<>(entry::getType, entry::getSerializer);
    }
}
