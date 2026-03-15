package com.gto.registrylib.providers.generators;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.providers.ProviderType;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RegistryLibBlockModelGenerator extends BlockModelGenerators {

    private final RegistryCore parent;
    public final Map<Block, BlockStateModelDispatcher> seenBlockstates = new Reference2ReferenceOpenHashMap<>();

    public RegistryLibBlockModelGenerator(
                                          RegistryCore parent,
                                          Consumer<BlockModelDefinitionGenerator> known,
                                          ItemModelOutput item,
                                          BiConsumer<Identifier, ModelInstance> model) {
        super(known, item, model);
        ObfuscationReflectionHelper
                .<BlockModelGenerators, Consumer<BlockModelDefinitionGenerator>>setPrivateValue(
                        BlockModelGenerators.class,
                        this,
                        g -> {
                            this.seenBlockstates.put(g.block(), g.create());
                            known.accept(g);
                        },
                        "blockStateOutput");
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.genData(ProviderType.BLOCKSTATE, this);
    }

    public void create(Block block, Identifier model) {
        this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(model)));
    }

    public Identifier mcLoc(String id) {
        return Identifier.withDefaultNamespace(id);
    }

    public Identifier modLoc(String id) {
        return Identifier.fromNamespaceAndPath(parent.getModid(), id);
    }

    public RegistryLibLegacyBlockModelBuilder withBuilder(
                                                          ExtendedModelTemplateBuilder template, TextureMapping texture) {
        return new RegistryLibLegacyBlockModelBuilder(modelOutput, template, texture);
    }

    public RegistryLibLegacyBlockModelBuilder withBuilder(ExtendedModelTemplateBuilder template) {
        return withBuilder(template, new TextureMapping());
    }

    public RegistryLibLegacyBlockModelBuilder getBuilder() {
        return withBuilder(new ExtendedModelTemplateBuilder());
    }

    public RegistryLibLegacyBlockModelBuilder withParent(ModelTemplate template) {
        return withBuilder(ExtendedModelTemplateBuilder.of(template));
    }

    public RegistryLibLegacyBlockModelBuilder withParent(
                                                         ModelTemplate template, TextureMapping texture) {
        return withBuilder(ExtendedModelTemplateBuilder.of(template), texture);
    }

    public RegistryLibLegacyBlockModelBuilder withParent(TexturedModel model) {
        return withBuilder(ExtendedModelTemplateBuilder.of(model.getTemplate()), model.getMapping());
    }

    public Material blockTexture(Block block) {
        return TextureMapping.getBlockTexture(block);
    }

    public Material blockTexture(Block block, String suffix) {
        return TextureMapping.getBlockTexture(block, suffix);
    }

    public void generateWithTemplate(Block block, ModelTemplate template, TextureMapping textures) {
        create(block, template.create(block, textures, modelOutput));
    }

    public void generate(Block block, TexturedModel.Provider texture) {
        blockStateOutput.accept(
                createSimpleBlock(block, plainVariant(texture.create(block, modelOutput))));
    }

    public void generateAxisBlock(RotatedPillarBlock block) {
        generateAxisBlock(block, blockTexture(block));
    }

    public void generateAxisBlock(RotatedPillarBlock block, Material baseName) {
        generateAxisBlock(
                block,
                new Material(baseName.sprite().withSuffix("_side")),
                new Material(baseName.sprite().withSuffix("_end")));
    }

    public void generateAxisBlock(RotatedPillarBlock block, Material side, Material end) {
        generateAxisBlock(
                block,
                plainVariant(
                        ModelTemplates.CUBE_COLUMN.create(
                                block, TextureMapping.column(side, end), modelOutput)),
                plainVariant(
                        ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(
                                block, TextureMapping.column(side, end), modelOutput)));
    }

    public void generateAxisBlock(
                                  RotatedPillarBlock block, MultiVariant vertical, MultiVariant horizontal) {
        blockStateOutput.accept(createRotatedPillarWithHorizontalVariant(block, vertical, horizontal));
    }

    public void generateHorizontalBlock(Block block, Material side, Material front, Material top) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.FRONT, front)
                .put(TextureSlot.TOP, top);
        generateHorizontalBlock(
                block, plainVariant(ModelTemplates.CUBE_ORIENTABLE.create(block, mapping, modelOutput)));
    }

    public void generateHorizontalBlock(Block block, MultiVariant model) {
        blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, model).with(ROTATION_HORIZONTAL_FACING));
    }

    public void generateStairsBlock(StairBlock block, Material texture) {
        generateStairsBlock(block, texture, texture, texture);
    }

    public void generateStairsBlock(StairBlock block, Material side, Material bottom, Material top) {
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top);
        blockStateOutput.accept(
                createStairs(
                        block,
                        plainVariant(ModelTemplates.STAIRS_INNER.create(block, textures, modelOutput)),
                        plainVariant(ModelTemplates.STAIRS_STRAIGHT.create(block, textures, modelOutput)),
                        plainVariant(ModelTemplates.STAIRS_OUTER.create(block, textures, modelOutput))));
    }

    public void generateSlabBlock(SlabBlock block, MultiVariant doubleSlab, Material texture) {
        generateSlabBlock(block, doubleSlab, texture, texture, texture);
    }

    public void generateSlabBlock(
                                  SlabBlock block, MultiVariant doubleSlab, Material side, Material bottom, Material top) {
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top);
        MultiVariant slabTop = plainVariant(ModelTemplates.SLAB_TOP.create(block, textures, modelOutput));
        MultiVariant slabBottom = plainVariant(ModelTemplates.SLAB_BOTTOM.create(block, textures, modelOutput));
        blockStateOutput.accept(createSlab(block, slabBottom, slabTop, doubleSlab));
    }

    public void createNonTemplateModelBlock(Block block) {
        blockStateOutput.accept(
                createSimpleBlock(
                        block,
                        plainVariant(
                                ModelTemplates.PARTICLE_ONLY.create(
                                        block, TextureMapping.particle(block), modelOutput))));
    }
}
