package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.client.Client;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.util.DistExecutor;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.Lazy;
import com.gto.registrylib.util.entry.FluidEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import com.google.common.base.Preconditions;

import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    private static final Identifier BUCKET_FLUID_TEXTURE = Identifier.fromNamespaceAndPath("registrylib", "item/bucket_fluid");
    private static final Identifier BUCKET_BASE_TEXTURE = Identifier.fromNamespaceAndPath("registrylib", "item/bucket_base");

    @StandardAPI
    public FluidBuilder<T, P> clientExtension(
                                              @Nonnull Supplier<Supplier<IClientFluidTypeExtensions>> clientExtension) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> Client.registerFluidTypeExtensions(fluidType, clientExtension.get().get()));
        return this;
    }

    @SyntaxSugar("clientExtension(() -> () -> new DefaultFluidTypeExtension(stillTexture, flowingTexture, -1))")
    public FluidBuilder<T, P> clientExtension(
                                              @Nonnull Identifier stillTexture, @Nonnull Identifier flowingTexture) {
        return clientExtension(
                () -> () -> new DefaultFluidTypeExtension(stillTexture, flowingTexture, -1));
    }

    @SyntaxSugar("clientExtension(() -> () -> new DefaultFluidTypeExtension(stillTexture, flowingTexture, tintColor))")
    public FluidBuilder<T, P> clientExtension(
                                              @Nonnull Identifier stillTexture, @Nonnull Identifier flowingTexture, int tintColor) {
        this.tintColor = tintColor;
        return clientExtension(
                () -> () -> new DefaultFluidTypeExtension(stillTexture, flowingTexture, tintColor));
    }

    // --- Static factory methods ---

    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(
                                                                            RegistryCore owner,
                                                                            P parent,
                                                                            String name,
                                                                            FluidTypeFactory typeFactory,
                                                                            FluidFactory<T> fluidFactory) {
        return new FluidBuilder<>(owner, parent, name, typeFactory, fluidFactory)
                .defaultLang()
                .defaultSource()
                .defaultBlock()
                .defaultBucket();
    }

    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(
                                                                            RegistryCore owner,
                                                                            P parent,
                                                                            String name,
                                                                            Supplier<FluidType> fluidType,
                                                                            FluidFactory<T> fluidFactory) {
        return new FluidBuilder<>(owner, parent, name, fluidType, fluidFactory)
                .defaultLang()
                .defaultSource()
                .defaultBlock()
                .defaultBucket();
    }

    // --- Fields ---

    private int tintColor = -1;

    private final String sourceName, bucketName;
    private final FluidFactory<T> fluidFactory;
    private final Supplier<FluidType> fluidType;

    @Nullable
    private Boolean defaultSource, defaultBlock, defaultBucket;
    @Nullable
    private ResourceKey<CreativeModeTab> defaultBucketTab;

    private Consumer<FluidType.Properties> typeProperties = FunctionUtil.noOpConsumer();
    private Consumer<BaseFlowingFluid.Properties> fluidProperties = FunctionUtil.noOpConsumer();

    private final boolean registerType;

    @Nullable
    private Supplier<? extends BaseFlowingFluid> source;

    // --- Constructors ---

    public FluidBuilder(
                        RegistryCore core,
                        P parent,
                        String name,
                        FluidTypeFactory typeFactory,
                        FluidFactory<T> fluidFactory) {
        super(core, parent, "flowing_" + name, Registries.FLUID);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.fluidFactory = fluidFactory;
        this.fluidType = Lazy.of(() -> typeFactory.create(makeTypeProperties()));
        this.registerType = true;
    }

    public FluidBuilder(
                        RegistryCore core,
                        P parent,
                        String name,
                        Supplier<FluidType> fluidType,
                        FluidFactory<T> fluidFactory) {
        super(core, parent, "flowing_" + name, Registries.FLUID);
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

    @SyntaxSugar("lang(f -> f.getFluidType().getDescriptionId(), RegistryLibLangProvider.toEnglishName(sourceName))")
    public FluidBuilder<T, P> defaultLang() {
        return lang(
                ProviderType.LANG,
                f -> f.getFluidType().getDescriptionId(),
                RegistryLibLangProvider.toEnglishName(sourceName));
    }

    @SyntaxSugar("lang(f -> f.getFluidType().getDescriptionId(), name)")
    public FluidBuilder<T, P> lang(@Nonnull String name) {
        return lang(ProviderType.LANG, f -> f.getFluidType().getDescriptionId(), name);
    }

    @SyntaxSugar("lang(type, f -> f.getFluidType().getDescriptionId(), name)")
    public FluidBuilder<T, P> lang(
                                   @Nonnull ProviderType<? extends RegistryLibLangProvider> type, @Nonnull String name) {
        return lang(type, f -> f.getFluidType().getDescriptionId(), name);
    }

    // --- Source ---

    @SyntaxSugar("source(BaseFlowingFluid.Source::new)")
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

    @SyntaxSugar("block($ -> {})")
    public FluidBuilder<T, P> defaultBlock() {
        if (this.defaultBlock != null) {
            throw new IllegalStateException(
                    "Cannot set a default block after a custom block has been created");
        }
        this.defaultBlock = true;
        return this;
    }

    @StandardAPI("Configures a BlockBuilder for the fluid block sub-entry via lambda.")
    public FluidBuilder<T, P> block(
                                    @Nonnull Consumer<BlockBuilder<LiquidBlock, FluidBuilder<T, P>>> consumer) {
        return block(LiquidBlock::new, consumer);
    }

    @StandardAPI("Configures a BlockBuilder with a custom block factory via lambda.")
    public <B extends LiquidBlock> FluidBuilder<T, P> block(
                                                            @Nonnull BiFunction<T, BlockBehaviour.Properties, ? extends B> factory,
                                                            @Nonnull Consumer<BlockBuilder<B, FluidBuilder<T, P>>> consumer) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        final Supplier<T> supplier = valueSupplier;
        final Supplier<Integer> lightLevel = Lazy.of(() -> fluidType.get().getLightLevel());
        final ToIntFunction<BlockState> lightLevelInt = $ -> lightLevel.get();
        final var block = core.<B, FluidBuilder<T, P>>block(this, sourceName, p -> factory.apply(supplier.get(), p))
                .properties(p -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
                .properties(p -> p.lightLevel(lightLevelInt))
                .blockstate(() -> (value, prov) -> prov.createNonTemplateModelBlock(value));
        var blockSupplier = block.valueSupplier;
        this.fluidProperties(p -> p.block(blockSupplier));
        consumer.accept(block);
        return block.build();
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

    @SyntaxSugar("bucket($ -> {})")
    public FluidBuilder<T, P> defaultBucket() {
        if (this.defaultBucket != null) {
            throw new IllegalStateException(
                    "Cannot set a default bucket after a custom bucket has been created");
        }
        defaultBucket = true;
        return this;
    }

    /**
     * Sets a default creative tab that will be applied to any bucket item created via {@link
     * #bucket}.
     */
    @StandardAPI
    public FluidBuilder<T, P> defaultBucketTab(@Nonnull ResourceKey<CreativeModeTab> tab) {
        this.defaultBucketTab = tab;
        return this;
    }

    @StandardAPI("Configures an ItemBuilder for the bucket sub-entry via lambda.")
    public FluidBuilder<T, P> bucket(
                                     @Nonnull Consumer<ItemBuilder<BucketItem, FluidBuilder<T, P>>> consumer) {
        return bucket(BucketItem::new, consumer);
    }

    @StandardAPI("Configures an ItemBuilder with a custom bucket factory via lambda.")
    public <I extends BucketItem> FluidBuilder<T, P> bucket(
                                                            @Nonnull BiFunction<BaseFlowingFluid, Item.Properties, ? extends I> factory,
                                                            @Nonnull Consumer<ItemBuilder<I, FluidBuilder<T, P>>> consumer) {
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
        final int bucketTintColor = this.tintColor;
        final var item = core.<I, FluidBuilder<T, P>>item(
                this, bucketName, p -> factory.apply(source.get(), p), false)
                .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
                .model(
                        () -> (ctx, prov) -> {
                            TextureMapping textures = new TextureMapping();
                            textures.put(TextureSlot.LAYER0, new Material(BUCKET_FLUID_TEXTURE));
                            textures.put(TextureSlot.LAYER1, new Material(BUCKET_BASE_TEXTURE));
                            Identifier modelId = ModelTemplates.TWO_LAYERED_ITEM.create(ctx, textures, prov.modelOutput);
                            if (bucketTintColor != -1) {
                                prov.itemModelOutput.accept(
                                        ctx,
                                        ItemModelUtils.tintedModel(
                                                modelId, ItemModelUtils.constantTint(bucketTintColor)));
                            } else {
                                prov.itemModelOutput.accept(ctx, ItemModelUtils.plainModel(modelId));
                            }
                        });
        var itemSupplier = item.valueSupplier;
        this.fluidProperties(p -> p.bucket(itemSupplier));
        if (defaultBucketTab != null) {
            item.addTab(defaultBucketTab);
        }
        consumer.accept(item);
        return item.build();
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
        return this.addTag(ProviderType.FLUID_TAGS, tags);
    }

    // --- Internal helpers ---

    private BaseFlowingFluid getSource() {
        Supplier<? extends BaseFlowingFluid> source = this.source;
        Preconditions.checkNotNull(source, "Fluid has no source block: " + sourceName);
        return source.get();
    }

    private BaseFlowingFluid.Properties makeProperties() {
        Supplier<? extends BaseFlowingFluid> source = this.source;
        BaseFlowingFluid.Properties ret = new BaseFlowingFluid.Properties(fluidType, source, valueSupplier);
        fluidProperties.accept(ret);
        return ret;
    }

    private FluidType.Properties makeTypeProperties() {
        FluidType.Properties properties = FluidType.Properties.create();
        this.typeProperties.accept(properties);
        properties.descriptionId(
                Identifier.fromNamespaceAndPath(core.getModid(), sourceName).toLanguageKey("fluid"));
        return properties;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @StandardAPI
    public FluidEntry<T> register() {
        if (this.registerType) {
            core.registry(
                    this.sourceName, NeoForgeRegistries.Keys.FLUID_TYPES, _ -> this.fluidType.get());
        }

        if (defaultSource == Boolean.TRUE) {
            source(BaseFlowingFluid.Source::new);
        }
        if (defaultBlock == Boolean.TRUE) {
            block(FunctionUtil.noOpConsumer());
        }
        if (defaultBucket == Boolean.TRUE) {
            bucket(FunctionUtil.noOpConsumer());
        }

        Supplier<? extends BaseFlowingFluid> source = this.source;
        if (source != null) {
            core.registry(sourceName, Registries.FLUID, _ -> source.get());
        } else {
            throw new IllegalStateException("Fluid must have a source version: " + name);
        }

        return (FluidEntry<T>) super.register();
    }

    @Override
    protected T createEntry(ResourceKey<Fluid> key) {
        return fluidFactory.create(makeProperties());
    }

    @Override
    protected RegistryEntry<Fluid, T> createEntryWrapper(ResourceKey<Fluid> key) {
        return new FluidEntry<>(core, key);
    }

    // --- DefaultFluidTypeExtension ---

    public static class DefaultFluidTypeExtension implements IClientFluidTypeExtensions {

        private final Identifier stillTexture, flowingTexture;
        private final int tintColor;

        public DefaultFluidTypeExtension(
                                         Identifier stillTexture, Identifier flowingTexture, int tintColor) {
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
            this.tintColor = tintColor;
        }

        @Override
        public Identifier getStillTexture() {
            return stillTexture;
        }

        @Override
        public Identifier getFlowingTexture() {
            return flowingTexture;
        }

        @Override
        public int getTintColor() {
            return tintColor;
        }

        @Override
        public void modifyFogColor(
                                   net.minecraft.client.Camera camera,
                                   float partialTick,
                                   net.minecraft.client.multiplayer.ClientLevel level,
                                   int renderDistance,
                                   float darkenWorldAmount,
                                   org.joml.Vector4f fluidFogColor) {
            if (tintColor != -1) {
                fluidFogColor.x = (tintColor >> 16 & 0xFF) / 255.0f;
                fluidFogColor.y = (tintColor >> 8 & 0xFF) / 255.0f;
                fluidFogColor.z = (tintColor & 0xFF) / 255.0f;
                fluidFogColor.w = 1.0f;
            }
        }
    }
}
