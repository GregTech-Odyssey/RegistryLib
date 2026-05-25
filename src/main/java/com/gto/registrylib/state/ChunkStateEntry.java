package com.gto.registrylib.state;

import com.gto.registrylib.util.entry.AttachmentTypeEntry;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.Optional;
import java.util.function.Consumer;

public final class ChunkStateEntry<T> extends AbstractStateEntry<T> {

    public ChunkStateEntry(
                           Identifier identifier,
                           Codec<T> codec,
                           AttachmentTypeEntry<T> attachment,
                           StateDebugConfig debugConfig,
                           boolean syncOnModify) {
        super(identifier, codec, attachment, debugConfig, syncOnModify);
    }

    @Override
    public StateScope scope() {
        return StateScope.CHUNK;
    }

    public T getOrCreate(ServerLevel level, ChunkPos pos) {
        return attachment.getOrCreate(level.getChunk(pos.x(), pos.z()));
    }

    public Optional<T> getIfLoaded(ServerLevel level, ChunkPos pos) {
        ChunkAccess chunk = level.getChunk(pos.x(), pos.z(), ChunkStatus.FULL, false);
        if (chunk == null) return Optional.empty();
        return attachment.getIfPresent(chunk);
    }

    public void modify(ServerLevel level, ChunkPos pos, Consumer<T> action) {
        ChunkAccess chunk = level.getChunk(pos.x(), pos.z());
        action.accept(attachment.getOrCreate(chunk));
        chunk.markUnsaved();
        if (syncOnModify) {
            chunk.syncData(attachment.get());
        }
    }

    public void set(ServerLevel level, ChunkPos pos, T value) {
        ChunkAccess chunk = level.getChunk(pos.x(), pos.z());
        attachment.set(chunk, value);
        chunk.markUnsaved();
        if (syncOnModify) {
            chunk.syncData(attachment.get());
        }
    }

    public void sync(ServerLevel level, ChunkPos pos) {
        level.getChunk(pos.x(), pos.z()).syncData(attachment.get());
    }
}
