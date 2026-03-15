package com.gto.registrylib.providers;

import com.gto.registrylib.RegistryCore;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.LanguageProvider;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class RegistryLibLangProvider extends LanguageProvider implements RegistryLibProvider {

    private static class AccessibleLanguageProvider extends LanguageProvider {

        public AccessibleLanguageProvider(PackOutput packOutput, String modid, String locale) {
            super(packOutput, modid, locale);
        }

        @Override
        public void add(@Nullable String key, @Nullable String value) {
            super.add(key, value);
        }

        @Override
        protected void addTranslations() {}
    }

    private final RegistryCore owner;

    public RegistryLibLangProvider(RegistryCore owner, PackOutput packOutput) {
        super(packOutput, owner.getModid(), "en_us");
        this.owner = owner;
    }

    /**
     * Constructor for custom-locale lang providers. The upside-down companion is not generated for
     * non-English locales.
     */
    protected RegistryLibLangProvider(RegistryCore owner, PackOutput packOutput, String locale) {
        super(packOutput, owner.getModid(), locale);
        this.owner = owner;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public String getName() {
        return "Lang (" + super.getName().replaceFirst(".*\\.", "") + ")";
    }

    @Override
    protected void addTranslations() {
        owner.genData(getProviderType(), this);
    }

    /**
     * Returns the {@link ProviderType} this provider is registered under. Subclasses must override
     * this to return their own ProviderType so that {@link #addTranslations()} drives the correct set
     * of registered callbacks.
     */
    protected ProviderType<? extends RegistryLibLangProvider> getProviderType() {
        return ProviderType.LANG;
    }

    public static String toEnglishName(String internalName) {
        return Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }

    @SuppressWarnings("unchecked")
    public <T> String getAutomaticName(
                                       Supplier<? extends T> sup, ResourceKey<? extends Registry<T>> registry) {
        return toEnglishName(
                ((Registry<Registry<T>>) BuiltInRegistries.REGISTRY)
                        .getValue(registry.identifier())
                        .getKey(sup.get())
                        .getPath());
    }

    public void addBlock(Supplier<? extends Block> block) {
        addBlock(block, getAutomaticName(block, Registries.BLOCK));
    }

    public void addItem(Supplier<? extends Item> item) {
        addItem(item, getAutomaticName(item, Registries.ITEM));
    }

    @Override
    public void add(String key, String value) {
        super.add(key, value);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return super.run(cache);
    }
}
