package com.gto.registrylib.builders;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.composite.IComponentItem;
import com.gto.registrylib.composite.ItemAttachment;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.generator.RegistryLibItemModelGenerator;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.tooltip.TooltipNodeCollector;
import com.gto.registrylib.tooltip.TooltipRegistry;
import com.gto.registrylib.util.CreativeModeTabModifier;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class ItemBuilder<T extends Item, P> extends AbstractBuilder<Item, T, P, ItemBuilder<T, P>> {

    public static <T extends Item, P> ItemBuilder<T, P> create(
                                                               RegistryCore owner,
                                                               P parent,
                                                               String name,
                                                               Function<Item.Properties, T> factory,
                                                               boolean isComponentItem) {
        return new ItemBuilder<>(owner, parent, name, factory, isComponentItem)
                .defaultModel()
                .defaultLang();
    }

    private final Function<Item.Properties, T> factory;

    private Supplier<Item.Properties> initialProperties;
    private Function<Item.Properties, Item.Properties> propertiesCallback = FunctionUtil.identityFn();

    private @Nullable Reference2ReferenceOpenHashMap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> creativeModeTabs;

    private @Nullable ArrayList<TooltipNodeCollector.TooltipConfig> tooltipConfigs;
    private final ArrayList<ItemAttachment<?>> pendingAttachments;

    protected ItemBuilder(
                          RegistryCore core,
                          P parent,
                          String name,
                          Function<Item.Properties, T> factory,
                          boolean isComponentItem) {
        super(core, parent, name, Registries.ITEM);
        this.factory = factory;
        pendingAttachments = isComponentItem ? new ArrayList<>() : null;
    }

    @StandardAPI
    public ItemBuilder<T, P> properties(@NotNull UnaryOperator<Item.Properties> func) {
        propertiesCallback = propertiesCallback.andThen(func);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> initialProperties(@NotNull Supplier<Item.Properties> properties) {
        initialProperties = properties;
        return this;
    }

    // === Syntax Sugar ===

    @SyntaxSugar("model(() -> (ctx, prov) -> prov.generateFlatItem(ctx.get(), ModelTemplates.FLAT_ITEM))")
    public ItemBuilder<T, P> defaultModel() {
        return model(() -> (ctx, prov) -> prov.generateFlatItem(ctx, ModelTemplates.FLAT_ITEM));
    }

    @SyntaxSugar("lang(Item::getDescriptionId)")
    public ItemBuilder<T, P> defaultLang() {
        return lang(Item::getDescriptionId);
    }

    // === Configuration ===

    private Reference2ReferenceOpenHashMap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> getLazyCreativeModeTabs() {
        var m = creativeModeTabs;
        if (m == null) creativeModeTabs = m = new Reference2ReferenceOpenHashMap<>(2);
        return m;
    }

    @StandardAPI
    public ItemBuilder<T, P> addTab(
                                    @NotNull ResourceKey<CreativeModeTab> tab,
                                    @NotNull Consumer<CreativeModeTabModifier> modifier) {
        getLazyCreativeModeTabs().put(tab, modifier);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> addTab(@NotNull ResourceKey<CreativeModeTab> tab) {
        getLazyCreativeModeTabs().put(tab, CreativeModeTabModifier.DEFAULT);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> addDefaultTab() {
        var tab = core.getDefaultCreativeModeTab();
        if (tab != null) getLazyCreativeModeTabs().put(tab, CreativeModeTabModifier.DEFAULT);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> removeTab(@NotNull ResourceKey<CreativeModeTab> tab) {
        if (creativeModeTabs != null) creativeModeTabs.remove(tab);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> texture(String path, Supplier<BufferedImage> image) {
        if (!core.doDatagen()) return this;
        return addData(
                ProviderType.GENERAL_RESOURCE, p -> p.addItemTexture(p.simpleTexture(path, image)));
    }

    @SyntaxSugar("texture(name, image)")
    public ItemBuilder<T, P> texture(Supplier<BufferedImage> image) {
        return texture(name, image);
    }

    @StandardAPI
    public ItemBuilder<T, P> model(
                                   @NotNull Supplier<BiConsumer<T, RegistryLibItemModelGenerator>> cons) {
        if (!core.doDatagen()) return this;
        return setData(ProviderType.ITEM_MODEL, p -> cons.get().accept(getValue(), p));
    }

    @SyntaxSugar("lang(Item::getDescriptionId, name)")
    public ItemBuilder<T, P> lang(@NotNull String name) {
        return lang(Item::getDescriptionId, name);
    }

    @SyntaxSugar("lang(type, Item::getDescriptionId, name)")
    public ItemBuilder<T, P> lang(
                                  @NotNull ProviderType<? extends RegistryLibLangProvider> type, @NotNull String name) {
        return lang(type, Item::getDescriptionId, name);
    }

    /**
     * 为此物品注册 tooltip 子节点配置。
     *
     * <p>
     * 配置在 tooltip 渲染阶段执行，接收当前 ItemStack， 可根据 ItemStack 数据动态生成节点。
     */
    @StandardAPI
    public ItemBuilder<T, P> addTooltip(@NotNull TooltipNodeCollector.TooltipConfig config) {
        if (tooltipConfigs == null) tooltipConfigs = new ArrayList<>();
        tooltipConfigs.add(config);
        return this;
    }

    /** 便捷添加一个 tooltip */
    @SyntaxSugar("addTooltip((collector, stack) -> collector.node(new SubNode.Basic(component, 0)))")
    public ItemBuilder<T, P> addTooltip(@NotNull Component component) {
        addTooltip((collector, stack) -> collector.node(new SubNode.Basic(component, 0)));
        return this;
    }

    /** 为此物品添加一个组合附件（仅当 Item 为 {@link IComponentItem} 或其子类时有效）。 */
    @StandardAPI
    public ItemBuilder<T, P> attach(@NotNull ItemAttachment<?> attachment) {
        if (pendingAttachments == null) throw new IllegalStateException("Item is not a component item");
        pendingAttachments.add(attachment);
        return this;
    }

    @SafeVarargs
    @StandardAPI
    public final ItemBuilder<T, P> addTag(@NotNull TagKey<Item>... tags) {
        return addTag(ProviderType.ITEM_TAGS, false, tags);
    }

    @Override
    protected T createEntry(ResourceKey<Item> key) {
        Item.Properties properties;
        var initialProperties = this.initialProperties;
        if (initialProperties == null) {
            properties = new Item.Properties();
        } else {
            properties = initialProperties.get();
        }
        properties = propertiesCallback.apply(properties);
        var item = factory.apply(properties.setId(key));
        // 注册 tooltip 配置
        if (tooltipConfigs != null) {
            for (var config : tooltipConfigs) {
                TooltipRegistry.register(item, config);
            }
            tooltipConfigs = null;
        }

        // 挂载组合附件
        if (pendingAttachments != null) {
            if (!(item instanceof IComponentItem<?> componentItem))
                throw new IllegalStateException(
                        "attach() requires IComponentItem, got: " + item.getClass().getName());
            for (var attachment : pendingAttachments) {
                componentItem.attachAttachment(attachment.self());
            }
            // 自动注册附件的 tooltip 收集
            boolean hasTooltipAttachment = false;
            for (var att : componentItem.getAttachments()) {
                if ((att.overrideFlags & ItemAttachment.COLLECT_TOOLTIP) != 0) {
                    hasTooltipAttachment = true;
                    break;
                }
            }
            if (hasTooltipAttachment) {
                TooltipRegistry.register(
                        item,
                        (collector, stack) -> {
                            for (var att : componentItem.getAttachments()) {
                                if ((att.overrideFlags & ItemAttachment.COLLECT_TOOLTIP) == 0) continue;
                                att.collectTooltipNodes(componentItem.self(), stack, collector);
                            }
                        });
            }
            pendingAttachments.clear();
        }
        return item;
    }

    @Override
    protected RegistryEntry<Item, T> createEntryWrapper(ResourceKey<Item> key) {
        return new ItemEntry<>(key);
    }

    @Override
    @StandardAPI
    public ItemEntry<T> register() {
        var entry = (ItemEntry<T>) super.register();
        var tabs = creativeModeTabs;
        if (tabs == null || tabs.isEmpty()) {
            var tab = core.getDefaultCreativeModeTab();
            if (tab != null) core.modifyCreativeModeTab(tab, m -> m.acceptEntry(entry));
        } else {
            var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
            for (var it = tabs.reference2ReferenceEntrySet().fastIterator(); it.hasNext();) {
                var e = it.next();
                var key = e.getKey();
                var value = e.getValue();
                if (value == CreativeModeTabModifier.DEFAULT) {
                    var finalVisibility = visibility;
                    core.modifyCreativeModeTab(key, m -> m.acceptEntry(entry, finalVisibility));
                    visibility = CreativeModeTab.TabVisibility.PARENT_TAB_ONLY;
                } else {
                    core.modifyCreativeModeTab(key, value);
                }
            }
            creativeModeTabs = null;
        }
        return entry;
    }
}
