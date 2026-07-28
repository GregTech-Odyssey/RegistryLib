package com.gto.registrylib.datagen.generator;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.util.TextureRef;
import com.gto.registrylib.util.visual.BlockModelLayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
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

    public Identifier createTintedCube(Block block, int tintIndex) {
        return createTintedCube(block, TextureMapping.getBlockTexture(block).sprite(), tintIndex);
    }

    public Identifier createTintedCube(Block block, Identifier texture, int tintIndex) {
        Identifier model = ModelLocationUtils.getModelLocation(block);
        modelOutput.accept(model, () -> createTintedCubeJson(texture, tintIndex));
        create(block, model);
        return model;
    }

    public Identifier createLayeredCube(Block block, TextureRef particle, BlockModelLayer... layers) {
        Identifier model = ModelLocationUtils.getModelLocation(block);
        modelOutput.accept(model, () -> createLayeredCubeJson(particle, layers));
        create(block, model);
        return model;
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

    public void generateCropStages(CropBlock block, TextureRef... stageTextures) {
        Identifier[] models = new Identifier[stageTextures.length];
        for (int i = 0; i < stageTextures.length; i++) {
            Identifier blockTexture = TextureMapping.getBlockTexture(block).sprite();
            models[i] = blockTexture.withSuffix("_stage" + i);
            Identifier texture = stageTextures[i].id();
            modelOutput.accept(models[i], () -> createCropStageJson(texture));
        }
        blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(
                                PropertyDispatch.initial(CropBlock.AGE)
                                        .generate(
                                                age -> {
                                                    int modelIndex = Math.min(
                                                            models.length - 1, age * models.length / (block.getMaxAge() + 1));
                                                    return plainVariant(models[modelIndex]);
                                                })));
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

    @Override
    public void createNonTemplateModelBlock(Block block) {
        createNonTemplateModelBlock(block, TextureMapping.getBlockTexture(block));
    }

    public void createNonTemplateModelBlock(Block block, Identifier particleTexture) {
        createNonTemplateModelBlock(block, new Material(particleTexture));
    }

    public void createNonTemplateModelBlock(Block block, Material particleTexture) {
        TextureMapping textures = new TextureMapping().put(TextureSlot.PARTICLE, particleTexture);
        blockStateOutput.accept(
                createSimpleBlock(
                        block,
                        plainVariant(ModelTemplates.PARTICLE_ONLY.create(block, textures, modelOutput))));
    }

    private static JsonObject createTintedCubeJson(Identifier texture, int tintIndex) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/block");

        JsonObject textures = new JsonObject();
        textures.addProperty("particle", texture.toString());
        textures.addProperty("all", texture.toString());
        root.add("textures", textures);

        JsonObject element = new JsonObject();
        element.add("from", vector(0, 0, 0));
        element.add("to", vector(16, 16, 16));

        JsonObject faces = new JsonObject();
        faces.add("down", face("down", tintIndex));
        faces.add("up", face("up", tintIndex));
        faces.add("north", face("north", tintIndex));
        faces.add("south", face("south", tintIndex));
        faces.add("west", face("west", tintIndex));
        faces.add("east", face("east", tintIndex));
        element.add("faces", faces);

        JsonArray elements = new JsonArray();
        elements.add(element);
        root.add("elements", elements);
        return root;
    }

    private static JsonObject createCropStageJson(Identifier texture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/crop");
        JsonObject textures = new JsonObject();
        textures.addProperty("crop", texture.toString());
        root.add("textures", textures);
        return root;
    }

    private static JsonObject createLayeredCubeJson(TextureRef particle, BlockModelLayer... layers) {
        if (layers.length == 0) {
            throw new IllegalArgumentException("Layered cube model requires at least one layer");
        }
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/block");

        JsonObject textures = new JsonObject();
        textures.addProperty("particle", particle.id().toString());
        for (int i = 0; i < layers.length; i++) {
            textures.add("layer" + i, textureValue(layers[i]));
        }
        root.add("textures", textures);

        JsonArray elements = new JsonArray();
        for (int i = 0; i < layers.length; i++) {
            elements.add(layerElement("layer" + i, layers[i]));
        }
        root.add("elements", elements);
        return root;
    }

    private static JsonObject layerElement(String textureKey, BlockModelLayer layer) {
        JsonObject element = new JsonObject();
        element.add("from", vector(0, 0, 0));
        element.add("to", vector(16, 16, 16));

        JsonObject faces = new JsonObject();
        faces.add("down", layeredFace("down", textureKey, layer));
        faces.add("up", layeredFace("up", textureKey, layer));
        faces.add("north", layeredFace("north", textureKey, layer));
        faces.add("south", layeredFace("south", textureKey, layer));
        faces.add("west", layeredFace("west", textureKey, layer));
        faces.add("east", layeredFace("east", textureKey, layer));
        element.add("faces", faces);
        return element;
    }

    private static JsonObject layeredFace(String cullface, String textureKey, BlockModelLayer layer) {
        JsonObject face = face(cullface, layer.tintIndex());
        face.addProperty("texture", "#" + textureKey);
        if (!layer.hasTint()) {
            face.remove("tintindex");
        }
        return face;
    }

    private static com.google.gson.JsonElement textureValue(BlockModelLayer layer) {
        if (!layer.forceTranslucent()) {
            return new com.google.gson.JsonPrimitive(layer.texture().id().toString());
        }
        JsonObject texture = new JsonObject();
        texture.addProperty("sprite", layer.texture().id().toString());
        texture.addProperty("force_translucent", true);
        return texture;
    }

    private static JsonObject face(String cullface, int tintIndex) {
        JsonObject face = new JsonObject();
        face.add("uv", vector(0, 0, 16, 16));
        face.addProperty("texture", "#all");
        face.addProperty("cullface", cullface);
        face.addProperty("tintindex", tintIndex);
        return face;
    }

    private static JsonArray vector(int... values) {
        JsonArray array = new JsonArray();
        for (int value : values) {
            array.add(value);
        }
        return array;
    }
}
