package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.providers.DataGenContext;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylib.providers.generators.RegistryLibBlockModelGenerator;
import com.gto.registrylib.providers.generators.RegistryLibRecipeProvider;
import com.gto.registrylib.providers.loot.RegistryLibBlockLootTables;
import com.gto.registrylib.providers.loot.RegistryLibLootTableProvider.LootType;
import com.gto.registrylib.util.FunctionUtil;
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

import java.util.function.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBuilder<T extends Block, P>
                         extends AbstractBuilder<Block, T, P, BlockBuilder<T, P>> {

    public static <T extends Block, P> BlockBuilder<T, P> create(
                                                                 RegistryCore owner,
                                                                 P parent,
                                                                 String name,
                                                                 BuilderCallback callback,
                                                                 Function<BlockBehaviour.Properties, T> factory) {
        return new BlockBuilder<>(owner, parent, name, callback, factory, BlockBehaviour.Properties::of)
                .defaultBlockstate()
                .defaultLoot()
                .defaultLang();
    }

    private final Function<BlockBehaviour.Properties, T> factory;
    private Supplier<BlockBehaviour.Properties> initialProperties;
    private Function<BlockBehaviour.Properties, BlockBehaviour.Properties> propertiesCallback = UnaryOperator.identity();
    @Nullable
    private ResourceKey<CreativeModeTab> defaultItemTab;

    protected BlockBuilder(
                           RegistryCore owner,
                           P parent,
                           String name,
                           BuilderCallback callback,
                           Function<BlockBehaviour.Properties, T> factory,
                           Supplier<BlockBehaviour.Properties> initialProperties) {
        super(owner, parent, name, callback, Registries.BLOCK);
        this.factory = factory;
        this.initialProperties = initialProperties;
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
        var builder = getOwner()
                .<I, BlockBuilder<T, P>>item(
                        this, getName(), p -> factory.apply(getEntry(), p.useBlockDescriptionPrefix()))
                .setData(ProviderType.LANG, (ctx, prov) -> {})
                .model(
                        () -> (ctx, prov) -> getOwner()
                                .getDataProvider(ProviderType.BLOCKSTATE)
                                .map(g -> g.seenBlockstates.get(getEntry()))
                                .flatMap(BlockStateModelDispatcher::simpleModels)
                                .map(b -> b.models().get(""))
                                .map(
                                        unbaked -> {
                                            if (unbaked instanceof SingleVariant.Unbaked(Variant variant)) {
                                                return variant.modelLocation();
                                            }
                                            return null;
                                        })
                                .ifPresent(model -> prov.createWithExistingModel(ctx.get(), model)));
        if (defaultItemTab != null) {
            builder.tab(defaultItemTab);
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
        var builder = getOwner().blockEntity(this, getName(), beFactory).validBlock(this::getEntry);
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
        return blockstate(() -> (ctx, prov) -> prov.createTrivialCube(ctx.getEntry()));
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

    @StandardAPI
    public BlockBuilder<T, P> initialProperties(@NotNull Supplier<? extends Block> block) {
        initialProperties = () -> BlockBehaviour.Properties.ofFullCopy(block.get());
        return this;
    }

    @StandardAPI
    public BlockBuilder<T, P> blockstate(
                                         @NotNull Supplier<BiConsumer<DataGenContext<Block, T>, RegistryLibBlockModelGenerator>> cons) {
        if (!getOwner().doDatagen()) return this;
        return setData(ProviderType.BLOCKSTATE, cons.get());
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
        return setData(
                ProviderType.LOOT,
                (ctx, prov) -> prov.addLootAction(
                        LootType.BLOCK,
                        tb -> {
                            if (ctx.getEntry().getLootTable().isPresent()) {
                                cons.accept(tb, ctx.getEntry());
                            }
                        }));
    }

    @StandardAPI
    public BlockBuilder<T, P> recipe(
                                     @NotNull BiConsumer<DataGenContext<Block, T>, RegistryLibRecipeProvider> cons) {
        return setData(ProviderType.RECIPE, cons);
    }

    @SafeVarargs
    @StandardAPI
    public final BlockBuilder<T, P> tag(@NotNull TagKey<Block>... tags) {
        return tag(ProviderType.BLOCK_TAGS, tags);
    }

    @Override
    protected T createEntry(ResourceKey<Block> key) {
        BlockBehaviour.Properties properties = this.initialProperties.get();
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
