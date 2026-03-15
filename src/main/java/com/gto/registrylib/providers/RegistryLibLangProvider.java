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
    @Nullable
    private final AccessibleLanguageProvider upsideDown;

    public RegistryLibLangProvider(RegistryCore owner, PackOutput packOutput) {
        super(packOutput, owner.getModid(), "en_us");
        this.owner = owner;
        this.upsideDown = owner.isUpsideDownLangEnabled()
                ? new AccessibleLanguageProvider(packOutput, owner.getModid(), "en_ud")
                : null;
    }

    /**
     * Constructor for custom-locale lang providers.
     * The upside-down companion is not generated for non-English locales.
     */
    protected RegistryLibLangProvider(RegistryCore owner, PackOutput packOutput, String locale) {
        super(packOutput, owner.getModid(), locale);
        this.owner = owner;
        this.upsideDown = null;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public String getName() {
        return upsideDown != null ? "Lang (en_us/en_ud)" : "Lang (" + super.getName().replaceFirst(".*\\.", "") + ")";
    }

    @Override
    protected void addTranslations() {
        owner.genData(getProviderType(), this);
    }

    /**
     * Returns the {@link ProviderType} this provider is registered under.
     * Subclasses must override this to return their own ProviderType so that
     * {@link #addTranslations()} drives the correct set of registered callbacks.
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
        if (upsideDown != null) {
            upsideDown.add(key, toUpsideDown(value));
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        if (upsideDown != null) {
            return CompletableFuture.allOf(super.run(cache), upsideDown.run(cache));
        }
        return super.run(cache);
    }

    private static final String NORMAL_CHARS = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "_,;.?!/\\'";
    private static final String UPSIDE_DOWN_CHARS = "\u0250q\u0254p\u01DD\u025Fb\u0265\u0131\u0638\u029E\u05DF\u026Fuuodb\u0279s\u0287n\u028C\u028Dx\u028Ez" + "\u2C6F\u15FA\u0186\u15E1\u018E\u2132\u2141HI\u017F\u029E\uA780WNO\u0500\u1F49\u1D1AS\u27D8\u2229\u039BMX\u028EZ" + "0\u0196\u1105\u0190\u3123\u03DB9\u312586" + "\u203E'\u061B\u02D9\u00BF\u00A1/\\,";

    private String toUpsideDown(String normal) {
        char[] ud = new char[normal.length()];
        for (int i = 0; i < normal.length(); i++) {
            char c = normal.charAt(i);
            int lookup = NORMAL_CHARS.indexOf(c);
            if (lookup >= 0) {
                c = UPSIDE_DOWN_CHARS.charAt(lookup);
            }
            ud[normal.length() - 1 - i] = c;
        }
        return new String(ud);
    }

}

