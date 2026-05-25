package com.gto.registrylib.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Environment {

    public static final Dist dist = FMLEnvironment.getDist();
    public static final boolean isClient = dist.isClient();
    public static final boolean isServer = !isClient;

    public static final boolean isProd = FMLEnvironment.isProduction();
    public static final boolean isDev = !isProd;
    public static final boolean isDatagen = DatagenModLoader.isRunningDataGen();

    public static boolean isProdStatic() {
        return isProd;
    }

    public static boolean isDatagenStatic() {
        return isDatagen;
    }
}
