package com.gto.registrylib.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class RegistryLibCropBlock extends CropBlock {

    @FunctionalInterface
    public interface GrowthRoll {

        boolean shouldGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);
    }

    @FunctionalInterface
    public interface HarvestCallback {

        void onHarvest(BlockState state, Level level, BlockPos pos, Player player);
    }

    private final Supplier<? extends Item> seedItem;
    private final GrowthRoll growthRoll;
    private final HarvestCallback harvestCallback;
    private final boolean rightClickHarvest;

    public RegistryLibCropBlock(
                                Properties properties,
                                Supplier<? extends Item> seedItem,
                                GrowthRoll growthRoll,
                                HarvestCallback harvestCallback,
                                boolean rightClickHarvest) {
        super(properties);
        this.seedItem = seedItem;
        this.growthRoll = growthRoll;
        this.harvestCallback = harvestCallback;
        this.rightClickHarvest = rightClickHarvest;
    }

    @Override
    protected Item getBaseSeedId() {
        return seedItem.get();
    }

    @Override
    protected void randomTick(
                              BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (growthRoll.shouldGrow(state, level, pos, random)) {
            super.randomTick(state, level, pos, random);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
                                               BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!rightClickHarvest || !isMaxAge(state)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        harvestCallback.onHarvest(state, level, pos, player);
        level.destroyBlock(pos, true, player);
        level.setBlock(pos, getStateForAge(0), 3);
        return InteractionResult.SUCCESS;
    }
}
