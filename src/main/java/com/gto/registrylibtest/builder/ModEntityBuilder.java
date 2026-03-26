package com.gto.registrylibtest.builder;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.builders.EntityBuilder;
import com.gto.registrylibtest.ModRegistryCore;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import org.jetbrains.annotations.NotNull;

/** Extended {@link EntityBuilder} that exposes a {@code langCn(String)} convenience method. */
public class ModEntityBuilder<T extends Entity, P> extends EntityBuilder<T, P> {

    public static <T extends Entity, P> ModEntityBuilder<T, P> create(
                                                                      RegistryCore owner,
                                                                      P parent,
                                                                      String name,
                                                                      EntityType.EntityFactory<T> factory,
                                                                      MobCategory category) {
        var builder = new ModEntityBuilder<>(owner, parent, name, factory, category);
        return (ModEntityBuilder<T, P>) builder.defaultLang();
    }

    protected ModEntityBuilder(
                               RegistryCore core,
                               P parent,
                               String name,
                               EntityType.EntityFactory<T> factory,
                               MobCategory category) {
        super(core, parent, name, factory, category);
    }

    public ModEntityBuilder<T, P> langCn(@NotNull String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
