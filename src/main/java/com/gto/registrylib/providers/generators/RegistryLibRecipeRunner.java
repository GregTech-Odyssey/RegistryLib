package com.gto.registrylib.providers.generators;

import com.gto.registrylib.RegistryLib;
import com.gto.registrylib.providers.RegistryLibProvider;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.fml.LogicalSide;

public class RegistryLibRecipeRunner extends RecipeProvider.Runner implements RegistryLibProvider {

  final RegistryLib owner;

  @Nullable RegistryLibRecipeProvider provider;

  public RegistryLibRecipeRunner(
      RegistryLib owner, PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
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
