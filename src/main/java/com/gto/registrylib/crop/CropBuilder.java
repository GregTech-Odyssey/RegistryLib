package com.gto.registrylib.crop;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.loot.RegistryLibBlockLootTables;
import com.gto.registrylib.util.TextureRef;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.ItemEntry;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class CropBuilder<P> {

    private final RegistryCore core;
    private final P parent;
    private final String name;
    private UnaryOperator<BlockBehaviour.Properties> properties = UnaryOperator.identity();
    private RegistryLibCropBlock.GrowthRoll growthRoll = (_state, _level, _pos, _random) -> true;
    private RegistryLibCropBlock.HarvestCallback harvestCallback = (_state, _level, _pos, _player) -> {};
    private boolean rightClickHarvest = true;
    private Consumer<com.gto.registrylib.builders.ItemBuilder<Item, RegistryCore>> seedConfig = _builder -> {};
    private Supplier<? extends Item> produceItem;
    private TextureRef[] stageTextures;

    private ItemEntry<Item> seedEntry;

    public CropBuilder(RegistryCore core, P parent, String name) {
        this.core = core;
        this.parent = parent;
        this.name = name;
    }

    public static <P> CropBuilder<P> create(RegistryCore core, P parent, String name) {
        return new CropBuilder<>(core, parent, name);
    }

    @StandardAPI
    public CropBuilder<P> properties(UnaryOperator<BlockBehaviour.Properties> properties) {
        UnaryOperator<BlockBehaviour.Properties> previous = this.properties;
        this.properties = p -> properties.apply(previous.apply(p));
        return this;
    }

    @StandardAPI
    public CropBuilder<P> growthRoll(RegistryLibCropBlock.GrowthRoll growthRoll) {
        this.growthRoll = growthRoll;
        return this;
    }

    @StandardAPI
    public CropBuilder<P> onHarvest(RegistryLibCropBlock.HarvestCallback harvestCallback) {
        this.harvestCallback = harvestCallback;
        return this;
    }

    @StandardAPI
    public CropBuilder<P> rightClickHarvest(boolean rightClickHarvest) {
        this.rightClickHarvest = rightClickHarvest;
        return this;
    }

    @StandardAPI
    public CropBuilder<P> seedItem(
                                   Consumer<com.gto.registrylib.builders.ItemBuilder<Item, RegistryCore>> seedConfig) {
        this.seedConfig = seedConfig;
        return this;
    }

    @StandardAPI
    public CropBuilder<P> produce(Supplier<? extends Item> produceItem) {
        this.produceItem = produceItem;
        return this;
    }

    @StandardAPI
    public CropBuilder<P> stageTextures(TextureRef... stageTextures) {
        this.stageTextures = stageTextures.clone();
        return this;
    }

    @StandardAPI
    public BlockEntry<RegistryLibCropBlock> register() {
        com.gto.registrylib.builders.ItemBuilder<Item, RegistryCore> seedBuilder = core.item(name + "_seeds", Item::new);
        seedConfig.accept(seedBuilder);
        seedEntry = seedBuilder.register();
        BlockEntry<RegistryLibCropBlock> crop = core
                .block(
                        name,
                        p -> new RegistryLibCropBlock(
                                p,
                                seedEntry,
                                growthRoll,
                                harvestCallback,
                                rightClickHarvest))
                .properties(properties)
                .blockstate(() -> (block, prov) -> {
                    if (stageTextures != null && stageTextures.length > 0) {
                        prov.generateCropStages(block, stageTextures);
                    }
                })
                .loot((tables, block) -> cropLoot(tables, block))
                .register();
        return crop;
    }

    @StandardAPI
    public P build() {
        register();
        return parent;
    }

    private void cropLoot(RegistryLibBlockLootTables tables, RegistryLibCropBlock block) {
        Item grown = produceItem == null ? seedEntry.get() : produceItem.get();
        tables.add(
                block,
                tables.createCropDrops(
                        block,
                        grown,
                        seedEntry.get(),
                        LootItemBlockStatePropertyCondition
                                .hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, block.getMaxAge()))));
    }
}
