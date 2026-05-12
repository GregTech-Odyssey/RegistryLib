package com.gto.registrylib.state;

import com.gto.registrylib.util.entry.AttachmentTypeEntry;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.Optional;
import java.util.function.Consumer;

public final class WorldStateEntry<T> implements StateEntry<T> {

    private final Identifier identifier;
    private final Codec<T> codec;
    private final AttachmentTypeEntry<T> attachment;
    private final StateDebugConfig debugConfig;
    private final boolean syncOnModify;

    public WorldStateEntry(
                           Identifier identifier,
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
    public Identifier identifier() {
        return identifier;
    }

    @Override
    public StateScope scope() {
        return StateScope.WORLD;
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

    public T getOrCreate(ServerLevel level) {
        return attachment.getOrCreate(level);
    }

    public Optional<T> getIfPresent(ServerLevel level) {
        return attachment.getIfPresent(level);
    }

    public void modify(ServerLevel level, Consumer<T> action) {
        action.accept(getOrCreate(level));
        if (syncOnModify) {
            sync(level);
        }
    }

    public void set(ServerLevel level, T value) {
        attachment.set(level, value);
        if (syncOnModify) {
            sync(level);
        }
    }

    public void sync(ServerLevel level) {
        level.syncData(attachment.get());
    }
}
