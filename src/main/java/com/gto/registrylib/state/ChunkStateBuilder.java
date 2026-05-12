package com.gto.registrylib.state;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.util.entry.AttachmentTypeEntry;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class ChunkStateBuilder<T, P> {

    private final RegistryCore core;
    private final P parent;
    private final String name;
    private final Codec<T> codec;
    private final Function<IAttachmentHolder, T> defaultValueFactory;
    private StateDebugConfig debugConfig = StateDebugConfig.disabled();
    private boolean syncOnModify;
    private StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec;
    private boolean registered;

    public ChunkStateBuilder(
                             RegistryCore core,
                             P parent,
                             String name,
                             Codec<T> codec,
                             Function<IAttachmentHolder, T> defaultValueFactory) {
        this.core = core;
        this.parent = parent;
        this.name = name;
        this.codec = codec;
        this.defaultValueFactory = defaultValueFactory;
    }

    public static <T, P> ChunkStateBuilder<T, P> create(
                                                        RegistryCore core,
                                                        P parent,
                                                        String name,
                                                        Codec<T> codec,
                                                        Supplier<T> defaultValueFactory) {
        return new ChunkStateBuilder<>(core, parent, name, codec, _holder -> defaultValueFactory.get());
    }

    @StandardAPI
    public ChunkStateBuilder<T, P> syncOnModify(boolean syncOnModify) {
        this.syncOnModify = syncOnModify;
        return this;
    }

    @StandardAPI
    public ChunkStateBuilder<T, P> sync(StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec) {
        this.syncCodec = syncCodec;
        this.syncOnModify = true;
        return this;
    }

    @StandardAPI
    public ChunkStateBuilder<T, P> debug() {
        this.debugConfig = StateDebugConfig.createEnabled();
        return this;
    }

    @StandardAPI
    public ChunkStateBuilder<T, P> debug(UnaryOperator<StateDebugConfig> config) {
        this.debugConfig = StateDebugConfig.createEnabled().configure(config);
        return this;
    }

    @StandardAPI
    public ChunkStateEntry<T> register() {
        if (registered) {
            throw new IllegalStateException("Builder already registered: " + name);
        }
        registered = true;
        var attachmentBuilder = core
                .attachmentType("chunk_state/" + name, defaultValueFactory)
                .serialize(codec.fieldOf("value"));
        if (syncCodec != null) {
            attachmentBuilder.sync(syncCodec);
        }
        AttachmentTypeEntry<T> attachment = attachmentBuilder.register();
        ChunkStateEntry<T> entry = new ChunkStateEntry<>(
                Identifier.fromNamespaceAndPath(core.getModid(), name),
                codec,
                attachment,
                debugConfig,
                syncOnModify && syncCodec != null);
        core.registerStateEntry(entry);
        return entry;
    }

    @StandardAPI
    public P build() {
        register();
        return parent;
    }
}
