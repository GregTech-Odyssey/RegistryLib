package com.gto.registrylib;

import com.gto.registrylib.client.Client;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RegistryLib.MOD_ID)
public final class RegistryLib {

    public static final String MOD_ID = "registrylib";

    public static final Logger LOGGER = LogManager.getLogger();

    public RegistryLib(IEventBus modEventBus) {
        modEventBus.addListener(RegistryCore::onRegister);
        modEventBus.addListener(EventPriority.LOWEST, RegistryCore::onRegisterLate);
        modEventBus.addListener(RegistryCore::onBuildCreativeModeTabContents);
        if (FMLEnvironment.getDist().isClient()) {
            Client.init(modEventBus);
        }
    }
}
