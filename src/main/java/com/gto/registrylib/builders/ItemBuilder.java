package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.composite.IComponentItem;
import com.gto.registrylib.composite.ItemAttachment;
import com.gto.registrylib.providers.DataGenContext;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylib.providers.generators.RegistryLibItemModelGenerator;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.tooltip.TooltipNodeCollector;
import com.gto.registrylib.tooltip.TooltipRegistry;
import com.gto.registrylib.util.CreativeModeTabModifier;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import javax.annotation.Nonnull;

public class ItemBuilder<T extends Item, P> extends AbstractBuilder<Item, T, P, ItemBuilder<T, P>> {

    public static <T extends Item, P> ItemBuilder<T, P> create(
                                                               RegistryCore owner,
                                                               P parent,
                                                               String name,
                                                               BuilderCallback callback,
                                                               Function<Item.Properties, T> factory,
                                                               boolean isComponentItem) {
        return new ItemBuilder<>(owner, parent, name, callback, factory, isComponentItem)
                .defaultModel()
                .defaultLang();
    }

    private final Function<Item.Properties, T> factory;

    private Supplier<Item.Properties> initialProperties;
    private Function<Item.Properties, Item.Properties> propertiesCallback = FunctionUtil.identityFn();

    private final Map<ResourceKey<CreativeModeTab>, BiConsumer<Item, CreativeModeTabModifier>> creativeModeTabs = new Reference2ReferenceOpenHashMap<>();

    private final List<TooltipNodeCollector.TooltipConfig> tooltipConfigs = new ArrayList<>();
    private final List<ItemAttachment<?>> pendingAttachments;

    protected ItemBuilder(
                          RegistryCore owner,
                          P parent,
                          String name,
                          BuilderCallback callback,
                          Function<Item.Properties, T> factory,
                          boolean isComponentItem) {
        super(owner, parent, name, callback, Registries.ITEM);
        this.factory = factory;
        pendingAttachments = isComponentItem ? new ArrayList<>() : null;
        onRegister(
                item -> {
                    creativeModeTabs.forEach(
                            (creativeModeTab, consumer) -> owner.modifyCreativeModeTab(
                                    creativeModeTab, modifier -> consumer.accept(item, modifier)));
                    creativeModeTabs.clear();

                    // 注册 tooltip 配置
                    for (var config : tooltipConfigs) {
                        TooltipRegistry.register(item, config);
                    }
                    tooltipConfigs.clear();

                    // 挂载组合附件
                    if (isComponentItem) {
                        if (!(item instanceof IComponentItem<?> componentItem))
                            throw new RuntimeException("Item is not a component item");
                        for (var attachment : pendingAttachments) {
                            componentItem.attachAttachment(attachment.self());
                        }
                        // 自动注册附件的 tooltip 收集
                        if (componentItem.getAttachments().stream()
                                .anyMatch(att -> (att.overrideFlags & ItemAttachment.COLLECT_TOOLTIP) != 0)) {
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
                });
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
        return model(() -> (ctx, prov) -> prov.generateFlatItem(ctx.get(), ModelTemplates.FLAT_ITEM));
    }

    @SyntaxSugar("lang(Item::getDescriptionId)")
    public ItemBuilder<T, P> defaultLang() {
        return lang(Item::getDescriptionId);
    }

    // === Configuration ===

    @StandardAPI
    public ItemBuilder<T, P> tab(
                                 @NotNull ResourceKey<CreativeModeTab> tab,
                                 @NotNull BiConsumer<Item, CreativeModeTabModifier> modifier) {
        creativeModeTabs.put(tab, modifier);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> tab(
                                 @NotNull ResourceKey<CreativeModeTab> tab,
                                 @NotNull Consumer<CreativeModeTabModifier> modifier) {
        return tab(tab, ($, m) -> modifier.accept(m));
    }

    @StandardAPI
    public ItemBuilder<T, P> tab(@NotNull ResourceKey<CreativeModeTab> tab) {
        return tab(tab, (item, modifier) -> modifier.accept(item));
    }

    @StandardAPI
    public ItemBuilder<T, P> removeTab(@NotNull ResourceKey<CreativeModeTab> tab) {
        creativeModeTabs.remove(tab);
        return this;
    }

    @StandardAPI
    public ItemBuilder<T, P> model(
                                   @NotNull Supplier<BiConsumer<DataGenContext<Item, T>, RegistryLibItemModelGenerator>> cons) {
        if (!getOwner().doDatagen()) return this;
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

    /** 为此物品添加一个组合附件（仅当 Item 为 {@link IComponentItem} 或其子类时有效）。 */
    @StandardAPI
    public ItemBuilder<T, P> attach(@Nonnull ItemAttachment<?> attachment) {
        if (pendingAttachments == null) throw new IllegalStateException("Item is not a component item");
        pendingAttachments.add(attachment);
        return this;
    }

    @SafeVarargs
    @StandardAPI
    public final ItemBuilder<T, P> tag(@NotNull TagKey<Item>... tags) {
        return tag(ProviderType.ITEM_TAGS, tags);
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
        return factory.apply(properties.setId(key));
    }

    @Override
    protected RegistryEntry<Item, T> createEntryWrapper(ResourceKey<Item> key) {
        return new ItemEntry<>(key);
    }

    @Override
    @StandardAPI
    public ItemEntry<T> register() {
        return (ItemEntry<T>) super.register();
    }
}
