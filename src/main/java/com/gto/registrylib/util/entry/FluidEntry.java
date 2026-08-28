package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import org.jetbrains.annotations.Nullable;

public class FluidEntry<T extends BaseFlowingFluid> extends AbstractHolderEntry<Fluid, T> {

    private final @Nullable BlockEntry<? extends Block> block;

    private FluidStack readOnlyStack;

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
    protected Holder<Fluid> delegate() {
        return value.builtInRegistryHolder;
    }

    @SuppressWarnings("unchecked")
    public <S extends BaseFlowingFluid> S getSource() {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return (S) value.getSource();
    }

    public FluidType getType() {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return value.getFluidType();
    }

    @SuppressWarnings("unchecked")
    public <B extends Block> B getBlock() {
        if (block == null) return null;
        return (B) block.value;
    }

    @SuppressWarnings("unchecked")
    public <I extends Item> I getBucket() {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return (I) value.getBucket();
    }

    public FluidStack readOnlyStack() {
        var stack = readOnlyStack;
        if (stack == null || stack.getAmount() < 1) {
            readOnlyStack = stack = asStack();
        }
        return stack;
    }

    public FluidStack asStack() {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return new FluidStack(value.builtInRegistryHolder, 1000);
    }

    public FluidStack asStack(int amount) {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return new FluidStack(value.builtInRegistryHolder, amount);
    }

    public FluidStack asStack(int amount, DataComponentPatch components) {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return new FluidStack(value.builtInRegistryHolder, amount, components);
    }

    public boolean is(FluidStack stack) {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return value.isSame(stack.getFluid());
    }

    public boolean is(Fluid fluid) {
        if (value == null) {
            throw new IllegalStateException("Registry entry '" + key + "' has not been bound yet.");
        }
        return value.isSame(fluid);
    }
}
