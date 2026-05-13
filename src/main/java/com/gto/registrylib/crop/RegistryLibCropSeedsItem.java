package com.gto.registrylib.crop;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public final class RegistryLibCropSeedsItem extends BlockItem {

    private final Supplier<? extends Block> cropBlock;

    public RegistryLibCropSeedsItem(Supplier<? extends Block> cropBlock, Item.Properties properties) {
        super(null, properties);
        this.cropBlock = cropBlock;
    }

    @Override
    public Block getBlock() {
        return cropBlock.get();
    }
}
