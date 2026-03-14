package com.gto.registrylib.providers;

import com.gto.registrylib.RegistryLib;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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

public abstract class RegistryLibLangProvider extends LanguageProvider
    implements RegistryLibLangEntryProvider {

  protected final RegistryLib owner;

  protected RegistryLibLangProvider(RegistryLib owner, PackOutput packOutput, String locale) {
    super(packOutput, owner.getModid(), locale);
    this.owner = owner;
  }

  @Override
  public LogicalSide getSide() {
    return LogicalSide.CLIENT;
  }

  @Override
  public void add(@Nullable String key, @Nullable String value) {
    super.add(key, value);
  }

  public static String toEnglishName(String internalName) {
    return Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_"))
        .map(StringUtils::capitalize)
        .collect(Collectors.joining(" "));
  }

  // ===== en_us / en_ud =====

  public static class EnUs extends RegistryLibLangProvider {

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

    private final AccessibleLanguageProvider upsideDown;

    public EnUs(RegistryLib owner, PackOutput packOutput) {
      super(owner, packOutput, "en_us");
      this.upsideDown = new AccessibleLanguageProvider(packOutput, owner.getModid(), "en_ud");
    }

    @Override
    public String getName() {
      return "Lang (en_us/en_ud)";
    }

    @Override
    protected void addTranslations() {
      owner.genData(ProviderType.LANG_EN_US, this);
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
      upsideDown.add(key, toUpsideDown(value));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
      return CompletableFuture.allOf(super.run(cache), upsideDown.run(cache));
    }

    private static final String NORMAL_CHARS =
        "abcdefghijklmnopqrstuvwxyz"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "0123456789"
            + "_,;.?!/\\'";
    private static final String UPSIDE_DOWN_CHARS =
        "\u0250q\u0254p\u01DD\u025Fb\u0265\u0131\u0638\u029E\u05DF\u026Fuuodb\u0279s\u0287n\u028C\u028Dx\u028Ez"
            + "\u2C6F\u15FA\u0186\u15E1\u018E\u2132\u2141HI\u017F\u029E\uA780WNO\u0500\u1F49\u1D1AS\u27D8\u2229\u039BMX\u028EZ"
            + "0\u0196\u1105\u0190\u3123\u03DB9\u312586"
            + "\u203E'\u061B\u02D9\u00BF\u00A1/\\,";

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

  // ===== zh_cn =====

  public static class ZhCn extends RegistryLibLangProvider {

    public ZhCn(RegistryLib owner, PackOutput packOutput) {
      super(owner, packOutput, "zh_cn");
    }

    @Override
    public String getName() {
      return "Lang (zh_cn)";
    }

    @Override
    protected void addTranslations() {
      owner.genData(ProviderType.LANG_ZH_CN, this);
    }
  }

  // ===== ru_ru =====

  public static class RuRu extends RegistryLibLangProvider {

    public RuRu(RegistryLib owner, PackOutput packOutput) {
      super(owner, packOutput, "ru_ru");
    }

    @Override
    public String getName() {
      return "Lang (ru_ru)";
    }

    @Override
    protected void addTranslations() {
      owner.genData(ProviderType.LANG_RU_RU, this);
    }
  }

  // ===== ja_jp =====

  public static class JaJp extends RegistryLibLangProvider {

    public JaJp(RegistryLib owner, PackOutput packOutput) {
      super(owner, packOutput, "ja_jp");
    }

    @Override
    public String getName() {
      return "Lang (ja_jp)";
    }

    @Override
    protected void addTranslations() {
      owner.genData(ProviderType.LANG_JA_JP, this);
    }
  }
}
