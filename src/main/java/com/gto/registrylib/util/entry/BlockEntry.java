package com.gto.registrylib.util.entry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntry<T extends Block> extends ItemProviderEntry<Block, T> {

    public BlockEntry(ResourceKey<Block> key) {
        super(key);
    }

    public BlockState getDefaultState() {
        return value.defaultBlockState();
    }

    public boolean has(BlockState state) {
        return is(state.getBlock());
    }

    public static <T extends Block> BlockEntry<T> cast(RegistryEntry<Block, T> entry) {
        return RegistryEntry.cast(BlockEntry.class, entry);
    }
}
