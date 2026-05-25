package com.gto.registrylib.util;

import net.neoforged.api.distmarker.Dist;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class DistExecutor {

    public void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        if (dist == Environment.dist) {
            toRun.get().run();
        }
    }

}
