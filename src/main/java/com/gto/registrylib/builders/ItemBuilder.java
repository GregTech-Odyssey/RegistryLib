package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.composite.CompositeItem;
import com.gto.registrylib.composite.CompositeItemAttachment;
import com.gto.registrylib.providers.DataGenContext;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylib.providers.generators.RegistryLibItemModelGenerator;
import com.gto.registrylib.providers.generators.RegistryLibRecipeProvider;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.tooltip.TooltipNodeCollector;
import com.gto.registrylib.tooltip.TooltipRegistry;
import com.gto.registrylib.util.CreativeModeTabModifier;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import com.google.common.collect.Maps;

import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

public class ItemBuilder<T extends Item, P> extends AbstractBuilder<Item, T, P, ItemBuilder<T, P>> {

    public static <T extends Item, P> ItemBuilder<T, P> create(
                                                               RegistryCore owner,
                                                               P parent,
                                                               String name,
                                                               BuilderCallback callback,
                                                               Function<Item.Properties, T> factory) {
        return new ItemBuilder<>(owner, parent, name, callback, factory).defaultModel().defaultLang();
    }

    private final Function<Item.Properties, T> factory;

    private Supplier<Item.Properties> initialProperties = Item.Properties::new;
    private Function<Item.Properties, Item.Properties> propertiesCallback = UnaryOperator.identity();

    private final Map<ResourceKey<CreativeModeTab>, BiConsumer<DataGenContext<Item, T>, CreativeModeTabModifier>> creativeModeTabs = Maps.newLinkedHashMap();

    private final List<TooltipNodeCollector.TooltipConfig> tooltipConfigs = new ArrayList<>();
    private final List<CompositeItemAttachment<?>> pendingAttachments = new ArrayList<>();

    protected ItemBuilder(
                          RegistryCore owner,
                          P parent,
                          String name,
                          BuilderCallback callback,
                          Function<Item.Properties, T> factory) {
        super(owner, parent, name, callback, Registries.ITEM);
        this.factory = factory;

        onRegister(
                item -> {
                    creativeModeTabs.forEach(
                            (creativeModeTab, consumer) -> owner.modifyCreativeModeTab(
                                    creativeModeTab,
                                    modifier -> consumer.accept(DataGenContext.from(this), modifier)));
                    creativeModeTabs.clear();

                    // 注册 tooltip 配置
                    for (var config : tooltipConfigs) {
                        TooltipRegistry.register(item, config);
                    }
                    tooltipConfigs.clear();

                    // 挂载组合附件
                    if (item instanceof CompositeItem composite) {
                        for (var attachment : pendingAttachments) {
                            composite.attachUnchecked(attachment);
                        }
                        // 自动注册附件的 tooltip 收集
                        if (composite.getAttachments().stream()
                                .anyMatch(
                                        att -> (att.overrideFlags & CompositeItemAttachment.COLLECT_TOOLTIP) != 0)) {
                            TooltipRegistry.register(
                                    item,
                                    (collector, stack) -> {
                                        for (var att : composite.getAttachments()) {
                                            if ((att.overrideFlags & CompositeItemAttachment.COLLECT_TOOLTIP) == 0)
                                                continue;
                                            att.collectTooltipNodes(composite, stack, collector);
                                        }
                                    });
                        }
                    }
                    pendingAttachments.clear();
                });
    }

    @StandardAPI
    public ItemBuilder<T, P> properties(@Nonnull UnaryOperator<Item.Properties> func) {
        propertiesCallback = propertiesCallback.andThen(func);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> initialProperties(@Nonnull Supplier<Item.Properties> properties) {
        initialProperties = properties;
        return this;
    }

    // === Syntax Sugar ===

    @SyntaxSugar("model(() -> (ctx, prov) -> prov.generateFlatItem(ctx.get(), ModelTemplates.FLAT_ITEM))")
    public ItemBuilder<T, P> defaultModel() {
        return model(() -> (ctx, prov) -> prov.generateFlatItem(ctx.get(), ModelTemplates.FLAT_ITEM));
    }

    @SyntaxSugar("lang(Item::getDescriptionId)")
    public ItemBuilder<T, P> defaultLang() {
        return lang(Item::getDescriptionId);
    }

    // === Configuration ===

    @StandardAPI
    public ItemBuilder<T, P> tab(
                                 @Nonnull ResourceKey<CreativeModeTab> tab,
                                 @Nonnull BiConsumer<DataGenContext<Item, T>, CreativeModeTabModifier> modifier) {
        creativeModeTabs.put(tab, modifier);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> tab(
                                 @Nonnull ResourceKey<CreativeModeTab> tab,
                                 @Nonnull Consumer<CreativeModeTabModifier> modifier) {
        return tab(tab, ($, m) -> modifier.accept(m));
    }

    @StandardAPI
    public ItemBuilder<T, P> tab(@Nonnull ResourceKey<CreativeModeTab> tab) {
        return tab(tab, (item, modifier) -> modifier.accept(item));
    }

    @StandardAPI
    public ItemBuilder<T, P> removeTab(@Nonnull ResourceKey<CreativeModeTab> tab) {
        creativeModeTabs.remove(tab);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> model(
                                   @Nonnull Supplier<BiConsumer<DataGenContext<Item, T>, RegistryLibItemModelGenerator>> cons) {
        if (!getOwner().doDatagen().get()) return this;
        return setData(ProviderType.ITEM_MODEL, cons.get());
    }

    @SyntaxSugar("lang(Item::getDescriptionId, name)")
    public ItemBuilder<T, P> lang(@Nonnull String name) {
        return lang(Item::getDescriptionId, name);
    }

    @SyntaxSugar("lang(type, Item::getDescriptionId, name)")
    public ItemBuilder<T, P> lang(
            @Nonnull ProviderType<? extends RegistryLibLangProvider> type, @Nonnull String name) {
        return lang(type, Item::getDescriptionId, name);
    }

    @StandardAPI
    public ItemBuilder<T, P> recipe(
                                    @Nonnull BiConsumer<DataGenContext<Item, T>, RegistryLibRecipeProvider> cons) {
        return setData(ProviderType.RECIPE, cons);
    }

    /**
     * 为此物品注册 tooltip 子节点配置。
     *
     * <p>
     * 配置在 tooltip 渲染阶段执行，接收当前 ItemStack， 可根据 ItemStack 数据动态生成节点。
     */
    @StandardAPI
    public ItemBuilder<T, P> tooltip(@Nonnull TooltipNodeCollector.TooltipConfig config) {
        tooltipConfigs.add(config);
        return this;
    }

    /** 便捷添加一个 tooltip */
    @SyntaxSugar("tooltip((collector, stack) -> collector.node(new SubNode.Basic(component, 0)))")
    public ItemBuilder<T, P> tooltip(@Nonnull Component component) {
        tooltip((collector, stack) -> collector.node(new SubNode.Basic(component, 0)));
        return this;
    }

    /** 为此物品添加一个组合附件（仅当 Item 为 {@link CompositeItem} 或其子类时有效）。 */
    @StandardAPI
    public ItemBuilder<T, P> attach(@Nonnull CompositeItemAttachment<?> attachment) {
        pendingAttachments.add(attachment);
        return this;
    }

    @SafeVarargs
    @SyntaxSugar("tag(ProviderType.ITEM_TAGS, tags)")
    public final ItemBuilder<T, P> tag(@Nonnull TagKey<Item>... tags) {
        return tag(ProviderType.ITEM_TAGS, tags);
    }

    @Override
    protected T createEntry() {
        Item.Properties properties = this.initialProperties.get();
        properties = propertiesCallback.apply(properties);
        return factory.apply(properties.setId(getResourceKey()));
    }

    @Override
    protected RegistryEntry<Item, T> createEntryWrapper(DeferredHolder<Item, T> delegate) {
        return new ItemEntry<>(getOwner(), delegate);
    }

    @Override
    @StandardAPI
    public ItemEntry<T> register() {
        return (ItemEntry<T>) super.register();
    }
}
