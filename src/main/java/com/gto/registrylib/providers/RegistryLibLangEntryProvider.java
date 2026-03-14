package com.gto.registrylib.providers;

import javax.annotation.Nullable;

public interface RegistryLibLangEntryProvider extends RegistryLibProvider {

    void add(@Nullable String key, @Nullable String value);
}
