package com.gto.registrylib.builders;

import com.google.common.base.Preconditions;
import com.gto.registrylib.RegistryLib;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylib.util.DistExecutor;
import com.gto.registrylib.util.Lazy;
import com.gto.registrylib.util.OneTimeEventReceiver;
import com.gto.registrylib.util.entry.FluidEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class FluidBuilder<T extends BaseFlowingFluid, P>
    extends AbstractBuilder<Fluid, T, P, FluidBuilder<T, P>> {

  @FunctionalInterface
  public interface FluidTypeFactory {
    FluidType create(FluidType.Properties properties);
  }

  @FunctionalInterface
  public interface FluidFactory<T> {
    T create(BaseFlowingFluid.Properties properties);
  }

  @Nullable private Supplier<Supplier<IClientFluidTypeExtensions>> clientExtension;

  @StandardAPI
  public FluidBuilder<T, P> clientExtension(
      @Nonnull Supplier<Supplier<IClientFluidTypeExtensions>> clientExtension) {
    if (this.clientExtension == null) {
      DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerClientExtension);
    }
    this.clientExtension = clientExtension;
    return this;
  }

  @StandardAPI
  public FluidBuilder<T, P> clientExtension(
      @Nonnull Identifier stillTexture, @Nonnull Identifier flowingTexture) {
    return clientExtension(() -> () -> new DefaultFluidTypeExtension(stillTexture, flowingTexture));
  }

  protected void registerClientExtension() {
    OneTimeEventReceiver.addModListener(
        getOwner(),
        RegisterClientExtensionsEvent.class,
        e -> {
          Supplier<Supplier<IClientFluidTypeExtensions>> ext = this.clientExtension;
          if (ext != null) {
            e.registerFluidType(ext.get().get(), fluidType.get());
          }
        });
  }

  // --- Static factory methods ---

  public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      FluidTypeFactory typeFactory,
      FluidFactory<T> fluidFactory) {
    return new FluidBuilder<>(owner, parent, name, callback, typeFactory, fluidFactory)
        .defaultLang()
        .defaultSource()
        .defaultBlock()
        .defaultBucket();
  }

  public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      Supplier<FluidType> fluidType,
      FluidFactory<T> fluidFactory) {
    return new FluidBuilder<>(owner, parent, name, callback, fluidType, fluidFactory)
        .defaultLang()
        .defaultSource()
        .defaultBlock()
        .defaultBucket();
  }

  // --- Fields ---

  private final String sourceName, bucketName;
  private final FluidFactory<T> fluidFactory;
  private final Supplier<FluidType> fluidType;

  @Nullable private Boolean defaultSource, defaultBlock, defaultBucket;

  private Consumer<FluidType.Properties> typeProperties = $ -> {};
  private Consumer<BaseFlowingFluid.Properties> fluidProperties = $ -> {};

  private boolean registerType;

  @Nullable private Supplier<? extends BaseFlowingFluid> source;
  private final List<TagKey<Fluid>> tags = new ArrayList<>();

  // --- Constructors ---

  public FluidBuilder(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      FluidTypeFactory typeFactory,
      FluidFactory<T> fluidFactory) {
    super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
    this.sourceName = name;
    this.bucketName = name + "_bucket";
    this.fluidFactory = fluidFactory;
    this.fluidType = Lazy.of(() -> typeFactory.create(makeTypeProperties()));
    this.registerType = true;
  }

  public FluidBuilder(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      Supplier<FluidType> fluidType,
      FluidFactory<T> fluidFactory) {
    super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
    this.sourceName = name;
    this.bucketName = name + "_bucket";
    this.fluidFactory = fluidFactory;
    this.fluidType = fluidType;
    this.registerType = false;
  }

  // === Configuration ===

  @StandardAPI
  public FluidBuilder<T, P> properties(@Nonnull Consumer<FluidType.Properties> cons) {
    typeProperties = typeProperties.andThen(cons);
    return this;
  }

  @StandardAPI
  public FluidBuilder<T, P> fluidProperties(@Nonnull Consumer<BaseFlowingFluid.Properties> cons) {
    fluidProperties = fluidProperties.andThen(cons);
    return this;
  }

  @SyntaxSugar(
      "lang(f -> f.getFluidType().getDescriptionId(), RegistryLibLangProvider.toEnglishName(sourceName))")
  public FluidBuilder<T, P> defaultLang() {
    return lang(
        f -> f.getFluidType().getDescriptionId(),
        RegistryLibLangProvider.toEnglishName(sourceName));
  }

  @StandardAPI
  public FluidBuilder<T, P> lang(@Nonnull String name) {
    return lang(f -> f.getFluidType().getDescriptionId(), name);
  }

  // --- Source ---

  @SyntaxSugar
  public FluidBuilder<T, P> defaultSource() {
    if (this.defaultSource != null) {
      throw new IllegalStateException(
          "Cannot set a default source after a custom source has been created");
    }
    this.defaultSource = true;
    return this;
  }

  @StandardAPI
  public FluidBuilder<T, P> source(
      @Nonnull Function<BaseFlowingFluid.Properties, ? extends BaseFlowingFluid> factory) {
    this.defaultSource = false;
    this.source = Lazy.of(() -> factory.apply(makeProperties()));
    return this;
  }

  // --- Block ---

  @SyntaxSugar
  public FluidBuilder<T, P> defaultBlock() {
    if (this.defaultBlock != null) {
      throw new IllegalStateException(
          "Cannot set a default block after a custom block has been created");
    }
    this.defaultBlock = true;
    return this;
  }

  @StandardAPI("Configures a LiquidBlock sub-entry via consumer. Returns this FluidBuilder.")
  public FluidBuilder<T, P> block(
      @Nonnull Consumer<BlockBuilder<LiquidBlock, FluidBuilder<T, P>>> config) {
    return block(LiquidBlock::new, config);
  }

  @StandardAPI("Configures a custom fluid block sub-entry via consumer. Returns this FluidBuilder.")
  public <B extends LiquidBlock> FluidBuilder<T, P> block(
      @Nonnull BiFunction<T, BlockBehaviour.Properties, ? extends B> factory,
      @Nonnull Consumer<BlockBuilder<B, FluidBuilder<T, P>>> config) {
    if (this.defaultBlock == Boolean.FALSE) {
      throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
    }
    this.defaultBlock = false;
    final Supplier<T> supplier = asSupplier();
    final Supplier<Integer> lightLevel = Lazy.of(() -> fluidType.get().getLightLevel());
    final ToIntFunction<BlockState> lightLevelInt = $ -> lightLevel.get();
    final var builder =
        getOwner()
            .<B, FluidBuilder<T, P>>block(this, sourceName, p -> factory.apply(supplier.get(), p))
            .properties(p -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
            .properties(p -> p.lightLevel(lightLevelInt))
            .blockstate(() -> (ctx, prov) -> prov.createNonTemplateModelBlock(ctx.get()));
    this.fluidProperties(p -> p.block(builder.asSupplier()));
    config.accept(builder);
    builder.register();
    return this;
  }

  @StandardAPI
  public FluidBuilder<T, P> noBlock() {
    if (this.defaultBlock == Boolean.FALSE) {
      throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
    }
    this.defaultBlock = false;
    return this;
  }

  // --- Bucket ---

  @SyntaxSugar
  public FluidBuilder<T, P> defaultBucket() {
    if (this.defaultBucket != null) {
      throw new IllegalStateException(
          "Cannot set a default bucket after a custom bucket has been created");
    }
    defaultBucket = true;
    return this;
  }

  @StandardAPI("Configures a BucketItem sub-entry via consumer. Returns this FluidBuilder.")
  public FluidBuilder<T, P> bucket(
      @Nonnull Consumer<ItemBuilder<BucketItem, FluidBuilder<T, P>>> config) {
    return bucket(BucketItem::new, config);
  }

  @StandardAPI("Configures a custom bucket sub-entry via consumer. Returns this FluidBuilder.")
  public <I extends BucketItem> FluidBuilder<T, P> bucket(
      @Nonnull BiFunction<BaseFlowingFluid, Item.Properties, ? extends I> factory,
      @Nonnull Consumer<ItemBuilder<I, FluidBuilder<T, P>>> config) {
    if (this.defaultBucket == Boolean.FALSE) {
      throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
    }
    this.defaultBucket = false;
    // Resolve default source if not yet created
    if (this.source == null && this.defaultSource == Boolean.TRUE) {
      source(BaseFlowingFluid.Source::new);
    }
    Supplier<? extends BaseFlowingFluid> source = this.source;
    if (source == null) {
      throw new IllegalStateException("Cannot create a bucket before creating a source block");
    }
    final var builder =
        getOwner()
            .<I, FluidBuilder<T, P>>item(this, bucketName, p -> factory.apply(source.get(), p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .model(() -> (ctx, prov) -> prov.generateFlatItem(ctx.get(), ModelTemplates.FLAT_ITEM));
    this.fluidProperties(p -> p.bucket(builder.asSupplier()));
    config.accept(builder);
    builder.register();
    return this;
  }

  @StandardAPI
  public FluidBuilder<T, P> noBucket() {
    if (this.defaultBucket == Boolean.FALSE) {
      throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
    }
    this.defaultBucket = false;
    return this;
  }

  // --- Tags ---

  @SafeVarargs
  @StandardAPI
  public final FluidBuilder<T, P> tag(TagKey<Fluid>... tags) {
    FluidBuilder<T, P> ret = this.tag(ProviderType.FLUID_TAGS, tags);
    if (this.tags.isEmpty()) {
      ret.getOwner()
          .setDataGenerator(
              ret.sourceName,
              getRegistryKey(),
              ProviderType.FLUID_TAGS,
              prov -> this.tags.stream().map(prov::tag).forEach(p -> p.add(getSource())));
    }
    this.tags.addAll(Arrays.asList(tags));
    return ret;
  }

  @SafeVarargs
  @StandardAPI
  public final FluidBuilder<T, P> removeTag(TagKey<Fluid>... tags) {
    this.tags.removeAll(Arrays.asList(tags));
    return this.removeTag(ProviderType.FLUID_TAGS, tags);
  }

  // --- Internal helpers ---

  private BaseFlowingFluid getSource() {
    Supplier<? extends BaseFlowingFluid> source = this.source;
    Preconditions.checkNotNull(source, "Fluid has no source block: " + sourceName);
    return source.get();
  }

  private BaseFlowingFluid.Properties makeProperties() {
    Supplier<? extends BaseFlowingFluid> source = this.source;
    BaseFlowingFluid.Properties ret =
        new BaseFlowingFluid.Properties(
            fluidType, source == null ? null : source::get, asSupplier());
    fluidProperties.accept(ret);
    return ret;
  }

  private FluidType.Properties makeTypeProperties() {
    FluidType.Properties properties = FluidType.Properties.create();
    Optional<RegistryEntry<Block, Block>> block =
        getOwner().getOptional(sourceName, Registries.BLOCK);
    this.typeProperties.accept(properties);

    if (block.isPresent() && block.get().isBound()) {
      properties.descriptionId(block.get().get().getDescriptionId());
      setData(ProviderType.LANG, (ctx, prov) -> {});
    } else {
      properties.descriptionId(
          Identifier.fromNamespaceAndPath(getOwner().getModid(), sourceName)
              .toLanguageKey("fluid"));
    }

    return properties;
  }

  @Override
  protected T createEntry() {
    return fluidFactory.create(makeProperties());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  @StandardAPI
  public FluidEntry<T> register() {
    if (this.registerType) {
      getOwner().simple(this, this.sourceName, NeoForgeRegistries.Keys.FLUID_TYPES, this.fluidType);
    }

    if (defaultSource == Boolean.TRUE) {
      source(BaseFlowingFluid.Source::new);
    }
    if (defaultBlock == Boolean.TRUE) {
      block($ -> {});
    }
    if (defaultBucket == Boolean.TRUE) {
      bucket($ -> {});
    }

    Supplier<? extends BaseFlowingFluid> source = this.source;
    if (source != null) {
      getCallback().accept(sourceName, Registries.FLUID, (FluidBuilder) this, source::get);
    } else {
      throw new IllegalStateException("Fluid must have a source version: " + getName());
    }

    return (FluidEntry<T>) super.register();
  }

  @Override
  protected RegistryEntry<Fluid, T> createEntryWrapper(DeferredHolder<Fluid, T> delegate) {
    return new FluidEntry<>(getOwner(), delegate);
  }

  // --- DefaultFluidTypeExtension ---

  public static class DefaultFluidTypeExtension implements IClientFluidTypeExtensions {
    private final Identifier stillTexture, flowingTexture;

    public DefaultFluidTypeExtension(Identifier stillTexture, Identifier flowingTexture) {
      this.stillTexture = stillTexture;
      this.flowingTexture = flowingTexture;
    }

    @Override
    public Identifier getStillTexture() {
      return stillTexture;
    }

    @Override
    public Identifier getFlowingTexture() {
      return flowingTexture;
    }
  }
}
