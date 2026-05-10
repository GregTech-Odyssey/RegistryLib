package com.gto.registrylib.datagen.generator;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.ProviderType;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class RegistryLibItemModelGenerator extends ItemModelGenerators {

    private final RegistryCore parent;

    public RegistryLibItemModelGenerator(
                                         RegistryCore parent, ItemModelOutput output, BiConsumer<Identifier, ModelInstance> model) {
        super(output, model);
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.genData(ProviderType.ITEM_MODEL, this);
    }

    public void createWithExistingModel(Item item, Identifier id) {
        itemModelOutput.accept(item, ItemModelUtils.plainModel(id));
    }

    public void generateWithTemplate(Item item, ModelTemplate template, TextureMapping textures) {
        itemModelOutput.accept(
                item, ItemModelUtils.plainModel(template.create(item, textures, modelOutput)));
    }

    public void generateFlatItem(Item item, Material layer0) {
        generateFlatItem(item, ModelTemplates.FLAT_ITEM, layer0);
    }

    public void generateFlatItem(Item item, ModelTemplate template, Material layer0) {
        itemModelOutput.accept(
                item,
                ItemModelUtils.plainModel(
                        template.create(item, TextureMapping.layer0(layer0), modelOutput)));
    }

    public void generateFlatTintedItem(Item item, int color) {
        generateFlatTintedItem(item, ItemModelUtils.constantTint(color));
    }

    public void generateFlatTintedItem(Item item, ItemTintSource... tintSources) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(item, TextureMapping.layer0(item), modelOutput);
        itemModelOutput.accept(item, ItemModelUtils.tintedModel(model, tintSources));
    }

    public void generateTintedBlockItem(Block block, int color) {
        generateTintedBlockItem(block, ItemModelUtils.constantTint(color));
    }

    public void generateTintedBlockItem(Block block, ItemTintSource... tintSources) {
        itemModelOutput.accept(
                block.asItem(),
                ItemModelUtils.tintedModel(ModelLocationUtils.getModelLocation(block), tintSources));
    }

    public void generateFlatBlockItem(BlockItem item) {
        generateFlatItem(item, TextureMapping.getBlockTexture(item.getBlock()));
    }

    public void generateFlatBlockItem(BlockItem item, String suffix) {
        generateFlatItem(item, TextureMapping.getBlockTexture(item.getBlock(), suffix));
    }

    public void generateBlockItem(BlockItem item, UnaryOperator<Identifier> modelMapper) {
        itemModelOutput.accept(
                item,
                ItemModelUtils.plainModel(
                        modelMapper.apply(ModelLocationUtils.getModelLocation(item.getBlock()))));
    }

    public void generateBlockItem(BlockItem item, String suffix) {
        generateBlockItem(item, model -> model.withSuffix(suffix));
    }

    public Identifier mcLoc(String id) {
        return Identifier.withDefaultNamespace(id);
    }

    public Identifier modLoc(String id) {
        return Identifier.fromNamespaceAndPath(parent.getModid(), id);
    }

    public String modid(Supplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getNamespace();
    }

    public String name(Supplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath();
    }
}
