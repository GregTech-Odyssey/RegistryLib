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
 * 祭坛方块：检测上方掉落物并按配方转化。
 *
 * <p>
 * Altar block: detects item entities above and transforms them according to altar recipes.
 */
public class AltarBlock extends Block implements EntityBlock {

    public AltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(SimpleRecipeExample.ALTAR_BE.get(), pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
                                                                            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (BlockEntityTicker<AltarBlockEntity>) AltarBlockEntity::serverTick;
        return ticker;
    }
}
