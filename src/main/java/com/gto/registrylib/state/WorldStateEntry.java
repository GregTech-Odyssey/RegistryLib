package com.gto.registrylib.state;

import com.gto.registrylib.util.entry.AttachmentTypeEntry;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;
import java.util.function.Consumer;

public final class WorldStateEntry<T> extends AbstractStateEntry<T> {

    public WorldStateEntry(
                           ResourceLocation identifier,
                           Codec<T> codec,
                           AttachmentTypeEntry<T> attachment,
                           StateDebugConfig debugConfig,
                           boolean syncOnModify) {
        super(identifier, codec, attachment, debugConfig, syncOnModify);
    }

    @Override
    public StateScope scope() {
        return StateScope.WORLD;
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
