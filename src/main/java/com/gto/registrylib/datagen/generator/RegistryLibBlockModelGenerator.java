package com.gto.registrylib.datagen.generator;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.util.TextureRef;
import com.gto.registrylib.util.visual.BlockModelLayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegistryLibBlockModelGenerator extends BlockModelGenerators {

    private final RegistryCore parent;

    /** 记录已生成 blockstate 的方块——供 {@code BlockBuilder.item()} 推导 block item 模型。 */
    public final Set<Block> seenBlockstates = new ReferenceOpenHashSet<>();

    private final Consumer<BlockStateGenerator> blockStateOutput;
    protected final BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;

    public RegistryLibBlockModelGenerator(
                                          RegistryCore parent,
                                          Consumer<BlockStateGenerator> blockStateOutput,
                                          BiConsumer<ResourceLocation, Supplier<JsonElement>> model) {
        super(blockStateOutput, model, block -> {});
        this.parent = parent;
        this.blockStateOutput = g -> {
            seenBlockstates.add(g.getBlock());
            blockStateOutput.accept(g);
        };
        this.modelOutput = model;
    }

    @Override
    public void run() {
        parent.genData(ProviderType.BLOCKSTATE, this);
    }

    // === 1.21.1 helpers (vanilla 中的 package-private static 方法，这里自行实现) ===

    private static MultiVariantGenerator simpleBlock(Block block, ResourceLocation model) {
        return MultiVariantGenerator.multiVariant(
                block, Variant.variant().with(VariantProperties.MODEL, model));
    }

    private static BlockStateGenerator rotatedPillarWithHorizontalVariant(
                                                                          Block block, ResourceLocation vertical, ResourceLocation horizontal) {
        return MultiVariantGenerator.multiVariant(block)
                .with(
                        PropertyDispatch.property(BlockStateProperties.AXIS)
                                .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, vertical))
                                .select(
                                        Direction.Axis.Z,
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, horizontal)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                                .select(
                                        Direction.Axis.X,
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, horizontal)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)));
    }

    private static BlockStateGenerator slab(
                                            Block block, ResourceLocation bottom, ResourceLocation top, ResourceLocation doubleSlab) {
        return MultiVariantGenerator.multiVariant(block)
                .with(
                        PropertyDispatch.property(BlockStateProperties.SLAB_TYPE)
                                .select(SlabType.BOTTOM, Variant.variant().with(VariantProperties.MODEL, bottom))
                                .select(SlabType.TOP, Variant.variant().with(VariantProperties.MODEL, top))
                                .select(
                                        SlabType.DOUBLE, Variant.variant().with(VariantProperties.MODEL, doubleSlab)));
    }

    private static BlockStateGenerator stairs(
                                              Block block, ResourceLocation inner, ResourceLocation straight, ResourceLocation outer) {
        return MultiVariantGenerator.multiVariant(block)
                .with(
                        PropertyDispatch.properties(
                                BlockStateProperties.HORIZONTAL_FACING,
                                BlockStateProperties.HALF,
                                BlockStateProperties.STAIRS_SHAPE)
                                .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, v(straight))
                                .select(
                                        Direction.WEST,
                                        Half.BOTTOM,
                                        StairsShape.STRAIGHT,
                                        vUv(straight).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.SOUTH,
                                        Half.BOTTOM,
                                        StairsShape.STRAIGHT,
                                        vUv(straight).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(
                                        Direction.NORTH,
                                        Half.BOTTOM,
                                        StairsShape.STRAIGHT,
                                        vUv(straight).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, v(outer))
                                .select(
                                        Direction.WEST,
                                        Half.BOTTOM,
                                        StairsShape.OUTER_RIGHT,
                                        vUv(outer).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.SOUTH,
                                        Half.BOTTOM,
                                        StairsShape.OUTER_RIGHT,
                                        vUv(outer).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(
                                        Direction.NORTH,
                                        Half.BOTTOM,
                                        StairsShape.OUTER_RIGHT,
                                        vUv(outer).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(
                                        Direction.EAST,
                                        Half.BOTTOM,
                                        StairsShape.OUTER_LEFT,
                                        vUv(outer).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(
                                        Direction.WEST,
                                        Half.BOTTOM,
                                        StairsShape.OUTER_LEFT,
                                        vUv(outer).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, v(outer))
                                .select(
                                        Direction.NORTH,
                                        Half.BOTTOM,
                                        StairsShape.OUTER_LEFT,
                                        vUv(outer).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, v(inner))
                                .select(
                                        Direction.WEST,
                                        Half.BOTTOM,
                                        StairsShape.INNER_RIGHT,
                                        vUv(inner).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.SOUTH,
                                        Half.BOTTOM,
                                        StairsShape.INNER_RIGHT,
                                        vUv(inner).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(
                                        Direction.NORTH,
                                        Half.BOTTOM,
                                        StairsShape.INNER_RIGHT,
                                        vUv(inner).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(
                                        Direction.EAST,
                                        Half.BOTTOM,
                                        StairsShape.INNER_LEFT,
                                        vUv(inner).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(
                                        Direction.WEST,
                                        Half.BOTTOM,
                                        StairsShape.INNER_LEFT,
                                        vUv(inner).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, v(inner))
                                .select(
                                        Direction.NORTH,
                                        Half.BOTTOM,
                                        StairsShape.INNER_LEFT,
                                        vUv(inner).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.EAST,
                                        Half.TOP,
                                        StairsShape.STRAIGHT,
                                        vUv(straight).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.WEST,
                                        Half.TOP,
                                        StairsShape.STRAIGHT,
                                        vUv(straight)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.SOUTH,
                                        Half.TOP,
                                        StairsShape.STRAIGHT,
                                        vUv(straight)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(
                                        Direction.NORTH,
                                        Half.TOP,
                                        StairsShape.STRAIGHT,
                                        vUv(straight)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(
                                        Direction.EAST,
                                        Half.TOP,
                                        StairsShape.OUTER_RIGHT,
                                        vUv(outer)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(
                                        Direction.WEST,
                                        Half.TOP,
                                        StairsShape.OUTER_RIGHT,
                                        vUv(outer)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(
                                        Direction.SOUTH,
                                        Half.TOP,
                                        StairsShape.OUTER_RIGHT,
                                        vUv(outer)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.NORTH,
                                        Half.TOP,
                                        StairsShape.OUTER_RIGHT,
                                        vUv(outer).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.EAST,
                                        Half.TOP,
                                        StairsShape.OUTER_LEFT,
                                        vUv(outer).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.WEST,
                                        Half.TOP,
                                        StairsShape.OUTER_LEFT,
                                        vUv(outer)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.SOUTH,
                                        Half.TOP,
                                        StairsShape.OUTER_LEFT,
                                        vUv(outer)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(
                                        Direction.NORTH,
                                        Half.TOP,
                                        StairsShape.OUTER_LEFT,
                                        vUv(outer)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(
                                        Direction.EAST,
                                        Half.TOP,
                                        StairsShape.INNER_RIGHT,
                                        vUv(inner)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(
                                        Direction.WEST,
                                        Half.TOP,
                                        StairsShape.INNER_RIGHT,
                                        vUv(inner)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(
                                        Direction.SOUTH,
                                        Half.TOP,
                                        StairsShape.INNER_RIGHT,
                                        vUv(inner)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.NORTH,
                                        Half.TOP,
                                        StairsShape.INNER_RIGHT,
                                        vUv(inner).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.EAST,
                                        Half.TOP,
                                        StairsShape.INNER_LEFT,
                                        vUv(inner).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.WEST,
                                        Half.TOP,
                                        StairsShape.INNER_LEFT,
                                        vUv(inner)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(
                                        Direction.SOUTH,
                                        Half.TOP,
                                        StairsShape.INNER_LEFT,
                                        vUv(inner)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(
                                        Direction.NORTH,
                                        Half.TOP,
                                        StairsShape.INNER_LEFT,
                                        vUv(inner)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)));
    }

    private static Variant v(ResourceLocation model) {
        return Variant.variant().with(VariantProperties.MODEL, model);
    }

    private static Variant vUv(ResourceLocation model) {
        return Variant.variant()
                .with(VariantProperties.MODEL, model)
                .with(VariantProperties.UV_LOCK, true);
    }

    // === Public API ===

    public void create(Block block, ResourceLocation model) {
        this.blockStateOutput.accept(simpleBlock(block, model));
    }

    public ResourceLocation createTintedCube(Block block, int tintIndex) {
        return createTintedCube(block, TextureMapping.getBlockTexture(block), tintIndex);
    }

    public ResourceLocation createTintedCube(Block block, ResourceLocation texture, int tintIndex) {
        ResourceLocation model = ModelLocationUtils.getModelLocation(block);
        modelOutput.accept(model, () -> createTintedCubeJson(texture, tintIndex));
        create(block, model);
        return model;
    }

    public ResourceLocation createLayeredCube(
                                              Block block, TextureRef particle, BlockModelLayer... layers) {
        ResourceLocation model = ModelLocationUtils.getModelLocation(block);
        modelOutput.accept(model, () -> createLayeredCubeJson(particle, layers));
        create(block, model);
        return model;
    }

    public ResourceLocation mcLoc(String id) {
        return ResourceLocation.withDefaultNamespace(id);
    }

    public ResourceLocation modLoc(String id) {
        return ResourceLocation.fromNamespaceAndPath(parent.getModid(), id);
    }

    public RegistryLibLegacyBlockModelBuilder withBuilder(
                                                          RegistryLibLegacyBlockModelBuilder.BuilderSeed seed) {
        return new RegistryLibLegacyBlockModelBuilder(modelOutput, seed);
    }

    public RegistryLibLegacyBlockModelBuilder withBuilder(
                                                          RegistryLibLegacyBlockModelBuilder.BuilderSeed seed, TextureMapping texture) {
        return withBuilder(seed);
    }

    public RegistryLibLegacyBlockModelBuilder getBuilder() {
        return withBuilder(new RegistryLibLegacyBlockModelBuilder.BuilderSeed());
    }

    public RegistryLibLegacyBlockModelBuilder withParent(ModelTemplate template) {
        return withBuilder(RegistryLibLegacyBlockModelBuilder.BuilderSeed.of(template));
    }

    public RegistryLibLegacyBlockModelBuilder withParent(
                                                         ModelTemplate template, TextureMapping texture) {
        return withBuilder(RegistryLibLegacyBlockModelBuilder.BuilderSeed.of(template));
    }

    public RegistryLibLegacyBlockModelBuilder withParent(TexturedModel model) {
        return withBuilder(RegistryLibLegacyBlockModelBuilder.BuilderSeed.of(model.getTemplate()));
    }

    public ResourceLocation blockTexture(Block block) {
        return TextureMapping.getBlockTexture(block);
    }

    public ResourceLocation blockTexture(Block block, String suffix) {
        return TextureMapping.getBlockTexture(block, suffix);
    }

    public void generateWithTemplate(Block block, ModelTemplate template, TextureMapping textures) {
        create(block, template.create(block, textures, modelOutput));
    }

    public void generateCropStages(CropBlock block, TextureRef... stageTextures) {
        ResourceLocation[] models = new ResourceLocation[stageTextures.length];
        for (int i = 0; i < stageTextures.length; i++) {
            ResourceLocation blockTexture = TextureMapping.getBlockTexture(block);
            models[i] = blockTexture.withSuffix("_stage" + i);
            ResourceLocation texture = stageTextures[i].id();
            modelOutput.accept(models[i], () -> createCropStageJson(texture));
        }
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block)
                        .with(
                                PropertyDispatch.property(CropBlock.AGE)
                                        .generate(
                                                age -> {
                                                    int modelIndex = Math.min(
                                                            models.length - 1, age * models.length / (block.getMaxAge() + 1));
                                                    return Variant.variant()
                                                            .with(VariantProperties.MODEL, models[modelIndex]);
                                                })));
    }

    public void generate(Block block, TexturedModel.Provider texture) {
        blockStateOutput.accept(simpleBlock(block, texture.create(block, modelOutput)));
    }

    public void generateAxisBlock(RotatedPillarBlock block) {
        generateAxisBlock(block, blockTexture(block));
    }

    public void generateAxisBlock(RotatedPillarBlock block, ResourceLocation baseName) {
        generateAxisBlock(block, baseName.withSuffix("_side"), baseName.withSuffix("_end"));
    }

    public void generateAxisBlock(
                                  RotatedPillarBlock block, ResourceLocation side, ResourceLocation end) {
        ResourceLocation vertical = ModelTemplates.CUBE_COLUMN.create(block, TextureMapping.column(side, end), modelOutput);
        ResourceLocation horizontal = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(
                block, TextureMapping.column(side, end), modelOutput);
        blockStateOutput.accept(rotatedPillarWithHorizontalVariant(block, vertical, horizontal));
    }

    /** 使用已经生成好的 竖直/水平 两个模型位置生成柱状方块 blockstate。 */
    public void generateAxisBlockWithModels(
                                            RotatedPillarBlock block, ResourceLocation vertical, ResourceLocation horizontal) {
        blockStateOutput.accept(rotatedPillarWithHorizontalVariant(block, vertical, horizontal));
    }

    public void generateHorizontalBlock(
                                        Block block, ResourceLocation side, ResourceLocation front, ResourceLocation top) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.FRONT, front)
                .put(TextureSlot.TOP, top);
        generateHorizontalBlock(
                block, ModelTemplates.CUBE_ORIENTABLE.create(block, mapping, modelOutput));
    }

    public void generateHorizontalBlock(Block block, ResourceLocation model) {
        blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(
                        block, Variant.variant().with(VariantProperties.MODEL, model))
                        .with(horizontalFacingDispatch(model)));
    }

    private static PropertyDispatch horizontalFacingDispatch(ResourceLocation model) {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                .select(
                        Direction.EAST,
                        Variant.variant()
                                .with(VariantProperties.MODEL, model)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .select(
                        Direction.SOUTH,
                        Variant.variant()
                                .with(VariantProperties.MODEL, model)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .select(
                        Direction.WEST,
                        Variant.variant()
                                .with(VariantProperties.MODEL, model)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, model));
    }

    public void generateStairsBlock(StairBlock block, ResourceLocation texture) {
        generateStairsBlock(block, texture, texture, texture);
    }

    public void generateStairsBlock(
                                    StairBlock block, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top);
        blockStateOutput.accept(
                stairs(
                        block,
                        ModelTemplates.STAIRS_INNER.create(block, textures, modelOutput),
                        ModelTemplates.STAIRS_STRAIGHT.create(block, textures, modelOutput),
                        ModelTemplates.STAIRS_OUTER.create(block, textures, modelOutput)));
    }

    public void generateSlabBlock(
                                  SlabBlock block, ResourceLocation doubleSlab, ResourceLocation texture) {
        generateSlabBlock(block, doubleSlab, texture, texture, texture);
    }

    public void generateSlabBlock(
                                  SlabBlock block,
                                  ResourceLocation doubleSlab,
                                  ResourceLocation side,
                                  ResourceLocation bottom,
                                  ResourceLocation top) {
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top);
        ResourceLocation slabTop = ModelTemplates.SLAB_TOP.create(block, textures, modelOutput);
        ResourceLocation slabBottom = ModelTemplates.SLAB_BOTTOM.create(block, textures, modelOutput);
        blockStateOutput.accept(slab(block, slabBottom, slabTop, doubleSlab));
    }

    public void createNonTemplateModelBlock(Block block) {
        createNonTemplateModelBlock(block, TextureMapping.getBlockTexture(block));
    }

    public void createNonTemplateModelBlock(Block block, ResourceLocation particleTexture) {
        TextureMapping textures = new TextureMapping().put(TextureSlot.PARTICLE, particleTexture);
        blockStateOutput.accept(
                simpleBlock(block, ModelTemplates.PARTICLE_ONLY.create(block, textures, modelOutput)));
    }

    // === JSON writers ===

    private static JsonObject createTintedCubeJson(ResourceLocation texture, int tintIndex) {
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

    private static JsonObject createCropStageJson(ResourceLocation texture) {
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
            textures.addProperty("layer" + i, layers[i].texture().id().toString());
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
        JsonObject face = new JsonObject();
        face.add("uv", vector(0, 0, 16, 16));
        face.addProperty("texture", "#" + textureKey);
        face.addProperty("cullface", cullface);
        if (layer.hasTint()) {
            face.addProperty("tintindex", layer.tintIndex());
        }
        return face;
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
