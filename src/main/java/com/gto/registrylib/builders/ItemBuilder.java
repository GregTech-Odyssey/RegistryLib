package com.gto.registrylib.builders;

import com.google.common.collect.Maps;
import com.gto.registrylib.RegistryLib;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.providers.DataGenContext;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.generators.RegistryLibItemModelGenerator;
import com.gto.registrylib.providers.generators.RegistryLibRecipeProvider;
import com.gto.registrylib.util.CreativeModeTabModifier;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemBuilder<T extends Item, P> extends AbstractBuilder<Item, T, P, ItemBuilder<T, P>> {

  public static <T extends Item, P> ItemBuilder<T, P> create(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      Function<Item.Properties, T> factory) {
    return new ItemBuilder<>(owner, parent, name, callback, factory).defaultModel().defaultLang();
  }

  private final Function<Item.Properties, T> factory;

  private Supplier<Item.Properties> initialProperties = Item.Properties::new;
  private Function<Item.Properties, Item.Properties> propertiesCallback = UnaryOperator.identity();

  private final Map<
          ResourceKey<CreativeModeTab>,
          BiConsumer<DataGenContext<Item, T>, CreativeModeTabModifier>>
      creativeModeTabs = Maps.newLinkedHashMap();

  protected ItemBuilder(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      Function<Item.Properties, T> factory) {
    super(owner, parent, name, callback, Registries.ITEM);
    this.factory = factory;

    onRegister(
        item -> {
          creativeModeTabs.forEach(
              (creativeModeTab, consumer) ->
                  owner.modifyCreativeModeTab(
                      creativeModeTab,
                      modifier -> consumer.accept(DataGenContext.from(this), modifier)));
          creativeModeTabs.clear();
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

  @SyntaxSugar(
      "model(() -> (ctx, prov) -> prov.generateFlatItem(ctx.get(), ModelTemplates.FLAT_ITEM))")
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

  @StandardAPI
  public ItemBuilder<T, P> lang(@Nonnull String name) {
    return lang(Item::getDescriptionId, name);
  }

  @StandardAPI
  public ItemBuilder<T, P> recipe(
      @Nonnull BiConsumer<DataGenContext<Item, T>, RegistryLibRecipeProvider> cons) {
    return setData(ProviderType.RECIPE, cons);
  }

  @SafeVarargs
  @StandardAPI
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
