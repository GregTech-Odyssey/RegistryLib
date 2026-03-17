package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.client.Client;
import com.gto.registrylib.util.DistExecutor;
import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class BlockEntityBuilder<BE extends BlockEntity, P>
                               extends AbstractBuilder<BlockEntityType<?>, BlockEntityType<BE>, P, BlockEntityBuilder<BE, P>> {

    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {

        T create(BlockEntityType<?> type, BlockPos pos, BlockState state);
    }

    public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(
                                                                             RegistryCore owner, P parent, String name, BlockEntityFactory<T> factory) {
        return new BlockEntityBuilder<>(owner, parent, name, factory);
    }

    private final BlockEntityFactory<BE> factory;
    private final Set<Supplier<? extends Block>> validBlocks = new ReferenceOpenHashSet<>();

    protected BlockEntityBuilder(
                                 RegistryCore core, P parent, String name, BlockEntityFactory<BE> factory) {
        super(core, parent, name, Registries.BLOCK_ENTITY_TYPE);
        this.factory = factory;
    }

    // === Configuration ===

    @StandardAPI
    public BlockEntityBuilder<BE, P> validBlock(@NotNull Supplier<? extends Block> block) {
        validBlocks.add(block);
        return this;
    }

    @SafeVarargs
    @StandardAPI
    public final BlockEntityBuilder<BE, P> validBlocks(@NotNull Supplier<? extends Block>... blocks) {
        Collections.addAll(validBlocks, blocks);
        return this;
    }

    @StandardAPI
    @SuppressWarnings("rawtypes")
    public BlockEntityBuilder<BE, P> renderer(
                                              @Nonnull Supplier<? extends BlockEntityRendererProvider> renderer) {
        Supplier supplier = valueSupplier;
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> Client.registerBER(supplier, renderer.get()));
        return this;
    }

    @Override
    protected BlockEntityType<BE> createEntry(ResourceKey<BlockEntityType<?>> key) {
        Block[] blocks = validBlocks.stream().map(Supplier::get).toArray(Block[]::new);
        var supplier = valueSupplier;
        return new BlockEntityType<>(
                (pos, state) -> factory.create(supplier.get(), pos, state), blocks);
    }

    @Override
    protected RegistryEntry<BlockEntityType<?>, BlockEntityType<BE>> createEntryWrapper(
                                                                                        ResourceKey<BlockEntityType<?>> key) {
        return new BlockEntityEntry<>(key);
    }

    @Override
    public BlockEntityEntry<BE> register() {
        return (BlockEntityEntry<BE>) super.register();
    }
}
