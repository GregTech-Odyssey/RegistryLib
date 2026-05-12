package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.util.entry.AttachmentTypeEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class AttachmentTypeBuilder<T, P>
                                     extends AbstractBuilder<AttachmentType<?>, AttachmentType<T>, P, AttachmentTypeBuilder<T, P>> {

    private final Function<IAttachmentHolder, T> defaultValueFactory;
    private UnaryOperator<AttachmentType.Builder<T>> builderCallback = b -> b;

    public static <T, P> AttachmentTypeBuilder<T, P> create(
                                                            RegistryCore owner,
                                                            P parent,
                                                            String name,
                                                            Function<IAttachmentHolder, T> defaultValueFactory) {
        return new AttachmentTypeBuilder<>(owner, parent, name, defaultValueFactory);
    }

    protected AttachmentTypeBuilder(
                                    RegistryCore core,
                                    P parent,
                                    String name,
                                    Function<IAttachmentHolder, T> defaultValueFactory) {
        super(core, parent, name, NeoForgeRegistries.Keys.ATTACHMENT_TYPES);
        this.defaultValueFactory = defaultValueFactory;
    }

    @StandardAPI
    public AttachmentTypeBuilder<T, P> configure(@NotNull UnaryOperator<AttachmentType.Builder<T>> callback) {
        UnaryOperator<AttachmentType.Builder<T>> previous = builderCallback;
        builderCallback = builder -> callback.apply(previous.apply(builder));
        return this;
    }

    @StandardAPI
    public AttachmentTypeBuilder<T, P> serialize(@NotNull MapCodec<T> codec) {
        return configure(b -> b.serialize(codec));
    }

    @StandardAPI
    public AttachmentTypeBuilder<T, P> sync(
                                            @NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return configure(b -> b.sync(streamCodec));
    }

    @Override
    protected AttachmentType<T> createEntry(ResourceKey<AttachmentType<?>> key) {
        return builderCallback.apply(AttachmentType.builder(defaultValueFactory)).build();
    }

    @Override
    protected RegistryEntry<AttachmentType<?>, AttachmentType<T>> createEntryWrapper(
                                                                                     ResourceKey<AttachmentType<?>> key) {
        return new AttachmentTypeEntry<>(key);
    }

    @Override
    public AttachmentTypeEntry<T> register() {
        return (AttachmentTypeEntry<T>) super.register();
    }
}
