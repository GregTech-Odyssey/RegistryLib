package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

/**
 * 1.21.1 的 {@code RecipeProvider} 没有 {@code Runner} 内部类；此类作为独立的 datagen provider 注册， 委托给 {@link
 * RegistryLibRecipeProvider}（其 final {@code run} 会驱动 {@code buildRecipes}）。
 */
public class RegistryLibRecipeRunner implements RegistryLibProvider {

    final RegistryCore owner;
    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> registries;

    @Nullable
    RegistryLibRecipeProvider provider;

    public RegistryLibRecipeRunner(
                                   RegistryCore owner,
                                   PackOutput packOutput,
                                   CompletableFuture<HolderLookup.Provider> provider) {
        this.owner = owner;
        this.packOutput = packOutput;
        this.registries = provider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return new RegistryLibRecipeProvider(packOutput, registries, this).run(cache);
    }

    @Override
    public String getName() {
        return "RegistryLib Recipe Runner for " + owner.getModid();
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    public RegistryLibRecipeProvider getRecipeProvider() {
        if (provider == null) throw new IllegalStateException("Recipe Provider is not available now");
        return provider;
    }
}
