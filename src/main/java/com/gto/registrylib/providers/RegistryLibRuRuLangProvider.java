package com.gto.registrylib.providers;

import com.gto.registrylib.RegistryLib;
import javax.annotation.Nullable;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class RegistryLibRuRuLangProvider extends LanguageProvider implements RegistryLibLangEntryProvider {

  private final RegistryLib owner;

  public RegistryLibRuRuLangProvider(RegistryLib owner, PackOutput packOutput) {
    super(packOutput, owner.getModid(), "ru_ru");
    this.owner = owner;
  }

  @Override
  public LogicalSide getSide() {
    return LogicalSide.CLIENT;
  }

  @Override
  public String getName() {
    return "Lang (ru_ru)";
  }

  @Override
  public void add(@Nullable String key, @Nullable String value) {
    super.add(key, value);
  }

  @Override
  protected void addTranslations() {
    owner.genData(ProviderType.LANG_RU_RU, this);
  }
}
