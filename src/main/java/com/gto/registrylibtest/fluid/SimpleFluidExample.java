package com.gto.registrylibtest.fluid;

import com.gto.registrylib.util.entry.FluidEntry;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/**
 * 最简单的流体注册：一个着色流体 + 语言。
 *
 * <p>
 * 演示 <b>Approach 2</b>：通过五参数 {@code fluid(parent, name, still, flow, factory)} 形式 获得 {@link
 * com.gto.registrylibtest.builder.ModFluidBuilder}，直接调用 {@code .langCn()}。 {@code clientExtension}
 * 已由 {@code fluid(parent, ...)} 内部自动设置，无需再次调用。
 */
public class SimpleFluidExample {

    private static final Identifier FLUID_STILL = Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_still");
    private static final Identifier FLUID_FLOW = Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_flow");

    public static final FluidEntry<BaseFlowingFluid.Flowing> ACID = RegistryLibTest.REGISTRYLIB
            .fluid(
                    RegistryLibTest.REGISTRYLIB,
                    "acid",
                    FLUID_STILL,
                    FLUID_FLOW,
                    BaseFlowingFluid.Flowing::new)
            .langCn("酸液") // Approach 2: ModFluidBuilder.langCn()
            .lang("Acid")
            // clientExtension 由 fluid(parent,...) 内部自动注册，无需重复调用
            .register();
}
