package com.gto.registrylibtest.enchantment;

import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 最简单的附魔注册示例：矿石财运（Ore Fortune）。
 *
 * <p>Simple enchantment registration example. Enchantments in NeoForge 1.21+ are data-driven — the
 * enchantment definition lives in a JSON file at {@code data/registrylibtest/enchantment/ore_fortune.json}.
 *
 * <p>This class only defines the {@link ResourceKey} for referencing the enchantment in code, and
 * registers the lang entries via RegistryLib. No custom effect component is needed; vanilla's
 * built-in {@code minecraft:block_experience} effect component is used.
 *
 * <p>附魔定义位于 JSON 数据文件中。本类仅提供 ResourceKey 引用和语言条目注册。 使用原版 block_experience 效果组件增加挖矿经验。
 */
public class SimpleEnchantmentExample {

    /** 附魔的 ResourceKey，用于在代码中引用此附魔。 */
    public static final ResourceKey<Enchantment> ORE_FORTUNE = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(RegistryLibTest.MOD_ID, "ore_fortune"));

    /** 触发静态初始化并注册语言条目。 */
    public static void register() {
        // 英文
        RegistryLibTest.REGISTRYLIB.addLang(
                "enchantment",
                Identifier.fromNamespaceAndPath(RegistryLibTest.MOD_ID, "ore_fortune"),
                "Ore Fortune");
        // 中文
        RegistryLibTest.REGISTRYLIB.addDataGenerator(
                ModRegistryCore.LANG_ZH_CN,
                prov -> prov.add("enchantment.registrylibtest.ore_fortune", "矿石财运"));
    }
}
