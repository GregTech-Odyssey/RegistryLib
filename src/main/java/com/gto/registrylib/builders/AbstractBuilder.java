package com.gto.registrylib.builders;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.gto.registrylib.RegistryLib;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylib.providers.RegistryLibTagsProvider;
import com.gto.registrylib.util.entry.LazyRegistryEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public abstract class AbstractBuilder<R, T extends R, P, S extends AbstractBuilder<R, T, P, S>>
    implements Builder<R, T, P, S> {

  private final RegistryLib owner;
  private final P parent;
  private final String name;
  private final BuilderCallback callback;
  private final ResourceKey<? extends Registry<R>> registryKey;

  private final Multimap<ProviderType<? extends RegistryLibTagsProvider<?>>, TagKey<?>> tagsByType =
      HashMultimap.create();
  private final LazyRegistryEntry<R, T> safeSupplier = new LazyRegistryEntry<>(this);
  private boolean isOptional = false;

  protected AbstractBuilder(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      ResourceKey<? extends Registry<R>> registryKey) {
    this.owner = owner;
    this.parent = parent;
    this.name = name;
    this.callback = callback;
    this.registryKey = registryKey;
  }

  @Override
  public RegistryLib getOwner() {
    return owner;
  }

  @Override
  public P getParent() {
    return parent;
  }

  @Override
  public String getName() {
    return name;
  }

  protected BuilderCallback getCallback() {
    return callback;
  }

  @Override
  public ResourceKey<? extends Registry<R>> getRegistryKey() {
    return registryKey;
  }

  protected abstract T createEntry();

  @Override
  @StandardAPI
  public RegistryEntry<R, T> register() {
    return callback.accept(name, registryKey, this, this::createEntry, this::createEntryWrapper);
  }

  protected RegistryEntry<R, T> createEntryWrapper(DeferredHolder<R, T> delegate) {
    return new RegistryEntry<>(getOwner(), delegate);
  }

  @Override
  public Supplier<T> asSupplier() {
    return safeSupplier;
  }

  // === Configuration ===

  @SuppressWarnings("unchecked")
  @SafeVarargs
  @StandardAPI
  public final <TP extends TagsProvider<R> & RegistryLibTagsProvider<R>> S tag(
      @Nonnull ProviderType<? extends TP> type, @Nonnull TagKey<R>... tags) {
    if (!tagsByType.containsKey(type)) {
      setData(
          type,
          (ctx, prov) ->
              tagsByType.get(type).stream()
                  .map(t -> (TagKey<R>) t)
                  .map(prov::rawBuilder)
                  .forEach(b -> b.add(asTag())));
    }
    tagsByType.putAll(type, Arrays.asList(tags));
    return (S) this;
  }

  @SuppressWarnings("unchecked")
  @StandardAPI
  public S asOptional() {
    isOptional = true;
    return (S) this;
  }

  protected TagEntry asTag() {
    Identifier id = Identifier.fromNamespaceAndPath(getOwner().getModid(), getName());
    if (isOptional) return TagEntry.optionalElement(id);
    return TagEntry.element(id);
  }

  @SuppressWarnings("unchecked")
  @SafeVarargs
  @StandardAPI
  public final <TP extends TagsProvider<R> & RegistryLibTagsProvider<R>> S removeTag(
      @Nonnull ProviderType<TP> type, @Nonnull TagKey<R>... tags) {
    if (tagsByType.containsKey(type)) {
      for (TagKey<R> tag : tags) {
        tagsByType.remove(type, tag);
      }
    }
    return (S) this;
  }

  @StandardAPI
  public S lang(@Nonnull Function<T, String> langKeyProvider) {
    return lang(langKeyProvider, (p, t) -> p.<R>getAutomaticName(t, getRegistryKey()));
  }

  @StandardAPI
  public S lang(@Nonnull Function<T, String> langKeyProvider, @Nonnull String name) {
    return lang(langKeyProvider, (p, s) -> name);
  }

  private S lang(
      @Nonnull Function<T, String> langKeyProvider,
      @Nonnull
          BiFunction<RegistryLibLangProvider, Supplier<? extends T>, String>
              localizedNameProvider) {
    return setData(
        ProviderType.LANG,
        (ctx, prov) ->
            prov.add(
                langKeyProvider.apply(ctx.getEntry()),
                localizedNameProvider.apply(prov, ctx::getEntry)));
  }

  public ResourceKey<R> getResourceKey() {
    return ResourceKey.create(
        getRegistryKey(), Identifier.fromNamespaceAndPath(getOwner().getModid(), getName()));
  }
}
