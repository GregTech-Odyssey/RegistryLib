package com.gto.registrylib.state;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.util.entry.AttachmentTypeEntry;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.function.Function;
import java.util.function.Supplier;

public final class WorldStateBuilder<T, P> extends AbstractStateBuilder<T, P, WorldStateBuilder<T, P>> {

    public WorldStateBuilder(
                             RegistryCore core,
                             P parent,
                             String name,
                             Codec<T> codec,
                             Function<IAttachmentHolder, T> defaultValueFactory) {
        super(core, parent, name, codec, defaultValueFactory);
    }

    public static <T, P> WorldStateBuilder<T, P> create(
                                                        RegistryCore core,
                                                        P parent,
                                                        String name,
                                                        Codec<T> codec,
                                                        Supplier<T> defaultValueFactory) {
        return new WorldStateBuilder<>(core, parent, name, codec, _holder -> defaultValueFactory.get());
    }

    @Override
    protected String attachmentPrefix() {
        return "world_state/";
    }

    @Override
    protected AbstractStateEntry<T> createEntry(
                                                Identifier identifier,
                                                Codec<T> codec,
                                                AttachmentTypeEntry<T> attachment,
                                                StateDebugConfig debugConfig,
                                                boolean syncOnModify) {
        return new WorldStateEntry<>(identifier, codec, attachment, debugConfig, syncOnModify);
    }

    @Override
    @StandardAPI
    public WorldStateEntry<T> register() {
        return (WorldStateEntry<T>) super.register();
    }
}
