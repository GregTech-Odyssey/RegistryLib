package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.generator.RegistryLibBlockModelGenerator;
import com.gto.registrylib.datagen.loot.RegistryLibBlockLootTables;
import com.gto.registrylib.datagen.loot.RegistryLibLootTableProvider.LootType;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.ImageUtil;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBuilder<T extends Block, P>
                         extends AbstractBuilder<Block, T, P, BlockBuilder<T, P>> {

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

    protected BlockBuilder(
                           RegistryCore core, P parent, String name, Function<BlockBehaviour.Properties, T> factory) {
        super(core, parent, name, Registries.BLOCK);
        this.factory = factory;
    }

    // === Sub-resource Configuration (Consumer-scoped, returns this BlockBuilder) ===
    @StandardAPI("Configures an ItemBuilder for the BlockItem sub-entry via lambda.")
    public BlockBuilder<T, P> item(
                                   @Nonnull Consumer<ItemBuilder<BlockItem, BlockBuilder<T, P>>> consumer) {
        return item(BlockItem::new, consumer);
    }

    @StandardAPI("Configures an ItemBuilder with a custom item factory via lambda.")
    public <I extends Item> BlockBuilder<T, P> item(
                                                    @Nonnull BiFunction<? super T, Item.Properties, ? extends I> factory,
                                                    @Nonnull Consumer<ItemBuilder<I, BlockBuilder<T, P>>> consumer) {
        var builder = core.<I, BlockBuilder<T, P>>item(
                this, name, p -> factory.apply(getValue(), p.useBlockDescriptionPrefix()), false)
                .setData(ProviderType.LANG, FunctionUtil.noOpConsumer())
                .model(
                        () -> (ctx, prov) -> core.getDataProvider(ProviderType.BLOCKSTATE)
                                .map(g -> g.seenBlockstates.get(getValue()))
                                .flatMap(BlockStateModelDispatcher::simpleModels)
                                .map(b -> b.models().get(""))
                                .map(
                                        unbaked -> {
                                            if (unbaked instanceof SingleVariant.Unbaked(Variant variant)) {
                                                return variant.modelLocation();
                                            }
                                            return null;
                                        })
                                .ifPresent(model -> prov.createWithExistingModel(ctx, model)));
        if (defaultItemTab != null) {
            builder.addTab(defaultItemTab);
        }
        consumer.accept(builder);
        return builder.build();
    }

    /**
     * Sets a default creative tab that will be applied to any BlockItem created via {@link #item}.
     */
    @StandardAPI
    public BlockBuilder<T, P> defaultItemTab(@Nonnull ResourceKey<CreativeModeTab> tab) {
        this.defaultItemTab = tab;
        return this;
    }

    @StandardAPI("Configures a BlockEntityBuilder for the block entity sub-entry via lambda.")
    public <BE extends BlockEntity> BlockBuilder<T, P> blockEntity(
                                                                   @Nonnull BlockEntityBuilder.BlockEntityFactory<BE> beFactory,
                                                                   @Nonnull Consumer<BlockEntityBuilder<BE, BlockBuilder<T, P>>> consumer) {
        var builder = core.blockEntity(this, name, beFactory).validBlock(this::getValue);
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

    @StandardAPI("Commonly used for vanilla")
    public BlockBuilder<T, P> initialProperties(Block block) {
        initialProperties = () -> BlockBehaviour.Properties.ofFullCopy(block);
        return this;
    }

    @StandardAPI
    public BlockBuilder<T, P> texture(Supplier<BufferedImage> image) {
        if (!core.doDatagen()) return this;
        setData(
                ProviderType.GENERAL_RESOURCE,
                p -> p.addBlockTexture(
                        (path, stream) -> {
                            try {
                                return ImageUtil.writeToStream(name, image.get(), path, stream);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }));
        return this;
    }

    @StandardAPI
    public BlockBuilder<T, P> blockstate(
                                         @NotNull Supplier<BiConsumer<T, RegistryLibBlockModelGenerator>> cons) {
        if (!core.doDatagen()) return this;
        return setData(ProviderType.BLOCKSTATE, p -> cons.get().accept(getValue(), p));
    }

    @StandardAPI
    public BlockBuilder<T, P> lang(@NotNull String name) {
        return lang(Block::getDescriptionId, name);
    }

    @SyntaxSugar("lang(type, Block::getDescriptionId, name)")
    public BlockBuilder<T, P> lang(
                                   @Nonnull ProviderType<? extends RegistryLibLangProvider> type, @Nonnull String name) {
        return lang(type, Block::getDescriptionId, name);
    }

    @StandardAPI
    public BlockBuilder<T, P> loot(@NotNull BiConsumer<RegistryLibBlockLootTables, T> cons) {
        if (!core.doDatagen()) return this;
        return setData(
                ProviderType.LOOT,
                prov -> prov.addLootAction(
                        LootType.BLOCK,
                        tb -> {
                            if (getValue().getLootTable().isPresent()) {
                                cons.accept(tb, getValue());
                            }
                        }));
    }

    @SafeVarargs
    @StandardAPI
    public final BlockBuilder<T, P> tag(@NotNull TagKey<Block>... tags) {
        return tag(ProviderType.BLOCK_TAGS, tags);
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
        return factory.apply(properties.setId(key));
    }

    @Override
    protected RegistryEntry<Block, T> createEntryWrapper(ResourceKey<Block> key) {
        return new BlockEntry<>(key);
    }

    @Override
    @StandardAPI
    public BlockEntry<T> register() {
        return (BlockEntry<T>) super.register();
    }
}
