package com.gto.registrylibtest.builder;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.builders.BuilderCallback;
import com.gto.registrylib.builders.ItemBuilder;
import com.gto.registrylibtest.ModRegistryCore;

import net.minecraft.world.item.Item;

import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Extended {@link ItemBuilder} that exposes a {@code langCn(String)} convenience method.
 *
 * <p>
 * Returned by {@link ModRegistryCore#newItemBuilder} so that every {@code .item(...)} call on a
 * {@code ModRegistryCore} instance automatically has access to {@code .langCn("中文名")}.
 */
public class ModItemBuilder<T extends Item, P> extends ItemBuilder<T, P> {

    public static <T extends Item, P> ModItemBuilder<T, P> create(
                                                                  RegistryCore owner,
                                                                  P parent,
                                                                  String name,
                                                                  BuilderCallback callback,
                                                                  Function<Item.Properties, T> factory,
                                                                  boolean isComponentItem) {
        var builder = new ModItemBuilder<>(owner, parent, name, callback, factory, isComponentItem);
        return (ModItemBuilder<T, P>) builder.defaultModel().defaultLang();
    }

    protected ModItemBuilder(
                             RegistryCore owner,
                             P parent,
                             String name,
                             BuilderCallback callback,
                             Function<Item.Properties, T> factory,
                             boolean isComponentItem) {
        super(owner, parent, name, callback, factory, isComponentItem);
    }

    /**
     * Adds a Simplified-Chinese translation to {@code zh_cn.json}. Sugar for {@code
     * lang(ModRegistryCore.LANG_ZH_CN, name)}.
     */
    public ModItemBuilder<T, P> langCn(@Nonnull String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
