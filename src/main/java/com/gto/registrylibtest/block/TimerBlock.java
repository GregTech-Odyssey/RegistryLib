package com.gto.registrylibtest.block;

import com.gto.registrylibtest.blockentity.TimerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

public class TimerBlock extends Block implements EntityBlock {

    private final int tier;

    public TimerBlock(BlockBehaviour.Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TimerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
                                                                            Level level, BlockState blockState, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (BlockEntityTicker<TimerBlockEntity>) TimerBlockEntity::serverTick;
        return ticker;
    }
}
