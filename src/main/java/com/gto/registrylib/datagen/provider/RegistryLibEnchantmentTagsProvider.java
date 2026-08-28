package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.ProviderType;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.LogicalSide;

import java.util.concurrent.CompletableFuture;

/**
 * Tag provider for data-driven enchantments.
 *
 * <p>
 * Enchantments are data-driven and have no {@code builtInRegistryHolder}. This provider extends
 * {@link EnchantmentTagsProvider} (backed by {@code KeyTagProvider}).
 */
public class RegistryLibEnchantmentTagsProvider extends EnchantmentTagsProvider
                                                implements RegistryLibTagsProvider<Enchantment> {

    private final RegistryCore owner;
    private final ProviderType<RegistryLibEnchantmentTagsProvider> type;

    public RegistryLibEnchantmentTagsProvider(
                                              RegistryCore owner,
                                              ProviderType<RegistryLibEnchantmentTagsProvider> type,
                                              PackOutput output,
                                              CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, owner.getModid(), null);
        this.owner = owner;
        this.type = type;
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        owner.genData(type, this);
    }

    public TagsProvider.TagAppender<Enchantment> tag(TagKey<Enchantment> key) {
        return super.tag(key);
    }

    @Override
    public TagBuilder rawBuilder(TagKey<Enchantment> key) {
        return super.getOrCreateRawBuilder(key);
    }

    @Override
    public CompletableFuture<TagsProvider.TagLookup<Enchantment>> contentsGetter() {
        return super.contentsGetter();
    }

    @Override
    public CompletableFuture<HolderLookup.Provider> getFilledProvider() {
        return createContentsProvider();
    }

    @Override
    public ResourceKey<? extends net.minecraft.core.Registry<Enchantment>> registry() {
        return registryKey;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public String getName() {
        return "Tags (enchantments)";
    }
}
