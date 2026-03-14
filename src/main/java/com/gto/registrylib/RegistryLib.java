package com.gto.registrylib;

import com.gto.registrylib.client.Client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RegistryLib.MOD_ID)
public final class RegistryLib {

    public static final String MOD_ID = "registrylib";

    public static final Logger LOGGER = LogManager.getLogger();

    public RegistryLib(IEventBus modEventBus, Dist dist, ModContainer container) {
        if (dist.isClient()) Client.init(modEventBus, container);
    }
}