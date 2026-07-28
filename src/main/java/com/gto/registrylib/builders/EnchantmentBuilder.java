package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibEnchantmentTagsProvider;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.util.entry.EnchantmentEntry;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 附魔 Builder，通过流畅 API 定义数据驱动附魔并自动生成附魔 JSON + 语言条目 + 标签。
 *
 * <p>
 * Fluent builder for data-driven enchantments. Uses Minecraft's {@link Enchantment.Builder} API
 * internally to construct enchantment definitions that are generated during datagen via {@link
 * net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider}.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * 
 * public static final EnchantmentEntry ORE_FORTUNE = REGISTRYLIB
 *         .enchantment("ore_fortune")
 *         .lang("Ore Fortune")
 *         .supportedItems(ItemTags.MINING_ENCHANTABLE)
 *         .weight(5).maxLevel(3)
 *         .minCost(15, 9).maxCost(65, 9)
 *         .anvilCost(4)
 *         .slots(EquipmentSlotGroup.MAINHAND)
 *         .withEffect(EnchantmentEffectComponents.BLOCK_EXPERIENCE,
 *                 new AddValue(LevelBasedValue.perLevel(1.0f, 1.0f)))
 *         .register();
 * }</pre>
 *
 * @param <P> the parent type (for builder chaining)
 */
public class EnchantmentBuilder<P> {

    private final RegistryCore core;
    private final P parent;
    private final String name;
    private boolean registered;

    // Enchantment definition fields
    private TagKey<Item> supportedItems;
    private TagKey<Item> primaryItems;
    private int weight = 5;
    private int maxLevel = 1;
    private int minCostBase = 1;
    private int minCostPerLevel = 0;
    private int maxCostBase = 21;
    private int maxCostPerLevel = 0;
    private int anvilCost = 1;
    private final List<EquipmentSlotGroup> slots = new ArrayList<>();
    private final List<ResourceKey<Enchantment>> exclusiveWith = new ArrayList<>();

    // Effect callbacks — applied to Enchantment.Builder during bootstrap
    private final List<Consumer<Enchantment.Builder>> effectCallbacks = new ArrayList<>();

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
     * <p>
     * Sets the English display name for this enchantment.
     */
    @StandardAPI
    public EnchantmentBuilder<P> lang(@NotNull String englishName) {
        this.langEn = englishName;
        return this;
    }

    /**
     * 为指定的语言提供器添加翻译。
     *
     * <p>
     * Adds a translation for the specified lang provider type.
     */
    @StandardAPI
    public EnchantmentBuilder<P> lang(
                                      @NotNull ProviderType<? extends RegistryLibLangProvider> type,
                                      @NotNull String localizedName) {
        langCallbacks.add(c -> c.addDataGenerator(type, prov -> prov.add(langKey(), localizedName)));
        return this;
    }

    /**
     * Register display names for multiple locales at once.
     *
     * @param localeToName map of locale code (e.g. {@code "en_us"}, {@code "zh_cn"}) to display name
     */
    @StandardAPI
    public EnchantmentBuilder<P> lang(@NotNull Map<String, String> localeToName) {
        for (var entry : localeToName.entrySet()) {
            String locale = entry.getKey().toLowerCase(Locale.ROOT);
            if ("en_us".equals(locale)) {
                lang(entry.getValue());
            } else {
                lang(core.locale(locale), entry.getValue());
            }
        }
        return this;
    }

    // === Enchantment Definition ===

    /**
     * 设置可附魔的物品（使用物品标签）。
     *
     * <p>
     * Sets supported items using an item TagKey.
     */
    @StandardAPI
    public EnchantmentBuilder<P> supportedItems(@NotNull TagKey<Item> tag) {
        this.supportedItems = tag;
        return this;
    }

    /**
     * 设置主要物品（可选，附魔台优先选择）。
     *
     * <p>
     * Sets primary items (items preferred by the enchanting table).
     */
    @StandardAPI
    public EnchantmentBuilder<P> primaryItems(@NotNull TagKey<Item> tag) {
        this.primaryItems = tag;
        return this;
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
     * @param base               the base cost at level 1
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
     * @param base               the base cost at level 1
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
     * <p>
     * Sets applicable equipment slot groups.
     */
    @StandardAPI
    public EnchantmentBuilder<P> slots(@NotNull EquipmentSlotGroup... slotGroups) {
        for (var s : slotGroups) {
            slots.add(s);
        }
        return this;
    }

    /**
     * 设置互斥附魔集合。
     *
     * <p>
     * Sets enchantments that are exclusive with this one.
     */
    @SafeVarargs
    @StandardAPI
    public final EnchantmentBuilder<P> exclusiveWith(@NotNull ResourceKey<Enchantment>... keys) {
        for (var key : keys) {
            exclusiveWith.add(key);
        }
        return this;
    }

    // === Effects ===

    /**
     * 添加条件效果（包含条件列表类型的效果组件）。
     *
     * <p>
     * Adds a conditional effect entry to the specified effect component type.
     *
     * <pre>{@code
     * .withEffect(EnchantmentEffectComponents.BLOCK_EXPERIENCE,
     *     new AddValue(LevelBasedValue.perLevel(1.0f, 1.0f)))
     * }</pre>
     *
     * @param type   the effect component type
     * @param effect the effect instance
     */
    @StandardAPI
    public <E> EnchantmentBuilder<P> withEffect(
                                                @NotNull DataComponentType<List<ConditionalEffect<E>>> type, @NotNull E effect) {
        effectCallbacks.add(builder -> builder.withEffect(type, effect));
        return this;
    }

    /**
     * 添加条件效果，使用延迟求值的类型引用（适用于模组注册的效果组件类型）。
     *
     * <p>
     * Adds a conditional effect using a lazily-evaluated type reference. Use this when the {@link
     * DataComponentType} is obtained from a {@link com.gto.registrylib.util.entry.RegistryEntry} that
     * may not yet be bound at class loading time.
     *
     * <pre>{@code
     * .withEffect(MY_EFFECT::get, new MyEffect(1.0f))
     * }</pre>
     *
     * @param typeSupplier supplier for the effect component type
     * @param effect       the effect instance
     */
    @StandardAPI
    public <E> EnchantmentBuilder<P> withEffect(
                                                @NotNull Supplier<DataComponentType<List<ConditionalEffect<E>>>> typeSupplier,
                                                @NotNull E effect) {
        effectCallbacks.add(builder -> builder.withEffect(typeSupplier.get(), effect));
        return this;
    }

    /**
     * 添加带条件的条件效果。
     *
     * <p>
     * Adds a conditional effect with a loot condition.
     *
     * @param type      the effect component type
     * @param effect    the effect instance
     * @param condition the loot item condition
     */
    @StandardAPI
    public <E> EnchantmentBuilder<P> withEffect(
                                                @NotNull DataComponentType<List<ConditionalEffect<E>>> type,
                                                @NotNull E effect,
                                                @NotNull LootItemCondition.Builder condition) {
        effectCallbacks.add(builder -> builder.withEffect(type, effect, condition));
        return this;
    }

    /**
     * 添加带条件的条件效果，使用延迟求值的类型引用。
     *
     * <p>
     * Adds a conditional effect with a loot condition, using a lazily-evaluated type reference.
     *
     * @param typeSupplier supplier for the effect component type
     * @param effect       the effect instance
     * @param condition    the loot item condition
     */
    @StandardAPI
    public <E> EnchantmentBuilder<P> withEffect(
                                                @NotNull Supplier<DataComponentType<List<ConditionalEffect<E>>>> typeSupplier,
                                                @NotNull E effect,
                                                @NotNull LootItemCondition.Builder condition) {
        effectCallbacks.add(builder -> builder.withEffect(typeSupplier.get(), effect, condition));
        return this;
    }

    /**
     * 添加特殊效果（非条件列表类型的效果组件，如 prevent_equipment_drop）。
     *
     * <p>
     * Adds a special (non-list) effect to the enchantment.
     *
     * @param type   the effect component type
     * @param effect the effect instance
     */
    @StandardAPI
    public <E> EnchantmentBuilder<P> withSpecialEffect(
                                                       @NotNull DataComponentType<E> type, @NotNull E effect) {
        effectCallbacks.add(builder -> builder.withSpecialEffect(type, effect));
        return this;
    }

    /**
     * 添加特殊效果，使用延迟求值的类型引用。
     *
     * <p>
     * Adds a special (non-list) effect using a lazily-evaluated type reference.
     *
     * @param typeSupplier supplier for the effect component type
     * @param effect       the effect instance
     */
    @StandardAPI
    public <E> EnchantmentBuilder<P> withSpecialEffect(
                                                       @NotNull Supplier<DataComponentType<E>> typeSupplier, @NotNull E effect) {
        effectCallbacks.add(builder -> builder.withSpecialEffect(typeSupplier.get(), effect));
        return this;
    }

    /**
     * 直接操作底层的 {@link Enchantment.Builder}，用于高级自定义。
     *
     * <p>
     * Provides direct access to the underlying {@link Enchantment.Builder} for advanced
     * customization not covered by the fluent API.
     */
    @StandardAPI
    public EnchantmentBuilder<P> configure(@NotNull Consumer<Enchantment.Builder> configurator) {
        effectCallbacks.add(configurator);
        return this;
    }

    // === Tags ===

    /**
     * 将此附魔添加到指定标签。
     *
     * <p>
     * Adds this enchantment to the specified enchantment tag (e.g. for enchanting table
     * availability).
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
     * 注册语言条目，通过 RegistrySetBuilder 生成附魔 JSON 和标签 JSON，返回 {@link EnchantmentEntry}。
     *
     * <p>
     * Registers lang entries, generates enchantment definition and tags during datagen via {@link
     * net.minecraft.core.RegistrySetBuilder}, and returns an {@link EnchantmentEntry}.
     */
    @StandardAPI
    public EnchantmentEntry register() {
        if (registered) {
            throw new IllegalStateException("Cannot register enchantment '" + name + "' twice");
        }
        registered = true;
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

        // Register enchantment via RegistrySetBuilder (datagen)
        if (core.doDatagen()) {
            // Capture fields for lambda
            final TagKey<Item> capturedSupportedItems = supportedItems;
            final TagKey<Item> capturedPrimaryItems = primaryItems;
            final int capturedWeight = weight;
            final int capturedMaxLevel = maxLevel;
            final Enchantment.Cost minCost = Enchantment.dynamicCost(minCostBase, minCostPerLevel);
            final Enchantment.Cost maxCost = Enchantment.dynamicCost(maxCostBase, maxCostPerLevel);
            final int capturedAnvilCost = anvilCost;
            final EquipmentSlotGroup[] capturedSlots = slots.toArray(EquipmentSlotGroup[]::new);
            final List<ResourceKey<Enchantment>> capturedExclusive = List.copyOf(exclusiveWith);
            final List<Consumer<Enchantment.Builder>> capturedEffects = List.copyOf(effectCallbacks);

            core.getDataGenInitializer()
                    .add(
                            Registries.ENCHANTMENT,
                            ctx -> {
                                HolderSet<Item> supportedItemSet = ctx.lookup(Registries.ITEM).getOrThrow(capturedSupportedItems);

                                Enchantment.EnchantmentDefinition definition;
                                if (capturedPrimaryItems != null) {
                                    HolderSet<Item> primaryItemSet = ctx.lookup(Registries.ITEM).getOrThrow(capturedPrimaryItems);
                                    definition = Enchantment.definition(
                                            supportedItemSet,
                                            primaryItemSet,
                                            capturedWeight,
                                            capturedMaxLevel,
                                            minCost,
                                            maxCost,
                                            capturedAnvilCost,
                                            capturedSlots);
                                } else {
                                    definition = Enchantment.definition(
                                            supportedItemSet,
                                            capturedWeight,
                                            capturedMaxLevel,
                                            minCost,
                                            maxCost,
                                            capturedAnvilCost,
                                            capturedSlots);
                                }

                                Enchantment.Builder builder = Enchantment.enchantment(definition);

                                // Apply exclusive set
                                if (!capturedExclusive.isEmpty()) {
                                    var enchLookup = ctx.lookup(Registries.ENCHANTMENT);
                                    var holders = capturedExclusive.stream().map(enchLookup::getOrThrow).toList();
                                    builder.exclusiveWith(HolderSet.direct(holders));
                                }

                                // Apply effects
                                for (var cb : capturedEffects) {
                                    cb.accept(builder);
                                }

                                ctx.register(key, builder.build(key.identifier()));
                            });

            // Generate tag entries
            if (!tags.isEmpty()) {
                core.addDataGenerator(
                        ProviderType.ENCHANTMENT_TAGS,
                        (RegistryLibEnchantmentTagsProvider prov) -> {
                            for (var tag : tags) {
                                prov.tag(tag).add(key);
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
}
