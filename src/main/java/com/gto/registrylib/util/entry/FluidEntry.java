package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

import org.jetbrains.annotations.Nullable;

public class FluidEntry<T extends BaseFlowingFluid> extends RegistryEntry<Fluid, T> {

    private final @Nullable BlockEntry<? extends Block> block;

    public FluidEntry(RegistryCore owner, ResourceKey<Fluid> key) {
        super(key);
        BlockEntry<? extends Block> block = null;
        try {
            block = BlockEntry.cast(getSibling(owner, BuiltInRegistries.BLOCK));
        } catch (IllegalArgumentException e) {
            // No block sibling
        }
        this.block = block;
    }

    @Override
    public <R> boolean is(R entry) {
        return value.isSame((Fluid) entry);
    }

    @SuppressWarnings("unchecked")
    public <S extends BaseFlowingFluid> S getSource() {
        return (S) value.getSource();
    }

    public FluidType getType() {
        return value.getFluidType();
    }

    @SuppressWarnings("unchecked")
    public <B extends Block> B getBlock() {
        if (block == null) return null;
        return (B) block.value;
    }

    @SuppressWarnings({ "unchecked", "null" })
    public <I extends Item> I getBucket() {
        return (I) value.getBucket();
    }
}
