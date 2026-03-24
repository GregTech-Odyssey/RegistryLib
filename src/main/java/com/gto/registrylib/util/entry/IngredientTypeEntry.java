package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import lombok.Getter;

/**
 * 自定义 Ingredient 类型注册条目，封装了 {@link IngredientType} 的注册引用。
 *
 * <p>
 * Wraps a registered {@link IngredientType} entry. Use {@link RegistryCore#ingredientType} to
 * create one via fluent API.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * public static final IngredientTypeEntry<MyIngredient> MY_INGREDIENT_TYPE = REGISTRYLIB
 *         .ingredientType("my_ingredient", MyIngredient.CODEC);
 * }</pre>
 *
 * @param <T> the concrete {@link ICustomIngredient} implementation type
 */
public class IngredientTypeEntry<T extends ICustomIngredient> {

    @Getter
    private final RegistryEntry<IngredientType<?>, IngredientType<T>> entry;

    public IngredientTypeEntry(RegistryEntry<IngredientType<?>, IngredientType<T>> entry) {
        this.entry = entry;
    }

    /**
     * 获取已注册的 {@link IngredientType} 实例。
     *
     * <p>
     * Returns the registered {@link IngredientType} instance.
     */
    public IngredientType<T> get() {
        return entry.get();
    }
}
