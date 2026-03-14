package com.gto.registrylib.providers;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegistryLibItemTagsProvider extends RegistryLibTagsProvider.IntrinsicImpl<Item> {

    private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
    private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap<>();

    public RegistryLibItemTagsProvider(
                                       RegistryCore owner,
                                       ProviderType<RegistryLibItemTagsProvider> type,
                                       String name,
                                       PackOutput output,
                                       CompletableFuture<HolderLookup.Provider> registriesLookup,
                                       CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
        super(
                owner,
                type,
                name,
                output,
                Registries.ITEM,
                registriesLookup,
                item -> item.builtInRegistryHolder().key());
        this.blockTags = blockTags;
    }

    public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        this.tagsToCopy.put(blockTag, itemTag);
    }

    @Override
    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return super.createContentsProvider()
                .thenCombineAsync(
                        this.blockTags,
                        (provider, blockTagLookup) -> {
                            this.tagsToCopy.forEach(
                                    (blockTagKey, itemTagKey) -> {
                                        TagBuilder tagbuilder = this.getOrCreateRawBuilder(itemTagKey);
                                        Optional<TagBuilder> optional = blockTagLookup.apply(blockTagKey);
                                        optional
                                                .orElseThrow(
                                                        () -> new IllegalStateException(
                                                                "Missing block tag " + itemTagKey.location()))
                                                .build()
                                                .forEach(tagbuilder::add);
                                    });
                            return provider;
                        });
    }
}
