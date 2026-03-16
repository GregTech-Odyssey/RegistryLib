package com.gto.registrylib;

import com.gto.registrylib.client.Client;
import com.gto.registrylib.util.DistExecutor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(RegistryLib.MOD_ID)
public final class RegistryLib {

    public static final String MOD_ID = "registrylib";

    public static final Logger LOGGER = LogManager.getLogger();

    public static final Random RANDOM = new Random();

    public RegistryLib(IEventBus modEventBus) {
        modEventBus.addListener(EventPriority.LOW, RegistryCore::onRegister);
        modEventBus.addListener(EventPriority.LOWEST, RegistryCore::onRegisterLate);
        modEventBus.addListener(RegistryCore::onBuildCreativeModeTabContents);
        modEventBus.addListener(RegistryCore::onEntityAttributeCreation);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Client.init(modEventBus));
    }
}
