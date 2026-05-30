package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.client.Client;
import com.gto.registrylib.util.DistExecutor;
import com.gto.registrylib.util.entry.BlockEntityTypeEntry;
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

    /**
     * Binds a client-side {@link BlockEntityRendererProvider} to this block entity type.
     *
     * <p>
     * The renderer is supplied through <b>two</b> lambda levels
     * ({@code Supplier<Supplier<...>>}) on purpose: the outer {@code Supplier} returns a plain
     * {@code Supplier} (a non-client type), so creating it at the call site never makes the JVM
     * resolve {@link BlockEntityRendererProvider}. The client provider type only appears inside the
     * <i>inner</i> lambda, whose {@code invokedynamic} is linked exclusively on the client (it lives
     * in the outer lambda's synthetic body, which only runs under {@link Dist#CLIENT}).
     *
     * <p>
     * A single-level {@code Supplier<BlockEntityRendererProvider>} would crash a dedicated server:
     * the JVM resolves a lambda's instantiated return type when the lambda is <i>created</i>, so
     * {@code () -> MyRenderer::new} would force {@code BlockEntityRendererProvider} to load on the
     * server even though the body never executes. Call this as
     * {@code .renderer(() -> () -> MyRenderer::new)}.
     */
    @StandardAPI
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BlockEntityBuilder<BE, P> renderer(
                                              @NotNull Supplier<Supplier<? extends BlockEntityRendererProvider>> renderer) {
        Supplier supplier = getValueSupplier();
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> Client.registerBER(supplier, renderer.get().get()));
        return this;
    }

    @Override
    protected BlockEntityType<BE> createEntry(ResourceKey<BlockEntityType<?>> key) {
        Block[] blocks = validBlocks.stream().map(Supplier::get).toArray(Block[]::new);
        var supplier = getValueSupplier();
        return new BlockEntityType<>(
                (pos, state) -> factory.create(supplier.get(), pos, state), blocks);
    }

    @Override
    protected RegistryEntry<BlockEntityType<?>, BlockEntityType<BE>> createEntryWrapper(
                                                                                        ResourceKey<BlockEntityType<?>> key) {
        return new BlockEntityTypeEntry<>(key);
    }

    @Override
    public BlockEntityTypeEntry<BE> register() {
        return (BlockEntityTypeEntry<BE>) super.register();
    }
}
