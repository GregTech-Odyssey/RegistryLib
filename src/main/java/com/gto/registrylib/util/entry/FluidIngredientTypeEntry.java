package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;

import lombok.Getter;

/**
 * 自定义 FluidIngredient 类型注册条目，封装了 {@link FluidIngredientType} 的注册引用。
 *
 * <p>
 * Wraps a registered {@link FluidIngredientType} entry. Use {@link RegistryCore#fluidIngredientType}
 * to create one via fluent API.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * public static final FluidIngredientTypeEntry<MyFluidIngredient> MY_TYPE = REGISTRYLIB
 *         .fluidIngredientType("my_fluid_ingredient", MyFluidIngredient.CODEC);
 * }</pre>
 *
 * @param <T> the concrete {@link FluidIngredient} implementation type
 */
public class FluidIngredientTypeEntry<T extends FluidIngredient> {

    @Getter
    private final RegistryEntry<FluidIngredientType<?>, FluidIngredientType<T>> entry;

    public FluidIngredientTypeEntry(RegistryEntry<FluidIngredientType<?>, FluidIngredientType<T>> entry) {
        this.entry = entry;
    }

    /**
     * 获取已注册的 {@link FluidIngredientType} 实例。
     *
     * <p>
     * Returns the registered {@link FluidIngredientType} instance.
     */
    public FluidIngredientType<T> get() {
        return entry.get();
    }
}
