package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.fml.LogicalSide;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

public class RegistryLibRecipeRunner extends RecipeProvider.Runner implements RegistryLibProvider {

    final RegistryCore owner;

    @Nullable
    RegistryLibRecipeProvider provider;

    public RegistryLibRecipeRunner(
                                   RegistryCore owner,
                                   PackOutput packOutput,
                                   CompletableFuture<HolderLookup.Provider> provider) {
        super(packOutput, provider);
        this.owner = owner;
    }

    @Override
    protected RecipeProvider createRecipeProvider(
                                                  HolderLookup.Provider registries, RecipeOutput output) {
        return new RegistryLibRecipeProvider(this, registries, output);
    }

    @Override
    public String getName() {
        return "";
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
