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

public final class ChunkStateBuilder<T, P> extends AbstractStateBuilder<T, P, ChunkStateBuilder<T, P>> {

    private boolean syncOnModify;

    public ChunkStateBuilder(
                             RegistryCore core,
                             P parent,
                             String name,
                             Codec<T> codec,
                             Function<IAttachmentHolder, T> defaultValueFactory) {
        super(core, parent, name, codec, defaultValueFactory);
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

    @Override
    @StandardAPI
    public ChunkStateBuilder<T, P> sync(StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec) {
        super.sync(syncCodec);
        this.syncOnModify = true;
        return self();
    }

    @Override
    protected String attachmentPrefix() {
        return "chunk_state/";
    }

    @Override
    protected AbstractStateEntry<T> createEntry(
                                                Identifier identifier,
                                                Codec<T> codec,
                                                AttachmentTypeEntry<T> attachment,
                                                StateDebugConfig debugConfig,
                                                boolean syncOnModify) {
        return new ChunkStateEntry<>(identifier, codec, attachment, debugConfig, syncOnModify);
    }

    @Override
    protected boolean computeSyncOnModify() {
        return syncOnModify && syncCodec != null;
    }

    @Override
    @StandardAPI
    public ChunkStateEntry<T> register() {
        return (ChunkStateEntry<T>) super.register();
    }
}
