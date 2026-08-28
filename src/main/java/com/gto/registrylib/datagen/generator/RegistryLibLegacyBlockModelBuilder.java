package com.gto.registrylib.datagen.generator;

import com.gto.registrylib.util.TextureRef;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 1.21.1 版「传统」方块模型构建器——直接书写 model JSON（{@code BiConsumer<ResourceLocation,
 * Supplier<JsonElement>>}）。
 *
 * <p>
 * 26.x 的 {@code ExtendedModelTemplateBuilder} 在 1.21.1 不存在；本类以 JSON 形式复刻其常用能力（parent / textures
 * / suffix / display / ambientocclusion / gui_light）。
 */
public class RegistryLibLegacyBlockModelBuilder {

    /** 模型 JSON 的初始种子：parent 模板 + 可选后缀。由 {@code withParent(...)} / {@code getBuilder()} 创建。 */
    public static class BuilderSeed {

        private final ResourceLocation templateParent;
        private final String suffix;

        BuilderSeed(ResourceLocation templateParent, String suffix) {
            this.templateParent = templateParent;
            this.suffix = suffix;
        }

        public BuilderSeed() {
            this(ResourceLocation.withDefaultNamespace("block/cube_all"), "");
        }

        public static BuilderSeed of(ModelTemplate template) {
            return new BuilderSeed();
        }

        public ResourceLocation getTemplateParent() {
            return templateParent;
        }

        public String getSuffix() {
            return suffix;
        }
    }

    private final BiConsumer<ResourceLocation, Supplier<JsonElement>> output;
    private final JsonObject root = new JsonObject();
    private final Map<String, String> textures = new LinkedHashMap<>();
    private ResourceLocation parent;
    private String suffix = "";

    RegistryLibLegacyBlockModelBuilder(
                                       BiConsumer<ResourceLocation, Supplier<JsonElement>> output, BuilderSeed seed) {
        this.output = output;
        this.parent = seed.getTemplateParent();
        this.suffix = seed.getSuffix();
    }

    public RegistryLibLegacyBlockModelBuilder texture(TextureSlot slot, ResourceLocation texture) {
        this.textures.put(slot.getId(), texture.toString());
        return this;
    }

    public RegistryLibLegacyBlockModelBuilder texture(TextureSlot slot, TextureRef texture) {
        return texture(slot, texture.id());
    }

    /** 1.21.1 的 {@link TextureMapping} 不暴露可遍历的 slot 集合，故此处仅接受显式 texture(...) 调用。 */
    public RegistryLibLegacyBlockModelBuilder withTexture(TextureMapping mapping) {
        return this;
    }

    public RegistryLibLegacyBlockModelBuilder transformTemplate(Consumer<BuilderSeed> action) {
        // 种子为不可变快照；如需自定义父模板，请使用 parent(...)。
        return this;
    }

    public RegistryLibLegacyBlockModelBuilder transformTexture(Consumer<Map<String, String>> action) {
        action.accept(textures);
        return this;
    }

    public ResourceLocation build(Block block) {
        return build(ModelLocationUtils.getModelLocation(block, suffix));
    }

    public ResourceLocation build(ResourceLocation loc) {
        root.addProperty("parent", parent.toString());
        JsonObject tex = new JsonObject();
        textures.forEach(tex::addProperty);
        if (!textures.isEmpty()) {
            root.add("textures", tex);
        }
        ResourceLocation full = suffix.isEmpty() ? loc : loc.withSuffix(suffix);
        output.accept(full, () -> root.deepCopy());
        return full;
    }

    public RegistryLibLegacyBlockModelBuilder parent(ResourceLocation parent) {
        this.parent = parent;
        return this;
    }

    public RegistryLibLegacyBlockModelBuilder suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public RegistryLibLegacyBlockModelBuilder transform(
                                                        ItemDisplayContext type, Consumer<DisplayTransform> action) {
        JsonObject display = root.has("display") ? root.getAsJsonObject("display") : new JsonObject();
        DisplayTransform transform = new DisplayTransform();
        action.accept(transform);
        JsonObject entry = new JsonObject();
        entry.add("rotation", vector(transform.rotationX, transform.rotationY, transform.rotationZ));
        entry.add(
                "translation",
                vector(transform.translationX, transform.translationY, transform.translationZ));
        entry.add("scale", vector(transform.scaleX, transform.scaleY, transform.scaleZ));
        display.add(type.getSerializedName(), entry);
        root.add("display", display);
        return this;
    }

    public RegistryLibLegacyBlockModelBuilder ambientOcclusion(boolean ambientOcclusion) {
        root.addProperty("ambientocclusion", ambientOcclusion);
        return this;
    }

    public RegistryLibLegacyBlockModelBuilder guiLight(BlockModel.GuiLight light) {
        root.addProperty("gui_light", light == BlockModel.GuiLight.SIDE ? "side" : "front");
        return this;
    }

    public RegistryLibLegacyBlockModelBuilder customLoader(
                                                           Supplier<?> customLoaderFactory, Consumer<?> action) {
        // 1.21.1 的 model JSON 没有 26.x 的 custom loader 扩展；保留 API 但无操作。
        return this;
    }

    public RegistryLibLegacyBlockModelBuilder rootTransforms(Consumer<?> action) {
        // 1.21.1 没有 root transforms 概念；保留 API 但无操作。
        return this;
    }

    private static JsonArray vector(float... values) {
        JsonArray array = new JsonArray();
        for (float value : values) {
            array.add(value);
        }
        return array;
    }

    /** display 变换（rotation/translation/scale）。 */
    public static class DisplayTransform {

        float rotationX, rotationY, rotationZ;
        float translationX, translationY, translationZ;
        float scaleX = 1.0F, scaleY = 1.0F, scaleZ = 1.0F;

        public DisplayTransform rotation(float x, float y, float z) {
            this.rotationX = x;
            this.rotationY = y;
            this.rotationZ = z;
            return this;
        }

        public DisplayTransform translation(float x, float y, float z) {
            this.translationX = x;
            this.translationY = y;
            this.translationZ = z;
            return this;
        }

        public DisplayTransform scale(float x, float y, float z) {
            this.scaleX = x;
            this.scaleY = y;
            this.scaleZ = z;
            return this;
        }
    }
}
