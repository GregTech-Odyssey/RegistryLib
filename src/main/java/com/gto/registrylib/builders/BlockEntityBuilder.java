package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryLib;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.util.OneTimeEventReceiver;
import com.gto.registrylib.util.RegistryLibDistExecutor;
import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BlockEntityBuilder<BE extends BlockEntity, P>
    extends AbstractBuilder<BlockEntityType<?>, BlockEntityType<BE>, P, BlockEntityBuilder<BE, P>> {

  @FunctionalInterface
  public interface BlockEntityFactory<T extends BlockEntity> {
    T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
  }

  public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      BlockEntityFactory<T> factory) {
    return new BlockEntityBuilder<>(owner, parent, name, callback, factory);
  }

  private final BlockEntityFactory<BE> factory;
  private final Set<Supplier<? extends Block>> validBlocks = new HashSet<>();
  @Nullable private Supplier<? extends BlockEntityRendererProvider<?, ?>> renderer;

  protected BlockEntityBuilder(
      RegistryLib owner,
      P parent,
      String name,
      BuilderCallback callback,
      BlockEntityFactory<BE> factory) {
    super(owner, parent, name, callback, Registries.BLOCK_ENTITY_TYPE);
    this.factory = factory;
  }

  // === Configuration ===

  @StandardAPI
  public BlockEntityBuilder<BE, P> validBlock(@Nonnull Supplier<? extends Block> block) {
    validBlocks.add(block);
    return this;
  }

  @SafeVarargs
  @StandardAPI
  public final BlockEntityBuilder<BE, P> validBlocks(@Nonnull Supplier<? extends Block>... blocks) {
    Arrays.stream(blocks).forEach(this::validBlock);
    return this;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @StandardAPI
  public <BERS extends BlockEntityRenderState> BlockEntityBuilder<BE, P> renderer(
      @Nonnull Supplier<BlockEntityRendererProvider<? super BE, BERS>> renderer) {
    if (this.renderer == null) {
      RegistryLibDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerRenderer);
    }
    this.renderer = (Supplier) renderer;
    return this;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void registerRenderer() {
    OneTimeEventReceiver.addModListener(
        getOwner(),
        FMLClientSetupEvent.class,
        $ -> {
          var r = this.renderer;
          if (r != null) {
            BlockEntityRenderers.register(getEntry(), (BlockEntityRendererProvider) r.get());
          }
        });
  }

  @Override
  protected BlockEntityType<BE> createEntry() {
    Block[] blocks = validBlocks.stream().map(Supplier::get).toArray(Block[]::new);
    Supplier<BlockEntityType<BE>> supplier = asSupplier();
    return new BlockEntityType<>(
        (pos, state) -> factory.create(supplier.get(), pos, state), blocks);
  }

  @Override
  protected RegistryEntry<BlockEntityType<?>, BlockEntityType<BE>> createEntryWrapper(
      DeferredHolder<BlockEntityType<?>, BlockEntityType<BE>> delegate) {
    return new BlockEntityEntry<>(getOwner(), delegate);
  }
}
