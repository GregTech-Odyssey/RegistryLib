package com.gto.registrylib.state;

import com.gto.registrylib.util.entry.AttachmentTypeEntry;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;

public abstract class AbstractStateEntry<T> implements StateEntry<T> {

    protected final ResourceLocation identifier;
    protected final Codec<T> codec;
    protected final AttachmentTypeEntry<T> attachment;
    protected final StateDebugConfig debugConfig;
    protected final boolean syncOnModify;

    protected AbstractStateEntry(
                                 ResourceLocation identifier,
                                 Codec<T> codec,
                                 AttachmentTypeEntry<T> attachment,
                                 StateDebugConfig debugConfig,
                                 boolean syncOnModify) {
        this.identifier = identifier;
        this.codec = codec;
        this.attachment = attachment;
        this.debugConfig = debugConfig;
        this.syncOnModify = syncOnModify;
    }

    @Override
    public ResourceLocation identifier() {
        return identifier;
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }

    @Override
    public AttachmentType<T> attachmentType() {
        return attachment.get();
    }

    @Override
    public StateDebugConfig debugConfig() {
        return debugConfig;
    }
}
