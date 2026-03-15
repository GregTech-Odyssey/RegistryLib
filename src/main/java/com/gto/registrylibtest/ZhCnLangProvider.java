package com.gto.registrylibtest;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylibtest.ModRegistryCore;

import net.minecraft.data.PackOutput;

/**
 * Simplified-Chinese lang provider for the test mod.
 *
 * <p>Extends {@link RegistryLibLangProvider} with locale {@code zh_cn} and ties itself
 * to {@link ModRegistryCore#LANG_ZH_CN} so that every builder callback registered
 * against that {@code ProviderType} ends up written into {@code zh_cn.json}.
 */
public class ZhCnLangProvider extends RegistryLibLangProvider {

    public ZhCnLangProvider(RegistryCore owner, PackOutput packOutput) {
        super(owner, packOutput, "zh_cn");
    }

    @Override
    protected ProviderType<? extends RegistryLibLangProvider> getProviderType() {
        return ModRegistryCore.LANG_ZH_CN;
    }
}
