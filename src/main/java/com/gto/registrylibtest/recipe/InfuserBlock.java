package com.gto.registrylibtest.recipe;

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

/**
 * 注入器方块：带等级的处理机器，检测上方掉落物并按配方转化。
 *
 * <p>
 * Infuser block: a tiered processing machine. Detects item entities above and transforms them
 * according to infuser recipes. The machine tier gates which recipes can be processed.
 */
public class InfuserBlock extends Block implements EntityBlock {

    private final int tier;

    public InfuserBlock(BlockBehaviour.Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfuserBlockEntity(FullRecipeExample.INFUSER_BE.get(), pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
                                                                            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (BlockEntityTicker<InfuserBlockEntity>) InfuserBlockEntity::serverTick;
        return ticker;
    }
}
