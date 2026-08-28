package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.client.Client;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.generator.RegistryLibBlockModelGenerator;
import com.gto.registrylib.datagen.loot.RegistryLibBlockLootTables;
import com.gto.registrylib.datagen.loot.RegistryLibLootTableProvider.LootType;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.util.DistExecutor;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.RegistryLibTintSources;
import com.gto.registrylib.util.TextureRef;
import com.gto.registrylib.util.color.ArgbColor;
import com.gto.registrylib.util.color.RgbColor;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import com.gto.registrylib.util.visual.BlockModelLayer;
import com.gto.registrylib.util.visual.BlockVisualPreset;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class BlockBuilder<T extends Block, P>
                         extends AbstractBuilder<Block, T, P, BlockBuilder<T, P>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockBuilder.class);

    public static <T extends Block, P> BlockBuilder<T, P> create(
                                                                 RegistryCore owner, P parent, String name, Function<BlockBehaviour.Properties, T> factory) {
        return new BlockBuilder<>(owner, parent, name, factory)
                .defaultBlockstate()
                .defaultLoot()
                .defaultLang();
    }

    private final Function<BlockBehaviour.Properties, T> factory;
    private Supplier<BlockBehaviour.Properties> initialProperties;
    private Function<BlockBehaviour.Properties, BlockBehaviour.Properties> propertiesCallback = FunctionUtil.identityFn();
    @Nullable
    private ResourceKey<CreativeModeTab> defaultItemTab;

    /** 方块物品的常量着色——1.21.1 由运行时 {@link ItemColor} 驱动。 */
    @Nullable
    private RgbColor blockItemTintColor;

    /** 方块（运行时）着色器数量。非客户端；仅用于 tintindex 校验。 */
    private int blockTintSourceCount;

    @Nullable
    private ArgbColor[] knownBlockTintColors;
    @Nullable
    private Integer maxBlockTintIndex;

    protected BlockBuilder(
                           RegistryCore core, P parent, String name, Function<BlockBehaviour.Properties, T> factory) {
        super(core, parent, name, Registries.BLOCK);
        this.factory = factory;
    }

    // === Sub-resource Configuration (Consumer-scoped, returns this BlockBuilder) ===
    @StandardAPI("Configures an ItemBuilder for the BlockItem sub-entry via lambda.")
    public BlockBuilder<T, P> item(
                                   @NotNull Consumer<ItemBuilder<BlockItem, BlockBuilder<T, P>>> consumer) {
        return item(BlockItem::new, consumer);
    }

    @StandardAPI("Configures an ItemBuilder with a custom item factory via lambda.")
    public <I extends Item> BlockBuilder<T, P> item(
                                                    @NotNull BiFunction<? super T, Item.Properties, ? extends I> factory,
                                                    @NotNull Consumer<ItemBuilder<I, BlockBuilder<T, P>>> consumer) {
        var supplier = getValueSupplier();
        var builder = core.<I, BlockBuilder<T, P>>item(this, name, p -> factory.apply(supplier.get(), p), false)
                .setData(ProviderType.LANG, FunctionUtil.noOpConsumer())
                .model(
                        () -> (ctx, prov) -> {
                            if (!core.isBlockExcludedFromModelValidation(name)) {
                                // 1.21.1：block item 模型委托给方块模型（方块模型的面自带 tintindex，
                                // 物品着色由 ItemColors 自动委托给 BlockColors）。
                                prov.createWithExistingModel(
                                        ctx, ModelLocationUtils.getModelLocation(getValue()));
                            } else {
                                prov.generateFlatItem(ctx, TextureMapping.getBlockTexture(getValue()));
                            }
                        });
        if (defaultItemTab != null) {
            builder.addTab(defaultItemTab);
        }
        if (blockItemTintColor != null && !core.isBlockExcludedFromModelValidation(name)) {
            // 方块物品使用独立于 BlockColors 委托的常量 ItemColor（例如方块本身不着色、物品着色）。
            final RgbColor tintColor = blockItemTintColor;
            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> Client.registerItemTintSources(
                            builder.getValueSupplier(), RegistryLibTintSources.itemConstant(tintColor)));
        }
        consumer.accept(builder);
        return builder.build();
    }

    /**
     * Sets a default creative tab that will be applied to any BlockItem created via {@link #item}.
     */
    @StandardAPI
    public BlockBuilder<T, P> defaultItemTab(@NotNull ResourceKey<CreativeModeTab> tab) {
        this.defaultItemTab = tab;
        return this;
    }

    @StandardAPI("Configures a BlockEntityBuilder for the block entity sub-entry via lambda.")
    public <BE extends BlockEntity> BlockBuilder<T, P> blockEntity(
                                                                   @NotNull BlockEntityBuilder.BlockEntityFactory<BE> beFactory,
                                                                   @NotNull Consumer<BlockEntityBuilder<BE, BlockBuilder<T, P>>> consumer) {
        Supplier<T> supplier = getValueSupplier();
        var builder = core.blockEntity(this, name, beFactory).validBlock(supplier::get);
        consumer.accept(builder);
        return builder.build();
    }

    // === Syntax Sugar ===

    @SyntaxSugar("item(FunctionUtil.noOpConsumer())")
    public BlockBuilder<T, P> simpleItem() {
        return item(FunctionUtil.noOpConsumer());
    }

    @SyntaxSugar("blockstate(() -> (ctx, prov) -> prov.createTrivialCube(ctx.getEntry()))")
    public BlockBuilder<T, P> defaultBlockstate() {
        return blockstate(() -> (value, prov) -> prov.createTrivialCube(value));
    }

    @SyntaxSugar("lang(Block::getDescriptionId)")
    public BlockBuilder<T, P> defaultLang() {
        return lang(Block::getDescriptionId);
    }

    @SyntaxSugar("loot(RegistryLibBlockLootTables::dropSelf)")
    public BlockBuilder<T, P> defaultLoot() {
        return loot(RegistryLibBlockLootTables::dropSelf);
    }

    // === Configuration ===

    @StandardAPI
    public BlockBuilder<T, P> properties(@NotNull UnaryOperator<BlockBehaviour.Properties> func) {
        propertiesCallback = propertiesCallback.andThen(func);
        return this;
    }

    @StandardAPI("Delayed registration entries applicable to other mods.")
    public BlockBuilder<T, P> initialProperties(@NotNull Supplier<? extends Block> block) {
        initialProperties = () -> BlockBehaviour.Properties.ofFullCopy(block.get());
        return this;
    }

    public BlockBuilder<T, P> setInitialProperties(
                                                   @NotNull Supplier<BlockBehaviour.Properties> properties) {
        this.initialProperties = properties;
        return this;
    }

    @SyntaxSugar("initialProperties(() -> block)")
    public BlockBuilder<T, P> initialProperties(Block block) {
        initialProperties = () -> BlockBehaviour.Properties.ofFullCopy(block);
        return this;
    }

    @StandardAPI
    public BlockBuilder<T, P> texture(String path, Supplier<BufferedImage> image) {
        if (!core.doDatagen()) return this;
        return addData(
                ProviderType.GENERAL_RESOURCE, p -> p.addBlockTexture(p.simpleTexture(path, image)));
    }

    @SyntaxSugar("texture(name, image)")
    public BlockBuilder<T, P> texture(Supplier<BufferedImage> image) {
        return texture(name, image);
    }

    @StandardAPI("Opts out of blockstate datagen so the block can use hand-written blockstate JSON.")
    public BlockBuilder<T, P> noBlockstate() {
        if (!core.doDatagen()) return this;
        core.excludeBlockFromModelValidation(name);
        return setData(ProviderType.BLOCKSTATE, FunctionUtil.noOpConsumerStatic());
    }

    @StandardAPI
    public BlockBuilder<T, P> blockstate(
                                         @NotNull Supplier<BiConsumer<T, RegistryLibBlockModelGenerator>> cons) {
        if (!core.doDatagen()) return this;
        core.includeBlockInModelValidation(name);
        return setData(ProviderType.BLOCKSTATE, p -> cons.get().accept(getValue(), p));
    }

    @StandardAPI
    public BlockBuilder<T, P> tintedCube(int tintIndex) {
        trackBlockTintIndex(tintIndex);
        return blockstate(() -> (ctx, prov) -> prov.createTintedCube(ctx, tintIndex));
    }

    @StandardAPI
    public BlockBuilder<T, P> tintedCube(@NotNull String texturePath, int tintIndex) {
        return tintedCube(core.texture(texturePath), tintIndex);
    }

    @StandardAPI
    public BlockBuilder<T, P> tintedCube(@NotNull ResourceLocation texture, int tintIndex) {
        return tintedCube(TextureRef.of(texture), tintIndex);
    }

    @StandardAPI
    public BlockBuilder<T, P> tintedCube(@NotNull TextureRef texture, int tintIndex) {
        trackBlockTintIndex(tintIndex);
        return blockstate(() -> (ctx, prov) -> prov.createTintedCube(ctx, texture.id(), tintIndex));
    }

    @StandardAPI
    public BlockBuilder<T, P> constantTint(@NotNull RgbColor color) {
        blockConstantTint(color);
        return blockItemConstantTint(color);
    }

    /** 记录方块物品的常量着色（运行时注册见 {@link #item(Consumer)} 中的处理）。 */
    private BlockBuilder<T, P> blockItemConstantTint(@NotNull RgbColor color) {
        blockItemTintColor = color;
        return this;
    }

    @StandardAPI
    public BlockBuilder<T, P> constantTint(@NotNull String texturePath, @NotNull RgbColor color) {
        return tintedCube(texturePath, 0).constantTint(color);
    }

    @StandardAPI
    public BlockBuilder<T, P> constantTint(@NotNull TextureRef texture, @NotNull RgbColor color) {
        return tintedCube(texture, 0).constantTint(color);
    }

    @StandardAPI
    public BlockBuilder<T, P> tintSource(@NotNull Supplier<Supplier<ItemColor[]>> tintSources) {
        if (!core.doDatagen()) return this;
        final ItemColor[] sources = tintSources.get().get();
        final Supplier<? extends Item> itemSupplier = () -> getValue().asItem();
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> Client.registerItemTintSources(
                        itemSupplier,
                        (stack, tintIndex) -> tintIndex >= 0 && tintIndex < sources.length ? sources[tintIndex].getColor(stack, tintIndex) : -1));
        core.setDataGenerator(
                name, Registries.ITEM, ProviderType.ITEM_MODEL, p -> p.generateTintedBlockItem(getValue()));
        return this;
    }

    @StandardAPI
    public BlockBuilder<T, P> blockTintSource(
                                              @NotNull Supplier<Supplier<BlockColor[]>> tintSourcesSupplier) {
        knownBlockTintColors = null;
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {
                    BlockColor[] sources = tintSourcesSupplier.get().get();
                    blockTintSourceCount = sources.length;
                    Client.registerBlockTintSources(getValueSupplier(), sources);
                });
        return this;
    }

    @StandardAPI
    public BlockBuilder<T, P> blockConstantTint(@NotNull RgbColor color) {
        return blockConstantTint(color.opaque());
    }

    @StandardAPI
    public BlockBuilder<T, P> blockConstantTint(@NotNull ArgbColor color) {
        if (color.isTransparent()) {
            LOGGER.warn(
                    "Block '{}' uses a fully transparent ARGB block tint color: 0x{}",
                    name,
                    Integer.toHexString(color.argb()));
        }
        knownBlockTintColors = new ArgbColor[] { color };
        blockTintSourceCount = 1;
        // Build the client BlockColor lazily inside the Dist.CLIENT lambda so the dedicated
        // server never resolves BlockColor.
        final ArgbColor tintColor = color;
        final Supplier<? extends Block> blockSupplier = getValueSupplier();
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> Client.registerBlockTintSources(
                        blockSupplier, RegistryLibTintSources.blockConstant(tintColor)));
        return this;
    }

    @StandardAPI
    public BlockBuilder<T, P> layeredCube(
                                          @NotNull TextureRef particle, @NotNull BlockModelLayer... layers) {
        for (BlockModelLayer layer : layers) {
            if (layer.hasTint()) {
                trackBlockTintIndex(layer.tintIndex());
            }
        }
        return blockstate(() -> (ctx, prov) -> prov.createLayeredCube(ctx, particle, layers));
    }

    @StandardAPI
    public BlockBuilder<T, P> existingTexture(@NotNull String texturePath) {
        return modelTexture(texturePath);
    }

    @StandardAPI
    public BlockBuilder<T, P> existingTexture(@NotNull TextureRef texture) {
        return modelTexture(texture);
    }

    @StandardAPI
    public BlockBuilder<T, P> modelTexture(@NotNull String texturePath) {
        return modelTexture(core.texture(texturePath));
    }

    @StandardAPI
    public BlockBuilder<T, P> modelTexture(@NotNull TextureRef texture) {
        return blockstate(
                () -> (ctx, prov) -> prov.generateWithTemplate(
                        ctx,
                        ModelTemplates.CUBE_ALL,
                        new TextureMapping().put(TextureSlot.ALL, texture.id())));
    }

    @StandardAPI
    public BlockBuilder<T, P> visual(@NotNull BlockVisualPreset preset) {
        preset.apply(this);
        return this;
    }

    @StandardAPI
    public BlockBuilder<T, P> debugTint() {
        int maxTintIndex = maxBlockTintIndex == null ? -1 : maxBlockTintIndex;
        int itemTintCount = blockItemTintColor != null ? 1 : 0;
        LOGGER.info(
                "RegistryLib tint debug for block '{}': maxBlockTintIndex={}, blockTintSourceCount={}, blockTintColors={}, blockItemTintSourceCount={}",
                name,
                maxTintIndex,
                blockTintSourceCount,
                describeColors(knownBlockTintColors),
                itemTintCount);
        validateKnownBlockTintState();
        return this;
    }

    private void trackBlockTintIndex(int tintIndex) {
        if (tintIndex < 0) return;
        maxBlockTintIndex = maxBlockTintIndex == null ? tintIndex : Math.max(maxBlockTintIndex, tintIndex);
    }

    private void validateKnownBlockTintState() {
        if (maxBlockTintIndex == null) return;
        if (blockTintSourceCount <= maxBlockTintIndex) {
            LOGGER.warn(
                    "Block '{}' model uses tintindex {} but only {} block tint source(s) are configured",
                    name,
                    maxBlockTintIndex,
                    blockTintSourceCount);
        }
    }

    private static String describeColors(@Nullable ArgbColor[] colors) {
        return ArgbColor.describeColors(colors);
    }

    @SyntaxSugar("lang(Block::getDescriptionId, name)")
    public BlockBuilder<T, P> lang(@NotNull String name) {
        return lang(Block::getDescriptionId, name);
    }

    @SyntaxSugar("lang(type, Block::getDescriptionId, name)")
    public BlockBuilder<T, P> lang(
                                   @NotNull ProviderType<? extends RegistryLibLangProvider> type, @NotNull String name) {
        return lang(type, Block::getDescriptionId, name);
    }

    /**
     * Register display names for multiple locales at once.
     *
     * @param localeToName map of locale code (e.g. {@code "en_us"}, {@code "zh_cn"}) to display name
     */
    @StandardAPI
    public BlockBuilder<T, P> lang(@NotNull Map<String, String> localeToName) {
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

    @StandardAPI
    public BlockBuilder<T, P> loot(@NotNull BiConsumer<RegistryLibBlockLootTables, T> cons) {
        if (!core.doDatagen()) return this;
        return setData(
                ProviderType.LOOT,
                prov -> prov.addLootAction(
                        LootType.BLOCK,
                        tb -> {
                            if (!getValue()
                                    .getLootTable()
                                    .equals(net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY)) {
                                cons.accept(tb, getValue());
                            }
                        }));
    }

    @SafeVarargs
    @StandardAPI
    public final BlockBuilder<T, P> addTag(@NotNull TagKey<Block>... tags) {
        return addTag(ProviderType.BLOCK_TAGS, false, tags);
    }

    @SafeVarargs
    @StandardAPI
    public final BlockBuilder<T, P> addItemTag(@NotNull TagKey<Item>... tags) {
        return addTag(ProviderType.ITEM_TAGS, false, tags);
    }

    @Override
    protected T createEntry(ResourceKey<Block> key) {
        BlockBehaviour.Properties properties;
        var initialProperties = this.initialProperties;
        if (initialProperties == null) {
            properties = BlockBehaviour.Properties.of();
        } else {
            properties = initialProperties.get();
        }
        properties = propertiesCallback.apply(properties);
        return factory.apply(properties);
    }

    @Override
    protected RegistryEntry<Block, T> createEntryWrapper(ResourceKey<Block> key) {
        return new BlockEntry<>(key);
    }

    @Override
    @StandardAPI
    public BlockEntry<T> register() {
        validateKnownBlockTintState();
        return (BlockEntry<T>) super.register();
    }
}
