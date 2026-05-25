package com.gto.registrylib.util.entry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BlockEntityTypeEntry<T extends BlockEntity>
                                 extends RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> {

    public BlockEntityTypeEntry(ResourceKey<BlockEntityType<?>> key) {
        super(key);
    }

    public T create(BlockPos pos, BlockState state) {
        if (value == null) throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        return value.create(pos, state);
    }

    public boolean is(@Nullable BlockEntity t) {
        if (value == null) throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        return t != null && t.getType() == value;
    }

    public Optional<T> get(BlockGetter world, BlockPos pos) {
        return Optional.ofNullable(getNullable(world, pos));
    }

    @SuppressWarnings("unchecked")
    public @Nullable T getNullable(BlockGetter world, BlockPos pos) {
        if (value == null) throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        BlockEntity be = world.getBlockEntity(pos);
        return is(be) ? (T) be : null;
    }

    public static <T extends BlockEntity> BlockEntityTypeEntry<T> cast(
                                                                       RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> entry) {
        return RegistryEntry.cast(BlockEntityTypeEntry.class, entry);
    }
}
