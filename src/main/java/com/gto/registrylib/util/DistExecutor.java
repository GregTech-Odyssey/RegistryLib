package com.gto.registrylib.util;

import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

@UtilityClass
public class DistExecutor {

  public void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
    if (dist == FMLEnvironment.getDist()) {
      toRun.get().run();
    }
  }
}
