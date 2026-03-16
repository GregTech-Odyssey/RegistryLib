package com.gto.registrylibtest.blockentity;

import com.gto.registrylibtest.block.TimerBlock;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class TimerBlockEntity extends BlockEntity {

    private int count = 0;

    public TimerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getTier() {
        if (getBlockState().getBlock() instanceof TimerBlock timerBlock) {
            return timerBlock.getTier();
        }
        return 1;
    }

    public int getCount() {
        return count;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TimerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int offset = Math.floorMod(pos.hashCode(), 20);
        if (level.getGameTime() % 20 == offset) {
            be.count++;
            be.setChanged();
            serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("count", Codec.INT, count);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        count = input.read("count", Codec.INT).orElse(0);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
