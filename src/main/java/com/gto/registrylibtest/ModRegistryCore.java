package com.gto.registrylibtest;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.builders.FluidBuilder;
import com.gto.registrylib.composite.ComponentItem;
import com.gto.registrylib.composite.IComponentItem;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylibtest.builder.ModBlockBuilder;
import com.gto.registrylibtest.builder.ModFluidBuilder;
import com.gto.registrylibtest.builder.ModItemBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * A subclass of {@link RegistryCore} that adds first-class Simplified-Chinese lang support.
 *
 * <p>
 * Key differences from {@link RegistryCore}:
 *
 * <ul>
 * <li>Declares the shared {@link #LANG_ZH_CN} {@link ProviderType} and the {@link
 * ZhCnLangProvider} that backs it.
 * <li>Overrides {@link #newBlockBuilder}, {@link #newItemBuilder}, and {@link #newFluidBuilder}
 * to return {@link ModBlockBuilder}, {@link ModItemBuilder}, and {@link ModFluidBuilder}
 * respectively — each of which carries a {@code .langCn(String)} convenience method.
 * </ul>
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * 
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
     * Shared Simplified-Chinese lang {@link ProviderType}. Registered once per JVM; drives the
     * generation of {@code zh_cn.json}.
     */
    public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN = ProviderType.registerClientProvider(
            "lang_zh_cn", () -> c -> new ZhCnLangProvider(c.parent(), c.output()));

    // ── Construction ────────────────────────────────────────────────────────

    protected ModRegistryCore(String modid) {
        super(modid);
    }

    /**
     * Creates a {@code ModRegistryCore} instance for the given mod id, registers all event listeners,
     * and returns it. Drop-in replacement for {@link RegistryCore#create(String)}.
     */
    public static ModRegistryCore create(String modid) {
        return new ModRegistryCore(modid);
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
                                                            @Nonnull P parent,
                                                            @Nonnull String name,
                                                            @Nonnull Function<BlockBehaviour.Properties, T> factory) {
        return ModBlockBuilder.create(this, parent, name, factory);
    }

    @Override
    public <T extends Item> ModItemBuilder<T, RegistryCore> item(
                                                                 @Nonnull String name, @Nonnull Function<Item.Properties, T> factory) {
        return item(this, name, factory, false);
    }

    @Override
    public ModItemBuilder<Item, RegistryCore> item(@Nonnull String name) {
        return item(this, name, Item::new, false);
    }

    @Override
    public <T extends Item & IComponentItem<T>> ModItemBuilder<T, RegistryCore> componentItem(
                                                                                              @Nonnull String name, @Nonnull Function<Item.Properties, T> factory) {
        return item(this, name, factory, true);
    }

    @Override
    public ModItemBuilder<ComponentItem, RegistryCore> componentItem(@Nonnull String name) {
        return componentItem(name, ComponentItem::new);
    }

    @Override
    public <T extends Item, P> ModItemBuilder<T, P> item(
                                                         @Nonnull P parent,
                                                         @Nonnull String name,
                                                         @Nonnull Function<Item.Properties, T> factory,
                                                         boolean isComponentItem) {
        return ModItemBuilder.create(this, parent, name, factory, isComponentItem);
    }

    @Override
    public <T extends BaseFlowingFluid, P> ModFluidBuilder<T, P> fluid(
                                                                       @Nonnull P parent,
                                                                       @Nonnull String name,
                                                                       @Nonnull Identifier stillTexture,
                                                                       @Nonnull Identifier flowingTexture,
                                                                       @Nonnull FluidBuilder.FluidFactory<T> fluidFactory) {
        return (ModFluidBuilder<T, P>) super.fluid(parent, name, stillTexture, flowingTexture, fluidFactory);
    }

    // ── Builder hooks ────────────────────────────────────────────────────────

    @Override
    protected <T extends BaseFlowingFluid, P> FluidBuilder<T, P> newFluidBuilder(
                                                                                 @Nonnull P parent, @Nonnull String name, @Nonnull FluidBuilder.FluidFactory<T> fluidFactory) {
        return ModFluidBuilder.create(this, parent, name, fluidFactory);
    }
}
