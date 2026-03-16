package com.gto.registrylibtest.builder;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.builders.FluidBuilder;
import com.gto.registrylibtest.ModRegistryCore;

import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nonnull;

/**
 * Extended {@link FluidBuilder} that exposes a {@code langCn(String)} convenience method.
 *
 * <p>
 * Returned by {@link ModRegistryCore#newFluidBuilder} so that every {@code .fluid(...)} call on
 * a {@code ModRegistryCore} instance automatically has access to {@code .langCn("中文名")}.
 */
public class ModFluidBuilder<T extends BaseFlowingFluid, P> extends FluidBuilder<T, P> {

    public static <T extends BaseFlowingFluid, P> ModFluidBuilder<T, P> create(
                                                                               RegistryCore owner, P parent, String name, FluidFactory<T> fluidFactory) {
        var builder = new ModFluidBuilder<>(owner, parent, name, FluidType::new, fluidFactory);
        return (ModFluidBuilder<T, P>) builder.defaultLang().defaultSource().defaultBlock().defaultBucket();
    }

    protected ModFluidBuilder(
                              RegistryCore owner,
                              P parent,
                              String name,
                              FluidTypeFactory typeFactory,
                              FluidFactory<T> fluidFactory) {
        super(owner, parent, name, typeFactory, fluidFactory);
    }

    /**
     * Adds a Simplified-Chinese translation for the fluid type to {@code zh_cn.json}. Sugar for
     * {@code lang(ModRegistryCore.LANG_ZH_CN, name)}.
     */
    public ModFluidBuilder<T, P> langCn(@Nonnull String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
