package com.gto.registrylibtest;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.builders.BlockBuilder;
import com.gto.registrylib.builders.BuilderCallback;
import com.gto.registrylib.builders.FluidBuilder;
import com.gto.registrylib.builders.ItemBuilder;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylibtest.builder.ModBlockBuilder;
import com.gto.registrylibtest.builder.ModFluidBuilder;
import com.gto.registrylibtest.builder.ModItemBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * A subclass of {@link RegistryCore} that adds first-class Simplified-Chinese lang support.
 *
 * <p>Key differences from {@link RegistryCore}:
 * <ul>
 *   <li>Declares the shared {@link #LANG_ZH_CN} {@link ProviderType} and the
 *       {@link ZhCnLangProvider} that backs it.
 *   <li>Overrides {@link #newBlockBuilder}, {@link #newItemBuilder}, and
 *       {@link #newFluidBuilder} to return {@link ModBlockBuilder}, {@link ModItemBuilder},
 *       and {@link ModFluidBuilder} respectively — each of which carries a
 *       {@code .langCn(String)} convenience method.
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * public static final ModRegistryCore REGISTRYLIB = ModRegistryCore.create(MOD_ID);
 *
 * public static final BlockEntry<Block> MAGIC_ORE = REGISTRYLIB
 *         .block("magic_ore", Block::new)
 *         .lang("Magic Ore")
 *         .langCn("魔法矿石")
 *         .register();
 * }</pre>
 */
public class ModRegistryCore extends RegistryCore {

    /**
     * Shared Simplified-Chinese lang {@link ProviderType}.
     * Registered once per JVM; drives the generation of {@code zh_cn.json}.
     */
    public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
            ProviderType.registerClientProvider(
                    "lang_zh_cn", () -> c -> new ZhCnLangProvider(c.parent(), c.output()));

    // ── Construction ────────────────────────────────────────────────────────

    protected ModRegistryCore(String modid) {
        super(modid);
    }

    /**
     * Creates a {@code ModRegistryCore} instance for the given mod id, registers
     * all event listeners, and returns it.  Drop-in replacement for
     * {@link RegistryCore#create(String)}.
     */
    public static ModRegistryCore create(String modid) {
        var ret = new ModRegistryCore(modid);
        ModList.get().getModContainerById(modid)
                .ifPresent(c -> ret.registerEventListeners(c.getEventBus()));
        return ret;
    }

    // ── Covariant public API overrides ──────────────────────────────────────
    // Java's generic invariance prevents overriding no-parent convenience methods
    // (e.g. block(String, factory) which returns BlockBuilder<T, RegistryCore>)
    // with a covariant Mod*Builder<T, ModRegistryCore> return type.
    //
    // The two-arg forms (parent, name, factory) CAN be overridden because the
    // generic "P" is the SAME type variable in both parent and override, making
    // ModBlockBuilder<T, P> a valid covariant subtype of BlockBuilder<T, P>.
    // At runtime the cast is safe because newXxxBuilder() is always overridden
    // to produce the corresponding Mod*Builder.

    @Override
    public <T extends Block, P> ModBlockBuilder<T, P> block(
            @Nonnull P parent, @Nonnull String name,
            @Nonnull Function<BlockBehaviour.Properties, T> factory) {
        return (ModBlockBuilder<T, P>) super.block(parent, name, factory);
    }

    @Override
    public <T extends Item, P> ModItemBuilder<T, P> item(
            @Nonnull P parent, @Nonnull String name,
            @Nonnull Function<Item.Properties, T> factory) {
        return (ModItemBuilder<T, P>) super.item(parent, name, factory);
    }

    @Override
    public <T extends BaseFlowingFluid, P> ModFluidBuilder<T, P> fluid(
            @Nonnull P parent, @Nonnull String name,
            @Nonnull Identifier stillTexture, @Nonnull Identifier flowingTexture,
            @Nonnull FluidBuilder.FluidFactory<T> fluidFactory) {
        return (ModFluidBuilder<T, P>)
                super.fluid(parent, name, stillTexture, flowingTexture, fluidFactory);
    }

    // ── Builder hooks ────────────────────────────────────────────────────────

    @Override
    protected <T extends Block, P> BlockBuilder<T, P> newBlockBuilder(
            @Nonnull P parent, @Nonnull String name, @Nonnull BuilderCallback callback,
            @Nonnull Function<BlockBehaviour.Properties, T> factory) {
        return ModBlockBuilder.create(this, parent, name, callback, factory);
    }

    @Override
    protected <T extends Item, P> ItemBuilder<T, P> newItemBuilder(
            @Nonnull P parent, @Nonnull String name, @Nonnull BuilderCallback callback,
            @Nonnull Function<Item.Properties, T> factory) {
        return ModItemBuilder.create(this, parent, name, callback, factory);
    }

    @Override
    protected <T extends BaseFlowingFluid, P> FluidBuilder<T, P> newFluidBuilder(
            @Nonnull P parent, @Nonnull String name, @Nonnull BuilderCallback callback,
            @Nonnull FluidBuilder.FluidFactory<T> fluidFactory) {
        return ModFluidBuilder.create(this, parent, name, callback, fluidFactory);
    }
}
