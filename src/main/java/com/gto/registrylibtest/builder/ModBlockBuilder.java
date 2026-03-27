package com.gto.registrylibtest.builder;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.builders.BlockBuilder;
import com.gto.registrylibtest.ModRegistryCore;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Extended {@link BlockBuilder} that exposes a {@code langCn(String)} convenience method.
 *
 * <p>
 * Returned by {@link ModRegistryCore#block(Object, String, Function)} so that every {@code
 * .block(...)} call on a {@code ModRegistryCore} instance automatically has access to {@code
 * .langCn("中文名")}.
 */
public class ModBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {

    public static <T extends Block, P> ModBlockBuilder<T, P> create(
                                                                    RegistryCore owner, P parent, String name, Function<BlockBehaviour.Properties, T> factory) {
        var builder = new ModBlockBuilder<>(owner, parent, name, factory);
        return (ModBlockBuilder<T, P>) builder.defaultBlockstate().defaultLoot().defaultLang();
    }

    protected ModBlockBuilder(
                              RegistryCore owner, P parent, String name, Function<BlockBehaviour.Properties, T> factory) {
        super(owner, parent, name, factory);
    }

    /**
     * Adds a Simplified-Chinese translation to {@code zh_cn.json}. Sugar for {@code
     * lang(ModRegistryCore.LANG_ZH_CN, name)}.
     */
    public ModBlockBuilder<T, P> langCn(@NotNull String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
