package com.gto.registrylib.util.entry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntry<T extends Block> extends ItemProviderEntry<Block, T> {

    public BlockEntry(ResourceKey<Block> key) {
        super(key);
    }

    public static <T extends Block> BlockEntry<T> cast(RegistryEntry<Block, T> entry) {
        return RegistryEntry.cast(BlockEntry.class, entry);
    }

    @Override
    protected Holder<Block> delegate() {
        return value.builtInRegistryHolder;
    }

    public BlockState getDefaultState() {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return value.defaultBlockState();
    }

    public boolean is(BlockState state) {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return value == state.getBlock();
    }
}
