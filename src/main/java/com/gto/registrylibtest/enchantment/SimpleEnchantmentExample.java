package com.gto.registrylibtest.enchantment;

import com.gto.registrylib.util.entry.EnchantmentEntry;
import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;

/**
 * 最简单的附魔注册示例：矿石财运（Ore Fortune）。
 *
 * <p>Simple enchantment registration example. Enchantments in NeoForge 1.21+ are data-driven — the
 * enchantment definition JSON is generated automatically during datagen via the builder.
 *
 * <p>本类使用 {@code .enchantment()} 流畅 API，一次性定义附魔属性、语言条目和标签。
 * 无需手写 JSON 或手动注册 ResourceKey。 使用原版 block_experience 效果组件增加挖矿经验。
 */
public class SimpleEnchantmentExample {

    /** 附魔注册条目：矿石财运。 */
    public static final EnchantmentEntry ORE_FORTUNE = RegistryLibTest.REGISTRYLIB
            .enchantment("ore_fortune")
            .lang("Ore Fortune")
            .lang(ModRegistryCore.LANG_ZH_CN, "矿石财运")
            .supportedItems(ItemTags.MINING_ENCHANTABLE)
            .weight(5)
            .maxLevel(3)
            .minCost(15, 9)
            .maxCost(65, 9)
            .anvilCost(4)
            .slots(EquipmentSlotGroup.MAINHAND)
            .addTag(EnchantmentTags.IN_ENCHANTING_TABLE)
            .withEffect(EnchantmentEffectComponents.BLOCK_EXPERIENCE,
                    new AddValue(LevelBasedValue.perLevel(1.0f, 1.0f)))
            .register();
}
