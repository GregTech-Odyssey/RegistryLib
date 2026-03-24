package com.gto.registrylib.util.entry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 附魔注册条目，封装了 ResourceKey&lt;Enchantment&gt;。
 *
 * <p>Wraps a {@link ResourceKey} for a data-driven enchantment. Since enchantments in NeoForge 26.1
 * are fully data-driven, this entry holds only the key (no code-registered object).
 */
public class EnchantmentEntry {

    private final ResourceKey<Enchantment> key;

    public EnchantmentEntry(ResourceKey<Enchantment> key) {
        this.key = key;
    }

    /** Returns the ResourceKey identifying this enchantment. */
    public ResourceKey<Enchantment> getKey() {
        return key;
    }
}
