package com.gto.registrylibtest.state;

import com.gto.registrylib.state.ChunkStateEntry;
import com.gto.registrylib.state.WorldStateEntry;
import com.gto.registrylibtest.RegistryLibTest;

import com.mojang.serialization.Codec;

public final class SimpleStateExample {

    public static final ChunkStateEntry<Integer> AMBIENT_ESSENCE = RegistryLibTest.REGISTRYLIB
            .chunkState("ambient_essence", Codec.INT, () -> 0)
            .debug(config -> config.writable(true).writePermission(4))
            .register();

    public static final WorldStateEntry<Integer> ESSENCE_EPOCH = RegistryLibTest.REGISTRYLIB
            .worldState("essence_epoch", Codec.INT, () -> 0)
            .debug(config -> config.writable(true).writePermission(4))
            .register();

    public static final WorldStateEntry<Integer> AMBIENT_ESSENCE_WORLD = RegistryLibTest.REGISTRYLIB
            .worldState("ambient_essence_world", Codec.INT, () -> 100)
            .debug()
            .register();

    private SimpleStateExample() {}
}
