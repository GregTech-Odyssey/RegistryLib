package com.gto.registrylib.providers;

import com.gto.registrylib.RegistryLib;
import javax.annotation.Nullable;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class RegistryLibJaJpLangProvider extends LanguageProvider implements RegistryLibLangEntryProvider {

  private final RegistryLib owner;

  public RegistryLibJaJpLangProvider(RegistryLib owner, PackOutput packOutput) {
    super(packOutput, owner.getModid(), "ja_jp");
    this.owner = owner;
  }

  @Override
  public LogicalSide getSide() {
    return LogicalSide.CLIENT;
  }

  @Override
  public String getName() {
    return "Lang (ja_jp)";
  }

  @Override
  public void add(@Nullable String key, @Nullable String value) {
    super.add(key, value);
  }

  @Override
  protected void addTranslations() {
    owner.genData(ProviderType.LANG_JA_JP, this);
  }
}
