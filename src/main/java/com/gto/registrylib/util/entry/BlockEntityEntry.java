package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;

import javax.annotation.Nullable;

public class BlockEntityEntry<T extends BlockEntity>
                             extends RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> {

    public BlockEntityEntry(
                            RegistryCore owner, DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> delegate) {
        super(owner, delegate);
    }

    public T create(BlockPos pos, BlockState state) {
        return value().create(pos, state);
    }

    public boolean is(@Nullable BlockEntity t) {
        return t != null && t.getType() == value();
    }

    public Optional<T> get(BlockGetter world, BlockPos pos) {
        return Optional.ofNullable(getNullable(world, pos));
    }

    @SuppressWarnings("unchecked")
    public @Nullable T getNullable(BlockGetter world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return is(be) ? (T) be : null;
    }

    public static <T extends BlockEntity> BlockEntityEntry<T> cast(
                                                                   RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> entry) {
        return RegistryEntry.cast(BlockEntityEntry.class, entry);
    }
}
