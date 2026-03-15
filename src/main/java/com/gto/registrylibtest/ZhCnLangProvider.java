package com.gto.registrylibtest;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.providers.RegistryLibLangProvider;

import net.minecraft.data.PackOutput;

/**
 * A simple Simplified-Chinese lang provider for the test mod.
 *
 * <p>Extend {@link RegistryLibLangProvider} with locale {@code zh_cn}.
 * Register it as a new {@code ProviderType} in {@link RegistryLibTest#LANG_ZH_CN}
 * and call {@code .lang(RegistryLibTest.LANG_ZH_CN, "中文名")} on any builder.
 */
public class ZhCnLangProvider extends RegistryLibLangProvider {

    public ZhCnLangProvider(RegistryCore owner, PackOutput packOutput) {
        super(owner, packOutput, "zh_cn");
    }
}
