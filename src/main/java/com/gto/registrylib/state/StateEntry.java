package com.gto.registrylib.state;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;

public interface StateEntry<T> {

    ResourceLocation identifier();

    StateScope scope();

    Codec<T> codec();

    AttachmentType<T> attachmentType();

    StateDebugConfig debugConfig();
}
