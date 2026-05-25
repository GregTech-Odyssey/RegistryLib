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
import java.util.function.UnaryOperator;

public abstract class AbstractStateBuilder<T, P, S extends AbstractStateBuilder<T, P, S>> {

    protected final RegistryCore core;
    protected final P parent;
    protected final String name;
    protected final Codec<T> codec;
    protected final Function<IAttachmentHolder, T> defaultValueFactory;
    protected StateDebugConfig debugConfig = StateDebugConfig.disabled();
    protected StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec;
    protected boolean registered;

    protected AbstractStateBuilder(
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

    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    @StandardAPI
    public S debug() {
        this.debugConfig = StateDebugConfig.createEnabled();
        return self();
    }

    @StandardAPI
    public S debug(UnaryOperator<StateDebugConfig> config) {
        this.debugConfig = StateDebugConfig.createEnabled().configure(config);
        return self();
    }

    @StandardAPI
    public S sync(StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec) {
        this.syncCodec = syncCodec;
        return self();
    }

    protected abstract String attachmentPrefix();

    protected abstract AbstractStateEntry<T> createEntry(
                                                         Identifier identifier,
                                                         Codec<T> codec,
                                                         AttachmentTypeEntry<T> attachment,
                                                         StateDebugConfig debugConfig,
                                                         boolean syncOnModify);

    protected boolean computeSyncOnModify() {
        return syncCodec != null;
    }

    @StandardAPI
    public AbstractStateEntry<T> register() {
        if (registered) {
            throw new IllegalStateException("Builder already registered: " + name);
        }
        registered = true;
        var attachmentBuilder = core.attachmentType(attachmentPrefix() + name, defaultValueFactory)
                .serialize(codec.fieldOf("value"));
        if (syncCodec != null) {
            attachmentBuilder.sync(syncCodec);
        }
        AttachmentTypeEntry<T> attachment = attachmentBuilder.register();
        AbstractStateEntry<T> entry = createEntry(
                Identifier.fromNamespaceAndPath(core.getModid(), name),
                codec,
                attachment,
                debugConfig,
                computeSyncOnModify());
        core.registerStateEntry(entry);
        return entry;
    }

    @StandardAPI
    public P build() {
        register();
        return parent;
    }
}
