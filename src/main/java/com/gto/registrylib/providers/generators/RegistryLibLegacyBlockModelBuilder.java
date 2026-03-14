package com.gto.registrylib.providers.generators;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.client.model.generators.template.RootTransformsBuilder;
import net.neoforged.neoforge.client.model.generators.template.TransformVecBuilder;

public class RegistryLibLegacyBlockModelBuilder {

  private final ExtendedModelTemplateBuilder template;
  private final TextureMapping texture;
  private final BiConsumer<Identifier, ModelInstance> output;

  RegistryLibLegacyBlockModelBuilder(
      BiConsumer<Identifier, ModelInstance> output,
      ExtendedModelTemplateBuilder template,
      TextureMapping texture) {
    this.output = output;
    this.template = template;
    this.texture = texture.copy();
  }

  public RegistryLibLegacyBlockModelBuilder texture(TextureSlot slot, Material material) {
    this.template.requiredTextureSlot(slot);
    this.texture.put(slot, material);
    return this;
  }

  public RegistryLibLegacyBlockModelBuilder transformTemplate(
      Consumer<ExtendedModelTemplateBuilder> action) {
    action.accept(template);
    return this;
  }

  public RegistryLibLegacyBlockModelBuilder transformTexture(Consumer<TextureMapping> action) {
    action.accept(texture);
    return this;
  }

  public Identifier build(Block block) {
    return template.build().create(block, texture, output);
  }

  public Identifier build(Identifier loc) {
    return template.build().create(loc, texture, output);
  }

  public RegistryLibLegacyBlockModelBuilder parent(Identifier parent) {
    template.parent(parent);
    return this;
  }

  public RegistryLibLegacyBlockModelBuilder suffix(String suffix) {
    template.suffix(suffix);
    return this;
  }

  public RegistryLibLegacyBlockModelBuilder transform(
      ItemDisplayContext type, Consumer<TransformVecBuilder> action) {
    template.transform(type, action);
    return this;
  }

  public RegistryLibLegacyBlockModelBuilder ambientOcclusion(boolean ambientOcclusion) {
    template.ambientOcclusion(ambientOcclusion);
    return this;
  }

  public RegistryLibLegacyBlockModelBuilder guiLight(UnbakedModel.GuiLight light) {
    template.guiLight(light);
    return this;
  }

  public <L extends CustomLoaderBuilder> RegistryLibLegacyBlockModelBuilder customLoader(
      Supplier<L> customLoaderFactory, Consumer<L> action) {
    template.customLoader(customLoaderFactory, action);
    return this;
  }

  public RegistryLibLegacyBlockModelBuilder rootTransforms(Consumer<RootTransformsBuilder> action) {
    template.rootTransforms(action);
    return this;
  }
}
