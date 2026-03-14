package com.gto.registrylibtest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TimerBlockEntity extends BlockEntity {

  public TimerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  public TimerBlockEntity(BlockPos pos, BlockState state) {
    this(RegistryLibTest.TIMER_BLOCK_ENTITY.get(), pos, state);
  }

  public int getTier() {
    if (getBlockState().getBlock() instanceof TimerBlock timerBlock) {
      return timerBlock.getTier();
    }
    return 1;
  }
}
