package com.gto.registrylib.datagen.provider;

import net.minecraft.data.DataProvider;
import net.neoforged.fml.LogicalSide;

public interface RegistryLibProvider extends DataProvider {

    LogicalSide getSide();
}
