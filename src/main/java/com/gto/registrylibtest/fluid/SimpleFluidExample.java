package com.gto.registrylibtest.fluid;

import com.gto.registrylib.util.entry.FluidEntry;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** 最简单的流体注册：一个着色流体 + 语言。 */
public class SimpleFluidExample {

    private static final Identifier FLUID_STILL =
            Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_still");
    private static final Identifier FLUID_FLOW =
            Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_flow");

    public static final FluidEntry<BaseFlowingFluid.Flowing> ACID =
            RegistryLibTest.REGISTRYLIB.fluid(
                    "acid",
                    FLUID_STILL,
                    FLUID_FLOW,
                    fluid -> {
                        fluid.lang("Acid")
                                .clientExtension(FLUID_STILL, FLUID_FLOW);
                    });
}
