package com.gto.registrylibtest.enchantment;

import java.util.List;

import com.gto.registrylib.util.entry.EnchantmentEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
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
 *   <li>Using the {@code .enchantment()} fluent builder with {@code .withEffect()};
 *   <li>Registering lang entries for EN and ZH_CN.
 * </ul>
 *
 * <p>附魔定义 JSON 由 datagen 自动生成（{@code data/registrylibtest/enchantment/auto_smelt.json}）。
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

    // ── 附魔注册（定义 + lang + tag 一步完成） ─────────────────────────────

    /** 附魔注册条目：自动熔炼。 */
    public static final EnchantmentEntry AUTO_SMELT = RegistryLibTest.REGISTRYLIB
            .enchantment("auto_smelt")
            .lang("Auto Smelt")
            .lang(ModRegistryCore.LANG_ZH_CN, "自动熔炼")
            .supportedItems(ItemTags.MINING_ENCHANTABLE)
            .weight(2)
            .maxLevel(1)
            .minCost(25, 25)
            .maxCost(75, 25)
            .anvilCost(8)
            .slots(EquipmentSlotGroup.MAINHAND)
            .addTag(EnchantmentTags.IN_ENCHANTING_TABLE)
            .withEffect(AUTO_SMELT_EFFECT, new AutoSmeltEffect(1.0f))
            .register();
}
