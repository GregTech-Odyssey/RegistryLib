package com.gto.registrylib.state;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;

public interface StateEntry<T> {

    Identifier identifier();

    StateScope scope();

    Codec<T> codec();

    AttachmentType<T> attachmentType();

    StateDebugConfig debugConfig();
}
