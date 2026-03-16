package com.gto.registrylibtest.block;

import com.gto.registrylibtest.blockentity.FullBlockEntityExample;
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

import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class TimerBlock extends Block implements EntityBlock {

    @Getter
    private final int tier;

    private final Supplier<BlockEntityType<TimerBlockEntity>> type;

    public TimerBlock(
                      BlockBehaviour.Properties properties,
                      int tier,
                      Supplier<BlockEntityType<TimerBlockEntity>> type) {
        super(properties);
        this.tier = tier;
        this.type = type;
    }

    public TimerBlock(BlockBehaviour.Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        this.type = FullBlockEntityExample.TIMER_BLOCK_ENTITY;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TimerBlockEntity(type.get(), pos, state);
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
