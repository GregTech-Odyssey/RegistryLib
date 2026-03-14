package com.gto.registrylib.providers.loot;

import com.gto.registrylib.RegistryLib;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jspecify.annotations.NonNull;

public class RegistryLibBlockLootTables extends BlockLootSubProvider
    implements RegistryLibLootTables {
  private final RegistryLib parent;
  private final Consumer<RegistryLibBlockLootTables> callback;

  private final HolderLookup<Item> itemLookup;
  private final HolderLookup<Block> blockLookup;
  private final HolderLookup<EntityType<?>> entityLookup;

  public RegistryLibBlockLootTables(
      HolderLookup.Provider provider,
      RegistryLib parent,
      Consumer<RegistryLibBlockLootTables> callback) {
    super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    this.parent = parent;
    this.callback = callback;
    itemLookup = registries.lookupOrThrow(Registries.ITEM);
    blockLookup = registries.lookupOrThrow(Registries.BLOCK);
    entityLookup = registries.lookupOrThrow(Registries.ENTITY_TYPE);
  }

  @Override
  protected void generate() {
    callback.accept(this);
  }

  @Override
  protected @NonNull Iterable<Block> getKnownBlocks() {
    return parent.getAll(Registries.BLOCK).stream().map(Supplier::get).collect(Collectors.toList());
  }

  public HolderLookup.Provider getRegistries() {
    return this.registries;
  }

  public HolderLookup<Item> itemLookup() {
    return itemLookup;
  }

  public HolderLookup<Block> blockLookup() {
    return blockLookup;
  }

  public HolderLookup<EntityType<?>> entityLookup() {
    return entityLookup;
  }

  // Expose protected methods from BlockLootSubProvider

  @Override
  public <T extends FunctionUserBuilder<T>> T applyExplosionDecay(
      ItemLike item, FunctionUserBuilder<T> functionBuilder) {
    return super.applyExplosionDecay(item, functionBuilder);
  }

  @Override
  public <T extends ConditionUserBuilder<T>> T applyExplosionCondition(
      ItemLike item, ConditionUserBuilder<T> conditionBuilder) {
    return super.applyExplosionCondition(item, conditionBuilder);
  }

  @Override
  public LootTable.Builder createSilkTouchDispatchTable(
      Block block, LootPoolEntryContainer.Builder<?> builder) {
    return super.createSilkTouchDispatchTable(block, builder);
  }

  @Override
  public LootTable.Builder createShearsDispatchTable(
      Block block, LootPoolEntryContainer.Builder<?> builder) {
    return super.createShearsDispatchTable(block, builder);
  }

  @Override
  public LootTable.Builder createSilkTouchOrShearsDispatchTable(
      Block block, LootPoolEntryContainer.Builder<?> builder) {
    return super.createSilkTouchOrShearsDispatchTable(block, builder);
  }

  @Override
  public LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike item) {
    return super.createSingleItemTableWithSilkTouch(block, item);
  }

  @Override
  public LootTable.Builder createSingleItemTable(ItemLike item, NumberProvider count) {
    return super.createSingleItemTable(item, count);
  }

  @Override
  public LootTable.Builder createSingleItemTableWithSilkTouch(
      Block block, ItemLike item, NumberProvider count) {
    return super.createSingleItemTableWithSilkTouch(block, item, count);
  }

  @Override
  public LootTable.Builder createSilkTouchOnlyTable(ItemLike item) {
    return super.createSilkTouchOnlyTable(item);
  }

  @Override
  public LootTable.Builder createSlabItemTable(Block block) {
    return super.createSlabItemTable(block);
  }

  @Override
  public LootTable.Builder createNameableBlockEntityTable(Block block) {
    return super.createNameableBlockEntityTable(block);
  }

  @Override
  public LootTable.Builder createOreDrop(Block block, Item item) {
    return super.createOreDrop(block, item);
  }

  @Override
  public LootTable.Builder createLeavesDrops(
      Block leavesBlock, Block saplingBlock, float... chances) {
    return super.createLeavesDrops(leavesBlock, saplingBlock, chances);
  }

  @Override
  public LootTable.Builder createCropDrops(
      Block cropBlock,
      Item grownCropItem,
      Item seedsItem,
      LootItemCondition.Builder dropGrownCropCondition) {
    return super.createCropDrops(cropBlock, grownCropItem, seedsItem, dropGrownCropCondition);
  }

  @Override
  public LootTable.Builder createDoorTable(Block doorBlock) {
    return super.createDoorTable(doorBlock);
  }

  @Override
  public void dropSelf(Block block) {
    super.dropSelf(block);
  }

  @Override
  public void add(Block block, LootTable.Builder builder) {
    super.add(block, builder);
  }

  @Override
  public void dropOther(Block block, ItemLike item) {
    super.dropOther(block, item);
  }

  @Override
  public void dropWhenSilkTouch(Block block) {
    super.dropWhenSilkTouch(block);
  }
}
