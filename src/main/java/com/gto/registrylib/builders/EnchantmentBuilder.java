package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.datagen.GeneratorType;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.util.entry.EnchantmentEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * 附魔 Builder，通过流畅 API 定义数据驱动附魔并自动生成 JSON + 语言条目 + 标签。
 *
 * <p>Fluent builder for data-driven enchantments. Generates enchantment JSON, lang entries, and
 * enchantment tag entries during datagen.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * public static final EnchantmentEntry ORE_FORTUNE = REGISTRYLIB
 *     .enchantment("ore_fortune")
 *     .lang("Ore Fortune")
 *     .supportedItems(ItemTags.MINING_ENCHANTABLE)
 *     .weight(5).maxLevel(3)
 *     .minCost(15, 9).maxCost(65, 9)
 *     .anvilCost(4)
 *     .slots("mainhand")
 *     .vanillaEffect("minecraft:block_experience", effect -> effect
 *         .add("type", "minecraft:add")
 *         .add("value", value -> value.add("type", "minecraft:linear")
 *             .add("base", 1.0).add("per_level_above_first", 1.0)))
 *     .register();
 * }</pre>
 *
 * @param <P> the parent type (for builder chaining)
 */
public class EnchantmentBuilder<P> {

    private final RegistryCore core;
    private final P parent;
    private final String name;

    // Enchantment definition fields
    private String supportedItems;
    private String primaryItems;
    private int weight = 5;
    private int maxLevel = 1;
    private int minCostBase = 1;
    private int minCostPerLevel = 0;
    private int maxCostBase = 21;
    private int maxCostPerLevel = 0;
    private int anvilCost = 1;
    private final List<String> slots = new ArrayList<>();
    private final Map<String, List<JsonObject>> effects = new LinkedHashMap<>();
    private final List<String> exclusiveWith = new ArrayList<>();

    // Lang entries
    private String langEn;
    private final List<Consumer<RegistryCore>> langCallbacks = new ArrayList<>();

    // Tags
    private final List<TagKey<Enchantment>> tags = new ArrayList<>();

    protected EnchantmentBuilder(RegistryCore core, P parent, String name) {
        this.core = core;
        this.parent = parent;
        this.name = name;
    }

    public static <P> EnchantmentBuilder<P> create(RegistryCore core, P parent, String name) {
        return new EnchantmentBuilder<>(core, parent, name);
    }

    // === Description / Lang ===

    /**
     * 设置英文名称（同时作为附魔描述翻译键的值）。
     *
     * <p>Sets the English display name for this enchantment.
     */
    @StandardAPI
    public EnchantmentBuilder<P> lang(@NotNull String englishName) {
        this.langEn = englishName;
        return this;
    }

    /**
     * 为指定的语言提供器添加翻译。
     *
     * <p>Adds a translation for the specified lang provider type.
     */
    @StandardAPI
    public EnchantmentBuilder<P> lang(
                                      @NotNull ProviderType<? extends RegistryLibLangProvider> type,
                                      @NotNull String localizedName) {
        langCallbacks.add(c -> c.addDataGenerator(type, prov -> prov.add(langKey(), localizedName)));
        return this;
    }

    // === Enchantment Definition ===

    /**
     * 设置可附魔的物品（使用物品标签引用字符串，如 {@code "#minecraft:enchantable/mining"}）。
     *
     * <p>Sets supported items using a tag reference string (e.g. {@code "#minecraft:enchantable/mining"}).
     */
    @StandardAPI
    public EnchantmentBuilder<P> supportedItems(@NotNull String tagRef) {
        this.supportedItems = tagRef;
        return this;
    }

    /**
     * 设置可附魔的物品（使用物品标签）。
     *
     * <p>Sets supported items using an item TagKey.
     */
    @SyntaxSugar("supportedItems(\"#\" + tag.location())")
    public EnchantmentBuilder<P> supportedItems(@NotNull TagKey<Item> tag) {
        return supportedItems("#" + tag.location());
    }

    /**
     * 设置主要物品（可选，附魔台优先选择）。
     *
     * <p>Sets primary items (items preferred by the enchanting table).
     */
    @StandardAPI
    public EnchantmentBuilder<P> primaryItems(@NotNull String tagRef) {
        this.primaryItems = tagRef;
        return this;
    }

    @SyntaxSugar("primaryItems(\"#\" + tag.location())")
    public EnchantmentBuilder<P> primaryItems(@NotNull TagKey<Item> tag) {
        return primaryItems("#" + tag.location());
    }

    /** 设置附魔权重（出现概率，值越大越常见）。 */
    @StandardAPI
    public EnchantmentBuilder<P> weight(int weight) {
        this.weight = weight;
        return this;
    }

    /** 设置附魔最大等级。 */
    @StandardAPI
    public EnchantmentBuilder<P> maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    /**
     * 设置最低附魔开销。
     *
     * @param base the base cost at level 1
     * @param perLevelAboveFirst cost increase per level above 1
     */
    @StandardAPI
    public EnchantmentBuilder<P> minCost(int base, int perLevelAboveFirst) {
        this.minCostBase = base;
        this.minCostPerLevel = perLevelAboveFirst;
        return this;
    }

    /**
     * 设置最高附魔开销。
     *
     * @param base the base cost at level 1
     * @param perLevelAboveFirst cost increase per level above 1
     */
    @StandardAPI
    public EnchantmentBuilder<P> maxCost(int base, int perLevelAboveFirst) {
        this.maxCostBase = base;
        this.maxCostPerLevel = perLevelAboveFirst;
        return this;
    }

    /** 设置铁砧开销。 */
    @StandardAPI
    public EnchantmentBuilder<P> anvilCost(int anvilCost) {
        this.anvilCost = anvilCost;
        return this;
    }

    /**
     * 设置附魔适用的装备槽位。
     *
     * <p>Sets applicable equipment slot groups (e.g. "mainhand", "armor", "any").
     */
    @StandardAPI
    public EnchantmentBuilder<P> slots(@NotNull String... slotGroups) {
        for (String s : slotGroups) {
            slots.add(s);
        }
        return this;
    }

    /**
     * 设置互斥附魔集合。
     *
     * <p>Sets enchantments that are exclusive with this one.
     */
    @SafeVarargs
    @StandardAPI
    public final EnchantmentBuilder<P> exclusiveWith(@NotNull ResourceKey<Enchantment>... keys) {
        for (var key : keys) {
            exclusiveWith.add(key.identifier().toString());
        }
        return this;
    }

    // === Effects ===

    /**
     * 添加原版附魔效果（使用效果类型 ID 和 JSON 构建器）。
     *
     * <p>Adds an enchantment effect using a vanilla effect component type ID and a JSON builder.
     *
     * <pre>{@code
     * .vanillaEffect("minecraft:block_experience", effect -> effect
     *     .add("type", "minecraft:add")
     *     .add("value", value -> value
     *         .add("type", "minecraft:linear")
     *         .add("base", 1.0)
     *         .add("per_level_above_first", 1.0)))
     * }</pre>
     *
     * @param effectTypeId the effect component type ID (e.g. "minecraft:block_experience")
     * @param effectBuilder a builder that constructs the "effect" JSON object
     */
    @StandardAPI
    public EnchantmentBuilder<P> vanillaEffect(
                                               @NotNull String effectTypeId,
                                               @NotNull Consumer<JsonBuilder> effectBuilder) {
        JsonBuilder builder = new JsonBuilder();
        effectBuilder.accept(builder);
        JsonObject entry = new JsonObject();
        entry.add("effect", builder.build());
        effects.computeIfAbsent(effectTypeId, k -> new ArrayList<>()).add(entry);
        return this;
    }

    /**
     * 添加自定义附魔效果（使用已注册的 DataComponentType 和 JSON 构建器）。
     *
     * <p>Adds a custom mod effect using the mod's registered {@link DataComponentType} and a JSON builder.
     *
     * @param effectTypeId the namespaced effect component type ID (e.g. "mymod:auto_smelt")
     * @param effectBuilder a builder that constructs the "effect" JSON object
     */
    @StandardAPI
    public EnchantmentBuilder<P> customEffect(
                                              @NotNull String effectTypeId,
                                              @NotNull Consumer<JsonBuilder> effectBuilder) {
        return vanillaEffect(effectTypeId, effectBuilder);
    }

    /**
     * 添加自定义附魔效果（使用已注册的 DataComponentType 和原始 JSON 对象）。
     *
     * <p>Adds a custom mod effect using a raw JsonObject as the effect data.
     */
    @StandardAPI
    public EnchantmentBuilder<P> customEffect(
                                              @NotNull String effectTypeId,
                                              @NotNull JsonObject effectJson) {
        JsonObject entry = new JsonObject();
        entry.add("effect", effectJson);
        effects.computeIfAbsent(effectTypeId, k -> new ArrayList<>()).add(entry);
        return this;
    }

    // === Tags ===

    /**
     * 将此附魔添加到指定标签。
     *
     * <p>Adds this enchantment to the specified enchantment tag (e.g. for enchanting table availability).
     */
    @SafeVarargs
    @StandardAPI
    public final EnchantmentBuilder<P> addTag(@NotNull TagKey<Enchantment>... enchantmentTags) {
        for (var t : enchantmentTags) {
            tags.add(t);
        }
        return this;
    }

    // === Registration ===

    /**
     * 注册语言条目，生成附魔 JSON 和标签 JSON，返回 {@link EnchantmentEntry}。
     *
     * <p>Registers lang entries, generates enchantment JSON and tag JSON during datagen, and
     * returns an {@link EnchantmentEntry}.
     */
    @StandardAPI
    public EnchantmentEntry register() {
        if (supportedItems == null) {
            throw new IllegalStateException(
                    "EnchantmentBuilder for '" + name + "' requires supportedItems() before register()");
        }

        Identifier id = Identifier.fromNamespaceAndPath(core.getModid(), name);
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);

        // Register lang
        if (langEn != null) {
            core.addLang("enchantment", id, langEn);
        }
        for (var cb : langCallbacks) {
            cb.accept(core);
        }

        // Generate enchantment JSON
        if (core.doDatagen()) {
            core.addDataGenerator(
                    ProviderType.ENCHANTMENT_DATA,
                    (RegistryLibEnchantmentDataCollector collector) -> collector.add(name, buildJson()));

            // Generate tag entries
            if (!tags.isEmpty()) {
                core.addDataGenerator(
                        ProviderType.ENCHANTMENT_DATA,
                        (RegistryLibEnchantmentDataCollector collector) -> {
                            for (var tag : tags) {
                                collector.addTag(tag, id);
                            }
                        });
            }
        }

        return new EnchantmentEntry(key);
    }

    @SyntaxSugar("register(); return parent")
    public P build() {
        register();
        return parent;
    }

    // === Internal ===

    private String langKey() {
        return "enchantment." + core.getModid() + "." + name;
    }

    private JsonObject buildJson() {
        JsonObject json = new JsonObject();

        // description
        JsonObject desc = new JsonObject();
        desc.addProperty("translate", langKey());
        json.add("description", desc);

        // supported_items
        json.addProperty("supported_items", supportedItems);

        // primary_items
        if (primaryItems != null) {
            json.addProperty("primary_items", primaryItems);
        }

        // weight
        json.addProperty("weight", weight);

        // max_level
        json.addProperty("max_level", maxLevel);

        // min_cost
        JsonObject minCost = new JsonObject();
        minCost.addProperty("base", minCostBase);
        minCost.addProperty("per_level_above_first", minCostPerLevel);
        json.add("min_cost", minCost);

        // max_cost
        JsonObject maxCost = new JsonObject();
        maxCost.addProperty("base", maxCostBase);
        maxCost.addProperty("per_level_above_first", maxCostPerLevel);
        json.add("max_cost", maxCost);

        // anvil_cost
        json.addProperty("anvil_cost", anvilCost);

        // slots
        JsonArray slotsArr = new JsonArray();
        for (String s : slots) {
            slotsArr.add(s);
        }
        json.add("slots", slotsArr);

        // exclusive_set
        if (!exclusiveWith.isEmpty()) {
            if (exclusiveWith.size() == 1) {
                json.addProperty("exclusive_set", exclusiveWith.getFirst());
            } else {
                JsonArray arr = new JsonArray();
                for (String s : exclusiveWith) {
                    arr.add(s);
                }
                json.add("exclusive_set", arr);
            }
        }

        // effects
        if (!effects.isEmpty()) {
            JsonObject effectsObj = new JsonObject();
            for (var entry : effects.entrySet()) {
                JsonArray arr = new JsonArray();
                for (JsonObject obj : entry.getValue()) {
                    arr.add(obj);
                }
                effectsObj.add(entry.getKey(), arr);
            }
            json.add("effects", effectsObj);
        }

        return json;
    }

    // === JSON Builder helper ===

    /**
     * 用于构建嵌套 JSON 结构的辅助类。
     *
     * <p>Helper class for building nested JSON structures fluently.
     */
    public static class JsonBuilder {
        private final JsonObject obj = new JsonObject();

        public JsonBuilder add(String key, String value) {
            obj.addProperty(key, value);
            return this;
        }

        public JsonBuilder add(String key, int value) {
            obj.addProperty(key, value);
            return this;
        }

        public JsonBuilder add(String key, float value) {
            obj.addProperty(key, value);
            return this;
        }

        public JsonBuilder add(String key, double value) {
            obj.addProperty(key, value);
            return this;
        }

        public JsonBuilder add(String key, boolean value) {
            obj.addProperty(key, value);
            return this;
        }

        public JsonBuilder add(String key, Consumer<JsonBuilder> nested) {
            JsonBuilder child = new JsonBuilder();
            nested.accept(child);
            obj.add(key, child.build());
            return this;
        }

        public JsonBuilder add(String key, JsonElement element) {
            obj.add(key, element);
            return this;
        }

        public JsonObject build() {
            return obj;
        }
    }

    // === Inner collector interface (used by the datagen provider) ===

    /**
     * 附魔数据收集器接口，由 datagen 提供器实现。
     *
     * <p>Interface for collecting enchantment definitions and tag entries during datagen.
     */
    public interface RegistryLibEnchantmentDataCollector {

        void add(String name, JsonObject enchantmentJson);

        void addTag(TagKey<Enchantment> tag, Identifier enchantmentId);
    }
}
