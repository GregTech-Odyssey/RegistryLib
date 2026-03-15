package com.gto.registrylib.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Environment {

    public final Dist dist = FMLEnvironment.getDist();
    public final boolean isClient = dist.isClient();
    public final boolean isServer = !isClient;

    public final boolean isProd = FMLEnvironment.isProduction();
    public final boolean isDev = !isProd;
    public final boolean isDatagen = DatagenModLoader.isRunningDataGen();
}
