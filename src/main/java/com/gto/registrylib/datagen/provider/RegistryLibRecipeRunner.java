package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;

import com.google.gson.JsonElement;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

public class RegistryLibRecipeRunner extends RecipeProvider.Runner implements RegistryLibProvider {

    final RegistryCore owner;
    private final PackOutput packOutput;

    @Nullable
    RegistryLibRecipeProvider provider;

    // Deferred writes for copy-recipe JSON that bypass the standard RecipeOutput pipeline.
    final List<DeferredRecipeWrite> deferredWrites = new ArrayList<>();

    record DeferredRecipeWrite(ResourceKey<Recipe<?>> key, JsonElement json) {}

    public RegistryLibRecipeRunner(
                                   RegistryCore owner,
                                   PackOutput packOutput,
                                   CompletableFuture<HolderLookup.Provider> provider) {
        super(packOutput, provider);
        this.owner = owner;
        this.packOutput = packOutput;
    }

    @Override
    protected RecipeProvider createRecipeProvider(
                                                  HolderLookup.Provider registries, RecipeOutput output) {
        return new RegistryLibRecipeProvider(this, registries, output);
    }

    /**
     * Writes deferred copy-recipe JSON files that were collected during {@code buildRecipes()}.
     * Called by {@link com.gto.registrylib.datagen.RegistryLibDataProvider} after {@code run()}
     * completes.
     */
    public CompletableFuture<?> writeDeferredRecipes(CachedOutput cache) {
        if (deferredWrites.isEmpty()) return CompletableFuture.completedFuture(null);
        var pathProvider = packOutput.createRegistryElementsPathProvider(Registries.RECIPE);
        var tasks = new ArrayList<CompletableFuture<?>>();
        for (var w : deferredWrites) {
            var path = pathProvider.json(w.key().identifier());
            tasks.add(DataProvider.saveStable(cache, w.json(), path));
        }
        deferredWrites.clear();
        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
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
