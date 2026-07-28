package com.gto.registrylib.util.entry;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AttachmentTypeEntry<T> extends RegistryEntry<AttachmentType<?>, AttachmentType<T>> {

    public AttachmentTypeEntry(ResourceKey<AttachmentType<?>> key) {
        super(key);
    }

    public T getOrCreate(IAttachmentHolder holder) {
        return holder.getData(get());
    }

    public Optional<T> getIfPresent(IAttachmentHolder holder) {
        return holder.getExistingData(get());
    }

    @Nullable
    public T set(IAttachmentHolder holder, T value) {
        return holder.setData(get(), value);
    }

    @Nullable
    public T remove(IAttachmentHolder holder) {
        return holder.removeData(get());
    }

    public void sync(IAttachmentHolder holder) {
        holder.syncData(get());
    }
}
