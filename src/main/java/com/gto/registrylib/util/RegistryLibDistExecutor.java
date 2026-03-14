package com.gto.registrylib.util;

import java.util.function.Supplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class RegistryLibDistExecutor {
  public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
    if (dist == FMLEnvironment.getDist()) {
      toRun.get().run();
    }
  }
}
