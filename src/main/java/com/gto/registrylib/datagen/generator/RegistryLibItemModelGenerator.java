package com.gto.registrylib.datagen.generator;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.util.TextureRef;
import com.gto.registrylib.util.color.RgbColor;

import com.google.gson.JsonElement;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class RegistryLibItemModelGenerator extends ItemModelGenerators {

    private final RegistryCore parent;
    public final BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;

    public RegistryLibItemModelGenerator(
                                         RegistryCore parent, BiConsumer<ResourceLocation, Supplier<JsonElement>> model) {
        super(model);
        this.modelOutput = model;
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.genData(ProviderType.ITEM_MODEL, this);
    }

    public void createWithExistingModel(Item item, ResourceLocation id) {
        modelOutput.accept(ModelLocationUtils.getModelLocation(item), new DelegatedModel(id));
    }

    public void generateWithTemplate(Item item, ModelTemplate template, TextureMapping textures) {
        template.create(ModelLocationUtils.getModelLocation(item), textures, modelOutput);
    }

    public void generateFlatItem(Item item, ResourceLocation layer0) {
        generateFlatItem(item, ModelTemplates.FLAT_ITEM, layer0);
    }

    /** 使用物品自身的默认纹理生成 flat item 模型。 */
    public void generateFlatItem(Item item, ModelTemplate template) {
        template.create(
                ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), modelOutput);
    }

    public void generateFlatItem(Item item, ModelTemplate template, ResourceLocation layer0) {
        template.create(
                ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(layer0), modelOutput);
    }

    /**
     * 1.21.1 中，生成式物品模型的第 N 层自动带有 tint index N，着色完全由运行时 {@code ItemColor} （通过 {@code
     * RegisterColorHandlersEvent.Item} 注册）驱动，因此 datagen 只需要生成普通的 flat item 模型。
     */
    public void generateFlatTintedItem(Item item, RgbColor color) {
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), modelOutput);
    }

    public void generateFlatTintedItem(Item item, TextureRef texture, RgbColor color) {
        generateFlatItem(item, texture.id());
    }

    /** 生成 block item 的委托模型——指向方块的模型（方块模型的面自带 tintindex，物品着色由 ItemColors 委托给 BlockColors）。 */
    public void generateTintedBlockItem(Block block) {
        modelOutput.accept(
                ModelLocationUtils.getModelLocation(block.asItem()),
                new DelegatedModel(ModelLocationUtils.getModelLocation(block)));
    }

    public void generateFlatBlockItem(BlockItem item) {
        generateFlatItem(item, TextureMapping.getBlockTexture(item.getBlock()));
    }

    public void generateFlatBlockItem(BlockItem item, String suffix) {
        generateFlatItem(item, TextureMapping.getBlockTexture(item.getBlock(), suffix));
    }

    public void generateBlockItem(BlockItem item, UnaryOperator<ResourceLocation> modelMapper) {
        modelOutput.accept(
                ModelLocationUtils.getModelLocation(item),
                new DelegatedModel(
                        modelMapper.apply(ModelLocationUtils.getModelLocation(item.getBlock()))));
    }

    public void generateBlockItem(BlockItem item, String suffix) {
        generateBlockItem(item, model -> model.withSuffix(suffix));
    }

    public ResourceLocation mcLoc(String id) {
        return ResourceLocation.withDefaultNamespace(id);
    }

    public ResourceLocation modLoc(String id) {
        return ResourceLocation.fromNamespaceAndPath(parent.getModid(), id);
    }

    public String modid(Supplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getNamespace();
    }

    public String name(Supplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath();
    }
}
