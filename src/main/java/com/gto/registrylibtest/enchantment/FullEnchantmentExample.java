package com.gto.registrylibtest.enchantment;

import java.util.List;

import com.gto.registrylib.util.entry.RegistryEntry;
import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 完整的附魔注册示例：自动熔炼（Auto Smelt）。
 *
 * <p>Full enchantment registration example. Demonstrates:
 *
 * <ul>
 *   <li>Defining a custom enchantment effect record with {@link MapCodec};
 *   <li>Registering a {@link DataComponentType} for the custom effect via
 *       {@code RegistryCore.simple()};
 *   <li>Referencing the enchantment definition via {@link ResourceKey};
 *   <li>Registering lang entries for EN and ZH_CN.
 * </ul>
 *
 * <p>附魔定义位于 JSON 数据文件中（{@code data/registrylibtest/enchantment/auto_smelt.json}）。
 * 自定义效果组件 {@link AutoSmeltEffect} 注册到附魔效果组件类型注册表。 通过事件监听器读取附魔并将矿物掉落替换为熔炼产物。
 */
public class FullEnchantmentExample {

    // ── 自定义附魔效果 ─────────────────────────────────────────────────────

    /**
     * 自动熔炼效果数据：控制每级触发概率。
     *
     * @param chancePerLevel 每级附魔增加的触发概率（0.0 ~ 1.0）
     */
    public record AutoSmeltEffect(float chancePerLevel) {

        public static final MapCodec<AutoSmeltEffect> CODEC = RecordCodecBuilder.mapCodec(
                inst -> inst.group(
                                Codec.FLOAT
                                        .optionalFieldOf("chance_per_level", 1.0F)
                                        .forGetter(AutoSmeltEffect::chancePerLevel))
                        .apply(inst, AutoSmeltEffect::new));
    }

    // ── 自定义效果组件类型注册 ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static final RegistryEntry<DataComponentType<?>, DataComponentType<List<ConditionalEffect<AutoSmeltEffect>>>>
            AUTO_SMELT_EFFECT = (RegistryEntry) RegistryLibTest.REGISTRYLIB.simple(
                    "auto_smelt",
                    Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
                    key -> DataComponentType.<List<ConditionalEffect<AutoSmeltEffect>>>builder()
                            .persistent(ConditionalEffect.codec(AutoSmeltEffect.CODEC.codec()).listOf())
                            .build());

    // ── 附魔 ResourceKey ───────────────────────────────────────────────────

    /** 附魔的 ResourceKey，用于在代码中引用此附魔。 */
    public static final ResourceKey<Enchantment> AUTO_SMELT = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(RegistryLibTest.MOD_ID, "auto_smelt"));

    /** 触发静态初始化并注册语言条目。 */
    public static void register() {
        // 触发 AUTO_SMELT_EFFECT 静态初始化
        var _effect = AUTO_SMELT_EFFECT;

        // 英文
        RegistryLibTest.REGISTRYLIB.addLang(
                "enchantment",
                Identifier.fromNamespaceAndPath(RegistryLibTest.MOD_ID, "auto_smelt"),
                "Auto Smelt");
        // 中文
        RegistryLibTest.REGISTRYLIB.addDataGenerator(
                ModRegistryCore.LANG_ZH_CN,
                prov -> prov.add("enchantment.registrylibtest.auto_smelt", "自动熔炼"));
    }
}
