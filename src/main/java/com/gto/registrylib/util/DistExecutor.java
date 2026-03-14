package com.gto.registrylib.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class DistExecutor {

    public void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        if (dist == FMLEnvironment.getDist()) {
            toRun.get().run();
        }
    }
}
