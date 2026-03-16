package com.gto.registrylib.util.entry;

import com.gto.registrylib.RegistryCore;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FluidEntry<T extends BaseFlowingFluid> extends RegistryEntry<Fluid, T>
                       implements Holder<Fluid> {

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

    public FluidResource asResource() {
        return FluidResource.of(value);
    }

    public FluidResource asResource(DataComponentPatch components) {
        return FluidResource.of(value, components);
    }

    public FluidStack readOnlyStack() {
        if (readOnlyStack == null) {
            readOnlyStack = asStack();
        }
        return readOnlyStack;
    }

    public FluidStack asStack() {
        return new FluidStack(value.builtInRegistryHolder, 1000);
    }

    public FluidStack asStack(int amount) {
        return new FluidStack(value.builtInRegistryHolder, amount);
    }

    public FluidStack asStack(int amount, DataComponentPatch components) {
        return new FluidStack(value.builtInRegistryHolder, amount, components);
    }

    public boolean is(FluidStack stack) {
        return value.isSame(stack.getFluid());
    }

    public boolean is(Fluid fluid) {
        return value.isSame(fluid);
    }

    @Override
    public final Fluid value() {
        return value;
    }

    @Override
    public final boolean isBound() {
        return value != null && value.builtInRegistryHolder.isBound();
    }

    @Override
    public final boolean areComponentsBound() {
        return value != null && value.builtInRegistryHolder.areComponentsBound();
    }

    @Override
    public final boolean is(Holder<Fluid> holder) {
        return value == holder.value();
    }

    @Override
    public final boolean is(Identifier key) {
        return this.key.identifier().equals(key);
    }

    @Override
    public final boolean is(ResourceKey<Fluid> key) {
        return this.key == key;
    }

    @Override
    public final boolean is(Predicate<ResourceKey<Fluid>> predicate) {
        return predicate.test(key);
    }

    @Override
    public final boolean is(TagKey<Fluid> tag) {
        return value != null && value.builtInRegistryHolder.is(tag);
    }

    @Override
    public final Stream<TagKey<Fluid>> tags() {
        return value != null ? value.builtInRegistryHolder.tags() : Stream.empty();
    }

    @Override
    public final DataComponentMap components() {
        return value != null ? value.builtInRegistryHolder.components() : DataComponentMap.EMPTY;
    }

    @Override
    public final Either<ResourceKey<Fluid>, Fluid> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public final Optional<ResourceKey<Fluid>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public final Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    @Override
    public final boolean canSerializeIn(HolderOwner<Fluid> registry) {
        return value != null && value.builtInRegistryHolder.canSerializeIn(registry);
    }

    @Override
    public final Holder<Fluid> getDelegate() {
        return value != null ? value.builtInRegistryHolder : this;
    }

    @Override
    public final ResourceKey<Fluid> getKey() {
        return this.key;
    }

    @Override
    public final <Z> @Nullable Z getData(DataMapType<Fluid, Z> type) {
        return value == null ? null : value.builtInRegistryHolder.getData(type);
    }
}
